package com.example.map_clock_api34.setting;

import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.map_clock_api34.R;
import android.content.Context;

import java.util.Locale;

public class SettingInterfaceActivity extends AppCompatActivity {

    private Button settingButton;
    private Button backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadLocale(); // 加载语言设置
        setContentView(R.layout.setting_interface);

        settingButton = findViewById(R.id.big_setting);
        backButton = findViewById(R.id.back);

        settingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adjustFontSize(v);
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        updateButtonLabels(); // 初始化时更新按钮文本
    }

    private void updateButtonLabels() {
        settingButton.setText(getButtonText("Adjust Font Size"));
        backButton.setText(getButtonText("Back"));
    }

    private String getButtonText(String text) {
        // 检查当前语言并返回相应的翻译
        SharedPreferences prefs = getSharedPreferences("settings", Context.MODE_PRIVATE);
        String language = prefs.getString("app_lang", "zh");

        if ("zh".equals(language)) {
            return translateToChinese(text);
        } else {
            return text; // 默认返回英文
        }
    }

    private String translateToChinese(String text) {
        switch (text) {
            case "Adjust Font Size":
                return "調整字體大小";
            case "Back":
                return "回上一頁";
            default:
                return text;
        }
    }

    private void loadLocale() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String language = prefs.getString("app_lang", "zh");
        setLocale(language);
    }

    private void setLocale(String lang) {
        Locale locale = new Locale(lang);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        getResources().updateConfiguration(config, getResources().getDisplayMetrics());
    }

    public void adjustFontSize(View view) {
        // 在按钮点击时执行的操作
        // TODO: Implement your logic to adjust the font size
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateButtonLabels(); // 确保返回活动时更新文本
    }
}
