package com.example.foodapp.model;

public class Food {
    private String id;
    private String name;
    private String description;
    private long price;
    private String imageUrl;
    private Category category;
    private double rating;
    private int ratingCount;
    private int prepTime;
    private int calories;
    private int soldCount;
    private boolean isPopular;
    private String createdBy;

    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description == null ? "" : description; }
    public long getPrice() { return price; }
    public String getImageUrl() { return imageUrl; }
    public Category getCategory() { return category; }
    public double getRating() { return rating; }
    public int getRatingCount() { return ratingCount; }
    public int getPrepTime() { return prepTime; }
    public int getCalories() { return calories; }
    public int getSoldCount() { return soldCount; }
    public boolean isPopular() { return isPopular; }
    public String getCreatedBy() { return createdBy; }

    public String getCategoryName() {
        return category == null ? "" : category.getName();
    }

    public String getCategoryId() {
        return category == null ? null : category.getId();
    }
}
