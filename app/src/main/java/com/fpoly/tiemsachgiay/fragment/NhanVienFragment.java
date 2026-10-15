package com.fpoly.tiemsachgiay.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.fpoly.tiemsachgiay.MainActivity;
import com.fpoly.tiemsachgiay.R;
import com.fpoly.tiemsachgiay.dao.NhanVienDAO;
import com.fpoly.tiemsachgiay.model.NhanVien;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class NhanVienFragment extends Fragment {

    private TextInputEditText edtMaNV, edtMatKhau;
    private CheckBox chkRemember;
    private MaterialButton btnLogin;

    private NhanVienDAO nhanVienDAO;
    private SharedPreferences pref;

    private static final String PREF_NAME = "NhanVienPref";
    private static final String KEY_MA_NV = "maNV";
    private static final String KEY_PASS = "pass";
    private static final String KEY_REMEMBER = "remember";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_nhan_vien, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        nhanVienDAO = new NhanVienDAO(requireContext());
        pref = requireContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        edtMaNV = view.findViewById(R.id.edt_ma_nv);
        edtMatKhau = view.findViewById(R.id.edt_mat_khau_nv);
        chkRemember = view.findViewById(R.id.chk_remember_nv);
        btnLogin = view.findViewById(R.id.btn_login_nv);

        // Load saved credentials if any
        boolean isRemember = pref.getBoolean(KEY_REMEMBER, false);
        if (isRemember) {
            edtMaNV.setText(pref.getString(KEY_MA_NV, ""));
            edtMatKhau.setText(pref.getString(KEY_PASS, ""));
            chkRemember.setChecked(true);
        }

        btnLogin.setOnClickListener(v -> performLogin());
    }

    private void performLogin() {
        String maNV = edtMaNV.getText() != null ? edtMaNV.getText().toString().trim() : "";
        String matKhau = edtMatKhau.getText() != null ? edtMatKhau.getText().toString().trim() : "";

        if (maNV.isEmpty() || matKhau.isEmpty()) {
            Toast.makeText(requireContext(), getString(R.string.msg_empty_fields), Toast.LENGTH_SHORT).show();
            return;
        }

        nhanVienDAO.checkLogin(maNV, matKhau, new NhanVienDAO.OnLoginListener() {
            @Override
            public void onSuccess(NhanVien nhanVien) {
                if (!isAdded()) return;
                SharedPreferences.Editor editor = pref.edit();
                if (chkRemember.isChecked()) {
                    editor.putString(KEY_MA_NV, maNV);
                    editor.putString(KEY_PASS, matKhau);
                    editor.putBoolean(KEY_REMEMBER, true);
                } else {
                    editor.clear();
                }
                editor.apply();

                Toast.makeText(requireContext(), getString(R.string.msg_login_success) + " Chào " + nhanVien.getHoTen(), Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(requireActivity(), MainActivity.class);
                intent.putExtra("user_type", "NHAN_VIEN");
                intent.putExtra("user_name", nhanVien.getHoTen());
                intent.putExtra("user_id", nhanVien.getMaNV());
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
}