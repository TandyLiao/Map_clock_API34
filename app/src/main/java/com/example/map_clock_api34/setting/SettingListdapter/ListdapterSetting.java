package com.example.map_clock_api34.setting.SettingListdapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.LayoutInflater;
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
import androidx.recyclerview.widget.RecyclerView;

import com.example.map_clock_api34.R;
import com.example.map_clock_api34.SharedViewModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class ListdapterSetting extends RecyclerView.Adapter<ListdapterSetting.ViewHolder> {

    private Context context;
    private SharedViewModel sharedViewModel;

    private int selectedPosition = RecyclerView.NO_POSITION; // 當前選擇的位置
    private Set<Integer> selectedPositions = new HashSet<>(); // 存儲多個選中項目的位置

    private boolean enableDrag; // 是否允許拖動
    private static final String TAG = "ListdapterSetting"; // 用於日誌
    private ArrayList<HashMap<String, String>> arrayList;

    // 建構函式
    public ListdapterSetting(Context context, ArrayList<HashMap<String, String>> arrayList, SharedViewModel sharedViewModel, boolean enableDrag) {
        this.context = context;
        this.arrayList = arrayList;
        this.sharedViewModel = sharedViewModel;
        this.enableDrag = enableDrag;
    }

    // ViewHolder 類別，負責 RecyclerView 的每個項目視圖
    class ViewHolder extends RecyclerView.ViewHolder {
        private TextView LocateionName; // 顯示位置名稱
        private ImageView dragHandle; // 用於拖動的圖標

        // ViewHolder 建構函式
        public ViewHolder(View itemView) {
            super(itemView);
            LocateionName = itemView.findViewById(R.id.textVLocateionName); // 取得位置名稱的 TextView
            dragHandle = itemView.findViewById(R.id.dragHandle); // 取得拖動圖標

            // 點擊拖動圖標的邏輯
            dragHandle.setOnClickListener(v -> {
                if (!enableDrag) { // 如果不允許拖動，進行選中處理
                    selectedPosition = getAdapterPosition(); // 取得當前項目的位置

                    Log.d(TAG, "Clicked position: " + selectedPosition);
                    int position = getAdapterPosition();
                    if (selectedPositions.contains(position)) {
                        selectedPositions.remove(position); // 如果已選中，則取消選中
                    } else {
                        selectedPositions.add(position); // 如果未選中，則添加到選中集合
                    }
                    notifyItemChanged(position); // 刷新當前項目視圖
                }
                showSettingsPopupWindow(); // 顯示設定的彈窗
            });

        }
    }

    // 創建 ViewHolder
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycleviewitem_route, parent, false); // 取得列表項目的佈局
        return new ViewHolder(view); // 創建 ViewHolder
    }

    // 綁定資料到 ViewHolder
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.LocateionName.setText(arrayList.get(position).get("data")); // 設定位置名稱

        // 設定每個項目的高度
        ViewGroup.LayoutParams layoutParams = holder.itemView.getLayoutParams();
        layoutParams.height = 150;
        holder.itemView.setLayoutParams(layoutParams);

        // 使用 getTag() 檢查是否已經有動態添加的 TextView
        TextView itemNumberTextView = (TextView) holder.itemView.getTag();

        if (itemNumberTextView == null) {
            // 如果沒有，動態添加一個 TextView
            itemNumberTextView = new TextView(holder.itemView.getContext());
            itemNumberTextView.setTextSize(16);
            itemNumberTextView.setTextColor(holder.itemView.getContext().getResources().getColor(android.R.color.black));

            // 設置 TextView 的佈局參數，並設定 layout_marginStart 來將其向右移動
            ViewGroup.MarginLayoutParams numberLayoutParams = new ViewGroup.MarginLayoutParams(
                    100, ViewGroup.LayoutParams.WRAP_CONTENT);
            numberLayoutParams.setMargins(50, 0, 0, 0); // 設定左邊距 50px，這樣編號會向右移動
            itemNumberTextView.setLayoutParams(numberLayoutParams);

            // 將 TextView 添加到佈局
            ViewGroup parent = (ViewGroup) holder.LocateionName.getParent();
            parent.addView(itemNumberTextView, 0); // 添加到第一個位置（左邊）

            // 使用 setTag 來記住已經創建的 TextView
            holder.itemView.setTag(itemNumberTextView);
        }

        // 設置編號
        itemNumberTextView.setText(String.valueOf(position + 1));

        // 根據是否震動、鈴聲和通知來設置拖動圖標
        boolean vibrate = sharedViewModel.getVibrate(position);
        boolean ringtone = sharedViewModel.getRingtone(position);
        int notification = sharedViewModel.getNotification(position);

        if (!enableDrag) { // 當不允許拖動時，根據狀態改變圖標
            if (!vibrate || !ringtone || notification != 5) {
                holder.dragHandle.setImageResource(R.drawable.vibrate_red); // 顯示紅色圖標
            } else {
                holder.dragHandle.setImageResource(R.drawable.vibrate); // 顯示正常圖標
            }
        }
    }

    // 返回項目數量
    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    // 顯示設定的彈出窗口
    public void showSettingsPopupWindow() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.CustomAlertDialog);

        // 取得自定義彈窗的佈局
        LayoutInflater inflater = LayoutInflater.from(context);
        View settingsView = inflater.inflate(R.layout.popupwindow_settings, null);

        // 取得佈局中的元件
        Switch ringtoneSwitch = settingsView.findViewById(R.id.switchRingtone);
        Switch vibrationSwitch = settingsView.findViewById(R.id.switchVibration);
        RadioGroup notificationTimeGroup = settingsView.findViewById(R.id.radioGroupNotificationTime);
        Button cancelButton = settingsView.findViewById(R.id.PopupCancel);
        Button confirmButton = settingsView.findViewById(R.id.PopupConfirm);

        // 從 ViewModel 取得當前項目的設置
        boolean isRingtoneEnabled = sharedViewModel.getRingtone(selectedPosition);
        boolean isVibrationEnabled = sharedViewModel.getVibrate(selectedPosition);
        int notificationTime = sharedViewModel.getNotification(selectedPosition);

        // 初始化開關和通知時間選項
        ringtoneSwitch.setChecked(isRingtoneEnabled);
        vibrationSwitch.setChecked(isVibrationEnabled);
        setSelectedNotificationTime(notificationTime, notificationTimeGroup);
        builder.setView(settingsView);

        final AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT)); // 設置背景透明
        dialog.show();

        // 取消按鈕的點擊事件
        cancelButton.setOnClickListener(v -> dialog.dismiss());

        // 確認按鈕的點擊事件
        confirmButton.setOnClickListener(v -> {
            boolean newRingtoneEnabled = ringtoneSwitch.isChecked();    // 取得新設置的鈴聲狀態
            boolean newVibrationEnabled = vibrationSwitch.isChecked();  // 取得新設置的震動狀態
            int newNotificationTime = getSelectedNotificationTime(notificationTimeGroup); // 取得新設置的通知時間

            // 將新的設置保存到 ViewModel
            sharedViewModel.setVibrate(newVibrationEnabled, selectedPosition);
            sharedViewModel.setRingtone(newRingtoneEnabled, selectedPosition);
            sharedViewModel.setNotification(newNotificationTime, selectedPosition);

            // 刷新 RecyclerView
            notifyDataSetChanged();

            String destinationName = arrayList.get(selectedPosition).get("data"); // 取得選定的目的地名稱

            // 顯示已保存的提示
            Toast.makeText(context, "已存取設定", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
            Log.d(TAG, "Settings Updated: Ringtone = " + newRingtoneEnabled +
                    ", Vibration = " + newVibrationEnabled +
                    ", Time = " + newNotificationTime +
                    ", Destination = " + destinationName);
        });
    }

    // 根據通知時間設置選中狀態
    private void setSelectedNotificationTime(int notificationTime, RadioGroup notificationTimeGroup) {
        if (notificationTime == 1) {
            notificationTimeGroup.check(R.id.radioOneMinute);
        } else if (notificationTime == 3) {
            notificationTimeGroup.check(R.id.radioThreeMinutes);
        } else {
            notificationTimeGroup.check(R.id.radioFiveMinutes);
        }
    }

    // 取得選中的通知時間
    private int getSelectedNotificationTime(RadioGroup notificationTimeGroup) {
        int selectedId = notificationTimeGroup.getCheckedRadioButtonId();
        if (selectedId == R.id.radioOneMinute) {
            return 1;
        } else if (selectedId == R.id.radioThreeMinutes) {
            return 3;
        } else if (selectedId == R.id.radioFiveMinutes) {
            return 5;
        }
        return 5; // 默認值為 5 分鐘
    }
}
