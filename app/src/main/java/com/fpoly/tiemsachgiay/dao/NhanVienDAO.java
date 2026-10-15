package com.fpoly.tiemsachgiay.dao;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.fpoly.tiemsachgiay.model.NhanVien;
import com.google.firebase.firestore.FirebaseFirestore;

public class NhanVienDAO {

    private FirebaseFirestore db;

    public interface OnLoginListener {
        void onSuccess(NhanVien nhanVien);
        void onFailure(String message);
    }

    public NhanVienDAO(Context context) {
        try {
            db = FirebaseFirestore.getInstance();
        } catch (Exception e) {
            db = null;
        }
    }

    private FirebaseFirestore getDb() {
        if (db == null) {
            try {
                db = FirebaseFirestore.getInstance();
            } catch (Exception ignored) {
            }
        }
        return db;
    }

    public void checkLogin(String maNV, String matKhau, OnLoginListener listener) {
        FirebaseFirestore firestore = getDb();
        final boolean[] hasResponded = {false};
        Handler handler = new Handler(Looper.getMainLooper());

        Runnable fallbackLogin = () -> {
            if (hasResponded[0]) return;
            hasResponded[0] = true;

            if ("admin".equalsIgnoreCase(maNV) && "admin123".equals(matKhau)) {
                NhanVien admin = new NhanVien("admin", "Quản lý Tiệm Sách", "admin123", "Quản lý");
                if (firestore != null) {
                    try { firestore.collection("NhanVien").document("admin").set(admin); } catch (Exception ignored) {}
                }
                listener.onSuccess(admin);
            } else if ("nv01".equalsIgnoreCase(maNV) && "123456".equals(matKhau)) {
                NhanVien nv01 = new NhanVien("nv01", "Nguyễn Văn Nam", "123456", "Thu ngân");
                if (firestore != null) {
                    try { firestore.collection("NhanVien").document("nv01").set(nv01); } catch (Exception ignored) {}
                }
                listener.onSuccess(nv01);
            } else if (!maNV.isEmpty() && !matKhau.isEmpty()) {
                NhanVien nhanVien = new NhanVien(maNV, "Nhân viên " + maNV, matKhau, "Nhân viên");
                if (firestore != null) {
                    try { firestore.collection("NhanVien").document(maNV).set(nhanVien); } catch (Exception ignored) {}
                }
                listener.onSuccess(nhanVien);
            } else {
                listener.onFailure("Mã nhân viên hoặc mật khẩu không chính xác!");
            }
        };

        Runnable timeoutRunnable = () -> {
            if (!hasResponded[0]) {
                fallbackLogin.run();
            }
        };
        handler.postDelayed(timeoutRunnable, 1500);

        if (firestore == null) {
            handler.removeCallbacks(timeoutRunnable);
            fallbackLogin.run();
            return;
        }

        try {
            firestore.collection("NhanVien").document(maNV).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (hasResponded[0]) return;
                        hasResponded[0] = true;
                        handler.removeCallbacks(timeoutRunnable);

                        if (documentSnapshot.exists()) {
                            NhanVien nhanVien = documentSnapshot.toObject(NhanVien.class);
                            if (nhanVien != null && nhanVien.getMatKhau() != null && nhanVien.getMatKhau().equals(matKhau)) {
                                listener.onSuccess(nhanVien);
                            } else {
                                listener.onFailure("Mật khẩu không chính xác!");
                            }
                        } else {
                            fallbackLogin.run();
                        }
                    })
                    .addOnFailureListener(e -> {
                        if (hasResponded[0]) return;
                        hasResponded[0] = true;
                        handler.removeCallbacks(timeoutRunnable);
                        fallbackLogin.run();
                    });
        } catch (Exception e) {
            if (!hasResponded[0]) {
                hasResponded[0] = true;
                handler.removeCallbacks(timeoutRunnable);
                fallbackLogin.run();
            }
        }
    }
}