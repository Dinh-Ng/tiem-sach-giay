package com.fpoly.tiemsachgiay;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fpoly.tiemsachgiay.adapter.TomTatDonHangAdapter;
import com.fpoly.tiemsachgiay.dao.DonHangDAO;
import com.fpoly.tiemsachgiay.dao.KhachHangDAO;
import com.fpoly.tiemsachgiay.model.DonHang;
import com.fpoly.tiemsachgiay.model.GioHang;
import com.fpoly.tiemsachgiay.model.KhachHang;
import com.fpoly.tiemsachgiay.model.Sach;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Màn hình Chọn địa chỉ và Phương thức thanh toán khi Khách hàng đặt mua sách.
 */
public class ThanhToanActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private TextInputEditText edtTen, edtSdt, edtDiaChi, edtGhiChu;
    private RadioGroup rgPhuongThucThanhToan;
    private RadioButton rbCod, rbChuyenKhoan, rbViDienTu;
    private TextView tvHuongDanThanhToan;
    private TextView tvTieuDeSanPham, tvTienHang, tvTongTien, tvBottomTongTien;
    private RecyclerView rvSanPham;
    private MaterialButton btnXacNhanDatHang;

    private DonHangDAO donHangDAO;
    private KhachHangDAO khachHangDAO;
    private final DecimalFormat formatter = new DecimalFormat("#,### ₫");

    private String userId = "";
    private String userName = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_thanh_toan);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.layout_thanh_toan_root), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });

        donHangDAO = new DonHangDAO();
        khachHangDAO = new KhachHangDAO();

        // Nhận thông tin truyền từ Intent
        Intent intent = getIntent();
        if (intent != null) {
            userId = intent.getStringExtra("user_id") != null ? intent.getStringExtra("user_id") : "";
            userName = intent.getStringExtra("user_name") != null ? intent.getStringExtra("user_name") : "";
        }

        initViews();
        setupToolbar();
        setupOrderSummary();
        setupPaymentMethodEvents();
        prefillCustomerData();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar_thanh_toan);
        edtTen = findViewById(R.id.edt_thanh_toan_ten);
        edtSdt = findViewById(R.id.edt_thanh_toan_sdt);
        edtDiaChi = findViewById(R.id.edt_thanh_toan_dia_chi);
        edtGhiChu = findViewById(R.id.edt_thanh_toan_ghi_chu);

        rgPhuongThucThanhToan = findViewById(R.id.rg_phuong_thuc_thanh_toan);
        rbCod = findViewById(R.id.rb_thanh_toan_cod);
        rbChuyenKhoan = findViewById(R.id.rb_thanh_toan_chuyen_khoan);
        rbViDienTu = findViewById(R.id.rb_thanh_toan_vi_dien_tu);
        tvHuongDanThanhToan = findViewById(R.id.tv_huong_dan_thanh_toan);

        tvTieuDeSanPham = findViewById(R.id.tv_tieu_de_san_pham);
        tvTienHang = findViewById(R.id.tv_thanh_toan_tien_hang);
        tvTongTien = findViewById(R.id.tv_thanh_toan_tong_tien);
        tvBottomTongTien = findViewById(R.id.tv_bottom_tong_tien);
        rvSanPham = findViewById(R.id.rv_thanh_toan_san_pham);
        btnXacNhanDatHang = findViewById(R.id.btn_xac_nhan_dat_hang);

        btnXacNhanDatHang.setOnClickListener(v -> submitOrder());
    }

    private void setupToolbar() {
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void prefillCustomerData() {
        if (!userName.isEmpty()) {
            edtTen.setText(userName);
        }
        if (!userId.isEmpty()) {
            edtSdt.setText(userId);
            // Thử query hồ sơ khách hàng để lấy sẵn địa chỉ đã lưu
            khachHangDAO.getById(userId, new KhachHangDAO.OnDataLoadedListener<KhachHang>() {
                @Override
                public void onSuccess(KhachHang khachHang) {
                    if (khachHang != null) {
                        if (edtTen.getText() == null || edtTen.getText().toString().trim().isEmpty()) {
                            if (khachHang.getTenKH() != null && !khachHang.getTenKH().isEmpty()) {
                                edtTen.setText(khachHang.getTenKH());
                            }
                        }
                        if (khachHang.getDiaChi() != null && !khachHang.getDiaChi().trim().isEmpty()) {
                            edtDiaChi.setText(khachHang.getDiaChi());
                        }
                    }
                }

                @Override
                public void onFailure(String message) {
                    // Không lấy được profile thì để người dùng tự nhập
                }
            });
        }
    }

    private void setupOrderSummary() {
        List<GioHang> cartList = GioHang.getGioHangList();
        if (cartList.isEmpty()) {
            Toast.makeText(this, "Giỏ hàng trống!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        int totalItems = GioHang.getTotalItems();
        double totalPrice = GioHang.getTotalPrice();

        tvTieuDeSanPham.setText("📚 Sản phẩm đặt mua (" + totalItems + " món)");
        tvTienHang.setText(formatter.format(totalPrice));
        tvTongTien.setText(formatter.format(totalPrice));
        tvBottomTongTien.setText(formatter.format(totalPrice));

        rvSanPham.setLayoutManager(new LinearLayoutManager(this));
        TomTatDonHangAdapter adapter = new TomTatDonHangAdapter(this, cartList);
        rvSanPham.setAdapter(adapter);
    }

    private void setupPaymentMethodEvents() {
        rgPhuongThucThanhToan.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rb_thanh_toan_cod) {
                tvHuongDanThanhToan.setText("👉 Bạn sẽ thanh toán bằng tiền mặt trực tiếp cho nhân viên giao hàng khi nhận sách.");
            } else if (checkedId == R.id.rb_thanh_toan_chuyen_khoan) {
                tvHuongDanThanhToan.setText("👉 Vui lòng chuyển khoản đến STK Tiệm Sách Giấy (MBBank: 0912345678 - TIEM SACH GIAY) kèm mã đơn sau khi đặt.");
            } else if (checkedId == R.id.rb_thanh_toan_vi_dien_tu) {
                tvHuongDanThanhToan.setText("👉 Cửa hàng sẽ gửi thông tin mã QR MoMo / ZaloPay qua SĐT để bạn thanh toán tiện lợi.");
            }
        });
    }

    private void submitOrder() {
        String tenNhan = edtTen.getText() != null ? edtTen.getText().toString().trim() : "";
        String sdtNhan = edtSdt.getText() != null ? edtSdt.getText().toString().trim() : "";
        String diaChi = edtDiaChi.getText() != null ? edtDiaChi.getText().toString().trim() : "";
        String ghiChu = edtGhiChu.getText() != null ? edtGhiChu.getText().toString().trim() : "";

        // Validate
        if (tenNhan.isEmpty()) {
            edtTen.setError("Vui lòng nhập họ và tên người nhận");
            edtTen.requestFocus();
            return;
        }

        if (sdtNhan.isEmpty()) {
            edtSdt.setError("Vui lòng nhập số điện thoại nhận hàng");
            edtSdt.requestFocus();
            return;
        }

        if (sdtNhan.length() < 9 || !sdtNhan.matches("\\d+")) {
            edtSdt.setError("Số điện thoại không hợp lệ");
            edtSdt.requestFocus();
            return;
        }

        if (diaChi.isEmpty()) {
            edtDiaChi.setError("Vui lòng nhập địa chỉ nhận hàng chi tiết");
            edtDiaChi.requestFocus();
            return;
        }

        // Lấy phương thức thanh toán đã chọn
        String phuongThucThanhToan = "Tiền mặt khi nhận hàng (COD)";
        int checkedId = rgPhuongThucThanhToan.getCheckedRadioButtonId();
        if (checkedId == R.id.rb_thanh_toan_chuyen_khoan) {
            phuongThucThanhToan = "Chuyển khoản ngân hàng (VietQR)";
        } else if (checkedId == R.id.rb_thanh_toan_vi_dien_tu) {
            phuongThucThanhToan = "Ví điện tử (MoMo / ZaloPay)";
        }

        // Build danh sách items trong đơn
        List<DonHang.ItemDonHang> danhSachItem = new ArrayList<>();
        for (GioHang item : GioHang.getGioHangList()) {
            Sach sach = item.getSach();
            if (sach == null) continue;

            double donGia = (sach.getGiaGiam() > 0 && sach.getGiaGiam() < sach.getGiaGoc())
                    ? sach.getGiaGiam()
                    : sach.getGiaGoc();

            danhSachItem.add(new DonHang.ItemDonHang(
                    sach.getMaSach() != null ? sach.getMaSach() : "",
                    sach.getTenSach() != null ? sach.getTenSach() : "",
                    sach.getTacGia() != null ? sach.getTacGia() : "",
                    donGia,
                    item.getSoLuong()
            ));
        }

        if (danhSachItem.isEmpty()) {
            Toast.makeText(this, "Không có sản phẩm nào để đặt hàng!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Tạo đối tượng DonHang hoàn chỉnh
        DonHang donHang = new DonHang(
                userId.isEmpty() ? sdtNhan : userId,
                tenNhan,
                sdtNhan,
                diaChi,
                phuongThucThanhToan,
                ghiChu,
                System.currentTimeMillis(),
                GioHang.getTotalPrice(),
                "Chờ xử lý",
                danhSachItem
        );

        // Vô hiệu hóa nút và thông báo đang xử lý
        btnXacNhanDatHang.setEnabled(false);
        Toast.makeText(this, "Đang xử lý đặt hàng...", Toast.LENGTH_SHORT).show();

        final String finalPhuongThuc = phuongThucThanhToan;
        donHangDAO.insert(donHang, new DonHangDAO.OnActionListener() {
            @Override
            public void onSuccess(String maDonHang) {
                btnXacNhanDatHang.setEnabled(true);

                // Xóa giỏ hàng sau khi đặt thành công
                GioHang.clearCart();

                // Hiển thị dialog thông báo thành công
                new AlertDialog.Builder(ThanhToanActivity.this)
                        .setTitle("🎉 Đặt hàng thành công!")
                        .setMessage("Cảm ơn bạn đã đặt sách tại Tiệm Sách Giấy!\n\n"
                                + "• Mã đơn: " + maDonHang + "\n"
                                + "• Người nhận: " + tenNhan + " (" + sdtNhan + ")\n"
                                + "• Địa chỉ: " + diaChi + "\n"
                                + "• Thanh toán: " + finalPhuongThuc + "\n"
                                + "• Tổng tiền: " + formatter.format(donHang.getTongTien()))
                        .setCancelable(false)
                        .setPositiveButton("Về trang chủ", (dialog, which) -> {
                            setResult(RESULT_OK);
                            finish();
                        })
                        .show();
            }

            @Override
            public void onFailure(String message) {
                btnXacNhanDatHang.setEnabled(true);
                Toast.makeText(ThanhToanActivity.this, "Đặt hàng thất bại: " + message, Toast.LENGTH_LONG).show();
            }
        });
    }
}
