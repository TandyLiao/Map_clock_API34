package com.example.map_clock_api34.note;

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
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.map_clock_api34.R;
import com.example.map_clock_api34.SharedViewModel;
import com.example.map_clock_api34.home.CreateLocation;
import com.example.map_clock_api34.home.ListAdapter.ListAdapterRoute;
import com.example.map_clock_api34.home.SelectPlace;

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

    private void setupActionBar() {
        CardView cardViewtitle = new CardView(requireContext());
        cardViewtitle.setLayoutParams(new CardView.LayoutParams(
                ActionBar.LayoutParams.WRAP_CONTENT,
                ActionBar.LayoutParams.WRAP_CONTENT));
        Drawable drawable = ContextCompat.getDrawable(requireContext(), R.drawable.cardviewtitle_shape);
        cardViewtitle.setBackground(drawable);

        // 建立LinearLayout在CardView等等放圖案和文字
        LinearLayout linearLayout = new LinearLayout(requireContext());
        linearLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        ));
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);

        // ImageView放置圖案
        ImageView mark = new ImageView(requireContext());
        mark.setImageResource(R.drawable.anya062516);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                100, // 设置宽度为 100 像素
                100 // 设置高度为 100 像素
        );
        params.setMarginStart(10); // 设置左边距
        mark.setLayoutParams(params);




        // 創建TextView
        TextView bookTitle = new TextView(requireContext());
        bookTitle.setText("記事");
        bookTitle.setTextSize(15);
        bookTitle.setTextColor(getResources().getColor(R.color.green)); // 更改文字颜色
        bookTitle.setPadding(10, 10, 10, 10); // 设置内边距

        // 將ImageView和TextView添加到LinearLayout
        linearLayout.addView(mark);
        linearLayout.addView(bookTitle);
        cardViewtitle.addView(linearLayout);

        // 创建自定义返回按钮
        ImageView returnButton = new ImageView(requireContext());
        returnButton.setImageResource(R.drawable.back);
        LinearLayout.LayoutParams returnButtonParams = new LinearLayout.LayoutParams(
                100, // 设置宽度为 100 像素
                100 // 设置高度为 100 像素
        );
        returnButton.setLayoutParams(returnButtonParams);


        // 建立ActionBar的父LinearLayout
        LinearLayout actionBarLayout = new LinearLayout(requireContext());
        actionBarLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        ));
        actionBarLayout.setOrientation(LinearLayout.HORIZONTAL);
        actionBarLayout.setWeightSum(1.0f);

        // 子LinearLayout用于返回按钮
        LinearLayout leftLayout = new LinearLayout(requireContext());
        leftLayout.setLayoutParams(new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.MATCH_PARENT,
                0.1f
        ));
        leftLayout.setOrientation(LinearLayout.HORIZONTAL);
        leftLayout.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
        leftLayout.addView(returnButton);

        // 子LinearLayout用于cardViewtitle
        LinearLayout rightLayout = new LinearLayout(requireContext());
        rightLayout.setLayoutParams(new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.MATCH_PARENT,
                0.9f
        ));
        rightLayout.setOrientation(LinearLayout.HORIZONTAL);
        rightLayout.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
        rightLayout.addView(cardViewtitle);

        // 将子LinearLayout添加到父LinearLayout
        actionBarLayout.addView(leftLayout);
        actionBarLayout.addView(rightLayout);

        androidx.appcompat.widget.Toolbar toolbar = (androidx.appcompat.widget.Toolbar) getActivity().findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(null); // 隐藏漢汉堡菜单

        // 获取ActionBar
        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false); // 隐藏原有的标题
            actionBar.setDisplayShowCustomEnabled(true);
            actionBar.setCustomView(actionBarLayout, new ActionBar.LayoutParams(
                    ActionBar.LayoutParams.MATCH_PARENT, // 宽度设置为 MATCH_PARENT
                    ActionBar.LayoutParams.MATCH_PARENT // 高度设置为 MATCH_PARENT
            ));
            actionBar.show();
        }

        // 设置返回按钮点击事件
        returnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CreateLocation createFragment = new CreateLocation();
                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.home_fragment_container, createFragment);
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });
    }
    public void onPause() {
        super.onPause();

        // 获取ActionBar
        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowCustomEnabled(false);
            actionBar.setCustomView(null);
            actionBar.setDisplayShowTitleEnabled(true); // 恢复显示标题
            actionBar.show();
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        RecycleViewReset();
        setupActionBar();
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

    //換頁
    // 跳到記事Fragment
    // 獲取 FragmentManager
    private void openWritingFragment() {
        NoteEnterContent noteEnterContent = new NoteEnterContent();
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.home_fragment_container, noteEnterContent);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    //初始化設定表和功能表
    private void setupRecyclerViews() {
        // 初始化路線的表
        recyclerViewRoute = rootView.findViewById(R.id.recycleViewnote);
        recyclerViewRoute.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerViewRoute.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));
        listAdapterRoute = new ListAdapterRoute(arrayList, sharedViewModel, false); // 禁用拖動功能，啟用單選功能
        listAdapterRoute.setOnItemClickListener(new ListAdapterRoute.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                openWritingFragment();
            }
        });

        recyclerViewRoute.setAdapter(listAdapterRoute);
    }

}