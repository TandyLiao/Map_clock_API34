package com.example.map_clock_api34.BusAdvice;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.map_clock_api34.BuildConfig;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import com.example.map_clock_api34.BuildConfig;


public class AuthHelper {

    //用於OAuth2.0身份驗證
    private static final String TOKEN_URL = "https://tdx.transportdata.tw/auth/realms/TDXConnect/protocol/openid-connect/token";
    private static final String CLIENT_ID = BuildConfig.AUTH_CLIENT_ID; // 替換為Client ID
    private static final String CLIENT_SECRET = BuildConfig.AUTH_CLIENT_SECRET; // 替換為Client Secret
    private static final long TOKEN_EXPIRY_BUFFER = 60 * 1000; // 1分鐘的緩衝時間

    // 用於保存和加載token的SharedPreferences
    private static final String PREFS_NAME = "AuthHelperPrefs";
    private static final String KEY_ACCESS_TOKEN = "AccessToken";
    private static final String KEY_TOKEN_EXPIRY_TIME = "TokenExpiryTime";

    // 用於發送HTTP請求
    private OkHttpClient client;

    // 緩存的訪問Token和過期時間
    private String cachedAccessToken;
    private long tokenExpiryTime;

    // 用於保存訪問令牌和過期時間的SharedPreferences
    private SharedPreferences sharedPreferences;

    // 初始化OkHttpClient和SharedPreferences，並加載緩存的Token
    public AuthHelper(Context context) {
        this.client = new OkHttpClient();
        this.sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        loadToken();
    }

    // 獲取訪問Token的方法，如果Token有效，直接回調成功；否則請求新Token
    public void getAccessToken(final AuthCallback callback) {

        // 檢查緩存的Token是否有效
        if (isTokenValid()) {
            callback.onSuccess(cachedAccessToken);
            return;
        }

        RequestBody formBody = new FormBody.Builder()
                .add("grant_type", "client_credentials")
                .add("client_id", CLIENT_ID)
                .add("client_secret", CLIENT_SECRET)
                .build();

        // 構建HTTP POST請求
        Request request = new Request.Builder()
                .url(TOKEN_URL)
                .post(formBody)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .build();

        // 執行HTTP請求，並處理回調
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                // 請求失敗時的回調處理
                Log.e("AuthHelper", "網路異常: " + e.getMessage());
                callback.onFailure("網路異常: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    // 請求不成功時的回調處理
                    Log.e("AuthHelper", "請求失敗: " + response.message());
                    callback.onFailure("請求失敗: " + response.message());
                    return;
                }

                try {
                    // 解析JSON回應，提取訪問Token和過期時間
                    String jsonResponse = response.body().string();
                    JSONObject jsonObject = new JSONObject(jsonResponse);
                    cachedAccessToken = jsonObject.getString("access_token");
                    int expiresIn = jsonObject.getInt("expires_in");
                    tokenExpiryTime = System.currentTimeMillis() + (expiresIn * 1000) - TOKEN_EXPIRY_BUFFER;
                    saveToken();
                    Log.d("AuthHelper", "Token: " + cachedAccessToken);
                    callback.onSuccess(cachedAccessToken);
                } catch (Exception e) {
                    // 解析JSON失敗時的回調處理
                    Log.e("AuthHelper", "錯誤: " + e.getMessage());
                    callback.onFailure("錯誤: " + e.getMessage());
                }
            }
        });
    }

    // 檢查緩存的Token是否有效的方法
    private boolean isTokenValid() {
        return cachedAccessToken != null && System.currentTimeMillis() < tokenExpiryTime;
    }

    // 保存訪問Token和過期時間到SharedPreferences的方法
    private void saveToken() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_ACCESS_TOKEN, cachedAccessToken);
        editor.putLong(KEY_TOKEN_EXPIRY_TIME, tokenExpiryTime);
        editor.apply();
    }

    // 從SharedPreferences加載訪問Token和過期時間的方法
    private void loadToken() {
        cachedAccessToken = sharedPreferences.getString(KEY_ACCESS_TOKEN, null);
        tokenExpiryTime = sharedPreferences.getLong(KEY_TOKEN_EXPIRY_TIME, 0);
    }

    // 回調接口，用於通知訪問Token的獲取結果
    public interface AuthCallback {
        void onSuccess(String accessToken);
        void onFailure(String errorMessage);
    }
}
