package com.example.map_clock_api34.Weather;

import android.content.Context;
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

import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class WeatherAdviceHelper {

    private SharedViewModel sharedViewModel;
    private WeatherService weatherService;
    private Context context;
    private View overlayView;

    public WeatherAdviceHelper(SharedViewModel sharedViewModel, WeatherService weatherService, Context context) {
        this.sharedViewModel = sharedViewModel;
        this.weatherService = weatherService;
        this.context = context;
    }

    public void getWeatherAdvice(View view) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String[] capital = new String[sharedViewModel.getI() + 1];

                    for (int x = 0; x <= sharedViewModel.getI(); x++) {

                        String weatherJson = weatherService.getWeather(sharedViewModel.getCapital(x));
                        List<String> advices = Collections.singletonList(WeatherService.getAdvice(weatherJson));

                        if (advices.size() > 0 && advices.get(0).contains("降雨概率")) {
                            capital[x] = sharedViewModel.getCapital(x) + advices.get(0);
                        } else {
                            capital[x] = null;
                        }
                    }
                    if (context instanceof FragmentActivity) {
                        ((FragmentActivity) context).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                showRainyLocations(view, sharedViewModel, capital);
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
                }
            }
        }).start();
    }
    //顯示各地天氣
    private void showRainyLocations(View view, SharedViewModel sharedViewModel, String[] capital) {

        if(sharedViewModel.getCapital(0)!=null){
            //呼叫PopupWindow顯示天氣
            weatherPopWindow(view, sharedViewModel);
        }
        else {
            Toast.makeText(context, "你還沒有選擇地區喔", Toast.LENGTH_SHORT).show();
        }
    }

    public void weatherPopWindow(View v, SharedViewModel sharedViewModel) {

        View view = LayoutInflater.from(context).inflate(R.layout.popupwindow_weather, null, false);
        PopupWindow popupWindow = new PopupWindow(view, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        popupWindow.setWidth(700);
        popupWindow.setFocusable(false);
        popupWindow.setOutsideTouchable(false);

        TextView weatherInfoTextView = view.findViewById(R.id.txtWeatherNote);

        //疊加View在底下，讓她不會按到底層就跳掉
        overlayView = new View(context);
        overlayView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        overlayView.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent));
        overlayView.setClickable(true);
        ((ViewGroup) ((FragmentActivity) context).findViewById(android.R.id.content)).addView(overlayView);

        new Thread(new Runnable() {
            @Override
            public void run() {

                StringBuilder weatherInfo = new StringBuilder();

                for (int x = 0; x <= sharedViewModel.getI(); x++) {

                    String capital = sharedViewModel.getCapital(x);

                    if (capital != null) {
                        try {
                            String weatherJson = weatherService.getWeather(capital);
                            List<String> advices = Collections.singletonList(WeatherService.getAdvice(weatherJson)); // 假設這裡可以直接取得多條天氣建議
                            for (String advice : advices) {
                                weatherInfo.append(capital).append("：").append(advice).append("\n");
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                            Log.e("WeatherService", "IOException occurred: " + e.getMessage());
                            weatherInfo.append(capital).append("：").append("無法獲取天氣資訊\n");
                        }
                    }
                }
                ((FragmentActivity) context).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // 將天氣資訊顯示在 TextView 中
                        weatherInfoTextView.setText(weatherInfo.toString());
                        // 設置取消按鈕的點擊事件
                        Button btnCancel = view.findViewById(R.id.PopupYes);
                        btnCancel.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                popupWindow.dismiss();
                                removeOverlayView();
                            }
                        });
                        // 顯示彈出視窗
                        popupWindow.showAtLocation(v, Gravity.CENTER, 0, 0);
                    }
                });
            }
        }).start();
    }
    //把疊加在底層的View刪掉
    private void removeOverlayView() {
        if (overlayView != null && overlayView.getParent() != null) {
            ((ViewGroup) overlayView.getParent()).removeView(overlayView);
            overlayView = null;
        }
    }
}
