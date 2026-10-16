package com.example.foodapp.model;

public class CartItem {
    private Food food;
    private int quantity;
    private long lineTotal;

    public Food getFood() { return food; }
    public int getQuantity() { return quantity; }
    public long getLineTotal() { return lineTotal; }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
        this.lineTotal = food == null ? 0 : food.getPrice() * quantity;
    }
}
