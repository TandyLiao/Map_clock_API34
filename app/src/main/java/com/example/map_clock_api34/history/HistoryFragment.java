// 引入必要的套件和常量
package com.example.map_clock_api34.history;

import static android.content.Context.MODE_PRIVATE;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
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
import com.example.map_clock_api34.history.HistoryListAdapter.ListAdapterHistory;
import com.example.map_clock_api34.history.HistoryDatabaseHelper.LocationTable;
import com.example.map_clock_api34.history.HistoryDatabaseHelper.HistoryTable;
import com.example.map_clock_api34.CreateLocation.CreateLocationFragment;
import com.example.map_clock_api34.TutorialFragment;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.navigation.NavigationView;

import android.widget.Button;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;

public class HistoryFragment extends Fragment {

    // 其他必要變數
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001; // 位置權限請求代碼

    SharedViewModel sharedViewModel;

    private HistoryDatabaseHelper dbHelper; // 資料庫輔助類別
    private FusedLocationProviderClient fusedLocationClient; // 用來獲取裝置位置的類別

    // 定義視圖變數
    private View rootView;
    private View overlayView;
    private Button btnEdit, btnSelect, btnClearAll; // 三個主要操作按鈕：編輯、選擇/套用、清除所有

    // RecyclerView 相關變數
    private RecyclerView recyclerViewHistory;
    private ListAdapterHistory listAdapterHistory; // 自訂的 RecyclerView 適配器
    private final ArrayList<HashMap<String, String>> arrayList = new ArrayList<>(); // 歷史紀錄的資料列表

    private boolean isEdit, isDelete; // 是否為編輯模式和刪除模式

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.history_fragment_history, container, false);

        // 檢查使用者是否已經造訪過，未造訪則跳轉到教學頁面
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("MyPrefs", MODE_PRIVATE);
        boolean isLoggedIn = sharedPreferences.getBoolean("HistoryLogin", false);

        Fragment currentFragment = getActivity().getSupportFragmentManager().findFragmentById(R.id.fl_container);
        if(isLoggedIn==false)
        {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt("WhichPage",1);
            //editor.putBoolean("HistoryLogin",true);
            editor.apply();

            TutorialFragment tutorialFragment = new TutorialFragment();
            FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
            transaction.hide(currentFragment);
            transaction.add(R.id.fl_container, tutorialFragment);
            transaction.addToBackStack(null);
            transaction.commit();
        }

        dbHelper = new HistoryDatabaseHelper(requireContext());
        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        setupActionBar(); // 設置 ActionBar
        setupButtons(); // 設置按鈕
        setupRecyclerViews(); // 設置 RecyclerView

        return rootView; // 返回主視圖
    }

    // 設置主要操作按鈕
    private void setupButtons() {
        // 編輯按鈕
        btnEdit = rootView.findViewById(R.id.EditButton);
        btnEdit.setOnClickListener(v -> {
            isEdit = !isEdit; // 切換編輯模式
            if (!isEdit) {
                isDelete = false; // 如果退出編輯模式，刪除模式也設為關閉
                updateButtonState(); // 更新按鈕狀態
                clearSelections(); // 清除所有選擇的項目
            } else {
                listAdapterHistory.clearSelections(); // 清除列表中的選擇
                updateButtonState(); // 更新按鈕狀態
            }
            listAdapterHistory.setEditMode(isEdit, isEdit); // 設置編輯模式
            updateButtonState(); // 更新按鈕狀態
        });

        // 清除資料庫按鈕
        btnClearAll = rootView.findViewById(R.id.ClearAllButton);
        btnClearAll.setOnClickListener(v -> showPopupWindowForClearAll()); // 顯示清除確認彈窗

        // 套用/刪除按鈕
        btnSelect = rootView.findViewById(R.id.SelectButton);
        btnSelect.setOnClickListener(v -> {
            if (!isEdit) {
                // 如果不是編輯模式，則執行套用操作
                if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    saveInShareviewModel(); // 將選中的資料儲存至 SharedViewModel 中
                } else {
                    requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
                    makeToast("請開啟定位權限",1000);
                }
            } else {
                // 編輯模式下，切換刪除狀態
                isEdit = true;
                isDelete = !isDelete;
                updateButtonState(); // 更新按鈕狀態
                listAdapterHistory.setEditMode(isEdit, isEdit); // 更新編輯模式
                if (isDelete) {
                    showPopupWindowForDelete(); // 顯示刪除確認彈窗
                } else {
                    listAdapterHistory.clearSelections(); // 清除選擇
                    updateButtonState(); // 更新按鈕狀態
                }
            }
        });

        updateButtonState(); // 初始化按鈕狀態
    }

    // 初始化 RecyclerView
    private void setupRecyclerViews() {
        recyclerViewHistory = rootView.findViewById(R.id.recycleViewHistory);
        recyclerViewHistory.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerViewHistory.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));
        listAdapterHistory = new ListAdapterHistory(arrayList);
        listAdapterHistory.setOnItemSelectedListener(this::updateButtonState); // 監聽選擇事件
        recyclerViewHistory.setAdapter(listAdapterHistory);
    }

    // 重置 RecyclerView，重新加載資料
    private void RecycleViewReset() {
        arrayList.clear();
        addItemFromDB(); // 從資料庫載入資料
        listAdapterHistory.notifyDataSetChanged();
        updateButtonState(); // 更新按鈕狀態
    }

    // 從資料庫載入歷史資料
    private void addItemFromDB() {
        String time;
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM history WHERE arranged_id=0", null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                String placeNameTemp = cursor.getString(3);
                int index = placeNameTemp.indexOf("->");
                String beforeArrow = placeNameTemp.substring(0, index);
                if (beforeArrow.length() > 20) {
                    beforeArrow = beforeArrow.substring(0, 20) + "...";
                }
                String afterArrow = placeNameTemp.substring(index + 2);
                if (afterArrow.length() > 20) {
                    afterArrow = afterArrow.substring(0, 20) + "...";
                }
                time = cursor.getString(1);

                HashMap<String, String> hashMap = new HashMap<>();
                hashMap.put("placeName", beforeArrow);
                hashMap.put("placeName2", "\u2193");
                hashMap.put("placeName3", afterArrow);
                hashMap.put("time", time);
                arrayList.add(0, hashMap);
            }
            cursor.close();
        }
        db.close();
    }

    // 從資料庫刪除選中的項目
    private void deleteItemFromDB() {
        ArrayList<HashMap<String, String>> selectedItems = new ArrayList<>();
        for (HashMap<String, String> item : arrayList) {
            if ("true".equals(item.get("isSelected"))) {
                selectedItems.add(item);
            }
        }

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            for (HashMap<String, String> item : selectedItems) {
                String time = item.get("time");
                String locationId = DatabaseUtils.stringForQuery(db,
                        "SELECT " + HistoryTable.COLUMN_LOCATION_ID +
                                " FROM " + HistoryTable.TABLE_NAME +
                                " WHERE " + HistoryTable.COLUMN_START_TIME + " = ?", new String[]{time});

                String locationUUID = DatabaseUtils.stringForQuery(db,
                        "SELECT " + LocationTable.COLUMN_ALARM_NAME +
                                " FROM " + LocationTable.TABLE_NAME +
                                " WHERE " + LocationTable.COLUMN_LOCATION_ID + "= ?", new String[]{locationId});
                db.execSQL("DELETE FROM " + HistoryTable.TABLE_NAME +
                        " WHERE " + HistoryTable.COLUMN_START_TIME + " = ?", new String[]{time});
                db.execSQL("DELETE FROM " + LocationTable.TABLE_NAME +
                        " WHERE " + LocationTable.COLUMN_ALARM_NAME + " = ?", new String[]{locationUUID});
            }
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
            db.close();
        }

        arrayList.removeAll(selectedItems); // 移除選中的項目
        listAdapterHistory.notifyDataSetChanged();
        isEdit = false;
        isDelete = false;
        updateButtonState();
    }

    // 設置 ActionBar
    private void setupActionBar() {
        ActionBar actionBar = ((AppCompatActivity) requireActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
            actionBar.setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(getActivity(), R.color.lightgreen)));
        }

        CardView cardViewTitle = new CardView(requireContext());
        cardViewTitle.setLayoutParams(new CardView.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT));
        Drawable drawable = ContextCompat.getDrawable(requireContext(), R.drawable.cardviewtitle_shape);
        cardViewTitle.setBackground(drawable);

        LinearLayout linearLayout = new LinearLayout(requireContext());
        linearLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);

        ImageView mark = new ImageView(requireContext());
        mark.setImageResource(R.drawable.history_record1);
        mark.setPadding(10, 10, 5, 10);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(100, 100);
        params.setMarginStart(10);
        mark.setLayoutParams(params);

        TextView bookTitle = new TextView(requireContext());
        bookTitle.setText("歷史紀錄");
        bookTitle.setTextSize(15);
        bookTitle.setTextColor(getResources().getColor(R.color.green));
        bookTitle.setPadding(10, 10, 30, 10);

        linearLayout.addView(mark);
        linearLayout.addView(bookTitle);
        cardViewTitle.addView(linearLayout);

        actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setDisplayShowCustomEnabled(true);
            actionBar.setCustomView(cardViewTitle, new ActionBar.LayoutParams(ActionBar.LayoutParams.WRAP_CONTENT, ActionBar.LayoutParams.WRAP_CONTENT, Gravity.END));
            actionBar.show();
        }
    }

    // 更新按鈕狀態
    private void updateButtonState() {
        // 根據 arrayList 的狀態來更新按鈕是否可用
        if (arrayList.isEmpty()) {
            TextView notification = rootView.findViewById(R.id.textView5);
            notification.setText("目前還沒有記錄喔");
        } else {
            TextView notification = rootView.findViewById(R.id.textView5);
            notification.setText("");
        }

        boolean hasItems = arrayList.isEmpty();
        boolean hasSelectedItems = false;
        for (HashMap<String, String> item : arrayList) {
            if ("true".equals(item.get("isSelected"))) {
                hasSelectedItems = true;
                break;
            }
        }

        // 根據是否有選擇的項目，更新按鈕的啟用狀態及顏色
        if (hasItems) {
            btnEdit.setEnabled(true);
            btnSelect.setEnabled(false);
            btnClearAll.setEnabled(false);
            btnClearAll.setVisibility(View.INVISIBLE);
            btnEdit.setTextColor(ContextCompat.getColor(requireContext(), R.color.lightgreen));
            btnSelect.setTextColor(ContextCompat.getColor(requireContext(), R.color.lightgreen));
            btnEdit.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.btn_unclickable));
            btnSelect.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.btn_unclickable));
            btnClearAll.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.btn_unclickable));
        } else {
            btnEdit.setEnabled(true);
            btnEdit.setTextColor(ContextCompat.getColor(requireContext(), R.color.darkgreen));
            btnEdit.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.btn_additem));
        }

        if (!isEdit) {
            btnEdit.setText("編輯");
            btnSelect.setText("套用");
            btnClearAll.setVisibility(View.INVISIBLE);
            btnClearAll.setEnabled(false);

            if (hasSelectedItems) {
                btnSelect.setEnabled(true);
                btnSelect.setTextColor(ContextCompat.getColor(requireContext(), R.color.darkgreen));
                btnSelect.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.btn_additem));
            } else {
                btnSelect.setEnabled(false);
                btnSelect.setTextColor(ContextCompat.getColor(requireContext(), R.color.lightgreen));
                btnSelect.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.btn_unclickable));
            }
        } else {
            btnEdit.setText("返回");
            btnSelect.setText("刪除");
            btnClearAll.setVisibility(View.VISIBLE);
            btnClearAll.setEnabled(true);
            btnClearAll.setTextColor(ContextCompat.getColor(requireContext(), R.color.darkgreen));
            btnClearAll.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.btn_additem));
            btnSelect.setEnabled(hasSelectedItems);
            btnSelect.setTextColor(hasSelectedItems ? ContextCompat.getColor(requireContext(), R.color.darkgreen) : ContextCompat.getColor(requireContext(), R.color.lightgreen));
            btnSelect.setBackground(ContextCompat.getDrawable(requireContext(), hasSelectedItems ? R.drawable.btn_additem : R.drawable.btn_unclickable));
        }
    }

    // 清除所有選擇
    private void clearSelections() {
        for (HashMap<String, String> item : arrayList) {
            item.put("isSelected", "false");
        }
        listAdapterHistory.notifyDataSetChanged();
        updateButtonState(); // 更新按鈕狀態
    }

    // 將選中的項目儲存到 SharedViewModel
    private void saveInShareviewModel() {
        sharedViewModel.clearAll();
        String time = "";
        for (HashMap<String, String> item : arrayList) {
            if ("true".equals(item.get("isSelected"))) {
                time = item.get("time");
            }
        }
        int count = 0;
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + HistoryDatabaseHelper.HistoryTable.TABLE_NAME + " WHERE " + HistoryDatabaseHelper.HistoryTable.COLUMN_START_TIME + " = ?", new String[]{time});
        db.beginTransaction();
        try {
            while (cursor.moveToNext()) {
                String locationId = cursor.getString(0);
                Cursor locationCursor = db.rawQuery("SELECT * FROM " + HistoryDatabaseHelper.LocationTable.TABLE_NAME + " WHERE " + HistoryDatabaseHelper.LocationTable.COLUMN_LOCATION_ID + " = ?", new String[]{locationId});
                if (locationCursor.moveToFirst()) {
                    String placeName = locationCursor.getString(3);
                    Double latitude = locationCursor.getDouble(2);
                    Double longitude = locationCursor.getDouble(1);
                    String city = locationCursor.getString(5);
                    String area = locationCursor.getString(6);
                    String note = locationCursor.getString(7);
                    boolean vibrate = locationCursor.getInt(8) != 0;
                    boolean ringtone = locationCursor.getInt(9) != 0;
                    int notificationTime = locationCursor.getInt(10);
                    sharedViewModel.setDestination(placeName, latitude, longitude);
                    sharedViewModel.setCapital(city);
                    sharedViewModel.setArea(area);
                    sharedViewModel.setNote(note, count);
                    sharedViewModel.setVibrate(vibrate, count);
                    sharedViewModel.setRingtone(ringtone, count);
                    sharedViewModel.setNotification(notificationTime, count++);

                    getLastKnownLocation(); // 獲取當前位置
                }
                locationCursor.close();
            }
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
            db.close();
        }
        openCreaLocationFragment(); // 打開路線規劃頁面
    }

    // 獲取當前裝置位置
    @SuppressLint("MissingPermission")
    private void getLastKnownLocation() {
        fusedLocationClient.getLastLocation().addOnSuccessListener(requireActivity(), new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    sharedViewModel.setNowLocation(location.getLatitude(), location.getLongitude());
                    Log.d("Location", "Latitude: " + location.getLatitude() + " Longitude: " + location.getLongitude());
                } else {
                    makeToast("找不到當前位置",1000);
                }
            }
        });
    }

    // 打開路線規劃頁面
    private void openCreaLocationFragment() {
        CreateLocationFragment createLocationFragment = new CreateLocationFragment();
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fl_container, createLocationFragment);
        transaction.addToBackStack(null);
        transaction.commit();

        NavigationView navigationView = getActivity().findViewById(R.id.navigation_view);
        navigationView.setCheckedItem(R.id.action_home);
    }

    // 顯示清空所有紀錄的確認彈窗
    private void showPopupWindowForClearAll() {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.popupwindow_reset_button, null, false);
        PopupWindow popupWindow = new PopupWindow(view, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        popupWindow.setWidth(700);
        popupWindow.setFocusable(false);
        popupWindow.setOutsideTouchable(false);

        // 顯示 PopupWindow
        popupWindow.showAtLocation(rootView, Gravity.CENTER, 0, 0);
        popupWindow.setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        // 疊加透明的 View
        overlayView = new View(getContext());
        overlayView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        overlayView.setBackgroundColor(ContextCompat.getColor(getContext(), android.R.color.transparent));
        overlayView.setClickable(true);
        ((ViewGroup) rootView).addView(overlayView);

        // 設置彈窗中的文字
        TextView warning = view.findViewById(R.id.txtNote);
        warning.setText("資料即將全部刪除");

        // 設置取消按鈕行為
        Button BTNPopup = (Button) view.findViewById(R.id.PopupCancel);
        BTNPopup.setOnClickListener(v -> {
            popupWindow.dismiss();
            removeOverlayView(); // 移除疊加的透明 View
        });

        // 設置確認按鈕行為
        Button btnsure = (Button) view.findViewById(R.id.Popupsure);
        btnsure.setOnClickListener(v -> {
            dbHelper.clearAllTables(); // 清除資料庫中的所有資料
            makeToast("已清除所有紀錄",1000);
            arrayList.clear();
            listAdapterHistory.notifyDataSetChanged();
            isEdit = false; // 返回初始狀態
            isDelete = false; // 返回初始狀態
            updateButtonState(); // 更新按鈕狀態
            popupWindow.dismiss();
            removeOverlayView(); // 移除疊加的透明 View
        });
    }

    // 顯示刪除確認彈窗
    private void showPopupWindowForDelete() {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.popupwindow_reset_button, null, false);
        PopupWindow popupWindow = new PopupWindow(view, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        popupWindow.setWidth(700);
        popupWindow.setFocusable(false);
        popupWindow.setOutsideTouchable(false);

        popupWindow.showAtLocation(rootView, Gravity.CENTER, 0, 0);
        popupWindow.setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        overlayView = new View(getContext());
        overlayView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        overlayView.setBackgroundColor(ContextCompat.getColor(getContext(), android.R.color.transparent));
        overlayView.setClickable(true);
        ((ViewGroup) rootView).addView(overlayView);

        TextView warning = view.findViewById(R.id.txtNote);
        warning.setText("資料即將刪除");

        Button BTNPopup = (Button) view.findViewById(R.id.PopupCancel);
        BTNPopup.setOnClickListener(v -> {
            isDelete=false;
            popupWindow.dismiss();
            removeOverlayView();
        });

        Button btnsure = (Button) view.findViewById(R.id.Popupsure);
        btnsure.setOnClickListener(v -> {
            deleteItemFromDB();
            popupWindow.dismiss();
            removeOverlayView();
            updateButtonState();
        });
    }

    // 移除疊加的透明 View
    private void removeOverlayView() {
        if (overlayView != null && overlayView.getParent() != null) {
            ((ViewGroup) overlayView.getParent()).removeView(overlayView);
            overlayView = null;
        }
    }

    public void makeToast(String message, int durationInMillis) {
        // 創建 Toast
        Toast toast = Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT);
        toast.show();

        // 使用 Handler 來控制顯示時長
        new Handler().postDelayed(toast::cancel, durationInMillis);
    }

    // Fragment 重啟時更新按鈕狀態和重置 RecyclerView
    @Override
    public void onResume() {
        super.onResume();
        isEdit = false;
        isDelete = false;
        updateButtonState();
        RecycleViewReset();
    }

}
