package com.fpoly.tiemsachgiay.dao;

import com.fpoly.tiemsachgiay.model.DonHang;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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

    public interface OnDataListLoadedListener<T> {
        void onSuccess(List<T> list);
        void onFailure(String message);
    }

    public interface OnSimpleActionListener {
        void onSuccess();
        void onFailure(String message);
    }

    public DonHangDAO() {
        db = FirebaseFirestore.getInstance();
    }

    /**
     * Tạo một đơn hàng mới trên Firestore.
     * ID tài liệu được Firestore tự sinh, sau đó ghi ngược lại vào trường maDonHang.
     */
    public void insert(DonHang donHang, OnActionListener listener) {
        db.collection(COLLECTION_NAME)
                .add(donHang)
                .addOnSuccessListener(documentReference -> {
                    String generatedId = documentReference.getId();
                    documentReference.update("maDonHang", generatedId)
                            .addOnSuccessListener(aVoid -> {
                                donHang.setMaDonHang(generatedId);
                                listener.onSuccess(generatedId);
                            })
                            .addOnFailureListener(e -> {
                                donHang.setMaDonHang(generatedId);
                                listener.onSuccess(generatedId);
                            });
                })
                .addOnFailureListener(e ->
                        listener.onFailure("Lỗi khi tạo đơn hàng: " + e.getMessage())
                );
    }

    /**
     * Lấy tất cả đơn hàng (dành cho Nhân viên quản lý).
     * Sắp xếp theo ngày đặt mới nhất lên đầu.
     */
    public void getAll(OnDataListLoadedListener<DonHang> callback) {
        db.collection(COLLECTION_NAME)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<DonHang> list = parseSnapshotToDonHangList(queryDocumentSnapshots);
                    // Sắp xếp ngày đặt giảm dần (mới nhất lên đầu)
                    Collections.sort(list, (o1, o2) -> Long.compare(o2.getNgayDat(), o1.getNgayDat()));
                    callback.onSuccess(list);
                })
                .addOnFailureListener(e -> callback.onFailure("Lỗi tải danh sách đơn hàng: " + e.getMessage()));
    }

    /**
     * Lấy danh sách đơn hàng của một khách hàng cụ thể (dành cho màn hình khách hàng).
     */
    public void getByKhachHangId(String khachHangId, OnDataListLoadedListener<DonHang> callback) {
        if (khachHangId == null || khachHangId.trim().isEmpty()) {
            callback.onSuccess(new ArrayList<>());
            return;
        }

        db.collection(COLLECTION_NAME)
                .whereEqualTo("khachHangId", khachHangId.trim())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<DonHang> list = parseSnapshotToDonHangList(queryDocumentSnapshots);
                    // Sắp xếp ngày đặt giảm dần
                    Collections.sort(list, (o1, o2) -> Long.compare(o2.getNgayDat(), o1.getNgayDat()));
                    callback.onSuccess(list);
                })
                .addOnFailureListener(e -> callback.onFailure("Lỗi tải đơn hàng của khách: " + e.getMessage()));
    }

    /**
     * Cập nhật trạng thái đơn hàng (ví dụ: "Chờ xử lý", "Đang giao", "Đã giao", "Đã hủy").
     */
    public void updateTrangThai(String maDonHang, String trangThaiMoi, OnSimpleActionListener callback) {
        if (maDonHang == null || maDonHang.trim().isEmpty()) {
            callback.onFailure("Mã đơn hàng không hợp lệ!");
            return;
        }

        db.collection(COLLECTION_NAME).document(maDonHang.trim())
                .update("trangThai", trangThaiMoi)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onFailure("Lỗi cập nhật trạng thái đơn hàng: " + e.getMessage()));
    }

    /**
     * Xóa đơn hàng khỏi Firestore.
     */
    public void delete(String maDonHang, OnSimpleActionListener callback) {
        if (maDonHang == null || maDonHang.trim().isEmpty()) {
            callback.onFailure("Mã đơn hàng không hợp lệ!");
            return;
        }

        db.collection(COLLECTION_NAME).document(maDonHang.trim())
                .delete()
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onFailure("Lỗi xóa đơn hàng: " + e.getMessage()));
    }

    private List<DonHang> parseSnapshotToDonHangList(QuerySnapshot snapshot) {
        List<DonHang> list = new ArrayList<>();
        if (snapshot != null && !snapshot.isEmpty()) {
            for (DocumentSnapshot doc : snapshot.getDocuments()) {
                DonHang dh = doc.toObject(DonHang.class);
                if (dh != null) {
                    if (dh.getMaDonHang() == null || dh.getMaDonHang().isEmpty()) {
                        dh.setMaDonHang(doc.getId());
                    }
                    list.add(dh);
                }
            }
        }
        return list;
    }
}
