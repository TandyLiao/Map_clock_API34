package com.example.map_clock_api34.MRTStationFinder;

import static com.example.map_clock_api34.Distance.getDistanceBetweenPointsNew;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class FindMRTStationStop {

    private Context context;  // 用來讀取 assets 資料夾中的 JSON 檔案 Used to read JSON files from the assets folder.
    private double userLat;
    private double userLon;
    private StaionRecord staionRecord;

    // 建構子：初始化使用者的經緯度 Constructor: Initialize the user's longitude and latitude
    public FindMRTStationStop(Context context, double userLat, double userLon, StaionRecord staionRecord) {
        this.context = context;
        this.userLat = userLat;
        this.userLon = userLon;
        this.staionRecord = staionRecord;
    }

    /**
     * 從 assets 資料夾讀取捷運站的 JSON 資料。Read the MRT station JSON data from the assets folder.
     */
    private String loadJSONFromAsset() {
        String json;
        try {
            AssetManager assetManager = context.getAssets();
            InputStream is = assetManager.open("MRT_Staion_Position.json");  // 檔案名稱
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            reader.close();
            json = sb.toString();
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }

    /**
     * 查找使用者附近的捷運站，不使用 Google 距離 API。
     */
    public void findNearestStation() {
        try {
            // 讀取 JSON 資料
            String jsonData = loadJSONFromAsset();
            if (jsonData == null) {
                Log.e("MRT", "無法讀取 JSON 檔案");
                return;
            }

            // 解析 JSON 並計算距離
            JSONObject jsonObject = new JSONObject(jsonData);
            JSONArray stations = jsonObject.getJSONArray("features");

            float minDistance = Float.MAX_VALUE;

            for (int i = 0; i < stations.length(); i++) {
                JSONObject station = stations.getJSONObject(i);
                JSONObject geometry = station.getJSONObject("geometry");
                JSONArray coordinates = geometry.getJSONArray("coordinates");

                double stationBeforeLat = coordinates.getDouble(0);
                double stationBeforeLon = coordinates.getDouble(1);
                TWD97ToWGS84Converter twd97Converter = new TWD97ToWGS84Converter();
                double stationLatLon[] = twd97Converter.TWD97ToWGS84Converter(stationBeforeLat,stationBeforeLon);


                // 使用你自定義的距離計算方法
                double distance = getDistanceBetweenPointsNew(userLat, userLon, stationLatLon[0], stationLatLon[1]);

                if (distance < minDistance) {
                    minDistance = (float) distance;
                    staionRecord.setNearestMRTStationFinder(
                            station.getJSONObject("properties").getString("NAME"),
                            minDistance, stationLatLon[0], stationLatLon[1]

                    );
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
