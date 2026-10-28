package com.fpoly.tiemsachgiay.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fpoly.tiemsachgiay.R;
import com.fpoly.tiemsachgiay.adapter.DonHangAdapter;
import com.fpoly.tiemsachgiay.dao.DonHangDAO;
import com.fpoly.tiemsachgiay.model.DonHang;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment Quản lý Đơn hàng dành cho Nhân viên (thay thế tab Hóa đơn).
 * Cho phép xem tất cả đơn hàng, lọc theo trạng thái, cập nhật trạng thái đơn và xóa đơn hàng.
 */
public class HoaDonFragment extends Fragment {

    private RecyclerView rvDonHang;
    private ProgressBar pbLoading;
    private LinearLayout layoutEmpty;
    private TextView tvCount;
    private ChipGroup chipGroupTrangThai;

    private DonHangDAO donHangDAO;
    private DonHangAdapter adapter;
    private final List<DonHang> allOrders = new ArrayList<>();
    private final List<DonHang> displayedOrders = new ArrayList<>();

    private String currentFilter = "Tất cả";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_hoa_don, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        donHangDAO = new DonHangDAO();

        initViews(view);
        setupRecyclerView();
        setupFilterChips();
        loadAllOrders();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadAllOrders();
    }

    private void initViews(View view) {
        rvDonHang = view.findViewById(R.id.rv_quan_ly_don_hang);
        pbLoading = view.findViewById(R.id.pb_loading_don_hang_nv);
        layoutEmpty = view.findViewById(R.id.layout_empty_don_hang_nv);
        tvCount = view.findViewById(R.id.tv_quan_ly_don_hang_count);
        chipGroupTrangThai = view.findViewById(R.id.chip_group_trang_thai);
    }

    private void setupRecyclerView() {
        rvDonHang.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new DonHangAdapter(requireContext(), displayedOrders, true, new DonHangAdapter.OnDonHangActionListener() {
            @Override
            public void onUpdateStatus(DonHang donHang) {
                showUpdateStatusDialog(donHang);
            }

            @Override
            public void onDeleteOrder(DonHang donHang) {
                showDeleteOrderDialog(donHang);
            }
        });
        rvDonHang.setAdapter(adapter);
    }

    private void setupFilterChips() {
        chipGroupTrangThai.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;
            int checkedId = checkedIds.get(0);

            if (checkedId == R.id.chip_cho_xu_ly) {
                currentFilter = "Chờ xử lý";
            } else if (checkedId == R.id.chip_dang_giao) {
                currentFilter = "Đang giao";
            } else if (checkedId == R.id.chip_da_giao) {
                currentFilter = "Đã giao";
            } else if (checkedId == R.id.chip_da_huy) {
                currentFilter = "Đã hủy";
            } else {
                currentFilter = "Tất cả";
            }

            applyFilter();
        });
    }

    private void loadAllOrders() {
        if (!isAdded()) return;

        pbLoading.setVisibility(View.VISIBLE);
        donHangDAO.getAll(new DonHangDAO.OnDataListLoadedListener<DonHang>() {
            @Override
            public void onSuccess(List<DonHang> list) {
                if (!isAdded()) return;
                pbLoading.setVisibility(View.GONE);
                allOrders.clear();
                if (list != null) {
                    allOrders.addAll(list);
                }
                applyFilter();
            }

            @Override
            public void onFailure(String message) {
                if (!isAdded()) return;
                pbLoading.setVisibility(View.GONE);
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void applyFilter() {
        displayedOrders.clear();
        if ("Tất cả".equalsIgnoreCase(currentFilter)) {
            displayedOrders.addAll(allOrders);
        } else {
            for (DonHang dh : allOrders) {
                if (currentFilter.equalsIgnoreCase(dh.getTrangThai())) {
                    displayedOrders.add(dh);
                }
            }
        }

        adapter.notifyDataSetChanged();

        if (displayedOrders.isEmpty()) {
            layoutEmpty.setVisibility(View.VISIBLE);
            rvDonHang.setVisibility(View.GONE);
            tvCount.setText("Không có đơn hàng nào (" + currentFilter + ")");
        } else {
            layoutEmpty.setVisibility(View.GONE);
            rvDonHang.setVisibility(View.VISIBLE);
            tvCount.setText("Tổng số đơn: " + displayedOrders.size() + " (" + currentFilter + ")");
        }
    }

    /**
     * Dialog cập nhật trạng thái đơn hàng cho nhân viên:
     * Chờ xử lý -> Đang giao -> Đã giao -> Đã hủy
     */
    private void showUpdateStatusDialog(DonHang donHang) {
        if (donHang == null) return;

        // Đơn đã hủy không thể cập nhật lại trạng thái nữa
        if ("Đã hủy".equalsIgnoreCase(donHang.getTrangThai())) {
            Toast.makeText(requireContext(), "Đơn hàng đã hủy không thể cập nhật lại trạng thái!", Toast.LENGTH_SHORT).show();
            return;
        }

        final String[] statuses = {"Chờ xử lý", "Đang giao", "Đã giao", "Đã hủy"};
        int currentIndex = 0;
        for (int i = 0; i < statuses.length; i++) {
            if (statuses[i].equalsIgnoreCase(donHang.getTrangThai())) {
                currentIndex = i;
                break;
            }
        }

        final int[] selectedIndex = {currentIndex};

        new AlertDialog.Builder(requireContext())
                .setTitle("Cập nhật trạng thái đơn #" + donHang.getMaDonHang())
                .setSingleChoiceItems(statuses, currentIndex, (dialog, which) -> selectedIndex[0] = which)
                .setPositiveButton("Lưu", (dialog, which) -> {
                    String newStatus = statuses[selectedIndex[0]];
                    donHangDAO.updateTrangThai(donHang.getMaDonHang(), newStatus, new DonHangDAO.OnSimpleActionListener() {
                        @Override
                        public void onSuccess() {
                            if (!isAdded()) return;
                            Toast.makeText(requireContext(), "Đã chuyển trạng thái sang: " + newStatus, Toast.LENGTH_SHORT).show();
                            loadAllOrders();
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

    /**
     * Dialog xác nhận xóa đơn hàng khỏi Firestore
     */
    private void showDeleteOrderDialog(DonHang donHang) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Xác nhận xóa đơn hàng")
                .setMessage("Bạn có chắc chắn muốn xóa đơn hàng #" + donHang.getMaDonHang() + " này không? Dữ liệu sẽ bị xóa hoàn toàn khỏi hệ thống.")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    donHangDAO.delete(donHang.getMaDonHang(), new DonHangDAO.OnSimpleActionListener() {
                        @Override
                        public void onSuccess() {
                            if (!isAdded()) return;
                            Toast.makeText(requireContext(), "Đã xóa đơn hàng thành công!", Toast.LENGTH_SHORT).show();
                            loadAllOrders();
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
