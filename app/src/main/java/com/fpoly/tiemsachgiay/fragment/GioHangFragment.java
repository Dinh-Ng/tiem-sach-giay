package com.fpoly.tiemsachgiay.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fpoly.tiemsachgiay.R;
import com.fpoly.tiemsachgiay.ThanhToanActivity;
import com.fpoly.tiemsachgiay.adapter.GioHangAdapter;
import com.fpoly.tiemsachgiay.model.GioHang;
import com.google.android.material.button.MaterialButton;

import java.text.DecimalFormat;
import java.util.List;

/**
 * Fragment Giỏ Hàng của Khách hàng.
 * Hiển thị danh sách sách trong giỏ, cho phép tăng/giảm số lượng,
 * xóa item. Khi bấm "Đặt hàng" sẽ chuyển sang màn hình Thanh toán & chọn địa chỉ.
 */
public class GioHangFragment extends Fragment {

    private RecyclerView rvGioHang;
    private LinearLayout layoutEmptyCart, layoutCheckoutBar;
    private TextView tvTongTien, tvCartHeaderCount;
    private MaterialButton btnDatHang;

    private GioHangAdapter adapter;
    private final DecimalFormat formatter = new DecimalFormat("#,### ₫");

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_gio_hang, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupRecyclerView();
        refreshUI();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh lại mỗi khi quay về tab giỏ hàng (ví dụ sau khi đặt hàng thành công)
        refreshUI();
    }

    private void initViews(View view) {
        rvGioHang = view.findViewById(R.id.rv_gio_hang);
        layoutEmptyCart = view.findViewById(R.id.layout_empty_cart);
        layoutCheckoutBar = view.findViewById(R.id.layout_checkout_bar);
        tvTongTien = view.findViewById(R.id.tv_tong_tien);
        tvCartHeaderCount = view.findViewById(R.id.tv_cart_header_count);
        btnDatHang = view.findViewById(R.id.btn_dat_hang);

        btnDatHang.setOnClickListener(v -> navigateToThanhToan());
    }

    private void setupRecyclerView() {
        List<GioHang> gioHangList = GioHang.getGioHangList();
        rvGioHang.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new GioHangAdapter(requireContext(), gioHangList, this::refreshUI);
        rvGioHang.setAdapter(adapter);
    }

    private void refreshUI() {
        if (!isAdded()) return;

        List<GioHang> gioHangList = GioHang.getGioHangList();
        int totalItems = GioHang.getTotalItems();
        double totalPrice = GioHang.getTotalPrice();

        // Header badge
        tvCartHeaderCount.setText(totalItems > 0 ? totalItems + " sản phẩm" : "Trống");

        // Tổng tiền
        tvTongTien.setText(formatter.format(totalPrice));

        // Ẩn/hiện vùng empty và checkout bar
        if (gioHangList.isEmpty()) {
            rvGioHang.setVisibility(View.GONE);
            layoutEmptyCart.setVisibility(View.VISIBLE);
            layoutCheckoutBar.setVisibility(View.GONE);
        } else {
            rvGioHang.setVisibility(View.VISIBLE);
            layoutEmptyCart.setVisibility(View.GONE);
            layoutCheckoutBar.setVisibility(View.VISIBLE);
            adapter.notifyDataSetChanged();
        }

        // Cập nhật badge trên MainActivity
        updateCartBadge();
    }

    /**
     * Chuyển sang màn hình chọn địa chỉ nhận hàng và phương thức thanh toán.
     */
    private void navigateToThanhToan() {
        if (!isAdded()) return;

        int totalItems = GioHang.getTotalItems();
        if (totalItems == 0) {
            Toast.makeText(requireContext(), "Giỏ hàng trống!", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(requireContext(), ThanhToanActivity.class);
        if (getActivity() != null && getActivity().getIntent() != null) {
            Intent actIntent = getActivity().getIntent();
            intent.putExtra("user_id", actIntent.getStringExtra("user_id"));
            intent.putExtra("user_name", actIntent.getStringExtra("user_name"));
        }
        startActivity(intent);
    }

    private void updateCartBadge() {
        if (getActivity() instanceof com.fpoly.tiemsachgiay.MainActivity) {
            ((com.fpoly.tiemsachgiay.MainActivity) getActivity()).updateCartBadge();
        }
    }
}
