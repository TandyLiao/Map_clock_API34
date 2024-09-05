package com.example.map_clock_api34.setting.Listdapter;


import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
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
import java.util.HashSet;
import java.util.Set;

public class ListdapterSetting extends RecyclerView.Adapter<ListdapterSetting.ViewHolder> {
    private Context context;
    private ArrayList<HashMap<String, String>> arrayList;
    private SharedViewModel sharedViewModel;
    private ItemTouchHelper itemTouchHelper;
    private int selectedPosition = RecyclerView.NO_POSITION;
    private Set<Integer> selectedPositions = new HashSet<>(); // 用於存儲選中的項目位置

    private boolean enableDrag;
    private OnItemClickListener onItemClickListener;
    private static final String TAG = "ListdapterSetting";

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public ListdapterSetting(Context context, ArrayList<HashMap<String, String>> arrayList, SharedViewModel sharedViewModel, boolean enableDrag) {
        this.context = context;
        this.arrayList = arrayList;
        this.sharedViewModel = sharedViewModel;
        this.enableDrag = enableDrag;
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
                        selectedPosition = getAdapterPosition();

                        Log.d(TAG, "Clicked position: " + selectedPosition);
                        int position = getAdapterPosition();
                        if (selectedPositions.contains(position)) {
                            selectedPositions.remove(position); // 如果已選中，則取消選中
                        } else {
                            selectedPositions.add(position); // 如果未選中，則添加到選中集合
                        }
                        notifyItemChanged(position);
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
        boolean vibrate = sharedViewModel.getVibrate(position);
        boolean ringtone = sharedViewModel.getRingtone(position);
        int notification = sharedViewModel.getNotification(position);


        if (!enableDrag) {
            if (!vibrate || ! ringtone || notification !=5) {
                holder.dragHandle.setImageResource(R.drawable.vibrate_red);
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
        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.CustomAlertDialog);

        //套用XML的布局
        LayoutInflater inflater = LayoutInflater.from(context);
        View settingsView = inflater.inflate(R.layout.popupwindow_settings, null);

        Switch ringtoneSwitch = settingsView.findViewById(R.id.switchRingtone);
        Switch vibrationSwitch = settingsView.findViewById(R.id.switchVibration);
        RadioGroup notificationTimeGroup = settingsView.findViewById(R.id.radioGroupNotificationTime);
        Button cancelButton = settingsView.findViewById(R.id.PopupCancel);
        Button confirmButton = settingsView.findViewById(R.id.PopupConfirm);

        boolean isRingtoneEnabled = sharedViewModel.getRingtone(selectedPosition);
        boolean isVibrationEnabled = sharedViewModel.getVibrate(selectedPosition);
        int notificationTime = sharedViewModel.getNotification(selectedPosition);

        ringtoneSwitch.setChecked(isRingtoneEnabled);
        vibrationSwitch.setChecked(isVibrationEnabled);
        setSelectedNotificationTime(notificationTime, notificationTimeGroup);
        builder.setView(settingsView);

        final AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();

        cancelButton.setOnClickListener(v -> dialog.dismiss());

        confirmButton.setOnClickListener(v -> {
            boolean newRingtoneEnabled = ringtoneSwitch.isChecked();
            boolean newVibrationEnabled = vibrationSwitch.isChecked();
            int newNotificationTime = getSelectedNotificationTime(notificationTimeGroup);

            sharedViewModel.setVibrate(vibrationSwitch.isChecked(), selectedPosition);
            sharedViewModel.setRingtone(ringtoneSwitch.isChecked(), selectedPosition);
            sharedViewModel.setNotification(newNotificationTime, selectedPosition);

            // 刷新 RecyclerView
            notifyDataSetChanged();

            String destinationName = arrayList.get(selectedPosition).get("data"); // 取得選定的目的地名稱

            Toast.makeText(context, "已存取設定", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
            Log.d(TAG, "Settings Updated: Ringtone = " + newRingtoneEnabled +
                    ", Vibration = " + newVibrationEnabled +
                    ", Time = " + newNotificationTime +
                    ", Destination = " + destinationName);

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
    private void updateicon(){

    }
}

