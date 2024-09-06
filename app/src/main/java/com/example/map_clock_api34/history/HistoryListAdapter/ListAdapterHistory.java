package com.example.map_clock_api34.history.HistoryListAdapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.map_clock_api34.R;

import java.util.ArrayList;
import java.util.HashMap;

public class ListAdapterHistory extends RecyclerView.Adapter<ListAdapterHistory.ViewHolder> {

    // 用來存放列表數據的 ArrayList，每個項目都是一個 HashMap，包含多個字串資料
    private final ArrayList<HashMap<String, String>> arrayList;
    private boolean isEditMode = false; // 是否處於編輯模式
    private boolean isMultiSelect = false; // 是否支持多選模式
    private OnItemSelectedListener onItemSelectedListener; // 項目選擇監聽器
    private int selectedPosition = RecyclerView.NO_POSITION; // 用來存儲當前選擇的位置

    // 介面：當項目被選擇時的回調
    public interface OnItemSelectedListener {
        void onItemSelected(); // 當選擇發生時觸發
    }

    // 設置選擇監聽器
    public void setOnItemSelectedListener(OnItemSelectedListener listener) {
        this.onItemSelectedListener = listener;
    }

    public ListAdapterHistory(ArrayList<HashMap<String, String>> arrayList) {
        this.arrayList = arrayList;
    }

    // 設定是否處於編輯模式與是否支持多選
    public void setEditMode(boolean isEditMode, boolean isMultiSelect) {
        this.isEditMode = isEditMode;
        this.isMultiSelect = isMultiSelect;
        notifyDataSetChanged(); // 更新列表視圖
    }

    // 建立 ViewHolder，將列表項目的視圖佈局 inflate 出來
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycleviewitem_history, parent, false);
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.height = 400; // 設定列表項目的高度
        return new ViewHolder(view);
    }

    // 綁定資料到 ViewHolder，設定每個列表項目的顯示內容
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        HashMap<String, String> item = arrayList.get(position); // 取得當前位置的資料項
        holder.routeName.setText(item.get("placeName")); // 顯示地點名稱
        holder.routeName2.setText(item.get("placeName2")); // 顯示第二個地點名稱
        holder.routeName3.setText(item.get("placeName3")); // 顯示第三個地點名稱

        String time = item.get("time"); // 取得時間資料
        // 分割日期和時間
        if (time != null && time.contains(" ")) {
            String[] timeParts = time.split(" ");
            if (timeParts.length == 2) {
                // 將日期和時間分行顯示
                holder.time.setText(timeParts[0] + "\n" + timeParts[1]);
            } else {
                holder.time.setText(time); // 若格式不正確，顯示原始時間字串
            }
        } else {
            holder.time.setText(time); // 若不包含空格，直接顯示
        }

        // 根據選擇狀態設定背景顏色
        if (item.getOrDefault("isSelected", "false").equals("true")) {
            // 選中的背景顏色
            holder.itemView.setBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.selected_item_background));
        } else {
            // 未選中的背景顏色
            holder.itemView.setBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.darkyellow));
        }

        // 處理項目點擊事件
        holder.itemView.setOnClickListener(v -> {
            if (isEditMode || !isMultiSelect) { // 如果是編輯模式或非多選模式
                boolean isSelected = item.getOrDefault("isSelected", "false").equals("true"); // 檢查選擇狀態
                if (isMultiSelect) {
                    // 多選模式下切換選擇狀態
                    item.put("isSelected", isSelected ? "false" : "true");
                } else {
                    // 單選模式下清除其他選擇
                    clearSelections();
                    item.put("isSelected", isSelected ? "false" : "true");
                    selectedPosition = isSelected ? RecyclerView.NO_POSITION : position; // 更新選擇位置
                }
                notifyDataSetChanged(); // 更新視圖
                if (onItemSelectedListener != null) {
                    onItemSelectedListener.onItemSelected(); // 觸發選擇回調
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return arrayList.size(); // 返回資料集大小
    }

    // 清除所有項目的選擇狀態
    public void clearSelections() {
        for (HashMap<String, String> item : arrayList) {
            item.put("isSelected", "false");
        }
        selectedPosition = RecyclerView.NO_POSITION; // 重置選擇位置
    }

    // 取得當前選擇的項目
    public HashMap<String, String> getSelectedItem() {
        if (selectedPosition != RecyclerView.NO_POSITION) {
            return arrayList.get(selectedPosition);
        }
        return null; // 沒有選擇項目時返回 null
    }

    // 自訂 ViewHolder，持有每個列表項目的子視圖
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView routeName, routeName2, routeName3; // 三個地點名稱的 TextView
        TextView time; // 顯示時間的 TextView

        // ViewHolder 建構子，初始化子視圖
        ViewHolder(View itemView) {
            super(itemView);
            routeName = itemView.findViewById(R.id.textRouteName);
            routeName2 = itemView.findViewById(R.id.textRouteName2);
            routeName3 = itemView.findViewById(R.id.textRouteName3);
            time = itemView.findViewById(R.id.textTime);
        }
    }
}
