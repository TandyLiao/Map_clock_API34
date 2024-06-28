package com.example.map_clock_api34.history;
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
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.map_clock_api34.history.ListAdapter.ListAdapterHistory;
import com.example.map_clock_api34.history.ListAdapter.RecyclerViewActionHistory;
import com.example.map_clock_api34.R;

import android.widget.Button;

import java.util.ArrayList;
import java.util.HashMap;


public class HistoryFragment extends Fragment {

    boolean isEdit;

    View rootView;
    Button btnEdit, btnSelect;

    RecyclerView recyclerViewHistory;
    ListAdapterHistory listAdapterHistory;
    RecyclerViewActionHistory recyclerViewActionHistory;
    ArrayList<HashMap<String, String>> arrayList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.history_fragment_history, container, false);

        setupActionBar();

        //初始化按鈕
        setupButtons();
        //初始化路線表和功能表
        setupRecyclerViews();

        return rootView;
    }

    //初始化按鈕(包含定位請求)
    private void setupButtons(){

        btnEdit = rootView.findViewById(R.id.EditButton);
        btnEdit.setOnClickListener(v -> {
            updateButtonState();
        });

        btnSelect = rootView.findViewById(R.id.SelectButton);
        btnSelect.setOnClickListener(v -> {

        });
    }
    private void setupRecyclerViews() {
        //初始化路線的表
        recyclerViewHistory = rootView.findViewById(R.id.recycleViewHistory);
        recyclerViewHistory.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerViewHistory.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));
        listAdapterHistory = new ListAdapterHistory(arrayList);
        recyclerViewHistory.setAdapter(listAdapterHistory);

        //讓路線表可以交換、刪除...等動作
        recyclerViewActionHistory = new RecyclerViewActionHistory();
        recyclerViewActionHistory.attachToRecyclerView(recyclerViewHistory, arrayList, listAdapterHistory, getActivity());

    }
    private void RecycleViewReset() {
        //清除原本的表
        arrayList.clear();

        HashMap<String, String> hashMap1 = new HashMap<>();
        hashMap1.put("data", "TANDY");
        arrayList.add(hashMap1);

        HashMap<String, String> hashMap2 = new HashMap<>();
        hashMap2.put("data", "吳俊廷");
        arrayList.add(hashMap2);

        HashMap<String, String> hashMap3 = new HashMap<>();
        hashMap3.put("data", "趙子陽");
        arrayList.add(hashMap3);


        //套用更新
        listAdapterHistory.notifyDataSetChanged();

    }
    @Override
    public void onResume() {
        super.onResume();
        RecycleViewReset();
    }

    private void setupActionBar() {

        //取消原本預設的ActionBar，為了之後自己的Bar創建用
        ActionBar actionBar = ((AppCompatActivity) requireActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
            actionBar.setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(getActivity(), R.color.lightgreen)));
        }

        // 建立 CardView 在 toolbar
        CardView cardViewtitle = new CardView(requireContext());
        cardViewtitle.setLayoutParams(new CardView.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT));
        Drawable drawable = ContextCompat.getDrawable(requireContext(), R.drawable.cardviewtitle_shape);
        cardViewtitle.setBackground(drawable);

        // 建立 LinearLayout 在 CardView 等等放图案和文字
        LinearLayout linearLayout = new LinearLayout(requireContext());
        linearLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        ));
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);

        // ImageView
        ImageView mark = new ImageView(requireContext());
        mark.setImageResource(R.drawable.history_record1);
        mark.setPadding(10, 10, 5, 10);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                100, // 设置宽度为 100 像素
                100 // 设置高度为 100 像素
        );
        params.setMarginStart(10); // 设置左边距
        mark.setLayoutParams(params);

        // TextView
        TextView bookTitle = new TextView(requireContext());
        bookTitle.setText("歷史紀錄");
        bookTitle.setTextSize(15);
        bookTitle.setTextColor(getResources().getColor(R.color.green)); // 更改文字颜色
        bookTitle.setPadding(10, 10, 10, 10); // 设置内边距

        linearLayout.addView(mark);
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
    private void updateButtonState(){

    }
}
