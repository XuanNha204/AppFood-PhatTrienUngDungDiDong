package com.example.foodapp.ui.home;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodapp.databinding.ItemBannerBinding;
import com.example.foodapp.model.Banner;
import com.example.foodapp.util.ImageUtils;

import java.util.ArrayList;
import java.util.List;

public class BannerAdapter extends RecyclerView.Adapter<BannerAdapter.VH> {

    public interface Listener {
        void onBannerClick(Banner banner);
    }

    private final List<Banner> items = new ArrayList<>();
    private final Listener listener;

    public BannerAdapter(Listener listener) {
        this.listener = listener;
    }

    public void setItems(List<Banner> banners) {
        items.clear();
        if (banners != null) items.addAll(banners);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VH(ItemBannerBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Banner banner = items.get(position);
        holder.binding.tvTitle.setText(banner.getTitle());
        holder.binding.tvSubtitle.setText(banner.getSubtitle());
        ImageUtils.loadFood(holder.binding.imgBanner, banner.getImageUrl());
        holder.binding.getRoot().setOnClickListener(v -> listener.onBannerClick(banner));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        final ItemBannerBinding binding;

        VH(ItemBannerBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
