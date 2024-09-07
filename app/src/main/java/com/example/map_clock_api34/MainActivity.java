package com.example.map_clock_api34;

import static com.google.android.material.internal.ViewUtils.hideKeyboard;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.map_clock_api34.book.BookFragment;
import com.example.map_clock_api34.history.HistoryFragment;
import com.example.map_clock_api34.CreateLocation.CreateLocation;
import com.example.map_clock_api34.CreateLocation.StartMapping;
import com.example.map_clock_api34.setting.SettingRemind;
import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity {

    private Toolbar toolbar;

    // 宣告 Fragment 變數，存儲各個頁面的實例
    private CreateLocation createLocationFragment;
    private BookFragment bookFragment;
    private HistoryFragment historyFragment;

    private SettingRemind settingRemind;

    private DrawerLayout drawerLayout; // 用於控制側邊菜單的開關
    private NavigationView navigation_view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 禁用夜間模式，強制使用亮色模式
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        // 初始化 DrawerLayout 和 NavigationView，並設置 Toolbar
        drawerLayout = findViewById(R.id.drawerLayout);
        navigation_view = findViewById(R.id.navigation_view);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar); // 設置 Toolbar 作為 ActionBar

        // 設置漢堡選單（側邊欄開關）的觸發器
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        toggle.getDrawerArrowDrawable().setColor(getResources().getColor(R.color.green)); // 設置箭頭顏色

        updateMenuIcons(); // 更新菜單圖標顏色

        // 修改側邊選單的圖標顏色
        Menu menu = navigation_view.getMenu();
        for (int i = 0; i < menu.size(); i++) {
            MenuItem menuItem = menu.getItem(i);
            if (menuItem.getIcon() != null) {
                menuItem.getIcon().setColorFilter(getResources().getColor(R.color.green), PorterDuff.Mode.SRC_IN);
            }
        }

        // 處理從 Intent 傳來的數據
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("show_start_mapping")) {
            boolean showStartMapping = intent.getBooleanExtra("show_start_mapping", false);
            if (showStartMapping) {
                Log.d("MainActivity", "onCreate: 顯示 StartMapping Fragment");
                showStartMappingFragment(); // 顯示 StartMapping Fragment
            }
        }

        // 頁面首次加載時初始化並顯示 HomeFragment（地圖頁面）
        if (createLocationFragment == null) {
            createLocationFragment = new CreateLocation();
        }
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fl_container, createLocationFragment, "map").commit();

        // 檢查是否有保存的狀態，若無則加載 HomeFragment
        if (savedInstanceState == null) {
            createLocationFragment = new CreateLocation();
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fl_container, createLocationFragment, "map").commit();
        }

        // 設置導航菜單的點擊事件
        navigation_view.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                // 點擊菜單項目時關閉側邊菜單
                drawerLayout.closeDrawer(GravityCompat.START);

                // 根據選單項目的 ID 來決定切換到哪個 Fragment
                int id = item.getItemId();

                if (id == R.id.action_home) {
                    // 切換到 HomeFragment
                    createLocationFragment = new CreateLocation();
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fl_container, createLocationFragment, "map").commit();
                    return true;

                } else if (id == R.id.action_book) {
                    // 切換到 BookFragment
                    bookFragment = new BookFragment();
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fl_container, bookFragment, "com/example/map_clock_api34/book").commit();
                    return true;

                } else if (id == R.id.action_history) {
                    // 切換到 HistoryFragment
                    historyFragment = new HistoryFragment();
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fl_container, historyFragment, "com/example/map_clock_api34/history").commit();
                    return true;

                } else if (id == R.id.action_setting) {
                    // 切換到 SettingRemind
                    settingRemind = new SettingRemind();
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fl_container, settingRemind, "com/example/map_clock_api34/setting").commit();
                    return true;

                }
                return false;
            }
        });
    }

    // 處理返回按鍵事件
    @Override
    public void onBackPressed() {
        super.onBackPressed(); // 默認的返回行為
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // 加載主選單
        getMenuInflater().inflate(R.menu.menu, menu);

        // 設置選單圖標顏色
        MenuItem menuItem1 = menu.findItem(R.id.action_home);
        menuItem1.getIcon().setColorFilter(getResources().getColor(R.color.green), PorterDuff.Mode.SRC_IN);
        MenuItem menuItem2 = menu.findItem(R.id.action_book);
        menuItem2.getIcon().setColorFilter(getResources().getColor(R.color.green), PorterDuff.Mode.SRC_IN);
        MenuItem menuItem3 = menu.findItem(R.id.action_history);
        menuItem3.getIcon().setColorFilter(getResources().getColor(R.color.green), PorterDuff.Mode.SRC_IN);
        MenuItem menuItem4 = menu.findItem(R.id.action_setting);
        menuItem4.getIcon().setColorFilter(getResources().getColor(R.color.green), PorterDuff.Mode.SRC_IN);

        // 隱藏菜單項目
        menuItem1.setVisible(false);
        menuItem2.setVisible(false);
        menuItem3.setVisible(false);
        menuItem4.setVisible(false);

        return true;
    }

    // 更新選單圖標顏色
    private void updateMenuIcons() {
        if (toolbar != null) {
            toolbar.post(new Runnable() {
                @Override
                public void run() {
                    invalidateOptionsMenu(); // 強制更新菜單圖標
                }
            });
        }
    }

    @Override
    public void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        updateMenuIcons(); // 在創建後更新圖標
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent); // 設置新的 Intent

        // 檢查新的 Intent 是否包含 show_start_mapping 標記
        if (intent.hasExtra("show_start_mapping")) {
            boolean showStartMapping = intent.getBooleanExtra("show_start_mapping", false);
            if (showStartMapping) {
                Log.d("MainActivity", "onNewIntent: 顯示 StartMapping Fragment");
                if (intent.hasExtra("triggerSendBroadcast")) {
                    sendBroadcastToLocationService(); // 發送廣播
                }
                showStartMappingFragment(); // 顯示 StartMapping Fragment
            }
        }
    }

    // 顯示 StartMapping Fragment
    private void showStartMappingFragment() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();

        // 檢查是否已存在 StartMapping Fragment
        StartMapping startMappingFragment = (StartMapping) fragmentManager.findFragmentByTag("StartMapping");
        if (startMappingFragment == null) {
            // 若無，則新增 StartMapping Fragment
            startMappingFragment = new StartMapping();
            transaction.add(R.id.fl_container, startMappingFragment, "StartMapping");
            Log.d("MainActivity", "新增 StartMapping Fragment");
            transaction.addToBackStack(null);
        } else {
            // 若已存在，則顯示它
            transaction.show(startMappingFragment);
            Log.d("MainActivity", "顯示已存在的 StartMapping Fragment");
        }
        transaction.commit();
    }

    // 發送廣播通知位置服務
    private void sendBroadcastToLocationService() {
        Intent broadcastIntent = new Intent("DESTINATIONINDEX_UPDATE");
        broadcastIntent.putExtra("triggerSendBroadcast", true);
        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent); // 發送廣播
    }

    // 可以按小鍵盤以外的地方取消鍵盤
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        View view = getCurrentFocus(); // 取得當前焦點

        if (view != null && ev.getAction() == MotionEvent.ACTION_DOWN) {
            if (view instanceof EditText) {
                // 當點擊到 EditText 以外的區域時隱藏鍵盤
                Rect outRect = new Rect();
                view.getGlobalVisibleRect(outRect);
                if (!outRect.contains((int) ev.getRawX(), (int) ev.getRawY())) {
                    view.clearFocus(); // 取消 EditText 焦點
                    hideKeyboard(view); // 隱藏鍵盤
                }
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    // 隱藏鍵盤
    private void hideKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null && view != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        invalidateOptionsMenu(); // 更新選單狀態
    }
}
