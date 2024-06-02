package com.example.map_clock_api34.book;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.map_clock_api34.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.annotation.NonNull;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;

public class Bookmark extends AppCompatActivity {
    private BottomNavigationView navigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bookmark);

        ListView listView1 = findViewById(R.id.list_view1);
        ListView listView2 = findViewById(R.id.list_view2);

        // 獲取 confirm_button 的引用並為它設置 OnClickListener
        Button confirmButton = findViewById(R.id.confirm_button);
        confirmButton.setOnClickListener(v -> {
            // 在這裡放置你的按鈕點擊事件的具體代碼
            Intent intentSomeActivity = new Intent(Bookmark.this, CreateBookmark.class); // 假設 SomeActivity 是你要打開的 Activity
            startActivity(intentSomeActivity);
        });

        navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
    }

    private final BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = menuItem -> {
        if (menuItem.getItemId() == R.id.action_create_bookmark) {
            Intent intent = new Intent(Bookmark.this, CreateBookmark.class);
            startActivityForResult(intent, 1);
            return true;
        }
        return false;
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        ListView listView1 = findViewById(R.id.list_view1);
        ListView listView2 = findViewById(R.id.list_view2);

        ArrayList<String> list1 = data.getStringArrayListExtra("list1");
        ArrayList<String> list2 = data.getStringArrayListExtra("list2");

        ArrayAdapter<String> adapter1 = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, list1);
        listView1.setAdapter(adapter1);

        ArrayAdapter<String> adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, list2);
        listView2.setAdapter(adapter2);
    }
}