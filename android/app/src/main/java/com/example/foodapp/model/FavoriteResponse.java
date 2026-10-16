package com.example.foodapp.model;

import java.util.List;

public class FavoriteResponse {
    private boolean isFavorite;
    private List<String> favorites;

    public boolean isFavorite() { return isFavorite; }
    public List<String> getFavorites() { return favorites; }
}
