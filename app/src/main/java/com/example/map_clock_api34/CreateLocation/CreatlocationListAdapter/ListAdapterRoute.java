package com.example.map_clock_api34.CreateLocation.CreatlocationListAdapter;

import android.annotation.SuppressLint;
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

/**
 * 自訂的 RecyclerView 適配器，負責顯示路線資訊，並支援項目點擊和拖曳功能。
 */
public class ListAdapterRoute extends RecyclerView.Adapter<ListAdapterRoute.ViewHolder> {

    private ArrayList<HashMap<String, String>> arrayList; // 儲存路線資料的列表
    private SharedViewModel sharedViewModel; // 與 ViewModel 共享的資料
    private ItemTouchHelper itemTouchHelper; // 負責處理拖曳功能
    private OnItemClickListener onItemClickListener; // 項目點擊的監聽器

    private int selectedPosition = RecyclerView.NO_POSITION; // 用來跟踪目前選擇的項目
    private boolean enableDrag; // 用來控制是否允許拖動

    //項目點擊的回調介面
    public interface OnItemClickListener {
        void onItemClick(int position); // 點擊時調用
    }

    // 設置項目點擊監聽器
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    public ListAdapterRoute(ArrayList<HashMap<String, String>> arrayList, SharedViewModel sharedViewModel, boolean enableDrag) {
        this.arrayList = arrayList;
        this.sharedViewModel = sharedViewModel;
        this.enableDrag = enableDrag;
    }

    // 設置 ItemTouchHelper，用於處理拖動操作
    public void setItemTouchHelper(ItemTouchHelper itemTouchHelper) {
        this.itemTouchHelper = itemTouchHelper;
    }

    // 自訂的 ViewHolder 類別，負責顯示每個路線項目的資料
    class ViewHolder extends RecyclerView.ViewHolder {
        private TextView LocateionName; // 顯示路線名稱的 TextView
        private ImageView dragHandle;   // 拖曳圖標

        @SuppressLint("ClickableViewAccessibility")
        public ViewHolder(View itemView) {
            super(itemView);
            LocateionName = itemView.findViewById(R.id.textVLocateionName);
            dragHandle = itemView.findViewById(R.id.dragHandle);

            // 處理拖曳圖標的點擊事件，進行選擇操作或拖動
            dragHandle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!enableDrag) { // 如果拖曳禁用，進行選擇操作
                        int previousSelectedPosition = selectedPosition;
                        if (selectedPosition == getAdapterPosition()) {
                            selectedPosition = RecyclerView.NO_POSITION; // 取消選擇
                        } else {
                            selectedPosition = getAdapterPosition(); // 設置新選擇項目
                        }
                        notifyItemChanged(previousSelectedPosition); // 通知 UI 更新舊選擇項目
                        notifyItemChanged(selectedPosition); // 通知 UI 更新新選擇項目
                    }
                    if (onItemClickListener != null) {
                        onItemClickListener.onItemClick(getAdapterPosition()); // 回調點擊事件
                    }
                }

            });

            // 處理拖曳事件
            dragHandle.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_DOWN && itemTouchHelper != null && enableDrag) {
                        itemTouchHelper.startDrag(ViewHolder.this); // 開始拖曳
                    }
                    return false;
                }
            });
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycleviewitem_route, parent, false);
        return new ViewHolder(view); // 創建並返回自訂的 ViewHolder
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.LocateionName.setText(arrayList.get(position).get("data")); // 設置路線名稱
        ViewGroup.LayoutParams layoutParams = holder.itemView.getLayoutParams();
        layoutParams.height = 150; // 設置每個項目的高度
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

        // 根據拖動狀態和選擇狀態設定圖標
        if (!enableDrag) {
            if (position == selectedPosition) {
                holder.dragHandle.setImageResource(R.drawable.route); // 選擇後的圖標
            } else {
                String note = sharedViewModel.getNote(position);
                if (note != null && !note.isEmpty()) {
                    holder.dragHandle.setImageResource(R.drawable.note_red); // 有備註的圖標
                } else {
                    holder.dragHandle.setImageResource(R.drawable.note); // 沒有備註的圖標
                }
            }
        } else {
            holder.dragHandle.setImageResource(R.drawable.equals_sign); // 拖動時顯示的圖標
        }
    }

    @Override
    public int getItemCount() {
        return arrayList.size(); // 返回項目的總數
    }
}
