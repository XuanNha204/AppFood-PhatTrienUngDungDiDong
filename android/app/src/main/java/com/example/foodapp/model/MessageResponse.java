package com.example.foodapp.model;

public class MessageResponse {
    private String message;
    // Chỉ có ở môi trường dev khi backend chưa cấu hình gửi email
    private String devOtp;

    public String getMessage() { return message; }
    public String getDevOtp() { return devOtp; }
}
