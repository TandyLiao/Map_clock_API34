package com.example.map_clock_api34.setting;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.preference.PreferenceManager;

import com.example.map_clock_api34.R;
import com.example.map_clock_api34.TutorialFragment;

public class SettingRemind extends Fragment {

    private static final int RINGTONE_PICKER_REQUEST_CODE = 1002;

    View rootView;
    private Toolbar toolbar; // 側邊選單的開關
    private DrawerLayout drawerLayout;      // 抽屜佈局
    private ActionBarDrawerToggle toggle;   // 側邊選單的開關

    private Ringtone mRingtone; // 用於播放系統鈴聲
    private AudioManager mAudioManager; // 用於管理音頻的 AudioManager

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.setting_remind, container, false); // 加載佈局

        mAudioManager = (AudioManager) requireContext().getSystemService(Context.AUDIO_SERVICE); // 初始化音頻管理器

        // 鈴聲選擇按鈕
        Button chooseButton = rootView.findViewById(R.id.btn_createLocation);
        // 震動測試按鈕
        Button vibrateTest = rootView.findViewById(R.id.vibrate_testt);

        // 鈴聲開關
        Switch switchRing = rootView.findViewById(R.id.switch_Ring);
        // 鈴聲開關的事件處理
        switchRing.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                playRingtone(); // 播放鈴聲
                if (isSilentMode()) {
                    Toast.makeText(requireContext(), "現在您是靜音模式，請打開才可聽到鈴聲", Toast.LENGTH_SHORT).show();
                }
            } else {
                stopRingtone(); // 停止鈴聲
            }
        });

        // 鈴聲選擇按鈕的事件處理
        chooseButton.setOnClickListener(v -> {
            Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_RINGTONE);
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "選擇鈴聲");
            startActivityForResult(intent, RINGTONE_PICKER_REQUEST_CODE); // 開啟系統鈴聲選擇器
        });

        // 震動測試按鈕的事件處理
        vibrateTest.setOnClickListener(v -> {

            WhichTutorialFragment whichTutorialFragment = new WhichTutorialFragment();
            FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fl_container, whichTutorialFragment);
            transaction.addToBackStack(null);
            transaction.commit();
        });

        // 震動開關
        Switch switchVibrate = rootView.findViewById(R.id.switch_VIBRATE);
        // 震動開關的事件處理
        switchVibrate.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                startVibrate(); // 開啟震動
            } else {
                stopVibrate(); // 停止震動
            }
        });

        if (getActivity() != null) {
            drawerLayout = getActivity().findViewById(R.id.drawerLayout);
            toolbar = requireActivity().findViewById(R.id.toolbar);
        }

        setupActionBar(); // 設置自定義 ActionBar
        updateButtonLabels(); // 更新按鈕標籤

        return rootView;
    }

    // 播放鈴聲的方法
    private void playRingtone() {
        Uri ringtoneUri = loadRingtoneUri(); // 取得已選鈴聲的 URI

        if (ringtoneUri != null) {
            mRingtone = RingtoneManager.getRingtone(requireContext(), ringtoneUri); // 取得 Ringtone
            if (mRingtone != null) {
                mRingtone.play(); // 播放鈴聲
                new Handler().postDelayed(() -> stopRingtone(), 3000); // 設置 3 秒後停止鈴聲
            }
        } else {
            Toast.makeText(requireContext(), "請先選擇鈴聲", Toast.LENGTH_SHORT).show();
        }
    }

    // 從偏好設定中載入鈴聲 URI
    private Uri loadRingtoneUri() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(requireContext());
        String uriString = preferences.getString("ringtone_uri", null);
        return uriString != null ? Uri.parse(uriString) : null;
    }

    // 停止鈴聲的方法
    private void stopRingtone() {
        if (mRingtone != null && mRingtone.isPlaying()) {
            mRingtone.stop(); // 停止鈴聲
        }
    }

    // 開始震動的方法
    private void startVibrate() {
        Vibrator vibrator = (Vibrator) requireContext().getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator != null && vibrator.hasVibrator()) {
            vibrator.vibrate(1000); // 震動 1 秒
        }
    }

    // 停止震動的方法
    private void stopVibrate() {
        Vibrator vibrator = (Vibrator) requireContext().getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator != null) {
            vibrator.cancel(); // 停止震動
        }
    }

    // 判斷當前是否為靜音模式
    private boolean isSilentMode() {
        int ringerMode = mAudioManager.getRingerMode();
        return ringerMode == AudioManager.RINGER_MODE_SILENT || ringerMode == AudioManager.RINGER_MODE_VIBRATE;
    }

    // 在選擇鈴聲後處理結果
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == getActivity().RESULT_OK && requestCode == RINGTONE_PICKER_REQUEST_CODE) {
            Uri uri = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI); // 取得選擇的鈴聲 URI
            if (uri != null) {
                saveRingtoneUri(uri); // 保存 URI 到偏好設定
                mRingtone = RingtoneManager.getRingtone(requireContext(), uri); // 更新 Ringtone
            }
        }
    }

    // 保存鈴聲 URI
    private void saveRingtoneUri(Uri uri) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(requireContext());
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("ringtone_uri", uri.toString());
        editor.apply();
    }

    // 更新按鈕標籤
    private void updateButtonLabels() {
        TextView ring = rootView.findViewById(R.id.text_RING); // 鈴聲文本
        ring.setText("鈴聲");

        TextView vibrate; // 震動文本
        vibrate = rootView.findViewById(R.id.text_VIBRATE);
        vibrate.setText("震動");
    }

    // 設置自定義 ActionBar
    private void setupActionBar() {
        ActionBar actionBar = ((AppCompatActivity) requireActivity()).getSupportActionBar();
        if (actionBar != null) {
            if (drawerLayout != null) {
                if (toggle == null) {
                    toggle = new ActionBarDrawerToggle(requireActivity(), drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close);
                    drawerLayout.addDrawerListener(toggle);
                    toggle.syncState();
                }
                toggle.getDrawerArrowDrawable().setColor(getResources().getColor(R.color.green));


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
    }
    // 設置側邊欄
    private void setupNavigationDrawer() {
        drawerLayout = requireActivity().findViewById(R.id.drawerLayout);
        toolbar = requireActivity().findViewById(R.id.toolbar);
        toggle = new ActionBarDrawerToggle(requireActivity(), drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        toggle.getDrawerArrowDrawable().setColor(getResources().getColor(R.color.green));
    }


    @Override
    public void onResume() {
        super.onResume();
        updateButtonLabels();
        setupActionBar();   // 設置工具列
        setupNavigationDrawer();    // 設置側邊欄
    }

    @Override
    public void onPause() {
        super.onPause();

        // 恢復 ActionBar 的原始狀態
        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowCustomEnabled(false);
            actionBar.setCustomView(null);
            actionBar.setDisplayShowTitleEnabled(true);
            actionBar.show();
        }
    }
}
