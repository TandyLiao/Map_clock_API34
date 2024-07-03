package com.example.map_clock_api34.home.ListAdapter;


import android.annotation.SuppressLint;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.example.map_clock_api34.R;
import com.example.map_clock_api34.SharedViewModel;

import java.util.ArrayList;

import java.util.HashMap;


public class ListAdapterRoute extends RecyclerView.Adapter<ListAdapterRoute.ViewHolder> {

    private ArrayList<HashMap<String, String>> arrayList;
    private SharedViewModel sharedViewModel;
    private ItemTouchHelper itemTouchHelper;
    private int selectedPosition = RecyclerView.NO_POSITION; // 用於跟踪選擇的項目
    private boolean enableDrag; // 是否啟用拖動功能

    public ListAdapterRoute(ArrayList<HashMap<String, String>> arrayList, SharedViewModel sharedViewModel, boolean enableDrag) {
        this.arrayList = arrayList;
        this.sharedViewModel = sharedViewModel;
        this.enableDrag = enableDrag;
    }

    public void setItemTouchHelper(ItemTouchHelper itemTouchHelper) {
        this.itemTouchHelper = itemTouchHelper;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private TextView LocateionName;
        private ImageView dragHandle;

        public ViewHolder(View itemView) {
            super(itemView);
            LocateionName = itemView.findViewById(R.id.textVLocateionName);
            dragHandle = itemView.findViewById(R.id.dragHandle);

            // 處理圖片點擊事件
            dragHandle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!enableDrag) { // 如果禁用拖動，則進行選擇操作
                        int previousSelectedPosition = selectedPosition;
                        if (selectedPosition == getAdapterPosition()) {
                            selectedPosition = RecyclerView.NO_POSITION; // 取消選擇
                        } else {
                            selectedPosition = getAdapterPosition();
                        }
                        notifyItemChanged(previousSelectedPosition);
                        notifyItemChanged(selectedPosition);
                    }
                }
            });

            // 讓拖曳的圖標可以動作
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

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycleviewitem_route, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.LocateionName.setText(arrayList.get(position).get("data"));
        ViewGroup.LayoutParams layoutParams = holder.itemView.getLayoutParams();
        layoutParams.height = 150;
        holder.itemView.setLayoutParams(layoutParams);

        if (!enableDrag) {
            // 設置選擇狀態的背景顏色和圖標變化
            if (position == selectedPosition) {
                holder.dragHandle.setImageResource(R.drawable.route); // 選擇後的圖標
            } else {
                holder.dragHandle.setImageResource(R.drawable.note); // 未選擇的圖標
            }
        } else {
            holder.dragHandle.setImageResource(R.drawable.equals_sign); // 拖動時的圖標
        }

    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }
}


