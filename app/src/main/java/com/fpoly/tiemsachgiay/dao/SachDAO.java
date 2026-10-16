package com.fpoly.tiemsachgiay.dao;

import android.content.Context;

import com.fpoly.tiemsachgiay.model.Sach;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class SachDAO {

    private final FirebaseFirestore db;
    private static final String COLLECTION_NAME = "Sach";

    public interface OnActionListener {
        void onSuccess();
        void onFailure(String message);
    }

    public interface OnDataLoadedListener<T> {
        void onSuccess(T data);
        void onFailure(String message);
    }

    public interface OnDataListLoadedListener<T> {
        void onSuccess(List<T> data);
        void onFailure(String message);
    }

    public SachDAO() {
        db = FirebaseFirestore.getInstance();
    }

    public SachDAO(Context context) {
        db = FirebaseFirestore.getInstance();
    }

    /**
     * Get all books from Firestore
     */
    public void getAll(OnDataListLoadedListener<Sach> callback) {
        db.collection(COLLECTION_NAME).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Sach> list = new ArrayList<>();
                    if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
                        for (DocumentSnapshot doc : queryDocumentSnapshots) {
                            Sach sach = doc.toObject(Sach.class);
                            if (sach != null) {
                                // Always bind maSach to the exact Firestore document ID
                                sach.setMaSach(doc.getId());
                                list.add(sach);
                            }
                        }
                    }
                    callback.onSuccess(list);
                })
                .addOnFailureListener(e -> callback.onFailure("Lỗi tải danh sách sách: " + e.getMessage()));
    }

    /**
     * Get book by ID
     */
    public void getById(String maSach, OnDataLoadedListener<Sach> callback) {
        if (maSach == null || maSach.trim().isEmpty()) {
            callback.onFailure("Mã sách không hợp lệ!");
            return;
        }

        db.collection(COLLECTION_NAME).document(maSach.trim()).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Sach sach = documentSnapshot.toObject(Sach.class);
                        if (sach != null) {
                            sach.setMaSach(documentSnapshot.getId());
                            callback.onSuccess(sach);
                        } else {
                            callback.onFailure("Dữ liệu sách không hợp lệ!");
                        }
                    } else {
                        callback.onFailure("Không tìm thấy sách!");
                    }
                })
                .addOnFailureListener(e -> callback.onFailure("Lỗi lấy thông tin sách: " + e.getMessage()));
    }

    /**
     * Insert new book into Cloud Firestore
     */
    public void insert(Sach sach, OnActionListener callback) {
        if (sach == null) {
            callback.onFailure("Thông tin sách không hợp lệ!");
            return;
        }

        if (sach.getTenSach() == null || sach.getTenSach().trim().isEmpty()) {
            callback.onFailure("Tên sách không được để trống!");
            return;
        }

        if (sach.getTacGia() == null || sach.getTacGia().trim().isEmpty()) {
            callback.onFailure("Tên tác giả không được để trống!");
            return;
        }

        if (sach.getGiaGoc() <= 0) {
            callback.onFailure("Giá gốc phải lớn hơn 0!");
            return;
        }

        if (sach.getGiaGiam() < 0 || sach.getGiaGiam() > sach.getGiaGoc()) {
            callback.onFailure("Giá giảm không được âm và không được lớn hơn giá gốc!");
            return;
        }

        if (sach.getSoLuong() < 0) {
            callback.onFailure("Số lượng không được âm!");
            return;
        }

        String docId = sach.getMaSach();
        if (docId == null || docId.trim().isEmpty()) {
            docId = "s_" + System.currentTimeMillis();
            sach.setMaSach(docId);
        }

        final String finalDocId = docId.trim();

        db.collection(COLLECTION_NAME).document(finalDocId).set(sach)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onFailure("Lỗi thêm sách: " + e.getMessage()));
    }

    /**
     * Update existing book in Cloud Firestore
     */
    public void update(Sach sach, OnActionListener callback) {
        if (sach == null || sach.getMaSach() == null || sach.getMaSach().trim().isEmpty()) {
            callback.onFailure("Mã sách không hợp lệ!");
            return;
        }

        if (sach.getTenSach() == null || sach.getTenSach().trim().isEmpty()) {
            callback.onFailure("Tên sách không được để trống!");
            return;
        }

        if (sach.getTacGia() == null || sach.getTacGia().trim().isEmpty()) {
            callback.onFailure("Tên tác giả không được để trống!");
            return;
        }

        if (sach.getGiaGoc() <= 0) {
            callback.onFailure("Giá gốc phải lớn hơn 0!");
            return;
        }

        if (sach.getGiaGiam() < 0 || sach.getGiaGiam() > sach.getGiaGoc()) {
            callback.onFailure("Giá giảm không được âm và không được lớn hơn giá gốc!");
            return;
        }

        if (sach.getSoLuong() < 0) {
            callback.onFailure("Số lượng không được âm!");
            return;
        }

        db.collection(COLLECTION_NAME).document(sach.getMaSach().trim()).set(sach)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onFailure("Lỗi cập nhật sách: " + e.getMessage()));
    }

    /**
     * Delete book by Document ID from Cloud Firestore
     */
    public void delete(String maSach, OnActionListener callback) {
        if (maSach == null || maSach.trim().isEmpty()) {
            callback.onFailure("Mã sách không hợp lệ!");
            return;
        }

        db.collection(COLLECTION_NAME).document(maSach.trim()).delete()
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onFailure("Lỗi xóa sách: " + e.getMessage()));
    }
}