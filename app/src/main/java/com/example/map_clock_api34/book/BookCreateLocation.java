package com.example.map_clock_api34.book;

import android.Manifest;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
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
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
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


import com.example.map_clock_api34.book.BookDatabaseHelper.BookTable;
import com.example.map_clock_api34.book.BookDatabaseHelper.LocationTable2;

import com.example.map_clock_api34.R;
import com.example.map_clock_api34.SharedViewModel;
import com.example.map_clock_api34.CreateLocation.CreatlocationListAdapter.ListAdapterRoute;
import com.example.map_clock_api34.CreateLocation.CreatlocationListAdapter.RecyclerViewActionHome;
import com.example.map_clock_api34.CreateLocation.SelectPlaceFragment;
import com.example.map_clock_api34.note.NoteFragment;
import com.example.map_clock_api34.setting.CreatLocation_setting;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

public class BookCreateLocation extends Fragment {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001; // 定義位置權限請求代碼

    private View rootView;
    private View overlayView; // 用來覆蓋畫面的遮罩
    private DrawerLayout drawerLayout;

    private ActionBar actionBar;
    private Button btnReset;    // 重置按鈕
    private EditText bookNameInput;     // 書籤名稱的輸入框

    private SharedViewModel sharedViewModel; // 共享的 ViewModel，用來在片段之間共享資料

    private BookDatabaseHelper dbBookHelper;

    private RecyclerView recyclerViewRoute; // 用來顯示路線的 RecyclerView
    private ListAdapterRoute listAdapterRoute; // 路線的適配器

    private final ArrayList<HashMap<String, String>> arrayList = new ArrayList<>(); // 用來儲存路線的列表

    private String uniqueID;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.book_fragment_book_creatlocation, container, false);

        dbBookHelper = new BookDatabaseHelper(requireContext());
        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        bookNameInput = rootView.findViewById(R.id.BookName);

        drawerLayout = getActivity().findViewById(R.id.drawerLayout);
        // 鎖定不能左滑漢堡選單
        if (drawerLayout != null) {
            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        }

        setupActionBar();   // 初始化 ActionBar
        setupButtons();     // 初始化按鈕
        setupRecyclerViews(); // 初始化 RecyclerView

        return rootView;
    }

    // 初始化按鈕，包括新增地點按鈕和重置按鈕的行為
    private void setupButtons() {
        // 新增地點按鈕
        Button btnAddItem = rootView.findViewById(R.id.btn_addItem);
        btnAddItem.setOnClickListener(v -> {
            // 如果使用者同意了位置權限，則打開選擇地點的頁面
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                if (sharedViewModel.getLocationCount() < 6) { // 限制最多只能添加6個地點
                    openSelectPlaceFragment();
                }
            } else {
                // 如果沒有同意權限，請求使用者開啟位置權限
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
                makeToast("請開啟定位權限",1000);
            }
        });

        // 重置按鈕
        btnReset = rootView.findViewById(R.id.btn_reset);
        btnReset.setOnClickListener(v -> ShowPopupWindow()); // 點擊重置按鈕，彈出確認重置的彈窗

        // 確認按鈕
        Button btnMapping = rootView.findViewById(R.id.btn_sure);
        btnMapping.setOnClickListener(v -> {
            if (sharedViewModel.getLocationCount() >= 0) { // 確認已選擇地點
                if (bookNameInput.getText().toString().equals("")) { // 檢查書名是否輸入
                    makeToast("沒有輸入書籤名稱喔",1000);
                    return;
                }

                // 書籤名稱長度限制
                if (bookNameInput.getText().toString().length() > 10) {
                    makeToast("書籤名稱必須小於10個字",1000);
                } else {
                    // 保存至資料庫並返回上一頁
                    saveInLocationDB();
                    saveInBookDB();
                    sharedViewModel.clearAll();
                    getActivity().getSupportFragmentManager().popBackStack();
                }
            } else {
                makeToast("你還沒有選擇地點喔",1000);
            }
        });

        // 記事按鈕
        ImageView noteView = rootView.findViewById(R.id.bookcreate_imageView);
        noteView.setOnClickListener(v -> {
            if (arrayList.isEmpty()) {
                makeToast("還沒有選擇地點喔",1000);
                return;
            }
            NoteFragment notesFragment = new NoteFragment(); // 打開記事頁面
            FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fl_container, notesFragment);
            transaction.addToBackStack(null);
            transaction.commit();
        });

        // 設定按鈕
        ImageView bookSetting = rootView.findViewById(R.id.bookset_imageView);
        bookSetting.setOnClickListener(v -> {
            if (arrayList.isEmpty()) {
                makeToast("還沒有選擇地點喔",1000);
                return;
            }
            CreatLocation_setting creatLocation_setting = new CreatLocation_setting(); // 打開設定頁面
            FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fl_container, creatLocation_setting);
            transaction.addToBackStack(null);
            transaction.commit();
        });
    }

    // 將數據保存到書籍資料庫中
    private void saveInBookDB() {
        try {
            SQLiteDatabase writeDB = dbBookHelper.getWritableDatabase(); // 可寫資料庫
            SQLiteDatabase readDB = dbBookHelper.getReadableDatabase(); // 可讀資料庫

            Cursor cursor = readDB.rawQuery("SELECT location_id " +
                    "FROM location WHERE alarm_name=?", new String[]{uniqueID});    //取得地點資料表的Cursor

            long currentTimeMillis = System.currentTimeMillis();    //取得系統時間
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String formattedDate = sdf.format(new Date(currentTimeMillis));

            // 用來控制排列編號
            int arranged_id_local = 0;

            // 將每個地點插入書籍表中
            while (cursor.moveToNext()) {
                if (bookNameInput.getText().toString() != null) {

                    ContentValues values = new ContentValues();
                    values.put(BookTable.COLUMN_START_TIME, formattedDate);
                    values.put(BookTable.COLUMN_ALARM_NAME, bookNameInput.getText().toString());
                    values.put(BookTable.COLUMN_LOCATION_ID, cursor.getString(0));
                    values.put(BookTable.COLUMN_ARRANGED_ID, arranged_id_local++);

                    writeDB.insert(BookTable.TABLE_NAME, null, values);
                }
            }

            writeDB.close();
            readDB.close();
            cursor.close();

        } catch (Exception e) {
            Log.d("DBProblem", e.getMessage());
        }
    }

    // 將數據保存到位置資料庫中
    private void saveInLocationDB() {

        uniqueID = UUID.randomUUID().toString(); // 生成唯一的 UUID 作為鬧鐘名稱

        SQLiteDatabase db = dbBookHelper.getWritableDatabase();

        // 將每個位置插入到位置表中
        for (int i = 0; i <= sharedViewModel.getLocationCount(); i++) {
            String name = sharedViewModel.getDestinationName(i);
            double latitude = sharedViewModel.getLatitude(i);
            double longitude = sharedViewModel.getLongitude(i);
            String CityName = sharedViewModel.getCapital(i);
            String AreaName = sharedViewModel.getArea(i);
            String Note = sharedViewModel.getNote(i);

            // 設定相關的提醒設置
            boolean vibrate = sharedViewModel.getVibrate(i);
            boolean ringtone = sharedViewModel.getRingtone(i);
            int notificationTime = sharedViewModel.getNotification(i);

            if (name != null) {
                ContentValues values = new ContentValues();
                values.put(LocationTable2.COLUMN_PLACE_NAME, name);
                values.put(LocationTable2.COLUMN_LATITUDE, latitude);
                values.put(LocationTable2.COLUMN_LONGITUDE, longitude);
                values.put(LocationTable2.COLUMN_ALARM_NAME, uniqueID);
                values.put(LocationTable2.COLUMN_CITY_NAME, CityName);
                values.put(LocationTable2.COLUMN_AREA_NAME, AreaName);
                values.put(LocationTable2.COLUMN_NOTE_INFO, Note);
                values.put(LocationTable2.COLUMN_VIBRATE, vibrate);
                values.put(LocationTable2.COLUMN_RINGTONE, ringtone);
                values.put(LocationTable2.COLUMN_notificationTime, notificationTime);

                db.insert(LocationTable2.TABLE_NAME, null, values); // 插入資料庫
            }
        }
        db.close();
    }

    // 打開選擇地點的頁面
    private void openSelectPlaceFragment() {
        SelectPlaceFragment mapFragment = new SelectPlaceFragment();
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fl_container, mapFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    // 初始化 RecyclerView，包括路線表和功能表
    private void setupRecyclerViews() {
        // 路線表的初始化
        recyclerViewRoute = rootView.findViewById(R.id.recycleViewRouteBook);
        recyclerViewRoute.setLayoutManager(new LinearLayoutManager(getActivity())); // 設置線性佈局管理器
        recyclerViewRoute.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL)); // 添加分隔線
        listAdapterRoute = new ListAdapterRoute(arrayList, sharedViewModel, true); // 啟用拖動功能
        recyclerViewRoute.setAdapter(listAdapterRoute);

        // 控制 RecyclerView 的行為，如交換、刪除等
        RecyclerViewActionHome recyclerViewActionHome = new RecyclerViewActionHome();
        recyclerViewActionHome.attachToRecyclerView(recyclerViewRoute, arrayList, listAdapterRoute, sharedViewModel, getActivity(), btnReset);
    }

    // 每次返回該頁面都重置路線表，避免重複添加
    private void RecycleViewReset() {

        arrayList.clear(); // 清空列表

        String shortLocationName;
        if (sharedViewModel.getLocationCount() != -1) {
            for (int j = 0; j <= sharedViewModel.getLocationCount(); j++) {
                HashMap<String, String> hashMap = new HashMap<>();
                shortLocationName = sharedViewModel.getDestinationName(j);
                // 如果地名超過20個字，截斷並添加 "..."
                if (shortLocationName.length() > 20) {
                    hashMap.put("data", shortLocationName.substring(0, 20) + "...");
                } else {
                    hashMap.put("data", shortLocationName);
                }
                arrayList.add(hashMap); // 添加到列表中
            }
        }
        listAdapterRoute.notifyDataSetChanged(); // 更新列表
        updateResetButtonState(); // 更新重置按鈕的狀態
    }

    // 顯示重置按鈕的彈窗，確認是否重置
    private void ShowPopupWindow() {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.popupwindow_reset_button, null, false);
        PopupWindow popupWindow = new PopupWindow(view, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        popupWindow.setWidth(700); // 設置寬度
        popupWindow.setFocusable(false); // 設置不可點擊其他地方
        popupWindow.setOutsideTouchable(false); // 不允許點擊外部關閉
        popupWindow.showAtLocation(rootView, Gravity.CENTER, 0, 0); // 顯示彈窗
        popupWindow.setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        // 添加覆蓋視圖，防止點擊底部的其他視圖
        overlayView = new View(getContext());
        overlayView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        overlayView.setBackgroundColor(ContextCompat.getColor(getContext(), android.R.color.transparent));
        overlayView.setClickable(true);
        ((ViewGroup) rootView).addView(overlayView);

        //設置對話框內的取消按紐
        Button btnCancel = view.findViewById(R.id.PopupCancel);
        btnCancel.setOnClickListener(v -> {
            popupWindow.dismiss(); // 關閉彈窗
            removeOverlayView(); // 移除覆蓋視圖
        });

        //設置對話框內的確認按紐
        Button btnSure = view.findViewById(R.id.Popupsure);
        btnSure.setOnClickListener(v -> {
            // 重置ShareViewModel 的地點計數並更新列表
            while (sharedViewModel.getLocationCount() >= 0) {
                arrayList.remove(sharedViewModel.getLocationCount());
                sharedViewModel.setLocationCount();
            }
            recyclerViewRoute.setAdapter(listAdapterRoute);
            updateResetButtonState(); // 更新重置按鈕狀態
            removeOverlayView(); // 移除覆蓋視圖
            popupWindow.dismiss();
        });
    }

    // 移除覆蓋視圖
    private void removeOverlayView() {
        if (overlayView != null && overlayView.getParent() != null) {
            ((ViewGroup) overlayView.getParent()).removeView(overlayView);
            overlayView = null;
        }
    }

    // 更新重置按鈕的狀態
    private void updateResetButtonState() {
        if (sharedViewModel.getLocationCount() >= 0) {
            btnReset.setEnabled(true); // 設置為可點擊
            btnReset.setTextColor(ContextCompat.getColor(requireContext(), R.color.darkgreen)); // 改變按鈕顏色
            btnReset.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.btn_additem)); // 設定啟用時的背景
        } else {
            btnReset.setEnabled(false); // 設置為不可點擊
            btnReset.setTextColor(ContextCompat.getColor(requireContext(), R.color.lightgreen)); // 改變按鈕顏色
            btnReset.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.btn_unclickable)); // 設定禁用時的背景
        }
        changeNotification(); // 更新通知
    }

    // 更改通知，如果沒有地點則顯示提示
    private void changeNotification() {
        if (arrayList.isEmpty()) {
            TextView notification = rootView.findViewById(R.id.textView);
            notification.setText("請按「新增」添加地點");
        } else {
            TextView notification = rootView.findViewById(R.id.textView);
            notification.setText("");
        }
    }

    // 初始化 ActionBar 的外觀和行為
    private void setupActionBar() {
        actionBar = ((AppCompatActivity) requireActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
            actionBar.setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(getActivity(), R.color.lightgreen)));
        }

        CardView cardViewtitle = new CardView(requireContext());
        cardViewtitle.setLayoutParams(new CardView.LayoutParams(ActionBar.LayoutParams.WRAP_CONTENT, ActionBar.LayoutParams.WRAP_CONTENT));
        Drawable drawable = ContextCompat.getDrawable(requireContext(), R.drawable.cardviewtitle_shape);
        cardViewtitle.setBackground(drawable);

        LinearLayout linearLayout = new LinearLayout(requireContext());
        linearLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);

        // 添加書籤圖標
        ImageView bookmark = new ImageView(requireContext());
        bookmark.setImageResource(R.drawable.routeadd);
        bookmark.setPadding(10, 10, 5, 10); // 設置圖標邊距
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(100, 100);
        params.setMarginStart(10); // 設置左邊距
        bookmark.setLayoutParams(params);

        // 添加標題文字
        TextView bookTitle = new TextView(requireContext());
        bookTitle.setText("建立路線");
        bookTitle.setTextSize(15);
        bookTitle.setTextColor(getResources().getColor(R.color.green)); // 設置文字顏色
        bookTitle.setPadding(10, 10, 30, 10); // 設置內距

        linearLayout.addView(bookmark);
        linearLayout.addView(bookTitle);
        cardViewtitle.addView(linearLayout);

        // 建立自定義返回按鈕
        ImageView returnButton = new ImageView(requireContext());
        returnButton.setImageResource(R.drawable.back);
        LinearLayout.LayoutParams returnButtonParams = new LinearLayout.LayoutParams(100, 100);
        returnButton.setLayoutParams(returnButtonParams);

        // 建立 ActionBar 的佈局，包含返回按鈕和標題
        LinearLayout actionBarLayout = new LinearLayout(requireContext());
        actionBarLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
        actionBarLayout.setOrientation(LinearLayout.HORIZONTAL);
        actionBarLayout.setWeightSum(1.0f);

        LinearLayout leftLayout = new LinearLayout(requireContext());
        leftLayout.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 0.1f));
        leftLayout.setOrientation(LinearLayout.HORIZONTAL);
        leftLayout.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
        leftLayout.addView(returnButton);

        LinearLayout rightLayout = new LinearLayout(requireContext());
        rightLayout.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 0.9f));
        rightLayout.setOrientation(LinearLayout.HORIZONTAL);
        rightLayout.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
        rightLayout.addView(cardViewtitle);

        actionBarLayout.addView(leftLayout);
        actionBarLayout.addView(rightLayout);

        Toolbar toolbar = getActivity().findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(null); // 隱藏漢堡選單

        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false); // 隱藏預設標題
            actionBar.setDisplayShowCustomEnabled(true);
            actionBar.setCustomView(actionBarLayout, new ActionBar.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT));
            actionBar.show();
        }

        //設置acrionBar上的返回按鈕
        returnButton.setOnClickListener(v -> {
            sharedViewModel.clearAll(); // 清空共享資料
            arrayList.clear(); // 清空列表
            getActivity().getSupportFragmentManager().popBackStack(); // 回到上一頁
        });
    }

    public void makeToast(String message, int durationInMillis) {
        // 創建 Toast
        Toast toast = Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT);
        toast.show();

        // 使用 Handler 來控制顯示時長
        new Handler().postDelayed(toast::cancel, durationInMillis);
    }

    // 當片段顯示時，重置 RecyclerView 並重新顯示 ActionBar
    @Override
    public void onResume() {
        super.onResume();
        RecycleViewReset(); // 重置路線表
        if (actionBar != null) {
            setupActionBar(); // 重新顯示 ActionBar
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
