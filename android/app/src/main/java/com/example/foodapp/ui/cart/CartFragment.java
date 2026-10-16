package com.example.foodapp.ui.cart;

import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodapp.R;
import com.example.foodapp.api.ApiCallback;
import com.example.foodapp.api.ApiClient;
import com.example.foodapp.data.AppState;
import com.example.foodapp.data.FoodActions;
import com.example.foodapp.databinding.FragmentCartBinding;
import com.example.foodapp.model.Cart;
import com.example.foodapp.model.CartItem;
import com.example.foodapp.ui.common.EmptyState;
import com.example.foodapp.ui.food.FoodDetailActivity;
import com.example.foodapp.ui.main.MainActivity;
import com.example.foodapp.ui.main.TabFragment;
import com.example.foodapp.util.Format;
import com.example.foodapp.util.UiUtils;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;

public class CartFragment extends Fragment implements TabFragment, CartAdapter.Listener {
    private FragmentCartBinding binding;
    private CartAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentCartBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        adapter = new CartAdapter(this);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerView.setAdapter(adapter);
        attachSwipeToDelete();

        binding.swipeRefresh.setColorSchemeResources(R.color.primary);
        binding.swipeRefresh.setOnRefreshListener(this::load);
        binding.tvClear.setOnClickListener(v -> confirmClear());
        binding.btnCheckout.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), CheckoutActivity.class)));

        // Dữ liệu được tải trong onResume (cả lần đầu và khi quay lại từ màn đặt hàng)
        binding.swipeRefresh.setRefreshing(true);
    }

    private void load() {
        request(ApiClient.get().getCart(), true);
    }

    /** Gửi request thao tác giỏ hàng, kết quả luôn là giỏ hàng mới nhất từ server. */
    private void request(Call<Cart> call, boolean animate) {
        call.enqueue(new ApiCallback<Cart>(this) {
            @Override
            public void onSuccess(Cart cart) {
                render(cart, animate);
            }

            @Override
            public void onError(String message) {
                UiUtils.toast(requireContext(), message);
                if (adapter.getItemCount() == 0) {
                    EmptyState.show(binding.empty, R.drawable.ic_cart, "Không tải được giỏ hàng", message,
                            getString(R.string.retry), v -> load());
                }
            }

            @Override
            public void onComplete() {
                binding.swipeRefresh.setRefreshing(false);
            }
        });
    }

    private void render(Cart cart, boolean animate) {
        AppState.updateCart(cart);
        adapter.setItems(cart.getItems());
        if (animate) binding.recyclerView.scheduleLayoutAnimation();

        boolean empty = cart.isEmpty();
        binding.tvClear.setVisibility(empty ? View.GONE : View.VISIBLE);
        binding.summary.setVisibility(empty ? View.GONE : View.VISIBLE);
        if (empty) {
            EmptyState.show(binding.empty, R.drawable.ic_cart, getString(R.string.empty_cart_title),
                    getString(R.string.empty_cart_message), getString(R.string.explore_foods),
                    v -> ((MainActivity) requireActivity()).selectTab(R.id.nav_home));
            return;
        }
        EmptyState.hide(binding.empty);

        binding.tvSubtotal.setText(Format.money(cart.getSubtotal()));
        binding.tvDeliveryFee.setText(cart.getDeliveryFee() == 0
                ? getString(R.string.free) : Format.money(cart.getDeliveryFee()));
        binding.tvTotal.setText(Format.money(cart.getTotal()));

        // Thanh tiến trình miễn phí giao hàng
        long threshold = cart.getFreeShipThreshold();
        if (threshold > 0) {
            int progress = (int) Math.min(100, cart.getSubtotal() * 100 / threshold);
            binding.progressFreeShip.setProgressCompat(progress, true);
            binding.tvFreeShip.setText(cart.getSubtotal() >= threshold
                    ? getString(R.string.free_ship_reached)
                    : getString(R.string.free_ship_remaining, Format.money(threshold - cart.getSubtotal())));
        }
    }

    @Override
    public void onQuantityChanged(CartItem item, int newQuantity) {
        String foodId = item.getFood().getId();
        if (newQuantity <= 0) {
            int index = adapter.getItemCount();
            for (int i = 0; i < adapter.getItemCount(); i++) {
                if (adapter.getItem(i) == item) index = i;
            }
            removeItem(index);
            return;
        }
        Map<String, Object> body = new HashMap<>();
        body.put("quantity", newQuantity);
        request(ApiClient.get().updateCartItem(foodId, body), false);
    }

    @Override
    public void onItemClick(CartItem item) {
        FoodDetailActivity.start(requireActivity(), item.getFood(), null);
    }

    private void removeItem(int position) {
        if (position < 0 || position >= adapter.getItemCount()) return;
        CartItem removed = adapter.removeAt(position);
        request(ApiClient.get().removeCartItem(removed.getFood().getId()), false);

        Snackbar.make(binding.getRoot(), getString(R.string.removed_from_cart, removed.getFood().getName()),
                        Snackbar.LENGTH_LONG)
                .setAnchorView(requireActivity().findViewById(R.id.bottomNav))
                .setAction(R.string.undo, v -> FoodActions.addToCart(requireContext(), this,
                        removed.getFood(), removed.getQuantity(), cart -> {
                            if (cart != null && binding != null) render(cart, false);
                        }))
                .show();
    }

    /** Vuốt sang trái để xoá món khỏi giỏ, hiển thị nền đỏ + icon thùng rác. */
    private void attachSwipeToDelete() {
        Drawable icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_delete);
        if (icon != null) icon.setTint(ContextCompat.getColor(requireContext(), R.color.white));
        ColorDrawable background = new ColorDrawable(ContextCompat.getColor(requireContext(), R.color.danger));

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView rv, @NonNull RecyclerView.ViewHolder vh,
                                  @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder vh, int direction) {
                removeItem(vh.getBindingAdapterPosition());
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView rv, @NonNull RecyclerView.ViewHolder vh,
                                    float dX, float dY, int actionState, boolean isCurrentlyActive) {
                View item = vh.itemView;
                int margin = UiUtils.dp(requireContext(), 16);
                if (dX < 0) {
                    background.setBounds(item.getRight() + (int) dX, item.getTop() + 12,
                            item.getRight() - margin, item.getBottom() - 12);
                    background.draw(c);
                    if (icon != null) {
                        int size = UiUtils.dp(requireContext(), 26);
                        int top = item.getTop() + (item.getHeight() - size) / 2;
                        int right = item.getRight() - margin - UiUtils.dp(requireContext(), 20);
                        icon.setBounds(right - size, top, right, top + size);
                        icon.draw(c);
                    }
                }
                super.onChildDraw(c, rv, vh, dX, dY, actionState, isCurrentlyActive);
            }
        }).attachToRecyclerView(binding.recyclerView);
    }

    private void confirmClear() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.clear_cart)
                .setMessage(R.string.confirm_clear_cart)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.ok, (d, w) -> request(ApiClient.get().clearCart(), false))
                .show();
    }

    @Override
    public void onTabSelected() {
        load();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!isHidden()) load();
    }

    @Override
    public void onDestroyView() {
        binding = null;
        super.onDestroyView();
    }
}
