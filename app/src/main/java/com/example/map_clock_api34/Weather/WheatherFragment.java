package com.example.map_clock_api34.Weather;

import static android.content.Context.MODE_PRIVATE;

import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.map_clock_api34.R;
import com.example.map_clock_api34.SharedViewModel;
import com.example.map_clock_api34.TutorialFragment;
import com.example.map_clock_api34.Weather.WeatherListAdapter.ListAdapterWeather;

import java.util.ArrayList;
import java.util.HashMap;

public class WheatherFragment extends Fragment {

    View rootView;
    private DrawerLayout drawerLayout;

    private SharedViewModel sharedViewModel;  // 用來管理共用數據的 ViewModel

    private final WeatherService weatherService = new WeatherService();  // 用來獲取天氣資料的服務類

    private ListAdapterWeather listAdapterWeather;  // RecyclerView 的 Adapter，負責展示天氣數據
    private final ArrayList<HashMap<String, String>> arrayList = new ArrayList<>();  // 用來存放天氣資訊的 ArrayList

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.weather_fragment_weather, container, false);

        // 檢查是否需要顯示教學頁面
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("MyPrefs", MODE_PRIVATE);
        boolean isLoggedIn = sharedPreferences.getBoolean("WeatherLogin", false);

        if (!isLoggedIn) {
            // 如果第一次進入，顯示教學頁面
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt("WhichPage", 6);
            editor.putBoolean("WeatherLogin", true);
            editor.apply();
            TutorialFragment tutorialFragment = new TutorialFragment();
            FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fl_container, tutorialFragment);
            transaction.addToBackStack(null);
            transaction.commit();
        }

        drawerLayout = getActivity().findViewById(R.id.drawerLayout);
        // 鎖定不能左滑漢堡選單
        if (drawerLayout != null) {
            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        }

        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);

        // 初始化 ActionBar 和 RecyclerView
        setupActionBar();
        setupRecyclerViews();

        return rootView;
    }

    // 設定 ActionBar，並且自訂標題和返回按鈕
    private void setupActionBar() {
        CardView cardViewtitle = new CardView(requireContext());
        cardViewtitle.setLayoutParams(new CardView.LayoutParams(
                ActionBar.LayoutParams.WRAP_CONTENT,
                ActionBar.LayoutParams.WRAP_CONTENT));
        Drawable drawable = ContextCompat.getDrawable(requireContext(), R.drawable.cardviewtitle_shape);
        cardViewtitle.setBackground(drawable);

        // 建立 LinearLayout 用來放置圖案和文字
        LinearLayout linearLayout = new LinearLayout(requireContext());
        linearLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        ));
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);

        // ImageView 放置圖案
        ImageView mark = new ImageView(requireContext());
        mark.setImageResource(R.drawable.weather);  // 使用自訂的天氣圖案
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                100, // 設定寬度
                100  // 設定高度
        );
        params.setMarginStart(10);  // 設定左邊距
        mark.setLayoutParams(params);

        // 創建 TextView 顯示標題 "天氣"
        TextView bookTitle = new TextView(requireContext());
        bookTitle.setText("天氣");
        bookTitle.setTextSize(15);
        bookTitle.setTextColor(getResources().getColor(R.color.green));  // 設定文字顏色
        bookTitle.setPadding(10, 10, 10, 10);  // 設定內邊距

        // 將 ImageView 和 TextView 添加到 LinearLayout
        linearLayout.addView(mark);
        linearLayout.addView(bookTitle);
        cardViewtitle.addView(linearLayout);

        // 建立返回按鈕的 ImageView
        ImageView returnButton = new ImageView(requireContext());
        returnButton.setImageResource(R.drawable.back);  // 使用自訂的返回圖案
        LinearLayout.LayoutParams returnButtonParams = new LinearLayout.LayoutParams(
                100,  // 設定寬度
                100   // 設定高度
        );
        returnButton.setLayoutParams(returnButtonParams);

        // 建立 ActionBar 的父 LinearLayout
        LinearLayout actionBarLayout = new LinearLayout(requireContext());
        actionBarLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        ));
        actionBarLayout.setOrientation(LinearLayout.HORIZONTAL);
        actionBarLayout.setWeightSum(1.0f);

        // 子 LinearLayout 用來放返回按鈕
        LinearLayout leftLayout = new LinearLayout(requireContext());
        leftLayout.setLayoutParams(new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.MATCH_PARENT,
                0.1f
        ));
        leftLayout.setOrientation(LinearLayout.HORIZONTAL);
        leftLayout.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
        leftLayout.addView(returnButton);

        // 子 LinearLayout 用來放標題
        LinearLayout rightLayout = new LinearLayout(requireContext());
        rightLayout.setLayoutParams(new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.MATCH_PARENT,
                0.9f
        ));
        rightLayout.setOrientation(LinearLayout.HORIZONTAL);
        rightLayout.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
        rightLayout.addView(cardViewtitle);

        // 將子 LinearLayout 添加到父 LinearLayout
        actionBarLayout.addView(leftLayout);
        actionBarLayout.addView(rightLayout);

        // 設定 ToolBar
        androidx.appcompat.widget.Toolbar toolbar = (androidx.appcompat.widget.Toolbar) getActivity().findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(null);  // 隱藏漢堡選單

        // 獲取 ActionBar，並設定自訂的 View
        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false);  // 隱藏原有的標題
            actionBar.setDisplayShowCustomEnabled(true);
            actionBar.setCustomView(actionBarLayout, new ActionBar.LayoutParams(
                    ActionBar.LayoutParams.MATCH_PARENT,
                    ActionBar.LayoutParams.MATCH_PARENT
            ));
            actionBar.show();
        }

        // 返回按鈕的點擊事件
        returnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });
    }

    // 重設 RecyclerView 的資料
    private void RecycleViewReset() {
        arrayList.clear();  // 清除原本的資料
        String shortLocationName;

        // 根據 SharedViewModel 的資料更新 RecyclerView
        if (sharedViewModel.getLocationCount() != -1) {
            for (int j = 0; j <= sharedViewModel.getLocationCount(); j++) {
                HashMap<String, String> hashMap = new HashMap<>();
                shortLocationName = sharedViewModel.getDestinationName(j);

                // 如果地名大於 20 字，使用 "..." 來代替超出部分
                if (shortLocationName.length() > 20) {
                    hashMap.put("data", shortLocationName.substring(0, 20) + "...");
                } else {
                    hashMap.put("data", shortLocationName);
                }
                arrayList.add(hashMap);  // 將資料添加到 ArrayList 中
            }
        }

        listAdapterWeather.notifyDataSetChanged();  // 通知 Adapter 更新資料
    }

    // 初始化 RecyclerView 和設定 Adapter
    private void setupRecyclerViews() {
        // 用來顯示天氣資訊的 RecyclerView
        RecyclerView recyclerViewWeather = rootView.findViewById(R.id.recycleView_wheather);  // 設定 RecyclerView
        recyclerViewWeather.setLayoutManager(new LinearLayoutManager(getActivity()));  // 使用 LinearLayoutManager 來顯示列表
        recyclerViewWeather.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));  // 添加分隔線

        // 初始化 ListAdapterWeather 並設置給 RecyclerView
        listAdapterWeather = new ListAdapterWeather(arrayList, sharedViewModel, weatherService, getActivity());
        recyclerViewWeather.setAdapter(listAdapterWeather);
    }

    @Override
    public void onResume() {
        super.onResume();
        RecycleViewReset();  // 在恢復時刷新 RecyclerView
        setupActionBar();
    }

    // 在 Fragment 被暫停時，將 ActionBar 恢復為顯示標題的模式
    public void onPause() {
        super.onPause();

        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowCustomEnabled(false);
            actionBar.setCustomView(null);
            actionBar.setDisplayShowTitleEnabled(true);  // 恢復顯示標題
            actionBar.show();
        }
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();

        drawerLayout = getActivity().findViewById(R.id.drawerLayout);
        // 解鎖 Drawer 以便其他頁面正常使用
        if (drawerLayout != null) {
            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        }
    }
}
