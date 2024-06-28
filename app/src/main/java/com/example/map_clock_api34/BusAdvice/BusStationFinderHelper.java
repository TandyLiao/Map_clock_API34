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

import com.example.map_clock_api34.Distance;
import com.example.map_clock_api34.R;
import com.example.map_clock_api34.SharedViewModel;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class BusStationFinderHelper {

    private static final String BASE_URL = "https://tdx.transportdata.tw/api/basic/v2/Bus/Stop/City/";

    private Context context;
    private View overlayView;
    private StringBuilder stationSuggestions;
    private SharedViewModel sharedViewModel;
    private OkHttpClient client;
    private AuthHelper authHelper;

    public BusStationFinderHelper(Context context, SharedViewModel sharedViewModel) {
        this.context = context;
        this.sharedViewModel = sharedViewModel;
        this.client = new OkHttpClient();
        this.authHelper = new AuthHelper();
    }

    public void findNearbyStations(View view) {
        authHelper.getAccessToken(new AuthHelper.AuthCallback() {
            @Override
            public void onSuccess(String accessToken) {
                requestBusStations(view, accessToken);
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

    private void requestBusStations(View view, String accessToken) {
        new Thread(() -> {
            stationSuggestions = new StringBuilder();
            String cityName = "NewTaipei"; // 使用被接受的城市名稱
            String districtName = "淡水"; // 具体区域名称
            String url = BASE_URL + cityName + "?$top=30&$filter=contains(StopName/Zh_tw,'" + districtName + "')&$format=JSON";
            Log.d("BusStationFinderHelper", "Request URL: " + url); // 打印URL以進行調試
            Request request = new Request.Builder()
                    .url(url)
                    .addHeader("Authorization", "Bearer " + accessToken)
                    .build();
            Log.d("BusStationFinderHelper", "Authorization Header: Bearer " + accessToken); // 打印Header以進行調試

            client.newCall(request).enqueue(new Callback() {
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
                        String errorBody = response.body().string();
                        Log.e("BusStationFinderHelper", "Request failed: " + response.message());
                        Log.e("BusStationFinderHelper", "Response: " + errorBody); // 打印響應以進行調試
                        if (context != null) {
                            ((FragmentActivity) context).runOnUiThread(() -> Toast.makeText(context, "無法獲取公車站牌資訊", Toast.LENGTH_SHORT).show());
                        }
                        return;
                    }

                    try {
                        String jsonResponse = response.body().string();
                        Log.d("BusStationFinderHelper", "Response: " + jsonResponse); // 打印響應內容以進行調試
                        parseBusStations(jsonResponse, view);
                    } catch (Exception e) {
                        Log.e("BusStationFinderHelper", "Parsing error: " + e.getMessage());
                        if (context != null) {
                            ((FragmentActivity) context).runOnUiThread(() -> Toast.makeText(context, "無法解析公車站牌資訊", Toast.LENGTH_SHORT).show());
                        }
                    }
                }
            });
        }).start();
    }

    private void parseBusStations(String jsonResponse, View view) throws Exception {
        Log.d("BusStationFinderHelper", "Parsing response"); // 調試信息
        JSONArray stopsArray = new JSONArray(jsonResponse);

        if (stopsArray.length() == 0) {
            showToast("找不到該區域的公交站牌");
            return;
        }

        List<BusStation> nearbyCurrentStations = new ArrayList<>();
        List<BusStation> nearbyDestinationStations = new ArrayList<>();

        double currentLat = sharedViewModel.getNowLantitude();
        double currentLon = sharedViewModel.getNowLontitude();
        double destLat = sharedViewModel.getLatitude(0);
        double destLon = sharedViewModel.getLongitude(0);

        for (int i = 0; i < stopsArray.length(); i++) {
            JSONObject station = stopsArray.getJSONObject(i);
            if (!station.has("StopPosition")) {
                Log.e("BusStationFinderHelper", "No StopPosition for station: " + station.getJSONObject("StopName").getString("Zh_tw"));
                continue;
            }

            JSONObject stationPosition = station.getJSONObject("StopPosition");
            double stationLat = stationPosition.getDouble("PositionLat");
            double stationLon = stationPosition.getDouble("PositionLon");

            double distanceToCurrent = Distance.getDistanceBetweenPointsNew(currentLat, currentLon, stationLat, stationLon)/1000;


            if (distanceToCurrent <= 0.5) {
                Log.e("BusStationFinderHelper", "距離: " +  stationLat+":"+stationLon);
                nearbyCurrentStations.add(new BusStation(station.getJSONObject("StopName").getString("Zh_tw"), stationLat, stationLon));
            }

            double distanceToDestination = Distance.getDistanceBetweenPointsNew(destLat, destLon, stationLat, stationLon);
            if (distanceToDestination <= 0.5) {
                nearbyDestinationStations.add(new BusStation(station.getJSONObject("StopName").getString("Zh_tw"), stationLat, stationLon));
            }
        }

        stationSuggestions.append("附近的公車站牌：\n\n");

        if (!nearbyCurrentStations.isEmpty()) {
            stationSuggestions.append("當前位置附近：\n");
            for (BusStation station : nearbyCurrentStations) {
                stationSuggestions.append(station.getStationName()).append("\n");
            }
            stationSuggestions.append("\n");
        }

        if (!nearbyDestinationStations.isEmpty()) {
            stationSuggestions.append("目的地附近：\n");
            for (BusStation station : nearbyDestinationStations) {
                stationSuggestions.append(station.getStationName()).append("\n");
            }
        }

        if (context instanceof FragmentActivity) {
            ((FragmentActivity) context).runOnUiThread(() -> showStationsPopup(view));
        }
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
            stationInfoTextView.setText(stationSuggestions);
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
        private final String stationName;
        private final double stationLat;
        private final double stationLon;

        public BusStation(String stationName, double stationLat, double stationLon) {
            this.stationName = stationName;
            this.stationLat = stationLat;
            this.stationLon = stationLon;
        }

        public String getStationName() {
            return stationName;
        }

        public double getStationLat() {
            return stationLat;
        }

        public double getStationLon() {
            return stationLon;
        }
    }
}

