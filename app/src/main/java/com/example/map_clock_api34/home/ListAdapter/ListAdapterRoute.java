package com.example.map_clock_api34.home.ListAdapter;


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


public class ListAdapterRoute extends RecyclerView.Adapter<ListAdapterRoute.ViewHolder> {

    private ArrayList<HashMap<String, String>> arrayList;
    private SharedViewModel sharedViewModel;
    private ItemTouchHelper itemTouchHelper;

    public ListAdapterRoute(ArrayList<HashMap<String, String>> arrayList, SharedViewModel sharedViewModel) {
        this.arrayList = arrayList;
        this.sharedViewModel = sharedViewModel;
    }

    public void setItemTouchHelper(ItemTouchHelper itemTouchHelper) {
        this.itemTouchHelper = itemTouchHelper;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        //地名
        private TextView LocateionName;
        //拖曳的圖標
        private ImageView dragHandle;

        @SuppressLint("ClickableViewAccessibility")
        public ViewHolder(View itemView) {
            super(itemView);
            LocateionName = itemView.findViewById(R.id.textVLocateionName);
            dragHandle = itemView.findViewById(R.id.dragHandle);

            //讓拖曳的圖標可以動作
            dragHandle.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        // 當觸摸ImageView時開始拖曳項目
                        if (itemTouchHelper != null) {
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

    //從HashMap中抓取資料並將其印出
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.LocateionName.setText(arrayList.get(position).get("data"));
        ViewGroup.LayoutParams layoutParams = holder.itemView.getLayoutParams();
        layoutParams.height = 150;
        holder.itemView.setLayoutParams(layoutParams);
    }

    //回傳arrayList的大小
    @Override
    public int getItemCount() {
        return arrayList.size();
    }
}
