package com.example.map_clock_api34.home.ListAdapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.example.map_clock_api34.R;
import com.example.map_clock_api34.SharedViewModel;
import com.example.map_clock_api34.Weather.WeatherAdviceHelper;
import com.example.map_clock_api34.Weather.WeatherService;
import com.example.map_clock_api34.note.Note;

import java.util.ArrayList;
import java.util.HashMap;

public class ListAdapterTool extends RecyclerView.Adapter<ListAdapterTool.ViewHolder> {

    private ArrayList<HashMap<String, String>> arrayList;
    private FragmentTransaction fragmentTransaction;
    private WeatherAdviceHelper weatherAdviceHelper;

    public ListAdapterTool(FragmentTransaction fragmentTransaction, SharedViewModel sharedViewModel, WeatherService weatherService, Context context) {
        this.fragmentTransaction = fragmentTransaction;
        this.arrayList = new ArrayList<>();
        this.weatherAdviceHelper = new WeatherAdviceHelper(sharedViewModel, weatherService, context);
        initData();
    }

    //這裡新增功能列表的功能
    private void initData() {
        HashMap<String, String> item1 = new HashMap<>();
        item1.put("data", "記事");
        arrayList.add(item1);
        HashMap<String, String> item2 = new HashMap<>();
        item2.put("data", "加入書籤");
        arrayList.add(item2);
        HashMap<String, String> item3 = new HashMap<>();
        item3.put("data", "天氣");
        arrayList.add(item3);
        HashMap<String, String> item4 = new HashMap<>();
        item4.put("data", "推薦路線");
        arrayList.add(item4);
        HashMap<String, String> item5 = new HashMap<>();
        item5.put("data", "震動");
        arrayList.add(item5);
        HashMap<String, String> item6 = new HashMap<>();
        item6.put("data", "鈴聲");
        arrayList.add(item6);
    }

    //新增功能列表的動作
    private void handleImageClick(View view, int position) {
        Context context = view.getContext();

        if (position == 0) {
            // 跳到記事Fragment
            Note notesFragment = new Note();
            fragmentTransaction.replace(R.id.fragment_container, notesFragment);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        }
        else if (position == 1) {
            Toast.makeText(context, "資料庫組等你開發", Toast.LENGTH_SHORT).show();
        }
        else if (position == 2) {
            weatherAdviceHelper.getWeatherAdvice(view);
        }
        else if (position == 3) {
            Toast.makeText(context, "沒這功能，等你開發呢!親~", Toast.LENGTH_SHORT).show();
        }
        else if (position == 4) {
            Toast.makeText(context, "沒這功能，等你開發呢!親~", Toast.LENGTH_SHORT).show();
        }
        else if (position == 5) {
            Toast.makeText(context, "設定組等你開發", Toast.LENGTH_SHORT).show();
        }
        else if (position == 6) {
            Toast.makeText(context, "設定組等你開發", Toast.LENGTH_SHORT).show();
        }

        // 有新增功能列表的功能請在這新增他的動作
    }

    //這裡改變功能列表上的圖片
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // 获取对应位置的数据
        HashMap<String, String> item = arrayList.get(position);
        String data = item.get("data");
        holder.horecycleName.setText(data);

        // 根据数据内容设置不同的图标
        if (data.equals("記事")) {
            holder.horecycleimageView.setImageResource(R.drawable.note);
        }
        else if (data.equals("加入書籤")) {
            holder.horecycleimageView.setImageResource(R.drawable.anya062516);
        }
        else if (data.equals("天氣")) {
            holder.horecycleimageView.setImageResource(R.drawable.weather);
        }
        else if (data.equals("推薦路線")) {
            holder.horecycleimageView.setImageResource(R.drawable.anya062516);
        }
        else if (data.equals("震動")) {
            holder.horecycleimageView.setImageResource(R.drawable.vibrate);
        } else if (data.equals("鈴聲")) {
            holder.horecycleimageView.setImageResource(R.drawable.bell);
        }
        // 可以继续添加其他条目的处理逻辑
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView horecycleName;
        private ImageView horecycleimageView;

        public ViewHolder(View itemView) {
            super(itemView);
            horecycleName = itemView.findViewById(R.id.horecycle_Name);
            horecycleimageView = itemView.findViewById(R.id.horecycle_imageView);

            horecycleimageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    handleImageClick(v, position);
                }
            });

            horecycleName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                }
            });
        }

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recyclerviewitem_tool, parent, false);
        return new ViewHolder(view);
    }
    @Override
    public int getItemCount() {
        return arrayList.size();
    }
}
