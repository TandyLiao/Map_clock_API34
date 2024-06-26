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

    public ListAdapterHistory(ArrayList<HashMap<String, String>> arrayList) {
        this.arrayList = arrayList;
    }

    public void setEditMode(boolean isEditMode) {
        this.isEditMode = isEditMode;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycleviewitem_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        HashMap<String, String> item = arrayList.get(position);
        holder.RouteName.setText(item.get("placeName"));
        holder.Latitude.setText(item.get("latitude"));
        holder.Longitude.setText(item.get("longitude"));

        // 設置背景顏色
        if (item.getOrDefault("isSelected", "false").equals("true")) {
            holder.itemView.setBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.selected_item_background));
        } else {
            holder.itemView.setBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.darkyellow));
        }

        holder.itemView.setOnClickListener(v -> {
            if (isEditMode) {
                boolean isSelected = item.getOrDefault("isSelected", "false").equals("true");
                item.put("isSelected", isSelected ? "false" : "true");
                notifyItemChanged(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return arrayList.size(); // 返回資料集合的大小
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView RouteName;
        TextView Latitude;
        TextView Longitude;

        ViewHolder(View itemView) {
            super(itemView);
            RouteName = itemView.findViewById(R.id.textRouteName);
            Latitude = itemView.findViewById(R.id.textLantitude);
            Longitude = itemView.findViewById(R.id.textLongtitude);
        }
    }
}
