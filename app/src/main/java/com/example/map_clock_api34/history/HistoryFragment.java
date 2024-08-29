package com.example.map_clock_api34.history;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
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

import com.example.map_clock_api34.HistoryDatabase.HistoryDatabaseHelper;
import com.example.map_clock_api34.R;
import com.example.map_clock_api34.SharedViewModel;
import com.example.map_clock_api34.history.ListAdapter.ListAdapterHistory;
import com.example.map_clock_api34.HistoryDatabase.HistoryDatabaseHelper.LocationTable;
import com.example.map_clock_api34.HistoryDatabase.HistoryDatabaseHelper.HistoryTable;
import com.example.map_clock_api34.home.HomeFragment;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.navigation.NavigationView;

import android.widget.Button;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;

public class HistoryFragment extends Fragment {

    boolean isEdit, isDelete;

    View rootView;
    View overlayView;
    Button btnEdit, btnSelect, btnClearAll;

    RecyclerView recyclerViewHistory;
    ListAdapterHistory listAdapterHistory;
    ArrayList<HashMap<String, String>> arrayList = new ArrayList<>();
    ArrayList<HashMap<String, String>> toRemove = new ArrayList<>();
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    SharedViewModel sharedViewModel;

    private HistoryDatabaseHelper dbHelper;
    private FusedLocationProviderClient fusedLocationClient;

    private boolean isFirstEditClick = true;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.history_fragment_history, container, false);

        dbHelper = new HistoryDatabaseHelper(requireContext());
        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
        //dbHelper.clearAllTables();
        setupActionBar();
        setupButtons();
        setupRecyclerViews();
        return rootView;
    }

    private void setupButtons() {
        // 編輯以及返回按鈕
        btnEdit = rootView.findViewById(R.id.EditButton);
        btnEdit.setOnClickListener(v -> {
            isEdit = !isEdit;
            // 如果 isEdit 為 false，則進入初始狀態
            if (!isEdit) {
                isDelete = false;
                updateButtonState();
                clearSelections();   // 清除選擇
            } else {
                listAdapterHistory.clearSelections();
                updateButtonState();
            }
            listAdapterHistory.setEditMode(isEdit, isEdit); // 設置適配器的編輯模式
            updateButtonState();

        });

        // 清除資料庫按鈕
        btnClearAll = rootView.findViewById(R.id.ClearAllButton);
        btnClearAll.setOnClickListener(v -> showPopupWindowForClearAll());



// 把疊加在底層的View刪掉


        // 套用和刪除按鈕
        btnSelect = rootView.findViewById(R.id.SelectButton);
        btnSelect.setOnClickListener(v -> {
            if (!isEdit) {
                // 正常模式下的套用功能
                if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    // 套用按鈕在這實現功能
                    saveInShareviewModel();
                } else {
                    requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
                    Toast.makeText(getActivity(), "請開啟定位權限", Toast.LENGTH_SHORT).show();
                }


            } else {
                isEdit = true;
                isDelete = !isDelete;
                updateButtonState();
                listAdapterHistory.setEditMode(isEdit, isEdit);

                if (isDelete) {
                    ShowPopupWindow();
                } else {
                    listAdapterHistory.clearSelections();
                    updateButtonState();

                }
            }
        });

        updateButtonState(); // 初始化按鈕狀態
    }
    private void showPopupWindowForClearAll() {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.popupwindow_reset_button, null, false);
        PopupWindow popupWindow = new PopupWindow(view, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        popupWindow.setWidth(700);
        popupWindow.setFocusable(false);
        popupWindow.setOutsideTouchable(false);

        // 讓PopupWindow顯示出來的關鍵句
        popupWindow.showAtLocation(rootView, Gravity.CENTER, 0, 0);
        popupWindow.setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        // 疊加View在底下，讓她不會按到底層就跳掉
        overlayView = new View(getContext());
        overlayView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        overlayView.setBackgroundColor(ContextCompat.getColor(getContext(), android.R.color.transparent));
        overlayView.setClickable(true);
        ((ViewGroup) rootView).addView(overlayView);

        // PopupWindow的文字顯示
        TextView warning = view.findViewById(R.id.txtNote);
        warning.setText("資料即將全部刪除");

        // PopUpWindow的取消按鈕
        Button BTNPopup = (Button) view.findViewById(R.id.PopupCancel);
        BTNPopup.setOnClickListener(v -> {
            popupWindow.dismiss();
            // 移除疊加在底下防止點擊其他區域的View
            removeOverlayView();
        });

        // PopupWindow的確認按鈕
        Button btnsure = (Button) view.findViewById(R.id.Popupsure);
        btnsure.setOnClickListener(v -> {
            // 清除所有記錄
            dbHelper.clearAllTables();
            Toast.makeText(getActivity(), "已清除所有紀錄", Toast.LENGTH_SHORT).show();

            arrayList.clear();
            listAdapterHistory.notifyDataSetChanged();

            isEdit = false;    // 回到初始狀態
            isDelete = false;  // 回到初始狀態
            updateButtonState(); // 更新按鈕狀態

            popupWindow.dismiss();
            removeOverlayView();
        });
    }

    private void setupRecyclerViews() {
        recyclerViewHistory = rootView.findViewById(R.id.recycleViewHistory);
        recyclerViewHistory.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerViewHistory.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));
        listAdapterHistory = new ListAdapterHistory(arrayList);
        //檢測是否有選擇RecycleView的監聽器
        listAdapterHistory.setOnItemSelectedListener(this::updateButtonState);
        recyclerViewHistory.setAdapter(listAdapterHistory);
    }

    private void RecycleViewReset() {
        arrayList.clear();
        addFromDB();
        listAdapterHistory.notifyDataSetChanged();
        updateButtonState(); // 更新按鈕狀態

    }

    private void addFromDB() {

        String time;

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM history WHERE arranged_id=0", null);
        if (cursor != null) {
            while (cursor.moveToNext()) {

                String placeNameTemp = cursor.getString(3);
                //找到"->"的位置
                int index = placeNameTemp.indexOf("->");
                //把"->"前的資料抓出來
                String beforeArrow = placeNameTemp.substring(0, index);
                if (beforeArrow.length() > 20) {
                    beforeArrow = beforeArrow.substring(0, 20) + "...";
                }
                //把"->"後的資料抓出來
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
                arrayList.add(hashMap);
            }
            cursor.close();
        }
        db.close();
    }

    @Override
    public void onResume() {
        super.onResume();

        //不在編輯模式下按鈕布林值
        isEdit = false;
        //刪除按鈕有無出現的布林值
        isDelete = false;
        Log.d("HistoryFragment", "onResume called");
        updateButtonState();
        RecycleViewReset();
    }

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
        linearLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        ));
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);

        ImageView mark = new ImageView(requireContext());
        mark.setImageResource(R.drawable.history_record1);
        mark.setPadding(10, 10, 5, 10);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                100, 100
        );
        params.setMarginStart(10);
        mark.setLayoutParams(params);

        TextView bookTitle = new TextView(requireContext());
        bookTitle.setText("歷史紀錄");
        bookTitle.setTextSize(15);
        bookTitle.setTextColor(getResources().getColor(R.color.green));
        bookTitle.setPadding(10, 10, 10, 10);

        linearLayout.addView(mark);
        linearLayout.addView(bookTitle);
        cardViewTitle.addView(linearLayout);

        actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setDisplayShowCustomEnabled(true);
            actionBar.setCustomView(cardViewTitle, new ActionBar.LayoutParams(
                    ActionBar.LayoutParams.WRAP_CONTENT,
                    ActionBar.LayoutParams.WRAP_CONTENT,
                    Gravity.END));
            actionBar.show();
        }
    }

    private void updateButtonState() {

        if(arrayList.isEmpty()){
            TextView notification = rootView.findViewById(R.id.textView5);
            notification.setText("目前還沒有記錄喔");
        }else{
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

        //RecycleView沒有東西時的按鈕狀態
        if (hasItems) {

            btnEdit.setEnabled(true);
            btnSelect.setEnabled(false);
            btnClearAll.setEnabled(false);
            btnClearAll.setVisibility(View.INVISIBLE);

            //兩個按鈕顏色的改變
            btnEdit.setTextColor(ContextCompat.getColor(requireContext(), R.color.lightgreen));
            btnSelect.setTextColor(ContextCompat.getColor(requireContext(), R.color.lightgreen));

            //三個按鈕不能被點擊
            btnEdit.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.btn_unclickable));
            btnSelect.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.btn_unclickable));
            btnClearAll.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.btn_unclickable));
        } else {
            //RecycleView有東西時的按鈕狀態
            btnEdit.setEnabled(true);
            btnEdit.setTextColor(ContextCompat.getColor(requireContext(), R.color.darkgreen));
            btnEdit.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.btn_additem));
        }

        //如果不在編輯模式下的按鈕狀態
        if (!isEdit) {
            btnEdit.setText("編輯");
            btnSelect.setText("套用");
            btnClearAll.setVisibility(View.INVISIBLE);
            btnClearAll.setEnabled(false);

            //不是編輯模式下的RecycleView選擇
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
            //在編輯模式下的按鈕狀態
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

    private void clearSelections() {
        for (HashMap<String, String> item : arrayList) {
            item.put("isSelected", "false");
        }
        listAdapterHistory.notifyDataSetChanged();
        updateButtonState(); // 更新按鈕狀態
    }

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
                    //設定
                    boolean vibrate=locationCursor.getInt(8)!=0;
                    boolean ringtone=locationCursor.getInt(9)!=0;
                    int notificationTime=locationCursor.getInt(10);

                    sharedViewModel.setDestination(placeName, latitude, longitude);
                    sharedViewModel.setCapital(city);
                    sharedViewModel.setArea(area);
                    sharedViewModel.setNote(note, count++);
                    //設定
                    sharedViewModel.setVibrate(vibrate,count++);
                    sharedViewModel.setRingtone(ringtone,count++);
                    sharedViewModel.setNotification(notificationTime,count++);


                    getLastKnownLocation();
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
        openCreaLocationFragment();
    }

    @SuppressLint("MissingPermission")
    private void getLastKnownLocation() {
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(requireActivity(), new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            sharedViewModel.setnowLocation(location.getLatitude(), location.getLongitude());
                            // Logic to handle location object
                            Log.d("Location", "Latitude: " + location.getLatitude() + " Longitude: " + location.getLongitude());
                        } else {
                            Toast.makeText(requireContext(), "Unable to get location", Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void ShowPopupWindow() {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.popupwindow_reset_button, null, false);
        PopupWindow popupWindow = new PopupWindow(view, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        popupWindow.setWidth(700);
        popupWindow.setFocusable(false);
        popupWindow.setOutsideTouchable(false);

        // 讓PopupWindow顯示出來的關鍵句
        popupWindow.showAtLocation(rootView, Gravity.CENTER, 0, 0);
        popupWindow.setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        // 疊加View在底下，讓她不會按到底層就跳掉
        overlayView = new View(getContext());
        overlayView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        overlayView.setBackgroundColor(ContextCompat.getColor(getContext(), android.R.color.transparent));
        overlayView.setClickable(true);
        ((ViewGroup) rootView).addView(overlayView);

        // PopupWindow的文字顯示
        TextView warning = view.findViewById(R.id.txtNote);
        warning.setText("資料即將刪除");

        // PopUpWindow的取消按鈕
        Button BTNPopup = (Button) view.findViewById(R.id.PopupCancel);
        BTNPopup.setOnClickListener(v -> {
            popupWindow.dismiss();
            // 移除疊加在底下防止點擊其他區域的View
            removeOverlayView();
        });

        // PopupWindow的確認按鈕
        Button btnsure = (Button) view.findViewById(R.id.Popupsure);
        btnsure.setOnClickListener(v -> {
            deleteFromDB();
            popupWindow.dismiss();
            removeOverlayView();
            updateButtonState();

            // 檢查是否已經全部刪除，如果是則設置按鈕狀態為 "套用" 和 "編輯"
            if (arrayList.isEmpty()) {
                isEdit = false;
                isDelete = false;
            }

            // 再次更新按鈕狀態以反映更改
            updateButtonState();
        });
    }

    private void deleteFromDB() {
        ArrayList<HashMap<String, String>> selectedItems = new ArrayList<>();
        for (HashMap<String, String> item : arrayList) {
            if ("true".equals(item.get("isSelected"))) {
                selectedItems.add(item);
            }
        }

        // 從數據庫中刪除選中的項目
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
                //
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
        // 從 arrayList 中刪除選中的項目
        arrayList.removeAll(selectedItems);
        listAdapterHistory.notifyDataSetChanged();
    }

    //打開導航頁面
    private void openCreaLocationFragment() {
        HomeFragment createLocationFragment = new HomeFragment();
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fl_container, createLocationFragment); // 確保R.id.fl_container是你的Fragment容器ID
        transaction.addToBackStack(null);
        transaction.commit();

        NavigationView navigationView = getActivity().findViewById(R.id.navigation_view);
        navigationView.setCheckedItem(R.id.action_home);
    }

    // 把疊加在底層的View刪掉
    private void removeOverlayView() {
        if (overlayView != null && overlayView.getParent() != null) {
            ((ViewGroup) overlayView.getParent()).removeView(overlayView);
            overlayView = null;
        }
    }
}
