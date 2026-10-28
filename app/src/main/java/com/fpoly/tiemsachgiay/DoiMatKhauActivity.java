package com.fpoly.tiemsachgiay;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.fpoly.tiemsachgiay.dao.KhachHangDAO;
import com.fpoly.tiemsachgiay.dao.NhanVienDAO;
import com.fpoly.tiemsachgiay.model.KhachHang;
import com.fpoly.tiemsachgiay.model.NhanVien;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

/**
 * Màn hình Đổi mật khẩu dùng riêng biệt cho cả Khách hàng và Nhân viên.
 * Yêu cầu nhập mật khẩu cũ, mật khẩu mới và xác nhận lại mật khẩu mới.
 */
public class DoiMatKhauActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private TextView tvUserInfo;
    private TextInputEditText edtMkCu, edtMkMoi, edtNhapLaiMkMoi;
    private MaterialButton btnXacNhan;

    private KhachHangDAO khachHangDAO;
    private NhanVienDAO nhanVienDAO;

    private String userId = "";
    private String userType = "KHACH_HANG";
    private String userName = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_doi_mat_khau);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.layout_doi_mat_khau_root), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });

        khachHangDAO = new KhachHangDAO();
        nhanVienDAO = new NhanVienDAO();

        Intent intent = getIntent();
        if (intent != null) {
            userId = intent.getStringExtra("user_id") != null ? intent.getStringExtra("user_id") : "";
            userType = intent.getStringExtra("user_type") != null ? intent.getStringExtra("user_type") : "KHACH_HANG";
            userName = intent.getStringExtra("user_name") != null ? intent.getStringExtra("user_name") : "";
        }

        initViews();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar_doi_mat_khau);
        tvUserInfo = findViewById(R.id.tv_doi_mk_user_info);
        edtMkCu = findViewById(R.id.edt_mk_cu);
        edtMkMoi = findViewById(R.id.edt_mk_moi);
        edtNhapLaiMkMoi = findViewById(R.id.edt_nhap_lai_mk_moi);
        btnXacNhan = findViewById(R.id.btn_xac_nhan_doi_mk);

        toolbar.setNavigationOnClickListener(v -> finish());

        if (!userId.isEmpty()) {
            tvUserInfo.setText("Tài khoản: " + userId + (userName.isEmpty() ? "" : " (" + userName + ")"));
        }

        btnXacNhan.setOnClickListener(v -> performChangePassword());
    }

    private void performChangePassword() {
        String mkCu = edtMkCu.getText() != null ? edtMkCu.getText().toString().trim() : "";
        String mkMoi = edtMkMoi.getText() != null ? edtMkMoi.getText().toString().trim() : "";
        String nhapLai = edtNhapLaiMkMoi.getText() != null ? edtNhapLaiMkMoi.getText().toString().trim() : "";

        // Validate
        if (mkCu.isEmpty()) {
            edtMkCu.setError("Vui lòng nhập mật khẩu hiện tại");
            edtMkCu.requestFocus();
            return;
        }

        if (mkMoi.isEmpty()) {
            edtMkMoi.setError("Vui lòng nhập mật khẩu mới");
            edtMkMoi.requestFocus();
            return;
        }

        if (mkMoi.length() < 6) {
            edtMkMoi.setError("Mật khẩu mới phải có ít nhất 6 ký tự");
            edtMkMoi.requestFocus();
            return;
        }

        if (mkMoi.equals(mkCu)) {
            edtMkMoi.setError("Mật khẩu mới không được trùng với mật khẩu cũ");
            edtMkMoi.requestFocus();
            return;
        }

        if (!mkMoi.equals(nhapLai)) {
            edtNhapLaiMkMoi.setError("Mật khẩu xác nhận không khớp!");
            edtNhapLaiMkMoi.requestFocus();
            return;
        }

        btnXacNhan.setEnabled(false);
        Toast.makeText(this, "Đang kiểm tra và đổi mật khẩu...", Toast.LENGTH_SHORT).show();

        if ("KHACH_HANG".equalsIgnoreCase(userType)) {
            changeCustomerPassword(mkCu, mkMoi);
        } else {
            changeEmployeePassword(mkCu, mkMoi);
        }
    }

    private void changeCustomerPassword(String mkCu, String mkMoi) {
        khachHangDAO.getById(userId, new KhachHangDAO.OnDataLoadedListener<KhachHang>() {
            @Override
            public void onSuccess(KhachHang khachHang) {
                if (khachHang == null) {
                    btnXacNhan.setEnabled(true);
                    Toast.makeText(DoiMatKhauActivity.this, "Không tìm thấy tài khoản!", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Kiểm tra mật khẩu cũ
                if (!mkCu.equals(khachHang.getMatKhau())) {
                    btnXacNhan.setEnabled(true);
                    edtMkCu.setError("Mật khẩu hiện tại không chính xác!");
                    edtMkCu.requestFocus();
                    return;
                }

                // Cập nhật mật khẩu mới
                khachHang.setMatKhau(mkMoi);
                khachHangDAO.update(khachHang, new KhachHangDAO.OnActionListener() {
                    @Override
                    public void onSuccess() {
                        btnXacNhan.setEnabled(true);
                        showSuccessDialog();
                    }

                    @Override
                    public void onFailure(String message) {
                        btnXacNhan.setEnabled(true);
                        Toast.makeText(DoiMatKhauActivity.this, "Đổi mật khẩu thất bại: " + message, Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onFailure(String message) {
                btnXacNhan.setEnabled(true);
                Toast.makeText(DoiMatKhauActivity.this, "Lỗi kết nối: " + message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void changeEmployeePassword(String mkCu, String mkMoi) {
        // Tài khoản test hardcode mẫu
        if ("admin".equalsIgnoreCase(userId) && "admin123".equals(mkCu)) {
            NhanVien admin = new NhanVien("admin", userName.isEmpty() ? "Quản lý" : userName, mkMoi, "Quản lý");
            nhanVienDAO.update(admin, new NhanVienDAO.OnActionListener() {
                @Override
                public void onSuccess() {
                    btnXacNhan.setEnabled(true);
                    showSuccessDialog();
                }

                @Override
                public void onFailure(String message) {
                    btnXacNhan.setEnabled(true);
                    Toast.makeText(DoiMatKhauActivity.this, "Đổi mật khẩu thất bại: " + message, Toast.LENGTH_SHORT).show();
                }
            });
            return;
        }

        if ("nv01".equalsIgnoreCase(userId) && "123456".equals(mkCu)) {
            NhanVien nv01 = new NhanVien("nv01", userName.isEmpty() ? "Nhân viên" : userName, mkMoi, "Thu ngân");
            nhanVienDAO.update(nv01, new NhanVienDAO.OnActionListener() {
                @Override
                public void onSuccess() {
                    btnXacNhan.setEnabled(true);
                    showSuccessDialog();
                }

                @Override
                public void onFailure(String message) {
                    btnXacNhan.setEnabled(true);
                    Toast.makeText(DoiMatKhauActivity.this, "Đổi mật khẩu thất bại: " + message, Toast.LENGTH_SHORT).show();
                }
            });
            return;
        }

        nhanVienDAO.getById(userId, new NhanVienDAO.OnDataLoadedListener<NhanVien>() {
            @Override
            public void onSuccess(NhanVien nhanVien) {
                if (nhanVien == null) {
                    btnXacNhan.setEnabled(true);
                    Toast.makeText(DoiMatKhauActivity.this, "Không tìm thấy thông tin nhân viên!", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Kiểm tra mật khẩu cũ
                if (!mkCu.equals(nhanVien.getMatKhau())) {
                    btnXacNhan.setEnabled(true);
                    edtMkCu.setError("Mật khẩu hiện tại không chính xác!");
                    edtMkCu.requestFocus();
                    return;
                }

                // Cập nhật mật khẩu mới
                nhanVien.setMatKhau(mkMoi);
                nhanVienDAO.update(nhanVien, new NhanVienDAO.OnActionListener() {
                    @Override
                    public void onSuccess() {
                        btnXacNhan.setEnabled(true);
                        showSuccessDialog();
                    }

                    @Override
                    public void onFailure(String message) {
                        btnXacNhan.setEnabled(true);
                        Toast.makeText(DoiMatKhauActivity.this, "Đổi mật khẩu thất bại: " + message, Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onFailure(String message) {
                btnXacNhan.setEnabled(true);
                Toast.makeText(DoiMatKhauActivity.this, "Lỗi kết nối: " + message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showSuccessDialog() {
        new AlertDialog.Builder(this)
                .setTitle("🎉 Thành công")
                .setMessage("Đổi mật khẩu thành công! Vui lòng ghi nhớ mật khẩu mới cho lần đăng nhập tiếp theo.")
                .setCancelable(false)
                .setPositiveButton("Đồng ý", (dialog, which) -> finish())
                .show();
    }
}
