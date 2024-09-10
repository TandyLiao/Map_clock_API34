package com.example.map_clock_api34.Weather;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.example.map_clock_api34.BuildConfig;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class WeatherService {

    // API Key 和基本 URL
    private static final String API_KEY = BuildConfig.WEATHER_API_KEY;
    private static final String BASE_URL = "https://opendata.cwa.gov.tw/api/v1/rest/datastore/";

    private WeatherLocationAreaHelper weatherHelper;

    // 根據 cityName 和 areaName 來獲取天氣資訊
    public String getWeather(String cityName, String areaName) throws IOException {

        weatherHelper = new WeatherLocationAreaHelper();
        OkHttpClient client = new OkHttpClient(); // 初始化 OkHttpClient

        String cityCode = weatherHelper.getWeatherLocationArea(cityName); // 根據 cityName 取得對應的 cityCode
        String encodedCityName = Uri.encode(cityName); // 編碼 cityName
        String encodedAreaName = Uri.encode(areaName); // 編碼 areaName
        String url;

        // 處理特定縣市的 API 請求，使用不同的 URL
        if (cityName.equals("金門縣") || cityName.equals("連江縣")) {
            url = BASE_URL + "F-C0032-001?Authorization=" + API_KEY + "&format=JSON&locationName=" + encodedCityName;
        } else {
            url = BASE_URL + "F-D0047-093?Authorization=" + API_KEY + "&format=JSON" + "&locationId=" + cityCode + "&locationName=" + encodedAreaName;
        }

        Request request = new Request.Builder()
                .url(url)
                .build(); // 創建請求

        // 執行網路請求並返回天氣資訊的 JSON 字串
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response); // 異常處理
            }
            return response.body().string();
        } finally {
            // 清空 OkHttp 連接池並關閉線程池
            client.connectionPool().evictAll();  // 清空連接池
            client.dispatcher().executorService().shutdown();  // 關閉線程池
        }
    }

    // 解析天氣資訊，並返回天氣建議的陣列
    public static String[] getAdvice(String capital, String jsonResponse, Context context) {
        String[] result = new String[6]; // 儲存結果的陣列
        try {
            // 對於特定縣市，使用特定的天氣建議解析
            if (capital.equals("金門縣") || capital.equals("連江縣")) {
                return getSpecialRegionAdvice(jsonResponse, context);
            }

            JSONObject jsonObject = new JSONObject(jsonResponse);
            JSONArray locationsArray = jsonObject.getJSONObject("records").getJSONArray("locations");

            if (locationsArray.length() == 0) {
                return new String[]{"找不到該地區的天氣資訊"}; // 當找不到資料時返回預設訊息
            }

            // 變數初始化
            String weatherDescription = "";
            int rainProbability = -1;
            int temperature = -1;
            String locationArrayString;

            // 解析 JSON 資料，提取天氣描述、溫度和降雨機率
            for (int i = 0; i < locationsArray.length(); i++) {
                JSONObject locations = locationsArray.getJSONObject(i);
                String cityName = locations.getString("locationsName"); // 取得縣市名字
                JSONArray locationArray = locations.getJSONArray("location");

                for (int k = 0; k < locationArray.length(); k++) {
                    JSONObject location = locationArray.getJSONObject(k);
                    locationArrayString = location.getString("locationName"); // 取得鄉鎮市區名字
                    JSONArray weatherElements = location.getJSONArray("weatherElement");

                    // 取得氣溫
                    for (int j = 0; j < weatherElements.length(); j++) {
                        JSONObject element = weatherElements.getJSONObject(j);
                        if (element.getString("elementName").equals("T")) {
                            temperature = element.getJSONArray("time").getJSONObject(0).getJSONArray("elementValue").getJSONObject(0).getInt("value");
                            break;
                        }
                    }

                    // 取得降雨機率
                    for (int j = 0; j < weatherElements.length(); j++) {
                        JSONObject element = weatherElements.getJSONObject(j);
                        if (element.getString("elementName").equals("PoP12h")) {
                            rainProbability = element.getJSONArray("time").getJSONObject(0).getJSONArray("elementValue").getJSONObject(0).getInt("value");
                            break;
                        }
                    }

                    // 取得天氣描述
                    for (int j = 0; j < weatherElements.length(); j++) {
                        JSONObject element = weatherElements.getJSONObject(j);
                        if (element.getString("elementName").equals("Wx")) {
                            weatherDescription = element.getJSONArray("time").getJSONObject(0).getJSONArray("elementValue").getJSONObject(0).getString("value");
                            break;
                        }
                    }

                    // 將解析到的資訊存到 result 陣列
                    result[0] = temperature + "˚C"; // 氣溫
                    result[1] = "降雨概率為" + rainProbability + "%"; // 降雨機率
                    result[2] = (weatherDescription.contains("雨") && rainProbability >= 40) ? weatherDescription + "\n" + "建議攜帶雨傘" : weatherDescription; // 天氣描述
                    result[3] = (weatherDescription.contains("雨") && rainProbability >= 40) ? "rain" : "sun"; // 天氣狀態（影響背景圖）
                    result[4] = cityName; // 縣市名稱
                    result[5] = locationArrayString; // 鄉鎮市區名稱

                    return result; // 返回解析後的天氣資訊
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            // 當發生異常時返回預設訊息
            result[0] = "未知温度";
            result[1] = "未知降雨概率";
            result[2] = "未知天氣描述";
            result[3] = "default";
            result[4] = "未知城市";
            result[5] = "未知地區";
            return result;
        }
        return null;
    }

    // 針對金門縣與連江縣的特別天氣解析方法
    private static String[] getSpecialRegionAdvice(String jsonResponse, Context context) {
        String[] result = new String[6];
        try {
            JSONObject jsonObject = new JSONObject(jsonResponse);
            JSONArray locationArray = jsonObject.getJSONObject("records").getJSONArray("location");

            if (locationArray.length() == 0) {
                return new String[]{"找不到該地區的天氣資訊"};
            }

            String weatherDescription = "未知天氣描述";
            int rainProbability = -1;
            int temperature = -1;
            String cityname;

            // 解析特定地區的天氣資訊
            for (int i = 0; i < locationArray.length(); i++) {
                JSONObject location = locationArray.getJSONObject(i);
                cityname = location.getString("locationName");
                JSONArray weatherElements = location.getJSONArray("weatherElement");

                // 找最大氣溫
                for (int j = 0; j < weatherElements.length(); j++) {
                    JSONObject element = weatherElements.getJSONObject(j);
                    if (element.getString("elementName").equals("MaxT")) {
                        temperature = element.getJSONArray("time").getJSONObject(0).getJSONObject("parameter").getInt("parameterName");
                        break;
                    }
                }

                // 找降雨機率
                for (int j = 0; j < weatherElements.length(); j++) {
                    JSONObject element = weatherElements.getJSONObject(j);
                    if (element.getString("elementName").equals("PoP")) {
                        rainProbability = element.getJSONArray("time").getJSONObject(0).getJSONObject("parameter").getInt("parameterName");
                        break;
                    }
                }

                // 找天氣描述
                for (int j = 0; j < weatherElements.length(); j++) {
                    JSONObject element = weatherElements.getJSONObject(j);
                    if (element.getString("elementName").equals("Wx")) {
                        weatherDescription = element.getJSONArray("time").getJSONObject(0).getJSONObject("parameter").getString("parameterName");
                        break;
                    }
                }

                // 將解析到的資訊存到 result 陣列
                result[0] = (temperature != -1) ? temperature + "˚C" : "?˚C";
                result[1] = (rainProbability != -1) ? "降雨概率為" + rainProbability : "未知降雨概率";
                result[2] = (weatherDescription.contains("雨") && rainProbability >= 40) ? weatherDescription + "\n" + "建議攜帶雨傘" : weatherDescription;
                result[3] = (weatherDescription.contains("雨") && rainProbability >= 40) ? "rain" : "sun";
                result[4] = cityname;
                result[5] = "";
                return result;
            }
        } catch (JSONException e) {
            e.printStackTrace();
            // 當發生異常時返回預設訊息
            result[0] = "未知温度";
            result[1] = "未知降雨概率";
            result[2] = "未知天氣描述";
            result[3] = "default";
            result[4] = "未知城市";
            result[5] = "未知地區";
            return result;
        }
        return new String[]{"無法獲取天氣建議"};
    }
}
