package com.example.map_clock_api34;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.Switch;
import android.widget.CompoundButton;
import android.net.Uri;
import android.widget.Toast;
import android.os.Vibrator;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.os.Build;
import android.provider.Settings;

import android.content.Intent;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;

public class SettingRemind extends AppCompatActivity {

    private Ringtone mRingtone;
    private AudioManager mAudioManager;
    private static final int PERMISSION_REQUEST_CODE = 1001;
    private static final int RINGTONE_PICKER_REQUEST_CODE = 1002;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setting_remind); // 加载 XML 布局文件
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        // 查找 XML 文件中的按钮
        Button chooseButton = findViewById(R.id.choose_setting);
        Button backButton = findViewById(R.id.back);

        // 查找 XML 文件中的開關
        Switch switchRing = findViewById(R.id.switch_Ring);
        Switch switchVibrate = findViewById(R.id.switch_VIBRATE);

        switchRing.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // 在這裡執行開關狀態發生變化時的操作
                if (isChecked) {
                    playRingtone();
                    if (isSilentMode()) {
                        // 如果当前是静音模式，向用户显示提示
                        Toast.makeText(SettingRemind.this, "現在您是靜音模式，請打開以聽到鈴聲", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    stopRingtone();
                }
            }
        });

        chooseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 在按钮点击时执行的操作
                Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
                // 设置类型为默认铃声
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_RINGTONE);
                // 设置标题
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "选择铃声");
                // 设置默认的铃声 URI
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, (Uri) null);
                // 启动选择铃声的活动
                startActivityForResult(intent, 0);
            }
        });


        switchVibrate.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // 在这里执行开关状态发生变化时的操作
                if (isChecked) {
                    // 如果开关被打开，执行震动操作
                    startVibrate();
                } else {
                    // 如果开关被关闭，停止震动
                    stopVibrate();
                }
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

    // 播放铃声的方法
    private void playRingtone() {
        Uri ringtoneUri = loadRingtoneUri(); // 加载保存的铃声 URI
        if (ringtoneUri != null) {
            mRingtone = RingtoneManager.getRingtone(getApplicationContext(), ringtoneUri);
            if (mRingtone != null) {
                // 如果获取铃声成功，则播放铃声，并设置播放时长为3秒
                mRingtone.play();
                // 延迟3秒后停止播放
                new Handler().postDelayed(new Runnable() {
                    @Override


                    public void run() {
                        stopRingtone();
                    }
                }, 3000); // 3000 毫秒 = 3 秒
            }
        } else {
            // 如果没有保存的铃声 URI，则提示用户选择铃声
            selectRingtone();
        }
    }
    private Uri loadRingtoneUri() {
        // 获取 SharedPreferences 实例
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        // 从 SharedPreferences 中加载铃声 URI
        String uriString = preferences.getString("ringtone_uri", null);
        if (uriString != null) {
            return Uri.parse(uriString);
        } else {
            return null;
        }
    }

    private void selectRingtone() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.System.canWrite(this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                intent.setData(Uri.parse("package:" + getPackageName()));
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
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "选择铃声");
        startActivityForResult(intent, RINGTONE_PICKER_REQUEST_CODE);
    }


    // 停止播放铃声的方法
    private void stopRingtone() {
        if (mRingtone != null && mRingtone.isPlaying()) {
            // 停止播放铃声
            mRingtone.stop();
        }
    }
    private void startVibrate() {
        // 在这里执行震动操作
        // 使用系统服务获取震动器实例
        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        // 检查设备是否支持震动
        if (vibrator.hasVibrator()) {
            // 设置震动模式，这里设置震动1秒钟
            vibrator.vibrate(1000);
        }
    }

    // 停止震动的方法
    private void stopVibrate() {
        // 在这里执行停止震动的操作
        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        // 停止震动
        vibrator.cancel();
    }
    // 检查设备是否处于静音模式的方法
    private boolean isSilentMode() {
        int ringerMode = mAudioManager.getRingerMode();
        return ringerMode == AudioManager.RINGER_MODE_SILENT || ringerMode == AudioManager.RINGER_MODE_VIBRATE;
    }

    // 定义与 app:onClick 属性匹配的方法
    public void ringDevice(View view) {
        // 在按钮点击时执行的操作
    }
    // 在 SettingRemind 类中的其他方法之后添加
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            // 获取用户选择的铃声 URI
            Uri uri = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
            // 将选择的铃声设置为当前铃声
            if (uri != null) {
                mRingtone = RingtoneManager.getRingtone(getApplicationContext(), uri);
                saveRingtoneUri(uri); // 保存用户选择的铃声 URI
            }
        }
    }

    private void saveRingtoneUri(Uri ringtoneUri) {
        // 获取 SharedPreferences 实例
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        // 使用 SharedPreferences.Editor 保存铃声 URI
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("ringtone_uri", ringtoneUri.toString());
        editor.apply();
        Log.d("SettingRemind", "Saved ringtone URI: " + ringtoneUri.toString());
    }

}
