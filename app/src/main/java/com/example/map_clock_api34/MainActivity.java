package com.example.map_clock_api34;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;


import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity {
    private HomeFragment homeFragment;
    private BookFragment bookFragment;

    private HistoryFragment historyFragment;

    private SettingFragment settingFragment;



    private DrawerLayout drawerLayout;
    private NavigationView navigation_view;
    private Toolbar toolbar;

    private CardView cardView;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        drawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        navigation_view = (NavigationView) findViewById(R.id.navigation_view);
        toolbar = (Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);

        //觸發器召喚漢堡選單
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        toggle.getDrawerArrowDrawable().setColor(getResources().getColor(R.color.green));


        //初始化地圖(第一個頁面是MAP)
        homeFragment = new HomeFragment();
        getSupportFragmentManager().beginTransaction().add(R.id.fl_container, homeFragment,"map").commit();
        //選單點擊
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
                            .replace(R.id.fl_container, homeFragment,"map").commit();
                    Toast.makeText(MainActivity.this, "Dora map", Toast.LENGTH_SHORT).show();
                    return true;


                }
                else if (id == R.id.action_book) {

                    bookFragment = new BookFragment();
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fl_container,bookFragment,"book").commit();
                    Toast.makeText(MainActivity.this, "book", Toast.LENGTH_SHORT).show();
                    return true;



                }
                else if (id == R.id.action_history) {

                    historyFragment=new HistoryFragment();
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fl_container,historyFragment,"history").commit();
                    Toast.makeText(MainActivity.this, "歷史紀錄", Toast.LENGTH_SHORT).show();

                    return true;
                }
                else if (id == R.id.action_setting) {
                    settingFragment=new SettingFragment();
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fl_container,settingFragment,"setting").commit();
                    Toast.makeText(MainActivity.this, "設定", Toast.LENGTH_SHORT).show();

                    return true;
                }

                return false;
            }
        });

    }

}