package com.example.map_clock_api34.book;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.EditText;

import androidx.annotation.NonNull;
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


import com.example.map_clock_api34.book.BookDatabaseHelper.BookTable;
import com.example.map_clock_api34.book.BookDatabaseHelper.LocationTable2;

import com.example.map_clock_api34.R;
import com.example.map_clock_api34.SharedViewModel;
import com.example.map_clock_api34.home.ListAdapter.ListAdapterRoute;
import com.example.map_clock_api34.home.ListAdapter.RecyclerViewActionHome;
import com.example.map_clock_api34.home.SelectPlace;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

public class FakeCreateLocation extends Fragment {

    private BookDatabaseHelper dbBookHelper;
    private EditText bookNameEditText;
    String names;
    String Booknames;
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
    EditText input;

    ArrayList<HashMap<String, String>> arrayList = new ArrayList<>();

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fake_fragment_creatlocation, container, false);

        dbBookHelper = new BookDatabaseHelper(requireContext());

        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);

        input = rootView.findViewById(R.id.BookName);

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

                if(input.getText().toString().equals("")){
                    Toast.makeText(getActivity(), "你沒有輸入書籤名稱!", Toast.LENGTH_SHORT).show();
                    return;
                }
                //Booknames = sharedViewModel.getDestinationName(0) + "->" + sharedViewModel.getDestinationName(sharedViewModel.getLocationCount());
                saveInLocationDB();
                saveInBookDB();
                sharedViewModel.clearAll();
                hideKeyboard();
                //回上頁
                getActivity().getSupportFragmentManager().popBackStack();
            } else {
                Toast.makeText(getActivity(), "你還沒有選擇地點", Toast.LENGTH_SHORT).show();
            }
        });

        // 導航到 book_fragment_book.xml
                /*FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
                Fragment bookFragment = new BookFragment(); // 需要你自己創建這個 Fragment
                transaction.replace(R.id.fragment_container, bookFragment);
                transaction.addToBackStack(null);
                transaction.commit();*/
        //換頁功能book_create_route
            /*Button editButton = view.findViewById(R.id.book_create_route);
            editButton.setOnClickListener(v -> {
                FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.fragment_container, new HistoryEditFragment());
                transaction.addToBackStack(null); // 將這個交易添加到後退堆棧中，以便用戶可以按返回按鈕返回
                transaction.commit();
            });*/
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

    private void saveInBookDB() {

        try {
            SQLiteDatabase writeDB = dbBookHelper.getWritableDatabase();
            SQLiteDatabase readDB = dbBookHelper.getReadableDatabase();

            Cursor cursor = readDB.rawQuery("SELECT location_id FROM location WHERE alarm_name=?", new String[]{uniqueID});

            long currentTimeMillis = System.currentTimeMillis();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String formattedDate = sdf.format(new Date(currentTimeMillis));

            //此變數要讓History的每筆路線都從0開始存入
            int arranged_id_local=0;

            while (cursor.moveToNext()) {
                if (input.getText().toString() != null) {
                    ContentValues values = new ContentValues();
                    values.put(BookTable.COLUMN_START_TIME, formattedDate);
                    values.put(BookTable.COLUMN_ALARM_NAME, input.getText().toString());

                    values.put(BookTable.COLUMN_LOCATION_ID, cursor.getString(0));

                    //arranged_id_local存入History表後再+1
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

    private void saveInLocationDB() {

        //產生一組獨一無二的ID存入區域變數內(重複機率近乎為0)
        //用這ID去做Location_id和History做配對
        uniqueID = UUID.randomUUID().toString();

        SQLiteDatabase db = dbBookHelper.getWritableDatabase();

        for (int i = 0; i <= sharedViewModel.getLocationCount(); i++) {

            String name = sharedViewModel.getDestinationName(i);
            double latitude = sharedViewModel.getLatitude(i);
            double longitude = sharedViewModel.getLongitude(i);
            String CityName = sharedViewModel.getCapital(i);
            String AreaName = sharedViewModel.getArea(i);

            if (name != null) {
                ContentValues values = new ContentValues();
                values.put(LocationTable2.COLUMN_PLACE_NAME, name);
                values.put(LocationTable2.COLUMN_LATITUDE, latitude);
                values.put(LocationTable2.COLUMN_LONGITUDE, longitude);
                values.put(LocationTable2.COLUMN_ALARM_NAME, uniqueID);
                values.put(LocationTable2.COLUMN_CITY_NAME, CityName);
                values.put(LocationTable2.COLUMN_AREA_NAME, AreaName);

                db.insert(LocationTable2.TABLE_NAME, null, values);
            }
        }
        db.close();
    }


    //打開選地點頁面
    private void openSelectPlaceFragment() {
        SelectPlace mapFragment = new SelectPlace();
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fl_container, mapFragment);
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
        cardViewtitle.setLayoutParams(new CardView.LayoutParams(ActionBar.LayoutParams.WRAP_CONTENT,
                ActionBar.LayoutParams.WRAP_CONTENT));
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

        // 创建自定义返回按钮
        ImageView returnButton = new ImageView(requireContext());
        returnButton.setImageResource(R.drawable.back);
        LinearLayout.LayoutParams returnButtonParams = new LinearLayout.LayoutParams(
                100, // 设置宽度为 100 像素
                100 // 设置高度为 100 像素
        );
        returnButton.setLayoutParams(returnButtonParams);

        // 建立ActionBar的父LinearLayout
        LinearLayout actionBarLayout = new LinearLayout(requireContext());
        actionBarLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        ));
        actionBarLayout.setOrientation(LinearLayout.HORIZONTAL);
        actionBarLayout.setWeightSum(1.0f);

        // 子LinearLayout用于返回按钮
        LinearLayout leftLayout = new LinearLayout(requireContext());
        leftLayout.setLayoutParams(new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.MATCH_PARENT,
                0.1f
        ));
        leftLayout.setOrientation(LinearLayout.HORIZONTAL);
        leftLayout.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
        leftLayout.addView(returnButton);

        // 子LinearLayout用于cardViewtitle
        LinearLayout rightLayout = new LinearLayout(requireContext());
        rightLayout.setLayoutParams(new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.MATCH_PARENT,
                0.9f
        ));
        rightLayout.setOrientation(LinearLayout.HORIZONTAL);
        rightLayout.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
        rightLayout.addView(cardViewtitle);

        // 将子LinearLayout添加到父LinearLayout
        actionBarLayout.addView(leftLayout);
        actionBarLayout.addView(rightLayout);

        androidx.appcompat.widget.Toolbar toolbar = getActivity().findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(null); // 隐藏漢汉堡菜单

        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false); // 隐藏原有的標題
            actionBar.setDisplayShowCustomEnabled(true);
            actionBar.setCustomView(actionBarLayout, new ActionBar.LayoutParams(
                    ActionBar.LayoutParams.MATCH_PARENT,
                    ActionBar.LayoutParams.MATCH_PARENT
            ));
            actionBar.show();
        }

        returnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sharedViewModel.clearAll();
                arrayList.clear();
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });
    }

    //初始化設定表和功能表
    private void setupRecyclerViews() {
        // 初始化路線的表
        recyclerViewRoute = rootView.findViewById(R.id.recycleViewRouteBook);
        recyclerViewRoute.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerViewRoute.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));
        listAdapterRoute = new ListAdapterRoute(arrayList, sharedViewModel, true); // 啟用拖動功能
        recyclerViewRoute.setAdapter(listAdapterRoute);

        // 讓路線表可以交換、刪除...等動作
        recyclerViewActionHome = new RecyclerViewActionHome();
        recyclerViewActionHome.attachToRecyclerView(recyclerViewRoute, arrayList, listAdapterRoute, sharedViewModel, getActivity(), btnReset);
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
            setupActionBar();
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
    private void hideKeyboard() {
        View view = getActivity().getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }
    }
}