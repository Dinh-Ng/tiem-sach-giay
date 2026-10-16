package com.fpoly.tiemsachgiay.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.fpoly.tiemsachgiay.DoiMatKhauActivity;
import com.fpoly.tiemsachgiay.DoiThongTinActivity;
import com.fpoly.tiemsachgiay.LoginActivity;
import com.fpoly.tiemsachgiay.R;
import com.google.android.material.button.MaterialButton;

public class CaiDatFragment extends Fragment {

    private TextView tvName, tvInfo;
    private MaterialButton btnDoiThongTin, btnChangePass, btnLogout;
    private View dividerDoiThongTin;

    private String userName = "";
    private String userId = "";
    private String userType = "";

    // Launcher để nhận kết quả khi khách hàng đổi thông tin cá nhân thành công
    private final ActivityResultLauncher<Intent> doiThongTinLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    String newName = result.getData().getStringExtra("new_user_name");
                    String newId = result.getData().getStringExtra("new_user_id");

                    if (newName != null && !newName.isEmpty()) {
                        userName = newName;
                        tvName.setText(userName);
                        if (getActivity() != null && getActivity().getIntent() != null) {
                            getActivity().getIntent().putExtra("user_name", userName);
                        }
                    }

                    if (newId != null && !newId.isEmpty()) {
                        userId = newId;
                        String rolePrefix = "KHACH_HANG".equals(userType) ? "Tài khoản Khách hàng: " : "Mã Nhân viên: ";
                        tvInfo.setText(rolePrefix + userId);
                        if (getActivity() != null && getActivity().getIntent() != null) {
                            getActivity().getIntent().putExtra("user_id", userId);
                        }
                    }
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_cai_dat, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvName = view.findViewById(R.id.tv_cai_dat_account_name);
        tvInfo = view.findViewById(R.id.tv_cai_dat_account_info);
        btnDoiThongTin = view.findViewById(R.id.btn_doi_thong_tin);
        dividerDoiThongTin = view.findViewById(R.id.divider_doi_thong_tin);
        btnChangePass = view.findViewById(R.id.btn_change_password);
        btnLogout = view.findViewById(R.id.btn_cai_dat_logout);

        if (getActivity() != null && getActivity().getIntent() != null) {
            Intent intent = getActivity().getIntent();
            userName = intent.getStringExtra("user_name") != null ? intent.getStringExtra("user_name") : "";
            userId = intent.getStringExtra("user_id") != null ? intent.getStringExtra("user_id") : "";
            userType = intent.getStringExtra("user_type") != null ? intent.getStringExtra("user_type") : "";

            if (!userName.isEmpty()) {
                tvName.setText(userName);
            }
            if (!userId.isEmpty()) {
                String rolePrefix = "KHACH_HANG".equals(userType) ? "Tài khoản Khách hàng: " : "Mã Nhân viên: ";
                tvInfo.setText(rolePrefix + userId);
            }
        }

        // Nhân viên chỉ được phép đổi mật khẩu. Khách hàng được phép đổi cả thông tin cá nhân.
        boolean isKhachHang = "KHACH_HANG".equalsIgnoreCase(userType);
        if (isKhachHang) {
            btnDoiThongTin.setVisibility(View.VISIBLE);
            dividerDoiThongTin.setVisibility(View.VISIBLE);
            btnDoiThongTin.setOnClickListener(v -> {
                Intent intent = new Intent(requireContext(), DoiThongTinActivity.class);
                intent.putExtra("user_id", userId);
                intent.putExtra("user_name", userName);
                doiThongTinLauncher.launch(intent);
            });
        } else {
            // Nhân viên: ẩn tính năng đổi thông tin cá nhân
            btnDoiThongTin.setVisibility(View.GONE);
            dividerDoiThongTin.setVisibility(View.GONE);
        }

        // Đổi mật khẩu: dùng màn hình riêng biệt cho cả Khách hàng và Nhân viên
        btnChangePass.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), DoiMatKhauActivity.class);
            intent.putExtra("user_id", userId);
            intent.putExtra("user_type", userType);
            intent.putExtra("user_name", userName);
            startActivity(intent);
        });

        // Đăng xuất
        btnLogout.setOnClickListener(v -> {
            Intent loginIntent = new Intent(requireActivity(), LoginActivity.class);
            startActivity(loginIntent);
            requireActivity().finish();
        });
    }
}
