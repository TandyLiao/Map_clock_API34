package com.example.map_clock_api34.history.ListAdapter;

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

    private final ArrayList<HashMap<String, String>> arrayList;
    private boolean isEditMode = false;
    private boolean isMultiSelect = false;
    private OnItemSelectedListener onItemSelectedListener;

    //檢測是否有選擇RecycleView的監聽器
    public interface OnItemSelectedListener {
        void onItemSelected();
    }

    public void setOnItemSelectedListener(OnItemSelectedListener listener) {
        this.onItemSelectedListener = listener;
    }

    public ListAdapterHistory(ArrayList<HashMap<String, String>> arrayList) {
        this.arrayList = arrayList;
    }

    //設定選擇模式
    public void setEditMode(boolean isEditMode, boolean isMultiSelect) {
        this.isEditMode = isEditMode;
        this.isMultiSelect = isMultiSelect;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycleviewitem_history, parent, false);
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.height = 400;
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        HashMap<String, String> item = arrayList.get(position);
        holder.routeName.setText(item.get("placeName"));
        holder.routeName2.setText(item.get("placeName2"));
        holder.routeName3.setText(item.get("placeName3"));
        holder.time.setText(item.get("time"));

        // 設置背景顏色
        if (item.getOrDefault("isSelected", "false").equals("true")) {
            holder.itemView.setBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.selected_item_background));
        } else {
            holder.itemView.setBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.darkyellow));
        }

        //是否有選擇東西
        holder.itemView.setOnClickListener(v -> {
            if (isEditMode || !isMultiSelect) {
                boolean isSelected = item.getOrDefault("isSelected", "false").equals("true");
                if (isMultiSelect) {
                    item.put("isSelected", isSelected ? "false" : "true");
                } else {
                    clearSelections();
                    item.put("isSelected", isSelected ? "false" : "true");
                }
                notifyDataSetChanged();
                if (onItemSelectedListener != null) {
                    onItemSelectedListener.onItemSelected();
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return arrayList.size(); // 返回數據集合的大小
    }

    public void clearSelections() {
        for (HashMap<String, String> item : arrayList) {
            item.put("isSelected", "false");
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView routeName, routeName2, routeName3;
        TextView time;

        ViewHolder(View itemView) {
            super(itemView);
            routeName = itemView.findViewById(R.id.textRouteName);
            routeName2 = itemView.findViewById(R.id.textRouteName2);
            routeName3 = itemView.findViewById(R.id.textRouteName3);
            time = itemView.findViewById(R.id.textTime);
        }
    }
}
