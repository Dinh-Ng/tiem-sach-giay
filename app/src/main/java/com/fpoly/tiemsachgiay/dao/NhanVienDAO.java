package com.fpoly.tiemsachgiay.dao;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.fpoly.tiemsachgiay.database.DbHelper;
import com.fpoly.tiemsachgiay.model.NhanVien;

public class NhanVienDAO {

    private final DbHelper dbHelper;

    public NhanVienDAO(Context context) {
        dbHelper = new DbHelper(context);
    }

    public NhanVien checkLogin(String maNV, String matKhau) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String sql = "SELECT * FROM " + DbHelper.TABLE_NHAN_VIEN + " WHERE maNV = ? AND matKhau = ?";
        Cursor cursor = db.rawQuery(sql, new String[]{maNV, matKhau});

        NhanVien nhanVien = null;
        if (cursor != null && cursor.moveToFirst()) {
            int indexMaNV = cursor.getColumnIndex("maNV");
            int indexHoTen = cursor.getColumnIndex("hoTen");
            int indexMatKhau = cursor.getColumnIndex("matKhau");
            int indexChucVu = cursor.getColumnIndex("chucVu");

            String id = indexMaNV != -1 ? cursor.getString(indexMaNV) : maNV;
            String name = indexHoTen != -1 ? cursor.getString(indexHoTen) : "";
            String pass = indexMatKhau != -1 ? cursor.getString(indexMatKhau) : matKhau;
            String role = indexChucVu != -1 ? cursor.getString(indexChucVu) : "";

            nhanVien = new NhanVien(id, name, pass, role);
            cursor.close();
        }
        return nhanVien;
    }
}
