package com.fpoly.tiemsachgiay.fragment;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fpoly.tiemsachgiay.DoanhThuActivity;
import com.fpoly.tiemsachgiay.R;
import com.fpoly.tiemsachgiay.adapter.HoaDonAdapter;
import com.fpoly.tiemsachgiay.dao.HoaDonDAO;
import com.fpoly.tiemsachgiay.model.HoaDon;
import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class HoaDonFragment extends Fragment {

    private RecyclerView rvHoaDon;
    private ProgressBar pbLoading;
    private LinearLayout layoutEmpty;

    private HoaDonDAO hoaDonDAO;
    private HoaDonAdapter adapter;

    private final List<HoaDon> listHoaDon = new ArrayList<>();
    private final String[] statusOptions = {"Đã thanh toán", "Chưa thanh toán", "Đã hủy"};

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_hoa_don, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        hoaDonDAO = new HoaDonDAO(requireContext());

        initViews(view);
        setupRecyclerView();

        loadInvoicesFromFirestore();
    }

    private void initViews(View view) {
        rvHoaDon = view.findViewById(R.id.rv_hoa_don);
        pbLoading = view.findViewById(R.id.pb_loading_hoa_don);
        layoutEmpty = view.findViewById(R.id.layout_empty_hoa_don);

        MaterialButton btnViewDoanhThu = view.findViewById(R.id.btn_view_doanh_thu);
        btnViewDoanhThu.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), DoanhThuActivity.class);
            startActivity(intent);
        });

        View fabAdd = view.findViewById(R.id.fab_add_hoa_don);
        fabAdd.setOnClickListener(v -> showAddInvoiceDialog());
    }

    private void setupRecyclerView() {
        rvHoaDon.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new HoaDonAdapter(requireContext(), listHoaDon);
        rvHoaDon.setAdapter(adapter);
    }

    private void loadInvoicesFromFirestore() {
        pbLoading.setVisibility(View.VISIBLE);
        rvHoaDon.setVisibility(View.GONE);
        layoutEmpty.setVisibility(View.GONE);

        hoaDonDAO.getAll(new HoaDonDAO.OnDataListLoadedListener<HoaDon>() {
            @Override
            public void onSuccess(List<HoaDon> data) {
                if (!isAdded()) return;
                pbLoading.setVisibility(View.GONE);
                listHoaDon.clear();
                if (data != null && !data.isEmpty()) {
                    listHoaDon.addAll(data);
                    rvHoaDon.setVisibility(View.VISIBLE);
                    layoutEmpty.setVisibility(View.GONE);
                } else {
                    rvHoaDon.setVisibility(View.GONE);
                    layoutEmpty.setVisibility(View.VISIBLE);
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(String message) {
                if (!isAdded()) return;
                pbLoading.setVisibility(View.GONE);
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
                listHoaDon.clear();
                adapter.notifyDataSetChanged();
                rvHoaDon.setVisibility(View.GONE);
                layoutEmpty.setVisibility(View.VISIBLE);
            }
        });
    }

    private void showAddInvoiceDialog() {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_hoa_don, null);

        EditText edtTenKH = dialogView.findViewById(R.id.edt_dialog_ten_kh_hd);
        EditText edtSdtKH = dialogView.findViewById(R.id.edt_dialog_sdt_kh_hd);
        EditText edtTongTien = dialogView.findViewById(R.id.edt_dialog_tong_tien_hd);
        Spinner spTrangThai = dialogView.findViewById(R.id.sp_dialog_trang_thai_hd);
        MaterialButton btnCancel = dialogView.findViewById(R.id.btn_dialog_cancel_hd);
        MaterialButton btnSave = dialogView.findViewById(R.id.btn_dialog_save_hd);

        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, statusOptions);
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spTrangThai.setAdapter(statusAdapter);

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .setCancelable(false)
                .create();

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnSave.setOnClickListener(v -> {
            String tenKH = edtTenKH.getText() != null ? edtTenKH.getText().toString().trim() : "";
            String sdtKH = edtSdtKH.getText() != null ? edtSdtKH.getText().toString().trim() : "";
            String strTongTien = edtTongTien.getText() != null ? edtTongTien.getText().toString().trim() : "";
            String trangThai = spTrangThai.getSelectedItem() != null ? spTrangThai.getSelectedItem().toString() : "Đã thanh toán";

            if (tenKH.isEmpty() || strTongTien.isEmpty()) {
                Toast.makeText(requireContext(), "Vui lòng nhập Tên khách hàng và Tổng tiền (*)", Toast.LENGTH_SHORT).show();
                return;
            }

            double tongTien;
            try {
                String clean = strTongTien.replaceAll("[^0-9]", "");
                tongTien = Double.parseDouble(clean);
            } catch (Exception e) {
                Toast.makeText(requireContext(), "Tổng tiền không hợp lệ!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (tongTien < 0) {
                Toast.makeText(requireContext(), "Tổng tiền không được âm!", Toast.LENGTH_SHORT).show();
                return;
            }

            Calendar cal = Calendar.getInstance();
            SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm", Locale.getDefault());

            String ngayLap = sdfDate.format(cal.getTime());
            String gioLap = sdfTime.format(cal.getTime());
            String maHD = "hd_" + System.currentTimeMillis();

            HoaDon newHoaDon = new HoaDon(maHD, "admin", "Quản lý Tiệm Sách", tenKH, sdtKH, ngayLap, gioLap, tongTien, trangThai);

            btnSave.setEnabled(false);
            Toast.makeText(requireContext(), "Đang tạo hóa đơn mới...", Toast.LENGTH_SHORT).show();

            hoaDonDAO.insert(newHoaDon, new HoaDonDAO.OnActionListener() {
                @Override
                public void onSuccess() {
                    if (!isAdded()) return;
                    btnSave.setEnabled(true);
                    Toast.makeText(requireContext(), "Tạo hóa đơn thành công!", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                    loadInvoicesFromFirestore();
                }

                @Override
                public void onFailure(String message) {
                    if (!isAdded()) return;
                    btnSave.setEnabled(true);
                    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
                }
            });
        });

        dialog.show();
    }
}