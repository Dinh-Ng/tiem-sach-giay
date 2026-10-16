package com.fpoly.tiemsachgiay.fragment;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fpoly.tiemsachgiay.R;
import com.fpoly.tiemsachgiay.adapter.SachMuaAdapter;
import com.fpoly.tiemsachgiay.dao.SachDAO;
import com.fpoly.tiemsachgiay.model.GioHang;
import com.fpoly.tiemsachgiay.model.Sach;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment dành riêng cho Khách hàng: xem và mua sách.
 * Hiển thị danh sách sách với nút "Thêm vào giỏ" và "Mua ngay".
 * Không có chức năng quản lý (thêm/sửa/xóa sách).
 */
public class SachKhachHangFragment extends Fragment {

    private RecyclerView rvSach;
    private ProgressBar pbLoading;
    private LinearLayout layoutEmpty;
    private TextView tvEmptyMessage;
    private TextInputEditText edtSearch;
    private Spinner spFilterTheLoai;

    private SachDAO sachDAO;
    private SachMuaAdapter adapter;

    private final List<Sach> fullBookList = new ArrayList<>();
    private final List<Sach> filteredBookList = new ArrayList<>();

    private final String[] categories = {
            "Tất cả thể loại", "Văn học", "Kinh tế", "Kỹ năng sống",
            "Công nghệ", "Thiếu nhi", "Lịch sử", "Ngoại ngữ", "Khác"
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_sach_khach_hang, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sachDAO = new SachDAO(requireContext());

        initViews(view);
        setupRecyclerView();
        setupFilterSpinner();
        setupSearchListener();
        loadBooksFromFirestore();
    }

    private void initViews(View view) {
        rvSach = view.findViewById(R.id.rv_sach_kh);
        pbLoading = view.findViewById(R.id.pb_loading_sach_kh);
        layoutEmpty = view.findViewById(R.id.layout_empty_sach_kh);
        tvEmptyMessage = view.findViewById(R.id.tv_empty_sach_kh);
        edtSearch = view.findViewById(R.id.edt_search_sach_kh);
        spFilterTheLoai = view.findViewById(R.id.sp_filter_the_loai_kh);
    }

    private void setupRecyclerView() {
        rvSach.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new SachMuaAdapter(requireContext(), filteredBookList, new SachMuaAdapter.OnSachMuaClickListener() {

            @Override
            public void onAddToCart(Sach sach) {
                boolean added = GioHang.addToCart(sach, 1);
                if (added) {
                    Toast.makeText(requireContext(),
                            "✓ Đã thêm \"" + sach.getTenSach() + "\" vào giỏ hàng!",
                            Toast.LENGTH_SHORT).show();
                    // Cập nhật badge giỏ hàng trên bottom navigation nếu cần
                    updateCartBadge();
                } else {
                    Toast.makeText(requireContext(),
                            "Số lượng trong giỏ đã đạt tối đa tồn kho!",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onBuyNow(Sach sach) {
                // Thêm vào giỏ và chuyển ngay sang tab giỏ hàng
                GioHang.addToCart(sach, 1);
                Toast.makeText(requireContext(),
                        "Chuyển đến giỏ hàng...", Toast.LENGTH_SHORT).show();
                navigateToCart();
            }
        });
        rvSach.setAdapter(adapter);
    }

    private void setupFilterSpinner() {
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                categories
        );
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spFilterTheLoai.setAdapter(spinnerAdapter);

        spFilterTheLoai.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                applySearchAndFilter();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void setupSearchListener() {
        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                applySearchAndFilter();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void loadBooksFromFirestore() {
        pbLoading.setVisibility(View.VISIBLE);
        rvSach.setVisibility(View.GONE);
        layoutEmpty.setVisibility(View.GONE);

        sachDAO.getAll(new SachDAO.OnDataListLoadedListener<Sach>() {
            @Override
            public void onSuccess(List<Sach> data) {
                if (!isAdded()) return;
                pbLoading.setVisibility(View.GONE);
                fullBookList.clear();
                if (data != null) {
                    fullBookList.addAll(data);
                }
                applySearchAndFilter();
            }

            @Override
            public void onFailure(String message) {
                if (!isAdded()) return;
                pbLoading.setVisibility(View.GONE);
                Toast.makeText(requireContext(), "Không thể tải danh sách sách: " + message, Toast.LENGTH_SHORT).show();
                applySearchAndFilter();
            }
        });
    }

    private void applySearchAndFilter() {
        String query = edtSearch.getText() != null ? edtSearch.getText().toString().trim().toLowerCase() : "";
        String selectedCategory = spFilterTheLoai.getSelectedItem() != null
                ? spFilterTheLoai.getSelectedItem().toString() : "Tất cả thể loại";

        filteredBookList.clear();

        for (Sach sach : fullBookList) {
            boolean matchesSearch = query.isEmpty()
                    || (sach.getTenSach() != null && sach.getTenSach().toLowerCase().contains(query))
                    || (sach.getTacGia() != null && sach.getTacGia().toLowerCase().contains(query));

            boolean matchesCategory = "Tất cả thể loại".equalsIgnoreCase(selectedCategory)
                    || (sach.getTheLoai() != null && sach.getTheLoai().equalsIgnoreCase(selectedCategory));

            if (matchesSearch && matchesCategory) {
                filteredBookList.add(sach);
            }
        }

        adapter.setListSach(filteredBookList);

        if (filteredBookList.isEmpty()) {
            rvSach.setVisibility(View.GONE);
            layoutEmpty.setVisibility(View.VISIBLE);
            tvEmptyMessage.setText((!query.isEmpty() || !"Tất cả thể loại".equals(selectedCategory))
                    ? "Không tìm thấy sách phù hợp"
                    : "Chưa có sách nào trong cửa hàng");
        } else {
            rvSach.setVisibility(View.VISIBLE);
            layoutEmpty.setVisibility(View.GONE);
        }
    }

    /** Cập nhật badge số lượng sản phẩm trên tab Giỏ hàng. */
    private void updateCartBadge() {
        if (getActivity() instanceof com.fpoly.tiemsachgiay.MainActivity) {
            ((com.fpoly.tiemsachgiay.MainActivity) getActivity()).updateCartBadge();
        }
    }

    /** Điều hướng sang tab Giỏ Hàng thông qua BottomNavigation. */
    private void navigateToCart() {
        if (getActivity() instanceof com.fpoly.tiemsachgiay.MainActivity) {
            ((com.fpoly.tiemsachgiay.MainActivity) getActivity()).navigateToCartTab();
        }
    }

    /** Gọi để reload dữ liệu khi fragment được resume lại. */
    @Override
    public void onResume() {
        super.onResume();
        updateCartBadge();
    }
}
