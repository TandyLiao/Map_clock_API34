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
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.appcompat.widget.Toolbar;

import com.example.map_clock_api34.R;

import java.util.Locale;

public class SettingFragment extends Fragment {
    private TextView tx1;
    private Toolbar toolbar;
    private Button interface_Button, remind_Button, language_Button;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_setting, container, false);

        // 初始化按钮
        interface_Button = view.findViewById(R.id.interface_Button);
        remind_Button = view.findViewById(R.id.remind_Button);
        language_Button = view.findViewById(R.id.language_Button);

        // 为每个按钮设置点击事件监听器
        interface_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 启动 SettingInterfaceActivity
                startActivity(new Intent(requireContext(), SettingInterfaceActivity.class));
            }
        });

        remind_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 启动其他 Activity 或执行其他操作
                startActivity(new Intent(requireContext(), SettingRemind.class));
            }
        });

        language_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 启动 SettingLanguageActivity
                startActivity(new Intent(requireContext(), SettingLanguage.class));
            }
        });

        // 在创建视图时更新按钮标签
        updateButtonLabels();

        return view;
    }

    private void updateButtonLabels() {
        interface_Button.setText(getButtonText("Adjust Font Size"));
        remind_Button.setText(getButtonText("Remind Setting"));
        language_Button.setText(getButtonText("Language Switch"));
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
            case "Adjust Font Size":
                return "調整字體大小";
            case "Remind Setting":
                return "提醒設置";
            case "Language Switch":
                return "中英切換";
            default:
                return text;
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tx1 = view.findViewById(R.id.textView);
        toolbar = getActivity().findViewById(R.id.toolbar);
    }

    @Override
    public void onResume() {
        super.onResume();

        tx1.setText("wakuwaku");

        //建立CardView在toolbar
        CardView cardViewtitle = new CardView(requireContext());
        cardViewtitle.setLayoutParams(new CardView.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT));
        Drawable drawable = ContextCompat.getDrawable(requireContext(), R.drawable.cardviewtitle_shape);
        cardViewtitle.setBackground(drawable);
        //建立LinearLayout在CardView等等放圖案和文字
        LinearLayout linearLayout = new LinearLayout(requireContext());
        linearLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        ));
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);

        //
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

        linearLayout.addView(mark);
        linearLayout.addView(bookTitle);
        cardViewtitle.addView(linearLayout);

        // 将cardview新增到actionBar
        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false); // 隐藏原有的标题
            actionBar.setDisplayShowCustomEnabled(true);
            actionBar.setCustomView(cardViewtitle, new ActionBar.LayoutParams(
                    ActionBar.LayoutParams.WRAP_CONTENT, // 宽度设置为 WRAP_CONTENT
                    ActionBar.LayoutParams.WRAP_CONTENT, // 高度设置为 WRAP_CONTENT
                    Gravity.END)); // 将包含 TextView 的 CardView 设置为自定义视图
            actionBar.show();
        }

        // 在恢复时更新按钮标签
        updateButtonLabels();
    }

    @Override
    public void onPause() {
        super.onPause();
        ActionBar actionBar = ((AppCompatActivity) requireActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowCustomEnabled(false);
            actionBar.setCustomView(null);
        }
    }
}
