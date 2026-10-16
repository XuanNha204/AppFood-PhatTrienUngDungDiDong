package com.example.foodapp.util;

import android.widget.ImageView;

import androidx.annotation.DrawableRes;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.example.foodapp.BuildConfig;
import com.example.foodapp.R;

public final class ImageUtils {
    private ImageUtils() {}

    /** Ảnh do server lưu (trong Firestore) có dạng "/images/abc" -> ghép thêm BASE_URL. */
    public static String fullUrl(String url) {
        if (url == null || url.isEmpty()) return null;
        if (url.startsWith("http://") || url.startsWith("https://")) return url;
        String base = BuildConfig.BASE_URL.endsWith("/")
                ? BuildConfig.BASE_URL.substring(0, BuildConfig.BASE_URL.length() - 1)
                : BuildConfig.BASE_URL;
        return base + (url.startsWith("/") ? url : "/" + url);
    }

    public static void loadFood(ImageView view, String url) {
        load(view, url, R.drawable.bg_image_placeholder);
    }

    public static void loadAvatar(ImageView view, String url) {
        Glide.with(view)
                .load(fullUrl(url))
                .placeholder(R.drawable.ic_person)
                .error(R.drawable.ic_person)
                .circleCrop()
                .into(view);
    }

    public static void load(ImageView view, String url, @DrawableRes int placeholder) {
        Glide.with(view)
                .load(fullUrl(url))
                .placeholder(placeholder)
                .error(placeholder)
                .centerCrop()
                .transition(DrawableTransitionOptions.withCrossFade(250))
                .into(view);
    }
}
