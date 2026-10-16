package com.example.foodapp.model;

import java.util.ArrayList;
import java.util.List;

public class User {
    private String id;
    private String name;
    private String email;
    private String phone;
    private String address;
    private String avatarUrl;
    private List<String> favorites;

    public String getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPhone() { return phone == null ? "" : phone; }
    public String getAddress() { return address == null ? "" : address; }
    public String getAvatarUrl() { return avatarUrl; }

    public List<String> getFavorites() {
        return favorites == null ? new ArrayList<>() : favorites;
    }

    public void setFavorites(List<String> favorites) { this.favorites = favorites; }
}
