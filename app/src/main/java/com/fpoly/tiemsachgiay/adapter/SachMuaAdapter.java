package com.fpoly.tiemsachgiay.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
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
import com.fpoly.tiemsachgiay.model.Sach;
import com.google.android.material.button.MaterialButton;

import java.text.DecimalFormat;
import java.util.List;

/**
 * Adapter hiển thị danh sách sách cho Khách hàng, với nút "Thêm vào giỏ" và "Mua ngay".
 */
public class SachMuaAdapter extends RecyclerView.Adapter<SachMuaAdapter.SachMuaViewHolder> {

    private final Context context;
    private List<Sach> listSach;
    private final OnSachMuaClickListener listener;
    private final DecimalFormat formatter = new DecimalFormat("#,### ₫");

    public interface OnSachMuaClickListener {
        void onAddToCart(Sach sach);
        void onBuyNow(Sach sach);
    }

    public SachMuaAdapter(Context context, List<Sach> listSach, OnSachMuaClickListener listener) {
        this.context = context;
        this.listSach = listSach;
        this.listener = listener;
    }

    public void setListSach(List<Sach> newList) {
        this.listSach = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SachMuaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_sach_mua, parent, false);
        return new SachMuaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SachMuaViewHolder holder, int position) {
        Sach sach = listSach.get(position);
        if (sach == null) return;

        holder.tvTenSach.setText(sach.getTenSach());
        holder.tvTacGia.setText("Tác giả: " + (sach.getTacGia() != null ? sach.getTacGia() : "Chưa cập nhật"));
        holder.tvTheLoai.setText(sach.getTheLoai() != null ? sach.getTheLoai() : "Khác");

        // Số lượng tồn kho
        int soLuong = sach.getSoLuong();
        boolean hetHang = soLuong <= 0;
        if (hetHang) {
            holder.tvSoLuong.setText("Hết hàng");
            holder.tvSoLuong.setTextColor(Color.parseColor("#D32F2F"));
            holder.btnAddToCart.setEnabled(false);
            holder.btnAddToCart.setAlpha(0.4f);
            holder.btnBuyNow.setEnabled(false);
            holder.btnBuyNow.setAlpha(0.4f);
        } else {
            holder.tvSoLuong.setText("Còn: " + soLuong);
            holder.tvSoLuong.setTextColor(Color.parseColor("#2E7D32"));
            holder.btnAddToCart.setEnabled(true);
            holder.btnAddToCart.setAlpha(1f);
            holder.btnBuyNow.setEnabled(true);
            holder.btnBuyNow.setAlpha(1f);
        }

        // Giá
        double giaGoc = sach.getGiaGoc();
        double giaGiam = sach.getGiaGiam();
        if (giaGiam > 0 && giaGiam < giaGoc) {
            holder.tvGiaGiam.setText(formatter.format(giaGiam));
            holder.tvGiaGoc.setText(formatter.format(giaGoc));
            holder.tvGiaGoc.setPaintFlags(holder.tvGiaGoc.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            holder.tvGiaGoc.setVisibility(View.VISIBLE);
        } else {
            holder.tvGiaGiam.setText(formatter.format(giaGoc));
            holder.tvGiaGoc.setVisibility(View.GONE);
        }

        // Ảnh bìa
        loadImage(holder.imgSach, sach.getHinhAnh(), sach.getMaSach());

        // Sự kiện nút
        holder.btnAddToCart.setOnClickListener(v -> {
            if (listener != null) listener.onAddToCart(sach);
        });
        holder.btnBuyNow.setOnClickListener(v -> {
            if (listener != null) listener.onBuyNow(sach);
        });
    }

    private void loadImage(ImageView imageView, String url, String bookId) {
        String mainUrl = (url != null && !url.trim().isEmpty()) ? url.trim() : getFallbackUrl(bookId);
        try {
            GlideUrl glideUrl = new GlideUrl(mainUrl, new LazyHeaders.Builder()
                    .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .build());
            Glide.with(context)
                    .load(glideUrl)
                    .override(300, 400)
                    .centerCrop()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(R.drawable.ic_book_placeholder)
                    .error(R.drawable.ic_book_placeholder)
                    .into(imageView);
        } catch (Exception e) {
            imageView.setImageResource(R.drawable.ic_book_placeholder);
        }
    }

    private String getFallbackUrl(String bookId) {
        if ("s01".equalsIgnoreCase(bookId)) return "https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?w=600&q=80";
        if ("s02".equalsIgnoreCase(bookId)) return "https://images.unsplash.com/photo-1512820790803-83ca734da794?w=600&q=80";
        if ("s03".equalsIgnoreCase(bookId)) return "https://images.unsplash.com/photo-1532012197267-da84d127e765?w=600&q=80";
        if ("s04".equalsIgnoreCase(bookId)) return "https://images.unsplash.com/photo-1543002588-bfa74002ed7e?w=600&q=80";
        if ("s05".equalsIgnoreCase(bookId)) return "https://images.unsplash.com/photo-1497633762265-9d179a990aa6?w=600&q=80";
        return "https://picsum.photos/id/24/300/400";
    }

    @Override
    public int getItemCount() {
        return listSach != null ? listSach.size() : 0;
    }

    public static class SachMuaViewHolder extends RecyclerView.ViewHolder {
        ImageView imgSach;
        TextView tvTenSach, tvTacGia, tvTheLoai, tvSoLuong, tvGiaGoc, tvGiaGiam;
        MaterialButton btnAddToCart, btnBuyNow;

        public SachMuaViewHolder(@NonNull View itemView) {
            super(itemView);
            imgSach = itemView.findViewById(R.id.img_sach_mua);
            tvTenSach = itemView.findViewById(R.id.tv_ten_sach_mua);
            tvTacGia = itemView.findViewById(R.id.tv_tac_gia_mua);
            tvTheLoai = itemView.findViewById(R.id.tv_the_loai_mua);
            tvSoLuong = itemView.findViewById(R.id.tv_so_luong_mua);
            tvGiaGoc = itemView.findViewById(R.id.tv_gia_goc_mua);
            tvGiaGiam = itemView.findViewById(R.id.tv_gia_giam_mua);
            btnAddToCart = itemView.findViewById(R.id.btn_add_to_cart);
            btnBuyNow = itemView.findViewById(R.id.btn_buy_now);
        }
    }
}
