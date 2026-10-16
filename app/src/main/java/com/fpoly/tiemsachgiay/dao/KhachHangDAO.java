package com.fpoly.tiemsachgiay.dao;

import android.content.Context;

import com.fpoly.tiemsachgiay.model.KhachHang;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KhachHangDAO {

    private final FirebaseFirestore db;
    private static final String COLLECTION_NAME = "KhachHang";

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

    // Backward-compatible Login & Register Listener Interfaces
    public interface OnLoginListener {
        void onSuccess(KhachHang khachHang);
        void onFailure(String message);
    }

    public interface OnRegisterListener {
        void onSuccess();
        void onFailure(String message);
    }

    public KhachHangDAO() {
        db = FirebaseFirestore.getInstance();
    }

    public KhachHangDAO(Context context) {
        db = FirebaseFirestore.getInstance();
    }

    private Map<String, Object> khachHangToMap(KhachHang kh) {
        Map<String, Object> map = new HashMap<>();
        map.put("tenKH", kh.getTenKH() != null ? kh.getTenKH() : "");
        map.put("taiKhoan", kh.getTaiKhoan() != null ? kh.getTaiKhoan() : "");
        map.put("matKhau", kh.getMatKhau() != null ? kh.getMatKhau() : "");
        map.put("sdt", kh.getSdt() != null ? kh.getSdt() : "");
        map.put("diaChi", kh.getDiaChi() != null ? kh.getDiaChi() : "");
        return map;
    }

    /**
     * Authenticate customer credentials against Firestore
     */
    public void checkLogin(String account, String matKhau, OnLoginListener listener) {
        if (account == null || account.trim().isEmpty() || matKhau == null || matKhau.trim().isEmpty()) {
            listener.onFailure("Tài khoản và mật khẩu không được để trống!");
            return;
        }

        final String cleanAccount = account.trim();
        final String cleanMatKhau = matKhau.trim();

        // Check hardcoded sample test accounts
        if (("khachhang".equalsIgnoreCase(cleanAccount) || "0912345678".equals(cleanAccount)) && "123456".equals(cleanMatKhau)) {
            KhachHang sample = new KhachHang("Khách Hàng Mẫu", "0912345678", "123456");
            sample.setTaiKhoan("0912345678");
            try {
                db.collection(COLLECTION_NAME).document("0912345678").set(khachHangToMap(sample));
            } catch (Exception ignored) {
            }
            listener.onSuccess(sample);
            return;
        }

        // Query Firestore by Document ID first (Phone Number or Account ID)
        db.collection(COLLECTION_NAME).document(cleanAccount).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        KhachHang khachHang = documentSnapshot.toObject(KhachHang.class);
                        if (khachHang != null && khachHang.getMatKhau() != null && khachHang.getMatKhau().equals(cleanMatKhau)) {
                            listener.onSuccess(khachHang);
                        } else {
                            listener.onFailure("Mật khẩu không chính xác!");
                        }
                    } else {
                        searchByField(cleanAccount, cleanMatKhau, listener);
                    }
                })
                .addOnFailureListener(e -> listener.onFailure("Lỗi kết nối máy chủ: " + e.getMessage()));
    }

    /**
     * Search customer by 'sdt' field if document ID match was not found
     */
    private void searchByField(String account, String matKhau, OnLoginListener listener) {
        db.collection(COLLECTION_NAME).whereEqualTo("sdt", account).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            KhachHang khachHang = doc.toObject(KhachHang.class);
                            if (khachHang != null && khachHang.getMatKhau() != null && khachHang.getMatKhau().equals(matKhau)) {
                                listener.onSuccess(khachHang);
                                return;
                            }
                        }
                        listener.onFailure("Mật khẩu không chính xác!");
                    } else {
                        listener.onFailure("Tài khoản khách hàng không tồn tại!");
                    }
                })
                .addOnFailureListener(e -> listener.onFailure("Lỗi kết nối máy chủ: " + e.getMessage()));
    }

    /**
     * Register new customer account
     */
    public void register(KhachHang kh, OnRegisterListener listener) {
        if (kh == null) {
            listener.onFailure("Thông tin đăng ký không hợp lệ!");
            return;
        }

        String userKey = kh.getSdt();
        if (userKey == null || userKey.trim().isEmpty()) {
            userKey = kh.getTaiKhoan();
        }

        if (userKey == null || userKey.trim().isEmpty()) {
            listener.onFailure("Vui lòng nhập số điện thoại đăng ký!");
            return;
        }

        final String finalKey = userKey.trim();

        // Check if customer phone number already exists
        db.collection(COLLECTION_NAME).document(finalKey).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        listener.onFailure("Số điện thoại này đã được đăng ký!");
                    } else {
                        db.collection(COLLECTION_NAME).document(finalKey).set(khachHangToMap(kh))
                                .addOnSuccessListener(aVoid -> listener.onSuccess())
                                .addOnFailureListener(e -> listener.onFailure("Đăng ký thất bại: " + e.getMessage()));
                    }
                })
                .addOnFailureListener(e -> listener.onFailure("Lỗi kiểm tra tài khoản: " + e.getMessage()));
    }

    /**
     * Get all customers
     */
    public void getAll(OnDataListLoadedListener<KhachHang> callback) {
        db.collection(COLLECTION_NAME).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<KhachHang> list = new ArrayList<>();
                    if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
                        for (DocumentSnapshot doc : queryDocumentSnapshots) {
                            KhachHang kh = doc.toObject(KhachHang.class);
                            if (kh != null) {
                                list.add(kh);
                            }
                        }
                    }
                    callback.onSuccess(list);
                })
                .addOnFailureListener(e -> callback.onFailure("Lỗi tải danh sách khách hàng: " + e.getMessage()));
    }

    /**
     * Get customer by phone/ID
     */
    public void getById(String sdt, OnDataLoadedListener<KhachHang> callback) {
        if (sdt == null || sdt.trim().isEmpty()) {
            callback.onFailure("Số điện thoại không hợp lệ!");
            return;
        }

        db.collection(COLLECTION_NAME).document(sdt.trim()).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        KhachHang kh = documentSnapshot.toObject(KhachHang.class);
                        if (kh != null) {
                            callback.onSuccess(kh);
                        } else {
                            callback.onFailure("Dữ liệu khách hàng không hợp lệ!");
                        }
                    } else {
                        callback.onFailure("Không tìm thấy khách hàng có SĐT: " + sdt);
                    }
                })
                .addOnFailureListener(e -> callback.onFailure("Lỗi lấy thông tin khách hàng: " + e.getMessage()));
    }

    /**
     * Insert new customer
     */
    public void insert(KhachHang khachHang, OnActionListener callback) {
        if (khachHang == null) {
            callback.onFailure("Thông tin khách hàng không hợp lệ!");
            return;
        }

        String key = khachHang.getSdt();
        if (key == null || key.trim().isEmpty()) {
            key = khachHang.getTaiKhoan();
        }

        if (key == null || key.trim().isEmpty()) {
            callback.onFailure("Số điện thoại khách hàng không được để trống!");
            return;
        }

        db.collection(COLLECTION_NAME).document(key.trim()).set(khachHangToMap(khachHang))
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onFailure("Lỗi thêm khách hàng: " + e.getMessage()));
    }

    /**
     * Update customer
     */
    public void update(KhachHang khachHang, OnActionListener callback) {
        if (khachHang == null) {
            callback.onFailure("Thông tin khách hàng không hợp lệ!");
            return;
        }

        String key = khachHang.getSdt();
        if (key == null || key.trim().isEmpty()) {
            key = khachHang.getTaiKhoan();
        }

        if (key == null || key.trim().isEmpty()) {
            callback.onFailure("Số điện thoại khách hàng không được để trống!");
            return;
        }

        db.collection(COLLECTION_NAME).document(key.trim()).set(khachHangToMap(khachHang))
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onFailure("Lỗi cập nhật khách hàng: " + e.getMessage()));
    }

    /**
     * Delete customer by phone/ID
     */
    public void delete(String sdt, OnActionListener callback) {
        if (sdt == null || sdt.trim().isEmpty()) {
            callback.onFailure("Số điện thoại không hợp lệ!");
            return;
        }

        db.collection(COLLECTION_NAME).document(sdt.trim()).delete()
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onFailure("Lỗi xóa khách hàng: " + e.getMessage()));
    }
}