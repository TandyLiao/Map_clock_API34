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

    private static final String API_KEY = BuildConfig.WEATHER_API_KEY;
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

    public static String[] getAdvice(String capital, String jsonResponse, Context context) {
        String[] result = new String[6];
        try {
            if (capital.equals("金門縣") || capital.equals("連江縣")) {
                return getSpecialRegionAdvice(jsonResponse, context);
            }
            JSONObject jsonObject = new JSONObject(jsonResponse);
            JSONArray locationsArray = jsonObject.getJSONObject("records").getJSONArray("locations");

            if (locationsArray.length() == 0) {
                return new String[]{"找不到該地區的天氣資訊"};
            }


            String weatherDescription = "";
            int rainProbability = -1;
            int temperature = -1;
            String locationArrayString = "";


            //去Json檔抓資料
            for (int i = 0; i < locationsArray.length(); i++) {

                JSONObject locations = locationsArray.getJSONObject(i);
                //抓縣市名字
                String cityName = locations.getString("locationsName");
                //抓鄉鎮市區的名字
                JSONArray locationArray = locations.getJSONArray("location");


                for (int k = 0; k < locationArray.length(); k++) {
                    JSONObject location = locationArray.getJSONObject(k);
                    locationArrayString = location.getString("locationName");
                    JSONArray weatherElements = location.getJSONArray("weatherElement");

                    //找氣溫
                    for (int j = 0; j < weatherElements.length(); j++) {
                        JSONObject element = weatherElements.getJSONObject(j);
                        if (element.getString("elementName").equals("T")) {
                            temperature = element.getJSONArray("time").getJSONObject(0).getJSONArray("elementValue").getJSONObject(0).getInt("value");
                            System.out.println(temperature + "度 ");
                            break;
                        }
                    }

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


                    System.out.println(cityName);
                    System.out.println(locationArrayString);

                    //把上面資料分別存到result

                    result[0] = temperature + "˚C";
                    result[1] = "降雨概率為" + rainProbability + "%";
                    result[2] = (weatherDescription.contains("雨") && rainProbability >= 40) ? weatherDescription + "\n" + "建議攜帶雨傘" : weatherDescription;
                    //存狀態判斷天氣改變背景
                    result[3] = (weatherDescription.contains("雨") && rainProbability >= 40) ? "rain" : "sun";
                    result[4] = cityName;
                    result[5] = locationArrayString;

                    return result;

                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            result[0] = "未知温度1";
            result[1] = "未知降雨概率2";
            result[2] = "未知天氣描述3";
            result[3] = "default";
            result[4] = "4";
            result[5] = "5";
            return result;
        }
        return null;
    }

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

            for (int i = 0; i < locationArray.length(); i++) {
                JSONObject location = locationArray.getJSONObject(i);
                cityname = location.getString("locationName");
                JSONArray weatherElements = location.getJSONArray("weatherElement");

                //沒有氣溫只好找最高氣溫
                for (int j = 0; j < weatherElements.length(); j++) {
                    JSONObject element = weatherElements.getJSONObject(j);
                    if (element.getString("elementName").equals("MaxT")) {
                        temperature = element.getJSONArray("time").getJSONObject(0).getJSONObject("parameter").getInt("parameterName");
                        System.out.println(temperature + "度 ");
                        break;
                    }
                }
                for (int j = 0; j < weatherElements.length(); j++) {
                    JSONObject element = weatherElements.getJSONObject(j);
                    if (element.getString("elementName").equals("PoP")) {
                        rainProbability = element.getJSONArray("time").getJSONObject(0).getJSONObject("parameter").getInt("parameterName");
                        System.out.println("Rain Probability: " + rainProbability);
                        break;
                    }
                }
                for (int j = 0; j < weatherElements.length(); j++) {
                    JSONObject element = weatherElements.getJSONObject(j);
                    if (element.getString("elementName").equals("Wx")) {
                        weatherDescription = element.getJSONArray("time").getJSONObject(0).getJSONObject("parameter").getString("parameterName");
                        System.out.println("Weather Description: " + weatherDescription);
                        break;
                    }
                }

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
            result[0] = "未知温度1";
            result[1] = "未知降雨概率2";
            result[2] = "未知天氣描述3";
            result[3] = "default";
            result[4] = "4";
            result[5] = "5";
            return result;
        }
        return new String[]{"無法獲取天氣建議"};
    }

}
