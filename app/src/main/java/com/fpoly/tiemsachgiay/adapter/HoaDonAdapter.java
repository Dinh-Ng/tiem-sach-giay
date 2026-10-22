package com.fpoly.tiemsachgiay.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fpoly.tiemsachgiay.R;
import com.fpoly.tiemsachgiay.model.HoaDon;

import java.text.DecimalFormat;
import java.util.List;

public class HoaDonAdapter extends RecyclerView.Adapter<HoaDonAdapter.HoaDonViewHolder> {

    private final Context context;
    private List<HoaDon> listHoaDon;
    private final DecimalFormat formatter = new DecimalFormat("#,### VNĐ");

    public HoaDonAdapter(Context context, List<HoaDon> listHoaDon) {
        this.context = context;
        this.listHoaDon = listHoaDon;
    }

    public void setListHoaDon(List<HoaDon> newList) {
        this.listHoaDon = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public HoaDonViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_hoa_don, parent, false);
        return new HoaDonViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HoaDonViewHolder holder, int position) {
        HoaDon hd = listHoaDon.get(position);
        if (hd == null) return;

        holder.tvMaHD.setText("#" + (hd.getMaHoaDon() != null ? hd.getMaHoaDon() : "N/A"));

        String khStr = hd.getTenKhachHang() != null ? hd.getTenKhachHang() : "Khách lẻ";
        if (hd.getSdtKhachHang() != null && !hd.getSdtKhachHang().trim().isEmpty()) {
            khStr += " (" + hd.getSdtKhachHang() + ")";
        }
        holder.tvKhachHangHD.setText("Khách hàng: " + khStr);

        String ngayGio = (hd.getNgayLap() != null ? hd.getNgayLap() : "") +
                (hd.getGioLap() != null && !hd.getGioLap().isEmpty() ? " lúc " + hd.getGioLap() : "");
        holder.tvNgayLapHD.setText("Thời gian: " + ngayGio);

        holder.tvTongTienHD.setText(formatter.format(hd.getTongTien()));

        String trangThai = hd.getTrangThai() != null ? hd.getTrangThai() : "Đã thanh toán";
        holder.tvTrangThaiHD.setText(trangThai);

        if ("Đã thanh toán".equalsIgnoreCase(trangThai)) {
            holder.tvTrangThaiHD.setTextColor(Color.parseColor("#2E7D32")); // Green
        } else if ("Đã hủy".equalsIgnoreCase(trangThai)) {
            holder.tvTrangThaiHD.setTextColor(Color.parseColor("#D32F2F")); // Red
        } else {
            holder.tvTrangThaiHD.setTextColor(Color.parseColor("#D97706")); // Orange
        }
    }

    @Override
    public int getItemCount() {
        return listHoaDon != null ? listHoaDon.size() : 0;
    }

    public static class HoaDonViewHolder extends RecyclerView.ViewHolder {
        TextView tvMaHD, tvTrangThaiHD, tvKhachHangHD, tvNgayLapHD, tvTongTienHD;

        public HoaDonViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMaHD = itemView.findViewById(R.id.tv_ma_hd);
            tvTrangThaiHD = itemView.findViewById(R.id.tv_trang_thai_hd);
            tvKhachHangHD = itemView.findViewById(R.id.tv_khach_hang_hd);
            tvNgayLapHD = itemView.findViewById(R.id.tv_ngay_lap_hd);
            tvTongTienHD = itemView.findViewById(R.id.tv_tong_tien_hd);
        }
    }
}