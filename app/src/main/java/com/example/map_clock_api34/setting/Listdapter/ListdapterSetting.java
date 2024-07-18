package com.example.map_clock_api34.setting.Listdapter;

import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.example.map_clock_api34.R;
import com.example.map_clock_api34.SharedViewModel;

import java.util.ArrayList;
import java.util.HashMap;

public class ListdapterSetting extends RecyclerView.Adapter<ListdapterSetting.ViewHolder> {

    private ArrayList<HashMap<String, String>> arrayList;
    private SharedViewModel sharedViewModel;
    private ItemTouchHelper itemTouchHelper;
    private int selectedPosition = RecyclerView.NO_POSITION; // 用於跟踪選擇的項目
    private boolean enableDrag; // 是否啟用拖動功能
    private OnItemClickListener onItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public ListdapterSetting(ArrayList<HashMap<String, String>> arrayList, SharedViewModel sharedViewModel, boolean enableDrag) {
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

    // 更新 ViewHolder，使其接收外部类的字段
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
                    if (onItemClickListener != null) {
                        onItemClickListener.onItemClick(getAdapterPosition());
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

    @NonNull
    @Override
    public ListdapterSetting.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycleviewitem_route, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // 设置列表项的文本内容
        holder.LocateionName.setText(arrayList.get(position).get("data"));

        // 设置列表项的高度
        ViewGroup.LayoutParams layoutParams = holder.itemView.getLayoutParams();
        layoutParams.height = 150;
        holder.itemView.setLayoutParams(layoutParams);

        if (!enableDrag) {
            // 根据选择状态设置图标
            if (position == selectedPosition) {
                holder.dragHandle.setImageResource(R.drawable.anya062516); // 选择后的图标
            } else {
                holder.dragHandle.setImageResource(R.drawable.vibrate); // 未选择的图标
            }
        }
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }
}
