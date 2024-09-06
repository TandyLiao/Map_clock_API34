package com.example.map_clock_api34.note;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.map_clock_api34.R;
import com.example.map_clock_api34.SharedViewModel;
import com.example.map_clock_api34.home.CreateLocation;
import com.example.map_clock_api34.home.ListAdapter.ListAdapterRoute;
import com.example.map_clock_api34.home.SelectPlace;

import java.util.ArrayList;
import java.util.HashMap;

public class Note extends Fragment {

    View rootView;
    View overlayView;

    SharedViewModel sharedViewModel; // 用於共享數據的 ViewModel
    RecyclerView recyclerViewRoute; // 顯示路線的 RecyclerView
    ListAdapterRoute listAdapterRoute; // 路線表的適配器
    Button btnreset; // 重置按鈕
    Button btnsure; // 確認按鈕

    ArrayList<HashMap<String, String>> arrayList = new ArrayList<>(); // 用於存放路線資料的 ArrayList

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.note_fragment_note, container, false); // 加載布局

        // 初始化按鈕並設置點擊事件
        btnreset = rootView.findViewById(R.id.btn_reset);
        btnreset.setOnClickListener(v -> {
            ShowPopupWindow(); // 點擊時顯示重置確認窗口
        });

        btnsure = rootView.findViewById(R.id.btn_sure);
        btnsure.setOnClickListener(v -> {
            CreateLocation createLocation = new CreateLocation(); // 點擊確認後跳轉到 CreateLocation
            FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fl_container, createLocation);
            transaction.commit();
        });

        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);

        updateResetButtonState(); // 更新重置按鈕狀態
        setupActionBar(); // 設置自定義 ActionBar
        setupRecyclerViews(); // 設置 RecyclerView

        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 再次設置按鈕點擊事件，確保在視圖創建後可以正常工作
        btnreset = view.findViewById(R.id.btn_reset);
        btnreset.setOnClickListener(v -> {
            ShowPopupWindow();
        });

        btnsure = view.findViewById(R.id.btn_sure);
        btnsure.setOnClickListener(v -> {
            getActivity().getSupportFragmentManager().popBackStack(); // 返回上一個 Fragment
        });

    }

    // 重置所有的備註
    private void resetAllNotes() {
        int locationCount = sharedViewModel.getLocationCount();
        if (locationCount >= 0) {
            for (int i = 0; i <= locationCount; i++) {
                sharedViewModel.setNote("", i); // 將所有備註設置為空字串
            }
        }
    }

    // 顯示重置確認窗口的設定
    private void ShowPopupWindow() {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.popupwindow_reset_button, null, false);
        PopupWindow popupWindow = new PopupWindow(view, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        popupWindow.setWidth(700); // 設置彈出窗口的寬度
        popupWindow.setFocusable(false); // 禁止點擊外部關閉窗口
        popupWindow.setOutsideTouchable(false);
        popupWindow.showAtLocation(rootView, Gravity.CENTER, 0, 0); // 將彈出窗口顯示在畫面中央
        popupWindow.setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT)); // 設置背景透明

        // 在底部添加覆蓋層，防止點擊其他區域
        overlayView = new View(getContext());
        overlayView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        overlayView.setBackgroundColor(ContextCompat.getColor(getContext(), android.R.color.transparent));
        overlayView.setClickable(true);
        ((ViewGroup) rootView).addView(overlayView);

        // 設置取消按鈕
        Button BTNPopup = view.findViewById(R.id.PopupCancel);
        BTNPopup.setOnClickListener(v -> {
            popupWindow.dismiss(); // 關閉彈出窗口
            removeOverlayView(); // 移除覆蓋層
        });

        // 設置確認按鈕，重置備註
        Button btnsure = view.findViewById(R.id.Popupsure);
        btnsure.setOnClickListener(v -> {
            resetAllNotes();            // 重置所有備註
            RecycleViewReset();         // 重置 RecyclerView 的資料
            updateResetButtonState();   // 更新重置按鈕的狀態
            recyclerViewRoute.setAdapter(listAdapterRoute); // 更新 RecyclerView 的適配器
            removeOverlayView();        // 移除覆蓋層
            popupWindow.dismiss();      // 關閉彈出窗口
        });
    }

    // 移除覆蓋層
    private void removeOverlayView() {
        if (overlayView != null && overlayView.getParent() != null) {
            ((ViewGroup) overlayView.getParent()).removeView(overlayView);
            overlayView = null;
        }
    }

    // 更新重置按鈕的狀態（是否可點擊）
    private void updateResetButtonState() {
        if (sharedViewModel.hasNotes()) {
            // 設置按鈕可點擊狀態
            btnreset.setEnabled(true);
            btnreset.setTextColor(ContextCompat.getColor(requireContext(), R.color.green)); // 更新按鈕文字顏色
            btnreset.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.btn_additem)); // 設置啟用狀態的背景
        } else {
            // 設置按鈕不可點擊狀態
            btnreset.setEnabled(false);
            btnreset.setTextColor(ContextCompat.getColor(requireContext(), R.color.lightgreen)); // 更新按鈕文字顏色
            btnreset.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.btn_unclickable)); // 設置禁用狀態的背景
        }
    }

    // 設置自定義的 ActionBar
    private void setupActionBar() {
        // 創建 CardView 作為自定義標題容器
        CardView cardViewtitle = new CardView(requireContext());
        cardViewtitle.setLayoutParams(new CardView.LayoutParams(
                ActionBar.LayoutParams.WRAP_CONTENT,
                ActionBar.LayoutParams.WRAP_CONTENT));
        Drawable drawable = ContextCompat.getDrawable(requireContext(), R.drawable.cardviewtitle_shape);
        cardViewtitle.setBackground(drawable);

        // 創建 LinearLayout 來放置圖標和標題文字
        LinearLayout linearLayout = new LinearLayout(requireContext());
        linearLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        ));
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);

        // 添加圖標
        ImageView mark = new ImageView(requireContext());
        mark.setImageResource(R.drawable.note);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                100, // 設置寬度
                100 // 設置高度
        );
        params.setMarginStart(10); // 設置圖標左側間距
        mark.setPadding(10, 10, 10, 10);
        mark.setLayoutParams(params);

        // 添加標題文字
        TextView bookTitle = new TextView(requireContext());
        bookTitle.setText("記事");
        bookTitle.setTextSize(15);
        bookTitle.setTextColor(getResources().getColor(R.color.green)); // 設置文字顏色
        bookTitle.setPadding(10, 10, 10, 10); // 設置內邊距

        // 將圖標和標題添加到 LinearLayout 中
        linearLayout.addView(mark);
        linearLayout.addView(bookTitle);
        cardViewtitle.addView(linearLayout);

        // 創建返回按鈕
        ImageView returnButton = new ImageView(requireContext());
        returnButton.setImageResource(R.drawable.back);
        LinearLayout.LayoutParams returnButtonParams = new LinearLayout.LayoutParams(
                100, // 設置寬度
                100 // 設置高度
        );
        returnButton.setLayoutParams(returnButtonParams);

        // 創建 ActionBar 的父 LinearLayout
        LinearLayout actionBarLayout = new LinearLayout(requireContext());
        actionBarLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        ));
        actionBarLayout.setOrientation(LinearLayout.HORIZONTAL);
        actionBarLayout.setWeightSum(1.0f);

        // 子 LinearLayout 用於放置返回按鈕
        LinearLayout leftLayout = new LinearLayout(requireContext());
        leftLayout.setLayoutParams(new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.MATCH_PARENT,
                0.1f
        ));
        leftLayout.setOrientation(LinearLayout.HORIZONTAL);
        leftLayout.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
        leftLayout.addView(returnButton);

        // 子 LinearLayout 用於放置標題
        LinearLayout rightLayout = new LinearLayout(requireContext());
        rightLayout.setLayoutParams(new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.MATCH_PARENT,
                0.9f
        ));
        rightLayout.setOrientation(LinearLayout.HORIZONTAL);
        rightLayout.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
        rightLayout.addView(cardViewtitle);

        // 將兩個子 LinearLayout 添加到父 LinearLayout
        actionBarLayout.addView(leftLayout);
        actionBarLayout.addView(rightLayout);

        // 設置自定義的 Toolbar
        androidx.appcompat.widget.Toolbar toolbar = (androidx.appcompat.widget.Toolbar) getActivity().findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(null); // 隱藏漢堡圖標

        // 獲取 ActionBar 並設置自定義視圖
        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false); // 隱藏原有標題
            actionBar.setDisplayShowCustomEnabled(true);
            actionBar.setCustomView(actionBarLayout, new ActionBar.LayoutParams(
                    ActionBar.LayoutParams.MATCH_PARENT,
                    ActionBar.LayoutParams.MATCH_PARENT
            ));
            actionBar.show();
        }

        // 返回按鈕的點擊事件
        returnButton.setOnClickListener(v -> getActivity().getSupportFragmentManager().popBackStack());
    }

    // 重置 RecyclerView 的內容
    private void RecycleViewReset() {
        arrayList.clear(); // 清除現有資料
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
        listAdapterRoute.notifyDataSetChanged(); // 更新適配器
    }

    // 開啟 NoteEnterContent Fragment，用於編輯備註
    private void openWritingFragment() {
        ChangeNote changeNote = new ChangeNote();
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fl_container, changeNote);
        transaction.addToBackStack(null); // 支援返回操作
        transaction.commit();
    }

    // 初始化 RecyclerView 和相關設置
    private void setupRecyclerViews() {
        recyclerViewRoute = rootView.findViewById(R.id.recycleViewnote);
        recyclerViewRoute.setLayoutManager(new LinearLayoutManager(getActivity())); // 設置 RecyclerView 的佈局管理器
        recyclerViewRoute.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL)); // 設置分隔線
        listAdapterRoute = new ListAdapterRoute(arrayList, sharedViewModel, false); // 創建適配器
        listAdapterRoute.setOnItemClickListener(new ListAdapterRoute.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                sharedViewModel.setPosition(position); // 設置當前選中的位置
                openWritingFragment(); // 開啟編輯備註的 Fragment
            }
        });

        recyclerViewRoute.setAdapter(listAdapterRoute); // 將適配器綁定到 RecyclerView
    }

    // 當 Fragment 恢復時，重新加載 ActionBar 和 RecyclerView
    @Override
    public void onResume() {
        super.onResume();
        RecycleViewReset(); // 重置 RecyclerView
        setupActionBar(); // 重新設置 ActionBar
    }

    // 當 Fragment 暫停時，恢復原有的 ActionBar 樣式
    public void onPause() {
        super.onPause();
        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowCustomEnabled(false); // 取消自定義視圖
            actionBar.setCustomView(null);
            actionBar.setDisplayShowTitleEnabled(true); // 恢復標題
            actionBar.show();
        }
    }
}
