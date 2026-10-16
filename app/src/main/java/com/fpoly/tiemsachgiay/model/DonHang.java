package com.fpoly.tiemsachgiay.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Model đại diện cho một Đơn Hàng.
 * Tương ứng với collection "DonHang" trên Firestore.
 */
public class DonHang {

    private String maDonHang;           // ID tài liệu Firestore (tự sinh)
    private String khachHangId;         // SĐT hoặc mã tài khoản khách hàng
    private String tenKhachHang;        // Tên hiển thị khách hàng
    private String sdtNhan;             // Số điện thoại nhận hàng
    private String diaChi;              // Địa chỉ giao hàng
    private String phuongThucThanhToan; // "Tiền mặt khi nhận hàng (COD)" | "Chuyển khoản ngân hàng" | "Ví điện tử"
    private String ghiChu;              // Ghi chú đơn hàng (nếu có)
    private long ngayDat;               // Timestamp (milliseconds) lúc đặt hàng
    private double tongTien;            // Tổng tiền đơn hàng
    private String trangThai;           // "Chờ xử lý" | "Đang giao" | "Đã giao" | "Đã hủy"
    private List<ItemDonHang> danhSachSach; // Chi tiết các sản phẩm trong đơn

    /** Constructor không tham số — bắt buộc để Firestore có thể deserialize. */
    public DonHang() {
    }

    public DonHang(String khachHangId, String tenKhachHang, long ngayDat,
                   double tongTien, String trangThai, List<ItemDonHang> danhSachSach) {
        this.khachHangId = khachHangId;
        this.tenKhachHang = tenKhachHang;
        this.ngayDat = ngayDat;
        this.tongTien = tongTien;
        this.trangThai = trangThai;
        this.danhSachSach = danhSachSach != null ? danhSachSach : new ArrayList<>();
    }

    public DonHang(String khachHangId, String tenKhachHang, String sdtNhan,
                   String diaChi, String phuongThucThanhToan, String ghiChu,
                   long ngayDat, double tongTien, String trangThai,
                   List<ItemDonHang> danhSachSach) {
        this.khachHangId = khachHangId;
        this.tenKhachHang = tenKhachHang;
        this.sdtNhan = sdtNhan;
        this.diaChi = diaChi;
        this.phuongThucThanhToan = phuongThucThanhToan;
        this.ghiChu = ghiChu;
        this.ngayDat = ngayDat;
        this.tongTien = tongTien;
        this.trangThai = trangThai;
        this.danhSachSach = danhSachSach != null ? danhSachSach : new ArrayList<>();
    }

    // ─── Getters & Setters ─────────────────────────────────────────────────────

    public String getMaDonHang() { return maDonHang; }
    public void setMaDonHang(String maDonHang) { this.maDonHang = maDonHang; }

    public String getKhachHangId() { return khachHangId; }
    public void setKhachHangId(String khachHangId) { this.khachHangId = khachHangId; }

    public String getTenKhachHang() { return tenKhachHang; }
    public void setTenKhachHang(String tenKhachHang) { this.tenKhachHang = tenKhachHang; }

    public String getSdtNhan() { return sdtNhan; }
    public void setSdtNhan(String sdtNhan) { this.sdtNhan = sdtNhan; }

    public String getDiaChi() { return diaChi; }
    public void setDiaChi(String diaChi) { this.diaChi = diaChi; }

    public String getPhuongThucThanhToan() { return phuongThucThanhToan; }
    public void setPhuongThucThanhToan(String phuongThucThanhToan) { this.phuongThucThanhToan = phuongThucThanhToan; }

    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }

    public long getNgayDat() { return ngayDat; }
    public void setNgayDat(long ngayDat) { this.ngayDat = ngayDat; }

    public double getTongTien() { return tongTien; }
    public void setTongTien(double tongTien) { this.tongTien = tongTien; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }

    public List<ItemDonHang> getDanhSachSach() { return danhSachSach; }
    public void setDanhSachSach(List<ItemDonHang> danhSachSach) { this.danhSachSach = danhSachSach; }

    // ─── Inner class: Chi tiết từng sản phẩm ─────────────────────────────────

    /**
     * Một dòng chi tiết trong đơn hàng (1 đầu sách + số lượng + giá).
     * Lưu dưới dạng nested object trong Firestore.
     */
    public static class ItemDonHang {
        private String maSach;
        private String tenSach;
        private String tacGia;
        private double donGia;
        private int soLuong;
        private double thanhTien;

        /** Constructor không tham số — bắt buộc cho Firestore. */
        public ItemDonHang() {
        }

        public ItemDonHang(String maSach, String tenSach, String tacGia,
                           double donGia, int soLuong) {
            this.maSach = maSach;
            this.tenSach = tenSach;
            this.tacGia = tacGia;
            this.donGia = donGia;
            this.soLuong = soLuong;
            this.thanhTien = donGia * soLuong;
        }

        public String getMaSach() { return maSach; }
        public void setMaSach(String maSach) { this.maSach = maSach; }

        public String getTenSach() { return tenSach; }
        public void setTenSach(String tenSach) { this.tenSach = tenSach; }

        public String getTacGia() { return tacGia; }
        public void setTacGia(String tacGia) { this.tacGia = tacGia; }

        public double getDonGia() { return donGia; }
        public void setDonGia(double donGia) { this.donGia = donGia; }

        public int getSoLuong() { return soLuong; }
        public void setSoLuong(int soLuong) { this.soLuong = soLuong; }

        public double getThanhTien() { return thanhTien; }
        public void setThanhTien(double thanhTien) { this.thanhTien = thanhTien; }
    }
}
