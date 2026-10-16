package com.example.foodapp.model;

public class Category {
    private String id;
    private String name;
    private String description;
    private String imageUrl;
    private int foodCount;

    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getImageUrl() { return imageUrl; }
    public int getFoodCount() { return foodCount; }

    // ArrayAdapter của dropdown hiển thị bằng toString()
    @Override
    public String toString() { return name; }
}
