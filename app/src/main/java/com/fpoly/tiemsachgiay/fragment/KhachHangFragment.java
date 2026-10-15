package com.fpoly.tiemsachgiay.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.fpoly.tiemsachgiay.MainActivity;
import com.fpoly.tiemsachgiay.R;
import com.fpoly.tiemsachgiay.dao.KhachHangDAO;
import com.fpoly.tiemsachgiay.model.KhachHang;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.textfield.TextInputEditText;

public class KhachHangFragment extends Fragment {

    // Views Toggle & Containers
    private MaterialButtonToggleGroup toggleGroup;
    private LinearLayout layoutLogin, layoutRegister;

    // Login Views
    private TextInputEditText edtTaiKhoanKh, edtMatKhauKh;
    private CheckBox chkRememberKh;
    private MaterialButton btnLoginKh;
    private TextView tvSwitchToRegister;

    // Register Views (Chỉ gồm: Họ tên - SĐT - Mật khẩu - Nhập lại mật khẩu)
    private TextInputEditText edtRegHoTen, edtRegSdt, edtRegMatKhau, edtRegNhapLaiMatKhau;
    private MaterialButton btnRegisterKh;
    private TextView tvSwitchToLogin;

    // DAO & Prefs
    private KhachHangDAO khachHangDAO;
    private SharedPreferences pref;

    private static final String PREF_NAME = "KhachHangPref";
    private static final String KEY_ACCOUNT = "account";
    private static final String KEY_PASS = "pass";
    private static final String KEY_REMEMBER = "remember";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_khach_hang, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        khachHangDAO = new KhachHangDAO(requireContext());
        pref = requireContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        initViews(view);
        setupListeners();
        loadSavedLoginInfo();
    }

    private void initViews(View view) {
        toggleGroup = view.findViewById(R.id.toggle_group_kh);
        layoutLogin = view.findViewById(R.id.layout_login_kh);
        layoutRegister = view.findViewById(R.id.layout_register_kh);

        // Login Views
        edtTaiKhoanKh = view.findViewById(R.id.edt_tai_khoan_kh);
        edtMatKhauKh = view.findViewById(R.id.edt_mat_khau_kh);
        chkRememberKh = view.findViewById(R.id.chk_remember_kh);
        btnLoginKh = view.findViewById(R.id.btn_login_kh);
        tvSwitchToRegister = view.findViewById(R.id.tv_switch_to_register);

        // Register Views
        edtRegHoTen = view.findViewById(R.id.edt_reg_ho_ten);
        edtRegSdt = view.findViewById(R.id.edt_reg_sdt);
        edtRegMatKhau = view.findViewById(R.id.edt_reg_mat_khau);
        edtRegNhapLaiMatKhau = view.findViewById(R.id.edt_reg_nhap_lai_mat_khau);
        btnRegisterKh = view.findViewById(R.id.btn_register_kh);
        tvSwitchToLogin = view.findViewById(R.id.tv_switch_to_login);
    }

    private void setupListeners() {
        toggleGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                if (checkedId == R.id.btn_toggle_login) {
                    showLoginLayout();
                } else if (checkedId == R.id.btn_toggle_register) {
                    showRegisterLayout();
                }
            }
        });

        tvSwitchToRegister.setOnClickListener(v -> toggleGroup.check(R.id.btn_toggle_register));
        tvSwitchToLogin.setOnClickListener(v -> toggleGroup.check(R.id.btn_toggle_login));

        btnLoginKh.setOnClickListener(v -> performLogin());
        btnRegisterKh.setOnClickListener(v -> performRegister());
    }

    private void showLoginLayout() {
        layoutLogin.setVisibility(View.VISIBLE);
        layoutRegister.setVisibility(View.GONE);
    }

    private void showRegisterLayout() {
        layoutLogin.setVisibility(View.GONE);
        layoutRegister.setVisibility(View.VISIBLE);
    }

    private void loadSavedLoginInfo() {
        boolean isRemember = pref.getBoolean(KEY_REMEMBER, false);
        if (isRemember) {
            edtTaiKhoanKh.setText(pref.getString(KEY_ACCOUNT, ""));
            edtMatKhauKh.setText(pref.getString(KEY_PASS, ""));
            chkRememberKh.setChecked(true);
        }
    }

    private void performLogin() {
        String account = edtTaiKhoanKh.getText() != null ? edtTaiKhoanKh.getText().toString().trim() : "";
        String matKhau = edtMatKhauKh.getText() != null ? edtMatKhauKh.getText().toString().trim() : "";

        if (account.isEmpty() || matKhau.isEmpty()) {
            Toast.makeText(requireContext(), getString(R.string.msg_empty_fields), Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(requireContext(), "Đang đăng nhập...", Toast.LENGTH_SHORT).show();

        khachHangDAO.checkLogin(account, matKhau, new KhachHangDAO.OnLoginListener() {
            @Override
            public void onSuccess(KhachHang khachHang) {
                if (!isAdded()) return;
                SharedPreferences.Editor editor = pref.edit();
                if (chkRememberKh.isChecked()) {
                    editor.putString(KEY_ACCOUNT, account);
                    editor.putString(KEY_PASS, matKhau);
                    editor.putBoolean(KEY_REMEMBER, true);
                } else {
                    editor.clear();
                }
                editor.apply();

                Toast.makeText(requireContext(), getString(R.string.msg_login_success) + " Chào " + khachHang.getTenKH(), Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(requireActivity(), MainActivity.class);
                intent.putExtra("user_type", "KHACH_HANG");
                intent.putExtra("user_name", khachHang.getTenKH());
                intent.putExtra("user_id", khachHang.getSdt() != null ? khachHang.getSdt() : khachHang.getTaiKhoan());
                startActivity(intent);
                requireActivity().finish();
            }

            @Override
            public void onFailure(String message) {
                if (!isAdded()) return;
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void performRegister() {
        String hoTen = edtRegHoTen.getText() != null ? edtRegHoTen.getText().toString().trim() : "";
        String sdt = edtRegSdt.getText() != null ? edtRegSdt.getText().toString().trim() : "";
        String matKhau = edtRegMatKhau.getText() != null ? edtRegMatKhau.getText().toString().trim() : "";
        String nhapLaiMatKhau = edtRegNhapLaiMatKhau.getText() != null ? edtRegNhapLaiMatKhau.getText().toString().trim() : "";

        if (hoTen.isEmpty() || sdt.isEmpty() || matKhau.isEmpty() || nhapLaiMatKhau.isEmpty()) {
            Toast.makeText(requireContext(), getString(R.string.msg_empty_fields), Toast.LENGTH_SHORT).show();
            return;
        }

        if (!matKhau.equals(nhapLaiMatKhau)) {
            Toast.makeText(requireContext(), getString(R.string.msg_password_mismatch), Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(requireContext(), "Đang xử lý đăng ký...", Toast.LENGTH_SHORT).show();

        KhachHang newKhachHang = new KhachHang(hoTen, sdt, matKhau);
        newKhachHang.setTaiKhoan(sdt);

        khachHangDAO.register(newKhachHang, new KhachHangDAO.OnRegisterListener() {
            @Override
            public void onSuccess() {
                if (!isAdded()) return;
                Toast.makeText(requireContext(), getString(R.string.msg_register_success), Toast.LENGTH_LONG).show();
                edtTaiKhoanKh.setText(sdt);
                edtMatKhauKh.setText(matKhau);

                toggleGroup.check(R.id.btn_toggle_login);

                edtRegHoTen.setText("");
                edtRegSdt.setText("");
                edtRegMatKhau.setText("");
                edtRegNhapLaiMatKhau.setText("");
            }

            @Override
            public void onFailure(String message) {
                if (!isAdded()) return;
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }
}