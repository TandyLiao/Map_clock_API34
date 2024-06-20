package com.example.map_clock_api34.setting;

import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.map_clock_api34.R;
import com.example.map_clock_api34.home.CreateLocation;

import java.util.Locale;

public class SettingLanguage extends Fragment {

    private Button settingButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.setting_language, container, false);

        settingButton = rootView.findViewById(R.id.translate_setting);

        settingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 切换语言
                switchLanguage();
                // 更新按钮标签
                updateButtonLabels();
                // 提示用户语言已更改
                Toast.makeText(getContext(), "Language changed", Toast.LENGTH_SHORT).show();

                CreateLocation createFragment = new CreateLocation();
                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.fl_container, createFragment);
                transaction.addToBackStack(null);
                transaction.commit();

            }
        });

        // 初始化时更新按钮标签
        updateButtonLabels();

        return rootView;
    }

    private void switchLanguage() {
        // 获取当前语言设置
        SharedPreferences prefs = requireActivity().getSharedPreferences("settings", requireContext().MODE_PRIVATE);
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

        // 重新加载 Fragment 或者 Activity 以应用语言更改
        requireActivity().recreate();
    }

    private void updateButtonLabels() {
        settingButton.setText(getButtonText("Switch Language"));
    }

    private String getButtonText(String text) {
        // 检查当前语言并返回相应的翻译
        SharedPreferences prefs = requireActivity().getSharedPreferences("settings", requireContext().MODE_PRIVATE);
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
            default:
                return text;
        }
    }


    @Override
    public void onResume() {
        super.onResume();

        // 建立CardView在toolbar
        CardView cardViewtitle = new CardView(requireContext());
        cardViewtitle.setLayoutParams(new CardView.LayoutParams(
                ActionBar.LayoutParams.WRAP_CONTENT,
                ActionBar.LayoutParams.WRAP_CONTENT));
        Drawable drawable = ContextCompat.getDrawable(requireContext(), R.drawable.cardviewtitle_shape);
        cardViewtitle.setBackground(drawable);

        // 建立LinearLayout在CardView等等放圖案和文字
        LinearLayout linearLayout = new LinearLayout(requireContext());
        linearLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        ));
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);

        // ImageView放置圖案
        ImageView mark = new ImageView(requireContext());
        mark.setImageResource(R.drawable.setting);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                100, // 设置宽度为 100 像素
                100 // 设置高度为 100 像素
        );
        params.setMarginStart(10); // 设置左边距
        mark.setLayoutParams(params);

        // 創建TextView
        TextView bookTitle = new TextView(requireContext());
        bookTitle.setText("設定");
        bookTitle.setTextSize(15);
        bookTitle.setTextColor(getResources().getColor(R.color.green)); // 更改文字颜色
        bookTitle.setPadding(10, 10, 10, 10); // 设置内边距

        // 將ImageView和TextView添加到LinearLayout
        linearLayout.addView(mark);
        linearLayout.addView(bookTitle);
        cardViewtitle.addView(linearLayout);

        // 创建自定义返回按钮
        ImageView returnButton = new ImageView(requireContext());
        returnButton.setImageResource(R.drawable.returnpage);
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

        androidx.appcompat.widget.Toolbar toolbar = (androidx.appcompat.widget.Toolbar) getActivity().findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(null); // 隐藏漢汉堡菜单

        // 获取ActionBar
        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false); // 隐藏原有的标题
            actionBar.setDisplayShowCustomEnabled(true);
            actionBar.setCustomView(actionBarLayout, new ActionBar.LayoutParams(
                    ActionBar.LayoutParams.MATCH_PARENT, // 宽度设置为 MATCH_PARENT
                    ActionBar.LayoutParams.MATCH_PARENT // 高度设置为 MATCH_PARENT
            ));
            actionBar.show();
        }

        // 设置返回按钮点击事件
        returnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SettingFragment settingFragment = new SettingFragment();
                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.fl_container, settingFragment);
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });

        // 在恢复时更新按钮标签
        updateButtonLabels();
    }
    public void onPause() {
        super.onPause();

        // 获取ActionBar
        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowCustomEnabled(false);
            actionBar.setCustomView(null);
            actionBar.setDisplayShowTitleEnabled(true); // 恢复显示标题
            actionBar.show();
        }

    }





}
