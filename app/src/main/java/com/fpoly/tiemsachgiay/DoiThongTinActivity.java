package com.fpoly.tiemsachgiay;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.fpoly.tiemsachgiay.dao.KhachHangDAO;
import com.fpoly.tiemsachgiay.model.KhachHang;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

/**
 * Màn hình Đổi thông tin cá nhân dành riêng cho Khách hàng.
 * Cho phép đổi họ tên, số điện thoại (tài khoản đăng nhập đổi tương ứng) và địa chỉ.
 */
public class DoiThongTinActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private TextInputEditText edtTen, edtSdt, edtDiaChi;
    private MaterialButton btnLuu;

    private KhachHangDAO khachHangDAO;
    private FirebaseFirestore db;
    private KhachHang currentKhachHang;

    private String userId = "";
    private String userName = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_doi_thong_tin);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.layout_doi_thong_tin_root), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });

        khachHangDAO = new KhachHangDAO();
        db = FirebaseFirestore.getInstance();

        Intent intent = getIntent();
        if (intent != null) {
            userId = intent.getStringExtra("user_id") != null ? intent.getStringExtra("user_id") : "";
            userName = intent.getStringExtra("user_name") != null ? intent.getStringExtra("user_name") : "";
        }

        initViews();
        loadCustomerProfile();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar_doi_thong_tin);
        edtTen = findViewById(R.id.edt_doi_ten);
        edtSdt = findViewById(R.id.edt_doi_sdt);
        edtDiaChi = findViewById(R.id.edt_doi_dia_chi);
        btnLuu = findViewById(R.id.btn_luu_thong_tin);

        toolbar.setNavigationOnClickListener(v -> finish());
        btnLuu.setOnClickListener(v -> saveProfileChanges());

        if (!userName.isEmpty()) {
            edtTen.setText(userName);
        }
        if (!userId.isEmpty()) {
            edtSdt.setText(userId);
        }
    }

    private void loadCustomerProfile() {
        if (userId.isEmpty()) return;

        khachHangDAO.getById(userId, new KhachHangDAO.OnDataLoadedListener<KhachHang>() {
            @Override
            public void onSuccess(KhachHang kh) {
                currentKhachHang = kh;
                if (kh != null) {
                    if (kh.getTenKH() != null && !kh.getTenKH().isEmpty()) {
                        edtTen.setText(kh.getTenKH());
                    }
                    String phone = kh.getSdt() != null && !kh.getSdt().isEmpty() ? kh.getSdt() : kh.getTaiKhoan();
                    if (phone != null && !phone.isEmpty()) {
                        edtSdt.setText(phone);
                    }
                    if (kh.getDiaChi() != null) {
                        edtDiaChi.setText(kh.getDiaChi());
                    }
                }
            }

            @Override
            public void onFailure(String message) {
                // Nếu chưa có trên Firestore, giữ dữ liệu từ Intent
            }
        });
    }

    private void saveProfileChanges() {
        String newTen = edtTen.getText() != null ? edtTen.getText().toString().trim() : "";
        String newSdt = edtSdt.getText() != null ? edtSdt.getText().toString().trim() : "";
        String newDiaChi = edtDiaChi.getText() != null ? edtDiaChi.getText().toString().trim() : "";

        if (newTen.isEmpty()) {
            edtTen.setError("Vui lòng nhập họ và tên khách hàng");
            edtTen.requestFocus();
            return;
        }

        if (newSdt.isEmpty()) {
            edtSdt.setError("Vui lòng nhập số điện thoại");
            edtSdt.requestFocus();
            return;
        }

        if (newSdt.length() < 9 || !newSdt.matches("\\d+")) {
            edtSdt.setError("Số điện thoại không hợp lệ (ít nhất 9 chữ số)");
            edtSdt.requestFocus();
            return;
        }

        btnLuu.setEnabled(false);
        Toast.makeText(this, "Đang cập nhật thông tin...", Toast.LENGTH_SHORT).show();

        String oldSdt = currentKhachHang != null && currentKhachHang.getSdt() != null && !currentKhachHang.getSdt().isEmpty()
                ? currentKhachHang.getSdt()
                : userId;

        // Nếu số điện thoại KHÔNG đổi, chỉ cần cập nhật Document hiện tại
        if (newSdt.equals(oldSdt)) {
            if (currentKhachHang == null) {
                currentKhachHang = new KhachHang(newTen, newSdt, "123456");
            }
            currentKhachHang.setTenKH(newTen);
            currentKhachHang.setDiaChi(newDiaChi);

            khachHangDAO.update(currentKhachHang, new KhachHangDAO.OnActionListener() {
                @Override
                public void onSuccess() {
                    btnLuu.setEnabled(true);
                    Toast.makeText(DoiThongTinActivity.this, "Cập nhật thông tin thành công!", Toast.LENGTH_SHORT).show();
                    finishWithResult(newTen, newSdt);
                }

                @Override
                public void onFailure(String message) {
                    btnLuu.setEnabled(true);
                    Toast.makeText(DoiThongTinActivity.this, "Lỗi cập nhật: " + message, Toast.LENGTH_SHORT).show();
                }
            });
            return;
        }

        // Nếu số điện thoại ĐỔI: kiểm tra số mới đã có tài khoản khác dùng chưa
        db.collection("KhachHang").document(newSdt).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                btnLuu.setEnabled(true);
                edtSdt.setError("Số điện thoại này đã được tài khoản khác sử dụng!");
                edtSdt.requestFocus();
            } else {
                // Tạo record mới với key là newSdt
                String matKhau = currentKhachHang != null && currentKhachHang.getMatKhau() != null
                        ? currentKhachHang.getMatKhau()
                        : "123456";

                Map<String, Object> map = new HashMap<>();
                map.put("tenKH", newTen);
                map.put("taiKhoan", newSdt); // taiKhoan dùng sđt nên đổi tương ứng
                map.put("matKhau", matKhau);
                map.put("sdt", newSdt);
                map.put("diaChi", newDiaChi);

                db.collection("KhachHang").document(newSdt).set(map)
                        .addOnSuccessListener(aVoid -> {
                            // Xóa document cũ trên Firestore
                            if (!oldSdt.isEmpty()) {
                                db.collection("KhachHang").document(oldSdt).delete();
                            }

                            btnLuu.setEnabled(true);
                            Toast.makeText(DoiThongTinActivity.this,
                                    "🎉 Đổi thông tin thành công! Tài khoản đăng nhập mới: " + newSdt,
                                    Toast.LENGTH_LONG).show();
                            finishWithResult(newTen, newSdt);
                        })
                        .addOnFailureListener(e -> {
                            btnLuu.setEnabled(true);
                            Toast.makeText(DoiThongTinActivity.this,
                                    "Lỗi lưu thông tin mới: " + e.getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        });
            }
        }).addOnFailureListener(e -> {
            btnLuu.setEnabled(true);
            Toast.makeText(DoiThongTinActivity.this,
                    "Lỗi kiểm tra số điện thoại: " + e.getMessage(),
                    Toast.LENGTH_SHORT).show();
        });
    }

    private void finishWithResult(String newTen, String newSdt) {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("new_user_name", newTen);
        resultIntent.putExtra("new_user_id", newSdt);
        setResult(RESULT_OK, resultIntent);
        finish();
    }
}
