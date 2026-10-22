package com.fpoly.tiemsachgiay.dao;

import android.content.Context;

import com.fpoly.tiemsachgiay.model.HoaDon;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HoaDonDAO {

    private final FirebaseFirestore db;
    private static final String COLLECTION_NAME = "HoaDon";

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

    public HoaDonDAO() {
        db = FirebaseFirestore.getInstance();
    }

    public HoaDonDAO(Context context) {
        db = FirebaseFirestore.getInstance();
    }

    /**
     * Get all invoices from Firestore
     */
    public void getAll(OnDataListLoadedListener<HoaDon> callback) {
        db.collection(COLLECTION_NAME).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<HoaDon> list = new ArrayList<>();
                    if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
                        for (DocumentSnapshot doc : queryDocumentSnapshots) {
                            HoaDon hd = doc.toObject(HoaDon.class);
                            if (hd != null) {
                                if (hd.getMaHoaDon() == null || hd.getMaHoaDon().trim().isEmpty()) {
                                    hd.setMaHoaDon(doc.getId());
                                }
                                list.add(hd);
                            }
                        }
                        callback.onSuccess(list);
                    } else {
                        seedSampleInvoices(callback);
                    }
                })
                .addOnFailureListener(e -> callback.onFailure("Lỗi tải danh sách hóa đơn: " + e.getMessage()));
    }

    /**
     * Seed initial sample invoices with realistic dates
     */
    private void seedSampleInvoices(OnDataListLoadedListener<HoaDon> callback) {
        List<HoaDon> sampleList = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Calendar cal = Calendar.getInstance();

        // Hôm nay
        String today = sdf.format(cal.getTime());

        // Hôm qua
        cal.add(Calendar.DAY_OF_YEAR, -1);
        String yesterday = sdf.format(cal.getTime());

        // 3 ngày trước
        cal.add(Calendar.DAY_OF_YEAR, -2);
        String threeDaysAgo = sdf.format(cal.getTime());

        // 10 ngày trước
        cal.add(Calendar.DAY_OF_YEAR, -7);
        String tenDaysAgo = sdf.format(cal.getTime());

        sampleList.add(new HoaDon("hd_101", "admin", "Quản lý Tiệm Sách", "Khách Hàng Mẫu", "0912345678", today, "09:30", 250000, "Đã thanh toán"));
        sampleList.add(new HoaDon("hd_102", "admin", "Quản lý Tiệm Sách", "Định", "0971851540", today, "11:15", 420000, "Đã thanh toán"));
        sampleList.add(new HoaDon("hd_103", "nv01", "Nguyễn Văn Nam", "Nguyễn Văn A", "0988776655", yesterday, "14:20", 180000, "Đã thanh toán"));
        sampleList.add(new HoaDon("hd_104", "nv01", "Nguyễn Văn Nam", "Trần Thị B", "0977665544", yesterday, "16:45", 310000, "Đã thanh toán"));
        sampleList.add(new HoaDon("hd_105", "admin", "Quản lý Tiệm Sách", "Lê Văn C", "0966554433", threeDaysAgo, "10:00", 550000, "Đã thanh toán"));
        sampleList.add(new HoaDon("hd_106", "nv01", "Nguyễn Văn Nam", "Phạm Thị D", "0955443322", threeDaysAgo, "15:30", 150000, "Đã hủy"));
        sampleList.add(new HoaDon("hd_107", "admin", "Quản lý Tiệm Sách", "Hoàng Văn E", "0944332211", tenDaysAgo, "11:00", 680000, "Đã thanh toán"));

        for (HoaDon hd : sampleList) {
            try {
                db.collection(COLLECTION_NAME).document(hd.getMaHoaDon()).set(hd);
            } catch (Exception ignored) {
            }
        }
        callback.onSuccess(sampleList);
    }

    /**
     * Get invoice by ID
     */
    public void getById(String maHoaDon, OnDataLoadedListener<HoaDon> callback) {
        if (maHoaDon == null || maHoaDon.trim().isEmpty()) {
            callback.onFailure("Mã hóa đơn không hợp lệ!");
            return;
        }

        db.collection(COLLECTION_NAME).document(maHoaDon.trim()).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        HoaDon hd = documentSnapshot.toObject(HoaDon.class);
                        if (hd != null) {
                            hd.setMaHoaDon(documentSnapshot.getId());
                            callback.onSuccess(hd);
                        } else {
                            callback.onFailure("Dữ liệu hóa đơn không hợp lệ!");
                        }
                    } else {
                        callback.onFailure("Không tìm thấy hóa đơn!");
                    }
                })
                .addOnFailureListener(e -> callback.onFailure("Lỗi lấy thông tin hóa đơn: " + e.getMessage()));
    }

    /**
     * Insert new invoice
     */
    public void insert(HoaDon hoaDon, OnActionListener callback) {
        if (hoaDon == null) {
            callback.onFailure("Thông tin hóa đơn không hợp lệ!");
            return;
        }

        if (hoaDon.getTenKhachHang() == null || hoaDon.getTenKhachHang().trim().isEmpty()) {
            callback.onFailure("Tên khách hàng không được để trống!");
            return;
        }

        if (hoaDon.getTongTien() < 0) {
            callback.onFailure("Tổng tiền không được âm!");
            return;
        }

        String docId = hoaDon.getMaHoaDon();
        if (docId == null || docId.trim().isEmpty()) {
            docId = "hd_" + System.currentTimeMillis();
            hoaDon.setMaHoaDon(docId);
        }

        final String finalDocId = docId.trim();

        db.collection(COLLECTION_NAME).document(finalDocId).set(hoaDon)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onFailure("Lỗi thêm hóa đơn: " + e.getMessage()));
    }

    /**
     * Update invoice
     */
    public void update(HoaDon hoaDon, OnActionListener callback) {
        if (hoaDon == null || hoaDon.getMaHoaDon() == null || hoaDon.getMaHoaDon().trim().isEmpty()) {
            callback.onFailure("Mã hóa đơn không hợp lệ!");
            return;
        }

        db.collection(COLLECTION_NAME).document(hoaDon.getMaHoaDon().trim()).set(hoaDon)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onFailure("Lỗi cập nhật hóa đơn: " + e.getMessage()));
    }

    /**
     * Delete invoice
     */
    public void delete(String maHoaDon, OnActionListener callback) {
        if (maHoaDon == null || maHoaDon.trim().isEmpty()) {
            callback.onFailure("Mã hóa đơn không hợp lệ!");
            return;
        }

        db.collection(COLLECTION_NAME).document(maHoaDon.trim()).delete()
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onFailure("Lỗi xóa hóa đơn: " + e.getMessage()));
    }
}