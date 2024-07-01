package com.example.map_clock_api34.setting;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Looper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.preference.PreferenceManager;

import com.example.map_clock_api34.R;

import java.util.Locale;

public class SettingRemind extends Fragment {

    private MediaPlayer mMediaPlayer;
    private Handler mHandler = new Handler();
    private Ringtone mRingtone;
    private AudioManager mAudioManager;
    private static final int PERMISSION_REQUEST_CODE = 1001;
    private static final int RINGTONE_PICKER_REQUEST_CODE = 1002;
    private Button chooseButton;
    private Button ringTest;
    private Button vibrateTest;
    private Switch switchRing;
    private Switch switchVibrate;
    private TextView ring;
    private TextView vibrate;
    private TextView texttt;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.setting_remind, container, false);
        mAudioManager = (AudioManager) requireContext().getSystemService(Context.AUDIO_SERVICE);

        chooseButton = view.findViewById(R.id.choose_setting);
        ringTest = view.findViewById(R.id.ring_test);
        vibrateTest = view.findViewById(R.id.vibrate_testt);
        switchRing = view.findViewById(R.id.switch_Ring);
        switchVibrate = view.findViewById(R.id.switch_VIBRATE);
        ring = view.findViewById(R.id.text_RING);
        vibrate = view.findViewById(R.id.text_VIBRATE);
        texttt = view.findViewById(R.id.texttt);

        loadLocale(); // 加载语言设置

        switchRing.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    playRingtone();
                    if (isSilentMode()) {
                        Toast.makeText(requireContext(), "現在您是靜音模式，請打開才可聽到鈴聲", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    stopRingtone();
                }
            }
        });

        chooseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_RINGTONE);
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "選擇鈴聲");
                startActivityForResult(intent, RINGTONE_PICKER_REQUEST_CODE);
            }
        });

        ringTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playRingtoneWithIncreasingVolume();
            }
        });

        vibrateTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Vibrator vibrator = (Vibrator) requireContext().getSystemService(Context.VIBRATOR_SERVICE);

                if (vibrator == null || !vibrator.hasVibrator()) {
                    Toast.makeText(requireContext(), "設備不支持震動功能", Toast.LENGTH_SHORT).show();
                    return;
                }

                final long[] vibrationPattern = {0, 1000, 1000, 1500, 1000, 2000}; // {等待, 震动1秒, 等待1秒, 震动2秒, 等待1秒, 震动3秒}
                vibrator.vibrate(vibrationPattern, -1); // -1 表示不重复
            }
        });

        switchVibrate.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    startVibrate();
                } else {
                    stopVibrate();
                }
            }
        });

        updateButtonLabels(); // 初始化时更新按钮文本

        return view;
    }

    private void playRingtone() {
        Uri ringtoneUri = loadRingtoneUri();
        if (ringtoneUri != null) {
            mRingtone = RingtoneManager.getRingtone(requireContext(), ringtoneUri);
            if (mRingtone != null) {
                mRingtone.play();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        stopRingtone();
                    }
                }, 3000);
            }
        } else {
            Toast.makeText(requireContext(), "請先選擇鈴聲", Toast.LENGTH_SHORT).show();
        }
    }

    private Uri loadRingtoneUri() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(requireContext());
        String uriString = preferences.getString("ringtone_uri", null);
        return uriString != null ? Uri.parse(uriString) : null;
    }

    private void playRingtoneWithIncreasingVolume() {
        Uri ringtoneUri = loadRingtoneUri(); // Load the saved ringtone URI

        final int duration = 3000;
        final int intervals = 3;
        final float volumeIncrement = 1.0f / intervals;

        mMediaPlayer = MediaPlayer.create(requireContext(), ringtoneUri);

        if (mMediaPlayer == null) {
            return;
        }
        mMediaPlayer.setLooping(false);

        // 使用 Handler 來控制音量增加和播放
        Handler mHandler = new Handler(Looper.getMainLooper());

        for (int i = 0; i < intervals; i++) {
            final int iteration = i;
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    float newVolume = volumeIncrement * (iteration + 1);
                    mMediaPlayer.setVolume(newVolume, newVolume);
                    if (!mMediaPlayer.isPlaying()) {
                        mMediaPlayer.start();
                    }
                }
            }, i * (duration + 1000));
        }

        // 設置在播放完成後停止並釋放 MediaPlayer
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mMediaPlayer != null) {
                    mMediaPlayer.stop();
                    mMediaPlayer.release();
                    mMediaPlayer = null;
                }
            }
        }, intervals * (duration + 1000));
    }

    private void selectRingtone() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.System.canWrite(requireContext())) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                intent.setData(Uri.parse("package:" + requireActivity().getPackageName()));
                startActivityForResult(intent, PERMISSION_REQUEST_CODE);
            } else {
                proceedToRingtoneSelection();
            }
        } else {
            proceedToRingtoneSelection();
        }
    }

    private void proceedToRingtoneSelection() {
        Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_RINGTONE);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "選擇鈴聲");
        startActivityForResult(intent, RINGTONE_PICKER_REQUEST_CODE);
    }

    private void stopRingtone() {
        if (mRingtone != null && mRingtone.isPlaying()) {
            mRingtone.stop();
        }
    }

    private void startVibrate() {
        Vibrator vibrator = (Vibrator) requireContext().getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator != null && vibrator.hasVibrator()) {
            vibrator.vibrate(1000);
        }
    }

    private void stopVibrate() {
        Vibrator vibrator = (Vibrator) requireContext().getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator != null) {
            vibrator.cancel();
        }
    }

    private boolean isSilentMode() {
        int ringerMode = mAudioManager.getRingerMode();
        return ringerMode == AudioManager.RINGER_MODE_SILENT || ringerMode == AudioManager.RINGER_MODE_VIBRATE;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == getActivity().RESULT_OK && requestCode == RINGTONE_PICKER_REQUEST_CODE) {
            Uri uri = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
            if (uri != null) {
                saveRingtoneUri(uri);
                mRingtone = RingtoneManager.getRingtone(requireContext(), uri);
            }
        } else if (requestCode == PERMISSION_REQUEST_CODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (Settings.System.canWrite(requireContext())) {
                    proceedToRingtoneSelection();
                } else {
                    Toast.makeText(requireContext(), "沒有允許設置系統的權限", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void saveRingtoneUri(Uri uri) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(requireContext());
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("ringtone_uri", uri.toString());
        editor.apply();
    }

    private void loadLocale() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(requireContext());
        String language = prefs.getString("My_Lang", "");
        setLocale(language);
    }

    private void setLocale(String lang) {
        Locale locale = new Locale(lang);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        requireContext().getResources().updateConfiguration(config, requireContext().getResources().getDisplayMetrics());

        // Update text views or any UI components if needed
        updateButtonLabels();
    }

    private void updateButtonLabels() {
        ring.setText("鈴聲");
        vibrate.setText("震動");
        texttt.setText("測試");
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



