package com.fpoly.tiemsachgiay.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fpoly.tiemsachgiay.R;
import com.fpoly.tiemsachgiay.model.DonHang;
import com.google.android.material.button.MaterialButton;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Adapter hiển thị danh sách đơn hàng cho cả Khách hàng và Nhân viên.
 */
public class DonHangAdapter extends RecyclerView.Adapter<DonHangAdapter.DonHangViewHolder> {

    private final Context context;
    private final List<DonHang> donHangList;
    private final boolean isEmployeeMode;
    private final OnDonHangActionListener listener;

    private final DecimalFormat currencyFormat = new DecimalFormat("#,### ₫");
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

    public interface OnDonHangActionListener {
        void onUpdateStatus(DonHang donHang);
        void onDeleteOrder(DonHang donHang);
    }

    public DonHangAdapter(Context context, List<DonHang> donHangList, boolean isEmployeeMode, OnDonHangActionListener listener) {
        this.context = context;
        this.donHangList = donHangList;
        this.isEmployeeMode = isEmployeeMode;
        this.listener = listener;
    }

    @NonNull
    @Override
    public DonHangViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_don_hang, parent, false);
        return new DonHangViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DonHangViewHolder holder, int position) {
        DonHang donHang = donHangList.get(position);
        if (donHang == null) return;

        // Mã đơn
        String maDon = donHang.getMaDonHang() != null ? donHang.getMaDonHang() : "N/A";
        holder.tvMaDon.setText("#" + maDon);

        // Ngày đặt
        if (donHang.getNgayDat() > 0) {
            holder.tvNgayDat.setText("📅 " + dateFormat.format(new Date(donHang.getNgayDat())));
        } else {
            holder.tvNgayDat.setText("📅 Mới đặt");
        }

        // Trạng thái đơn hàng
        String trangThai = donHang.getTrangThai() != null ? donHang.getTrangThai() : "Chờ xử lý";
        holder.tvTrangThai.setText(trangThai);
        applyStatusColor(holder.tvTrangThai, trangThai);

        // Người nhận & SĐT
        String nguoiNhan = donHang.getTenKhachHang() != null ? donHang.getTenKhachHang() : "Khách hàng";
        String sdt = donHang.getSdtNhan() != null && !donHang.getSdtNhan().isEmpty() ? donHang.getSdtNhan() : donHang.getKhachHangId();
        holder.tvNguoiNhan.setText("👤 " + nguoiNhan + (sdt != null && !sdt.isEmpty() ? " • " + sdt : ""));

        // Địa chỉ
        String diaChi = donHang.getDiaChi() != null && !donHang.getDiaChi().isEmpty()
                ? donHang.getDiaChi()
                : "Chưa cập nhật địa chỉ";
        holder.tvDiaChi.setText("📍 " + diaChi);

        // Phương thức thanh toán
        String pt = donHang.getPhuongThucThanhToan() != null && !donHang.getPhuongThucThanhToan().isEmpty()
                ? donHang.getPhuongThucThanhToan()
                : "Tiền mặt khi nhận hàng (COD)";
        holder.tvThanhToan.setText("💳 " + pt);

        // Ghi chú nếu có
        if (donHang.getGhiChu() != null && !donHang.getGhiChu().trim().isEmpty()) {
            holder.tvGhiChu.setVisibility(View.VISIBLE);
            holder.tvGhiChu.setText("📝 Ghi chú: " + donHang.getGhiChu().trim());
        } else {
            holder.tvGhiChu.setVisibility(View.GONE);
        }

        // Tóm tắt danh sách sách
        StringBuilder sb = new StringBuilder();
        if (donHang.getDanhSachSach() != null && !donHang.getDanhSachSach().isEmpty()) {
            int tongSoLuong = 0;
            for (DonHang.ItemDonHang item : donHang.getDanhSachSach()) {
                tongSoLuong += item.getSoLuong();
            }
            sb.append("📚 ").append(tongSoLuong).append(" sản phẩm:\n");
            for (DonHang.ItemDonHang item : donHang.getDanhSachSach()) {
                sb.append("• ").append(item.getTenSach() != null ? item.getTenSach() : "Sách")
                        .append(" (x").append(item.getSoLuong()).append(")\n");
            }
            if (sb.length() > 0 && sb.charAt(sb.length() - 1) == '\n') {
                sb.deleteCharAt(sb.length() - 1);
            }
        } else {
            sb.append("📚 Không có chi tiết sản phẩm");
        }
        holder.tvDanhSachSach.setText(sb.toString());

        // Tổng tiền
        holder.tvTongTien.setText(currencyFormat.format(donHang.getTongTien()));

        // Phân quyền hiển thị nút hành động
        if (isEmployeeMode) {
            holder.layoutNhanVienActions.setVisibility(View.VISIBLE);
            holder.btnKhachHuyDon.setVisibility(View.GONE);

            // Đơn đã hủy không thể cập nhật lại trạng thái nữa
            if ("Đã hủy".equalsIgnoreCase(trangThai)) {
                holder.btnDoiTrangThai.setVisibility(View.GONE);
            } else {
                holder.btnDoiTrangThai.setVisibility(View.VISIBLE);
                holder.btnDoiTrangThai.setOnClickListener(v -> {
                    if (listener != null) listener.onUpdateStatus(donHang);
                });
            }

            holder.btnXoaDon.setOnClickListener(v -> {
                if (listener != null) listener.onDeleteOrder(donHang);
            });
        } else {
            holder.layoutNhanVienActions.setVisibility(View.GONE);

            // Khách hàng có thể hủy đơn nếu đơn đang ở trạng thái "Chờ xử lý"
            if ("Chờ xử lý".equalsIgnoreCase(trangThai)) {
                holder.btnKhachHuyDon.setVisibility(View.VISIBLE);
                holder.btnKhachHuyDon.setOnClickListener(v -> {
                    if (listener != null) listener.onUpdateStatus(donHang);
                });
            } else {
                holder.btnKhachHuyDon.setVisibility(View.GONE);
            }
        }
    }

    private void applyStatusColor(TextView tvStatus, String status) {
        if ("Chờ xử lý".equalsIgnoreCase(status) || "Đang xử lý".equalsIgnoreCase(status)) {
            tvStatus.setTextColor(Color.parseColor("#D97706")); // Cam
        } else if ("Đang giao".equalsIgnoreCase(status)) {
            tvStatus.setTextColor(Color.parseColor("#1976D2")); // Xanh dương
        } else if ("Đã giao".equalsIgnoreCase(status)) {
            tvStatus.setTextColor(Color.parseColor("#2E7D32")); // Xanh lá
        } else if ("Đã hủy".equalsIgnoreCase(status)) {
            tvStatus.setTextColor(Color.parseColor("#D32F2F")); // Đỏ
        } else {
            tvStatus.setTextColor(Color.parseColor("#786A60"));
        }
    }

    @Override
    public int getItemCount() {
        return donHangList != null ? donHangList.size() : 0;
    }

    static class DonHangViewHolder extends RecyclerView.ViewHolder {
        TextView tvMaDon, tvTrangThai, tvNgayDat, tvNguoiNhan, tvDiaChi, tvThanhToan, tvGhiChu, tvDanhSachSach, tvTongTien;
        LinearLayout layoutNhanVienActions;
        MaterialButton btnDoiTrangThai, btnXoaDon, btnKhachHuyDon;

        public DonHangViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMaDon = itemView.findViewById(R.id.tv_item_don_hang_ma);
            tvTrangThai = itemView.findViewById(R.id.tv_item_don_hang_trang_thai);
            tvNgayDat = itemView.findViewById(R.id.tv_item_don_hang_ngay_dat);
            tvNguoiNhan = itemView.findViewById(R.id.tv_item_don_hang_nguoi_nhan);
            tvDiaChi = itemView.findViewById(R.id.tv_item_don_hang_dia_chi);
            tvThanhToan = itemView.findViewById(R.id.tv_item_don_hang_thanh_toan);
            tvGhiChu = itemView.findViewById(R.id.tv_item_don_hang_ghi_chu);
            tvDanhSachSach = itemView.findViewById(R.id.tv_item_don_hang_danh_sach_sach);
            tvTongTien = itemView.findViewById(R.id.tv_item_don_hang_tong_tien);

            layoutNhanVienActions = itemView.findViewById(R.id.layout_nhan_vien_actions);
            btnDoiTrangThai = itemView.findViewById(R.id.btn_item_doi_trang_thai);
            btnXoaDon = itemView.findViewById(R.id.btn_item_xoa_don);
            btnKhachHuyDon = itemView.findViewById(R.id.btn_item_khach_huy_don);
        }
    }
}
