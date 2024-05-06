package com.example.map_clock_api34;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class SettingRemind extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setting_remind); // 加载 XML 布局文件

        // 查找 XML 文件中的按钮
        Button ringButton = findViewById(R.id.ring_setting);
        Button chooseButton = findViewById(R.id.choose_setting);
        Button vibrate = findViewById(R.id.vibrate_setting);
        Button backButton = findViewById(R.id.back);


        // 设置按钮点击事件监听器
        ringButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 在按钮点击时执行的操作
                ringDevice(v); // 调用自定义的方法处理点击事件
            }
        });

        chooseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 在按钮点击时执行的操作
                ringDevice(v); // 调用自定义的方法处理点击事件
            }
        });
        vibrate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 在按钮点击时执行的操作
                ringDevice(v); // 调用自定义的方法处理点击事件
            }
        });
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 返回到上一个活动
                finish();
            }
        });

        // 在此处添加其他操作...
    }

    // 在此处添加其他操作...


    // 定义与 app:onClick 属性匹配的方法
    public void ringDevice(View view) {
        // 在按钮点击时执行的操作
    }
}