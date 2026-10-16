package com.fpoly.tiemsachgiay.dao;

import android.content.Context;

import com.fpoly.tiemsachgiay.model.NhanVien;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NhanVienDAO {

    private final FirebaseFirestore db;
    private static final String COLLECTION_NAME = "NhanVien";

    // Generic Action Callback Interface
    public interface OnActionListener {
        void onSuccess();
        void onFailure(String message);
    }

    // Generic Data Callback Interfaces
    public interface OnDataLoadedListener<T> {
        void onSuccess(T data);
        void onFailure(String message);
    }

    public interface OnDataListLoadedListener<T> {
        void onSuccess(List<T> data);
        void onFailure(String message);
    }

    // Backward-compatible Login Listener Interface
    public interface OnLoginListener {
        void onSuccess(NhanVien nhanVien);
        void onFailure(String message);
    }

    public NhanVienDAO() {
        db = FirebaseFirestore.getInstance();
    }

    public NhanVienDAO(Context context) {
        db = FirebaseFirestore.getInstance();
    }

    private Map<String, Object> nhanVienToMap(NhanVien nv) {
        Map<String, Object> map = new HashMap<>();
        map.put("maNV", nv.getMaNV() != null ? nv.getMaNV() : "");
        map.put("hoTen", nv.getHoTen() != null ? nv.getHoTen() : "");
        map.put("matKhau", nv.getMatKhau() != null ? nv.getMatKhau() : "");
        map.put("chucVu", nv.getChucVu() != null ? nv.getChucVu() : "Nhân viên");
        return map;
    }

    /**
     * Authenticate employee credentials against Firestore
     */
    public void checkLogin(String maNV, String matKhau, OnLoginListener listener) {
        if (maNV == null || maNV.trim().isEmpty() || matKhau == null || matKhau.trim().isEmpty()) {
            listener.onFailure("Mã nhân viên và mật khẩu không được để trống!");
            return;
        }

        final String cleanMaNV = maNV.trim();
        final String cleanMatKhau = matKhau.trim();

        // Check hardcoded sample test accounts
        if ("admin".equalsIgnoreCase(cleanMaNV) && "admin123".equals(cleanMatKhau)) {
            NhanVien admin = new NhanVien("admin", "Quản lý Tiệm Sách", "admin123", "Quản lý");
            try {
                db.collection(COLLECTION_NAME).document("admin").set(nhanVienToMap(admin));
            } catch (Exception ignored) {
            }
            listener.onSuccess(admin);
            return;
        }

        if ("nv01".equalsIgnoreCase(cleanMaNV) && "123456".equals(cleanMatKhau)) {
            NhanVien nv01 = new NhanVien("nv01", "Nguyễn Văn Nam", "123456", "Thu ngân");
            try {
                db.collection(COLLECTION_NAME).document("nv01").set(nhanVienToMap(nv01));
            } catch (Exception ignored) {
            }
            listener.onSuccess(nv01);
            return;
        }

        // Query Firestore for employee document
        db.collection(COLLECTION_NAME).document(cleanMaNV).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        NhanVien nhanVien = documentSnapshot.toObject(NhanVien.class);
                        if (nhanVien != null && nhanVien.getMatKhau() != null && nhanVien.getMatKhau().equals(cleanMatKhau)) {
                            listener.onSuccess(nhanVien);
                        } else {
                            listener.onFailure("Mật khẩu không chính xác!");
                        }
                    } else {
                        listener.onFailure("Mã nhân viên không tồn tại!");
                    }
                })
                .addOnFailureListener(e -> listener.onFailure("Lỗi kết nối máy chủ: " + e.getMessage()));
    }

    /**
     * Get all employees
     */
    public void getAll(OnDataListLoadedListener<NhanVien> callback) {
        db.collection(COLLECTION_NAME).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<NhanVien> list = new ArrayList<>();
                    if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
                        for (DocumentSnapshot doc : queryDocumentSnapshots) {
                            NhanVien nv = doc.toObject(NhanVien.class);
                            if (nv != null) {
                                list.add(nv);
                            }
                        }
                    }
                    callback.onSuccess(list);
                })
                .addOnFailureListener(e -> callback.onFailure("Lỗi tải danh sách nhân viên: " + e.getMessage()));
    }

    /**
     * Get employee by ID
     */
    public void getById(String maNV, OnDataLoadedListener<NhanVien> callback) {
        if (maNV == null || maNV.trim().isEmpty()) {
            callback.onFailure("Mã nhân viên không hợp lệ!");
            return;
        }

        db.collection(COLLECTION_NAME).document(maNV.trim()).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        NhanVien nv = documentSnapshot.toObject(NhanVien.class);
                        if (nv != null) {
                            callback.onSuccess(nv);
                        } else {
                            callback.onFailure("Dữ liệu nhân viên không hợp lệ!");
                        }
                    } else {
                        callback.onFailure("Không tìm thấy nhân viên có mã: " + maNV);
                    }
                })
                .addOnFailureListener(e -> callback.onFailure("Lỗi lấy thông tin nhân viên: " + e.getMessage()));
    }

    /**
     * Insert new employee
     */
    public void insert(NhanVien nhanVien, OnActionListener callback) {
        if (nhanVien == null || nhanVien.getMaNV() == null || nhanVien.getMaNV().trim().isEmpty()) {
            callback.onFailure("Thông tin nhân viên không hợp lệ!");
            return;
        }

        db.collection(COLLECTION_NAME).document(nhanVien.getMaNV().trim()).set(nhanVienToMap(nhanVien))
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onFailure("Lỗi thêm nhân viên: " + e.getMessage()));
    }

    /**
     * Update employee
     */
    public void update(NhanVien nhanVien, OnActionListener callback) {
        if (nhanVien == null || nhanVien.getMaNV() == null || nhanVien.getMaNV().trim().isEmpty()) {
            callback.onFailure("Thông tin nhân viên không hợp lệ!");
            return;
        }

        db.collection(COLLECTION_NAME).document(nhanVien.getMaNV().trim()).set(nhanVienToMap(nhanVien))
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onFailure("Lỗi cập nhật nhân viên: " + e.getMessage()));
    }

    /**
     * Delete employee by ID
     */
    public void delete(String maNV, OnActionListener callback) {
        if (maNV == null || maNV.trim().isEmpty()) {
            callback.onFailure("Mã nhân viên không hợp lệ!");
            return;
        }

        db.collection(COLLECTION_NAME).document(maNV.trim()).delete()
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onFailure("Lỗi xóa nhân viên: " + e.getMessage()));
    }
}