package com.fpoly.tiemsachgiay.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.fpoly.tiemsachgiay.fragment.KhachHangFragment;
import com.fpoly.tiemsachgiay.fragment.NhanVienFragment;

public class AuthPagerAdapter extends FragmentStateAdapter {

    public AuthPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == 0) {
            return new NhanVienFragment();
        }
        return new KhachHangFragment();
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
