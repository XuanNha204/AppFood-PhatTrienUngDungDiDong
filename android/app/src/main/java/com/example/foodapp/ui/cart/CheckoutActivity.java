package com.example.foodapp.ui.cart;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.animation.OvershootInterpolator;

import androidx.appcompat.app.AlertDialog;

import com.example.foodapp.R;
import com.example.foodapp.api.ApiCallback;
import com.example.foodapp.api.ApiClient;
import com.example.foodapp.data.AppState;
import com.example.foodapp.data.SessionManager;
import com.example.foodapp.databinding.ActivityCheckoutBinding;
import com.example.foodapp.databinding.DialogOrderSuccessBinding;
import com.example.foodapp.model.Cart;
import com.example.foodapp.model.CartItem;
import com.example.foodapp.model.Order;
import com.example.foodapp.model.User;
import com.example.foodapp.ui.common.BaseActivity;
import com.example.foodapp.ui.main.MainActivity;
import com.example.foodapp.ui.order.OrderDetailActivity;
import com.example.foodapp.ui.order.OrderViews;
import com.example.foodapp.util.Format;
import com.example.foodapp.util.UiUtils;
import com.example.foodapp.util.Validator;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CheckoutActivity extends BaseActivity {
    private ActivityCheckoutBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCheckoutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setupToolbar(binding.toolbar, getString(R.string.checkout_title));

        User user = SessionManager.get().getUser();
        if (user != null && savedInstanceState == null) {
            setText(binding.tilPhone, user.getPhone());
            setText(binding.tilAddress, user.getAddress());
        }

        binding.btnPlaceOrder.setOnClickListener(v -> placeOrder());
        loadCart();
    }

    private void setText(com.google.android.material.textfield.TextInputLayout til, String value) {
        if (til.getEditText() != null) til.getEditText().setText(value);
    }

    private void loadCart() {
        ApiClient.get().getCart().enqueue(new ApiCallback<Cart>(this) {
            @Override
            public void onSuccess(Cart cart) {
                if (cart.isEmpty()) {
                    UiUtils.toast(CheckoutActivity.this, "Giỏ hàng đang trống");
                    finish();
                    return;
                }
                List<OrderViews.Line> lines = new ArrayList<>();
                for (CartItem item : cart.getItems()) {
                    lines.add(new OrderViews.Line(item.getFood().getName(), item.getFood().getImageUrl(),
                            item.getFood().getPrice(), item.getQuantity()));
                }
                OrderViews.bindItems(binding.itemsContainer, lines);
                binding.tvSubtotal.setText(Format.money(cart.getSubtotal()));
                binding.tvDeliveryFee.setText(cart.getDeliveryFee() == 0
                        ? getString(R.string.free) : Format.money(cart.getDeliveryFee()));
                binding.tvTotal.setText(Format.money(cart.getTotal()));
                binding.btnPlaceOrder.setEnabled(true);
            }

            @Override
            public void onError(String message) {
                UiUtils.toast(CheckoutActivity.this, message);
            }
        });
    }

    private void placeOrder() {
        boolean valid = Validator.phone(binding.tilPhone, false)
                && Validator.required(binding.tilAddress, "Vui lòng nhập địa chỉ giao hàng");
        if (!valid) return;

        UiUtils.hideKeyboard(this);
        UiUtils.setLoading(binding.btnPlaceOrder, true, getString(R.string.place_order));

        Map<String, String> body = new HashMap<>();
        body.put("phone", Validator.text(binding.tilPhone));
        body.put("address", Validator.text(binding.tilAddress));
        body.put("note", Validator.text(binding.tilNote));
        body.put("paymentMethod", binding.rbBanking.isChecked() ? "BANKING" : "COD");

        ApiClient.get().createOrder(body).enqueue(new ApiCallback<Order>(this) {
            @Override
            public void onSuccess(Order order) {
                AppState.resetCart();
                showSuccess(order);
            }

            @Override
            public void onError(String message) {
                UiUtils.setLoading(binding.btnPlaceOrder, false, getString(R.string.place_order));
                UiUtils.toast(CheckoutActivity.this, message);
            }
        });
    }

    private void showSuccess(Order order) {
        DialogOrderSuccessBinding dialogBinding = DialogOrderSuccessBinding.inflate(LayoutInflater.from(this));
        dialogBinding.tvMessage.setText(getString(R.string.order_success_message, order.getCode()));
        AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setView(dialogBinding.getRoot())
                .setCancelable(false)
                .create();

        dialogBinding.btnViewOrders.setOnClickListener(v -> {
            dialog.dismiss();
            MainActivity.openTab(this, R.id.nav_home);
            startActivity(new Intent(this, OrderDetailActivity.class)
                    .putExtra(OrderDetailActivity.EXTRA_ORDER_ID, order.getId()));
            finish();
        });
        dialogBinding.btnContinue.setOnClickListener(v -> {
            dialog.dismiss();
            MainActivity.openTab(this, R.id.nav_home);
            finish();
        });
        dialog.show();

        // Hiệu ứng dấu tích "nảy" lên
        dialogBinding.iconContainer.setScaleX(0f);
        dialogBinding.iconContainer.setScaleY(0f);
        dialogBinding.iconContainer.animate().scaleX(1f).scaleY(1f).setDuration(500)
                .setStartDelay(150).setInterpolator(new OvershootInterpolator(2.5f)).start();
        dialogBinding.imgCheck.setRotation(-120f);
        dialogBinding.imgCheck.animate().rotation(0f).setDuration(600).setStartDelay(250).start();
    }
}
