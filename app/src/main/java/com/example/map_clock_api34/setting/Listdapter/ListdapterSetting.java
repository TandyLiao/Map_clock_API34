package com.example.map_clock_api34.setting.Listdapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.example.map_clock_api34.R;
import com.example.map_clock_api34.SharedViewModel;

import java.util.ArrayList;
import java.util.HashMap;

public class ListdapterSetting extends RecyclerView.Adapter<ListdapterSetting.ViewHolder> {
    private Context context;
    private ArrayList<HashMap<String, String>> arrayList;
    private SharedViewModel sharedViewModel;
    private ItemTouchHelper itemTouchHelper;
    private int selectedPosition = RecyclerView.NO_POSITION;
    private boolean enableDrag;
    private OnItemClickListener onItemClickListener;
    private SharedPreferences preferences;
    private static final String TAG = "ListdapterSetting";

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public ListdapterSetting(Context context, ArrayList<HashMap<String, String>> arrayList, SharedViewModel sharedViewModel, boolean enableDrag) {
        this.context = context;
        this.arrayList = arrayList;
        this.sharedViewModel = sharedViewModel;
        this.enableDrag = enableDrag;
        this.preferences = context.getSharedPreferences("settings", Context.MODE_PRIVATE);
    }

    public void setItemTouchHelper(ItemTouchHelper itemTouchHelper) {
        this.itemTouchHelper = itemTouchHelper;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private TextView LocateionName;
        private ImageView dragHandle;

        public ViewHolder(View itemView) {
            super(itemView);
            LocateionName = itemView.findViewById(R.id.textVLocateionName);
            dragHandle = itemView.findViewById(R.id.dragHandle);

            dragHandle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!enableDrag) {
                        int previousSelectedPosition = selectedPosition;
                        if (selectedPosition == getAdapterPosition()) {
                            selectedPosition = RecyclerView.NO_POSITION; // 取消選擇
                        } else {
                            selectedPosition = getAdapterPosition();
                        }
                        notifyItemChanged(previousSelectedPosition);
                        notifyItemChanged(selectedPosition);
                    }
                    if (onItemClickListener != null) {
                        onItemClickListener.onItemClick(getAdapterPosition());
                    }
                    showSettingsPopupWindow();
                }
            });

            dragHandle.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        if (itemTouchHelper != null && enableDrag) {
                            itemTouchHelper.startDrag(ViewHolder.this);
                        }
                    }
                    return false;
                }
            });
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycleviewitem_route, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.LocateionName.setText(arrayList.get(position).get("data"));

        ViewGroup.LayoutParams layoutParams = holder.itemView.getLayoutParams();
        layoutParams.height = 150;
        holder.itemView.setLayoutParams(layoutParams);

        if (!enableDrag) {
            if (position == selectedPosition) {
                holder.dragHandle.setImageResource(R.drawable.anya062516);
            } else {
                holder.dragHandle.setImageResource(R.drawable.vibrate);
            }
        }
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public void showSettingsPopupWindow() {
        View settingsView = LayoutInflater.from(context).inflate(R.layout.popupwindow_settings, null);

        Switch ringtoneSwitch = settingsView.findViewById(R.id.switchRingtone);
        Switch vibrationSwitch = settingsView.findViewById(R.id.switchVibration);
        RadioGroup notificationTimeGroup = settingsView.findViewById(R.id.radioGroupNotificationTime);
        Button cancelButton = settingsView.findViewById(R.id.PopupCancel);
        Button confirmButton = settingsView.findViewById(R.id.PopupConfirm);

        boolean isRingtoneEnabled = preferences.getBoolean("ringtone_enabled", true);
        boolean isVibrationEnabled = preferences.getBoolean("vibration_enabled", true);
        int notificationTime = preferences.getInt("notification_time", 5);

        ringtoneSwitch.setChecked(isRingtoneEnabled);
        vibrationSwitch.setChecked(isVibrationEnabled);
        setSelectedNotificationTime(notificationTime, notificationTimeGroup);

        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.CustomAlertDialog);
        builder.setView(settingsView);

        final AlertDialog dialog = builder.create();
        dialog.show();

        cancelButton.setOnClickListener(v -> dialog.dismiss());

        confirmButton.setOnClickListener(v -> {
            boolean newRingtoneEnabled = ringtoneSwitch.isChecked();
            boolean newVibrationEnabled = vibrationSwitch.isChecked();
            int newNotificationTime = getSelectedNotificationTime(notificationTimeGroup);

            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean("ringtone_enabled", newRingtoneEnabled);
            editor.putBoolean("vibration_enabled", newVibrationEnabled);
            editor.putInt("notification_time", newNotificationTime);
            editor.apply();

            Toast.makeText(context, "已存取設定", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
            Log.d(TAG, "Settings Updated: Ringtone = " + newRingtoneEnabled + ", Vibration = " + newVibrationEnabled + ", Time = " + newNotificationTime);
        });
    }

    private void setSelectedNotificationTime(int notificationTime, RadioGroup notificationTimeGroup) {
        if (notificationTime == 1) {
            notificationTimeGroup.check(R.id.radioOneMinute);
        } else if (notificationTime == 3) {
            notificationTimeGroup.check(R.id.radioThreeMinutes);
        } else {
            notificationTimeGroup.check(R.id.radioFiveMinutes);
        }
    }

    private int getSelectedNotificationTime(RadioGroup notificationTimeGroup) {
        int selectedId = notificationTimeGroup.getCheckedRadioButtonId();
        if (selectedId == R.id.radioOneMinute) {
            return 1;
        } else if (selectedId == R.id.radioThreeMinutes) {
            return 3;
        } else if (selectedId == R.id.radioFiveMinutes) {
            return 5;
        }
        return 5; // 默認值
    }
}