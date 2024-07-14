package com.example.map_clock_api34.Weather.ListAdapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
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

    private ArrayList<HashMap<String, String>> arrayList;
    private SharedViewModel sharedViewModel;
    private WeatherAdviceHelper weatherAdviceHelper;
    private Random random = new Random();

    public ListAdapterWeather(ArrayList<HashMap<String, String>> arrayList, SharedViewModel sharedViewModel, WeatherService weatherService, Context context) {
        this.arrayList = arrayList;
        this.sharedViewModel = sharedViewModel;
        this.weatherAdviceHelper = new WeatherAdviceHelper(sharedViewModel, weatherService, context);
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private TextView Weather_location;
        private TextView Weather_info;
        private TextView Weather_temp;
        private ImageView Weather_image;
        private TextView Weather_city;

        public ViewHolder(View itemView) {
            super(itemView);
            Weather_location=itemView.findViewById(R.id.text_location);
            Weather_info = itemView.findViewById(R.id.text_wheather);
            Weather_temp = itemView.findViewById(R.id.temperature);
            Weather_image = itemView.findViewById(R.id.weather_imageView);
            Weather_city= itemView.findViewById(R.id.cityname);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycleviewitem_weather, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        weatherAdviceHelper.getWeatherAdvice(position, new WeatherAdviceHelper.WeatherAdviceCallback() {
            @Override
            //把List<String>分別丟到textview
            public void onWeatherAdviceReceived( List<String> locationAdvices,List<String> descriptionAdvices, List<String> temperatureAdvices, List<String> imageAdvices, List<String> cityAdvices) {
                if (!imageAdvices.isEmpty()) {
                    String imageResource = imageAdvices.get(0);
                    holder.Weather_location.setText(locationAdvices.get(0));
                    if (holder.Weather_image != null) {
                        //隨機改變item背景
                        int imageResId = getImageResourceId(imageResource);
                        holder.Weather_image.setImageResource(imageResId);
                    }
                    if (!descriptionAdvices.isEmpty()) {
                        holder.Weather_info.setText(descriptionAdvices.get(0));
                    } else {
                        holder.Weather_info.setText("找不到當地天氣資訊");
                    }

                    if (!temperatureAdvices.isEmpty()) {
                        holder.Weather_temp.setText(temperatureAdvices.get(0));
                    } else {
                        holder.Weather_temp.setText("未知溫度");
                    }
                    if (!cityAdvices.isEmpty()) {
                        holder.Weather_city.setText(cityAdvices.get(0));
                    } else {
                        holder.Weather_city.setText("未知城市");
                    }

                }
            }
        });
    }

    //雨天晴天背景隨機產生碼
    private int getImageResourceId(String imageResource) {
        if (imageResource.equals("rain")) {
            int random_rain_Index = random.nextInt(3);
            switch (random_rain_Index) {
                case 0:
                    return R.drawable.rain1;
                case 1:
                    return R.drawable.rain2;
                case 2:
                    return R.drawable.rain3;
                default:
                    return R.drawable.weather_default;
            }

        } else {
            int random_sun_Index = random.nextInt(3);
            switch (random_sun_Index) {
                case 0:
                    return R.drawable.sunny1;
                case 1:
                    return R.drawable.sunny2;
                case 2:
                    return R.drawable.sunny3;
                default:
                    return R.drawable.weather_default;
            }

        }
    }


    @Override
    public int getItemCount() {
        return arrayList.size();
    }
}


