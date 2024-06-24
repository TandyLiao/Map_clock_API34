package com.example.map_clock_api34.setting;

import android.content.Context;
import android.content.Intent;
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
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import com.example.map_clock_api34.R;
import java.util.Locale;

public class SettingFragment extends Fragment {

    private androidx.appcompat.widget.Toolbar toolbar;
    private Button interface_Button, remind_Button, language_Button;
    private ImageView setting_clock;
    private ActionBarDrawerToggle toggle;
    private DrawerLayout drawerLayout; // 声明 DrawerLayout 对象

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_setting, container, false);

        // 初始化按钮
        interface_Button = view.findViewById(R.id.interface_Button);
        remind_Button = view.findViewById(R.id.remind_Button);
        language_Button = view.findViewById(R.id.language_Button);
        setting_clock = view.findViewById(R.id.myImageView);

        setting_clock.setImageResource(R.drawable.setting_alarm_24);

        // 为每个按钮设置点击事件监听器
        interface_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 启动 SettingInterfaceActivity
                SettingInterfaceActivity settingInterfaceActivity = new SettingInterfaceActivity();
                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.fl_container, settingInterfaceActivity);
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });

        remind_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SettingRemind settingRemind = new SettingRemind();
                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.fl_container, settingRemind);
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });

        language_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 启动 SettingLanguage Fragment
                SettingLanguage settingLanguage = new SettingLanguage();
                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.fl_container, settingLanguage);
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });

        // 在创建视图时更新按钮标签
        updateButtonLabels();

        return view;
    }

    private void updateButtonLabels() {
        interface_Button.setText(getButtonText("Interface"));
        remind_Button.setText(getButtonText("Remind"));
        language_Button.setText(getButtonText("Language"));
    }

    private String getButtonText(String text) {
        // 检查当前语言并返回相应的翻译
        SharedPreferences prefs = requireContext().getSharedPreferences("settings", Context.MODE_PRIVATE);
        String language = prefs.getString("app_lang", "zh");

        if ("zh".equals(language)) {
            return translateToChinese(text);
        } else {
            return text; // 默认返回英文
        }
    }

    private String translateToChinese(String text) {
        switch (text) {
            case "Interface":
                return "介面";
            case "Remind":
                return "提醒";
            case "Language":
                return "語言";
            default:
                return text;
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 通过 getActivity() 获取 MainActivity 中的 DrawerLayout
        drawerLayout = getActivity().findViewById(R.id.drawerLayout);
        toolbar = getActivity().findViewById(R.id.toolbar);
    }

    @Override
    public void onResume() {
        super.onResume();

        // 获取 ActionBar
        ActionBar actionBar = ((AppCompatActivity) requireActivity()).getSupportActionBar();
        if (actionBar != null) {
            // 设置漢堡選單觸發鍵的顏色
            if (toggle == null) {
                toggle = new ActionBarDrawerToggle(
                        getActivity(), drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close);
                drawerLayout.addDrawerListener(toggle);
                toggle.syncState();
            }
            toggle.getDrawerArrowDrawable().setColor(getResources().getColor(R.color.green));

            // 建立 CardView 在 toolbar
            CardView cardViewtitle = new CardView(requireContext());
            cardViewtitle.setLayoutParams(new CardView.LayoutParams(
                    ActionBar.LayoutParams.MATCH_PARENT,
                    ActionBar.LayoutParams.MATCH_PARENT));
            Drawable drawable = ContextCompat.getDrawable(requireContext(), R.drawable.cardviewtitle_shape);
            cardViewtitle.setBackground(drawable);

            // 建立 LinearLayout 在 CardView 里放置图标和文字
            LinearLayout linearLayout = new LinearLayout(requireContext());
            linearLayout.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT));
            linearLayout.setOrientation(LinearLayout.HORIZONTAL);

            // ImageView 放置图标
            ImageView mark = new ImageView(requireContext());
            mark.setImageResource(R.drawable.setting);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    100, // 设置宽度为 100 像素
                    100 // 设置高度为 100 像素
            );
            params.setMarginStart(10); // 设置左边距
            mark.setLayoutParams(params);

            // 创建 TextView
            TextView bookTitle = new TextView(requireContext());
            bookTitle.setText("設定");
            bookTitle.setTextSize(15);
            bookTitle.setTextColor(getResources().getColor(R.color.green)); // 更改文字颜色
            bookTitle.setPadding(10, 10, 10, 10); // 设置内边距

            // 将 ImageView 和 TextView 添加到 LinearLayout
            linearLayout.addView(mark);
            linearLayout.addView(bookTitle);
            cardViewtitle.addView(linearLayout);

            // 设置自定义视图到 ActionBar
            actionBar.setDisplayShowTitleEnabled(false); // 隐藏原有的标题
            actionBar.setDisplayShowCustomEnabled(true);
            actionBar.setCustomView(cardViewtitle, new ActionBar.LayoutParams(
                    ActionBar.LayoutParams.WRAP_CONTENT, // 宽度设置为 WRAP_CONTENT
                    ActionBar.LayoutParams.WRAP_CONTENT, // 高度设置为 WRAP_CONTENT
                    Gravity.END)); // 设置位置为右侧

            actionBar.show();
        }

        // 在恢复时更新按钮标签
        updateButtonLabels();
    }

    @Override
    public void onPause() {
        super.onPause();

        // 清除 ActionBar 的自定义视图
        ActionBar actionBar = ((AppCompatActivity) requireActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowCustomEnabled(false);
            actionBar.setCustomView(null);
            actionBar.setDisplayShowTitleEnabled(true); // 恢复显示标题
        }
    }
}
