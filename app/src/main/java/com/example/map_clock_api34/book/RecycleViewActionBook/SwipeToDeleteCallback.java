package com.example.map_clock_api34.book.RecycleViewActionBook;

import android.content.Context;
import android.graphics.Canvas;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.example.map_clock_api34.R;

import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;

public class SwipeToDeleteCallback extends ItemTouchHelper.SimpleCallback {


    private Context context;
    private OnSwipedListener listener;

    // 初始化並設置滑動方向為左(ItemTouchHelper.LEFT)
    public SwipeToDeleteCallback(Context context, OnSwipedListener listener) {
        super(0, ItemTouchHelper.LEFT);  // 第一個參數是拖動方向，設為 0 表示不支持拖動，第二個參數是滑動方向
        this.context = context;
        this.listener = listener;
    }

    // 處理拖動事件，但這裡不支持拖動，因此直接返回 false
    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
        return false;
    }

    // 在項目被滑動時觸發，通知 listener 處理滑動後的操作
    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        listener.onSwiped(viewHolder.getAdapterPosition());  // 調用 listener，傳遞被滑動的項目位置
    }

    //負責繪製滑動時的背景及圖標
    @Override
    public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        // 使用 RecyclerViewSwipeDecorator 建造器來設定滑動背景及圖標
        new RecyclerViewSwipeDecorator.Builder(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                .addBackgroundColor(ContextCompat.getColor(context, android.R.color.holo_red_dark)) // 設定滑動時的背景顏色，這裡是紅色
                .addActionIcon(R.drawable.baseline_delete_24)  // 設定滑動時顯示的圖標，這裡是刪除圖標
                .create()
                .decorate();

        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
    }

    // 定義一個接口，用來處理滑動完成後的事件
    public interface OnSwipedListener {
        void onSwiped(int position);  // 當被滑動時，傳遞該項目的位置
    }
}
