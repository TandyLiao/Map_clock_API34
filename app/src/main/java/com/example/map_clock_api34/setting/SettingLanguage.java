package com.example.map_clock_api34.setting;

import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.map_clock_api34.R;

import java.util.Locale;

public class SettingLanguage extends AppCompatActivity {

    private Button settingButton;
    private Button backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setting_language);

        settingButton = findViewById(R.id.translate_setting);
        backButton = findViewById(R.id.back);

        settingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 切换语言
                switchLanguage();
                // 更新按钮标签
                updateButtonLabels();
                // 提示用户语言已更改
                Toast.makeText(SettingLanguage.this, "Language changed", Toast.LENGTH_SHORT).show();
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        // 初始化时更新按钮标签
        updateButtonLabels();
    }

    private void switchLanguage() {
        // 获取当前语言设置
        SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);
        String currentLang = prefs.getString("app_lang", "zh");

        // 切换语言
        String newLang = currentLang.equals("zh") ? "en" : "zh";

        // 保存语言设置到共享首选项
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("app_lang", newLang);
        editor.apply();

        // 更改应用语言
        setLocale(newLang);
    }

    private void setLocale(String lang) {
        // 更改应用语言
        Resources resources = getResources();
        DisplayMetrics dm = resources.getDisplayMetrics();
        Configuration config = resources.getConfiguration();
        Locale locale = new Locale(lang);
        Locale.setDefault(locale);
        config.setLocale(locale);
        resources.updateConfiguration(config, dm);
    }

    private void updateButtonLabels() {
        settingButton.setText(getButtonText("Switch Language"));
        backButton.setText(getButtonText("Back"));
    }

    private String getButtonText(String text) {
        // 检查当前语言并返回相应的翻译
        SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);
        String language = prefs.getString("app_lang", "zh");

        if ("zh".equals(language)) {
            return translateToChinese(text);
        } else {
            return text; // 默认返回英文
        }
    }

    private String translateToChinese(String text) {
        switch (text) {
            case "Switch Language":
                return "中英切換";
            case "Back":
                return "回上一頁";
            default:
                return text;
        }
    }
}

