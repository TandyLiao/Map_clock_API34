package com.example.map_clock_api34.Weather;

import android.net.Uri;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;

public class WeatherService {

    private static final String API_KEY = "CWA-90088B0B-CD8C-470A-9064-457A4FCD60FC";
    private static final String BASE_URL = "https://opendata.cwa.gov.tw/api/v1/rest/datastore/";

    public String getWeather(String location) throws IOException {
        OkHttpClient client = new OkHttpClient();
        String encodedLocation = Uri.encode(location);
        String url = BASE_URL + "F-C0032-001?Authorization=" + API_KEY + "&format=JSON" + "&locationName=" + encodedLocation;

        Request request = new Request.Builder()
                .url(url)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }
            return response.body().string();
        }
    }

    public static String getAdvice(String jsonResponse) {
        try {
            JSONObject jsonObject = new JSONObject(jsonResponse);
            JSONArray locationArray = jsonObject.getJSONObject("records").getJSONArray("location");

            // 确保 locationArray 不为空
            if (locationArray.length() == 0) {
                return "找不到該地區的天氣資訊";
            }

            JSONObject location = locationArray.getJSONObject(0);
            JSONArray weatherElements = location.getJSONArray("weatherElement");

            String weatherDescription = "";
            int rainProbability = -1; // 初始化降雨概率为负数

            // 查找降雨概率字段
            for (int i = 0; i < weatherElements.length(); i++) {
                JSONObject element = weatherElements.getJSONObject(i);
                if (element.getString("elementName").equals("PoP")) { // 假设这个是降雨概率的字段名
                    rainProbability = element.getJSONArray("time").getJSONObject(0).getJSONObject("parameter").getInt("parameterName");
                    break;
                }
            }

            // 获取天气描述
            for (int i = 0; i < weatherElements.length(); i++) {
                JSONObject element = weatherElements.getJSONObject(i);
                if (element.getString("elementName").equals("Wx")) {
                    weatherDescription = element.getJSONArray("time").getJSONObject(0).getJSONObject("parameter").getString("parameterName");
                    break;
                }
            }

            // 根据天气描述和降雨概率给出建议
            if (weatherDescription.contains("雨") && rainProbability >= 40) {
                return "降雨概率為"+rainProbability+"，建議攜帶雨傘";
            }
            else {
                return "天氣良好";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "無法獲取天氣建議";
        }
    }
}
