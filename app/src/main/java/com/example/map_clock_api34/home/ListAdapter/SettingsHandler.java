package com.example.map_clock_api34.home.ListAdapter;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import com.example.map_clock_api34.R;

public class SettingsHandler {
    private Context context;
    private SharedPreferences preferences;
    private static final String TAG = "SettingsHandler";

    public SettingsHandler(Context context) {
        this.context = context;
        this.preferences = context.getSharedPreferences("settings", Context.MODE_PRIVATE);
    }

    public void showSettingsPopupWindow() {
        // 创建设置弹窗视图
        View settingsView = LayoutInflater.from(context).inflate(R.layout.popupwindow_settings, null);

        // 获取设置项的控件
        Switch ringtoneSwitch = settingsView.findViewById(R.id.switchRingtone);
        Switch vibrationSwitch = settingsView.findViewById(R.id.switchVibration);
        RadioGroup notificationTimeGroup = settingsView.findViewById(R.id.radioGroupNotificationTime);
        Button cancelButton = settingsView.findViewById(R.id.PopupCancel);
        Button confirmButton = settingsView.findViewById(R.id.PopupConfirm);

        // 从SharedPreferences加载已保存的设置
        boolean isRingtoneEnabled = preferences.getBoolean("ringtone_enabled", true);
        boolean isVibrationEnabled = preferences.getBoolean("vibration_enabled", true);
        int notificationTime = preferences.getInt("notification_time", 5);

        // 设置控件的初始状态
        ringtoneSwitch.setChecked(isRingtoneEnabled);
        vibrationSwitch.setChecked(isVibrationEnabled);
        setSelectedNotificationTime(notificationTime, notificationTimeGroup);

        // 创建对话框
        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.CustomAlertDialog);
        builder.setView(settingsView);

        final AlertDialog dialog = builder.create();
        dialog.show();

        // 设置取消按钮的点击事件
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss(); // 关闭对话框
            }
        });

        // 设置确认按钮的点击事件
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 保存设置到SharedPreferences
                boolean isRingtoneEnabled = ringtoneSwitch.isChecked();
                boolean isVibrationEnabled = vibrationSwitch.isChecked();
                int notificationTime = getSelectedNotificationTime(notificationTimeGroup);

                SharedPreferences.Editor editor = preferences.edit();
                editor.putBoolean("ringtone_enabled", isRingtoneEnabled);
                editor.putBoolean("vibration_enabled", isVibrationEnabled);
                editor.putInt("notification_time", notificationTime);
                editor.apply();

                Toast.makeText(context, "已存取設定", Toast.LENGTH_SHORT).show();
                dialog.dismiss(); // 关闭对话框
                Log.d(TAG, "Initial Settings: Ringtone Enabled = " + isRingtoneEnabled);
                Log.d(TAG, "Initial Settings: Vibration Enabled = " + isVibrationEnabled);
                Log.d(TAG, "Initial Settings: Notification Time = " + notificationTime);
            }
        });
    }

    private void setSelectedNotificationTime(int notificationTime, RadioGroup notificationTimeGroup) {
        // 使用 if-else 语句来设置选中的 RadioButton
        if (notificationTime == 1) {
            notificationTimeGroup.check(R.id.radioOneMinute);
        } else if (notificationTime == 3) {
            notificationTimeGroup.check(R.id.radioThreeMinutes);
        } else if (notificationTime == 5) {
            notificationTimeGroup.check(R.id.radioFiveMinutes);
        } else {
            notificationTimeGroup.check(R.id.radioFiveMinutes); // 默认值
        }
    }

    private int getSelectedNotificationTime(RadioGroup notificationTimeGroup) {
        // 获取选中的 RadioButton ID
        int selectedId = notificationTimeGroup.getCheckedRadioButtonId();

        // 使用 if-else 语句来确定选中的时间
        if (selectedId == R.id.radioOneMinute) {
            return 1;
        } else if (selectedId == R.id.radioThreeMinutes) {
            return 3;
        } else if (selectedId == R.id.radioFiveMinutes) {
            return 5;
        }
        return selectedId;
    }
}



