package com.example.map_clock_api34.BusAdvice;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.example.map_clock_api34.R;
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

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class BusStationFinderHelper {

    //對TDX抓取公車站牌的URL前綴
    private static final String BASE_URL = "https://tdx.transportdata.tw/api/basic/v2/Bus/Stop/City/";
    //TDX抓取公車路線的URL前綴
    private static final String STOP_OF_ROUTE_URL = "https://tdx.transportdata.tw/api/basic/v2/Bus/StopOfRoute/City/";

    private Context context;
    private View overlayView;

    private StringBuilder nearbyStationSuggestions;
    private StringBuilder destinationStationSuggestions;
    private StringBuilder routeSuggestions;

    private SharedViewModel sharedViewModel;

    private OkHttpClient client;
    private AuthHelper authHelper;
    private Handler mainHandler;
    private GoogleDistanceHelper googleDistanceHelper;

    //建構子
    public BusStationFinderHelper(Context context, SharedViewModel sharedViewModel) {
        this.context = context;
        this.sharedViewModel = sharedViewModel;
        this.client = new OkHttpClient();
        this.authHelper = new AuthHelper(context);
        this.mainHandler = new Handler(Looper.getMainLooper());
        this.googleDistanceHelper = new GoogleDistanceHelper(context);
    }

    //外面呼叫找公車的方法
    public void findNearbyStations(View view) {
        //拿TDX的TOKEN
        authHelper.getAccessToken(new AuthHelper.AuthCallback() {
            @Override
            public void onSuccess(String accessToken) {

                //初始化儲存使用者附近的公車站牌
                nearbyStationSuggestions = new StringBuilder();
                //初始化目的地附近的公車站牌
                destinationStationSuggestions = new StringBuilder();

                //PopupWindow的前綴
                routeSuggestions = new StringBuilder("可到達目的地的公車路線:\n");
                try {
                    //呼叫內部找公車站牌的方法
                    findNearbyBusStops(view, accessToken);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }

            //TOKEN獲取失敗會到這裡
            @Override
            public void onFailure(String errorMessage) {
                Log.e("BusStationFinderHelper", "Auth error: " + errorMessage);
                if (context != null) {
                    mainHandler.post(() -> Toast.makeText(context, "無法獲取授權", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    //內部的找公車站牌方法
    private void findNearbyBusStops(View view, String accessToken) throws UnsupportedEncodingException {

        //抓使用者的位置
        double currentLat = sharedViewModel.getNowLantitude();
        double currentLon = sharedViewModel.getNowLontitude();

        //抓第一個目的地的經緯度
        double destLat = sharedViewModel.getLatitude(0);
        double destLon = sharedViewModel.getLongitude(0);

        //經緯度偏移量，是用經緯度的度做偏移，大約200公尺。
        double latDiff = 0.0020;
        double lonDiff = 0.0020;

        //抓出使用者附近200公尺的經緯度範圍
        double minLat = currentLat - latDiff;
        double maxLat = currentLat + latDiff;
        double minLon = currentLon - lonDiff;
        double maxLon = currentLon + lonDiff;

        //抓出目的地附近200公尺的經緯度範圍
        double destMinLat = destLat - latDiff;
        double destMaxLat = destLat + latDiff;
        double destMinLon = destLon - lonDiff;
        double destMaxLon = destLon + lonDiff;

        //程式名字
        String cityName = "NewTaipei";

        //抓取TDX公車站牌的URL再加上過濾條件(經緯度範圍)
        String userStopsUrl = BASE_URL + cityName + "?$filter=( " +
                "StopPosition/PositionLat ge " + minLat + " and StopPosition/PositionLat le " + maxLat + " and " +
                "StopPosition/PositionLon ge " + minLon + " and StopPosition/PositionLon le " + maxLon + "" +
                ") or ( " +
                "StopPosition/PositionLat ge " + destMinLat + " and StopPosition/PositionLat le " + destMaxLat + " and " +
                "StopPosition/PositionLon ge " + destMinLon + " and StopPosition/PositionLon le " + destMaxLon + "" +
                ")&$format=JSON";

        Log.d("BusStationFinderHelper", "User Stops URL: " + userStopsUrl);

        Request userStopsRequest = new Request.Builder()
                .url(userStopsUrl)
                .addHeader("Authorization", "Bearer " + accessToken)
                .build();

        //網路錯誤會跳這裡
        client.newCall(userStopsRequest).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("BusStationFinderHelper", "Network error: " + e.getMessage());
                if (context != null) {
                    mainHandler.post(() -> Toast.makeText(context, "網路錯誤", Toast.LENGTH_SHORT).show());
                }
            }

            //TDX的回應
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                //抓取失敗會跳這裡
                if (!response.isSuccessful()) {
                    Log.e("BusStationFinderHelper", "Request failed: " + response.message() + ", Code: " + response.code());
                    Log.e("BusStationFinderHelper", "Response Body: " + response.body().string());
                    if (context != null) {
                        mainHandler.post(() -> Toast.makeText(context, "無法獲取公車站牌資訊", Toast.LENGTH_SHORT).show());
                    }
                    return;
                }

                try {
                    String userStopsResponse = response.body().string();
                    Log.d("BusStationFinderHelper", "User Stops Response: " + userStopsResponse);

                    //第一次過濾附近和目的地附近的站牌
                    Map<String, String> firstCurrentMap = parseStops(userStopsResponse, currentLat, currentLon);
                    Map<String, String> firstDesMap = parseStops(userStopsResponse, destLat, destLon);

                    Map<String, String> secondNearByStop = new HashMap<>();
                    Map<String, String> secondDesStop = new HashMap<>();

                    //因為會有異步運行的問題所以這等待CallBack才會運行，因為呼叫外面的API會比較久，所以才會有這問題
                    Runnable onCompletion = () -> {
                        nearbyStationSuggestions.append("附近站牌:\n");
                        for (Map.Entry<String, String> entry : secondNearByStop.entrySet()) {
                            nearbyStationSuggestions.append(entry.getKey()).append("\n");
                        }

                        destinationStationSuggestions.append("目的地站牌:\n");
                        for (Map.Entry<String, String> entry : secondDesStop.entrySet()) {
                            destinationStationSuggestions.append(entry.getKey()).append("\n");
                        }

                        mainHandler.post(() -> showPopup(view));
                    };

                    filterNearbyStops(currentLat, currentLon, firstCurrentMap, secondNearByStop, () ->
                            filterNearbyStops(destLat, destLon, firstDesMap, secondDesStop, onCompletion));

                } catch (Exception e) {
                    Log.e("BusStationFinderHelper", "Parsing error: " + e.getMessage());
                    if (context != null) {
                        mainHandler.post(() -> Toast.makeText(context, "無法解析公車站牌資訊", Toast.LENGTH_SHORT).show());
                    }
                }
            }
        });
    }

    //從GOOGLE找出附近站牌步行500公尺內能到的站
    private void filterNearbyStops(double originLat, double originLon, Map<String, String> firstMap, Map<String, String> resultMap, Runnable callback) {
        List<String> destinations = new ArrayList<>(firstMap.values());
        googleDistanceHelper.getWalkingDistances(originLat, originLon, destinations, new GoogleDistanceHelper.DistanceCallback() {
            @Override
            public void onSuccess(String jsonResponse) {
                try {
                    Map<String, String> stopDistances = parseDistances(jsonResponse, firstMap);

                    for (Map.Entry<String, String> entry : stopDistances.entrySet()) {
                        if (Integer.valueOf(entry.getValue()) <= 500) {
                            resultMap.put(entry.getKey(), firstMap.get(entry.getKey()));
                        }
                    }

                    callback.run();
                } catch (Exception e) {
                    Log.e("BusStationFinderHelper", "Error parsing distances: " + e.getMessage());
                    if (context != null) {
                        mainHandler.post(() -> Toast.makeText(context, "解析距離資訊時出錯", Toast.LENGTH_SHORT).show());
                    }
                }
            }

            @Override
            public void onFailure(String errorMessage) {
                Log.e("BusStationFinderHelper", "Distance matrix error: " + errorMessage);
                if (context != null) {
                    mainHandler.post(() -> Toast.makeText(context, "無法獲取距離資訊", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    //距離抓取方法
    private Map<String, String> parseDistances(String jsonResponse, Map<String, String> nearByStop) throws Exception {
        Map<String, String> stopDistances = new HashMap<>();

        JSONObject jsonObject = new JSONObject(jsonResponse);
        JSONArray rows = jsonObject.getJSONArray("rows");
        JSONArray elements = rows.getJSONObject(0).getJSONArray("elements");

        int i = -1;
        for (Map.Entry<String, String> entry : nearByStop.entrySet()) {
            i++;
            while (i < elements.length()) {
                JSONObject element = elements.getJSONObject(i);
                String status = element.getString("status");

                if ("OK".equals(status)) {
                    String distanceValue = element.getJSONObject("distance").getString("value");
                    stopDistances.put(entry.getKey(), distanceValue);
                    break;
                }
            }
        }
        Log.d("BusStationFinderHelper", "stopDistances " + stopDistances);

        return stopDistances;
    }

    private void findRoutes(View view, String accessToken, String cityName, Set<String> nearbyStops, Set<String> destinationStops) {
        if (nearbyStops.isEmpty()) {
            Log.e("BusStationFinderHelper", "No nearby stops found within 500 meters.");
            return;
        }

        Set<String> allStops = new HashSet<>(nearbyStops);
        allStops.addAll(destinationStops);

        StringBuilder stopFilter = new StringBuilder();
        for (String stop : allStops) {
            if (stopFilter.length() > 0) {
                stopFilter.append(" or ");
            }
            stopFilter.append("Stops/any(s: s/StopName/Zh_tw eq '").append(stop).append("')");
        }

        String routeUrl = STOP_OF_ROUTE_URL + cityName + "?$filter=" + stopFilter.toString() + "&$format=JSON";
        Log.d("BusStationFinderHelper", "Route URL: " + routeUrl);

        Request routeRequest = new Request.Builder()
                .url(routeUrl)
                .addHeader("Authorization", "Bearer " + accessToken)
                .build();

        client.newCall(routeRequest).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("BusStationFinderHelper", "Network error: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    Log.e("BusStationFinderHelper", "Request failed: " + response.message() + ", Code: " + response.code());
                    Log.e("BusStationFinderHelper", "Response Body: " + response.body().string());
                    return;
                }

                try {
                    String routeResponse = response.body().string();
                    Log.d("BusStationFinderHelper", "Route Response: " + routeResponse);

                    JSONArray jsonArray = new JSONArray(routeResponse);
                    Set<String> routesFound = new HashSet<>();

                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject routeObj = jsonArray.getJSONObject(i);
                        String routeName = routeObj.getJSONObject("RouteName").getString("Zh_tw");
                        JSONArray stopsArray = routeObj.getJSONArray("Stops");
                        Set<String> routeStops = new HashSet<>();
                        for (int j = 0; j < stopsArray.length(); j++) {
                            String stopName = stopsArray.getJSONObject(j).getJSONObject("StopName").getString("Zh_tw");
                            routeStops.add(stopName);
                        }

                        if (!nearbyStops.isEmpty() && !destinationStops.isEmpty()) {
                            for (String nearbyStop : nearbyStops) {
                                if (routeStops.contains(nearbyStop)) {
                                    for (String destinationStop : destinationStops) {
                                        if (routeStops.contains(destinationStop)) {
                                            routesFound.add(routeName);
                                            routeSuggestions.append(routeName).append("\n");
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                    }
                    mainHandler.post(() -> showPopup(view));
                } catch (Exception e) {
                    Log.e("BusStationFinderHelper", "Parsing error: " + e.getMessage());
                }
            }
        });
    }

    private Map<String, String> parseStops(String jsonResponse, double Lat, double Lon) throws Exception {
        JSONArray jsonArray = new JSONArray(jsonResponse);
        Map<String, String> firstMap = new HashMap<>();

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject stopObj = jsonArray.getJSONObject(i);
            String stopName = stopObj.getJSONObject("StopName").getString("Zh_tw");
            double stopLat = stopObj.getJSONObject("StopPosition").getDouble("PositionLat");
            double stopLon = stopObj.getJSONObject("StopPosition").getDouble("PositionLon");
            String latLon = stopLat + "," + stopLon;

            if (isNearby(stopLat, stopLon, Lat, Lon)) {
                firstMap.put(stopName, latLon);
            }
        }

        return firstMap;
    }

    //先過濾第一次經緯度附近的站牌
    private boolean isNearby(double stopLat, double stopLon, double targetLat, double targetLon) {
        double latDiff = 0.0020;
        double lonDiff = 0.0020;
        return Math.abs(stopLat - targetLat) <= latDiff && Math.abs(stopLon - targetLon) <= lonDiff;
    }

    private void showPopup(View view) {
        View popupView = LayoutInflater.from(context).inflate(R.layout.popupwindow_station, null, false);
        PopupWindow popupWindow = new PopupWindow(popupView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        popupWindow.setWidth(700);
        popupWindow.setFocusable(false);
        popupWindow.setOutsideTouchable(false);

        TextView stationInfoTextView = popupView.findViewById(R.id.txtStationNote);

        overlayView = new View(context);
        overlayView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        overlayView.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent));
        overlayView.setClickable(true);
        ((ViewGroup) ((FragmentActivity) context).findViewById(android.R.id.content)).addView(overlayView);

        mainHandler.post(() -> {
            stationInfoTextView.setText(nearbyStationSuggestions.toString() + "\n" + destinationStationSuggestions.toString() + "\n" );
            Button btnCancel = popupView.findViewById(R.id.PopupYes);
            btnCancel.setOnClickListener(v -> {
                popupWindow.dismiss();
                removeOverlayView();
            });
            popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);
        });
    }

    private void removeOverlayView() {
        if (overlayView != null && overlayView.getParent() != null) {
            ((ViewGroup) overlayView.getParent()).removeView(overlayView);
            overlayView = null;
        }
    }

    private static class BusStation {
        private final String stopName;
        private final double stopLat;
        private final double stopLon;
        private final String stopID;

        public BusStation(String stopName, double stopLat, double stopLon, String stopID) {
            this.stopName = stopName;
            this.stopLat = stopLat;
            this.stopLon = stopLon;
            this.stopID = stopID;
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

        public String getStopID() {
            return stopID;
        }

        @Override
        public String toString() {
            return "BusStation{" +
                    "stopName='" + stopName + '\'' +
                    ", stopLat=" + stopLat +
                    ", stopLon=" + stopLon +
                    ", stopID='" + stopID + '\'' +
                    '}';
        }
    }
}
