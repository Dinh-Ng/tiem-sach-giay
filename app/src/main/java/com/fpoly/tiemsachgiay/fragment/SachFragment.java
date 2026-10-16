package com.fpoly.tiemsachgiay.fragment;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
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

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.fpoly.tiemsachgiay.R;
import com.fpoly.tiemsachgiay.adapter.SachAdapter;
import com.fpoly.tiemsachgiay.dao.SachDAO;
import com.fpoly.tiemsachgiay.model.Sach;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SachFragment extends Fragment {

    private RecyclerView rvSach;
    private ProgressBar pbLoading;
    private LinearLayout layoutEmpty;
    private TextView tvEmptyMessage;
    private EditText edtSearch;
    private Spinner spFilterTheLoai;

    private SachDAO sachDAO;
    private SachAdapter adapter;

    private final List<Sach> fullBookList = new ArrayList<>();
    private final List<Sach> filteredBookList = new ArrayList<>();

    private final String[] categories = {
            "Tất cả thể loại", "Văn học", "Kinh tế", "Kỹ năng sống", "Công nghệ", "Thiếu nhi", "Lịch sử", "Ngoại ngữ", "Khác"
    };

    private final String[] categoryOptionsForm = {
            "Văn học", "Kinh tế", "Kỹ năng sống", "Công nghệ", "Thiếu nhi", "Lịch sử", "Ngoại ngữ", "Khác"
    };

    private final String[] unsplashSampleTitles = {
            "-- Chọn mẫu ảnh Unsplash đẹp --",
            "Mẫu 1 - Cà phê & Sách",
            "Mẫu 2 - Trang sách mở",
            "Mẫu 3 - Sách trên bàn gỗ",
            "Mẫu 4 - Đọc sách thư giãn",
            "Mẫu 5 - Giá sách thư viện",
            "Mẫu 6 - Sách văn học cổ điển",
            "Mẫu 7 - Sách công nghệ & máy tính",
            "Mẫu 8 - Sách minh họa thiếu nhi"
    };

    private final String[] unsplashSampleUrls = {
            "",
            "https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?w=800&auto=format&fit=crop&q=80",
            "https://images.unsplash.com/photo-1512820790803-83ca734da794?w=800&auto=format&fit=crop&q=80",
            "https://images.unsplash.com/photo-1532012197267-da84d127e765?w=800&auto=format&fit=crop&q=80",
            "https://images.unsplash.com/photo-1543002588-bfa74002ed7e?w=800&auto=format&fit=crop&q=80",
            "https://images.unsplash.com/photo-1497633762265-9d179a990aa6?w=800&auto=format&fit=crop&q=80",
            "https://images.unsplash.com/photo-1589829085413-56de8ae18c73?w=800&auto=format&fit=crop&q=80",
            "https://images.unsplash.com/photo-1526374965328-7f61d4dc18c5?w=800&auto=format&fit=crop&q=80",
            "https://images.unsplash.com/photo-1516979187457-637abb4f9353?w=800&auto=format&fit=crop&q=80"
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_sach, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sachDAO = new SachDAO(requireContext());

        initViews(view);
        setupRecyclerView();
        setupFilterSpinner();
        setupSearchListener();

        // Load danh sách sách từ Firestore
        loadBooksFromFirestore();
    }

    private void initViews(View view) {
        rvSach = view.findViewById(R.id.rv_sach);
        pbLoading = view.findViewById(R.id.pb_loading_sach);
        layoutEmpty = view.findViewById(R.id.layout_empty_sach);
        tvEmptyMessage = view.findViewById(R.id.tv_empty_message);
        edtSearch = view.findViewById(R.id.edt_search_sach);
        spFilterTheLoai = view.findViewById(R.id.sp_filter_the_loai);

        View fabAdd = view.findViewById(R.id.fab_add_sach);
        fabAdd.setOnClickListener(v -> showAddBookDialog());
    }

    private void setupRecyclerView() {
        rvSach.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new SachAdapter(requireContext(), filteredBookList, new SachAdapter.OnBookClickListener() {
            @Override
            public void onEditClick(Sach sach) {
                showEditBookDialog(sach);
            }

            @Override
            public void onDeleteClick(Sach sach) {
                showDeleteConfirmation(sach);
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
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
                fullBookList.clear();
                applySearchAndFilter();
            }
        });
    }

    private void applySearchAndFilter() {
        String query = edtSearch.getText() != null ? edtSearch.getText().toString().trim().toLowerCase() : "";
        String selectedCategory = spFilterTheLoai.getSelectedItem() != null ? spFilterTheLoai.getSelectedItem().toString() : "Tất cả thể loại";

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
            if (!query.isEmpty() || !"Tất cả thể loại".equals(selectedCategory)) {
                tvEmptyMessage.setText("Không tìm thấy sách phù hợp với bộ lọc");
            } else {
                tvEmptyMessage.setText("Chưa có sách nào trong danh sách");
            }
        } else {
            rvSach.setVisibility(View.VISIBLE);
            layoutEmpty.setVisibility(View.GONE);
        }
    }

    private void showAddBookDialog() {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_sach, null);

        TextView tvTitle = dialogView.findViewById(R.id.tv_dialog_title);
        EditText edtTenSach = dialogView.findViewById(R.id.edt_dialog_ten_sach);
        EditText edtTacGia = dialogView.findViewById(R.id.edt_dialog_tac_gia);
        Spinner spTheLoai = dialogView.findViewById(R.id.sp_dialog_the_loai);
        EditText edtGiaGoc = dialogView.findViewById(R.id.edt_dialog_gia_goc);
        EditText edtGiaGiam = dialogView.findViewById(R.id.edt_dialog_gia_giam);
        EditText edtSoLuong = dialogView.findViewById(R.id.edt_dialog_so_luong);
        Spinner spSampleImages = dialogView.findViewById(R.id.sp_dialog_sample_images);
        EditText edtHinhAnh = dialogView.findViewById(R.id.edt_dialog_hinh_anh);
        ImageView imgPreview = dialogView.findViewById(R.id.img_dialog_preview);
        MaterialButton btnCancel = dialogView.findViewById(R.id.btn_dialog_cancel);
        MaterialButton btnSave = dialogView.findViewById(R.id.btn_dialog_save);

        tvTitle.setText("Thêm Sách Mới");
        edtSoLuong.setText("10");

        // Spinner Thể loại
        ArrayAdapter<String> adapterTheLoai = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, categoryOptionsForm);
        adapterTheLoai.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spTheLoai.setAdapter(adapterTheLoai);

        // Spinner Mẫu ảnh Unsplash
        ArrayAdapter<String> adapterSamples = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, unsplashSampleTitles);
        adapterSamples.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spSampleImages.setAdapter(adapterSamples);

        spSampleImages.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0 && position < unsplashSampleUrls.length) {
                    String url = unsplashSampleUrls[position];
                    edtHinhAnh.setText(url);
                    updateImagePreview(imgPreview, url);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        edtHinhAnh.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateImagePreview(imgPreview, s.toString().trim());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .setCancelable(false)
                .create();

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnSave.setOnClickListener(v -> {
            String tenSach = edtTenSach.getText() != null ? edtTenSach.getText().toString().trim() : "";
            String tacGia = edtTacGia.getText() != null ? edtTacGia.getText().toString().trim() : "";
            String theLoai = spTheLoai.getSelectedItem() != null ? spTheLoai.getSelectedItem().toString() : "Văn học";
            String strGiaGoc = edtGiaGoc.getText() != null ? edtGiaGoc.getText().toString().trim() : "";
            String strGiaGiam = edtGiaGiam.getText() != null ? edtGiaGiam.getText().toString().trim() : "";
            String strSoLuong = edtSoLuong.getText() != null ? edtSoLuong.getText().toString().trim() : "";
            String hinhAnh = edtHinhAnh.getText() != null ? edtHinhAnh.getText().toString().trim() : "";

            if (tenSach.isEmpty() || tacGia.isEmpty() || strGiaGoc.isEmpty() || strSoLuong.isEmpty()) {
                Toast.makeText(requireContext(), "Vui lòng nhập đầy đủ các trường bắt buộc (*)", Toast.LENGTH_SHORT).show();
                return;
            }

            double giaGoc;
            try {
                giaGoc = Double.parseDouble(strGiaGoc);
            } catch (Exception e) {
                Toast.makeText(requireContext(), "Giá gốc không hợp lệ!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (giaGoc <= 0) {
                Toast.makeText(requireContext(), "Giá gốc phải lớn hơn 0!", Toast.LENGTH_SHORT).show();
                return;
            }

            double giaGiam = giaGoc;
            if (!strGiaGiam.isEmpty()) {
                try {
                    giaGiam = Double.parseDouble(strGiaGiam);
                } catch (Exception e) {
                    Toast.makeText(requireContext(), "Giá giảm không hợp lệ!", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            if (giaGiam < 0 || giaGiam > giaGoc) {
                Toast.makeText(requireContext(), "Giá giảm không được âm và không được lớn hơn giá gốc!", Toast.LENGTH_SHORT).show();
                return;
            }

            int soLuong = 10;
            try {
                soLuong = Integer.parseInt(strSoLuong);
            } catch (Exception e) {
                Toast.makeText(requireContext(), "Số lượng không hợp lệ!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (soLuong < 0) {
                Toast.makeText(requireContext(), "Số lượng không được âm!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (hinhAnh.isEmpty()) {
                hinhAnh = "https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?w=800&auto=format&fit=crop&q=80";
            }

            btnSave.setEnabled(false);
            Toast.makeText(requireContext(), "Đang lưu sách mới...", Toast.LENGTH_SHORT).show();

            Sach newSach = new Sach(tenSach, tacGia, giaGoc, giaGiam, theLoai, hinhAnh, soLuong);

            sachDAO.insert(newSach, new SachDAO.OnActionListener() {
                @Override
                public void onSuccess() {
                    if (!isAdded()) return;
                    btnSave.setEnabled(true);
                    Toast.makeText(requireContext(), "Thêm sách mới thành công!", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                    loadBooksFromFirestore();
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

    private void showEditBookDialog(Sach sach) {
        if (sach == null) return;

        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_sach, null);

        TextView tvTitle = dialogView.findViewById(R.id.tv_dialog_title);
        EditText edtTenSach = dialogView.findViewById(R.id.edt_dialog_ten_sach);
        EditText edtTacGia = dialogView.findViewById(R.id.edt_dialog_tac_gia);
        Spinner spTheLoai = dialogView.findViewById(R.id.sp_dialog_the_loai);
        EditText edtGiaGoc = dialogView.findViewById(R.id.edt_dialog_gia_goc);
        EditText edtGiaGiam = dialogView.findViewById(R.id.edt_dialog_gia_giam);
        EditText edtSoLuong = dialogView.findViewById(R.id.edt_dialog_so_luong);
        Spinner spSampleImages = dialogView.findViewById(R.id.sp_dialog_sample_images);
        EditText edtHinhAnh = dialogView.findViewById(R.id.edt_dialog_hinh_anh);
        ImageView imgPreview = dialogView.findViewById(R.id.img_dialog_preview);
        MaterialButton btnCancel = dialogView.findViewById(R.id.btn_dialog_cancel);
        MaterialButton btnSave = dialogView.findViewById(R.id.btn_dialog_save);

        tvTitle.setText("Chỉnh Sửa Thông Tin Sách");

        // Set giá trị hiện tại
        edtTenSach.setText(sach.getTenSach());
        edtTacGia.setText(sach.getTacGia());
        edtGiaGoc.setText(String.valueOf((long) sach.getGiaGoc()));
        edtGiaGiam.setText(String.valueOf((long) sach.getGiaGiam()));
        edtSoLuong.setText(String.valueOf(sach.getSoLuong()));
        edtHinhAnh.setText(sach.getHinhAnh() != null ? sach.getHinhAnh() : "");
        updateImagePreview(imgPreview, sach.getHinhAnh());

        // Spinner Thể loại
        ArrayAdapter<String> adapterTheLoai = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, categoryOptionsForm);
        adapterTheLoai.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spTheLoai.setAdapter(adapterTheLoai);

        int selectedCategoryIndex = Arrays.asList(categoryOptionsForm).indexOf(sach.getTheLoai());
        if (selectedCategoryIndex >= 0) {
            spTheLoai.setSelection(selectedCategoryIndex);
        }

        // Spinner Mẫu ảnh Unsplash
        ArrayAdapter<String> adapterSamples = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, unsplashSampleTitles);
        adapterSamples.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spSampleImages.setAdapter(adapterSamples);

        spSampleImages.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0 && position < unsplashSampleUrls.length) {
                    String url = unsplashSampleUrls[position];
                    edtHinhAnh.setText(url);
                    updateImagePreview(imgPreview, url);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        edtHinhAnh.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateImagePreview(imgPreview, s.toString().trim());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .setCancelable(false)
                .create();

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnSave.setOnClickListener(v -> {
            String tenSach = edtTenSach.getText() != null ? edtTenSach.getText().toString().trim() : "";
            String tacGia = edtTacGia.getText() != null ? edtTacGia.getText().toString().trim() : "";
            String theLoai = spTheLoai.getSelectedItem() != null ? spTheLoai.getSelectedItem().toString() : "Văn học";
            String strGiaGoc = edtGiaGoc.getText() != null ? edtGiaGoc.getText().toString().trim() : "";
            String strGiaGiam = edtGiaGiam.getText() != null ? edtGiaGiam.getText().toString().trim() : "";
            String strSoLuong = edtSoLuong.getText() != null ? edtSoLuong.getText().toString().trim() : "";
            String hinhAnh = edtHinhAnh.getText() != null ? edtHinhAnh.getText().toString().trim() : "";

            if (tenSach.isEmpty() || tacGia.isEmpty() || strGiaGoc.isEmpty() || strSoLuong.isEmpty()) {
                Toast.makeText(requireContext(), "Vui lòng nhập đầy đủ các trường bắt buộc (*)", Toast.LENGTH_SHORT).show();
                return;
            }

            double giaGoc;
            try {
                giaGoc = Double.parseDouble(strGiaGoc);
            } catch (Exception e) {
                Toast.makeText(requireContext(), "Giá gốc không hợp lệ!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (giaGoc <= 0) {
                Toast.makeText(requireContext(), "Giá gốc phải lớn hơn 0!", Toast.LENGTH_SHORT).show();
                return;
            }

            double giaGiam = giaGoc;
            if (!strGiaGiam.isEmpty()) {
                try {
                    giaGiam = Double.parseDouble(strGiaGiam);
                } catch (Exception e) {
                    Toast.makeText(requireContext(), "Giá giảm không hợp lệ!", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            if (giaGiam < 0 || giaGiam > giaGoc) {
                Toast.makeText(requireContext(), "Giá giảm không được âm và không được lớn hơn giá gốc!", Toast.LENGTH_SHORT).show();
                return;
            }

            int soLuong = 10;
            try {
                soLuong = Integer.parseInt(strSoLuong);
            } catch (Exception e) {
                Toast.makeText(requireContext(), "Số lượng không hợp lệ!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (soLuong < 0) {
                Toast.makeText(requireContext(), "Số lượng không được âm!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (hinhAnh.isEmpty()) {
                hinhAnh = "https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?w=800&auto=format&fit=crop&q=80";
            }

            btnSave.setEnabled(false);
            Toast.makeText(requireContext(), "Đang cập nhật thông tin sách...", Toast.LENGTH_SHORT).show();

            sach.setTenSach(tenSach);
            sach.setTacGia(tacGia);
            sach.setTheLoai(theLoai);
            sach.setGiaGoc(giaGoc);
            sach.setGiaGiam(giaGiam);
            sach.setHinhAnh(hinhAnh);
            sach.setSoLuong(soLuong);

            sachDAO.update(sach, new SachDAO.OnActionListener() {
                @Override
                public void onSuccess() {
                    if (!isAdded()) return;
                    btnSave.setEnabled(true);
                    Toast.makeText(requireContext(), "Cập nhật sách thành công!", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                    loadBooksFromFirestore();
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

    private void showDeleteConfirmation(Sach sach) {
        if (sach == null || sach.getMaSach() == null) return;

        new AlertDialog.Builder(requireContext())
                .setTitle("Xác nhận xóa sách")
                .setMessage("Bạn có chắc chắn muốn xóa cuốn sách \"" + sach.getTenSach() + "\" không?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    Toast.makeText(requireContext(), "Đang xóa sách...", Toast.LENGTH_SHORT).show();
                    sachDAO.delete(sach.getMaSach(), new SachDAO.OnActionListener() {
                        @Override
                        public void onSuccess() {
                            if (!isAdded()) return;
                            Toast.makeText(requireContext(), "Xóa sách thành công!", Toast.LENGTH_SHORT).show();
                            fullBookList.remove(sach);
                            applySearchAndFilter();
                            loadBooksFromFirestore();
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

    private void updateImagePreview(ImageView imageView, String url) {
        if (url != null && !url.trim().isEmpty()) {
            try {
                String cleanUrl = url.trim();
                GlideUrl glideUrl = new GlideUrl(cleanUrl, new LazyHeaders.Builder()
                        .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/110.0.0.0 Safari/537.36")
                        .build());

                Glide.with(this)
                        .load(glideUrl)
                        .override(300, 400)
                        .centerCrop()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .placeholder(R.drawable.ic_book_placeholder)
                        .error(R.drawable.ic_book_placeholder)
                        .into(imageView);
            } catch (Exception e) {
                imageView.setImageResource(R.drawable.ic_book_placeholder);
            }
        } else {
            imageView.setImageResource(R.drawable.ic_book_placeholder);
        }
    }
}