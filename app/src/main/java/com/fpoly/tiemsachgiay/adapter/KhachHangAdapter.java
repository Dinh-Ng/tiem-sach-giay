package com.fpoly.tiemsachgiay.adapter;

import android.content.Context;
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
import com.fpoly.tiemsachgiay.model.KhachHang;

import java.util.List;

public class KhachHangAdapter extends RecyclerView.Adapter<KhachHangAdapter.KhachHangViewHolder> {

    private final Context context;
    private List<KhachHang> listKhachHang;
    private final OnCustomerClickListener listener;

    public interface OnCustomerClickListener {
        void onDeleteClick(KhachHang khachHang);
    }

    public KhachHangAdapter(Context context, List<KhachHang> listKhachHang, OnCustomerClickListener listener) {
        this.context = context;
        this.listKhachHang = listKhachHang;
        this.listener = listener;
    }

    public void setListKhachHang(List<KhachHang> newList) {
        this.listKhachHang = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public KhachHangViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_khach_hang_manage, parent, false);
        return new KhachHangViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull KhachHangViewHolder holder, int position) {
        KhachHang kh = listKhachHang.get(position);
        if (kh == null) return;

        String name = kh.getTenKH() != null && !kh.getTenKH().isEmpty() ? kh.getTenKH() : "Khách hàng";
        holder.tvTenKH.setText(name);

        String sdtStr = kh.getSdt() != null && !kh.getSdt().isEmpty() ? kh.getSdt() : kh.getTaiKhoan();
        holder.tvSdtKH.setText("SĐT / Tài khoản: " + (sdtStr != null ? sdtStr : "N/A"));

        String diaChiStr = kh.getDiaChi() != null && !kh.getDiaChi().trim().isEmpty() ? kh.getDiaChi() : "Chưa cập nhật địa chỉ";
        holder.tvDiaChiKH.setText("Địa chỉ: " + diaChiStr);

        // Load fake avatar photo
        loadAvatar(holder.imgAvatarKH, name, sdtStr);

        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteClick(kh);
            }
        });
    }

    private void loadAvatar(ImageView imageView, String name, String sdt) {
        int hash = Math.abs((name != null ? name.hashCode() : 0) + (sdt != null ? sdt.hashCode() : 0));
        String[] sampleAvatars = {
                "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=400&auto=format&fit=crop&q=80",
                "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=400&auto=format&fit=crop&q=80",
                "https://images.unsplash.com/photo-1570295999919-56ceb5ecca61?w=400&auto=format&fit=crop&q=80",
                "https://images.unsplash.com/photo-1438761681033-6461ffad8d80?w=400&auto=format&fit=crop&q=80",
                "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=400&auto=format&fit=crop&q=80",
                "https://images.unsplash.com/photo-1544005313-94ddf0286df2?w=400&auto=format&fit=crop&q=80"
        };
        String avatarUrl = sampleAvatars[hash % sampleAvatars.length];

        try {
            GlideUrl glideUrl = new GlideUrl(avatarUrl, new LazyHeaders.Builder()
                    .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .build());

            Glide.with(context)
                    .load(glideUrl)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(R.drawable.ic_person)
                    .error(R.drawable.ic_person)
                    .into(imageView);
        } catch (Exception e) {
            imageView.setImageResource(R.drawable.ic_person);
        }
    }

    @Override
    public int getItemCount() {
        return listKhachHang != null ? listKhachHang.size() : 0;
    }

    public static class KhachHangViewHolder extends RecyclerView.ViewHolder {
        ImageView imgAvatarKH;
        TextView tvTenKH, tvSdtKH, tvDiaChiKH;
        ImageButton btnDelete;

        public KhachHangViewHolder(@NonNull View itemView) {
            super(itemView);
            imgAvatarKH = itemView.findViewById(R.id.img_avatar_kh);
            tvTenKH = itemView.findViewById(R.id.tv_ten_kh);
            tvSdtKH = itemView.findViewById(R.id.tv_sdt_kh);
            tvDiaChiKH = itemView.findViewById(R.id.tv_dia_chi_kh);
            btnDelete = itemView.findViewById(R.id.btn_delete_kh);
        }
    }
}