package com.fpoly.tiemsachgiay.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.fpoly.tiemsachgiay.database.DbHelper;
import com.fpoly.tiemsachgiay.model.KhachHang;

public class KhachHangDAO {

    private final DbHelper dbHelper;

    public KhachHangDAO(Context context) {
        dbHelper = new DbHelper(context);
    }

    public KhachHang checkLogin(String account, String matKhau) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String sql = "SELECT * FROM " + DbHelper.TABLE_KHACH_HANG +
                " WHERE (taiKhoan = ? OR sdt = ?) AND matKhau = ?";
        Cursor cursor = db.rawQuery(sql, new String[]{account, account, matKhau});

        KhachHang khachHang = null;
        if (cursor != null && cursor.moveToFirst()) {
            int indexMaKH = cursor.getColumnIndex("maKH");
            int indexTenKH = cursor.getColumnIndex("tenKH");
            int indexTaiKhoan = cursor.getColumnIndex("taiKhoan");
            int indexMatKhau = cursor.getColumnIndex("matKhau");
            int indexSdt = cursor.getColumnIndex("sdt");
            int indexDiaChi = cursor.getColumnIndex("diaChi");

            int id = indexMaKH != -1 ? cursor.getInt(indexMaKH) : 0;
            String ten = indexTenKH != -1 ? cursor.getString(indexTenKH) : "";
            String acc = indexTaiKhoan != -1 ? cursor.getString(indexTaiKhoan) : account;
            String pass = indexMatKhau != -1 ? cursor.getString(indexMatKhau) : matKhau;
            String sdt = indexSdt != -1 ? cursor.getString(indexSdt) : "";
            String diaChi = indexDiaChi != -1 ? cursor.getString(indexDiaChi) : "";

            khachHang = new KhachHang(id, ten, acc, pass, sdt, diaChi);
            cursor.close();
        }
        return khachHang;
    }

    public boolean checkAccountExists(String account) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String sql = "SELECT * FROM " + DbHelper.TABLE_KHACH_HANG + " WHERE taiKhoan = ? OR sdt = ?";
        Cursor cursor = db.rawQuery(sql, new String[]{account, account});
        boolean exists = (cursor != null && cursor.getCount() > 0);
        if (cursor != null) {
            cursor.close();
        }
        return exists;
    }

    public boolean register(KhachHang kh) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("tenKH", kh.getTenKH());
        values.put("taiKhoan", kh.getTaiKhoan());
        values.put("matKhau", kh.getMatKhau());
        values.put("sdt", kh.getSdt());
        values.put("diaChi", kh.getDiaChi());

        long result = db.insert(DbHelper.TABLE_KHACH_HANG, null, values);
        return result != -1;
    }
}
