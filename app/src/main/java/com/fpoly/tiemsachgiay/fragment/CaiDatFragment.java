package com.fpoly.tiemsachgiay.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.fpoly.tiemsachgiay.LoginActivity;
import com.fpoly.tiemsachgiay.R;
import com.google.android.material.button.MaterialButton;

public class CaiDatFragment extends Fragment {

    private TextView tvName, tvInfo;
    private MaterialButton btnChangePass, btnLogout;

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
        btnChangePass = view.findViewById(R.id.btn_change_password);
        btnLogout = view.findViewById(R.id.btn_cai_dat_logout);

        if (getActivity() != null && getActivity().getIntent() != null) {
            Intent intent = getActivity().getIntent();
            String userName = intent.getStringExtra("user_name");
            String userId = intent.getStringExtra("user_id");
            String userType = intent.getStringExtra("user_type");

            if (userName != null) {
                tvName.setText(userName);
            }
            if (userId != null) {
                String rolePrefix = "KHACH_HANG".equals(userType) ? "Tài khoản Khách hàng: " : "Mã Nhân viên: ";
                tvInfo.setText(rolePrefix + userId);
            }
        }

        btnChangePass.setOnClickListener(v -> 
            Toast.makeText(requireContext(), "Tính năng đổi mật khẩu đang cập nhật", Toast.LENGTH_SHORT).show()
        );

        btnLogout.setOnClickListener(v -> {
            Intent loginIntent = new Intent(requireActivity(), LoginActivity.class);
            startActivity(loginIntent);
            requireActivity().finish();
        });
    }
}
