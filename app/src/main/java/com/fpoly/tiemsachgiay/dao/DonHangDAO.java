package com.fpoly.tiemsachgiay.dao;

import com.fpoly.tiemsachgiay.model.DonHang;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * DAO xử lý các thao tác với collection "DonHang" trên Firestore.
 */
public class DonHangDAO {

    private static final String COLLECTION_NAME = "DonHang";

    private final FirebaseFirestore db;

    public interface OnActionListener {
        void onSuccess(String maDonHang);
        void onFailure(String message);
    }

    public DonHangDAO() {
        db = FirebaseFirestore.getInstance();
    }

    /**
     * Tạo một đơn hàng mới trên Firestore.
     * ID tài liệu được Firestore tự sinh, sau đó ghi ngược lại vào trường maDonHang.
     *
     * @param donHang  Đơn hàng cần lưu
     * @param listener Callback trả về maDonHang nếu thành công
     */
    public void insert(DonHang donHang, OnActionListener listener) {
        // Tạo một document reference với ID tự sinh
        db.collection(COLLECTION_NAME)
                .add(donHang)
                .addOnSuccessListener(documentReference -> {
                    String generatedId = documentReference.getId();
                    // Ghi lại maDonHang vào chính document đó
                    documentReference.update("maDonHang", generatedId)
                            .addOnSuccessListener(aVoid -> {
                                donHang.setMaDonHang(generatedId);
                                listener.onSuccess(generatedId);
                            })
                            .addOnFailureListener(e -> {
                                // Dù update field phụ thất bại, đơn hàng vẫn đã được tạo
                                donHang.setMaDonHang(generatedId);
                                listener.onSuccess(generatedId);
                            });
                })
                .addOnFailureListener(e ->
                        listener.onFailure("Lỗi khi tạo đơn hàng: " + e.getMessage())
                );
    }
}
