package com.fpoly.tiemsachgiay.model;

import java.io.Serializable;

public class HoaDon implements Serializable {
    private String maHoaDon;
    private String maNV;
    private String tenNV;
    private String tenKhachHang;
    private String sdtKhachHang;
    private String ngayLap; // Định dạng yyyy-MM-dd
    private String gioLap;  // Định dạng HH:mm
    private double tongTien;
    private String trangThai; // "Đã thanh toán", "Đã hủy", "Chưa thanh toán"

    public HoaDon() {
    }

    public HoaDon(String maHoaDon, String maNV, String tenNV, String tenKhachHang, String sdtKhachHang, String ngayLap, String gioLap, double tongTien, String trangThai) {
        this.maHoaDon = maHoaDon;
        this.maNV = maNV;
        this.tenNV = tenNV;
        this.tenKhachHang = tenKhachHang;
        this.sdtKhachHang = sdtKhachHang;
        this.ngayLap = ngayLap;
        this.gioLap = gioLap;
        this.tongTien = tongTien;
        this.trangThai = trangThai;
    }

    public HoaDon(String tenKhachHang, String sdtKhachHang, String ngayLap, String gioLap, double tongTien, String trangThai) {
        this.tenKhachHang = tenKhachHang;
        this.sdtKhachHang = sdtKhachHang;
        this.ngayLap = ngayLap;
        this.gioLap = gioLap;
        this.tongTien = tongTien;
        this.trangThai = trangThai;
    }

    public String getMaHoaDon() {
        return maHoaDon;
    }

    public void setMaHoaDon(String maHoaDon) {
        this.maHoaDon = maHoaDon;
    }

    public String getMaNV() {
        return maNV;
    }

    public void setMaNV(String maNV) {
        this.maNV = maNV;
    }

    public String getTenNV() {
        return tenNV;
    }

    public void setTenNV(String tenNV) {
        this.tenNV = tenNV;
    }

    public String getTenKhachHang() {
        return tenKhachHang;
    }

    public void setTenKhachHang(String tenKhachHang) {
        this.tenKhachHang = tenKhachHang;
    }

    public String getSdtKhachHang() {
        return sdtKhachHang;
    }

    public void setSdtKhachHang(String sdtKhachHang) {
        this.sdtKhachHang = sdtKhachHang;
    }

    public String getNgayLap() {
        return ngayLap;
    }

    public void setNgayLap(String ngayLap) {
        this.ngayLap = ngayLap;
    }

    public String getGioLap() {
        return gioLap;
    }

    public void setGioLap(String gioLap) {
        this.gioLap = gioLap;
    }

    public double getTongTien() {
        return tongTien;
    }

    public void setTongTien(double tongTien) {
        this.tongTien = tongTien;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }
}