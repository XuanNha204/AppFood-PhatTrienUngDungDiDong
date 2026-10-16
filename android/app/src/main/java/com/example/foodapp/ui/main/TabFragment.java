package com.example.foodapp.ui.main;

/** Fragment trong BottomNavigation: được gọi khi tab hiển thị lại để làm mới dữ liệu. */
public interface TabFragment {
    void onTabSelected();

    default void onTabReselected() {}
}
