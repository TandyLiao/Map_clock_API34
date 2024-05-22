package com.example.map_clock_api34.home;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.map_clock_api34.MainActivity;
import com.example.map_clock_api34.R;
import com.example.map_clock_api34.SharedViewModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;

public class CreateLocation extends Fragment {
    private View v;
    RecyclerView recyclerView;
    ListAdapter listAdapter;
    SharedViewModel sharedViewModel;

    //新增一個HashMap存放每筆資料
    ArrayList<HashMap<String, String>> arrayList = new ArrayList<>();
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        v = inflater.inflate(R.layout.fragment_creatlocation, container, false);

        ActionBar actionBar = ((AppCompatActivity) requireActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);


        ImageView huButton = v.findViewById(R.id.huButton);
        huButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DrawerLayout drawerLayout = getActivity().findViewById(R.id.drawerLayout);
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });
        ImageView test = v.findViewById(R.id.imageView_weather);
        test.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    Toast.makeText(getActivity(), arrayList.toString(), Toast.LENGTH_SHORT).show();

            }
        });


        Button btnA = v.findViewById(R.id.btn_addItem);
        btnA.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //確定有無開定位權限，沒有就請求，如果用戶拒絕，只能糗她手動開啟權限，不然不能運行程地圖
                if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {

                    if(sharedViewModel.getI()<6){

                        MapsFragment mapFragment = new MapsFragment();
                        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                        transaction.replace(R.id.fragment_container, mapFragment);
                        transaction.addToBackStack(null);
                        transaction.commit();

                    }
                }else{
                    requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                            LOCATION_PERMISSION_REQUEST_CODE);

                    Toast.makeText(getActivity(), "請開啟定位權限", Toast.LENGTH_SHORT).show();
                }


            }
        });

        Button btnD = v.findViewById(R.id.btn_dropItem);
        btnD.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(sharedViewModel.getI()>=0){

                    arrayList.remove(sharedViewModel.getI());
                    sharedViewModel.setI();
                    recyclerView.setAdapter(listAdapter);
                }

            }
        });

        Button btnReset = v.findViewById(R.id.btn_reset);
        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initPopWindow(v,sharedViewModel);
            }
        });


        Button btnmapping = v.findViewById(R.id.btn_sure);
        btnmapping.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (sharedViewModel.getI() >=0) {
                    mapping mapping = new mapping();
                    FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                    transaction.replace(R.id.fragment_container, mapping);
                    transaction.addToBackStack(null);
                    transaction.commit();
                }else{
                    Toast.makeText(getActivity(), "你還沒有選擇地點",Toast.LENGTH_SHORT).show();
                }
            }
        });

        //設置RecycleView
        recyclerView = v.findViewById(R.id.recycleView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));
        listAdapter = new ListAdapter();
        recyclerView.setAdapter(listAdapter);

        recyclerViewAction();


        return v;

    }

    //ListAdapter的class
    private class ListAdapter extends RecyclerView.Adapter<ListAdapter.ViewHolder> {

        class ViewHolder extends RecyclerView.ViewHolder {
            private TextView tx1,tx2;
            public ViewHolder(View itemView) {
                super(itemView);
                tx2 = itemView.findViewById(R.id.textVLocateionName);
            }
        }
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.recycleviewitem, parent, false);
            return new ViewHolder(view);
        }

        //從HashMap中抓取資料並將其印出
        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            holder.tx2.setText(arrayList.get(position).get("data"));
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
    private void recyclerViewAction(){
            ItemTouchHelper helper = new ItemTouchHelper(new ItemTouchHelper.Callback() {
            @Override
            public int getMovementFlags(RecyclerView recyclerView,RecyclerView.ViewHolder viewHolder) {
                return makeMovementFlags(ItemTouchHelper.UP | ItemTouchHelper.DOWN , ItemTouchHelper.LEFT );
                //這裡是告訴RecyclerView你想開啟哪些操作
            }


            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                int position_dragged = viewHolder.getAdapterPosition();
                int position_target = target.getAdapterPosition();
                Collections.swap(arrayList, position_dragged, position_target);
                listAdapter.notifyItemMoved(position_dragged, position_target);

                sharedViewModel.swap(position_dragged,position_target);
                return true;//管理上下拖曳
            }


            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {

                int position = viewHolder.getAdapterPosition();
                switch (direction) {
                    case ItemTouchHelper.LEFT:
                    case ItemTouchHelper.RIGHT:
                        arrayList.remove(position);
                        sharedViewModel.delet(position);
                        listAdapter.notifyItemRemoved(position);
                        break;
                //管理滑動情形
                }
            }
                @Override
                public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                    new RecyclerViewSwipeDecorator.Builder(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                            .addBackgroundColor(ContextCompat.getColor(getActivity(), android.R.color.holo_red_dark))
                            .addActionIcon(R.drawable.baseline_delete_24)
                            .create()
                            .decorate();
                    super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
                }


            });


            helper.attachToRecyclerView(recyclerView);
    }

    @Override
    public void onResume() {
        super.onResume();
        resetData();
    }
    private void resetData() {
        arrayList.clear();
        if (sharedViewModel.getI() != -1) {
            for (int j = 0; j <= sharedViewModel.getI(); j++) {
                HashMap<String, String> hashMap = new HashMap<>();
                hashMap.put("data", sharedViewModel.getDestinationName(j));
                arrayList.add(hashMap);
            }
        }
        listAdapter.notifyDataSetChanged();
    }


    private void initPopWindow(View v,SharedViewModel sharedViewModel){

        View view = LayoutInflater.from(getActivity()).inflate(R.layout.popupwindow, null, false);

        PopupWindow popupWindow = new PopupWindow(view, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);

        popupWindow.setWidth(700);

        popupWindow.setFocusable(true);
        popupWindow.setOutsideTouchable(false);
        popupWindow.setTouchable(true);

        popupWindow.showAtLocation(v, Gravity.CENTER,0,0);

        Button BTNPopup = (Button) view.findViewById(R.id.PopupCancel);
        BTNPopup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
            }
        });

        Button btnsure = (Button) view.findViewById(R.id.Popupsure);
        btnsure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                while(sharedViewModel.getI()>=0){
                    arrayList.remove(sharedViewModel.getI());
                    sharedViewModel.setI();
                }
                recyclerView.setAdapter(listAdapter);
                popupWindow.dismiss();
            }
        });
    }
}
