package com.example.foodapp.model;

public class OrderItem {
    private String food;
    private String name;
    private long price;
    private String imageUrl;
    private int quantity;

    public String getFoodId() { return food; }
    public String getName() { return name; }
    public long getPrice() { return price; }
    public String getImageUrl() { return imageUrl; }
    public int getQuantity() { return quantity; }
}
