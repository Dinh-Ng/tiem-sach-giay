package com.fpoly.tiemsachgiay.model;

import java.io.Serializable;

public class Sach implements Serializable {
    private String maSach;
    private String tenSach;
    private String tacGia;
    private double giaGoc;
    private double giaGiam;
    private String theLoai;
    private String hinhAnh;
    private int soLuong;

    public Sach() {
    }

    public Sach(String maSach, String tenSach, String tacGia, double giaGoc, double giaGiam, String theLoai, String hinhAnh, int soLuong) {
        this.maSach = maSach;
        this.tenSach = tenSach;
        this.tacGia = tacGia;
        this.giaGoc = giaGoc;
        this.giaGiam = giaGiam;
        this.theLoai = theLoai;
        this.hinhAnh = hinhAnh;
        this.soLuong = soLuong;
    }

    public Sach(String tenSach, String tacGia, double giaGoc, double giaGiam, String theLoai, String hinhAnh, int soLuong) {
        this.tenSach = tenSach;
        this.tacGia = tacGia;
        this.giaGoc = giaGoc;
        this.giaGiam = giaGiam;
        this.theLoai = theLoai;
        this.hinhAnh = hinhAnh;
        this.soLuong = soLuong;
    }

    public String getMaSach() {
        return maSach;
    }

    public void setMaSach(String maSach) {
        this.maSach = maSach;
    }

    public String getTenSach() {
        return tenSach;
    }

    public void setTenSach(String tenSach) {
        this.tenSach = tenSach;
    }

    public String getTacGia() {
        return tacGia;
    }

    public void setTacGia(String tacGia) {
        this.tacGia = tacGia;
    }

    public double getGiaGoc() {
        return giaGoc;
    }

    public void setGiaGoc(double giaGoc) {
        this.giaGoc = giaGoc;
    }

    public double getGiaGiam() {
        return giaGiam;
    }

    public void setGiaGiam(double giaGiam) {
        this.giaGiam = giaGiam;
    }

    public String getTheLoai() {
        return theLoai;
    }

    public void setTheLoai(String theLoai) {
        this.theLoai = theLoai;
    }

    public String getHinhAnh() {
        return hinhAnh;
    }

    public void setHinhAnh(String hinhAnh) {
        this.hinhAnh = hinhAnh;
    }

    public int getSoLuong() {
        return soLuong;
    }

    public void setSoLuong(int soLuong) {
        this.soLuong = soLuong;
    }
}