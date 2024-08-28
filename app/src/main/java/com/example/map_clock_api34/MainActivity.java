package com.example.map_clock_api34;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.cardview.widget.CardView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.map_clock_api34.book.BookFragment;
import com.example.map_clock_api34.history.HistoryFragment;
import com.example.map_clock_api34.home.EndMapping;
import com.example.map_clock_api34.home.HomeFragment;
import com.example.map_clock_api34.home.StartMapping;
import com.example.map_clock_api34.setting.SettingRemind;
import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity {
    private HomeFragment homeFragment;
    private BookFragment bookFragment;
    private HistoryFragment historyFragment;
    private SettingRemind settingRemind;
    private DrawerLayout drawerLayout;
    private NavigationView navigation_view;
    private Toolbar toolbar;
    private CardView cardView;
    private StartMapping startMapping;
    private boolean wasStartMapping = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);//漢堡選單icon部會因深色模式改色


        drawerLayout = findViewById(R.id.drawerLayout);
        navigation_view = findViewById(R.id.navigation_view);
        toolbar = findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);

        // 觸發器召喚漢堡選單
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        toggle.getDrawerArrowDrawable().setColor(getResources().getColor(R.color.green));

        updateMenuIcons();

        // 檢查 Intent 是否包含 "show_start_mapping"或 "show_end_map" 的額外信息
        Intent intent = getIntent();
        if (intent != null) {
            if (intent.hasExtra("show_start_mapping")) {
                boolean showStartMapping = intent.getBooleanExtra("show_start_mapping", false);
                if (showStartMapping) {
                    Log.d("MainActivity", "onCreate: Intent contains show_start_mapping, calling showStartMappingFragment.");
                    showStartMappingFragment();
                }
            }

            
        }
        // 初始化地圖(第一個頁面是MAP)
        if (homeFragment == null) {
            homeFragment = new HomeFragment();
        }
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fl_container, homeFragment, "map").commit();

        if (savedInstanceState == null) {
            homeFragment = new HomeFragment();
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fl_container, homeFragment, "map")
                    .commit();
        }

        // 選單點擊
        navigation_view.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                // 點選時收起選單
                drawerLayout.closeDrawer(GravityCompat.START);

                // 取得選項id
                int id = item.getItemId();

                // 依照id判斷點了哪個項目並做相應事件
                if (id == R.id.action_home) {
                    homeFragment = new HomeFragment();
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fl_container, homeFragment, "map").commit();
                    Toast.makeText(MainActivity.this, "Dora map", Toast.LENGTH_SHORT).show();
                    return true;

                } else if (id == R.id.action_book) {
                    bookFragment = new BookFragment();
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fl_container, bookFragment, "com/example/map_clock_api34/book").commit();
                    Toast.makeText(MainActivity.this, "收藏路線", Toast.LENGTH_SHORT).show();
                    return true;

                } else if (id == R.id.action_history) {
                    historyFragment = new HistoryFragment();
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fl_container, historyFragment, "com/example/map_clock_api34/history").commit();
                    Toast.makeText(MainActivity.this, "歷史紀錄", Toast.LENGTH_SHORT).show();
                    return true;

                } else if (id == R.id.action_setting) {
                    settingRemind = new SettingRemind();
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fl_container, settingRemind, "com/example/map_clock_api34/setting").commit();
                    Toast.makeText(MainActivity.this, "設定", Toast.LENGTH_SHORT).show();
                    return true;
                }

                return false;
            }
        });
    }

    @Override
    public void onBackPressed() {
        // 鎖定返回鍵，現在所有頁面按返回都不會有反應
    }

    @Override
    public void onResume() {
        super.onResume();
        invalidateOptionsMenu();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);  // 替換成你的菜單資源 ID

        // 在這裡設置圖標顏色
        MenuItem menuItem1 = menu.findItem(R.id.action_home);
        menuItem1.getIcon().setColorFilter(getResources().getColor(R.color.green), PorterDuff.Mode.SRC_IN);
        MenuItem menuItem2 = menu.findItem(R.id.action_book);
        menuItem2.getIcon().setColorFilter(getResources().getColor(R.color.green), PorterDuff.Mode.SRC_IN);
        MenuItem menuItem3 = menu.findItem(R.id.action_history);
        menuItem3.getIcon().setColorFilter(getResources().getColor(R.color.green), PorterDuff.Mode.SRC_IN);
        MenuItem menuItem4 = menu.findItem(R.id.action_setting);
        menuItem4.getIcon().setColorFilter(getResources().getColor(R.color.green), PorterDuff.Mode.SRC_IN);

        menuItem1.setVisible(false);
        menuItem2.setVisible(false);
        menuItem3.setVisible(false);
        menuItem4.setVisible(false);

        return true;
    }
    private void updateMenuIcons() {
        // 确保菜单已经创建后再更新图标颜色
        if (toolbar != null) {
            toolbar.post(new Runnable() {
                @Override
                public void run() {
                    invalidateOptionsMenu(); // 触发菜单更新
                }
            });
        }
    }

    @Override
    public void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        updateMenuIcons(); // 确保在创建后设置图标颜色
    }
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent); // 更新 Activity 的 Intent

        // 检查新的 Intent 中是否包含打开 StartMapping 的标识符
        if (intent.hasExtra("show_start_mapping")) {
            boolean showStartMapping = intent.getBooleanExtra("show_start_mapping", false);
            if (showStartMapping) {
                Log.d("MainActivity", "onNewIntent: New Intent contains show_start_mapping, calling showStartMappingFragment.");
                showStartMappingFragment();
            }
        } else if (intent.hasExtra("show_end_map")) {
            boolean showEndMap = intent.getBooleanExtra("show_end_map", false);

        }

}

    private void showStartMappingFragment() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();

        // 查找是否存在 StartMapping 片段
        StartMapping startMappingFragment = (StartMapping) fragmentManager.findFragmentByTag("StartMapping");
        if (startMappingFragment == null) {
            // 如果 StartMapping 片段不存在，创建一个新的
            startMappingFragment = new StartMapping();
            transaction.add(R.id.fl_container, startMappingFragment, "StartMapping");
            Log.d("MainActivity", "Adding new StartMapping fragment");
        } else {
            // 如果 StartMapping 片段已经存在，显示它
            transaction.show(startMappingFragment);
            Log.d("MainActivity", "Showing existing StartMapping fragment");
        }


        transaction.addToBackStack(null);
        transaction.commit();
    }
}