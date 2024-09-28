package com.example.map_clock_api34.MRTStationFinder;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class MRTStationLocator {

    private Context context;

    // 建構子，傳入 Context 來讀取 assets 中的 JSON 檔案
    public MRTStationLocator(Context context) {
        this.context = context;
    }

    /**
     * 從 assets 資料夾中讀取捷運站的 JSON 資料，並查找指定站名的經緯度。
     * @param stationName 站名
     * @return 包含經緯度的雙重陣列，或 null 如果未找到
     */
    public double[] findStationCoordinates(String stationName) {
        try {
            // 讀取 JSON 資料
            String jsonData = loadJSONFromAsset();
            if (jsonData == null) {
                Log.e("MRTStationLocator", "無法讀取 JSON 檔案");
                return null;
            }

            // 解析 JSON 並查找指定站名
            JSONObject jsonObject = new JSONObject(jsonData);
            JSONArray stations = jsonObject.getJSONArray("features");

            for (int i = 0; i < stations.length(); i++) {
                JSONObject station = stations.getJSONObject(i);
                JSONObject properties = station.getJSONObject("properties");
                String name = properties.getString("NAME");

                if (name.equals(stationName)) {
                    // 獲取座標 (EPSG:3826 格式，TWD97)
                    JSONObject geometry = station.getJSONObject("geometry");
                    JSONArray coordinates = geometry.getJSONArray("coordinates");

                    double stationTWD97X = coordinates.getDouble(0);
                    double stationTWD97Y = coordinates.getDouble(1);

                    // 將 TWD97 座標轉換為 WGS84 (經緯度)
                    TWD97ToWGS84Converter converter = new TWD97ToWGS84Converter();
                    double[] latLon = converter.TWD97ToWGS84Converter(stationTWD97X, stationTWD97Y);

                    // 回傳經緯度
                    return latLon;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 從 assets 資料夾讀取捷運站的 JSON 檔案。
     * @return JSON 資料的字串
     */
    private String loadJSONFromAsset() {
        String json;
        try {
            AssetManager assetManager = context.getAssets();
            InputStream is = assetManager.open("MRT_Staion_Position.json");
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
}
