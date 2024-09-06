package com.example.map_clock_api34.book;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
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
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
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
import com.example.map_clock_api34.SharedViewModel;
import com.example.map_clock_api34.book.RecycleViewActionBook.SwipeToDeleteCallback;
import com.example.map_clock_api34.history.ListAdapter.ListAdapterHistory;
import com.example.map_clock_api34.book.BookDatabaseHelper.BookTable;
import com.example.map_clock_api34.book.BookDatabaseHelper.LocationTable2;
import com.example.map_clock_api34.home.CreateLocation;
import com.example.map_clock_api34.home.HomeFragment;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.HashMap;

import androidx.recyclerview.widget.ItemTouchHelper;

import android.widget.Toast;

public class BookFragment extends Fragment {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001; // 定義位置權限請求代碼

    private View rootView;

    private Toolbar toolbar;                // 定義工具列
    private ActionBarDrawerToggle toggle;   // 側邊選單的開關
    private DrawerLayout drawerLayout;      // 抽屜佈局

    private SharedViewModel sharedViewModel; // ViewModel 用來在Fragment間共享數據

    private final ArrayList<HashMap<String, String>> arrayList = new ArrayList<>(); // 用來儲存書籍資料的列表
    private RecyclerView recyclerViewBook; // RecyclerView 用來顯示書籍項目
    private ListAdapterHistory listAdapterBook;

    private BookDatabaseHelper dbHelper; // 資料庫輔助類

    private FusedLocationProviderClient fusedLocationClient; // 用來獲取位置的客戶端

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.book_fragment_book, container, false);

        dbHelper = new BookDatabaseHelper(requireContext());
        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
        dbHelper = new BookDatabaseHelper(requireContext()); // 初始化資料庫輔助類

        setupRecyclerViews(); // 設置 RecyclerView
        addItemFromDB(); // 從資料庫中讀取數據
        setButton();
        setupActionBar(); // 設置工具列

        if (getActivity() != null) {
            drawerLayout = getActivity().findViewById(R.id.drawerLayout);
            toolbar = requireActivity().findViewById(R.id.toolbar);
        }

        return rootView;
    }

    // 設置按鈕的行為
    private void setButton() {

        // 編輯書籍按鈕的圖片
        ImageView editbook_imageView = rootView.findViewById(R.id.bookset_imageView);
        // 創建書籍按鈕的圖片
        ImageView createbook_imageView = rootView.findViewById(R.id.bookcreate_imageView);

        // 設置創建書籍按鈕的點擊監聽器
        createbook_imageView.setOnClickListener(v -> {
            sharedViewModel.clearAll(); // 清空共享數據
            //轉換頁面到書籤的建立地點 122-126
            BookCreateLocation bookCreateLocation = new BookCreateLocation();
            FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fl_container, bookCreateLocation);
            transaction.addToBackStack(null);
            transaction.commit();
        });

        // 設置設置編輯書籍按鈕的點擊監聽器
        editbook_imageView.setOnClickListener(v -> {
            HashMap<String, String> selectedItem = listAdapterBook.getSelectedItem(); // 獲取使用者選中的項目

            if (selectedItem!=null) {

                sharedViewModel.clearAll(); // 清空共享數據

                //站存路線名稱和時間
                sharedViewModel.routeName = selectedItem.get("placeName2");
                sharedViewModel.time = selectedItem.get("time");
                String time = sharedViewModel.time;

                int count = 0;  //讓存進ShareViewModel可以從第一個位置開始儲存
                SQLiteDatabase db = dbHelper.getReadableDatabase();
                Cursor cursor = db.rawQuery("SELECT * FROM " + BookTable.TABLE_NAME +
                        " WHERE " + BookTable.COLUMN_START_TIME + " = ?", new String[]{time});  //取得路線資料表的Cursor

                try {
                    while (cursor.moveToNext()) {

                        String locationId = cursor.getString(0);
                        Cursor locationCursor = db.rawQuery("SELECT * FROM " + LocationTable2.TABLE_NAME +
                                " WHERE " + LocationTable2.COLUMN_LOCATION_ID + " = ?", new String[]{locationId});//取得地點資料表的Cursor

                        // 獲取數據並設置到共享模型中
                        if (locationCursor.moveToFirst()) {

                            String placeName    = locationCursor.getString(3);
                            Double latitude     = locationCursor.getDouble(2);
                            Double longitude    = locationCursor.getDouble(1);
                            String city     = locationCursor.getString(5);
                            String area     = locationCursor.getString(6);
                            String Note     = locationCursor.getString(7);

                            boolean vibrate     = locationCursor.getInt(8) != 0;
                            boolean ringtone    = locationCursor.getInt(9) != 0;
                            int notificationTime = locationCursor.getInt(10);

                            sharedViewModel.uuid = locationCursor.getString(4);
                            sharedViewModel.setDestination(placeName, latitude, longitude);
                            sharedViewModel.setCapital(city);
                            sharedViewModel.setArea(area);
                            sharedViewModel.setNote(Note, count);
                            sharedViewModel.setVibrate(vibrate,count);
                            sharedViewModel.setRingtone(ringtone,count);
                            sharedViewModel.setNotification(notificationTime,count++);
                        }
                        locationCursor.close(); // 關閉地點 Cursor
                    }
                } catch (Exception e) {
                    e.printStackTrace(); // 打印錯誤信息
                } finally {
                    db.close(); // 關閉資料庫
                    cursor.close(); // 關閉路線 Cursor
                }

                // 轉換頁面到編輯書籤 188-192
                EditCreateLocation editCreateLocation = new EditCreateLocation();
                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.fl_container, editCreateLocation);
                transaction.addToBackStack(null);
                transaction.commit();
            }else{
                makeToast("請選擇要修改的路線",1000);
            }
        });

        // 使用者確認按鈕
        Button user_sure = rootView.findViewById(R.id.book_usesure);
        // 設置使用者確認按鈕的點擊監聽器
        user_sure.setOnClickListener(v -> {
            HashMap<String, String> selectedItem = listAdapterBook.getSelectedItem(); // 獲取使用者選中的項目
            if (selectedItem != null) {
                if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) { //檢查是否授予位置權限

                    saveInShareviewModel(); // 保存至SharedViewModel
                } else {

                    requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE); // 請求位置權限
                    makeToast("請開啟定位權限",1000);
                }
            }else{
                makeToast("請選擇要套用的路線",1000);
            }

        });
    }

    // 重置 RecyclerView 的數據
    private void RecycleViewReset() {
        arrayList.clear();  // 清空列表
        addItemFromDB();        // 從資料庫中重新加載數據
        listAdapterBook.notifyDataSetChanged(); // 通知適配器更新數據
    }

    // 設置工具列
    private void setupActionBar() {

        ActionBar actionBar = ((AppCompatActivity) requireActivity()).getSupportActionBar();

        if (actionBar != null) {
            if (drawerLayout != null) {
                if (toggle == null) {
                    toggle = new ActionBarDrawerToggle(requireActivity(), drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close);
                    drawerLayout.addDrawerListener(toggle);
                    toggle.syncState();
                }
                toggle.getDrawerArrowDrawable().setColor(getResources().getColor(R.color.green));

                CardView cardViewtitle = new CardView(requireContext());
                cardViewtitle.setLayoutParams(new CardView.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT));
                Drawable drawable = ContextCompat.getDrawable(requireContext(), R.drawable.cardviewtitle_shape);
                cardViewtitle.setBackground(drawable);

                LinearLayout linearLayout = new LinearLayout(requireContext());
                linearLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
                linearLayout.setOrientation(LinearLayout.HORIZONTAL);

                // 建立頁面小圖示
                ImageView mark = new ImageView(requireContext());
                mark.setImageResource(R.drawable.routemark);
                mark.setPadding(10, 10, 5, 10);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(100, 100);
                params.setMarginStart(10); // 設置左邊距
                mark.setLayoutParams(params);

                // 建立頁面標題
                TextView bookTitle = new TextView(requireContext());
                bookTitle.setText("收藏路線");
                bookTitle.setTextSize(15);
                bookTitle.setTextColor(getResources().getColor(R.color.green)); // 改變文字顏色
                bookTitle.setPadding(10, 10, 30, 10); // 設置內距

                // 將 ImageView 和 TextView 加入到線性佈局
                linearLayout.addView(mark);
                linearLayout.addView(bookTitle);
                cardViewtitle.addView(linearLayout);

                actionBar.setDisplayShowTitleEnabled(false); // 隱藏預設標題
                actionBar.setDisplayShowCustomEnabled(true);
                actionBar.setCustomView(cardViewtitle, new ActionBar.LayoutParams(ActionBar.LayoutParams.WRAP_CONTENT, ActionBar.LayoutParams.WRAP_CONTENT, Gravity.END)); // 將 CardView 設置為工具列的自訂視圖，並對齊右側

                actionBar.show(); // 顯示工具列
            }
        }
    }

    // 設置側邊欄
    private void setupNavigationDrawer() {
        drawerLayout = requireActivity().findViewById(R.id.drawerLayout);
        toolbar = requireActivity().findViewById(R.id.toolbar);
        toggle = new ActionBarDrawerToggle(requireActivity(), drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        toggle.getDrawerArrowDrawable().setColor(getResources().getColor(R.color.green));
    }

    // 設置 RecyclerView
    private void setupRecyclerViews() {

        recyclerViewBook = rootView.findViewById(R.id.recycleView_book);
        recyclerViewBook.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerViewBook.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));
        listAdapterBook = new ListAdapterHistory(arrayList);

        recyclerViewBook.setAdapter(listAdapterBook); // 設置適配器

        // 添加左滑刪除功能
        SwipeToDeleteCallback swipeToDeleteCallback = new SwipeToDeleteCallback(getContext(), new SwipeToDeleteCallback.OnSwipedListener() {
            @Override
            public void onSwiped(int position) {
                // 當滑動刪除時顯示刪除確認對話框
                View itemView = recyclerViewBook.getLayoutManager().findViewByPosition(position);
                if (itemView != null) {
                    showDeleteConfirmationDialog(itemView, position);
                }
            }
        });

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(swipeToDeleteCallback);
        itemTouchHelper.attachToRecyclerView(recyclerViewBook); // 將滑動刪除功能附加到 RecyclerView
    }

    // 顯示刪除確認對話框
    private void showDeleteConfirmationDialog(View view, int position) {

        Context context = view.getContext();
        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.CustomAlertDialog); // 創建對話框

        // 套用自訂佈局
        LayoutInflater inflater = LayoutInflater.from(context);
        View customView = inflater.inflate(R.layout.dialog_deltebook, null);
        builder.setView(customView); // 設置佈局

        // 創建並顯示對話框
        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false); // 設置點擊外部不會關閉對話框

        Button positiveButton = customView.findViewById(R.id.Popupsure);
        Button negativeButton = customView.findViewById(R.id.PopupCancel);

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT)); // 設置背景為透明

        //設置對話框裡確認按鈕的點擊監聽器
        positiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeItemFromDB(position); // 刪除項目
                changeNotification(); // 更新UI
                dialog.cancel(); // 關閉對話框
            }
        });

        //設置對話框裡取消按鈕的點擊監聽器
        negativeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listAdapterBook.notifyItemChanged(position); // 通知適配器取消使用者的選取
                dialog.cancel(); // 關閉對話框
            }

        });

        dialog.show(); // 顯示對話框
    }

    // 刪除項目
    private void removeItemFromDB(int position) {

        HashMap<String, String> item = arrayList.get(position);
        String time = item.get("time");

        // 從資料庫中刪除選中的項目
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        db.beginTransaction();
        try {

            String locationId = DatabaseUtils.stringForQuery(db, "SELECT " + BookTable.COLUMN_LOCATION_ID +
                    " FROM " + BookTable.TABLE_NAME + " WHERE " + BookTable.COLUMN_START_TIME + " = ?", new String[]{time});


            String locationUUID = DatabaseUtils.stringForQuery(db, "SELECT " + LocationTable2.COLUMN_ALARM_NAME +
                    " FROM " + LocationTable2.TABLE_NAME + " WHERE " + LocationTable2.COLUMN_LOCATION_ID + "= ?", new String[]{locationId});

            // 從路線表中刪除
            db.execSQL("DELETE FROM " + BookTable.TABLE_NAME +
                    " WHERE " + BookTable.COLUMN_START_TIME + " = ?", new String[]{time});

            // 從地點表中刪除
            db.execSQL("DELETE FROM " + LocationTable2.TABLE_NAME +
                    " WHERE " + LocationTable2.COLUMN_ALARM_NAME + " = ?", new String[]{locationUUID});

            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace(); // 打印錯誤信息
        } finally {
            db.endTransaction();
            db.close(); // 關閉資料庫
        }

        arrayList.remove(position); // 從列表中刪除項目
        listAdapterBook.notifyItemRemoved(position); // 通知適配器該項目已被刪除
    }

    // 從資料庫中添加數據到列表
    private void addItemFromDB() {

        String time;

        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM book WHERE arranged_id=0", null);  //取得路線資料表的Cursor
        if (cursor != null) {
            while (cursor.moveToNext()) {

                String placeNameTemp = cursor.getString(3);
                time = cursor.getString(1);

                HashMap<String, String> hashMap = new HashMap<>();
                hashMap.put("placeName2", placeNameTemp);
                hashMap.put("time", time);
                arrayList.add(0, hashMap); // 將數據插入到列表
            }
            cursor.close(); // 關閉 Cursor
        }
        db.close(); // 關閉資料庫
    }

    // 將數據保存到ShareViewModel
    private void saveInShareviewModel() {

        sharedViewModel.clearAll(); // 清空共享數據
        String time = "";
        for (HashMap<String, String> item : arrayList) {
            if ("true".equals(item.get("isSelected"))) {
                time = item.get("time"); // 獲取選中的時間
            }
        }

        int count = 0;  //讓存進ShareViewModel可以從第一個位置開始儲存
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + BookDatabaseHelper.BookTable.TABLE_NAME +
                " WHERE " + BookDatabaseHelper.BookTable.COLUMN_START_TIME + " = ?", new String[]{time});   //取得路線資料表的Cursor

        db.beginTransaction();
        try {
            while (cursor.moveToNext()) {

                // 獲取位置數據並存進ShareViewModel
                String locationId = cursor.getString(0);
                Cursor locationCursor = db.rawQuery("SELECT * FROM " + BookDatabaseHelper.LocationTable2.TABLE_NAME +
                        " WHERE " + BookDatabaseHelper.LocationTable2.COLUMN_LOCATION_ID + " = ?", new String[]{locationId});

                if (locationCursor.moveToFirst()) {
                    String placeName    = locationCursor.getString(3);
                    Double latitude     = locationCursor.getDouble(2);
                    Double longitude    = locationCursor.getDouble(1);
                    String city = locationCursor.getString(5);
                    String area = locationCursor.getString(6);
                    String note = locationCursor.getString(7);

                    boolean vibrate     = locationCursor.getInt(8) != 0;
                    boolean ringtone    = locationCursor.getInt(9) != 0;
                    int notificationTime = locationCursor.getInt(10);

                    sharedViewModel.setDestination(placeName, latitude, longitude);
                    sharedViewModel.setCapital(city);
                    sharedViewModel.setArea(area);
                    sharedViewModel.setNote(note, count);
                    sharedViewModel.setVibrate(vibrate,count);
                    sharedViewModel.setRingtone(ringtone,count);
                    sharedViewModel.setNotification(notificationTime,count++);
                    getLastKnownLocation(); // 獲取當前位置
                }
                locationCursor.close(); // 關閉 Cursor
            }
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e("BookFragment", "Error while trying to save in ShareViewModel", e); // 打印錯誤信息
        } finally {
            db.endTransaction(); // 結束事務
            cursor.close(); // 關閉 Cursor

            //頁面轉換到創建路線 505-509
            CreateLocation createLocationFragment = new CreateLocation();
            FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fl_container, createLocationFragment);
            transaction.addToBackStack(null);
            transaction.commit();

            //側邊選單顯示改成路線規劃
            NavigationView navigationView = getActivity().findViewById(R.id.navigation_view);
            navigationView.setCheckedItem(R.id.action_home);
        }
    }

    // 獲取當前最後已知的位置
    @SuppressLint("MissingPermission")
    private void getLastKnownLocation() {
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(requireActivity(), new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            sharedViewModel.setnowLocation(location.getLatitude(), location.getLongitude());
                            Log.d("Location", "Latitude: " + location.getLatitude() + " Longitude: " + location.getLongitude());
                        } else {
                            makeToast("無法取得現在位置",1000);
                        }
                    }
                });
    }

    // 更改UI信息
    private void changeNotification() {
        if (arrayList.isEmpty()) {
            TextView notification = rootView.findViewById(R.id.textView7);
            notification.setText("目前還沒有東西喔");
        } else {
            TextView notification = rootView.findViewById(R.id.textView7);
            notification.setText("");
        }
    }

    public void makeToast(String message, int durationInMillis) {
        // 創建 Toast
        Toast toast = Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT);
        toast.show();

        // 使用 Handler 來控制顯示時長
        new Handler().postDelayed(toast::cancel, durationInMillis);
    }

    @Override
    public void onResume() {
        super.onResume();
        setupActionBar();   // 設置工具列
        setupNavigationDrawer();    // 設置側邊欄
        RecycleViewReset();         // 重置 RecyclerView
        changeNotification();       // 更新UI
    }

    @Override
    public void onPause() {
        super.onPause();
        ActionBar actionBar = ((AppCompatActivity) requireActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowCustomEnabled(false);
            actionBar.setCustomView(null);
            actionBar.setDisplayShowTitleEnabled(true);
        }
    }
}
