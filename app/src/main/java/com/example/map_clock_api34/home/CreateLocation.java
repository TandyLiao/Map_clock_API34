package com.example.map_clock_api34.home;

import android.Manifest;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.map_clock_api34.R;
import com.example.map_clock_api34.SharedViewModel;
import com.example.map_clock_api34.Weather.WeatherService;
import com.example.map_clock_api34.note.Note;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;
public class CreateLocation extends Fragment {
    private View v;
    RecyclerView recyclerView;
    RecyclerView recyclerView2;
    ListAdapter listAdapter;

    ListAdapter listAdapter2;
    private ArrayList<ClipData.Item> itemList;
    SharedViewModel sharedViewModel;
    private ItemTouchHelper itemTouchHelper;
    //新增一個HashMap存放每筆資料
    ArrayList<HashMap<String, String>> arrayList = new ArrayList<>();
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private WeatherService weatherService = new WeatherService();
    String[] capital= new String[7];
    String[] weather = new String[7];
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        v = inflater.inflate(R.layout.fragment_creatlocation, container, false);

        recyclerView2 = v.findViewById(R.id.recycleView2);
        recyclerView2.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));

        ArrayList<HashMap<String, String>> arrayList2 = new ArrayList<>();
        HashMap<String, String> item1 = new HashMap<>();
        item1.put("data", "記事");
        arrayList2.add(item1);
        HashMap<String, String> item2 = new HashMap<>();
        item2.put("data", "天氣");
        arrayList2.add(item2);
        HashMap<String, String> item3 = new HashMap<>();
        item3.put("data", "震動");
        arrayList2.add(item3);
        HashMap<String, String> item4 = new HashMap<>();
        item4.put("data", "鈴聲");
        arrayList2.add(item4);


        ListAdapter2 listAdapter2 = new ListAdapter2(arrayList2);
        recyclerView2.setAdapter(listAdapter2);


        ActionBar actionBar = ((AppCompatActivity) requireActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
            actionBar.setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(getActivity(), R.color.lightgreen)));

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
    private void getWeatherAdvice() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    for (int x = 0; x <= sharedViewModel.getI(); x++) {
                        String weatherJson = weatherService.getWeather(sharedViewModel.getCapital(x));
                        List<String> advices = Collections.singletonList(WeatherService.getAdvice(weatherJson));
                        if (advices.size() > 0 && advices.get(0).contains("降雨概率")) {
                            capital[x] = sharedViewModel.getCapital(x) + advices.get(0);
                        } else {
                            capital[x] = null;
                        }
                    }
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                showRainyLocations();
                            }
                        });
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e("WeatherService", "IOException occurred: " + e.getMessage());
                    if (getActivity() != null && !getActivity().isFinishing()) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getActivity(), "無法獲取天氣資訊", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            }
        }).start();
    }
    private void showRainyLocations() {
        if (capital != null && capital.length > 0) {
            weatherPopWindow(v, sharedViewModel);
        } else {
            Toast.makeText(getActivity(), "目前沒有地區有機率降雨", Toast.LENGTH_SHORT).show();
        }
    }
    //ListAdapter的class
    private class ListAdapter extends RecyclerView.Adapter<ListAdapter.ViewHolder> {
        class ViewHolder extends RecyclerView.ViewHolder {
            private TextView tx2;
            private ImageView dragHandle;
            public ViewHolder(View itemView) {
                super(itemView);
                tx2 = itemView.findViewById(R.id.textVLocateionName);
                dragHandle = itemView.findViewById(R.id.dragHandle);
                dragHandle.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        if (event.getAction() == MotionEvent.ACTION_DOWN) {
                            // 當觸摸ImageView時開始拖曳項目
                            itemTouchHelper.startDrag(ViewHolder.this);
                        }
                        return false;
                    }
                });
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
                listAdapter.notifyItemMoved(position_dragged, position_target);
                sharedViewModel.swap(position_dragged, position_target);
                return true;
            }
            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                arrayList.remove(position);
                sharedViewModel.delet(position);
                listAdapter.notifyItemRemoved(position);
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
        };
        itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }
    @Override
    public void onResume() {
        super.onResume();
        CardView cardViewtitle = new CardView(requireContext());
        cardViewtitle.setLayoutParams(new CardView.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT,
                ActionBar.LayoutParams.MATCH_PARENT));

        Drawable drawable = ContextCompat.getDrawable(requireContext(), R.drawable.cardviewtitle_shape);
        cardViewtitle.setBackground(drawable);

        //建立LinearLayout在CardView等等放圖案和文字
        LinearLayout linearLayout = new LinearLayout(requireContext());
        linearLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        ));
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);

        //
        ImageView bookmark = new ImageView(requireContext());
        bookmark.setImageResource(R.drawable.anya062516);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                100, // 设置宽度为 100 像素
                100 // 设置高度为 100 像素
        );
        params.setMarginStart(10); // 设置左边距
        bookmark.setLayoutParams(params);

        // 創建TextView
        TextView bookTitle = new TextView(requireContext());
        bookTitle.setText("路線規劃");
        bookTitle.setTextSize(15);
        bookTitle.setTextColor(getResources().getColor(R.color.green)); // 更改文字颜色
        bookTitle.setPadding(10, 10, 10, 10); // 设置内边距

        linearLayout.addView(bookmark);
        linearLayout.addView(bookTitle);
        cardViewtitle.addView(linearLayout);

        // 將cardview新增到actionBar
        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false); // 隐藏原有的標題
            actionBar.setDisplayShowCustomEnabled(true);
            actionBar.setCustomView(cardViewtitle, new ActionBar.LayoutParams(
                    ActionBar.LayoutParams.WRAP_CONTENT,
                    ActionBar.LayoutParams.WRAP_CONTENT,
                    Gravity.END));
            actionBar.show();
        }
        resetData();
    }
    public void onPause() {
        super.onPause();
        ActionBar actionBar = ((AppCompatActivity) requireActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowCustomEnabled(false);
            actionBar.setCustomView(null);
        }
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

        popupWindow.setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        //View rootView = getActivity().getWindow().getDecorView().getRootView();
        //rootView.setAlpha(0.1f);有bug

        Button BTNPopup = (Button) view.findViewById(R.id.PopupCancel);
        BTNPopup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                popupWindow.dismiss();
                /*if (popupWindow != null && popupWindow.isShowing()) {
                    popupWindow.dismiss();
                    rootView.setAlpha(1.0f);
                }*/

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
                /*if (popupWindow != null && popupWindow.isShowing()) {
                    popupWindow.dismiss();
                    rootView.setAlpha(1.0f);
                }*/
            }
        });
    }
    private void weatherPopWindow(View v, SharedViewModel sharedViewModel) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.popupwindow_weather, null, false);
        PopupWindow popupWindow = new PopupWindow(view, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        popupWindow.setWidth(700);
        popupWindow.setFocusable(true);
        popupWindow.setOutsideTouchable(false);
        popupWindow.setTouchable(true);
        TextView weatherInfoTextView = view.findViewById(R.id.txtWeatherNote);
        new Thread(new Runnable() {
            @Override
            public void run() {
                StringBuilder weatherInfo = new StringBuilder();
                for (int x = 0; x <= sharedViewModel.getI(); x++) {
                    String capital = sharedViewModel.getCapital(x);
                    if (capital != null) {
                        try {
                            String weatherJson = weatherService.getWeather(capital);
                            List<String> advices = Collections.singletonList(WeatherService.getAdvice(weatherJson)); // 假設這裡可以直接取得多條天氣建議
                            for (String advice : advices) {
                                weatherInfo.append(capital).append("：").append(advice).append("\n");
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                            Log.e("WeatherService", "IOException occurred: " + e.getMessage());
                            weatherInfo.append(capital).append("：").append("無法獲取天氣資訊\n");
                        }
                    }
                }
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // 將天氣資訊顯示在 TextView 中
                        weatherInfoTextView.setText(weatherInfo.toString());
                        // 設置取消按鈕的點擊事件
                        Button btnCancel = view.findViewById(R.id.PopupYes);
                        btnCancel.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                popupWindow.dismiss();
                            }
                        });
                        // 顯示彈出視窗
                        popupWindow.showAtLocation(v, Gravity.CENTER, 0, 0);
                    }
                });
            }
        }).start();
    }
    public class ListAdapter2 extends RecyclerView.Adapter<ListAdapter2.ViewHolder> {
        private ArrayList<HashMap<String, String>> arrayList;

        public ListAdapter2(ArrayList<HashMap<String, String>> arrayList) {
            this.arrayList = arrayList;
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            private TextView horecycleName;
            private ImageView horecycleimageView;

            public ViewHolder(View itemView) {
                super(itemView);
                horecycleName = itemView.findViewById(R.id.horecycle_Name);
                horecycleimageView = itemView.findViewById(R.id.horecycle_imageView);

                horecycleimageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int position = getAdapterPosition();
                        handleImageClick(v.getContext(), position);
                    }
                });

                horecycleName.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int position = getAdapterPosition();
                    }
                });
            }

            private void handleImageClick(Context context, int position) {
                if (position == 0) {
                    // 跳到記事Fragment
                    Note notesFragment = new Note();
                    FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                    transaction.replace(R.id.fragment_container, notesFragment);
                    transaction.addToBackStack(null);
                    transaction.commit();
                } else if (position == 1) {

                    getWeatherAdvice();
                }
                else if (position == 2) {

                    getWeatherAdvice();
                }
                else if (position == 3) {

                    getWeatherAdvice();
                }

                // 可以继续添加其他条目的点击处理逻辑
            }



        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.horizontal_recyclerview_item, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            // 获取对应位置的数据
            HashMap<String, String> item = arrayList.get(position);
            String data = item.get("data");
            holder.horecycleName.setText(data);

            // 根据数据内容设置不同的图标
            if (data.equals("記事")) {
                holder.horecycleimageView.setImageResource(R.drawable.bookmark);
            }
            else if (data.equals("天氣")) {
                holder.horecycleimageView.setImageResource(R.drawable.anya062516);
            }
            else if (data.equals("震動")) {
                holder.horecycleimageView.setImageResource(R.drawable.anya062516);
            }
            else if (data.equals("鈴聲")) {
                holder.horecycleimageView.setImageResource(R.drawable.anya062516);
            }
            // 可以继续添加其他条目的处理逻辑
        }

        @Override
        public int getItemCount() {
            return arrayList.size();
        }
    }


}
