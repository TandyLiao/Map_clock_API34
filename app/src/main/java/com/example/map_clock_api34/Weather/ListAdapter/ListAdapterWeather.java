package com.example.map_clock_api34.Weather.ListAdapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.map_clock_api34.R;
import com.example.map_clock_api34.SharedViewModel;
import com.example.map_clock_api34.Weather.WeatherAdviceHelper;
import com.example.map_clock_api34.Weather.WeatherService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class ListAdapterWeather extends RecyclerView.Adapter<ListAdapterWeather.ViewHolder> {

    private final ArrayList<HashMap<String, String>> arrayList; // 用於存放天氣資料的列表
    private final WeatherAdviceHelper weatherAdviceHelper; // 天氣建議的輔助類
    private final Random random = new Random(); // 用於隨機生成圖片背景

    // 建構函式，初始化數據和 WeatherAdviceHelper
    public ListAdapterWeather(ArrayList<HashMap<String, String>> arrayList, SharedViewModel sharedViewModel, WeatherService weatherService, Context context) {
        this.arrayList = arrayList;
        this.weatherAdviceHelper = new WeatherAdviceHelper(sharedViewModel, weatherService, context); // 初始化 WeatherAdviceHelper
    }

    // ViewHolder 類別，持有 RecyclerView 中的每個項目視圖
    class ViewHolder extends RecyclerView.ViewHolder {
        private TextView Weather_location;  // 顯示地點
        private TextView Weather_info;      // 顯示天氣資訊
        private TextView Weather_temp;      // 顯示溫度
        private ImageView Weather_image;    // 顯示天氣圖片
        private TextView Weather_city;      // 顯示城市名稱

        // ViewHolder 的建構函式，初始化每個項目的視圖
        public ViewHolder(View itemView) {
            super(itemView);
            Weather_location = itemView.findViewById(R.id.text_location);
            Weather_info = itemView.findViewById(R.id.text_wheather);
            Weather_temp = itemView.findViewById(R.id.temperature);
            Weather_image = itemView.findViewById(R.id.weather_imageView);
            Weather_city = itemView.findViewById(R.id.cityname);
        }
    }

    // 創建 ViewHolder，從佈局文件生成項目的視圖
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycleviewitem_weather, parent, false); // 載入項目的佈局
        return new ViewHolder(view);
    }

    // 綁定數據到 ViewHolder，根據位置來填充數據
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        weatherAdviceHelper.getWeatherAdvice(position, new WeatherAdviceHelper.WeatherAdviceCallback() {
            @Override
            // 接收 WeatherAdviceHelper 的天氣數據並綁定到對應的 TextView 和 ImageView 上
            public void onWeatherAdviceReceived(List<String> locationAdvices, List<String> descriptionAdvices, List<String> temperatureAdvices, List<String> imageAdvices, List<String> cityAdvices) {
                if (!imageAdvices.isEmpty()) {
                    String imageResource = imageAdvices.get(0);
                    holder.Weather_location.setText(locationAdvices.get(0));

                    // 根據天氣類型設置對應的圖片
                    if (holder.Weather_image != null) {
                        int imageResId = getImageResourceId(imageResource);
                        holder.Weather_image.setImageResource(imageResId); // 設置天氣圖片
                    }

                    // 如果有天氣描述，設置描述；否則顯示預設提示
                    if (!descriptionAdvices.isEmpty()) {
                        holder.Weather_info.setText(descriptionAdvices.get(0));
                    } else {
                        holder.Weather_info.setText("找不到當地天氣資訊");
                    }

                    // 如果有溫度資訊，設置溫度；否則顯示未知溫度
                    if (!temperatureAdvices.isEmpty()) {
                        holder.Weather_temp.setText(temperatureAdvices.get(0));
                    } else {
                        holder.Weather_temp.setText("未知溫度");
                    }

                    // 如果有城市資訊，設置城市名稱；否則顯示未知城市
                    if (!cityAdvices.isEmpty()) {
                        holder.Weather_city.setText(cityAdvices.get(0));
                    } else {
                        holder.Weather_city.setText("未知城市");
                    }
                }
            }
        });
    }

    // 根據天氣類型隨機生成不同背景圖片，雨天或晴天
    private int getImageResourceId(String imageResource) {
        if (imageResource.equals("rain")) {
            // 隨機選擇三張雨天圖片
            int random_rain_Index = random.nextInt(3);
            switch (random_rain_Index) {
                case 0:
                    return R.drawable.rain1;
                case 1:
                    return R.drawable.rain2;
                case 2:
                    return R.drawable.rain3;
                default:
                    return R.drawable.weather_default; // 預設圖片
            }

        } else {
            // 隨機選擇三張晴天圖片
            int random_sun_Index = random.nextInt(3);
            switch (random_sun_Index) {
                case 0:
                    return R.drawable.sunny1;
                case 1:
                    return R.drawable.sunny2;
                case 2:
                    return R.drawable.sunny3;
                default:
                    return R.drawable.weather_default; // 預設圖片
            }
        }
    }

    // 返回列表項目的數量
    @Override
    public int getItemCount() {
        return arrayList.size();
    }
}
