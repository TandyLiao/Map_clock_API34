package com.example.map_clock_api34.home.ListAdapter;

import android.content.Context;
import android.graphics.Canvas;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.example.map_clock_api34.R;
import com.example.map_clock_api34.SharedViewModel;
import com.example.map_clock_api34.home.CreateLocation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;

public class RecyclerViewAction {

    private ItemTouchHelper itemTouchHelper;

    public void attachToRecyclerView(RecyclerView recyclerView, ArrayList<HashMap<String, String>> arrayList, ListAdapterRoute listAdapterRoute, SharedViewModel sharedViewModel, Context context, Button btnReset) {
        ItemTouchHelper.Callback callback = new ItemTouchHelper.Callback() {
            @Override
            public boolean isLongPressDragEnabled() {
                return false;
            }

            @Override
            public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                return makeMovementFlags(ItemTouchHelper.UP | ItemTouchHelper.DOWN, ItemTouchHelper.LEFT);
            }

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                int position_dragged = viewHolder.getAdapterPosition();
                int position_target = target.getAdapterPosition();
                Collections.swap(arrayList, position_dragged, position_target);
                listAdapterRoute.notifyItemMoved(position_dragged, position_target);
                sharedViewModel.swap(position_dragged, position_target);
                return true;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                arrayList.remove(position);
                sharedViewModel.delet(position);
                listAdapterRoute.notifyItemRemoved(position);

                updateResetButtonState(sharedViewModel, context, btnReset);
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                new RecyclerViewSwipeDecorator.Builder(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                        .addBackgroundColor(ContextCompat.getColor(context, android.R.color.holo_red_dark))
                        .addActionIcon(R.drawable.baseline_delete_24)
                        .create()
                        .decorate();
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        };
        itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
        listAdapterRoute.setItemTouchHelper(itemTouchHelper);

    }
    private void updateResetButtonState(SharedViewModel sharedViewModel, Context context, Button btnReset) {

        if (sharedViewModel.getI() >= 0) {
            //設置可點擊狀態
            btnReset.setEnabled(true);
            //改變按鈕文字顏色
            btnReset.setTextColor(ContextCompat.getColor(context, R.color.darkgreen));
            //改變按鈕的Drawable
            btnReset.setBackground(ContextCompat.getDrawable(context, R.drawable.btn_additem)); // 設定啟用時的背景顏色
        } else {
            //設置不可點擊狀態
            btnReset.setEnabled(false);
            //改變按鈕文字顏色
            btnReset.setTextColor(ContextCompat.getColor(context, R.color.lightgreen));
            //改變按鈕的Drawable
            btnReset.setBackground(ContextCompat.getDrawable(context, R.drawable.btn_unclickable)); // 設定禁用時的背景顏色
        }
    }
}
