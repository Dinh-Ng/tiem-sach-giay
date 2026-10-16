package com.fpoly.tiemsachgiay.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Model đại diện cho một item trong Giỏ Hàng.
 */
public class GioHang {

    private Sach sach;
    private int soLuong;

    public GioHang(Sach sach, int soLuong) {
        this.sach = sach;
        this.soLuong = soLuong;
    }

    public Sach getSach() {
        return sach;
    }

    public void setSach(Sach sach) {
        this.sach = sach;
    }

    public int getSoLuong() {
        return soLuong;
    }

    public void setSoLuong(int soLuong) {
        this.soLuong = soLuong;
    }

    /** Tính thành tiền cho item này dựa trên giá giảm (nếu có). */
    public double getThanhTien() {
        if (sach == null) return 0;
        double gia = sach.getGiaGiam() > 0 && sach.getGiaGiam() < sach.getGiaGoc()
                ? sach.getGiaGiam()
                : sach.getGiaGoc();
        return gia * soLuong;
    }

    // ─── Singleton giỏ hàng trong phiên ───────────────────────────────────────

    private static final List<GioHang> gioHangList = new ArrayList<>();

    public static List<GioHang> getGioHangList() {
        return gioHangList;
    }

    /**
     * Thêm sách vào giỏ. Nếu đã tồn tại thì tăng số lượng.
     * @return true nếu thêm/tăng thành công, false nếu vượt tồn kho.
     */
    public static boolean addToCart(Sach sach, int soLuong) {
        for (GioHang item : gioHangList) {
            if (item.getSach() != null && item.getSach().getMaSach() != null
                    && item.getSach().getMaSach().equals(sach.getMaSach())) {
                int newQty = item.getSoLuong() + soLuong;
                if (newQty > sach.getSoLuong()) return false;
                item.setSoLuong(newQty);
                return true;
            }
        }
        if (soLuong > sach.getSoLuong()) return false;
        gioHangList.add(new GioHang(sach, soLuong));
        return true;
    }

    /** Xóa 1 item khỏi giỏ hàng. */
    public static void removeFromCart(Sach sach) {
        gioHangList.removeIf(item ->
                item.getSach() != null
                        && item.getSach().getMaSach() != null
                        && item.getSach().getMaSach().equals(sach.getMaSach())
        );
    }

    /** Tổng tiền toàn giỏ. */
    public static double getTotalPrice() {
        double total = 0;
        for (GioHang item : gioHangList) {
            total += item.getThanhTien();
        }
        return total;
    }

    /** Tổng số lượng item trong giỏ (tính theo đầu sản phẩm riêng biệt). */
    public static int getTotalItems() {
        return gioHangList.size();
    }

    /** Xoá toàn bộ giỏ. */
    public static void clearCart() {
        gioHangList.clear();
    }
}
