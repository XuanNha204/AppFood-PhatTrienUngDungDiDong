package com.example.foodapp.api;

import com.example.foodapp.model.AuthResponse;
import com.example.foodapp.model.Banner;
import com.example.foodapp.model.Cart;
import com.example.foodapp.model.Category;
import com.example.foodapp.model.ChatHistory;
import com.example.foodapp.model.ChatMessage;
import com.example.foodapp.model.FavoriteResponse;
import com.example.foodapp.model.Food;
import com.example.foodapp.model.MessageResponse;
import com.example.foodapp.model.Order;
import com.example.foodapp.model.User;

import java.util.List;
import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.PartMap;
import retrofit2.http.Path;
import retrofit2.http.QueryMap;

public interface ApiService {

    // ---------- Auth (JWT) ----------
    @POST("api/auth/register")
    Call<AuthResponse> register(@Body Map<String, String> body);

    @POST("api/auth/login")
    Call<AuthResponse> login(@Body Map<String, String> body);

    @POST("api/auth/forgot-password")
    Call<MessageResponse> forgotPassword(@Body Map<String, String> body);

    @POST("api/auth/reset-password")
    Call<MessageResponse> resetPassword(@Body Map<String, String> body);

    @POST("api/auth/change-password")
    Call<MessageResponse> changePassword(@Body Map<String, String> body);

    // ---------- User ----------
    @GET("api/users/me")
    Call<User> getMe();

    @PUT("api/users/me")
    Call<User> updateMe(@Body Map<String, String> body);

    @Multipart
    @POST("api/users/me/avatar")
    Call<User> uploadAvatar(@Part MultipartBody.Part image);

    /** Đặt avatar bằng link ảnh: { "imageUrl": "https://..." } */
    @POST("api/users/me/avatar")
    Call<User> setAvatarLink(@Body Map<String, String> body);

    @GET("api/users/me/favorites")
    Call<List<Food>> getFavorites();

    @POST("api/users/me/favorites/{foodId}")
    Call<FavoriteResponse> toggleFavorite(@Path("foodId") String foodId);

    // ---------- Catalog ----------
    @GET("api/banners")
    Call<List<Banner>> getBanners();

    @GET("api/categories")
    Call<List<Category>> getCategories();

    @GET("api/foods")
    Call<List<Food>> getFoods(@QueryMap Map<String, String> query);

    @GET("api/foods/mine")
    Call<List<Food>> getMyFoods();

    @GET("api/foods/{id}")
    Call<Food> getFood(@Path("id") String id);

    @Multipart
    @POST("api/foods")
    Call<Food> createFood(@PartMap Map<String, RequestBody> fields, @Part MultipartBody.Part image);

    @Multipart
    @PUT("api/foods/{id}")
    Call<Food> updateFood(@Path("id") String id, @PartMap Map<String, RequestBody> fields,
                          @Part MultipartBody.Part image);

    @DELETE("api/foods/{id}")
    Call<MessageResponse> deleteFood(@Path("id") String id);

    // ---------- Cart ----------
    @GET("api/cart")
    Call<Cart> getCart();

    @POST("api/cart/items")
    Call<Cart> addToCart(@Body Map<String, Object> body);

    @PUT("api/cart/items/{foodId}")
    Call<Cart> updateCartItem(@Path("foodId") String foodId, @Body Map<String, Object> body);

    @DELETE("api/cart/items/{foodId}")
    Call<Cart> removeCartItem(@Path("foodId") String foodId);

    @DELETE("api/cart")
    Call<Cart> clearCart();

    // ---------- Orders ----------
    @POST("api/orders")
    Call<Order> createOrder(@Body Map<String, String> body);

    @GET("api/orders")
    Call<List<Order>> getOrders();

    @GET("api/orders/{id}")
    Call<Order> getOrder(@Path("id") String id);

    @PUT("api/orders/{id}/cancel")
    Call<Order> cancelOrder(@Path("id") String id);

    // ===== Chatbot AI =====
    @GET("api/chat")
    Call<ChatHistory> getChatHistory();

    /** body: { "message": "..." } -> câu trả lời của trợ lý */
    @POST("api/chat")
    Call<ChatMessage> sendChatMessage(@Body Map<String, String> body);

    @DELETE("api/chat")
    Call<MessageResponse> clearChat();
}
