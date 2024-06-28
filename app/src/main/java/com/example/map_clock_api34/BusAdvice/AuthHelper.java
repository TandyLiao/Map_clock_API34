package com.example.map_clock_api34.BusAdvice;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.fragment.app.FragmentActivity;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AuthHelper {

    private static final String TOKEN_URL = "https://tdx.transportdata.tw/auth/realms/TDXConnect/protocol/openid-connect/token";
    private static final String CLIENT_ID = "410631773-c1cdfe37-9629-4d15"; // 替換為你的Client ID
    private static final String CLIENT_SECRET = "0c434717-9fe4-4326-a7a4-b8c08394587f"; // 替換為你的Client Secret
    private OkHttpClient client;

    public AuthHelper() {
        this.client = new OkHttpClient();
    }

    public void getAccessToken(final AuthCallback callback) {
        RequestBody formBody = new FormBody.Builder()
                .add("grant_type", "client_credentials")
                .add("client_id", CLIENT_ID)
                .add("client_secret", CLIENT_SECRET)
                .build();

        Request request = new Request.Builder()
                .url(TOKEN_URL)
                .post(formBody)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("AuthHelper", "Network error: " + e.getMessage());
                callback.onFailure("Network error: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    Log.e("AuthHelper", "Request failed: " + response.message());
                    callback.onFailure("Request failed: " + response.message());
                    return;
                }

                try {
                    String jsonResponse = response.body().string();
                    JSONObject jsonObject = new JSONObject(jsonResponse);
                    String accessToken = jsonObject.getString("access_token");
                    Log.d("AuthHelper", "Access Token: " + accessToken);
                    callback.onSuccess(accessToken);
                } catch (Exception e) {
                    Log.e("AuthHelper", "Parsing error: " + e.getMessage());
                    callback.onFailure("Parsing error: " + e.getMessage());
                }
            }
        });
    }

    public interface AuthCallback {
        void onSuccess(String accessToken);
        void onFailure(String errorMessage);
    }
}

