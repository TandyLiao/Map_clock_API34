package com.example.map_clock_api34.CreateLocation;

import static android.content.Context.MODE_PRIVATE;

// 引入必要的 Android 和其他庫
import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
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
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.map_clock_api34.history.HistoryDatabaseHelper;
import com.example.map_clock_api34.history.HistoryDatabaseHelper.HistoryTable;
import com.example.map_clock_api34.history.HistoryDatabaseHelper.LocationTable;
import com.example.map_clock_api34.R;
import com.example.map_clock_api34.SharedViewModel;
import com.example.map_clock_api34.Weather.WeatherService;
import com.example.map_clock_api34.CreateLocation.CreatlocationListAdapter.ListAdapterRoute;
import com.example.map_clock_api34.CreateLocation.CreatlocationListAdapter.ListAdapterTool;
import com.example.map_clock_api34.CreateLocation.CreatlocationListAdapter.RecyclerViewActionHome;
import com.example.map_clock_api34.TutorialFragment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class CreateLocationFragment extends Fragment {

    // 權限相關常量
    private static final int MULTIPLE_PERMISSIONS_REQUEST_CODE = 101;

    // 檢查權限請求的邏輯
    private static final String PREFS_NAME = "PermissionPrefs";
    private static final String LOCATION_DENY_COUNT = "locationDenyCount";
    private static final String NOTIFICATION_DENY_COUNT = "notificationDenyCount";
    private static final int MAX_DENY_COUNT = 2; // 當拒絕權限達到兩次時跳轉到設置頁面

    // 定義資料庫輔助類
    private HistoryDatabaseHelper dbHistoryHelper;

    // ActionBar 相關
    private ActionBar actionBar;
    private Toolbar toolbar;
    private ActionBarDrawerToggle toggle;
    private DrawerLayout drawerLayout;

    private AlertDialog permissionDialog;
    private View rootView;
    private View overlayView;
    private Button btnReset;

    // RecyclerView 和 Adapter 相關
    private RecyclerView recyclerViewRoute;
    private ListAdapterRoute listAdapterRoute;
    private SharedViewModel sharedViewModel;

    private final WeatherService weatherService = new WeatherService();

    private String Historynames;    // 路線名稱
    private String uniqueID;        // 唯一的 ID

    // 存放路線資料的 ArrayList
    private final ArrayList<HashMap<String, String>> arrayList = new ArrayList<>();

    // onCreateView 用來初始化畫面和資料
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.home_fragment_creatlocation, container, false);

        dbHistoryHelper = new HistoryDatabaseHelper(requireContext());
        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);

        // 檢查是否需要顯示教學頁面
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("MyPrefs", MODE_PRIVATE);
        boolean isLoggedIn = sharedPreferences.getBoolean("CreateLogin", false);

        if (!isLoggedIn) {
            // 如果第一次進入，顯示教學頁面
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt("WhichPage", 5);
            editor.putBoolean("CreateLogin", true);
            editor.apply();
            TutorialFragment tutorialFragment = new TutorialFragment();
            FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fl_container, tutorialFragment);
            transaction.addToBackStack(null);
            transaction.commit();
        }

        // 初始化 ActionBar
        setupActionBar();
        // 初始化側邊選單
        setupNavigationDrawer();
        // 初始化按鈕
        setupButtons();
        // 初始化 RecyclerView
        setupRecyclerViews();

        // 換頁回來再次設置側邊選單
        if (getActivity() != null) {
            drawerLayout = getActivity().findViewById(R.id.drawerLayout);
            toolbar = requireActivity().findViewById(R.id.toolbar);
        }
        return rootView;
    }

    // 初始化按鈕和其點擊事件
    private void setupButtons() {
        // 新增地點按鈕，當地點數量少於 6 個時允許新增
        Button btnAddItem = rootView.findViewById(R.id.btn_addItem);
        btnAddItem.setOnClickListener(v -> {
            if (sharedViewModel.getLocationCount() < 6) {
                openSelectPlaceFragment();  // 打開選擇地點頁面
            }else{
                makeToast("地點最多只能7個喔",1000);
            }
        });

        // 重置按鈕
        btnReset = rootView.findViewById(R.id.btn_reset);
        btnReset.setOnClickListener(v -> ShowPopupWindow());  // 顯示重置的 PopupWindow

        // 確認按鈕，當有選擇地點時進行導航
        Button btnMapping = rootView.findViewById(R.id.btn_sure);
        btnMapping.setOnClickListener(v -> {
            if (sharedViewModel.getLocationCount() >= 0) {
                openStartMappingFragment();  // 打開導航頁面

                // 設置路線名稱並儲存到資料庫
                Historynames = sharedViewModel.getDestinationName(0) + "->" + sharedViewModel.getDestinationName(sharedViewModel.getLocationCount());
                saveInDB();  // 儲存地點資料到資料庫
                saveInHistoryDB();  // 儲存歷史資料到資料庫

            } else {
                makeToast("還沒有選擇地點喔",1000);
            }
        });
    }

    // 檢查和請求權限
    private void checkAndRequestPermissions() {
        SharedPreferences prefs = getActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        int locationDenyCount = prefs.getInt(LOCATION_DENY_COUNT, 0);
        int notificationDenyCount = prefs.getInt(NOTIFICATION_DENY_COUNT, 0);

        List<String> permissionsNeeded = new ArrayList<>();

        // 檢查定位權限
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (locationDenyCount < MAX_DENY_COUNT) {  // 如果拒絕次數未超過兩次，繼續請求
                permissionsNeeded.add(Manifest.permission.ACCESS_FINE_LOCATION);
            }
        } else {
            prefs.edit().putInt(LOCATION_DENY_COUNT, 0).apply();  // 如果權限已授予，重置拒絕次數
        }

        // 檢查通知權限（Android 13 及以上版本需要）
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                if (notificationDenyCount < MAX_DENY_COUNT) {
                    permissionsNeeded.add(Manifest.permission.POST_NOTIFICATIONS);
                }
            } else {
                prefs.edit().putInt(NOTIFICATION_DENY_COUNT, 0).apply();  // 如果權限已授予，重置拒絕次數
            }
        }

        // 如果拒絕次數超過上限，顯示提示並跳轉到設置頁面
        if (locationDenyCount >= MAX_DENY_COUNT || notificationDenyCount >= MAX_DENY_COUNT) {
            showPermissionDeniedDialog(locationDenyCount, notificationDenyCount);
            return;
        }

        // 如果沒有需要請求的權限，直接返回
        if (permissionsNeeded.isEmpty()) {
            return;
        }

        // 請求權限
        requestPermissions(permissionsNeeded.toArray(new String[0]), MULTIPLE_PERMISSIONS_REQUEST_CODE);
    }

    // 處理請求權限的結果
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        SharedPreferences prefs = getActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        if (requestCode == MULTIPLE_PERMISSIONS_REQUEST_CODE) {
            boolean locationPermissionGranted = true;
            boolean notificationsPermissionGranted = true;

            // 檢查每個權限的結果
            for (int i = 0; i < permissions.length; i++) {
                if (permissions[i].equals(Manifest.permission.ACCESS_FINE_LOCATION)) {
                    locationPermissionGranted = grantResults[i] == PackageManager.PERMISSION_GRANTED;
                    if (!locationPermissionGranted) {
                        incrementDenyCount(prefs, editor, LOCATION_DENY_COUNT);  // 如果拒絕，增加拒絕次數
                    }
                } else if (permissions[i].equals(Manifest.permission.POST_NOTIFICATIONS)) {
                    notificationsPermissionGranted = grantResults[i] == PackageManager.PERMISSION_GRANTED;
                    if (!notificationsPermissionGranted) {
                        incrementDenyCount(prefs, editor, NOTIFICATION_DENY_COUNT); // 如果拒絕，增加拒絕次數
                    }
                }
            }

            // 如果權限未授予，顯示提示並關閉應用
            if (!locationPermissionGranted || !notificationsPermissionGranted) {
                StringBuilder message = new StringBuilder("需要");
                if (!locationPermissionGranted) {
                    message.append(" 定位");
                }
                if (!notificationsPermissionGranted) {
                    message.append(" 通知");
                }
                message.append(" 權限才能正常運行");
                makeToast(message.toString(),1000);
                getActivity().finish();  // 關閉當前 Activity
            }
        }
        editor.apply();
    }

    // 增加拒絕次數
    private void incrementDenyCount(SharedPreferences prefs, SharedPreferences.Editor editor, String key) {
        int count = prefs.getInt(key, 0);
        editor.putInt(key, count + 1);
        editor.commit();  // 保存更新
    }

    // 顯示權限被拒的對話框並提供跳轉到設置頁面的選項
    private void showPermissionDeniedDialog(int locationDenyCount, int notificationDenyCount) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        permissionDialog = builder.create();

        StringBuilder message = new StringBuilder("您已多次拒絕以下權限:\n");

        if (locationDenyCount >= MAX_DENY_COUNT) {
            message.append(" - 定位權限\n");
        }
        if (notificationDenyCount >= MAX_DENY_COUNT) {
            message.append(" - 通知權限\n");
        }
        message.append("請在設置中手動打開這些權限");

        // 套用自訂佈局顯示對話框
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View customView = inflater.inflate(R.layout.dialog_deltebook, null);

        Button positiveButton = customView.findViewById(R.id.Popupsure);
        Button negativeButton = customView.findViewById(R.id.PopupCancel);
        TextView showTitle = customView.findViewById(R.id.txtNote);

        positiveButton.setText("打開設置");
        // 設置跳轉到設置頁面的按鈕
        positiveButton.setOnClickListener(v -> {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            Uri uri = Uri.fromParts("package", getActivity().getPackageName(), null);
            intent.setData(uri);
            startActivity(intent);
            permissionDialog.cancel();  // 關閉對話框
        });

        // 設置取消按鈕
        negativeButton.setOnClickListener(v -> {
            getActivity().finish();
            permissionDialog.cancel();  // 關閉對話框
        });

        showTitle.setText(message);
        showTitle.setTextSize(20);

        permissionDialog.setView(customView);
        permissionDialog.setCanceledOnTouchOutside(false);
        permissionDialog.show();  // 顯示對話框
    }

    // 儲存歷史資料到資料庫
    private void saveInHistoryDB() {
        try {
            SQLiteDatabase writeDB = dbHistoryHelper.getWritableDatabase();
            SQLiteDatabase readDB = dbHistoryHelper.getReadableDatabase();

            // 查詢地點 ID
            Cursor cursor = readDB.rawQuery("SELECT location_id FROM location WHERE alarm_name=?", new String[]{uniqueID});

            long currentTimeMillis = System.currentTimeMillis();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String formattedDate = sdf.format(new Date(currentTimeMillis));

            // 用來讓每筆歷史路線從 0 開始
            int arranged_id_local = 0;

            // 將資料插入到歷史資料表中
            while (cursor.moveToNext()) {
                if (Historynames != null) {
                    ContentValues values = new ContentValues();
                    values.put(HistoryTable.COLUMN_START_TIME, formattedDate);
                    values.put(HistoryTable.COLUMN_ALARM_NAME, Historynames);
                    values.put(HistoryTable.COLUMN_LOCATION_ID, cursor.getString(0));
                    values.put(HistoryTable.COLUMN_ARRANGED_ID, arranged_id_local++);
                    writeDB.insert(HistoryTable.TABLE_NAME, null, values);
                }
            }

            writeDB.close();
            readDB.close();
        } catch (Exception e) {
            Log.d("DBProblem", e.getMessage());
        }
    }

    // 儲存地點資料到資料庫
    private void saveInDB() {
        uniqueID = UUID.randomUUID().toString();  // 產生一個唯一的 ID

        SQLiteDatabase db = dbHistoryHelper.getWritableDatabase();

        for (int i = 0; i <= sharedViewModel.getLocationCount(); i++) {
            String name = sharedViewModel.getDestinationName(i);
            double latitude = sharedViewModel.getLatitude(i);
            double longitude = sharedViewModel.getLongitude(i);
            String CityName = sharedViewModel.getCapital(i);
            String AreaName = sharedViewModel.getArea(i);
            String Note = sharedViewModel.getNote(i);
            boolean vibrate = sharedViewModel.getVibrate(i);
            boolean ringtone = sharedViewModel.getRingtone(i);
            int notificationTime = sharedViewModel.getNotification(i);

            // 將地點資料插入資料庫
            if (name != null) {
                ContentValues values = new ContentValues();
                values.put(LocationTable.COLUMN_PLACE_NAME, name);
                values.put(LocationTable.COLUMN_LATITUDE, latitude);
                values.put(LocationTable.COLUMN_LONGITUDE, longitude);
                values.put(LocationTable.COLUMN_ALARM_NAME, uniqueID);
                values.put(LocationTable.COLUMN_CITY_NAME, CityName);
                values.put(LocationTable.COLUMN_AREA_NAME, AreaName);
                values.put(LocationTable.COLUMN_NOTE_INFO, Note);
                values.put(LocationTable.COLUMN_VIBRATE, vibrate);
                values.put(LocationTable.COLUMN_RINGTONE, ringtone);
                values.put(LocationTable.COLUMN_NOTIFICATION_TIME, notificationTime);

                db.insert(LocationTable.TABLE_NAME, null, values);
            }
        }
        db.close();
    }

    // 打開選擇地點頁面
    private void openSelectPlaceFragment() {
        SelectPlaceFragment mapFragment = new SelectPlaceFragment();
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fl_container, mapFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    // 打開導航頁面
    private void openStartMappingFragment() {
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        StartMappingFragment startMappingFragment = (StartMappingFragment) fragmentManager.findFragmentByTag("StartMapping");

        FragmentTransaction transaction = fragmentManager.beginTransaction();

        if (startMappingFragment == null) {
            // 如果 fragment 不存在，則創建一個新的
            startMappingFragment = new StartMappingFragment();
            transaction.add(R.id.fl_container, startMappingFragment, "StartMapping");
        } else {
            // 如果 fragment 已存在，則顯示它
            transaction.show(startMappingFragment);
        }

        transaction.addToBackStack(null);
        transaction.commit();
    }

    // 初始化側邊選單
    private void setupNavigationDrawer() {
        drawerLayout = requireActivity().findViewById(R.id.drawerLayout);
        toolbar = requireActivity().findViewById(R.id.toolbar);
        toggle = new ActionBarDrawerToggle(
                requireActivity(), drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        toggle.getDrawerArrowDrawable().setColor(getResources().getColor(R.color.green));
    }

    // 初始化 ActionBar
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
        // 建立 LinearLayout 放置圖示和文字
        LinearLayout linearLayout = new LinearLayout(requireContext());
        linearLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        ));
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);
        // 設置右上角的小圖示
        ImageView bookmark = new ImageView(requireContext());
        bookmark.setImageResource(R.drawable.route);
        bookmark.setPadding(10, 10, 5, 10);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                100, // 設置寬度為 100 像素
                100  // 設置高度為 100 像素
        );
        params.setMarginStart(10);  // 設置左邊距
        bookmark.setLayoutParams(params);

        // 創建標題文字
        TextView bookTitle = new TextView(requireContext());
        bookTitle.setText("路線規劃");
        bookTitle.setTextSize(15);
        bookTitle.setTextColor(getResources().getColor(R.color.green));
        bookTitle.setPadding(10, 10, 30, 10);
        linearLayout.addView(bookmark);
        linearLayout.addView(bookTitle);
        cardViewtitle.addView(linearLayout);

        // 將 CardView 添加到 ActionBar
        actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false);  // 隱藏原有標題
            actionBar.setDisplayShowCustomEnabled(true);
            actionBar.setCustomView(cardViewtitle, new ActionBar.LayoutParams(
                    ActionBar.LayoutParams.WRAP_CONTENT,
                    ActionBar.LayoutParams.WRAP_CONTENT,
                    Gravity.END));
            actionBar.show();
        }
    }

    // 初始化 RecyclerView
    private void setupRecyclerViews() {
        // 初始化路線列表
        recyclerViewRoute = rootView.findViewById(R.id.recycleViewRouteBook);
        recyclerViewRoute.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerViewRoute.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));
        listAdapterRoute = new ListAdapterRoute(arrayList, sharedViewModel, true);  // 啟用拖動功能
        recyclerViewRoute.setAdapter(listAdapterRoute);

        // 設置路線表可以進行拖動和刪除操作
        RecyclerViewActionHome recyclerViewActionHome = new RecyclerViewActionHome();
        recyclerViewActionHome.attachToRecyclerView(recyclerViewRoute, arrayList, listAdapterRoute, sharedViewModel, getActivity(), btnReset);

        // 初始化工具列表
        RecyclerView recyclerViewTool = rootView.findViewById(R.id.recycleViewTool);
        recyclerViewTool.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));

        // 創建 ListAdapterTool 並傳入 FragmentTransaction
        FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
        ListAdapterTool listAdapterTool = new ListAdapterTool(fragmentTransaction, sharedViewModel, weatherService, getActivity());
        recyclerViewTool.setAdapter(listAdapterTool);
    }

    // 重置路線列表，防止列表內容疊加
    private void RecycleViewReset() {
        arrayList.clear();  // 清除原列表
        String shortLocationName;
        if (sharedViewModel.getLocationCount() != -1) {
            for (int j = 0; j <= sharedViewModel.getLocationCount(); j++) {
                HashMap<String, String> hashMap = new HashMap<>();
                shortLocationName = sharedViewModel.getDestinationName(j);
                // 如果地名超過 20 字，使用 "..." 代替
                if (shortLocationName.length() > 20) {
                    hashMap.put("data", shortLocationName.substring(0, 20) + "...");
                } else {
                    hashMap.put("data", shortLocationName);
                }
                arrayList.add(hashMap);
            }
        }
        listAdapterRoute.notifyDataSetChanged();  // 通知 Adapter 更新列表
        updateResetButtonState();  // 更新重置按鈕狀態
    }

    // 顯示重置按鈕的 PopupWindow
    private void ShowPopupWindow() {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.popupwindow_reset_button, null, false);
        PopupWindow popupWindow = new PopupWindow(view, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        popupWindow.setWidth(800);
        popupWindow.setFocusable(false);
        popupWindow.setOutsideTouchable(false);

        // 顯示 PopupWindow
        popupWindow.showAtLocation(rootView, Gravity.CENTER, 0, 0);
        popupWindow.setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        // 疊加防止點擊其他區域
        overlayView = new View(getContext());
        overlayView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        overlayView.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.transparent_black));
        overlayView.setClickable(true);
        ((ViewGroup) rootView).addView(overlayView);

        Button BTNPopup = view.findViewById(R.id.PopupCancel);
        BTNPopup.setOnClickListener(v -> {
            popupWindow.dismiss();
            removeOverlayView();  // 移除疊加的防點擊區域
        });

        Button btnsure = view.findViewById(R.id.Popupsure);
        btnsure.setOnClickListener(v -> {
            while (sharedViewModel.getLocationCount() >= 0) {
                arrayList.clear();
                sharedViewModel.clearAll();
            }
            recyclerViewRoute.setAdapter(listAdapterRoute);
            updateResetButtonState();  // 更新重置按鈕狀態
            removeOverlayView();  // 移除防點擊區域
            popupWindow.dismiss();
        });
    }

    // 移除疊加的防點擊區域
    private void removeOverlayView() {
        if (overlayView != null && overlayView.getParent() != null) {
            ((ViewGroup) overlayView.getParent()).removeView(overlayView);
            overlayView = null;
        }
    }

    // 更新重置按鈕的狀態
    private void updateResetButtonState() {
        if (sharedViewModel.getLocationCount() >= 0) {
            // 按鈕可點擊
            btnReset.setEnabled(true);
            // 改變按鈕文字顏色
            btnReset.setTextColor(ContextCompat.getColor(requireContext(), R.color.green));
            // 設定啟用狀態的背景
            btnReset.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.btn_additem));
        } else {
            // 按鈕不可點擊
            btnReset.setEnabled(false);
            // 改變按鈕文字顏色
            btnReset.setTextColor(ContextCompat.getColor(requireContext(), R.color.lightgreen));
            // 設定禁用狀態的背景
            btnReset.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.btn_unclickable));
        }

        // 顯示提示
        TextView notification = rootView.findViewById(R.id.textView6);
        if (sharedViewModel.getLocationCount() != -1) {
            notification.setText("");
        } else {
            notification.setText("請按「新增」添加地點");
        }
    }

    public void makeToast(String message, int durationInMillis) {
        // 創建 Toast
        Toast toast = Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT);
        toast.show();

        // 使用 Handler 來控制顯示時長
        new Handler().postDelayed(toast::cancel, durationInMillis);
    }

    // Fragment 生命週期相關
    @Override
    public void onResume() {
        super.onResume();
        // 重置 RecyclerView
        RecycleViewReset();
        checkAndRequestPermissions();  // 檢查和請求權限

        // 如果權限已授予且 Dialog 顯示，則關閉 Dialog
        if (permissionDialog != null && permissionDialog.isShowing()) {
            if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    && (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                    ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED)) {
                permissionDialog.dismiss();
            }
        }

        // 重新設置側邊選單
        setupNavigationDrawer();
        if (actionBar != null) {
            if (drawerLayout != null) {
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

}
