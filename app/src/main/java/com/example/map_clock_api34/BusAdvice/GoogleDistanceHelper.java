package com.example.map_clock_api34.BusAdvice;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.map_clock_api34.BuildConfig;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class GoogleDistanceHelper {
    private static final String TAG = "GoogleDistanceHelper";
    private static final String BASE_URL = "https://maps.googleapis.com/maps/api/distancematrix/json";
    private static final String API_KEY = BuildConfig.GOOGLE_DISTANCE_API_KEY;
    private static final int MAX_DESTINATIONS_PER_REQUEST = 25;
    private static final int RATE_LIMIT = 50; // 每秒最多請求次數
    private static final long TIME_WINDOW = 1000L; // 毫秒

    private Context context;
    private OkHttpClient client;
    private Handler rateLimitHandler;
    private AtomicInteger requestCount;

    public GoogleDistanceHelper(Context context) {
        this.context = context;
        this.client = new OkHttpClient();
        this.rateLimitHandler = new Handler(Looper.getMainLooper());
        this.requestCount = new AtomicInteger(0);
    }

    public void getWalkingDistances(double originLat, double originLon, List<String> destinations, DistanceCallback callback) {
        int numDestinations = destinations.size();
        List<String> destinationBatch = new ArrayList<>();

        for (int i = 0; i < numDestinations; i++) {
            destinationBatch.add(destinations.get(i));
            if (destinationBatch.size() == MAX_DESTINATIONS_PER_REQUEST || i == numDestinations - 1) {
                String destinationString = String.join("|", destinationBatch);
                String requestUrl = BASE_URL + "?units=metric&origins=" + originLat + "," + originLon +
                        "&destinations=" + destinationString + "&mode=walking&key=" + API_KEY;

                Log.d(TAG, "Request URL: " + requestUrl);

                Request request = new Request.Builder().url(requestUrl).build();

                executeRequestWithRateLimit(request, callback);

                destinationBatch.clear();
            }
        }
    }

    private void executeRequestWithRateLimit(Request request, DistanceCallback callback) {
        // 使用 Handler 來管理計時和請求排程
        rateLimitHandler.post(() -> {
            // 檢查當前的請求計數是否小於設定的速率限制（50次/秒）
            if (requestCount.get() < RATE_LIMIT) {
                // 增加請求計數
                requestCount.incrementAndGet();
                // 使用 OkHttpClient 執行 HTTP 請求並處理回調
                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        // 如果請求失敗，記錄錯誤，調用 callback.onFailure 並減少請求計數
                        Log.e(TAG, "Network error: " + e.getMessage());
                        if (callback != null) {
                            callback.onFailure(e.getMessage());
                        }
                        requestCount.decrementAndGet();
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        try {
                            // 如果請求成功，處理響應
                            if (!response.isSuccessful()) {
                                Log.e(TAG, "Request failed: " + response.message());
                                if (callback != null) {
                                    callback.onFailure(response.message());
                                }
                            } else {
                                String responseBody = response.body().string();
                                if (callback != null) {
                                    callback.onSuccess(responseBody);
                                }
                            }
                        } finally {
                            // 最終減少請求計數
                            requestCount.decrementAndGet();
                        }
                    }
                });
            } else {
                // 如果請求計數超過速率限制，延遲 TIME_WINDOW（1000毫秒）後重試請求
                Log.e(TAG, "Rate limit exceeded, delaying request");
                rateLimitHandler.postDelayed(() -> executeRequestWithRateLimit(request, callback), TIME_WINDOW);
            }

            // 每秒鐘減少請求計數
            rateLimitHandler.postDelayed(() -> requestCount.decrementAndGet(), TIME_WINDOW);
        });
    }


    public interface DistanceCallback {
        void onSuccess(String jsonResponse);
        void onFailure(String errorMessage);
    }
}
