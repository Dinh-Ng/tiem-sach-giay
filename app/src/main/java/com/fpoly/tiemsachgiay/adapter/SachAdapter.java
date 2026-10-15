package com.fpoly.tiemsachgiay.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
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

import java.text.DecimalFormat;
import java.util.List;

public class SachAdapter extends RecyclerView.Adapter<SachAdapter.SachViewHolder> {

    private final Context context;
    private List<Sach> listSach;
    private final OnBookClickListener listener;
    private final DecimalFormat formatter = new DecimalFormat("#,### ₫");

    public interface OnBookClickListener {
        void onEditClick(Sach sach);
        void onDeleteClick(Sach sach);
    }

    public SachAdapter(Context context, List<Sach> listSach, OnBookClickListener listener) {
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
    public SachViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_sach, parent, false);
        return new SachViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SachViewHolder holder, int position) {
        Sach sach = listSach.get(position);
        if (sach == null) return;

        holder.tvTenSach.setText(sach.getTenSach());
        holder.tvTacGia.setText("Tác giả: " + (sach.getTacGia() != null ? sach.getTacGia() : "Chưa cập nhật"));
        holder.tvTheLoai.setText(sach.getTheLoai() != null ? sach.getTheLoai() : "Khác");

        // Hiển thị Số lượng còn lại
        int soLuong = sach.getSoLuong();
        if (soLuong <= 0) {
            holder.tvSoLuong.setText("Hết hàng");
            holder.tvSoLuong.setTextColor(Color.parseColor("#D32F2F"));
        } else {
            holder.tvSoLuong.setText("Còn lại: " + soLuong);
            holder.tvSoLuong.setTextColor(Color.parseColor("#6A381F"));
        }

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

        // Tải ảnh bìa sách bằng Glide với cơ chế fallback
        loadImage(holder.imgSach, sach.getHinhAnh(), sach.getMaSach());

        holder.btnEdit.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEditClick(sach);
            }
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteClick(sach);
            }
        });
    }

    private void loadImage(ImageView imageView, String url, String bookId) {
        String mainUrl = (url != null && !url.trim().isEmpty()) ? url.trim() : getFallbackUrl(bookId);
        String fallbackUrl = getFallbackUrl(bookId);

        try {
            GlideUrl primaryGlideUrl = new GlideUrl(mainUrl, new LazyHeaders.Builder()
                    .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .build());

            GlideUrl fallbackGlideUrl = new GlideUrl(fallbackUrl, new LazyHeaders.Builder()
                    .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .build());

            Glide.with(context)
                    .load(primaryGlideUrl)
                    .override(300, 400)
                    .centerCrop()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(R.drawable.ic_book_placeholder)
                    .error(
                            Glide.with(context)
                                    .load(fallbackGlideUrl)
                                    .override(300, 400)
                                    .centerCrop()
                                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                                    .placeholder(R.drawable.ic_book_placeholder)
                                    .error(R.drawable.ic_book_placeholder)
                    )
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
        if ("s06".equalsIgnoreCase(bookId)) return "https://images.unsplash.com/photo-1589829085413-56de8ae18c73?w=600&q=80";
        return "https://picsum.photos/id/24/300/400";
    }

    @Override
    public int getItemCount() {
        return listSach != null ? listSach.size() : 0;
    }

    public static class SachViewHolder extends RecyclerView.ViewHolder {
        ImageView imgSach;
        TextView tvTenSach, tvTacGia, tvTheLoai, tvSoLuong, tvGiaGoc, tvGiaGiam;
        ImageButton btnEdit, btnDelete;

        public SachViewHolder(@NonNull View itemView) {
            super(itemView);
            imgSach = itemView.findViewById(R.id.img_sach);
            tvTenSach = itemView.findViewById(R.id.tv_ten_sach);
            tvTacGia = itemView.findViewById(R.id.tv_tac_gia);
            tvTheLoai = itemView.findViewById(R.id.tv_the_loai);
            tvSoLuong = itemView.findViewById(R.id.tv_so_luong);
            tvGiaGoc = itemView.findViewById(R.id.tv_gia_goc);
            tvGiaGiam = itemView.findViewById(R.id.tv_gia_giam);
            btnEdit = itemView.findViewById(R.id.btn_edit_sach);
            btnDelete = itemView.findViewById(R.id.btn_delete_sach);
        }
    }
}