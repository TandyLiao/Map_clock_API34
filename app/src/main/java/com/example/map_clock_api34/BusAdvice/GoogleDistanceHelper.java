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
    // 常量定義
    private static final String TAG = "GoogleDistanceHelper"; // 用於日誌記錄的標籤
    private static final String BASE_URL = "https://maps.googleapis.com/maps/api/distancematrix/json"; // Google Distance Matrix API 基本 URL
    private static final String API_KEY = BuildConfig.GOOGLE_DISTANCE_API_KEY; // 使用 BuildConfig 中定義的 API 密鑰
    private static final int MAX_DESTINATIONS_PER_REQUEST = 25; // 每次請求的最大目的地數量
    private static final int RATE_LIMIT = 50; // 每秒最多請求次數限制
    private static final long TIME_WINDOW = 1000L; // 時間窗口，1 秒

    private Context context;
    private OkHttpClient client; // OkHttp 用於發送 HTTP 請求
    private Handler rateLimitHandler; // 用於速率限制的 Handler
    private AtomicInteger requestCount; // 用於追踪當前請求次數的原子計數器

    // 建構子，初始化 OkHttpClient 和 Handler
    public GoogleDistanceHelper(Context context) {
        this.context = context;
        this.client = new OkHttpClient();
        this.rateLimitHandler = new Handler(Looper.getMainLooper());
        this.requestCount = new AtomicInteger(0);
    }

    /**
     * 發送請求以獲取步行距離。
     *
     * @param originLat   起點的緯度
     * @param originLon   起點的經度
     * @param destinations 目的地列表
     * @param callback    請求結果的回調
     */
    public void getWalkingDistances(double originLat, double originLon, List<String> destinations, DistanceCallback callback) {
        int numDestinations = destinations.size(); // 獲取目的地數量
        List<String> destinationBatch = new ArrayList<>(); // 用於批次處理目的地

        // 逐一處理每個目的地
        for (int i = 0; i < numDestinations; i++) {
            destinationBatch.add(destinations.get(i)); // 添加目的地到批次
            // 如果已達到每次請求的最大目的地數量，或已處理完所有目的地
            if (destinationBatch.size() == MAX_DESTINATIONS_PER_REQUEST || i == numDestinations - 1) {
                // 將批次中的目的地組合為一個字符串
                String destinationString = String.join("|", destinationBatch);
                // 構建請求 URL
                String requestUrl = BASE_URL + "?units=metric&origins=" + originLat + "," + originLon +
                        "&destinations=" + destinationString + "&mode=walking&key=" + API_KEY;

                Log.d(TAG, "請求 URL: " + requestUrl);

                // 創建 HTTP 請求
                Request request = new Request.Builder().url(requestUrl).build();

                // 發送請求並執行速率限制
                executeRequestWithRateLimit(request, callback);

                // 清空批次
                destinationBatch.clear();
            }
        }
    }

    /**
     * 執行帶有速率限制的 HTTP 請求。
     *
     * @param request   要發送的 HTTP 請求
     * @param callback  處理結果的回調介面
     */
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
                        Log.e(TAG, "網路錯誤: " + e.getMessage());
                        if (callback != null) {
                            callback.onFailure(e.getMessage());
                        }
                        requestCount.decrementAndGet(); // 請求結束，計數減少
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        try {
                            // 如果請求成功，處理響應
                            if (!response.isSuccessful()) {
                                Log.e(TAG, "請求失敗: " + response.message());
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
                Log.e(TAG, "傳輸速率超過限制");
                rateLimitHandler.postDelayed(() -> executeRequestWithRateLimit(request, callback), TIME_WINDOW);
            }

            // 每秒鐘減少請求計數
            rateLimitHandler.postDelayed(() -> requestCount.decrementAndGet(), TIME_WINDOW);
        });
    }

    // 回調介面，用於處理距離計算結果
    public interface DistanceCallback {
        void onSuccess(String jsonResponse); // 當請求成功時調用
        void onFailure(String errorMessage); // 當請求失敗時調用
    }
}
