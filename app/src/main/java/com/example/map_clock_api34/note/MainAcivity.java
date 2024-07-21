package com.example.map_clock_api34.note;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.map_clock_api34.R;
//初始化 Fragment 容器
public class MainAcivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.recycleViewnote, new NoteEnterContent())
                    .commit();
        }
    }
}
