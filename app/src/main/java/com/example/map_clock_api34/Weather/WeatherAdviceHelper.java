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

    private final SharedViewModel sharedViewModel;  // 用於存取天氣資訊的 ViewModel
    private final WeatherService weatherService;    // 用於請求天氣數據的服務
    private final Context context; // 用於顯示 UI 信息的上下文

    public WeatherAdviceHelper(SharedViewModel sharedViewModel, WeatherService weatherService, Context context) {
        this.sharedViewModel = sharedViewModel;
        this.weatherService = weatherService;
        this.context = context;
    }

    // 根據索引獲取天氣建議，並通過回調傳遞結果
    public void getWeatherAdvice(int index, WeatherAdviceCallback callback) {
        new Thread(new Runnable() { // 在新線程中執行網路請求
            @Override
            public void run() {
                try {
                    // 從 ViewModel 中取得城市和地區資訊
                    String capital = sharedViewModel.getCapital(index);
                    String area = sharedViewModel.getArea(index);

                    // 使用天氣服務請求天氣資訊
                    String weatherJson = weatherService.getWeather(capital, area);

                    // 獲取天氣建議並處理
                    String[] advice = WeatherService.getAdvice(capital, weatherJson, context);

                    // 創建用於存放不同天氣資訊的 List
                    List<String> locationAdvices = new ArrayList<>();
                    locationAdvices.add(sharedViewModel.getDestinationName(index)); // 添加地點名稱

                    List<String> descriptionAdvices = new ArrayList<>();
                    if (advice.length > 1) {
                        descriptionAdvices.add("\n" + advice[1] + "\n" + advice[2]); // 添加天氣描述
                    }

                    List<String> temperatureAdvices = new ArrayList<>();
                    if (advice.length > 0) {
                        temperatureAdvices.add(advice[0]); // 添加溫度資訊
                    }

                    List<String> imageAdvices = new ArrayList<>();
                    if (advice.length > 3) {
                        imageAdvices.add(advice[3]); // 添加天氣圖片資訊
                    }

                    List<String> cityAdvices = new ArrayList<>();
                    if (advice.length > 4) {
                        cityAdvices.add(advice[4] + advice[5]); // 添加城市資訊
                    }

                    // 確保 UI 操作在主線程中執行
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
                    // 異常處理：無法獲取天氣資訊時顯示提示
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
                    // 異常處理：捕捉所有其他異常並顯示提示
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
        }).start(); // 開始線程
    }

    // 定義回調介面，用於將天氣數據傳遞回呼叫者
    public interface WeatherAdviceCallback {
        void onWeatherAdviceReceived(List<String> locationAdvices, List<String> descriptionAdvices, List<String> temperatureAdvices, List<String> imageAdvices, List<String> cityAdvices);
    }
}
