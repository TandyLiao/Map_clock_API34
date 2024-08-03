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
    List<BusStation> secondNearByStop = new ArrayList<>();
    List<BusStation> secondDesStop = new ArrayList<>();

    // Handler for updating arrival times
    private Handler updateHandler;
    private Runnable updateRunnable;
    private static final int UPDATE_INTERVAL = 60000; // 1 minute


    private BusStationFinderCallback callback;

    //建構子
    public BusStationFinderHelper(Context context, SharedViewModel sharedViewModel, BusStationFinderCallback callback) {
        this.context = context;
        this.sharedViewModel = sharedViewModel;
        this.client = new OkHttpClient();
        this.authHelper = new AuthHelper(context);
        this.mainHandler = new Handler(Looper.getMainLooper());
        this.googleDistanceHelper = new GoogleDistanceHelper(context);
        this.callback = callback;

        this.updateHandler = new Handler(Looper.getMainLooper());
        this.updateRunnable = new Runnable() {
            @Override
            public void run() {
                refreshArrivalTimes();
                updateHandler.postDelayed(this, UPDATE_INTERVAL);
            }
        };
    }

    //外面可呼叫過濾站牌的方法
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

    //第一次過濾找附近和目的地站牌
    private void findNearbyBusStops(View view, String accessToken) throws UnsupportedEncodingException {

        double currentLat = sharedViewModel.getNowLantitude();
        double currentLon = sharedViewModel.getNowLontitude();
        double destLat = sharedViewModel.getLatitude(0);
        double destLon = sharedViewModel.getLongitude(0);

        double latDiff = 0.0025;
        double lonDiff = 0.0025;

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

                    //後執行，先執行的程式在下面
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
                        findRoutes(view, accessToken, cityName, secondNearByStop, secondDesStop);

                    };
                    //先執行
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
    //第一次過濾站牌內分成附近和目的地站牌的方法
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
        double latDiff = 0.0025;
        double lonDiff = 0.0025;
        return Math.abs(stopLat - targetLat) <= latDiff && Math.abs(stopLon - targetLon) <= lonDiff;
    }
    //第二次過濾站牌
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

    //過濾500公尺內步行能到的站牌
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

    private void findRoutes(View view, String accessToken, String cityName, List<BusStation> nearbyStops, List<BusStation> destinationStops) {
        // 一次性获取所有这些站点的路线
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

                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject routeObj = jsonArray.getJSONObject(i);
                        String routeName = routeObj.getJSONObject("RouteName").getString("Zh_tw");
                        JSONArray stopsArray = routeObj.getJSONArray("Stops");
                        Map<BusStation.LatLng, String> routeStops = new HashMap<>();

                        for (int j = 0; j < stopsArray.length(); j++) {
                            JSONObject stopObj = stopsArray.getJSONObject(j);
                            String stopName = stopObj.getJSONObject("StopName").getString("Zh_tw");
                            double stopLat = stopObj.getJSONObject("StopPosition").getDouble("PositionLat");
                            double stopLon = stopObj.getJSONObject("StopPosition").getDouble("PositionLon");
                            routeStops.put(new BusStation.LatLng(stopLat, stopLon), stopName);
                        }

                        for (BusStation station : nearbyStops) {
                            if (isNearbyStop(station, routeStops)) {
                                Map<BusStation.LatLng, String> destinationStopsMap = new HashMap<>();
                                for (BusStation destinationStation : destinationStops) {
                                    if (isNearbyStop(destinationStation, routeStops)) {
                                        destinationStopsMap.put(new BusStation.LatLng(destinationStation.getStopLat(), destinationStation.getStopLon()), destinationStation.getStopName());
                                    }
                                }
                                station.addRoute(routeName, destinationStopsMap);
                            }
                        }
                    }
                    mainHandler.post(() -> {
                        // 确认所有 BusStation 的信息
                        for (BusStation station : nearbyStops) {
                            Log.d("BusStationFinderHelper", "BusStation: " + station.toString());
                        }
                        Toast.makeText(context, "路線已更新", Toast.LENGTH_LONG).show();
                    });
                    findArrivalTimes(accessToken, cityName, nearbyStops);
                } catch (Exception e) {
                    Log.e("BusStationFinderHelper", "Parsing error: " + e.getMessage());
                }
            }
        });
    }

    private boolean isNearbyStop(BusStation station, Map<BusStation.LatLng, String> routeStops) {
        double epsilon = 0.00001; // 允许的误差范围
        for (Map.Entry<BusStation.LatLng, String> entry : routeStops.entrySet()) {
            BusStation.LatLng latLng = entry.getKey();
            if (Math.abs(station.getStopLat() - latLng.getLat()) < epsilon && Math.abs(station.getStopLon() - latLng.getLon()) < epsilon) {
                return true;
            }
        }
        return false;
    }

    //負責回傳結果
    public interface BusStationFinderCallback {
        void onBusStationsFound(List<BusStation> nearbyStops, List<BusStation> destinationStops);
    }

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

        client.newCall(arrivalTimeRequest).enqueue(new Callback() {
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
                        mainHandler.post(() -> Toast.makeText(context, "无法获取到站时间", Toast.LENGTH_SHORT).show());
                    }
                    return;
                }

                try {
                    String arrivalTimeResponse = response.body().string();
                    Log.d("BusStationFinderHelper", "Arrival Time Response: " + arrivalTimeResponse);

                    // 解析到站时间
                    parseArrivalTimes(arrivalTimeResponse);

                } catch (Exception e) {
                    Log.e("BusStationFinderHelper", "Parsing error: " + e.getMessage());
                    if (context != null) {
                        mainHandler.post(() -> Toast.makeText(context, "无法解析到站时间", Toast.LENGTH_SHORT).show());
                    }
                }
            }
        });

        // Start the periodic update
        updateHandler.postDelayed(updateRunnable, UPDATE_INTERVAL);
    }

    private void refreshArrivalTimes() {
        authHelper.getAccessToken(new AuthHelper.AuthCallback() {
            @Override
            public void onSuccess(String accessToken) {
                findArrivalTimes(accessToken, "NewTaipei", secondNearByStop); // Assuming the city is NewTaipei
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



    private void parseArrivalTimes(String jsonResponse) throws Exception {
        JSONArray jsonArray = new JSONArray(jsonResponse);
        Map<String, String> arrivalTimes = new HashMap<>();

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject arrivalObj = jsonArray.getJSONObject(i);
            String routeName = arrivalObj.getJSONObject("RouteName").getString("Zh_tw");
            String stopName = arrivalObj.getJSONObject("StopName").getString("Zh_tw");
            int estimateTime = arrivalObj.optInt("EstimateTime", -1);

            String key = routeName + "-" + stopName;
            String value = estimateTime == -1 ? "未發車" : (estimateTime / 60) + " 分鐘";

            arrivalTimes.put(key, value);
        }

        // 将抵达时间存储到相应的站点
        for (BusStation station : secondNearByStop) {
            Map<String, String> stationArrivalTimes = new HashMap<>();
            for (String routeName : station.getRoutes().keySet()) {
                String key = routeName + "-" + station.getStopName();
                if (arrivalTimes.containsKey(key)) {
                    stationArrivalTimes.put(routeName, arrivalTimes.get(key));
                }
            }
            station.setArrivalTimes(stationArrivalTimes);
        }

        mainHandler.post(() -> {
            callback.onBusStationsFound(secondNearByStop, secondDesStop);
        });
    }


    public static class BusStation {
        private final String stopName;
        private final double stopLat;
        private final double stopLon;
        private String distance;
        private Map<String, Map<LatLng, String>> routes;
        private Map<String, String> arrivalTimes; // 新增字段以存储到站时间

        public BusStation(String stopName, double stopLat, double stopLon) {
            this.stopName = stopName;
            this.stopLat = stopLat;
            this.stopLon = stopLon;
            this.routes = new HashMap<>();
            this.arrivalTimes = new HashMap<>();
        }

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

        public void addRoute(String routeName, Map<LatLng, String> destinationStops) {
            if (this.routes.containsKey(routeName)) {
                Map<LatLng, String> existingStops = this.routes.get(routeName);
                existingStops.putAll(destinationStops);
            } else {
                this.routes.put(routeName, destinationStops);
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
