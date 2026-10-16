package com.example.foodapp.model;

import java.util.ArrayList;
import java.util.List;

public class Order {
    public static final String PENDING = "PENDING";
    public static final String CONFIRMED = "CONFIRMED";
    public static final String DELIVERING = "DELIVERING";
    public static final String COMPLETED = "COMPLETED";
    public static final String CANCELLED = "CANCELLED";

    private String id;
    private String code;
    private List<OrderItem> items;
    private long subtotal;
    private long deliveryFee;
    private long total;
    private String address;
    private String phone;
    private String note;
    private String paymentMethod;
    private String status;
    private String createdAt;

    public String getId() { return id; }
    public String getCode() { return code; }
    public List<OrderItem> getItems() { return items == null ? new ArrayList<>() : items; }
    public long getSubtotal() { return subtotal; }
    public long getDeliveryFee() { return deliveryFee; }
    public long getTotal() { return total; }
    public String getAddress() { return address; }
    public String getPhone() { return phone; }
    public String getNote() { return note == null ? "" : note; }
    public String getPaymentMethod() { return paymentMethod; }
    public String getStatus() { return status; }
    public String getCreatedAt() { return createdAt; }

    public int getTotalQuantity() {
        int sum = 0;
        for (OrderItem item : getItems()) sum += item.getQuantity();
        return sum;
    }
}
