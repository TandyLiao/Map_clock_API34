package com.example.map_clock_api34.setting;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.os.Vibrator;
import com.example.map_clock_api34.R;

public class SettingRemind extends AppCompatActivity {

    private MediaPlayer mMediaPlayer;
    private Handler mHandler = new Handler();
    private Ringtone mRingtone;
    private AudioManager mAudioManager;
    private static final int PERMISSION_REQUEST_CODE = 1001;
    private static final int RINGTONE_PICKER_REQUEST_CODE = 1002;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setting_remind);
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        Button chooseButton = findViewById(R.id.choose_setting);
        Button backButton = findViewById(R.id.back);
        Button ringTest = findViewById(R.id.ring_test);
        Button vibrateTest = findViewById(R.id.vibrate_testt);

        Switch switchRing = findViewById(R.id.switch_Ring);
        Switch switchVibrate = findViewById(R.id.switch_VIBRATE);

        switchRing.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    playRingtone();
                    if (isSilentMode()) {
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
                Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_RINGTONE);
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "选择铃声");
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
                final Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

                if (vibrator == null || !vibrator.hasVibrator()) {
                    Toast.makeText(SettingRemind.this, "您的设备不支持震动功能", Toast.LENGTH_SHORT).show();
                    return;
                }


                final long[] vibrationPattern = {0, 1000, 1000, 1500, 1000, 2000}; // {等待, 震动1秒, 等待1秒, 震动2秒, 等待1秒, 震动3秒}

                // Vibrate pattern array: { delay before start, vibrate duration, delay, vibrate duration, ...}
                // 0 - start immediately, 1000 - vibrate 1 second, 1000 - delay 1 second, 2000 - vibrate 2 seconds, etc.

                vibrator.vibrate(vibrationPattern, -1); // -1 means don't repeat
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

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private void playRingtone() {
        Uri ringtoneUri = loadRingtoneUri();
        if (ringtoneUri != null) {
            mRingtone = RingtoneManager.getRingtone(getApplicationContext(), ringtoneUri);
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
            Toast.makeText(this, "请先选择铃声", Toast.LENGTH_SHORT).show();
        }
    }

    private Uri loadRingtoneUri() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String uriString = preferences.getString("ringtone_uri", null);
        return uriString != null ? Uri.parse(uriString) : null;
    }

    private void playRingtoneWithIncreasingVolume() {
        Uri ringtoneUri = loadRingtoneUri(); // Load the saved ringtone URI

        final int duration = 3000;
        final int intervals = 3;
        final float volumeIncrement = 1.0f / intervals;

        mMediaPlayer = MediaPlayer.create(this, ringtoneUri);

        if (mMediaPlayer == null) {
            return;
        }
        mMediaPlayer.setLooping(false);

        for (int i = 0; i < intervals; i++) {
            final int iteration = i;
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mMediaPlayer.setVolume(volumeIncrement * (iteration + 1), volumeIncrement * (iteration + 1));
                    if (mMediaPlayer.isPlaying()) {
                        mMediaPlayer.seekTo(0);
                    } else {
                        mMediaPlayer.start();
                    }
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mMediaPlayer.pause();
                        }
                    }, duration);
                }
            }, i * (duration + 1000));
        }
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

    private void stopRingtone() {
        if (mRingtone != null && mRingtone.isPlaying()) {
            mRingtone.stop();
        }
    }

    private void startVibrate() {
        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator != null && vibrator.hasVibrator()) {
            vibrator.vibrate(1000);
        }
    }

    private void stopVibrate() {
        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator != null) {
            vibrator.cancel();
        }
    }

    private boolean isSilentMode() {
        int ringerMode = mAudioManager.getRingerMode();
        return ringerMode == AudioManager.RINGER_MODE_SILENT || ringerMode == AudioManager.RINGER_MODE_VIBRATE;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == RINGTONE_PICKER_REQUEST_CODE) {
            Uri uri = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
            if (uri != null) {
                saveRingtoneUri(uri);
                mRingtone = RingtoneManager.getRingtone(getApplicationContext(), uri);
            }
        } else if (requestCode == PERMISSION_REQUEST_CODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (Settings.System.canWrite(this)) {
                    proceedToRingtoneSelection();
                } else {
                    Toast.makeText(this, "需要权限来更改铃声设置", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void saveRingtoneUri(Uri ringtoneUri) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("ringtone_uri", ringtoneUri.toString());
        editor.apply();
        Log.d("SettingRemind", "Saved ringtone URI: " + ringtoneUri.toString());
    }
}

