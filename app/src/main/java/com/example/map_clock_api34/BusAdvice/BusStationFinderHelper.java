package com.example.map_clock_api34.BusAdvice;

import android.content.Context;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class BusStationFinderHelper {

    private static final String BASE_URL = "https://tdx.transportdata.tw/api/basic/v2/Bus/Stop/City/";

    private Context context;
    private View overlayView;
    private StringBuilder nearbyStationSuggestions;
    private StringBuilder destinationStationSuggestions;
    private SharedViewModel sharedViewModel;
    private OkHttpClient client;
    private AuthHelper authHelper;

    public BusStationFinderHelper(Context context, SharedViewModel sharedViewModel) {
        this.context = context;
        this.sharedViewModel = sharedViewModel;
        this.client = new OkHttpClient();
        this.authHelper = new AuthHelper(context); // 傳遞context給AuthHelper
    }

    public void findNearbyStations(View view) {
        authHelper.getAccessToken(new AuthHelper.AuthCallback() {
            @Override
            public void onSuccess(String accessToken) {
                nearbyStationSuggestions = new StringBuilder(); // 初始化變數
                destinationStationSuggestions = new StringBuilder(); // 初始化變數
                try {
                    findNearbyBusStops(view, accessToken);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(String errorMessage) {
                Log.e("BusStationFinderHelper", "Auth error: " + errorMessage);
                if (context != null) {
                    ((FragmentActivity) context).runOnUiThread(() -> Toast.makeText(context, "無法獲取授權", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    private void findNearbyBusStops(View view, String accessToken) throws UnsupportedEncodingException {
        double currentLat = sharedViewModel.getNowLantitude();
        double currentLon = sharedViewModel.getNowLontitude();
        double destLat = sharedViewModel.getLatitude(0);
        double destLon = sharedViewModel.getLongitude(0);

        // 增加和減少經緯度值來獲取200公尺範圍
        double latDiff = 0.0018; // 200公尺約為0.0018度緯度
        double lonDiff = 0.0018; // 200公尺約為0.0018度經度

        double minLat = currentLat - latDiff;
        double maxLat = currentLat + latDiff;
        double minLon = currentLon - lonDiff;
        double maxLon = currentLon + lonDiff;

        double destMinLat = destLat - latDiff;
        double destMaxLat = destLat + latDiff;
        double destMinLon = destLon - lonDiff;
        double destMaxLon = destLon + lonDiff;

        String cityName = "NewTaipei"; // 使用被接受的城市名稱

        // 構造查詢附近站牌的URL，使用 or 邏輯
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

        client.newCall(userStopsRequest).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("BusStationFinderHelper", "Network error: " + e.getMessage());
                if (context != null) {
                    ((FragmentActivity) context).runOnUiThread(() -> Toast.makeText(context, "網路錯誤", Toast.LENGTH_SHORT).show());
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    Log.e("BusStationFinderHelper", "Request failed: " + response.message() + ", Code: " + response.code());
                    Log.e("BusStationFinderHelper", "Response Body: " + response.body().string());
                    if (context != null) {
                        ((FragmentActivity) context).runOnUiThread(() -> Toast.makeText(context, "無法獲取公車站牌資訊", Toast.LENGTH_SHORT).show());
                    }
                    return;
                }

                try {
                    String userStopsResponse = response.body().string();
                    Log.d("BusStationFinderHelper", "User Stops Response: " + userStopsResponse);
                    List<BusStation> userStops = parseStops(userStopsResponse, currentLat, currentLon, destLat, destLon);
                    nearbyStationSuggestions.append("附近站牌:\n");
                    destinationStationSuggestions.append("目的地站牌:\n");
                    for (BusStation stop : userStops) {
                        if (isNearby(stop, currentLat, currentLon)) {
                            nearbyStationSuggestions.append(stop.getStopName()).append("\n");
                            Log.d("BusStationFinderHelper", "Nearby Stop: " + stop.getStopName() + " (Lat: " + stop.getStopLat() + ", Lon: " + stop.getStopLon() + ")");
                        } else if (isNearby(stop, destLat, destLon)) {
                            destinationStationSuggestions.append(stop.getStopName()).append("\n");
                            Log.d("BusStationFinderHelper", "Destination Stop: " + stop.getStopName() + " (Lat: " + stop.getStopLat() + ", Lon: " + stop.getStopLon() + ")");
                        }
                    }
                    ((FragmentActivity) context).runOnUiThread(() -> showStationsPopup(view));
                } catch (Exception e) {
                    Log.e("BusStationFinderHelper", "Parsing error: " + e.getMessage());
                    if (context != null) {
                        ((FragmentActivity) context).runOnUiThread(() -> Toast.makeText(context, "無法解析公車站牌資訊", Toast.LENGTH_SHORT).show());
                    }
                }
            }
        });
    }

    private List<BusStation> parseStops(String jsonResponse, double currentLat, double currentLon, double destLat, double destLon) throws Exception {
        List<BusStation> stops = new ArrayList<>();
        Set<String> stopNames = new HashSet<>();
        JSONArray jsonArray = new JSONArray(jsonResponse);
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject stopObj = jsonArray.getJSONObject(i);
            String stopName = stopObj.getJSONObject("StopName").getString("Zh_tw");
            if (!stopNames.contains(stopName)) {
                double stopLat = stopObj.getJSONObject("StopPosition").getDouble("PositionLat");
                double stopLon = stopObj.getJSONObject("StopPosition").getDouble("PositionLon");
                String stopID = stopObj.getString("StopID");
                stops.add(new BusStation(stopName, stopLat, stopLon, stopID));
                stopNames.add(stopName);
            }
        }
        return stops;
    }

    private boolean isNearby(BusStation stop, double targetLat, double targetLon) {
        double latDiff = 0.0018; // 200公尺約為0.0018度緯度
        double lonDiff = 0.0018; // 200公尺約為0.0018度經度
        return Math.abs(stop.getStopLat() - targetLat) <= latDiff && Math.abs(stop.getStopLon() - targetLon) <= lonDiff;
    }

    private void showStationsPopup(View view) {
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

        ((FragmentActivity) context).runOnUiThread(() -> {
            stationInfoTextView.setText(nearbyStationSuggestions.toString() + "\n" + destinationStationSuggestions.toString());
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

    private void showToast(String message) {
        if (context instanceof FragmentActivity) {
            ((FragmentActivity) context).runOnUiThread(() -> Toast.makeText(context, message, Toast.LENGTH_SHORT).show());
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
    }
}
