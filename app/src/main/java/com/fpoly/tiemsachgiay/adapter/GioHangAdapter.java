package com.fpoly.tiemsachgiay.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
 * Adapter hiển thị danh sách items trong Giỏ Hàng.
 * Hỗ trợ tăng/giảm số lượng và xóa item.
 */
public class GioHangAdapter extends RecyclerView.Adapter<GioHangAdapter.GioHangViewHolder> {

    private final Context context;
    private final List<GioHang> gioHangList;
    private final OnCartChangedListener listener;
    private final DecimalFormat formatter = new DecimalFormat("#,### ₫");

    public interface OnCartChangedListener {
        void onCartChanged();
    }

    public GioHangAdapter(Context context, List<GioHang> gioHangList, OnCartChangedListener listener) {
        this.context = context;
        this.gioHangList = gioHangList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public GioHangViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_gio_hang, parent, false);
        return new GioHangViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GioHangViewHolder holder, int position) {
        GioHang item = gioHangList.get(position);
        if (item == null || item.getSach() == null) return;

        Sach sach = item.getSach();

        holder.tvTenSach.setText(sach.getTenSach());
        holder.tvTacGia.setText(sach.getTacGia() != null ? sach.getTacGia() : "");

        // Giá đơn vị
        double donGia = sach.getGiaGiam() > 0 && sach.getGiaGiam() < sach.getGiaGoc()
                ? sach.getGiaGiam() : sach.getGiaGoc();
        holder.tvGia.setText(formatter.format(donGia));

        // Số lượng
        holder.tvSoLuong.setText(String.valueOf(item.getSoLuong()));

        // Thành tiền
        holder.tvThanhTien.setText(formatter.format(item.getThanhTien()));

        // Ảnh bìa
        loadImage(holder.imgSach, sach.getHinhAnh());

        // Tăng số lượng
        holder.btnTangSl.setOnClickListener(v -> {
            int newQty = item.getSoLuong() + 1;
            if (newQty > sach.getSoLuong()) {
                Toast.makeText(context, "Không đủ hàng trong kho!", Toast.LENGTH_SHORT).show();
                return;
            }
            item.setSoLuong(newQty);
            holder.tvSoLuong.setText(String.valueOf(newQty));
            holder.tvThanhTien.setText(formatter.format(item.getThanhTien()));
            if (listener != null) listener.onCartChanged();
        });

        // Giảm số lượng
        holder.btnGiamSl.setOnClickListener(v -> {
            int newQty = item.getSoLuong() - 1;
            if (newQty <= 0) {
                // Xóa item nếu về 0
                gioHangList.remove(position);
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, gioHangList.size());
                GioHang.removeFromCart(sach);
            } else {
                item.setSoLuong(newQty);
                holder.tvSoLuong.setText(String.valueOf(newQty));
                holder.tvThanhTien.setText(formatter.format(item.getThanhTien()));
            }
            if (listener != null) listener.onCartChanged();
        });

        // Xóa khỏi giỏ
        holder.btnXoa.setOnClickListener(v -> {
            gioHangList.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, gioHangList.size());
            GioHang.removeFromCart(sach);
            if (listener != null) listener.onCartChanged();
        });
    }

    private void loadImage(ImageView imageView, String url) {
        if (url != null && !url.trim().isEmpty()) {
            try {
                GlideUrl glideUrl = new GlideUrl(url.trim(), new LazyHeaders.Builder()
                        .addHeader("User-Agent", "Mozilla/5.0")
                        .build());
                Glide.with(context)
                        .load(glideUrl)
                        .override(200, 280)
                        .centerCrop()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .placeholder(R.drawable.ic_book_placeholder)
                        .error(R.drawable.ic_book_placeholder)
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
        return gioHangList != null ? gioHangList.size() : 0;
    }

    public static class GioHangViewHolder extends RecyclerView.ViewHolder {
        ImageView imgSach, btnTangSl, btnGiamSl, btnXoa;
        TextView tvTenSach, tvTacGia, tvGia, tvSoLuong, tvThanhTien;

        public GioHangViewHolder(@NonNull View itemView) {
            super(itemView);
            imgSach = itemView.findViewById(R.id.img_gio_hang_sach);
            tvTenSach = itemView.findViewById(R.id.tv_gio_hang_ten_sach);
            tvTacGia = itemView.findViewById(R.id.tv_gio_hang_tac_gia);
            tvGia = itemView.findViewById(R.id.tv_gio_hang_gia);
            tvSoLuong = itemView.findViewById(R.id.tv_so_luong_gio_hang);
            tvThanhTien = itemView.findViewById(R.id.tv_gio_hang_thanh_tien);
            btnTangSl = itemView.findViewById(R.id.btn_tang_sl);
            btnGiamSl = itemView.findViewById(R.id.btn_giam_sl);
            btnXoa = itemView.findViewById(R.id.btn_xoa_khoi_gio);
        }
    }
}
