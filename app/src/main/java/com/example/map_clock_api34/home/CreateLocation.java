package com.example.map_clock_api34.home;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.map_clock_api34.Database.AppDatabaseHelper;
import com.example.map_clock_api34.SharedViewModel;

import android.content.ContentValues;
import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
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
import com.example.map_clock_api34.Weather.WeatherService;
import com.example.map_clock_api34.home.ListAdapter.ListAdapterRoute;
import com.example.map_clock_api34.home.ListAdapter.ListAdapterTool;
import com.example.map_clock_api34.home.ListAdapter.RecyclerViewActionHome;

import java.util.ArrayList;
import java.util.HashMap;

import com.example.map_clock_api34.Database.AppDatabaseHelper.LocationTable;
import com.example.map_clock_api34.Database.AppDatabaseHelper.HistoryTable;

import java.text.SimpleDateFormat;
import java.util.Date;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class CreateLocation extends Fragment {

    private AppDatabaseHelper dbHelper;
    String names;
    String Historynames;
    double latitudes;
    double longitudes;
    String uniqueID;

    ActionBar actionBar;
    private Toolbar toolbar;
    private ActionBarDrawerToggle toggle;
    private DrawerLayout drawerLayout;

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    View rootView;
    View overlayView;
    RecyclerView recyclerViewRoute;
    RecyclerView recyclerViewTool;
    RecyclerViewActionHome recyclerViewActionHome;
    ListAdapterRoute listAdapterRoute;
    //獨立出來是因為要設置不可點擊狀態
    Button btnReset;
    SharedViewModel sharedViewModel;
    WeatherService weatherService = new WeatherService();

    ArrayList<HashMap<String, String>> arrayList = new ArrayList<>();

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.home_fragment_creatlocation, container, false);

        dbHelper = new AppDatabaseHelper(requireContext());
        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);

        //初始化ActionBar
        setupActionBar();
        //初始化漢堡選單
        setupNavigationDrawer();
        //初始化按鈕
        setupButtons();
        //初始化路線表和功能表
        setupRecyclerViews();

        //換頁回來再召喚漢堡選單
        if (getActivity() != null) {
            drawerLayout = getActivity().findViewById(R.id.drawerLayout);
            toolbar = requireActivity().findViewById(R.id.toolbar);
        }


        return rootView;
    }


    //初始化按鈕(包含定位請求)
    private void setupButtons() {
        //新增地點按鈕初始化
        Button btnAddItem = rootView.findViewById(R.id.btn_addItem);
        btnAddItem.setOnClickListener(v -> {
            //如果他沒同意定位需求則跳else叫他打開
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                if (sharedViewModel.getLocationCount() < 6) {
                    openSelectPlaceFragment();
                }
            } else {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
                Toast.makeText(getActivity(), "請開啟定位權限", Toast.LENGTH_SHORT).show();
            }
        });
        //重置按鈕初始化
        btnReset = rootView.findViewById(R.id.btn_reset);
        btnReset.setOnClickListener(v -> ShowPopupWindow());
        Button btnMapping = rootView.findViewById(R.id.btn_sure);
        btnMapping.setOnClickListener(v -> {
            //如有選擇地點就導航，沒有就跳提醒，加上吳俊廷的匯入資料庫的程式
            if (sharedViewModel.getLocationCount() >= 0) {
                openStartMappingFragment();

                Historynames = sharedViewModel.getDestinationName(0) + "->" + sharedViewModel.getDestinationName(sharedViewModel.getLocationCount());
                saveInDB();
                saveInHistoryDB();

            } else {
                Toast.makeText(getActivity(), "你還沒有選擇地點", Toast.LENGTH_SHORT).show();
            }
        });
        /*吳俊廷的實驗區。都別給我碰(#`Д´)ﾉ
    public void insertDataToDatabase() {
        //Get data from SharedViewModel
        String[] names = sharedViewModel.getDestinationNameArray();
        double[] latitudes = sharedViewModel.getLatitudeArray();
        double[] longitudes = sharedViewModel.getLongitudeArray();
        //Open database in write mode
        SQLiteDatabase db = this.getWritableDatabase();
        //Insert data into the database
        for (int i = 0; i < names.length; i++) {
            if (names[i] != null && latitudes[i] != 0 && longitudes[i] != 0) {
                ContentValues values = new ContentValues();
                values.put(LocationTable.COLUMN_PLACE_NAME, names[i]);
                values.put(LocationTable.COLUMN_LATITUDE, latitudes[i]);
                values.put(LocationTable.COLUMN_LONGITUDE, longitudes[i]);
                db.insert(LocationTable.TABLE_NAME, null, values);
            }
        }
        Close the database
        db.close();
    }
    public class Example {
    public static void main(String[] args) {
        long currentTimeMillis = System.currentTimeMillis();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String formattedDate = sdf.format(new Date(currentTimeMillis));
        System.out.println("Current time: " + formattedDate);
    }
}
    */

    }

    private void saveInHistoryDB() {

        try {
            SQLiteDatabase writeDB = dbHelper.getWritableDatabase();
            SQLiteDatabase readDB = dbHelper.getReadableDatabase();

            Cursor cursor = readDB.rawQuery("SELECT location_id FROM location WHERE alarm_name=?", new String[]{uniqueID});


            long currentTimeMillis = System.currentTimeMillis();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String formattedDate = sdf.format(new Date(currentTimeMillis));

            while (cursor.moveToNext()) {
                if (Historynames != null) {
                    ContentValues values = new ContentValues();
                    values.put(HistoryTable.COLUMN_ALARM_NAME, Historynames);
                    values.put(HistoryTable.COLUMN_LOCATION_ID, cursor.getString(0));
                    values.put(HistoryTable.COLUMN_START_TIME, formattedDate);
                    writeDB.insert(HistoryTable.TABLE_NAME, null, values);
                }
            }

            writeDB.close();
            readDB.close();
        } catch (Exception e) {
            Log.d("DBProblem", e.getMessage());
        }
    }

    private void saveInDB() {
        uniqueID = UUID.randomUUID().toString();
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        for (int i = 0; i <= sharedViewModel.getLocationCount(); i++) {
            String name = sharedViewModel.getDestinationName(i);
            double latitude = sharedViewModel.getLatitude(i);
            double longitude = sharedViewModel.getLongitude(i);
            if (name != null) {
                ContentValues values = new ContentValues();
                values.put(LocationTable.COLUMN_PLACE_NAME, name);
                values.put(LocationTable.COLUMN_LATITUDE, latitude);
                values.put(LocationTable.COLUMN_LONGITUDE, longitude);
                values.put(LocationTable.COLUMN_ALARM_NAME, uniqueID);

                db.insert(LocationTable.TABLE_NAME, null, values);
            }
        }
        db.close();
    }

    //打開選地點頁面
    private void openSelectPlaceFragment() {
        SelectPlace mapFragment = new SelectPlace();
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.home_fragment_container, mapFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    //打開導航頁面
    private void openStartMappingFragment() {
        StartMapping StartMapping = new StartMapping();
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.home_fragment_container, StartMapping);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    //初始化漢堡選單
    private void setupNavigationDrawer() {
    }

    //ActionBar初始設定
    private void setupActionBar() {
        actionBar = ((AppCompatActivity) requireActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
            actionBar.setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(getActivity(), R.color.lightgreen)));
        }
        CardView cardViewtitle = new CardView(requireContext());
        cardViewtitle.setLayoutParams(new CardView.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT,
                ActionBar.LayoutParams.MATCH_PARENT));
        Drawable drawable = ContextCompat.getDrawable(requireContext(), R.drawable.cardviewtitle_shape);
        cardViewtitle.setBackground(drawable);
        //建立LinearLayout在CardView等等放圖案和文字
        LinearLayout linearLayout = new LinearLayout(requireContext());
        linearLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        ));
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);
        //設置右上角的小圖示
        ImageView bookmark = new ImageView(requireContext());
        bookmark.setImageResource(R.drawable.route);
        bookmark.setPadding(10, 10, 5, 10);//設定icon邊界
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                100, // 设置宽度为 100 像素
                100 // 设置高度为 100 像素
        );
        params.setMarginStart(10); // 设置左边距
        bookmark.setLayoutParams(params);
        // 創建右上角的名字
        TextView bookTitle = new TextView(requireContext());
        bookTitle.setText("路線規劃");
        bookTitle.setTextSize(15);
        bookTitle.setTextColor(getResources().getColor(R.color.green)); // 更改文字颜色
        bookTitle.setPadding(10, 10, 10, 10); // 设置内边距
        linearLayout.addView(bookmark);
        linearLayout.addView(bookTitle);
        cardViewtitle.addView(linearLayout);
        // 將cardview新增到actionBar
        actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false); // 隐藏原有的標題
            actionBar.setDisplayShowCustomEnabled(true);
            actionBar.setCustomView(cardViewtitle, new ActionBar.LayoutParams(
                    ActionBar.LayoutParams.WRAP_CONTENT,
                    ActionBar.LayoutParams.WRAP_CONTENT,
                    Gravity.END));
            actionBar.show();
        }
    }

    //初始化設定表和功能表
    private void setupRecyclerViews() {
        // 初始化路線的表
        recyclerViewRoute = rootView.findViewById(R.id.recycleViewRoute);
        recyclerViewRoute.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerViewRoute.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));
        listAdapterRoute = new ListAdapterRoute(arrayList, sharedViewModel, true); // 啟用拖動功能
        recyclerViewRoute.setAdapter(listAdapterRoute);

        // 讓路線表可以交換、刪除...等動作
        recyclerViewActionHome = new RecyclerViewActionHome();
        recyclerViewActionHome.attachToRecyclerView(recyclerViewRoute, arrayList, listAdapterRoute, sharedViewModel, getActivity(), btnReset);

        // 初始化下面工具列的表
        recyclerViewTool = rootView.findViewById(R.id.recycleViewTool);
        recyclerViewTool.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
        // 由於ListAdapter獨立出去了，所以要創建換頁的動作並傳給他
        FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
        ListAdapterTool listAdapterTool = new ListAdapterTool(fragmentTransaction, sharedViewModel, weatherService, getActivity());
        recyclerViewTool.setAdapter(listAdapterTool);
    }

    //每次回到路線規劃都會重製路線表，不然會疊加
    private void RecycleViewReset() {
        //清除原本的表
        arrayList.clear();
        String shortLocationName;
        if (sharedViewModel.getLocationCount() != -1) {
            for (int j = 0; j <= sharedViewModel.getLocationCount(); j++) {
                HashMap<String, String> hashMap = new HashMap<>();
                shortLocationName = sharedViewModel.getDestinationName(j);
                //如果地名大於20字，後面都用...代替
                if (shortLocationName.length() > 20) {
                    hashMap.put("data", shortLocationName.substring(0, 20) + "...");
                } else {
                    hashMap.put("data", shortLocationName);
                }
                //重新加回路線表
                arrayList.add(hashMap);
            }
        }
        //套用更新
        listAdapterRoute.notifyDataSetChanged();
        //設置重置按鈕的顏色和觸及狀態
        updateResetButtonState();
    }

    //按重置紐後PopupWindow跳出來的設定
    private void ShowPopupWindow() {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.popupwindow_reset_button, null, false);
        PopupWindow popupWindow = new PopupWindow(view, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        popupWindow.setWidth(700);
        popupWindow.setFocusable(false);
        popupWindow.setOutsideTouchable(false);
        //讓PopupWindow顯示出來的關鍵句
        popupWindow.showAtLocation(rootView, Gravity.CENTER, 0, 0);
        popupWindow.setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        //疊加View在底下，讓她不會按到底層就跳掉
        overlayView = new View(getContext());
        overlayView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        overlayView.setBackgroundColor(ContextCompat.getColor(getContext(), android.R.color.transparent));
        overlayView.setClickable(true);
        ((ViewGroup) rootView).addView(overlayView);
        Button BTNPopup = (Button) view.findViewById(R.id.PopupCancel);
        BTNPopup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
                //移除疊加在底下防止點擊其他區域的View
                removeOverlayView();
            }
        });
        Button btnsure = (Button) view.findViewById(R.id.Popupsure);
        btnsure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                while (sharedViewModel.getLocationCount() >= 0) {
                    arrayList.remove(sharedViewModel.getLocationCount());
                    sharedViewModel.setLocationCount();
                }
                recyclerViewRoute.setAdapter(listAdapterRoute);
                //改變重置按鈕狀態
                updateResetButtonState();
                //移除疊加在底下防止點擊其他區域的View
                removeOverlayView();
                popupWindow.dismiss();
            }
        });
    }

    //把疊加在底層的View刪掉
    private void removeOverlayView() {
        if (overlayView != null && overlayView.getParent() != null) {
            ((ViewGroup) overlayView.getParent()).removeView(overlayView);
            overlayView = null;
        }
    }

    //Fragment生命週期相關
    @Override
    public void onResume() {
        super.onResume();
        //重新更新RecycleView
        RecycleViewReset();

        //換頁回來再召喚漢堡選單
        if (actionBar != null) {
            // Ensure drawerLayout is not null
            if (drawerLayout != null) {
                // Set up ActionBarDrawerToggle
                if (toggle == null) {
                    toggle = new ActionBarDrawerToggle(
                            requireActivity(), drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close);
                    drawerLayout.addDrawerListener(toggle);
                    toggle.syncState();
                }
                toggle.getDrawerArrowDrawable().setColor(getResources().getColor(R.color.green));
            }
        }
    }

    // 更新重置按鈕的狀態
    private void updateResetButtonState() {
        if (sharedViewModel.getLocationCount() >= 0) {
            //設置可點擊狀態
            btnReset.setEnabled(true);
            //改變按鈕文字顏色
            btnReset.setTextColor(ContextCompat.getColor(requireContext(), R.color.darkgreen));
            //改變按鈕的Drawable
            btnReset.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.btn_additem)); // 設定啟用時的背景顏色
        } else {
            //設置不可點擊狀態
            btnReset.setEnabled(false);
            //改變按鈕文字顏色
            btnReset.setTextColor(ContextCompat.getColor(requireContext(), R.color.lightgreen));
            //改變按鈕的Drawable
            btnReset.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.btn_unclickable)); // 設定禁用時的背景顏色
        }
    }
}