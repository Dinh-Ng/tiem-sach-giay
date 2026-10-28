package com.fpoly.tiemsachgiay.fragment;

import android.content.Intent;
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

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment hiển thị tất cả các đơn hàng mà khách hàng hiện tại đã đặt.
 */
public class DonHangKhachHangFragment extends Fragment {

    private RecyclerView rvDonHang;
    private ProgressBar pbLoading;
    private LinearLayout layoutEmpty;
    private TextView tvSoLuongDon;

    private DonHangDAO donHangDAO;
    private DonHangAdapter adapter;
    private final List<DonHang> listDonHang = new ArrayList<>();
    private String userId = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_don_hang_khach_hang, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        donHangDAO = new DonHangDAO();

        if (getActivity() != null && getActivity().getIntent() != null) {
            Intent intent = getActivity().getIntent();
            userId = intent.getStringExtra("user_id") != null ? intent.getStringExtra("user_id") : "";
        }

        initViews(view);
        setupRecyclerView();
        loadCustomerOrders();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Cập nhật lại danh sách khi quay lại tab
        loadCustomerOrders();
    }

    private void initViews(View view) {
        rvDonHang = view.findViewById(R.id.rv_don_hang_khach_hang);
        pbLoading = view.findViewById(R.id.pb_loading_don_hang_kh);
        layoutEmpty = view.findViewById(R.id.layout_empty_don_hang_kh);
        tvSoLuongDon = view.findViewById(R.id.tv_so_luong_don_kh);
    }

    private void setupRecyclerView() {
        rvDonHang.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new DonHangAdapter(requireContext(), listDonHang, false, new DonHangAdapter.OnDonHangActionListener() {
            @Override
            public void onUpdateStatus(DonHang donHang) {
                // Khách hàng bấm hủy đơn
                showCancelOrderDialog(donHang);
            }

            @Override
            public void onDeleteOrder(DonHang donHang) {
                // Khách hàng không có quyền xóa đơn
            }
        });
        rvDonHang.setAdapter(adapter);
    }

    private void loadCustomerOrders() {
        if (!isAdded()) return;

        pbLoading.setVisibility(View.VISIBLE);
        donHangDAO.getByKhachHangId(userId, new DonHangDAO.OnDataListLoadedListener<DonHang>() {
            @Override
            public void onSuccess(List<DonHang> list) {
                if (!isAdded()) return;
                pbLoading.setVisibility(View.GONE);
                listDonHang.clear();
                if (list != null) {
                    listDonHang.addAll(list);
                }
                adapter.notifyDataSetChanged();

                if (listDonHang.isEmpty()) {
                    layoutEmpty.setVisibility(View.VISIBLE);
                    rvDonHang.setVisibility(View.GONE);
                    tvSoLuongDon.setText("Bạn chưa có đơn đặt hàng nào");
                } else {
                    layoutEmpty.setVisibility(View.GONE);
                    rvDonHang.setVisibility(View.VISIBLE);
                    tvSoLuongDon.setText("Bạn có " + listDonHang.size() + " đơn hàng đã đặt");
                }
            }

            @Override
            public void onFailure(String message) {
                if (!isAdded()) return;
                pbLoading.setVisibility(View.GONE);
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showCancelOrderDialog(DonHang donHang) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Xác nhận hủy đơn hàng")
                .setMessage("Bạn có chắc chắn muốn hủy đơn hàng #" + donHang.getMaDonHang() + " này không?")
                .setPositiveButton("Hủy đơn", (dialog, which) -> {
                    donHangDAO.updateTrangThai(donHang.getMaDonHang(), "Đã hủy", new DonHangDAO.OnSimpleActionListener() {
                        @Override
                        public void onSuccess() {
                            if (!isAdded()) return;
                            Toast.makeText(requireContext(), "Đã hủy đơn hàng thành công!", Toast.LENGTH_SHORT).show();
                            loadCustomerOrders();
                        }

                        @Override
                        public void onFailure(String message) {
                            if (!isAdded()) return;
                            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Đóng", null)
                .show();
    }
}
