package com.example.foodapp.ui.common;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.GridLayoutAnimationController;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * RecyclerView hỗ trợ android:layoutAnimation dạng <gridLayoutAnimation>.
 * RecyclerView gốc không cung cấp vị trí hàng/cột cho hiệu ứng lưới nên sẽ crash
 * (ClassCastException ... GridLayoutAnimationController$AnimationParameters).
 */
public class GridRecyclerView extends RecyclerView {

    public GridRecyclerView(@NonNull Context context) {
        super(context);
    }

    public GridRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public GridRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void attachLayoutAnimationParameters(View child, ViewGroup.LayoutParams params, int index, int count) {
        if (!(getLayoutManager() instanceof GridLayoutManager) || getAdapter() == null) {
            super.attachLayoutAnimationParameters(child, params, index, count);
            return;
        }
        GridLayoutAnimationController.AnimationParameters animParams =
                (GridLayoutAnimationController.AnimationParameters) params.layoutAnimationParameters;
        if (animParams == null) {
            animParams = new GridLayoutAnimationController.AnimationParameters();
            params.layoutAnimationParameters = animParams;
        }

        int columns = ((GridLayoutManager) getLayoutManager()).getSpanCount();
        animParams.count = count;
        animParams.index = index;
        animParams.columnsCount = columns;
        animParams.rowsCount = (count + columns - 1) / columns;
        animParams.column = index % columns;
        animParams.row = index / columns;
    }
}
