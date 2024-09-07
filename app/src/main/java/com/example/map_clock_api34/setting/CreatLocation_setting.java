package com.example.map_clock_api34.setting;

import static android.content.Context.MODE_PRIVATE;

import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
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
import com.example.map_clock_api34.setting.SettingListdapter.ListdapterSetting;

import java.util.ArrayList;
import java.util.HashMap;

public class CreatLocation_setting extends Fragment {
    View rootView;
    private DrawerLayout drawerLayout;

    SharedViewModel sharedViewModel;

    RecyclerView recyclerViewRoute;         // 用於顯示地點設定的 RecyclerView
    ListdapterSetting listAdapterSetting;   // 用於管理 RecyclerView 項目的適配器

    ArrayList<HashMap<String, String>> arrayList = new ArrayList<>(); // 存儲地點的數據

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.setting_creatlocation, container, false);
        drawerLayout = getActivity().findViewById(R.id.drawerLayout);
        // 鎖定不能左滑漢堡選單
        if (drawerLayout != null) {
            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        }

        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);

        // 檢查是否需要顯示教學頁面
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("MyPrefs", MODE_PRIVATE);
        boolean isLoggedIn = sharedPreferences.getBoolean("settingLogin", false);

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("WhichPage", 3);
        editor.putBoolean("settingLogin", false);
        editor.apply();

        Fragment currentFragment = getActivity().getSupportFragmentManager().findFragmentById(R.id.fl_container);
        if (!isLoggedIn) {
            // 如果第一次進入，顯示教學頁面
            /*SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt("WhichPage", 3);
            //editor.putBoolean("settingLogin", true);
            editor.apply();*/

            TutorialFragment tutorialFragment = new TutorialFragment();
            FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
            transaction.hide(currentFragment);
            transaction.add(R.id.fl_container, tutorialFragment);
            transaction.addToBackStack(null);
            transaction.commit();
        }

        // 初始化 ActionBar 和 RecyclerView
        setupActionBar();
        setupRecyclerViews();

        return rootView;
    }

    // 初始化自定義 ActionBar
    private void setupActionBar() {
        CardView cardViewtitle = new CardView(requireContext());
        cardViewtitle.setLayoutParams(new CardView.LayoutParams(
                ActionBar.LayoutParams.WRAP_CONTENT,
                ActionBar.LayoutParams.WRAP_CONTENT));
        Drawable drawable = ContextCompat.getDrawable(requireContext(), R.drawable.cardviewtitle_shape);
        cardViewtitle.setBackground(drawable); // 設置背景為自定義形狀

        // 創建 LinearLayout 用於存放圖標和文字
        LinearLayout linearLayout = new LinearLayout(requireContext());
        linearLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        ));
        linearLayout.setOrientation(LinearLayout.HORIZONTAL); // 設置水平排列

        // 設置圖標 ImageView
        ImageView mark = new ImageView(requireContext());
        mark.setImageResource(R.drawable.vibrate); // 使用自定義圖標
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                80, // 寬度
                80 // 高度
        );
        params.setMargins(30, 10, 0, 10); // 設置外邊距
        mark.setPadding(10, 10, 10, 10); // 設置內邊距
        mark.setLayoutParams(params);

        // 創建標題 TextView
        TextView bookTitle = new TextView(requireContext());
        bookTitle.setText("地點設定"); // 設置標題
        bookTitle.setTextSize(15);
        bookTitle.setTextColor(getResources().getColor(R.color.green)); // 設置文字顏色
        bookTitle.setPadding(10, 10, 30, 10); // 設置內邊距

        // 將圖標和標題添加到 LinearLayout 中
        linearLayout.addView(mark);
        linearLayout.addView(bookTitle);
        cardViewtitle.addView(linearLayout); // 將 LinearLayout 添加到 CardView 中

        // 創建自定義返回按鈕
        ImageView returnButton = new ImageView(requireContext());
        returnButton.setImageResource(R.drawable.back); // 返回按鈕圖標
        LinearLayout.LayoutParams returnButtonParams = new LinearLayout.LayoutParams(
                100, // 寬度
                100 // 高度
        );
        returnButton.setLayoutParams(returnButtonParams);

        // 創建 ActionBar 的父容器 LinearLayout
        LinearLayout actionBarLayout = new LinearLayout(requireContext());
        actionBarLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        ));
        actionBarLayout.setOrientation(LinearLayout.HORIZONTAL); // 水平排列
        actionBarLayout.setWeightSum(1.0f); // 設置權重總和

        // 創建左側 LinearLayout 用於返回按鈕
        LinearLayout leftLayout = new LinearLayout(requireContext());
        leftLayout.setLayoutParams(new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.MATCH_PARENT,
                0.1f
        ));
        leftLayout.setOrientation(LinearLayout.HORIZONTAL);
        leftLayout.setGravity(Gravity.START | Gravity.CENTER_VERTICAL); // 設置對齊方式
        leftLayout.addView(returnButton);

        // 創建右側 LinearLayout 用於標題
        LinearLayout rightLayout = new LinearLayout(requireContext());
        rightLayout.setLayoutParams(new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.MATCH_PARENT,
                0.9f
        ));
        rightLayout.setOrientation(LinearLayout.HORIZONTAL);
        rightLayout.setGravity(Gravity.END | Gravity.CENTER_VERTICAL); // 設置對齊方式
        rightLayout.addView(cardViewtitle);

        // 將左右側的 LinearLayout 添加到父容器中
        actionBarLayout.addView(leftLayout);
        actionBarLayout.addView(rightLayout);

        // 設置 ActionBar
        androidx.appcompat.widget.Toolbar toolbar = getActivity().findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(null); // 隱藏漢堡菜單

        // 獲取並自定義 ActionBar
        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false); // 隱藏原有標題
            actionBar.setDisplayShowCustomEnabled(true); // 啟用自定義視圖
            actionBar.setCustomView(actionBarLayout, new ActionBar.LayoutParams(
                    ActionBar.LayoutParams.MATCH_PARENT,
                    ActionBar.LayoutParams.MATCH_PARENT
            ));
            actionBar.show(); // 顯示 ActionBar
        }

        // 設置返回按鈕的點擊事件
        returnButton.setOnClickListener(v -> getActivity().getSupportFragmentManager().popBackStack());
    }

    // 重置 RecyclerView 的數據
    private void RecycleViewReset() {
        arrayList.clear(); // 清空列表

        // ShareViewModel 取得數據並添加到 arrayList
        String shortLocationName;
        if (sharedViewModel.getLocationCount() != -1) {
            for (int j = 0; j <= sharedViewModel.getLocationCount(); j++) {
                HashMap<String, String> hashMap = new HashMap<>();
                shortLocationName = sharedViewModel.getDestinationName(j);

                // 如果地名超過 20 個字，則顯示前 20 個字並加上 "..."
                if (shortLocationName.length() > 20) {
                    hashMap.put("data", shortLocationName.substring(0, 20) + "...");
                } else {
                    hashMap.put("data", shortLocationName);
                }
                arrayList.add(hashMap); // 添加路線資料
            }
        }
        // 更新適配器
        if (listAdapterSetting != null) {
            listAdapterSetting.notifyDataSetChanged();
        }
    }

    // 初始化 RecyclerView 和相關設置
    private void setupRecyclerViews() {
        recyclerViewRoute = rootView.findViewById(R.id.recycleViewset); // 找到 RecyclerView
        recyclerViewRoute.setLayoutManager(new LinearLayoutManager(getActivity())); // 設置布局管理器
        recyclerViewRoute.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL)); // 添加分隔線
        listAdapterSetting = new ListdapterSetting(requireContext(), arrayList, sharedViewModel, false); // 創建適配器
        recyclerViewRoute.setAdapter(listAdapterSetting); // 設置適配器
    }

    @Override
    public void onPause() {
        super.onPause();

        // 獲取並恢復原有 ActionBar 樣式
        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowCustomEnabled(false); // 取消自定義視圖
            actionBar.setCustomView(null);
            actionBar.setDisplayShowTitleEnabled(true); // 恢復標題
            actionBar.show();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        RecycleViewReset(); // 重置 RecyclerView 的數據
        setupActionBar();   // 重新設置 ActionBar
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
