package com.fpoly.tiemsachgiay.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.fpoly.tiemsachgiay.R;
import com.fpoly.tiemsachgiay.model.GioHang;
import com.fpoly.tiemsachgiay.model.Sach;

import java.text.DecimalFormat;
import java.util.List;

/**
 * Adapter tóm tắt danh sách sản phẩm trong màn hình Đặt hàng / Thanh toán.
 */
public class TomTatDonHangAdapter extends RecyclerView.Adapter<TomTatDonHangAdapter.TomTatViewHolder> {

    private final Context context;
    private final List<GioHang> itemList;
    private final DecimalFormat formatter = new DecimalFormat("#,### ₫");

    public TomTatDonHangAdapter(Context context, List<GioHang> itemList) {
        this.context = context;
        this.itemList = itemList;
    }

    @NonNull
    @Override
    public TomTatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_sach_thanh_toan, parent, false);
        return new TomTatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TomTatViewHolder holder, int position) {
        GioHang item = itemList.get(position);
        if (item == null || item.getSach() == null) return;

        Sach sach = item.getSach();
        holder.tvTenSach.setText(sach.getTenSach() != null ? sach.getTenSach() : "Sách");
        holder.tvTacGia.setText(sach.getTacGia() != null ? sach.getTacGia() : "");

        double donGia = (sach.getGiaGiam() > 0 && sach.getGiaGiam() < sach.getGiaGoc())
                ? sach.getGiaGiam()
                : sach.getGiaGoc();

        holder.tvSoLuongGia.setText(formatter.format(donGia) + " × " + item.getSoLuong());
        holder.tvThanhTien.setText(formatter.format(donGia * item.getSoLuong()));

        // Tải ảnh bìa
        loadImage(holder.imgBia, sach.getHinhAnh());
    }

    private void loadImage(ImageView imageView, String url) {
        if (url != null && !url.trim().isEmpty()) {
            try {
                GlideUrl glideUrl = new GlideUrl(url, new LazyHeaders.Builder()
                        .addHeader("User-Agent", "Mozilla/5.0")
                        .build());

                Glide.with(context)
                        .load(glideUrl)
                        .placeholder(R.drawable.ic_book_placeholder)
                        .error(R.drawable.ic_book_placeholder)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(imageView);
            } catch (Exception e) {
                imageView.setImageResource(R.drawable.ic_book_placeholder);
            }
        } else {
            imageView.setImageResource(R.drawable.ic_book_placeholder);
        }
    }

    @Override
    public int getItemCount() {
        return itemList != null ? itemList.size() : 0;
    }

    static class TomTatViewHolder extends RecyclerView.ViewHolder {
        ImageView imgBia;
        TextView tvTenSach, tvTacGia, tvSoLuongGia, tvThanhTien;

        public TomTatViewHolder(@NonNull View itemView) {
            super(itemView);
            imgBia = itemView.findViewById(R.id.img_thanh_toan_sach);
            tvTenSach = itemView.findViewById(R.id.tv_thanh_toan_ten_sach);
            tvTacGia = itemView.findViewById(R.id.tv_thanh_toan_tac_gia);
            tvSoLuongGia = itemView.findViewById(R.id.tv_thanh_toan_so_luong_gia);
            tvThanhTien = itemView.findViewById(R.id.tv_thanh_toan_thanh_tien);
        }
    }
}
