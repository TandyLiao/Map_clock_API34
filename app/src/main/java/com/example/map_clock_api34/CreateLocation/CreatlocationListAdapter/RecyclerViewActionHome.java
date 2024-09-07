package com.example.map_clock_api34.CreateLocation.CreatlocationListAdapter;

// 引入所需的 Android 和第三方庫
import android.content.Context;
import android.graphics.Canvas;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.example.map_clock_api34.R;
import com.example.map_clock_api34.SharedViewModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;

// RecyclerViewActionHome 用來處理 RecyclerView 中的拖動與滑動手勢
public class RecyclerViewActionHome {

    // 定義 ItemTouchHelper 變數，用於處理拖動和滑動
    private ItemTouchHelper itemTouchHelper;

    // 將觸控事件綁定到 RecyclerView，並處理拖動與滑動的邏輯
    public void attachToRecyclerView(RecyclerView recyclerView, ArrayList<HashMap<String, String>> arrayList, ListAdapterRoute listAdapterRoute, SharedViewModel sharedViewModel, Context context, Button btnReset) {

        // 自定義 ItemTouchHelper 的 Callback，用來定義拖動與滑動的行為
        ItemTouchHelper.Callback callback = new ItemTouchHelper.Callback() {

            // 禁用長按拖動
            @Override
            public boolean isLongPressDragEnabled() {
                return false;
            }

            // 設定拖動與滑動的方向
            @Override
            public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                // 允許向上和向下拖動，以及向左滑動
                return makeMovementFlags(ItemTouchHelper.UP | ItemTouchHelper.DOWN, ItemTouchHelper.LEFT);
            }

            // 當項目被拖動時，交換列表中的數據並更新畫面
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                int position_dragged = viewHolder.getAdapterPosition();  // 被拖動項目的位置
                int position_target = target.getAdapterPosition();  // 目標位置
                // 交換 arrayList 中的數據
                Collections.swap(arrayList, position_dragged, position_target);
                // 通知 Adapter 項目位置已變更
                listAdapterRoute.notifyItemMoved(position_dragged, position_target);
                // 通知 ViewModel 位置交換
                sharedViewModel.swap(position_dragged, position_target);
                return true;
            }

            // 當項目被滑動時，刪除該項目並更新畫面
            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();  // 取得滑動的項目位置
                arrayList.remove(position);         // 從 arrayList 中移除該項目

                sharedViewModel.delet(position);    // 通知 ViewModel 刪除該項目

                listAdapterRoute.notifyItemRemoved(position);  // 通知 Adapter 項目已被移除

                listAdapterRoute.notifyItemRangeChanged(position, arrayList.size()); // 通知 Adapter 更新剩餘項目的編號，防止刪除後編號錯誤

                // 更新 Reset 按鈕的狀態
                updateResetButtonState(sharedViewModel, context, btnReset);

            }

            // 自定義項目滑動過程中的裝飾效果（例如滑動時的背景顏色和圖標）
            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                // 使用 RecyclerViewSwipeDecorator 來設置滑動裝飾效果
                new RecyclerViewSwipeDecorator.Builder(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                        .addBackgroundColor(ContextCompat.getColor(context, android.R.color.holo_red_dark))  // 設定背景顏色
                        .addActionIcon(R.drawable.baseline_delete_24)  // 設定刪除圖標
                        .create()
                        .decorate();  // 套用裝飾
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }

            // 在拖動結束後，刷新整個 RecyclerView 以確保編號更新
            @Override
            public void clearView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                super.clearView(recyclerView, viewHolder);

                // 拖動結束後，通知 Adapter 刷新所有項目以更新編號
                listAdapterRoute.notifyDataSetChanged();
            }
        };

        // 將 Callback 綁定到 ItemTouchHelper，並附加到 RecyclerView
        itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(recyclerView);

        // 將 ItemTouchHelper 傳遞給 listAdapterRoute
        listAdapterRoute.setItemTouchHelper(itemTouchHelper);
    }

    // 更新 Reset 按鈕的狀態，根據是否有選擇地點來改變按鈕的狀態和外觀
    private void updateResetButtonState(SharedViewModel sharedViewModel, Context context, Button btnReset) {
        if (sharedViewModel.getLocationCount() >= 0) {
            // 如果有選擇地點，設定按鈕為可點擊狀態
            btnReset.setEnabled(true);
            // 改變按鈕文字顏色
            btnReset.setTextColor(ContextCompat.getColor(context, R.color.darkgreen));
            // 設定啟用時的按鈕背景
            btnReset.setBackground(ContextCompat.getDrawable(context, R.drawable.btn_additem));
        } else {
            // 如果沒有選擇地點，設定按鈕為不可點擊狀態
            btnReset.setEnabled(false);
            // 改變按鈕文字顏色
            btnReset.setTextColor(ContextCompat.getColor(context, R.color.lightgreen));
            // 設定禁用時的按鈕背景
            btnReset.setBackground(ContextCompat.getDrawable(context, R.drawable.btn_unclickable));
        }
    }
}
