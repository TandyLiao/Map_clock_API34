package com.example.map_clock_api34.BusAdvice;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.map_clock_api34.SharedViewModel;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class BusStationFinderHelper {

    // API 相關常量
    private static final String BASE_URL = "https://tdx.transportdata.tw/api/basic/v2/Bus/Stop/City/";
    private static final String STOP_OF_ROUTE_URL = "https://tdx.transportdata.tw/api/basic/v2/Bus/StopOfRoute/City/";

    private final Context context;
    private final SharedViewModel sharedViewModel;

    private final OkHttpClient client;      // OkHttp 用來發送 HTTP 請求
    private final AuthHelper authHelper;    // 認證輔助工具
    private final Handler mainHandler;      // 用來在主執行緒執行任務的 Handler
    private final GoogleDistanceHelper googleDistanceHelper; // Google 距離輔助工具

    private CityInEnglishHelper cityTranslate; // 城市名稱翻譯輔助工具

    private List<BusStation> secondNearByStop = new ArrayList<>();  // 附近的站牌
    private List<BusStation> secondDesStop = new ArrayList<>();     // 目的地站牌

    // 自動更新的 Handler
    private final Handler updateHandler;
    private final Runnable updateRunnable;
    private static final int UPDATE_INTERVAL = 60000; // 自動更新間隔，1分鐘

    // 請求速率限制
    private static final int RATE_LIMIT = 50;       // 每秒最多請求次數
    private static final long TIME_WINDOW = 1000L;  // 1 秒為單位
    private int requestCount = 0;
    private final Handler rateLimitHandler = new Handler(Looper.getMainLooper());
    private static final long REQUEST_DELAY = 2000L; // 請求之間的延遲時間（2秒）

    private final BusStationFinderCallback callback; // 查找站點的回調介面
    private ToastCallback toastCallback; // Toast 的回調介面

    private String cityName; // 城市名稱
    private Boolean isShow = false; // 控制 Toast 是否已顯示

    // 介面：用來回傳站牌查找結果
    public interface BusStationFinderCallback {
        void onBusStationsFound(List<BusStation> nearbyStops);
    }

    // 介面：用來顯示 Toast 訊息
    public interface ToastCallback {
        void onToastShown(String message);
    }

    // 設定 Toast 回調
    public void setToastCallback(ToastCallback toastCallback) {
        this.toastCallback = toastCallback;
    }

    public BusStationFinderHelper(Context context, SharedViewModel sharedViewModel, BusStationFinderCallback callback) {
        this.context = context;
        this.sharedViewModel = sharedViewModel;
        this.client = new OkHttpClient();
        this.authHelper = new AuthHelper(context);
        this.mainHandler = new Handler(Looper.getMainLooper());
        this.googleDistanceHelper = new GoogleDistanceHelper(context);
        this.cityTranslate = new CityInEnglishHelper();
        this.callback = callback;

        // 初始化自動更新的 Handler 和 Runnable
        this.updateHandler = new Handler(Looper.getMainLooper());
        this.updateRunnable = new Runnable() {
            @Override
            public void run() {
                refreshArrivalTimes(); // 更新到站時間
                updateHandler.postDelayed(this, UPDATE_INTERVAL); // 定期調度
            }
        };
    }

    // 查找附近站牌
    public void findNearbyStations(View view) {
        authHelper.getAccessToken(new AuthHelper.AuthCallback() {
            @Override
            public void onSuccess(String accessToken) {
                try {
                    findNearbyBusStops(view, accessToken); // 成功獲取到 AccessToken 後查找站牌
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(String errorMessage) {
                Log.e("BusStationFinderHelper", "Auth error: " + errorMessage);
                if (context != null) {
                    mainHandler.post(() -> showToast("無法獲取授權")); // 認證失敗提示
                }
            }
        });
    }

    // 查找使用者附近和目的地的站牌
    private void findNearbyBusStops(View view, String accessToken) throws UnsupportedEncodingException {
        // 獲取使用者當前位置和目的地位置的經緯度
        double currentLat = sharedViewModel.getNowLantitude();
        double currentLon = sharedViewModel.getNowLontitude();
        double destLat = sharedViewModel.getLatitude(0);
        double destLon = sharedViewModel.getLongitude(0);

        // 設定經緯度的範圍，用於過濾站牌
        double latDiff = 0.0025;
        double lonDiff = 0.0025;

        // 計算使用者附近的經緯度範圍
        double minLat = currentLat - latDiff;
        double maxLat = currentLat + latDiff;
        double minLon = currentLon - lonDiff;
        double maxLon = currentLon + lonDiff;

        // 計算目的地附近的經緯度範圍
        double destMinLat = destLat - latDiff;
        double destMaxLat = destLat + latDiff;
        double destMinLon = destLon - lonDiff;
        double destMaxLon = destLon + lonDiff;

        // 取得目的地的城市名稱
        cityName = cityTranslate.getCityInEnglish(sharedViewModel.getCapital(0));

        // 建立查詢站牌的 URL
        String userStopsUrl = BASE_URL + cityName + "?$filter=( " +
                "StopPosition/PositionLat ge " + minLat + " and StopPosition/PositionLat le " + maxLat + " and " +
                "StopPosition/PositionLon ge " + minLon + " and StopPosition/PositionLon le " + maxLon + "" +
                ") or ( " +
                "StopPosition/PositionLat ge " + destMinLat + " and StopPosition/PositionLat le " + destMaxLat + " and " +
                "StopPosition/PositionLon ge " + destMinLon + " and StopPosition/PositionLon le " + destMaxLon + "" +
                ")&$format=JSON";

        Log.d("BusStationFinderHelper", "User Stops URL: " + userStopsUrl);

        // 發送 HTTP 請求查詢站牌
        Request userStopsRequest = new Request.Builder()
                .url(userStopsUrl)
                .addHeader("Authorization", "Bearer " + accessToken)
                .build();

        executeRequestWithRateLimit(userStopsRequest, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("BusStationFinderHelper", "Network error: " + e.getMessage());
                if (context != null) {
                    mainHandler.post(() -> showToast("網路錯誤")); // 網路錯誤提示
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    Log.e("BusStationFinderHelper", "Request failed: " + response.message() + ", Code: " + response.code());
                    Log.e("BusStationFinderHelper", "Response Body: " + response.body().string());
                    if (context != null) {
                        mainHandler.post(() -> showToast("無法獲取公車站牌，請稍後再試一次"));
                    }
                    return;
                }

                try {
                    String userStopsResponse = response.body().string(); // 取得回應內容
                    Log.d("BusStationFinderHelper", "User Stops Response: " + userStopsResponse);

                    // 由於附近地區和目的地地區是一起送出並回傳的，所以需另外解析並分開使用者附近和目的地的站牌
                    List<BusStation> firstCurrentList = parseStops(userStopsResponse, currentLat, currentLon);
                    List<BusStation> firstDesList = parseStops(userStopsResponse, destLat, destLon);

                    secondNearByStop = new ArrayList<>();
                    secondDesStop = new ArrayList<>();

                    // 當206行執行完後，篩選完成時的回調，再進行路線的查詢
                    Runnable onCompletion = () -> {
                        mainHandler.postDelayed(() -> findRoutes(view, accessToken, cityName, secondNearByStop, secondDesStop), REQUEST_DELAY);
                    };

                    // 依照距離過濾站牌
                    filterNearbyStops(currentLat, currentLon, firstCurrentList, secondNearByStop, () ->
                            filterNearbyStops(destLat, destLon, firstDesList, secondDesStop, onCompletion));

                } catch (Exception e) {
                    Log.e("BusStationFinderHelper", "Parsing error: " + e.getMessage());
                    if (context != null) {
                        mainHandler.post(() -> showToast("無法解析公車站牌資訊，請稍後再試"));
                    }
                }
            }
        });
    }

    // 限制請求速率，避免超出伺服器限制
    private void executeRequestWithRateLimit(Request request, Callback callback) {
        rateLimitHandler.post(() -> {
            if (requestCount < RATE_LIMIT) {
                requestCount++;
                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        Log.e("BusStationFinderHelper", "網路錯誤: " + e.getMessage());
                        if (context != null) {
                            mainHandler.post(() -> showToast("網路錯誤"));
                        }
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        if (response.code() == 429) { // Too Many Requests
                            long retryAfter = 2000L; // 默認的重試時間（2秒）
                            if (response.headers().getDate("Retry-After") != null) {
                                retryAfter = response.headers().getDate("Retry-After").getTime() - System.currentTimeMillis();
                            }
                            rateLimitHandler.postDelayed(() -> executeRequestWithRateLimit(request, callback), retryAfter);
                        } else {
                            callback.onResponse(call, response);
                        }
                    }
                });
            } else {
                rateLimitHandler.postDelayed(() -> executeRequestWithRateLimit(request, callback), TIME_WINDOW);
            }

            rateLimitHandler.postDelayed(() -> requestCount--, TIME_WINDOW);
        });
    }

    // 解析站牌資訊
    private List<BusStation> parseStops(String jsonResponse, double Lat, double Lon) throws Exception {
        JSONArray jsonArray = new JSONArray(jsonResponse);
        List<BusStation> stationList = new ArrayList<>();
        Set<String> seenStops = new HashSet<>(); // 防止重複加入相同站牌

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject stopObj = jsonArray.getJSONObject(i);
            String stopName = stopObj.getJSONObject("StopName").getString("Zh_tw");
            double stopLat = stopObj.getJSONObject("StopPosition").getDouble("PositionLat");
            double stopLon = stopObj.getJSONObject("StopPosition").getDouble("PositionLon");

            String key = stopName + "," + stopLat + "," + stopLon;

            if (isNearby(stopLat, stopLon, Lat, Lon) && !seenStops.contains(key)) {
                stationList.add(new BusStation(stopName, stopLat, stopLon)); // 新增站牌到列表
                seenStops.add(key); // 記錄該站牌，避免重複
            }
        }

        return stationList;
    }

    // 判斷站牌是否在指定範圍內
    private boolean isNearby(double stopLat, double stopLon, double targetLat, double targetLon) {
        double latDiff = 0.0025;
        double lonDiff = 0.0025;
        return Math.abs(stopLat - targetLat) <= latDiff && Math.abs(stopLon - targetLon) <= lonDiff;
    }

    // 過濾 700 公尺內步行可到的站牌
    private void filterNearbyStops(double originLat, double originLon, List<BusStation> firstList, List<BusStation> resultList, Runnable callback) {
        List<String> destinations = new ArrayList<>();
        for (BusStation station : firstList) {
            destinations.add(station.getStopLat() + "," + station.getStopLon());
        }

        googleDistanceHelper.getWalkingDistances(originLat, originLon, destinations, new GoogleDistanceHelper.DistanceCallback() {
            @Override
            public void onSuccess(String jsonResponse) {
                try {
                    List<BusStation> stopDistances = parseDistances(jsonResponse, firstList); // 解析距離
                    Set<String> seenStops = new HashSet<>();

                    for (BusStation station : stopDistances) {
                        String key = station.getStopName() + "," + station.getStopLat() + "," + station.getStopLon();
                        if (Integer.valueOf(station.getDistance()) <= 700 && !seenStops.contains(key)) {
                            resultList.add(station); // 新增 700 公尺內的站牌到結果列表
                            seenStops.add(key); // 記錄已處理的站牌
                        }
                    }

                    callback.run(); // 執行回調
                } catch (Exception e) {
                    Log.e("BusStationFinderHelper", "解析距離時出錯: " + e.getMessage());
                    if (context != null) {
                        mainHandler.post(() -> showToast("解析距離時出錯"));
                    }
                }
            }

            @Override
            public void onFailure(String errorMessage) {
                Log.e("BusStationFinderHelper", "無法獲取距離: " + errorMessage);
                if (context != null) {
                    mainHandler.post(() -> showToast("無法獲取距離，請稍後再試"));
                }
            }
        });
    }

    // 解析距離資料
    private List<BusStation> parseDistances(String jsonResponse, List<BusStation> nearByStop) throws Exception {
        List<BusStation> stopDistances = new ArrayList<>();

        JSONObject jsonObject = new JSONObject(jsonResponse);
        JSONArray rows = jsonObject.getJSONArray("rows");

        if (rows.length() == 0) {
            throw new Exception("回傳值為空");
        }

        JSONArray elements = rows.getJSONObject(0).getJSONArray("elements");

        if (elements.length() != nearByStop.size()) {
            throw new Exception("回傳值和站牌數量不匹配");
        }

        for (int i = 0; i < elements.length(); i++) {
            JSONObject element = elements.getJSONObject(i);
            String status = element.getString("status");

            if ("OK".equals(status)) {
                String distanceValue = element.getJSONObject("distance").getString("value");
                BusStation station = nearByStop.get(i);
                stopDistances.add(new BusStation(station.getStopName(), station.getStopLat(), station.getStopLon(), distanceValue));
            }
        }

        Log.d("BusStationFinderHelper", "stopDistances " + stopDistances);

        return stopDistances;
    }

    // 查找站點路線
    private void findRoutes(View view, String accessToken, String cityName, List<BusStation> nearbyStops, List<BusStation> destinationStops) {
        StringBuilder stopFilter = new StringBuilder();
        for (BusStation station : nearbyStops) {
            if (stopFilter.length() > 0) {
                stopFilter.append(" or ");
            }
            stopFilter.append("(Stops/any(s: s/StopPosition/PositionLat eq ").append(station.getStopLat())
                    .append(" and s/StopPosition/PositionLon eq ").append(station.getStopLon()).append("))");
        }
        for (BusStation station : destinationStops) {
            if (stopFilter.length() > 0) {
                stopFilter.append(" or ");
            }
            stopFilter.append("(Stops/any(s: s/StopPosition/PositionLat eq ").append(station.getStopLat())
                    .append(" and s/StopPosition/PositionLon eq ").append(station.getStopLon()).append("))");
        }

        String routeUrl = STOP_OF_ROUTE_URL + cityName + "?$filter=" + stopFilter.toString() + "&$format=JSON";
        Log.d("BusStationFinderHelper", "Route URL: " + routeUrl);

        Request routeRequest = new Request.Builder()
                .url(routeUrl)
                .addHeader("Authorization", "Bearer " + accessToken)
                .build();

        executeRequestWithRateLimit(routeRequest, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("BusStationFinderHelper", "網路錯誤: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    Log.e("BusStationFinderHelper", "請求失敗: " + response.message() + ", Code: " + response.code());
                    Log.e("BusStationFinderHelper", "失敗回應: " + response.body().string());
                    return;
                }

                try {
                    String routeResponse = response.body().string();
                    Log.d("BusStationFinderHelper", "路線回復: " + routeResponse);
                    JSONArray jsonArray = new JSONArray(routeResponse);

                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject routeObj = jsonArray.getJSONObject(i);
                        String routeName = routeObj.getJSONObject("RouteName").getString("Zh_tw");
                        JSONArray stopsArray = routeObj.getJSONArray("Stops");

                        List<BusStation.LatLng> routeLatLngs = new ArrayList<>();

                        // 取得該路線的站牌序列
                        for (int j = 0; j < stopsArray.length(); j++) {
                            JSONObject stopObj = stopsArray.getJSONObject(j);
                            double stopLat = stopObj.getJSONObject("StopPosition").getDouble("PositionLat");
                            double stopLon = stopObj.getJSONObject("StopPosition").getDouble("PositionLon");
                            routeLatLngs.add(new BusStation.LatLng(stopLat, stopLon));
                        }

                        // 比對起點和終點站牌是否出現在路線的順序中，只要起點站站續小於終點站站續，就是有效站牌
                        for (BusStation startStation : nearbyStops) {
                            Map<BusStation.LatLng, String> destinationStopsMap = new HashMap<>();

                            for (BusStation endStation : destinationStops) {
                                if (isRouteValid(routeLatLngs, startStation, endStation)) {
                                    // 添加目的地站點到目標站點對應的路線
                                    destinationStopsMap.put(new BusStation.LatLng(endStation.getStopLat(), endStation.getStopLon()), endStation.getStopName());
                                }
                            }

                            if (!destinationStopsMap.isEmpty()) {
                                // 添加這條路線和目標站點到起點站的資料中
                                startStation.addRoute(routeName, destinationStopsMap);
                            }
                        }
                    }

                    mainHandler.post(() -> {
                        for (BusStation station : nearbyStops) {
                            Log.d("BusStationFinderHelper", "BusStation: " + station.toString());
                        }
                    });

                    // 延遲查找到站時間
                    mainHandler.postDelayed(() -> findArrivalTimes(accessToken, cityName, nearbyStops), REQUEST_DELAY);
                } catch (Exception e) {
                    Log.e("BusStationFinderHelper", "錯誤資訊: " + e.getMessage());
                }
            }
        });
    }


    private boolean isRouteValid(List<BusStation.LatLng> routeLatLngs, BusStation startStation, BusStation endStation) {
        int startIndex = -1;
        int endIndex = -1;

        // 找出起點站和終點站在路線站牌序列中的位置
        for (int i = 0; i < routeLatLngs.size(); i++) {
            BusStation.LatLng latLng = routeLatLngs.get(i);
            if (Math.abs(latLng.getLat() - startStation.getStopLat()) < 0.00001 && Math.abs(latLng.getLon() - startStation.getStopLon()) < 0.0001) {
                startIndex = i;
            }
            if (Math.abs(latLng.getLat() - endStation.getStopLat()) < 0.00001 && Math.abs(latLng.getLon() - endStation.getStopLon()) < 0.00001) {
                endIndex = i;
            }
        }

        // 如果起點站在終點站之前出現，則為有效路線
        return startIndex != -1 && endIndex != -1 && startIndex < endIndex;
    }

    // 查找到站時間
    private void findArrivalTimes(String accessToken, String cityName, List<BusStation> nearbyStops) {
        List<String> stopNames = new ArrayList<>();
        for (BusStation station : nearbyStops) {
            stopNames.add(station.getStopName());
        }

        String stopNameFilter = String.join(" or ", stopNames.stream().map(name -> "StopName/Zh_tw eq '" + name + "'").toArray(String[]::new));
        String arrivalTimeUrl = "https://tdx.transportdata.tw/api/basic/v2/Bus/EstimatedTimeOfArrival/City/" + cityName + "?$filter=" + stopNameFilter + "&$format=JSON";
        Log.d("BusStationFinderHelper", "Arrival Time URL: " + arrivalTimeUrl);

        Request arrivalTimeRequest = new Request.Builder()
                .url(arrivalTimeUrl)
                .addHeader("Authorization", "Bearer " + accessToken)
                .build();

        executeRequestWithRateLimit(arrivalTimeRequest, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("BusStationFinderHelper", "網路錯誤: " + e.getMessage());
                if (context != null) {
                    mainHandler.post(() -> showToast("網路錯誤"));
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    Log.e("BusStationFinderHelper", "Request failed: " + response.message() + ", Code: " + response.code());
                    Log.e("BusStationFinderHelper", "Response Body: " + response.body().string());
                    if (context != null) {
                        mainHandler.post(() -> showToast("無法獲取到站時間，請稍後再試"));
                    }
                    return;
                }

                try {
                    String arrivalTimeResponse = response.body().string();
                    Log.d("BusStationFinderHelper", "抵達時間回復: " + arrivalTimeResponse);

                    // 解析到站時間
                    parseArrivalTimes(arrivalTimeResponse);

                } catch (Exception e) {
                    Log.e("BusStationFinderHelper", "無法解析到站時間: " + e.getMessage());
                    if (context != null) {
                        mainHandler.post(() -> showToast("無法解析到站時間"));
                    }
                }
            }
        });

        // 啟動自動更新到站時間
        updateHandler.postDelayed(updateRunnable, UPDATE_INTERVAL);
    }

    // 解析到站時間
    private void parseArrivalTimes(String jsonResponse) throws Exception {
        JSONArray jsonArray = new JSONArray(jsonResponse);
        Map<String, String> arrivalTimes = new HashMap<>();

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject arrivalObj = jsonArray.getJSONObject(i);
            String routeName = arrivalObj.getJSONObject("RouteName").getString("Zh_tw");
            String stopName = arrivalObj.getJSONObject("StopName").getString("Zh_tw");
            int estimateTime = arrivalObj.optInt("EstimateTime", -1);

            String key = routeName + "-" + stopName;
            String value = estimateTime == -1 ? "未發車" : (estimateTime / 60) + " 分鐘"; // 將到站時間轉換為分鐘數

            arrivalTimes.put(key, value);
        }

        // 儲存到站時間到各個站牌
        for (BusStation station : secondNearByStop) {
            Map<String, String> stationArrivalTimes = new HashMap<>();
            for (String routeName : station.getRoutes().keySet()) {
                String key = routeName + "-" + station.getStopName();
                if (arrivalTimes.containsKey(key)) {
                    stationArrivalTimes.put(routeName, arrivalTimes.get(key));
                }
            }
            station.setArrivalTimes(stationArrivalTimes); // 設定站牌的到站時間
        }

        mainHandler.post(() -> {
            if (!isShow) {
                showToast("路線已更新");
                isShow = true;
            }

            callback.onBusStationsFound(secondNearByStop); // 通知回調更新站牌資訊
        });
    }

    // 刷新到站時間
    private void refreshArrivalTimes() {
        authHelper.getAccessToken(new AuthHelper.AuthCallback() {
            @Override
            public void onSuccess(String accessToken) {
                findArrivalTimes(accessToken, cityName, secondNearByStop);
            }

            @Override
            public void onFailure(String errorMessage) {
                Log.e("BusStationFinderHelper", "無法獲取授權: " + errorMessage);
                if (context != null) {
                    mainHandler.post(() -> showToast("無法獲取授權"));
                }
            }
        });
    }

    // 停止自動更新
    public void stopUpdating() {
        updateHandler.removeCallbacks(updateRunnable);
    }
    // 停止查找和更新
    public void stopSearching() {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> {
            try {
                // 清空連接池和關閉線程池
                client.dispatcher().cancelAll(); // 取消所有正在進行的請求
                client.connectionPool().evictAll(); // 清空連接池

                // 關閉線程池並等待其關閉
                client.dispatcher().executorService().shutdown();
                if (!client.dispatcher().executorService().awaitTermination(5, TimeUnit.SECONDS)) {
                    client.dispatcher().executorService().shutdownNow(); // 強制關閉
                }
            } catch (Exception e) {
                Log.e("BusStationFinderHelper", "Error while shutting down: " + e.getMessage());
            }
        });
        stopUpdating(); // 停止自動更新
    }


    // 顯示 Toast 訊息
    private void showToast(String message) {
        Toast toast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
        toast.show();

        // 使用 Handler 來控制顯示時長
        new Handler().postDelayed(toast::cancel, 1000);

        if (toastCallback != null) {
            toastCallback.onToastShown(message); // 調用回調更新訊息
        }

    }

    // 公車站牌類別
    public static class BusStation {
        private final String stopName;
        private final double stopLat;
        private final double stopLon;
        private String distance;
        private Map<String, Map<LatLng, String>> routes; // 儲存站牌對應的路線
        private Map<String, String> arrivalTimes; // 儲存站牌的到站時間

        // 建構子，初始設定位置信息
        public BusStation(String stopName, double stopLat, double stopLon) {
            this.stopName = stopName;
            this.stopLat = stopLat;
            this.stopLon = stopLon;
            this.routes = new HashMap<>();
            this.arrivalTimes = new HashMap<>();
        }

        // 建構子，初始設定位置信息和距離
        public BusStation(String stopName, double stopLat, double stopLon, String distance) {
            this.stopName = stopName;
            this.stopLat = stopLat;
            this.stopLon = stopLon;
            this.distance = distance;
            this.routes = new HashMap<>();
            this.arrivalTimes = new HashMap<>();
        }

        public String getStopName() {
            return stopName;
        }

        public double getStopLat() {
            return stopLat;
        }

        public double getStopLon() {
            return stopLon;
        }

        public String getDistance() {
            return distance;
        }

        public Map<String, Map<LatLng, String>> getRoutes() {
            return routes;
        }

        // 將路線添加到站牌
        public void addRoute(String routeName, Map<LatLng, String> destinationStops) {
            if (this.routes.containsKey(routeName)) {
                Map<LatLng, String> existingStops = this.routes.get(routeName);
                existingStops.putAll(destinationStops); // 合併相同路線的目的地站牌
            } else {
                this.routes.put(routeName, destinationStops); // 新增路線到站牌
            }
        }

        public Map<String, String> getArrivalTimes() {
            return arrivalTimes;
        }

        public void setArrivalTimes(Map<String, String> arrivalTimes) {
            this.arrivalTimes = arrivalTimes;
        }

        @Override
        public String toString() {
            return "BusStation{" +
                    "stopName='" + stopName + '\'' +
                    ", stopLat=" + stopLat +
                    ", stopLon=" + stopLon +
                    ", distance='" + distance + '\'' +
                    ", routes=" + routes +
                    ", arrivalTimes=" + arrivalTimes +
                    '}';
        }

        // 經緯度類別，用來表示站牌位置
        public static class LatLng {
            private final double lat;
            private final double lon;

            public LatLng(double lat, double lon) {
                this.lat = lat;
                this.lon = lon;
            }

            public double getLat() {
                return lat;
            }

            public double getLon() {
                return lon;
            }

            @Override
            public String toString() {
                return "LatLng{" +
                        "lat=" + lat +
                        ", lon=" + lon +
                        '}';
            }
        }
    }
}
