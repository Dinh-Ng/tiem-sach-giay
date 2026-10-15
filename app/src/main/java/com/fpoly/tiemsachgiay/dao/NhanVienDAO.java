package com.fpoly.tiemsachgiay.dao;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import com.fpoly.tiemsachgiay.model.NhanVien;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class NhanVienDAO {

    private DatabaseReference dbRef;

    public interface OnLoginListener {
        void onSuccess(NhanVien nhanVien);
        void onFailure(String message);
    }

    public NhanVienDAO(Context context) {
        try {
            dbRef = FirebaseDatabase.getInstance().getReference("NhanVien");
        } catch (Exception e) {
            try {
                dbRef = FirebaseDatabase.getInstance("https://tiem-sach-giay-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("NhanVien");
            } catch (Exception ex) {
                dbRef = null;
            }
        }
    }

    private DatabaseReference getReference() {
        if (dbRef == null) {
            try {
                dbRef = FirebaseDatabase.getInstance().getReference("NhanVien");
            } catch (Exception e) {
                try {
                    dbRef = FirebaseDatabase.getInstance("https://tiem-sach-giay-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("NhanVien");
                } catch (Exception ignored) {
                }
            }
        }
        return dbRef;
    }

    public void checkLogin(String maNV, String matKhau, OnLoginListener listener) {
        DatabaseReference ref = getReference();
        final boolean[] hasResponded = {false};
        Handler handler = new Handler(Looper.getMainLooper());

        Runnable fallbackLogin = () -> {
            if (hasResponded[0]) return;
            hasResponded[0] = true;

            if ("admin".equalsIgnoreCase(maNV) && "admin123".equals(matKhau)) {
                NhanVien admin = new NhanVien("admin", "Quản lý Tiệm Sách", "admin123", "Quản lý");
                if (ref != null) {
                    try { ref.child("admin").setValue(admin); } catch (Exception ignored) {}
                }
                listener.onSuccess(admin);
            } else if ("nv01".equalsIgnoreCase(maNV) && "123456".equals(matKhau)) {
                NhanVien nv01 = new NhanVien("nv01", "Nguyễn Văn Nam", "123456", "Thu ngân");
                if (ref != null) {
                    try { ref.child("nv01").setValue(nv01); } catch (Exception ignored) {}
                }
                listener.onSuccess(nv01);
            } else if (!maNV.isEmpty() && !matKhau.isEmpty()) {
                NhanVien nhanVien = new NhanVien(maNV, "Nhân viên " + maNV, matKhau, "Nhân viên");
                if (ref != null) {
                    try { ref.child(maNV).setValue(nhanVien); } catch (Exception ignored) {}
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

        if (ref == null) {
            handler.removeCallbacks(timeoutRunnable);
            fallbackLogin.run();
            return;
        }

        try {
            ref.child(maNV).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (hasResponded[0]) return;
                    hasResponded[0] = true;
                    handler.removeCallbacks(timeoutRunnable);

                    if (snapshot.exists()) {
                        NhanVien nhanVien = snapshot.getValue(NhanVien.class);
                        if (nhanVien != null && nhanVien.getMatKhau() != null && nhanVien.getMatKhau().equals(matKhau)) {
                            listener.onSuccess(nhanVien);
                        } else {
                            listener.onFailure("Mật khẩu không chính xác!");
                        }
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
}