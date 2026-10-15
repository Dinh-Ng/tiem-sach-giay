package com.fpoly.tiemsachgiay.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DbHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "tiem_sach_giay.db";
    private static final int DB_VERSION = 1;

    public static final String TABLE_NHAN_VIEN = "NhanVien";
    public static final String TABLE_KHACH_HANG = "KhachHang";

    public DbHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Tạo bảng Nhân viên
        String createTableNhanVien = "CREATE TABLE " + TABLE_NHAN_VIEN + " (" +
                "maNV TEXT PRIMARY KEY, " +
                "hoTen TEXT NOT NULL, " +
                "matKhau TEXT NOT NULL, " +
                "chucVu TEXT NOT NULL)";
        db.execSQL(createTableNhanVien);

        // Tạo bảng Khách hàng
        String createTableKhachHang = "CREATE TABLE " + TABLE_KHACH_HANG + " (" +
                "maKH INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "tenKH TEXT NOT NULL, " +
                "taiKhoan TEXT UNIQUE NOT NULL, " +
                "matKhau TEXT NOT NULL, " +
                "sdt TEXT, " +
                "diaChi TEXT)";
        db.execSQL(createTableKhachHang);

        // Chèn dữ liệu nhân viên mặc định
        db.execSQL("INSERT INTO " + TABLE_NHAN_VIEN + " (maNV, hoTen, matKhau, chucVu) " +
                "VALUES ('admin', 'Quản lý Tiệm Sách', 'admin123', 'Quản lý')");
        db.execSQL("INSERT INTO " + TABLE_NHAN_VIEN + " (maNV, hoTen, matKhau, chucVu) " +
                "VALUES ('nv01', 'Nguyễn Văn Nam', '123456', 'Thu ngân')");

        // Chèn dữ liệu khách hàng mẫu
        db.execSQL("INSERT INTO " + TABLE_KHACH_HANG + " (tenKH, taiKhoan, matKhau, sdt, diaChi) " +
                "VALUES ('Khách Hàng Mẫu', 'khachhang', '123456', '0912345678', 'Hà Nội')");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NHAN_VIEN);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_KHACH_HANG);
        onCreate(db);
    }
}
