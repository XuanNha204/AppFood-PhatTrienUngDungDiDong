package com.example.foodapp.api;

import androidx.annotation.NonNull;

import com.example.foodapp.data.AppState;
import com.example.foodapp.data.SessionManager;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/** Gắn JWT vào header của mọi request và xử lý khi token hết hạn (HTTP 401). */
public class AuthInterceptor implements Interceptor {

    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        SessionManager session = SessionManager.get();
        String token = session.getToken();

        Request request = chain.request();
        if (token != null) {
            request = request.newBuilder()
                    .header("Authorization", "Bearer " + token)
                    .build();
        }

        Response response = chain.proceed(request);
        boolean isAuthEndpoint = request.url().encodedPath().startsWith("/api/auth/");
        if (response.code() == 401 && token != null && !isAuthEndpoint) {
            session.clear();
            AppState.notifySessionExpired();
        }
        return response;
    }
}
