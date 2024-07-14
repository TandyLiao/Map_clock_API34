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

    private SharedViewModel sharedViewModel;
    private WeatherService weatherService;
    private Context context;

    public WeatherAdviceHelper(SharedViewModel sharedViewModel, WeatherService weatherService, Context context) {
        this.sharedViewModel = sharedViewModel;
        this.weatherService = weatherService;
        this.context = context;
    }

    public void getWeatherAdvice(int index, WeatherAdviceCallback callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String capital = sharedViewModel.getCapital(index);
                    String area = sharedViewModel.getArea(index);
                    String weatherJson = weatherService.getWeather(capital, area);
                    // 確保這裡傳遞的是正確的 Context 對象
                    String[] advice = WeatherService.getAdvice(capital, weatherJson, context);

                    //advice存的資料分別放到List<String>到時候分別傳到各自的textview 摁對我目前只想到這方法
                    List<String> locationAdvices = new ArrayList<>();
                    locationAdvices.add(sharedViewModel.getDestinationName(index));

                    List<String> descriptionAdvices = new ArrayList<>();
                    if (advice.length > 1) {
                        descriptionAdvices.add("\n" + advice[1] + "\n" + advice[2]);
                    }

                    List<String> temperatureAdvices = new ArrayList<>();
                    if (advice.length > 0) {
                        temperatureAdvices.add(advice[0]);
                    }

                    List<String> imageAdvices = new ArrayList<>();
                    if (advice.length > 3) {
                        imageAdvices.add(advice[3]);
                    }

                    List<String> cityAdvices = new ArrayList<>();
                    if (advice.length > 4) {
                        cityAdvices.add(advice[4] + advice[5]);
                    }

                    if (context instanceof FragmentActivity) {
                        ((FragmentActivity) context).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Log.d("WeatherAdviceHelper", "Weather Advice Received: " + descriptionAdvices+temperatureAdvices);
                                callback.onWeatherAdviceReceived(locationAdvices,descriptionAdvices,temperatureAdvices,imageAdvices,cityAdvices);
                            }
                        });
                    }
                } catch (IOException e) {
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
                }catch (Exception e) {
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
        }).start();
    }

    public interface WeatherAdviceCallback {
        void onWeatherAdviceReceived( List<String> locationAdvices,List<String> descriptionAdvices, List<String> temperatureAdvices,List<String> imageAdvices,List<String> cityAdvices);
    }

}
