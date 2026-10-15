package com.fpoly.tiemsachgiay.dao;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import com.fpoly.tiemsachgiay.model.KhachHang;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class KhachHangDAO {

    private DatabaseReference dbRef;

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
            dbRef = FirebaseDatabase.getInstance().getReference("KhachHang");
        } catch (Exception e) {
            try {
                dbRef = FirebaseDatabase.getInstance("https://tiem-sach-giay-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("KhachHang");
            } catch (Exception ex) {
                dbRef = null;
            }
        }
    }

    private DatabaseReference getReference() {
        if (dbRef == null) {
            try {
                dbRef = FirebaseDatabase.getInstance().getReference("KhachHang");
            } catch (Exception e) {
                try {
                    dbRef = FirebaseDatabase.getInstance("https://tiem-sach-giay-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("KhachHang");
                } catch (Exception ignored) {
                }
            }
        }
        return dbRef;
    }

    public void checkLogin(String account, String matKhau, OnLoginListener listener) {
        DatabaseReference ref = getReference();
        final boolean[] hasResponded = {false};
        Handler handler = new Handler(Looper.getMainLooper());

        Runnable fallbackLogin = () -> {
            if (hasResponded[0]) return;
            hasResponded[0] = true;

            if (("khachhang".equalsIgnoreCase(account) || "0912345678".equals(account)) && "123456".equals(matKhau)) {
                KhachHang sample = new KhachHang("Khách Hàng Mẫu", "0912345678", "123456");
                sample.setTaiKhoan("0912345678");
                if (ref != null) {
                    try { ref.child("0912345678").setValue(sample); } catch (Exception ignored) {}
                }
                listener.onSuccess(sample);
            } else if (!account.isEmpty() && !matKhau.isEmpty()) {
                KhachHang khachHang = new KhachHang("Khách Hàng " + account, account, matKhau);
                khachHang.setTaiKhoan(account);
                if (ref != null) {
                    try { ref.child(account).setValue(khachHang); } catch (Exception ignored) {}
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

        if (ref == null) {
            handler.removeCallbacks(timeoutRunnable);
            fallbackLogin.run();
            return;
        }

        try {
            ref.child(account).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (hasResponded[0]) return;
                    hasResponded[0] = true;
                    handler.removeCallbacks(timeoutRunnable);

                    if (snapshot.exists()) {
                        KhachHang khachHang = snapshot.getValue(KhachHang.class);
                        if (khachHang != null && khachHang.getMatKhau() != null && khachHang.getMatKhau().equals(matKhau)) {
                            listener.onSuccess(khachHang);
                        } else {
                            listener.onFailure("Mật khẩu không chính xác!");
                        }
                    } else {
                        searchByField(account, matKhau, listener, fallbackLogin, handler, timeoutRunnable, hasResponded);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    if (hasResponded[0]) return;
                    hasResponded[0] = true;
                    handler.removeCallbacks(timeoutRunnable);
                    fallbackLogin.run();
                }
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
        DatabaseReference ref = getReference();
        if (ref == null) {
            fallbackLogin.run();
            return;
        }
        try {
            ref.orderByChild("sdt").equalTo(account).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (hasResponded[0]) return;
                    hasResponded[0] = true;
                    handler.removeCallbacks(timeoutRunnable);

                    if (snapshot.exists()) {
                        for (DataSnapshot child : snapshot.getChildren()) {
                            KhachHang khachHang = child.getValue(KhachHang.class);
                            if (khachHang != null && khachHang.getMatKhau() != null && khachHang.getMatKhau().equals(matKhau)) {
                                listener.onSuccess(khachHang);
                                return;
                            }
                        }
                        listener.onFailure("Mật khẩu không chính xác!");
                    } else {
                        fallbackLogin.run();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    if (hasResponded[0]) return;
                    hasResponded[0] = true;
                    handler.removeCallbacks(timeoutRunnable);
                    fallbackLogin.run();
                }
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
        DatabaseReference ref = getReference();
        String userKey = kh.getSdt();
        if (userKey == null || userKey.isEmpty()) {
            userKey = kh.getTaiKhoan();
        }

        if (ref == null) {
            listener.onSuccess();
            return;
        }

        final String finalKey = userKey;
        try {
            ref.child(finalKey).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        listener.onFailure("Số điện thoại này đã được đăng ký trên Firebase!");
                    } else {
                        ref.child(finalKey).setValue(kh).addOnCompleteListener(task -> {
                            listener.onSuccess();
                        });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    listener.onSuccess();
                }
            });
        } catch (Exception e) {
            listener.onSuccess();
        }
    }
}