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
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.map_clock_api34.R;
import com.example.map_clock_api34.TutorialFragment;

public class WhichTutorialFragment extends Fragment {

    View rootView;

    private ActionBar actionBar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.setting_which_tutorial, container, false);


        setupButton();
        setupActionBar();

        return rootView;
    }

    private void setupButton() {
        Button btn_createLocation = rootView.findViewById(R.id.btn_createLocation);
        btn_createLocation.setOnClickListener(v -> {

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
        btn_note.setOnClickListener(v -> {
            // 檢查使用者是否已經造訪過，未造訪則跳轉到教學頁面
            SharedPreferences sharedPreferences = getActivity().getSharedPreferences("MyPrefs", MODE_PRIVATE);

            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt("WhichPage", 4);
            editor.apply();

            TutorialFragment tutorialFragment = new TutorialFragment();
            FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fl_container, tutorialFragment);
            transaction.addToBackStack(null);
            transaction.commit();
        });

        Button btn_locationSetting = rootView.findViewById(R.id.btn_locationSetting);
        btn_locationSetting.setOnClickListener(v -> {
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
        btn_findBus.setOnClickListener(v -> {
            // 檢查使用者是否已經造訪過，未造訪則跳轉到教學頁面
            SharedPreferences sharedPreferences = getActivity().getSharedPreferences("MyPrefs", MODE_PRIVATE);

            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt("WhichPage", 5);
            editor.apply();

            TutorialFragment tutorialFragment = new TutorialFragment();
            FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fl_container, tutorialFragment);
            transaction.addToBackStack(null);
            transaction.commit();
        });

        Button btn_findWeather = rootView.findViewById(R.id.btn_findWeather);
        btn_findWeather.setOnClickListener(v -> {

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
        btn_book.setOnClickListener(v -> {
            // 檢查使用者是否已經造訪過，未造訪則跳轉到教學頁面
            SharedPreferences sharedPreferences = getActivity().getSharedPreferences("MyPrefs", MODE_PRIVATE);

            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt("WhichPage", 2);
            editor.apply();

            TutorialFragment tutorialFragment = new TutorialFragment();
            FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fl_container, tutorialFragment);
            transaction.addToBackStack(null);
            transaction.commit();
        });

        Button btn_history = rootView.findViewById(R.id.btn_history);
        btn_history.setOnClickListener(v -> {
            // 檢查使用者是否已經造訪過，未造訪則跳轉到教學頁面
            SharedPreferences sharedPreferences = getActivity().getSharedPreferences("MyPrefs", MODE_PRIVATE);

            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt("WhichPage", 1);
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
        actionBar = ((AppCompatActivity) requireActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
            actionBar.setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(getActivity(), R.color.lightgreen)));
        }

        CardView cardViewtitle = new CardView(requireContext());
        cardViewtitle.setLayoutParams(new CardView.LayoutParams(ActionBar.LayoutParams.WRAP_CONTENT, ActionBar.LayoutParams.WRAP_CONTENT));
        Drawable drawable = ContextCompat.getDrawable(requireContext(), R.drawable.cardviewtitle_shape);
        cardViewtitle.setBackground(drawable);

        LinearLayout linearLayout = new LinearLayout(requireContext());
        linearLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);

        // 添加書籤圖標
        ImageView bookmark = new ImageView(requireContext());
        bookmark.setImageResource(R.drawable.setting1);
        bookmark.setPadding(10, 10, 5, 10); // 設置圖標邊距
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(100, 100);
        params.setMarginStart(10); // 設置左邊距
        bookmark.setLayoutParams(params);

        // 添加標題文字
        TextView bookTitle = new TextView(requireContext());
        bookTitle.setText("操作說明");
        bookTitle.setTextSize(15);
        bookTitle.setTextColor(getResources().getColor(R.color.green)); // 設置文字顏色
        bookTitle.setPadding(10, 10, 30, 10); // 設置內距

        linearLayout.addView(bookmark);
        linearLayout.addView(bookTitle);
        cardViewtitle.addView(linearLayout);

        // 建立自定義返回按鈕
        ImageView returnButton = new ImageView(requireContext());
        returnButton.setImageResource(R.drawable.back);
        LinearLayout.LayoutParams returnButtonParams = new LinearLayout.LayoutParams(100, 100);
        returnButton.setLayoutParams(returnButtonParams);

        // 建立 ActionBar 的佈局，包含返回按鈕和標題
        LinearLayout actionBarLayout = new LinearLayout(requireContext());
        actionBarLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
        actionBarLayout.setOrientation(LinearLayout.HORIZONTAL);
        actionBarLayout.setWeightSum(1.0f);

        LinearLayout leftLayout = new LinearLayout(requireContext());
        leftLayout.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 0.1f));
        leftLayout.setOrientation(LinearLayout.HORIZONTAL);
        leftLayout.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
        leftLayout.addView(returnButton);

        LinearLayout rightLayout = new LinearLayout(requireContext());
        rightLayout.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 0.9f));
        rightLayout.setOrientation(LinearLayout.HORIZONTAL);
        rightLayout.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
        rightLayout.addView(cardViewtitle);

        actionBarLayout.addView(leftLayout);
        actionBarLayout.addView(rightLayout);

        Toolbar toolbar = getActivity().findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(null); // 隱藏漢堡選單

        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false); // 隱藏預設標題
            actionBar.setDisplayShowCustomEnabled(true);
            actionBar.setCustomView(actionBarLayout, new ActionBar.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT));
            actionBar.show();
        }

        //設置acrionBar上的返回按鈕
        returnButton.setOnClickListener(v -> {

            getActivity().getSupportFragmentManager().popBackStack(); // 回到上一頁
        });

    }
}
