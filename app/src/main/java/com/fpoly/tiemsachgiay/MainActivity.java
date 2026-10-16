package com.fpoly.tiemsachgiay;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.fpoly.tiemsachgiay.fragment.CaiDatFragment;
import com.fpoly.tiemsachgiay.fragment.GioHangFragment;
import com.fpoly.tiemsachgiay.fragment.HoaDonFragment;
import com.fpoly.tiemsachgiay.fragment.KhachHangManageFragment;
import com.fpoly.tiemsachgiay.fragment.SachFragment;
import com.fpoly.tiemsachgiay.fragment.SachKhachHangFragment;
import com.fpoly.tiemsachgiay.model.GioHang;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.badge.BadgeDrawable;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private TextView tvHeaderUserInfo;

    // Phân biệt loại tài khoản để điều hướng đúng
    private boolean isKhachHang = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });

        tvHeaderUserInfo = findViewById(R.id.tv_header_user_info);
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        Intent intent = getIntent();
        String userType = intent.getStringExtra("user_type");
        String userName = intent.getStringExtra("user_name");

        isKhachHang = "KHACH_HANG".equalsIgnoreCase(userType);

        if (userName != null && !userName.isEmpty()) {
            String roleText = isKhachHang ? "Khách hàng: " : "Nhân viên: ";
            tvHeaderUserInfo.setText("Xin chào " + roleText + userName);
        }

        // Cấu hình menu Bottom Navigation theo loại tài khoản:
        // - Khách hàng: Sách (mua) | Giỏ hàng | Cài đặt
        // - Nhân viên:  Sách (QL)  | Hóa đơn  | Khách hàng | Cài đặt
        bottomNavigationView.getMenu().clear();
        if (isKhachHang) {
            bottomNavigationView.inflateMenu(R.menu.menu_khach_hang);
        } else {
            bottomNavigationView.inflateMenu(R.menu.menu_nhan_vien);
        }

        // Listener xử lý khi bấm chuyển Tab
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            Fragment selectedFragment = null;

            if (isKhachHang) {
                // ── Luồng Khách hàng ──────────────────────────────
                if (itemId == R.id.nav_sach_kh) {
                    selectedFragment = new SachKhachHangFragment();
                } else if (itemId == R.id.nav_gio_hang) {
                    selectedFragment = new GioHangFragment();
                    // Xóa badge khi vào giỏ hàng
                    clearCartBadge();
                } else if (itemId == R.id.nav_cai_dat) {
                    selectedFragment = new CaiDatFragment();
                }
            } else {
                // ── Luồng Nhân viên ───────────────────────────────
                if (itemId == R.id.nav_sach) {
                    selectedFragment = new SachFragment();
                } else if (itemId == R.id.nav_hoa_don) {
                    selectedFragment = new HoaDonFragment();
                } else if (itemId == R.id.nav_khach_hang) {
                    selectedFragment = new KhachHangManageFragment();
                } else if (itemId == R.id.nav_cai_dat) {
                    selectedFragment = new CaiDatFragment();
                }
            }

            if (selectedFragment != null) {
                replaceFragment(selectedFragment);
                return true;
            }
            return false;
        });

        // Mặc định hiển thị tab Sách khi vào Trang chủ
        if (savedInstanceState == null) {
            if (isKhachHang) {
                replaceFragment(new SachKhachHangFragment());
                bottomNavigationView.setSelectedItemId(R.id.nav_sach_kh);
            } else {
                replaceFragment(new SachFragment());
                bottomNavigationView.setSelectedItemId(R.id.nav_sach);
            }
        }
    }

    private void replaceFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    /**
     * Cập nhật badge số lượng sản phẩm trên tab Giỏ hàng.
     * Được gọi từ SachKhachHangFragment và GioHangFragment.
     */
    public void updateCartBadge() {
        if (!isKhachHang) return;
        int count = GioHang.getTotalItems();
        try {
            if (count > 0) {
                BadgeDrawable badge = bottomNavigationView.getOrCreateBadge(R.id.nav_gio_hang);
                badge.setNumber(count);
                badge.setVisible(true);
            } else {
                bottomNavigationView.removeBadge(R.id.nav_gio_hang);
            }
        } catch (Exception ignored) {
            // BadgeDrawable có thể chưa sẵn sàng nếu gọi quá sớm
        }
    }

    /** Xóa badge khi người dùng vào xem giỏ hàng. */
    private void clearCartBadge() {
        try {
            bottomNavigationView.removeBadge(R.id.nav_gio_hang);
        } catch (Exception ignored) {
        }
    }

    /**
     * Điều hướng sang tab Giỏ Hàng từ bên ngoài (ví dụ: nhấn "Mua ngay" trong SachKhachHangFragment).
     */
    public void navigateToCartTab() {
        if (isKhachHang) {
            bottomNavigationView.setSelectedItemId(R.id.nav_gio_hang);
        }
    }
}