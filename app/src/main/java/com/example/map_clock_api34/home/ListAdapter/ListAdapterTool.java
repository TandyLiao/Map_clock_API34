package com.example.map_clock_api34.home.ListAdapter;

import android.view.View;
import android.view.ViewGroup;

import android.content.Context;
import android.content.DialogInterface;
import android.text.InputType;

import android.view.LayoutInflater;

import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.example.map_clock_api34.BusAdvice.busMapsFragment;
import com.example.map_clock_api34.R;
import com.example.map_clock_api34.SharedViewModel;
import com.example.map_clock_api34.Weather.WeatherAdviceHelper;
import com.example.map_clock_api34.Weather.WeatherService;
import com.example.map_clock_api34.Weather.WheatherFragment;
import com.example.map_clock_api34.book.BookDatabaseHelper;
import com.example.map_clock_api34.note.Note;
import com.example.map_clock_api34.setting.CreatLocation_setting;

import java.util.ArrayList;
import java.util.HashMap;



public class ListAdapterTool extends RecyclerView.Adapter<ListAdapterTool.ViewHolder> {

    private ArrayList<HashMap<String, String>> arrayList;
    private FragmentTransaction fragmentTransaction;
    private WeatherAdviceHelper weatherAdviceHelper;
    private SharedViewModel sharedViewModel;
    private BookDatabaseHelper dbBookHelper;

    public ListAdapterTool(FragmentTransaction fragmentTransaction, SharedViewModel sharedViewModel, WeatherService weatherService, Context context) {
        this.fragmentTransaction = fragmentTransaction;
        this.arrayList = new ArrayList<>();
        this.weatherAdviceHelper = new WeatherAdviceHelper(sharedViewModel, weatherService, context);
        this.sharedViewModel = sharedViewModel;
        this.dbBookHelper = new BookDatabaseHelper(context);
        initData();
    }

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
        item5.put("data", "地點設定");
        arrayList.add(item5);
        HashMap<String, String> item6 = new HashMap<>();
        item6.put("data", "哇哭哇哭");
        arrayList.add(item6);
    }

    private void handleImageClick(View view, int position) {
        Context context = view.getContext();

        if (position == 0) {
            if (sharedViewModel.getLocationCount() == -1) {
                Toast.makeText(context, "你還沒有選擇地點喔", Toast.LENGTH_SHORT).show();
                return;
            }
            Note notesFragment = new Note();
            fragmentTransaction.replace(R.id.home_fragment_container, notesFragment);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        }
        else if (position == 1) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.CustomAlertDialog);
            builder.setTitle("請輸入書籤名稱");

            // 设置输入框
            final EditText input = new EditText(context);
            input.setInputType(InputType.TYPE_CLASS_TEXT);
            input.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT)); // 设置输入框布局参数
            builder.setView(input);

            // 设置确定按钮
            builder.setPositiveButton("確定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String bookmarkName = input.getText().toString();
                    if (!bookmarkName.isEmpty()) {
                        // 处理确定按钮点击事件
                        dbBookHelper.addBookmark(bookmarkName);
                        Toast.makeText(context, "書籤 '" + bookmarkName + "' 已添加", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context, "請輸入書籤名稱", Toast.LENGTH_SHORT).show();
                    }
                }
            });

            // 设置取消按钮
            builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // 处理取消按钮点击事件
                    dialog.cancel();
                }
            });

            // 创建并显示对话框
            AlertDialog dialog = builder.create();
            dialog.show();
        }



        else if (position == 2) {
            if (sharedViewModel.getLocationCount() == -1) {
                Toast.makeText(context, "你還沒有選擇地點喔", Toast.LENGTH_SHORT).show();
                return;
            }
            WheatherFragment wheatherFragment = new WheatherFragment();
            fragmentTransaction.replace(R.id.home_fragment_container, wheatherFragment);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        } else if (position == 3) {
            if (sharedViewModel.getLocationCount() == -1) {
                Toast.makeText(context, "你還沒有選擇地點喔", Toast.LENGTH_SHORT).show();
                return;
            }
            busMapsFragment busFragment = new busMapsFragment();
            fragmentTransaction.replace(R.id.home_fragment_container, busFragment);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        } else if (position == 4) {
            CreatLocation_setting createlocation_setting = new CreatLocation_setting();
            fragmentTransaction.replace(R.id.home_fragment_container, createlocation_setting);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();

        } else if (position == 5) {
            Toast.makeText(context, "設定組等你開發", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        HashMap<String, String> item = arrayList.get(position);
        String data = item.get("data");
        holder.horecycleName.setText(data);

        switch (data) {
            case "記事":
                holder.horecycleimageView.setImageResource(R.drawable.note);
                break;
            case "加入書籤":
                holder.horecycleimageView.setImageResource(R.drawable.addbookmark);
                break;
            case "天氣":
                holder.horecycleimageView.setImageResource(R.drawable.weather);
                break;
            case "推薦路線":
                holder.horecycleimageView.setImageResource(R.drawable.route_well2);
                break;
            case "地點設定":
                holder.horecycleimageView.setImageResource(R.drawable.vibrate);
                break;
            case "哇哭哇哭":
                holder.horecycleimageView.setImageResource(R.drawable.bell);
                break;
        }
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
                    handleImageClick(v, position);
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
