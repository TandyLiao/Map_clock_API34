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

    private BusStationFinderCallback callback;

    public BusStationFinderHelper(Context context, SharedViewModel sharedViewModel, BusStationFinderCallback callback) {
        this.context = context;
        this.sharedViewModel = sharedViewModel;
        this.client = new OkHttpClient();
        this.authHelper = new AuthHelper(context);
        this.mainHandler = new Handler(Looper.getMainLooper());
        this.googleDistanceHelper = new GoogleDistanceHelper(context);
        this.callback = callback;
    }

    public void findNearbyStations(View view) {
        authHelper.getAccessToken(new AuthHelper.AuthCallback() {
            @Override
            public void onSuccess(String accessToken) {
                nearbyStationSuggestions = new StringBuilder();
                destinationStationSuggestions = new StringBuilder();
                routeSuggestions = new StringBuilder("可到达目的地的公车路线:\n");
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
                    mainHandler.post(() -> Toast.makeText(context, "无法获取授权", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    private void findNearbyBusStops(View view, String accessToken) throws UnsupportedEncodingException {

        double currentLat = sharedViewModel.getNowLantitude();
        double currentLon = sharedViewModel.getNowLontitude();
        double destLat = sharedViewModel.getLatitude(0);
        double destLon = sharedViewModel.getLongitude(0);

        double latDiff = 0.0022;
        double lonDiff = 0.0022;

        double minLat = currentLat - latDiff;
        double maxLat = currentLat + latDiff;
        double minLon = currentLon - lonDiff;
        double maxLon = currentLon + lonDiff;
        double destMinLat = destLat - latDiff;
        double destMaxLat = destLat + latDiff;
        double destMinLon = destLon - lonDiff;
        double destMaxLon = destLon + lonDiff;

        String cityName = "NewTaipei";

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
                    mainHandler.post(() -> Toast.makeText(context, "网络错误", Toast.LENGTH_SHORT).show());
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    Log.e("BusStationFinderHelper", "Request failed: " + response.message() + ", Code: " + response.code());
                    Log.e("BusStationFinderHelper", "Response Body: " + response.body().string());
                    if (context != null) {
                        mainHandler.post(() -> Toast.makeText(context, "无法获取公车站牌资讯", Toast.LENGTH_SHORT).show());
                    }
                    return;
                }

                try {
                    String userStopsResponse = response.body().string();
                    Log.d("BusStationFinderHelper", "User Stops Response: " + userStopsResponse);

                    List<BusStation> firstCurrentList = parseStops(userStopsResponse, currentLat, currentLon);
                    List<BusStation> firstDesList = parseStops(userStopsResponse, destLat, destLon);

                    List<BusStation> secondNearByStop = new ArrayList<>();
                    List<BusStation> secondDesStop = new ArrayList<>();

                    Runnable onCompletion = () -> {
                        nearbyStationSuggestions.setLength(0);
                        destinationStationSuggestions.setLength(0);

                        nearbyStationSuggestions.append("附近站牌:\n");
                        for (BusStation station : secondNearByStop) {
                            nearbyStationSuggestions.append(station.getStopName()).append("\n");
                        }

                        destinationStationSuggestions.append("目的地站牌:\n");
                        for (BusStation station : secondDesStop) {
                            destinationStationSuggestions.append(station.getStopName()).append("\n");
                        }

                        mainHandler.post(() -> {
                            showPopup(view);
                            callback.onBusStationsFound(secondNearByStop, secondDesStop);
                        });
                    };

                    filterNearbyStops(currentLat, currentLon, firstCurrentList, secondNearByStop, () ->
                            filterNearbyStops(destLat, destLon, firstDesList, secondDesStop, onCompletion));

                } catch (Exception e) {
                    Log.e("BusStationFinderHelper", "Parsing error: " + e.getMessage());
                    if (context != null) {
                        mainHandler.post(() -> Toast.makeText(context, "无法解析公车站牌资讯", Toast.LENGTH_SHORT).show());
                    }
                }
            }
        });
    }

    private void filterNearbyStops(double originLat, double originLon, List<BusStation> firstList, List<BusStation> resultList, Runnable callback) {
        List<String> destinations = new ArrayList<>();
        for (BusStation station : firstList) {
            destinations.add(station.getStopLat() + "," + station.getStopLon());
        }

        googleDistanceHelper.getWalkingDistances(originLat, originLon, destinations, new GoogleDistanceHelper.DistanceCallback() {
            @Override
            public void onSuccess(String jsonResponse) {
                try {
                    List<BusStation> stopDistances = parseDistances(jsonResponse, firstList);
                    Set<String> seenStops = new HashSet<>();

                    for (BusStation station : stopDistances) {
                        String key = station.getStopName() + "," + station.getStopLat() + "," + station.getStopLon();
                        if (Integer.valueOf(station.getDistance()) <= 500 && !seenStops.contains(key)) {
                            resultList.add(station);
                            seenStops.add(key);
                        }
                    }

                    callback.run();
                } catch (Exception e) {
                    Log.e("BusStationFinderHelper", "Error parsing distances: " + e.getMessage());
                    if (context != null) {
                        mainHandler.post(() -> Toast.makeText(context, "解析距离资讯时出错", Toast.LENGTH_SHORT).show());
                    }
                }
            }

            @Override
            public void onFailure(String errorMessage) {
                Log.e("BusStationFinderHelper", "Distance matrix error: " + errorMessage);
                if (context != null) {
                    mainHandler.post(() -> Toast.makeText(context, "无法获取距离资讯", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    private List<BusStation> parseDistances(String jsonResponse, List<BusStation> nearByStop) throws Exception {
        List<BusStation> stopDistances = new ArrayList<>();

        JSONObject jsonObject = new JSONObject(jsonResponse);
        JSONArray rows = jsonObject.getJSONArray("rows");

        if (rows.length() == 0) {
            throw new Exception("No rows in distance matrix response");
        }

        JSONArray elements = rows.getJSONObject(0).getJSONArray("elements");

        if (elements.length() != nearByStop.size()) {
            throw new Exception("Mismatch between number of stops and elements in distance matrix response");
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

    private List<BusStation> parseStops(String jsonResponse, double Lat, double Lon) throws Exception {
        JSONArray jsonArray = new JSONArray(jsonResponse);
        List<BusStation> stationList = new ArrayList<>();
        Set<String> seenStops = new HashSet<>();

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject stopObj = jsonArray.getJSONObject(i);
            String stopName = stopObj.getJSONObject("StopName").getString("Zh_tw");
            double stopLat = stopObj.getJSONObject("StopPosition").getDouble("PositionLat");
            double stopLon = stopObj.getJSONObject("StopPosition").getDouble("PositionLon");

            String key = stopName + "," + stopLat + "," + stopLon;

            if (isNearby(stopLat, stopLon, Lat, Lon) && !seenStops.contains(key)) {
                stationList.add(new BusStation(stopName, stopLat, stopLon));
                seenStops.add(key);
            }
        }

        return stationList;
    }

    private boolean isNearby(double stopLat, double stopLon, double targetLat, double targetLon) {
        double latDiff = 0.0022;
        double lonDiff = 0.0022;
        return Math.abs(stopLat - targetLat) <= latDiff && Math.abs(stopLon - targetLon) <= lonDiff;
    }

    public interface BusStationFinderCallback {
        void onBusStationsFound(List<BusStation> nearbyStops, List<BusStation> destinationStops);
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

    public static class BusStation {
        private final String stopName;
        private final double stopLat;
        private final double stopLon;
        private String distance;

        public BusStation(String stopName, double stopLat, double stopLon) {
            this.stopName = stopName;
            this.stopLat = stopLat;
            this.stopLon = stopLon;
        }

        public BusStation(String stopName, double stopLat, double stopLon, String distance) {
            this.stopName = stopName;
            this.stopLat = stopLat;
            this.stopLon = stopLon;
            this.distance = distance;
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

        @Override
        public String toString() {
            return "BusStation{" +
                    "stopName='" + stopName + '\'' +
                    ", stopLat=" + stopLat +
                    ", stopLon=" + stopLon +
                    ", distance='" + distance + '\'' +
                    '}';
        }
    }
}
