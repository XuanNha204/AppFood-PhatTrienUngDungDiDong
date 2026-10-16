package com.example.foodapp.ui.order;

import android.os.Bundle;
import android.view.View;
import android.view.animation.AnimationUtils;

import com.example.foodapp.R;
import com.example.foodapp.api.ApiCallback;
import com.example.foodapp.api.ApiClient;
import com.example.foodapp.databinding.ActivityOrderDetailBinding;
import com.example.foodapp.model.Order;
import com.example.foodapp.model.OrderItem;
import com.example.foodapp.ui.common.BaseActivity;
import com.example.foodapp.util.Format;
import com.example.foodapp.util.UiUtils;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.List;

public class OrderDetailActivity extends BaseActivity {
    public static final String EXTRA_ORDER_ID = "order_id";

    private ActivityOrderDetailBinding binding;
    private String orderId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOrderDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setupToolbar(binding.toolbar, getString(R.string.order_detail_title));

        orderId = getIntent().getStringExtra(EXTRA_ORDER_ID);
        binding.swipeRefresh.setColorSchemeResources(R.color.primary);
        binding.swipeRefresh.setOnRefreshListener(this::load);
        binding.btnCancel.setOnClickListener(v -> confirmCancel());
        binding.swipeRefresh.setRefreshing(true);
        load();
    }

    private void load() {
        ApiClient.get().getOrder(orderId).enqueue(new ApiCallback<Order>(this) {
            @Override
            public void onSuccess(Order order) {
                bind(order);
            }

            @Override
            public void onError(String message) {
                UiUtils.toast(OrderDetailActivity.this, message);
            }

            @Override
            public void onComplete() {
                binding.swipeRefresh.setRefreshing(false);
            }
        });
    }

    private void bind(Order order) {
        if (binding.content.getVisibility() != View.VISIBLE) {
            binding.content.setVisibility(View.VISIBLE);
            binding.content.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_in_up));
        }
        binding.tvCode.setText(getString(R.string.order_code, order.getCode()));
        binding.tvDate.setText(Format.dateTime(order.getCreatedAt()));
        OrderViews.bindStatus(binding.tvStatus, order.getStatus());

        binding.tvPhone.setText(order.getPhone());
        binding.tvAddress.setText(order.getAddress());
        binding.tvPayment.setText("BANKING".equals(order.getPaymentMethod())
                ? R.string.payment_banking : R.string.payment_cod);
        binding.tvNote.setVisibility(order.getNote().isEmpty() ? View.GONE : View.VISIBLE);
        binding.tvNote.setText(order.getNote());

        List<OrderViews.Line> lines = new ArrayList<>();
        for (OrderItem item : order.getItems()) {
            lines.add(new OrderViews.Line(item.getName(), item.getImageUrl(), item.getPrice(), item.getQuantity()));
        }
        OrderViews.bindItems(binding.itemsContainer, lines);
        binding.tvSubtotal.setText(Format.money(order.getSubtotal()));
        binding.tvDeliveryFee.setText(order.getDeliveryFee() == 0
                ? getString(R.string.free) : Format.money(order.getDeliveryFee()));
        binding.tvTotal.setText(Format.money(order.getTotal()));

        binding.btnCancel.setVisibility(Order.PENDING.equals(order.getStatus()) ? View.VISIBLE : View.GONE);
    }

    private void confirmCancel() {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.cancel_order)
                .setMessage(R.string.confirm_cancel_order)
                .setNegativeButton(R.string.close, null)
                .setPositiveButton(R.string.cancel_order, (d, w) -> cancelOrder())
                .show();
    }

    private void cancelOrder() {
        binding.btnCancel.setEnabled(false);
        ApiClient.get().cancelOrder(orderId).enqueue(new ApiCallback<Order>(this) {
            @Override
            public void onSuccess(Order order) {
                UiUtils.toast(OrderDetailActivity.this, "Đã huỷ đơn hàng");
                bind(order);
            }

            @Override
            public void onError(String message) {
                UiUtils.toast(OrderDetailActivity.this, message);
            }

            @Override
            public void onComplete() {
                binding.btnCancel.setEnabled(true);
            }
        });
    }
}
