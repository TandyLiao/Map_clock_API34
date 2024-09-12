package com.example.map_clock_api34.setting;

import static android.content.Context.MODE_PRIVATE;

import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
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
import androidx.fragment.app.FragmentTransaction;

import com.example.map_clock_api34.R;
import com.example.map_clock_api34.TutorialFragment;

public class WhichTutorialFragment extends Fragment {

    View rootView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.setting_which_tutorial, container, false);


        setupButton();
        setupActionBar();

        return rootView;
    }

    private void setupButton(){
        Button btn_createLocation = rootView.findViewById(R.id.btn_createLocation);
        btn_createLocation.setOnClickListener( v -> {

            // 檢查是否需要顯示教學頁面
            SharedPreferences sharedPreferences = getActivity().getSharedPreferences("MyPrefs", MODE_PRIVATE);
            // 如果第一次進入，顯示教學頁面
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt("WhichPage", 0);
            editor.apply();
            TutorialFragment tutorialFragment = new TutorialFragment();
            FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fl_container, tutorialFragment);
            transaction.addToBackStack(null);
            transaction.commit();
        });

        Button btn_note = rootView.findViewById(R.id.btn_note);
        btn_note.setOnClickListener( v -> {
            // 檢查使用者是否已經造訪過，未造訪則跳轉到教學頁面
            SharedPreferences sharedPreferences = getActivity().getSharedPreferences("MyPrefs", MODE_PRIVATE);

            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt("WhichPage",4);
            editor.apply();

            TutorialFragment tutorialFragment = new TutorialFragment();
            FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fl_container, tutorialFragment);
            transaction.addToBackStack(null);
            transaction.commit();
        });

        Button btn_locationSetting = rootView.findViewById(R.id.btn_locationSetting);
        btn_locationSetting.setOnClickListener( v -> {
            // 檢查是否需要顯示教學頁面
            SharedPreferences sharedPreferences = getActivity().getSharedPreferences("MyPrefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt("WhichPage", 3);
            editor.putBoolean("settingLogin", true);
            editor.apply();

            TutorialFragment tutorialFragment = new TutorialFragment();
            FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fl_container, tutorialFragment);
            transaction.addToBackStack(null);
            transaction.commit();
        });

        Button btn_findBus = rootView.findViewById(R.id.btn_findBus);
        btn_findBus.setOnClickListener( v -> {
            // 檢查使用者是否已經造訪過，未造訪則跳轉到教學頁面
            SharedPreferences sharedPreferences = getActivity().getSharedPreferences("MyPrefs", MODE_PRIVATE);

            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt("WhichPage",5);
            editor.apply();

            TutorialFragment tutorialFragment = new TutorialFragment();
            FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fl_container, tutorialFragment);
            transaction.addToBackStack(null);
            transaction.commit();
        });

        Button btn_findWeather = rootView.findViewById(R.id.btn_findWeather);
        btn_findWeather.setOnClickListener( v -> {

            // 檢查是否需要顯示教學頁面
            SharedPreferences sharedPreferences = getActivity().getSharedPreferences("MyPrefs", MODE_PRIVATE);

            // 如果第一次進入，顯示教學頁面
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt("WhichPage", 6);
            editor.apply();

            TutorialFragment tutorialFragment = new TutorialFragment();
            FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fl_container, tutorialFragment);
            transaction.addToBackStack(null);
            transaction.commit();
        });

        Button btn_book = rootView.findViewById(R.id.btn_book);
        btn_book.setOnClickListener( v -> {
            // 檢查使用者是否已經造訪過，未造訪則跳轉到教學頁面
            SharedPreferences sharedPreferences = getActivity().getSharedPreferences("MyPrefs", MODE_PRIVATE);

            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt("WhichPage",2);
            editor.apply();

            TutorialFragment tutorialFragment = new TutorialFragment();
            FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fl_container, tutorialFragment);
            transaction.addToBackStack(null);
            transaction.commit();
        });

        Button btn_history = rootView.findViewById(R.id.btn_history);
        btn_history.setOnClickListener( v-> {
            // 檢查使用者是否已經造訪過，未造訪則跳轉到教學頁面
            SharedPreferences sharedPreferences = getActivity().getSharedPreferences("MyPrefs", MODE_PRIVATE);

            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt("WhichPage",1);
            editor.apply();

            TutorialFragment tutorialFragment = new TutorialFragment();
            FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fl_container, tutorialFragment);
            transaction.addToBackStack(null);
            transaction.commit();
        });
    }

    // 設置自定義 ActionBar
    private void setupActionBar() {
        ActionBar actionBar = ((AppCompatActivity) requireActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide(); // 隱藏原有的 ActionBar
            actionBar.setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(getActivity(), R.color.lightgreen)));
        }

        // 創建自定義的 ActionBar 視圖
        CardView cardViewTitle = new CardView(requireContext());
        cardViewTitle.setLayoutParams(new CardView.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT));
        Drawable drawable = ContextCompat.getDrawable(requireContext(), R.drawable.cardviewtitle_shape);
        cardViewTitle.setBackground(drawable);

        // 創建 LinearLayout 用於存放圖標和標題
        LinearLayout linearLayout = new LinearLayout(requireContext());
        linearLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        ));
        linearLayout.setOrientation(LinearLayout.HORIZONTAL); // 設置水平排列

        // 設置圖標
        ImageView mark = new ImageView(requireContext());
        mark.setImageResource(R.drawable.setting1);
        mark.setPadding(10, 10, 5, 10);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                100, 100
        );
        params.setMarginStart(10);
        mark.setLayoutParams(params);

        // 設置標題
        TextView bookTitle = new TextView(requireContext());
        bookTitle.setText("設定");
        bookTitle.setTextSize(15);
        bookTitle.setTextColor(getResources().getColor(R.color.green));
        bookTitle.setPadding(10, 10, 30, 10);

        // 將圖標和標題添加到 LinearLayout 中
        linearLayout.addView(mark);
        linearLayout.addView(bookTitle);
        cardViewTitle.addView(linearLayout);

        // 設置自定義的 ActionBar
        actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false); // 隱藏原有標題
            actionBar.setDisplayShowCustomEnabled(true);
            actionBar.setCustomView(cardViewTitle, new ActionBar.LayoutParams(
                    ActionBar.LayoutParams.WRAP_CONTENT,
                    ActionBar.LayoutParams.WRAP_CONTENT,
                    Gravity.END
            ));
            actionBar.show(); // 顯示 ActionBar
        }
    }
}
