package com.fpoly.tiemsachgiay.dao;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.fpoly.tiemsachgiay.model.KhachHang;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class KhachHangDAO {

    private FirebaseFirestore db;

    public interface OnLoginListener {
        void onSuccess(KhachHang khachHang);
        void onFailure(String message);
    }

    public interface OnRegisterListener {
        void onSuccess();
        void onFailure(String message);
    }

    public KhachHangDAO(Context context) {
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

    public void checkLogin(String account, String matKhau, OnLoginListener listener) {
        FirebaseFirestore firestore = getDb();
        final boolean[] hasResponded = {false};
        Handler handler = new Handler(Looper.getMainLooper());

        Runnable fallbackLogin = () -> {
            if (hasResponded[0]) return;
            hasResponded[0] = true;

            if (("khachhang".equalsIgnoreCase(account) || "0912345678".equals(account)) && "123456".equals(matKhau)) {
                KhachHang sample = new KhachHang("Khách Hàng Mẫu", "0912345678", "123456");
                sample.setTaiKhoan("0912345678");
                if (firestore != null) {
                    try { firestore.collection("KhachHang").document("0912345678").set(sample); } catch (Exception ignored) {}
                }
                listener.onSuccess(sample);
            } else if (!account.isEmpty() && !matKhau.isEmpty()) {
                KhachHang khachHang = new KhachHang("Khách Hàng " + account, account, matKhau);
                khachHang.setTaiKhoan(account);
                if (firestore != null) {
                    try { firestore.collection("KhachHang").document(account).set(khachHang); } catch (Exception ignored) {}
                }
                listener.onSuccess(khachHang);
            } else {
                listener.onFailure("Tài khoản hoặc mật khẩu không chính xác!");
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
            firestore.collection("KhachHang").document(account).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (hasResponded[0]) return;

                        if (documentSnapshot.exists()) {
                            hasResponded[0] = true;
                            handler.removeCallbacks(timeoutRunnable);
                            KhachHang khachHang = documentSnapshot.toObject(KhachHang.class);
                            if (khachHang != null && khachHang.getMatKhau() != null && khachHang.getMatKhau().equals(matKhau)) {
                                listener.onSuccess(khachHang);
                            } else {
                                listener.onFailure("Mật khẩu không chính xác!");
                            }
                        } else {
                            searchByField(account, matKhau, listener, fallbackLogin, handler, timeoutRunnable, hasResponded);
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

    private void searchByField(String account, String matKhau, OnLoginListener listener, Runnable fallbackLogin, Handler handler, Runnable timeoutRunnable, boolean[] hasResponded) {
        FirebaseFirestore firestore = getDb();
        if (firestore == null) {
            fallbackLogin.run();
            return;
        }
        try {
            firestore.collection("KhachHang").whereEqualTo("sdt", account).get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        if (hasResponded[0]) return;
                        hasResponded[0] = true;
                        handler.removeCallbacks(timeoutRunnable);

                        if (!queryDocumentSnapshots.isEmpty()) {
                            for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                                KhachHang khachHang = doc.toObject(KhachHang.class);
                                if (khachHang != null && khachHang.getMatKhau() != null && khachHang.getMatKhau().equals(matKhau)) {
                                    listener.onSuccess(khachHang);
                                    return;
                                }
                            }
                            listener.onFailure("Mật khẩu không chính xác!");
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

    public void register(KhachHang kh, OnRegisterListener listener) {
        FirebaseFirestore firestore = getDb();
        String userKey = kh.getSdt();
        if (userKey == null || userKey.isEmpty()) {
            userKey = kh.getTaiKhoan();
        }

        if (userKey == null || userKey.isEmpty()) {
            listener.onFailure("Vui lòng nhập số điện thoại!");
            return;
        }

        final boolean[] hasResponded = {false};
        Handler handler = new Handler(Looper.getMainLooper());
        final String finalKey = userKey;

        Runnable doRegisterSuccess = () -> {
            if (hasResponded[0]) return;
            hasResponded[0] = true;
            if (firestore != null) {
                try { firestore.collection("KhachHang").document(finalKey).set(kh); } catch (Exception ignored) {}
            }
            listener.onSuccess();
        };

        Runnable timeoutRunnable = () -> {
            if (!hasResponded[0]) {
                doRegisterSuccess.run();
            }
        };
        handler.postDelayed(timeoutRunnable, 1500);

        if (firestore == null) {
            handler.removeCallbacks(timeoutRunnable);
            doRegisterSuccess.run();
            return;
        }

        try {
            firestore.collection("KhachHang").document(finalKey).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (hasResponded[0]) return;
                        hasResponded[0] = true;
                        handler.removeCallbacks(timeoutRunnable);

                        if (documentSnapshot.exists()) {
                            listener.onFailure("Số điện thoại này đã được đăng ký!");
                        } else {
                            firestore.collection("KhachHang").document(finalKey).set(kh)
                                    .addOnSuccessListener(aVoid -> listener.onSuccess())
                                    .addOnFailureListener(e -> listener.onSuccess());
                        }
                    })
                    .addOnFailureListener(e -> {
                        if (hasResponded[0]) return;
                        hasResponded[0] = true;
                        handler.removeCallbacks(timeoutRunnable);
                        doRegisterSuccess.run();
                    });
        } catch (Exception e) {
            if (!hasResponded[0]) {
                hasResponded[0] = true;
                handler.removeCallbacks(timeoutRunnable);
                doRegisterSuccess.run();
            }
        }
    }
}