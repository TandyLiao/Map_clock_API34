package com.example.map_clock_api34.note;

import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.map_clock_api34.R;
import com.example.map_clock_api34.SharedViewModel;
import com.example.map_clock_api34.history.ListAdapter.ListAdapterHistory;
import com.example.map_clock_api34.home.ListAdapter.ListAdapterRoute;
import com.example.map_clock_api34.home.ListAdapter.ListAdapterTool;
import com.example.map_clock_api34.home.ListAdapter.RecyclerViewActionHome;

import java.util.ArrayList;
import java.util.HashMap;

public class Note extends Fragment {

    View rootView;

    SharedViewModel sharedViewModel;
    RecyclerView recyclerViewRoute;
    ListAdapterRoute listAdapterRoute;

    ArrayList<HashMap<String, String>> arrayList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.note_fragment_note, container, false);

        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);

        //初始化ActionBar
        setupActionBar();
        setupRecyclerViews();
        return rootView;
    }
    //ActionBar初始設定
    private void setupActionBar() {
        //取消原本預設的ActionBar，為了之後自己的Bar創建用
        ActionBar actionBar = ((AppCompatActivity) requireActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
            actionBar.setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(getActivity(), R.color.lightgreen)));
        }
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
        //設置右上角的小圖示
        ImageView bookmark = new ImageView(requireContext());
        bookmark.setImageResource(R.drawable.route);
        bookmark.setPadding(10, 10, 5, 10);//設定icon邊界
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                100, // 设置宽度为 100 像素
                100 // 设置高度为 100 像素
        );
        params.setMarginStart(10); // 设置左边距
        bookmark.setLayoutParams(params);
        // 創建右上角的名字
        TextView bookTitle = new TextView(requireContext());
        bookTitle.setText("記事");
        bookTitle.setTextSize(15);
        bookTitle.setTextColor(getResources().getColor(R.color.green)); // 更改文字颜色
        bookTitle.setPadding(10, 10, 10, 10); // 设置内边距
        linearLayout.addView(bookmark);
        linearLayout.addView(bookTitle);
        cardViewtitle.addView(linearLayout);
        // 將cardview新增到actionBar
        actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false); // 隐藏原有的標題
            actionBar.setDisplayShowCustomEnabled(true);
            actionBar.setCustomView(cardViewtitle, new ActionBar.LayoutParams(
                    ActionBar.LayoutParams.WRAP_CONTENT,
                    ActionBar.LayoutParams.WRAP_CONTENT,
                    Gravity.END));
            actionBar.show();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        RecycleViewReset();
    }
    private void RecycleViewReset() {
        //清除原本的表
        arrayList.clear();
        String shortLocationName;
        if (sharedViewModel.getLocationCount() != -1) {
            for (int j = 0; j <= sharedViewModel.getLocationCount(); j++) {
                HashMap<String, String> hashMap = new HashMap<>();
                shortLocationName = sharedViewModel.getDestinationName(j);
                //如果地名大於20字，後面都用...代替
                if (shortLocationName.length() > 20) {
                    hashMap.put("data", shortLocationName.substring(0, 20) + "...");
                } else {
                    hashMap.put("data", shortLocationName);
                }
                //重新加回路線表
                arrayList.add(hashMap);
            }
        }
        //套用更新
        listAdapterRoute.notifyDataSetChanged();

    }
    //初始化設定表和功能表
    private void setupRecyclerViews() {
        // 初始化路線的表
        recyclerViewRoute = rootView.findViewById(R.id.recycleViewnote);
        recyclerViewRoute.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerViewRoute.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));
        listAdapterRoute = new ListAdapterRoute(arrayList, sharedViewModel, false); // 禁用拖動功能，啟用單選功能
        recyclerViewRoute.setAdapter(listAdapterRoute);
    }

}