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
import com.fpoly.tiemsachgiay.fragment.HoaDonFragment;
import com.fpoly.tiemsachgiay.fragment.KhachHangManageFragment;
import com.fpoly.tiemsachgiay.fragment.SachFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private TextView tvHeaderUserInfo;

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

        if (userName != null && !userName.isEmpty()) {
            String roleText = "NHAN_VIEN".equalsIgnoreCase(userType) ? "Nhân viên: " : "Khách hàng: ";
            tvHeaderUserInfo.setText("Xin chào " + roleText + userName);
        }

        // Cấu hình Menu Bottom Navigation tương ứng theo loại tài khoản:
        // - Khách hàng: 2 tab (Sách - Cài đặt)
        // - Nhân viên: 4 tab (Sách - Hóa đơn - Khách hàng - Cài đặt)
        bottomNavigationView.getMenu().clear();
        if ("KHACH_HANG".equalsIgnoreCase(userType)) {
            bottomNavigationView.inflateMenu(R.menu.menu_khach_hang);
        } else {
            bottomNavigationView.inflateMenu(R.menu.menu_nhan_vien);
        }

        // Listener xử lý khi bấm chuyển Tab
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            Fragment selectedFragment = null;

            if (itemId == R.id.nav_sach) {
                selectedFragment = new SachFragment();
            } else if (itemId == R.id.nav_hoa_don) {
                selectedFragment = new HoaDonFragment();
            } else if (itemId == R.id.nav_khach_hang) {
                selectedFragment = new KhachHangManageFragment();
            } else if (itemId == R.id.nav_cai_dat) {
                selectedFragment = new CaiDatFragment();
            }

            if (selectedFragment != null) {
                replaceFragment(selectedFragment);
                return true;
            }
            return false;
        });

        // Mặc định hiển thị tab Sách khi vào Trang chủ
        if (savedInstanceState == null) {
            replaceFragment(new SachFragment());
            bottomNavigationView.setSelectedItemId(R.id.nav_sach);
        }
    }

    private void replaceFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
}