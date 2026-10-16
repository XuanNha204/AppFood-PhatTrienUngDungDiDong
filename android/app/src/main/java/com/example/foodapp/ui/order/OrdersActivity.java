package com.example.foodapp.ui.order;

import android.content.Intent;
import android.os.Bundle;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.foodapp.R;
import com.example.foodapp.api.ApiCallback;
import com.example.foodapp.api.ApiClient;
import com.example.foodapp.databinding.ActivityOrdersBinding;
import com.example.foodapp.model.Order;
import com.example.foodapp.ui.common.BaseActivity;
import com.example.foodapp.ui.common.EmptyState;
import com.example.foodapp.ui.main.MainActivity;

import java.util.List;

public class OrdersActivity extends BaseActivity {
    private ActivityOrdersBinding binding;
    private OrderAdapter adapter;
    private boolean firstLoad = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOrdersBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setupToolbar(binding.toolbar, getString(R.string.orders_title));

        adapter = new OrderAdapter(order -> startActivity(new Intent(this, OrderDetailActivity.class)
                .putExtra(OrderDetailActivity.EXTRA_ORDER_ID, order.getId())));
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerView.setAdapter(adapter);

        binding.swipeRefresh.setColorSchemeResources(R.color.primary);
        binding.swipeRefresh.setOnRefreshListener(this::load);
        binding.swipeRefresh.setRefreshing(true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        load(); // tải lại khi quay về từ chi tiết (có thể đã huỷ đơn)
    }

    private void load() {
        ApiClient.get().getOrders().enqueue(new ApiCallback<List<Order>>(this) {
            @Override
            public void onSuccess(List<Order> data) {
                adapter.setItems(data);
                if (firstLoad) binding.recyclerView.scheduleLayoutAnimation();
                firstLoad = false;
                if (data.isEmpty()) {
                    EmptyState.show(binding.empty, R.drawable.ic_receipt, getString(R.string.empty_orders_title),
                            getString(R.string.empty_orders_message), getString(R.string.explore_foods),
                            v -> MainActivity.openTab(OrdersActivity.this, R.id.nav_home));
                } else {
                    EmptyState.hide(binding.empty);
                }
            }

            @Override
            public void onError(String message) {
                EmptyState.show(binding.empty, R.drawable.ic_receipt, "Không tải được đơn hàng", message,
                        getString(R.string.retry), v -> load());
            }

            @Override
            public void onComplete() {
                binding.swipeRefresh.setRefreshing(false);
            }
        });
    }
}
