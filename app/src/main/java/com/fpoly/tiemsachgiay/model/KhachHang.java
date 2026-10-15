package com.fpoly.tiemsachgiay.model;

public class KhachHang {
    private String tenKH;
    private String taiKhoan;
    private String matKhau;
    private String sdt;
    private String diaChi;

    public KhachHang() {
    }

    public KhachHang(String tenKH, String sdt, String matKhau) {
        this.tenKH = tenKH;
        this.sdt = sdt;
        this.taiKhoan = sdt;
        this.matKhau = matKhau;
        this.diaChi = "";
    }

    public KhachHang(String tenKH, String taiKhoan, String matKhau, String sdt, String diaChi) {
        this.tenKH = tenKH;
        this.taiKhoan = taiKhoan;
        this.matKhau = matKhau;
        this.sdt = sdt;
        this.diaChi = diaChi;
    }

    public String getTenKH() {
        return tenKH;
    }

    public void setTenKH(String tenKH) {
        this.tenKH = tenKH;
    }

    public String getTaiKhoan() {
        return taiKhoan;
    }

    public void setTaiKhoan(String taiKhoan) {
        this.taiKhoan = taiKhoan;
    }

    public String getMatKhau() {
        return matKhau;
    }

    public void setMatKhau(String matKhau) {
        this.matKhau = matKhau;
    }

    public String getSdt() {
        return sdt;
    }

    public void setSdt(String sdt) {
        this.sdt = sdt;
    }

    public String getDiaChi() {
        return diaChi;
    }

    public void setDiaChi(String diaChi) {
        this.diaChi = diaChi;
    }
}