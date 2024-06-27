package com.example.map_clock_api34.Weather;

import android.net.Uri;
import android.widget.Toast;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;

public class WeatherService {

    private static final String API_KEY = "CWA-90088B0B-CD8C-470A-9064-457A4FCD60FC";
    private static final String BASE_URL = "https://opendata.cwa.gov.tw/api/v1/rest/datastore/";

    WeatherLocationAreaHelper weatherHelper;

    public String getWeather(String cityName, String areaName) throws IOException {

        weatherHelper = new WeatherLocationAreaHelper();

        OkHttpClient client = new OkHttpClient();

        String cityCode = weatherHelper.getWeatherLocationArea(cityName);
        String encodedCityName = Uri.encode(cityName);
        String encodedAreaName = Uri.encode(areaName);
        String url;

        if (cityName.equals("金門縣") || cityName.equals("連江縣")) {
            // 使用新的 API URL
            url = BASE_URL + "F-C0032-001?Authorization=" + API_KEY + "&format=JSON&locationName=" + encodedCityName;
        } else {
            // 其他縣市的處理
            url = BASE_URL + "F-D0047-093?Authorization=" + API_KEY + "&format=JSON" + "&locationId=" + cityCode + "&locationName=" + encodedAreaName;
        }

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

    public static String getAdvice(String capital, String jsonResponse) {

        try {
            if(capital.equals("金門縣") || capital.equals("連江縣")){
                return getSpecialRegionAdvice(jsonResponse);
            }
            JSONObject jsonObject = new JSONObject(jsonResponse);
            JSONArray locationsArray = jsonObject.getJSONObject("records").getJSONArray("locations");

            if (locationsArray.length() == 0) {
                return "找不到該地區的天氣資訊";
            }


            String weatherDescription = "";
            int rainProbability = -1;

            //去Json檔抓資料
            for (int i = 0; i < locationsArray.length(); i++) {

                JSONObject locations = locationsArray.getJSONObject(i);
                //抓縣市名字
                String cityName = locations.getString("locationsName");
                //抓鄉鎮市區的名字
                JSONArray locationArray = locations.getJSONArray("location");

                for (int k = 0; k < locationArray.length(); k++) {
                    JSONObject location = locationArray.getJSONObject(k);
                    JSONArray weatherElements = location.getJSONArray("weatherElement");

                    // 找降雨機率
                    for (int j = 0; j < weatherElements.length(); j++) {
                        JSONObject element = weatherElements.getJSONObject(j);
                        if (element.getString("elementName").equals("PoP12h")) {
                            rainProbability = element.getJSONArray("time").getJSONObject(0).getJSONArray("elementValue").getJSONObject(0).getInt("value");
                            System.out.println("Rain Probability: " + rainProbability);
                            break;
                        }
                    }

                    // 找天氣描述
                    for (int j = 0; j < weatherElements.length(); j++) {
                        JSONObject element = weatherElements.getJSONObject(j);
                        if (element.getString("elementName").equals("Wx")) {
                            weatherDescription = element.getJSONArray("time").getJSONObject(0).getJSONArray("elementValue").getJSONObject(0).getString("value");
                            System.out.println("Weather Description: " + weatherDescription);
                            break;
                        }
                    }

                    // 根據天氣描述和降雨機率給出建議
                    if (weatherDescription.contains("雨") && rainProbability >= 40) {
                        return " 降雨概率為" + rainProbability + "，建議攜帶雨傘";
                    }else{
                        return "天氣良好";
                    }
                }
            }

        }
        catch (Exception e) {
            e.printStackTrace();
            System.out.println("Exception: " + e.getMessage());
            return "無法獲取天氣建議";
        }
        return null;
    }

    private static String getSpecialRegionAdvice(String jsonResponse) {
        try {
            JSONObject jsonObject = new JSONObject(jsonResponse);
            JSONArray locationArray = jsonObject.getJSONObject("records").getJSONArray("location");

            if (locationArray.length() == 0) {
                return "找不到該地區的天氣資訊";
            }

            String weatherDescription = "";
            int rainProbability = -1;

            for (int i = 0; i < locationArray.length(); i++) {
                JSONObject location = locationArray.getJSONObject(i);
                String cityName = location.getString("locationName");
                JSONArray weatherElements = location.getJSONArray("weatherElement");

                for (int j = 0; j < weatherElements.length(); j++) {
                    JSONObject element = weatherElements.getJSONObject(j);
                    if (element.getString("elementName").equals("PoP")) {
                        rainProbability = element.getJSONArray("time").getJSONObject(0).getJSONObject("parameter").getInt("parameterName");
                        System.out.println("Rain Probability: " + rainProbability);
                    }

                    if (element.getString("elementName").equals("Wx")) {
                        weatherDescription = element.getJSONArray("time").getJSONObject(0).getJSONObject("parameter").getString("parameterName");
                        System.out.println("Weather Description: " + weatherDescription);
                    }
                }

                if (weatherDescription.contains("雨") && rainProbability >= 40) {
                    return "降雨概率為" + rainProbability + "，建議攜帶雨傘";
                } else {
                    return "天氣良好";
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Exception: " + e.getMessage());
            return "無法獲取天氣建議";
        }
        return "無法獲取天氣建議";
    }

}
