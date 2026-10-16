package com.example.foodapp.model;

import java.util.ArrayList;
import java.util.List;

public class Cart {
    private List<CartItem> items;
    private int totalQuantity;
    private long subtotal;
    private long deliveryFee;
    private long total;
    private long freeShipThreshold;

    public List<CartItem> getItems() { return items == null ? new ArrayList<>() : items; }
    public int getTotalQuantity() { return totalQuantity; }
    public long getSubtotal() { return subtotal; }
    public long getDeliveryFee() { return deliveryFee; }
    public long getTotal() { return total; }
    public long getFreeShipThreshold() { return freeShipThreshold; }

    public boolean isEmpty() { return getItems().isEmpty(); }
}
