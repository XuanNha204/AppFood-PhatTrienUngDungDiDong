package com.example.foodapp.ui.common;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodapp.R;
import com.example.foodapp.model.Category;
import com.example.foodapp.util.ImageUtils;

import java.util.ArrayList;
import java.util.List;

/** Adapter danh mục: dạng tròn (trang chủ) hoặc dạng thẻ lưới (màn Tất cả danh mục). */
public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.VH> {

    public interface Listener {
        void onCategoryClick(Category category);
    }

    private final List<Category> items = new ArrayList<>();
    private final Listener listener;
    private final boolean gridStyle;

    public CategoryAdapter(boolean gridStyle, Listener listener) {
        this.gridStyle = gridStyle;
        this.listener = listener;
    }

    public void setItems(List<Category> categories) {
        items.clear();
        if (categories != null) items.addAll(categories);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layout = gridStyle ? R.layout.item_category_grid : R.layout.item_category;
        return new VH(LayoutInflater.from(parent.getContext()).inflate(layout, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Category category = items.get(position);
        holder.name.setText(category.getName());
        ImageUtils.load(holder.image, category.getImageUrl(), R.drawable.ic_restaurant);
        if (holder.description != null) holder.description.setText(category.getDescription());
        if (holder.count != null) {
            holder.count.setText(holder.itemView.getContext()
                    .getString(R.string.food_count, category.getFoodCount()));
        }
        holder.itemView.setOnClickListener(v -> listener.onCategoryClick(category));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        final ImageView image;
        final TextView name;
        final TextView description;
        final TextView count;

        VH(View view) {
            super(view);
            image = view.findViewById(R.id.imgCategory);
            name = view.findViewById(R.id.tvName);
            description = view.findViewById(R.id.tvDescription);
            count = view.findViewById(R.id.tvCount);
        }
    }
}
