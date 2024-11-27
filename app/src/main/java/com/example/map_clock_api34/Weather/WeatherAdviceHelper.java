package com.example.map_clock_api34.Weather;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.fragment.app.FragmentActivity;

import com.example.map_clock_api34.R;
import com.example.map_clock_api34.SharedViewModel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class WeatherAdviceHelper {

    private final SharedViewModel sharedViewModel;  // 用於存取天氣資訊的 ViewModel ViewModel for accessing weather information
    private final WeatherService weatherService;    // 用於請求天氣數據的服務 Service for requesting weather data.
    private final Context context; // 用於顯示 UI 信息的上下文 Context for displaying UI information

    public WeatherAdviceHelper(SharedViewModel sharedViewModel, WeatherService weatherService, Context context) {
        this.sharedViewModel = sharedViewModel;
        this.weatherService = weatherService;
        this.context = context;
    }

    // 根據索引獲取天氣建議，並通過回調傳遞結果Get weather suggestions based on the index and pass the result via a callback
    public void getWeatherAdvice(int index, WeatherAdviceCallback callback) {
        new Thread(new Runnable() { // 在新線程中執行網路請求
            @Override
            public void run() {
                try {
                    // 從 ViewModel 中取得城市和地區資訊 Retrieve city and region information from the ViewModel
                    String capital = sharedViewModel.getCapital(index);
                    String area = sharedViewModel.getArea(index);

                    // 使用天氣服務請求天氣資訊 Request weather information using the weather service.
                    String weatherJson = weatherService.getWeather(capital, area);

                    // 獲取天氣建議並處理 Retrieve and process weather suggestions
                    String[] advice = WeatherService.getAdvice(capital, weatherJson, context);

                    // 創建用於存放不同天氣資訊的 List Create a List for storing various weather information.
                    List<String> locationAdvices = new ArrayList<>();
                    locationAdvices.add(sharedViewModel.getDestinationName(index)); // 添加地點名稱 Add location name

                    List<String> descriptionAdvices = new ArrayList<>();
                    if (advice.length > 1) {
                        descriptionAdvices.add("\n" + advice[1] + "\n" + advice[2]); // 添加天氣描述 Add weather description
                    }

                    List<String> temperatureAdvices = new ArrayList<>();
                    if (advice.length > 0) {
                        temperatureAdvices.add(advice[0]); // 添加溫度資訊 Add temperature information
                    }

                    List<String> imageAdvices = new ArrayList<>();
                    if (advice.length > 3) {
                        imageAdvices.add(advice[3]); // 添加天氣圖片資訊 Add weather image information
                    }

                    List<String> cityAdvices = new ArrayList<>();
                    if (advice.length > 4) {
                        cityAdvices.add(advice[4] + advice[5]); // 添加城市資訊 Add city information
                    }

                    // 確保 UI 操作在主線程中執行 Ensure UI operations are executed on the main thread.
                    if (context instanceof FragmentActivity) {
                        ((FragmentActivity) context).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                // 回調傳遞天氣資訊
                                Log.d("WeatherAdviceHelper", "Weather Advice Received: " + descriptionAdvices + temperatureAdvices);
                                callback.onWeatherAdviceReceived(locationAdvices, descriptionAdvices, temperatureAdvices, imageAdvices, cityAdvices);
                            }
                        });
                    }
                } catch (IOException e) {
                    // 異常處理：無法獲取天氣資訊時顯示提示 Error handling: Display a prompt when weather information cannot be retrieved
                    e.printStackTrace();
                    Log.e("WeatherService", "IOException occurred: " + e.getMessage());
                    if (context != null) {
                        ((FragmentActivity) context).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(context, "無法獲取天氣資訊", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                } catch (Exception e) {
                    // 異常處理：捕捉所有其他異常並顯示提示 Error handling: Catch all other exceptions and display a prompt.
                    e.printStackTrace();
                    Log.e("WeatherAdviceHelper", "Exception occurred: " + e.getMessage());
                    if (context != null && context instanceof FragmentActivity) {
                        ((FragmentActivity) context).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(context, "超出範圍", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            }
        }).start(); // 開始線程 Start a thread.
    }

    // 定義回調介面，用於將天氣數據傳遞回呼叫者 Define a callback interface to pass weather data back to the caller
    public interface WeatherAdviceCallback {
        void onWeatherAdviceReceived(List<String> locationAdvices, List<String> descriptionAdvices, List<String> temperatureAdvices, List<String> imageAdvices, List<String> cityAdvices);
    }
}
