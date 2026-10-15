package com.fpoly.tiemsachgiay.fragment;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fpoly.tiemsachgiay.R;
import com.fpoly.tiemsachgiay.adapter.KhachHangAdapter;
import com.fpoly.tiemsachgiay.dao.KhachHangDAO;
import com.fpoly.tiemsachgiay.model.KhachHang;

import java.util.ArrayList;
import java.util.List;

public class KhachHangManageFragment extends Fragment {

    private RecyclerView rvKhachHang;
    private ProgressBar pbLoading;
    private LinearLayout layoutEmpty;
    private TextView tvEmptyMessage;
    private EditText edtSearch;

    private KhachHangDAO khachHangDAO;
    private KhachHangAdapter adapter;

    private final List<KhachHang> fullCustomerList = new ArrayList<>();
    private final List<KhachHang> filteredCustomerList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_khach_hang_manage, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        khachHangDAO = new KhachHangDAO(requireContext());

        initViews(view);
        setupRecyclerView();
        setupSearchListener();

        // Load danh sách khách hàng từ Firestore
        loadCustomersFromFirestore();
    }

    private void initViews(View view) {
        rvKhachHang = view.findViewById(R.id.rv_khach_hang);
        pbLoading = view.findViewById(R.id.pb_loading_kh);
        layoutEmpty = view.findViewById(R.id.layout_empty_kh);
        tvEmptyMessage = view.findViewById(R.id.tv_empty_message_kh);
        edtSearch = view.findViewById(R.id.edt_search_kh);
    }

    private void setupRecyclerView() {
        rvKhachHang.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new KhachHangAdapter(requireContext(), filteredCustomerList, this::showDeleteConfirmation);
        rvKhachHang.setAdapter(adapter);
    }

    private void setupSearchListener() {
        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                applySearch();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void loadCustomersFromFirestore() {
        pbLoading.setVisibility(View.VISIBLE);
        rvKhachHang.setVisibility(View.GONE);
        layoutEmpty.setVisibility(View.GONE);

        khachHangDAO.getAll(new KhachHangDAO.OnDataListLoadedListener<KhachHang>() {
            @Override
            public void onSuccess(List<KhachHang> data) {
                if (!isAdded()) return;
                pbLoading.setVisibility(View.GONE);
                fullCustomerList.clear();
                if (data != null) {
                    fullCustomerList.addAll(data);
                }
                applySearch();
            }

            @Override
            public void onFailure(String message) {
                if (!isAdded()) return;
                pbLoading.setVisibility(View.GONE);
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
                fullCustomerList.clear();
                applySearch();
            }
        });
    }

    private void applySearch() {
        String query = edtSearch.getText() != null ? edtSearch.getText().toString().trim().toLowerCase() : "";

        filteredCustomerList.clear();

        for (KhachHang kh : fullCustomerList) {
            boolean matchesSearch = query.isEmpty()
                    || (kh.getTenKH() != null && kh.getTenKH().toLowerCase().contains(query))
                    || (kh.getSdt() != null && kh.getSdt().contains(query))
                    || (kh.getTaiKhoan() != null && kh.getTaiKhoan().toLowerCase().contains(query));

            if (matchesSearch) {
                filteredCustomerList.add(kh);
            }
        }

        adapter.setListKhachHang(filteredCustomerList);

        if (filteredCustomerList.isEmpty()) {
            rvKhachHang.setVisibility(View.GONE);
            layoutEmpty.setVisibility(View.VISIBLE);
            if (!query.isEmpty()) {
                tvEmptyMessage.setText("Không tìm thấy khách hàng nào phù hợp");
            } else {
                tvEmptyMessage.setText("Chưa có khách hàng nào trong danh sách");
            }
        } else {
            rvKhachHang.setVisibility(View.VISIBLE);
            layoutEmpty.setVisibility(View.GONE);
        }
    }

    private void showDeleteConfirmation(KhachHang khachHang) {
        if (khachHang == null) return;

        String key = khachHang.getSdt() != null && !khachHang.getSdt().isEmpty() ? khachHang.getSdt() : khachHang.getTaiKhoan();
        if (key == null || key.isEmpty()) return;

        String displayName = khachHang.getTenKH() != null ? khachHang.getTenKH() : key;

        new AlertDialog.Builder(requireContext())
                .setTitle("Xác nhận xóa khách hàng")
                .setMessage("Bạn có chắc chắn muốn xóa khách hàng \"" + displayName + "\" (SĐT: " + key + ") không?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    Toast.makeText(requireContext(), "Đang xóa khách hàng...", Toast.LENGTH_SHORT).show();
                    khachHangDAO.delete(key, new KhachHangDAO.OnActionListener() {
                        @Override
                        public void onSuccess() {
                            if (!isAdded()) return;
                            Toast.makeText(requireContext(), "Xóa khách hàng thành công!", Toast.LENGTH_SHORT).show();
                            fullCustomerList.remove(khachHang);
                            applySearch();
                            loadCustomersFromFirestore();
                        }

                        @Override
                        public void onFailure(String message) {
                            if (!isAdded()) return;
                            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}