package com.example.foodapp.ui.main;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.foodapp.R;
import com.example.foodapp.data.AppState;
import com.example.foodapp.data.FoodActions;
import com.example.foodapp.databinding.ActivityMainBinding;
import com.example.foodapp.ui.cart.CartFragment;
import com.example.foodapp.ui.chat.ChatActivity;
import com.example.foodapp.ui.common.BaseActivity;
import com.example.foodapp.ui.favorite.FavoritesFragment;
import com.example.foodapp.ui.home.HomeFragment;
import com.example.foodapp.ui.profile.ProfileFragment;
import com.google.android.material.badge.BadgeDrawable;

public class MainActivity extends BaseActivity {
    private static final String EXTRA_TAB = "tab";
    private static final String STATE_TAB = "current_tab";

    private ActivityMainBinding binding;
    private int currentTab = R.id.nav_home;

    /** Mở MainActivity và chuyển tới tab chỉ định (ví dụ từ màn chi tiết -> giỏ hàng). */
    public static void openTab(Context context, @IdRes int tabId) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra(EXTRA_TAB, tabId);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (savedInstanceState != null) {
            currentTab = savedInstanceState.getInt(STATE_TAB, R.id.nav_home);
        } else {
            currentTab = getIntent().getIntExtra(EXTRA_TAB, R.id.nav_home);
        }

        // Chọn tab ban đầu trước khi gắn listener để tránh gọi showTab 2 lần
        binding.bottomNav.setSelectedItemId(currentTab);
        showTab(currentTab);
        binding.bottomNav.setOnItemSelectedListener(item -> {
            showTab(item.getItemId());
            return true;
        });
        binding.bottomNav.setOnItemReselectedListener(item -> {
            Fragment f = getSupportFragmentManager().findFragmentByTag(tag(item.getItemId()));
            if (f instanceof TabFragment && f.getView() != null) ((TabFragment) f).onTabReselected();
        });

        binding.fabChat.setOnClickListener(v -> startActivity(new Intent(this, ChatActivity.class)));

        // Badge số lượng món trên icon giỏ hàng
        AppState.cartCount().observe(this, count -> {
            BadgeDrawable badge = binding.bottomNav.getOrCreateBadge(R.id.nav_cart);
            badge.setBackgroundColor(ContextCompat.getColor(this, R.color.danger));
            badge.setNumber(count == null ? 0 : count);
            badge.setVisible(count != null && count > 0);
        });

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (currentTab != R.id.nav_home) {
                    binding.bottomNav.setSelectedItemId(R.id.nav_home);
                } else {
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        FoodActions.refreshCartCount();
    }

    @Override
    protected void onNewIntent(@NonNull Intent intent) {
        super.onNewIntent(intent);
        int tab = intent.getIntExtra(EXTRA_TAB, 0);
        if (tab != 0) binding.bottomNav.setSelectedItemId(tab);
    }

    public void selectTab(@IdRes int tabId) {
        binding.bottomNav.setSelectedItemId(tabId);
    }

    public void setChatFabExtended(boolean extended) {
        if (extended) binding.fabChat.extend();
        else binding.fabChat.shrink();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_TAB, currentTab);
    }

    /** Giữ nguyên trạng thái các tab bằng show/hide thay vì replace. */
    private void showTab(@IdRes int tabId) {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction tx = fm.beginTransaction()
                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);

        for (Fragment f : fm.getFragments()) {
            if (!f.isHidden() && !tag(tabId).equals(f.getTag())) tx.hide(f);
        }
        Fragment target = fm.findFragmentByTag(tag(tabId));
        boolean isNew = target == null;
        if (isNew) {
            target = createFragment(tabId);
            tx.add(R.id.container, target, tag(tabId));
        } else {
            tx.show(target);
        }
        tx.commitNow();
        currentTab = tabId;
        if (tabId == R.id.nav_home) {
            binding.fabChat.show();
            binding.fabChat.extend();
        } else {
            binding.fabChat.hide();
        }

        // Khi Activity được tạo lại (xoay màn hình, đổi Dark mode) fragment chưa có view -> bỏ qua
        if (!isNew && target instanceof TabFragment && target.getView() != null) {
            ((TabFragment) target).onTabSelected();
        }
    }

    private Fragment createFragment(@IdRes int tabId) {
        if (tabId == R.id.nav_favorites) return new FavoritesFragment();
        if (tabId == R.id.nav_cart) return new CartFragment();
        if (tabId == R.id.nav_profile) return new ProfileFragment();
        return new HomeFragment();
    }

    private static String tag(@IdRes int tabId) {
        return "tab_" + tabId;
    }
}
