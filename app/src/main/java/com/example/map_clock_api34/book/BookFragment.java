package com.example.map_clock_api34.book;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import android.app.Activity;

import com.example.map_clock_api34.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;

public class BookFragment extends Fragment {
    private static final int CREATE_BOOKMARK_REQUEST = 1;

    private TextView tx1;
    private Toolbar toolbar;
    private BottomNavigationView navigation;
    private ListView listView1, listView2;
    private ArrayAdapter<String> adapter1, adapter2;
    private ArrayList<String> list1, list2;



    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bookmark, container, false);

        listView1 = view.findViewById(R.id.list_view1);
        listView2 = view.findViewById(R.id.list_view2);

        list1 = new ArrayList<String>();
        list2 = new ArrayList<String>();

        adapter1 = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, list1);
        listView1.setAdapter(adapter1);

        adapter2 = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, list2);
        listView2.setAdapter(adapter2);

        return view;
    }


    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        toolbar = getActivity().findViewById(R.id.toolbar);

        // 獲取 confirm_button 的引用並為它設置 OnClickListener
        Button confirmButton = view.findViewById(R.id.confirm_button);
        confirmButton.setOnClickListener(v -> {
            CreateBookmark createBookmarkFragment = new CreateBookmark();
            getActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.container_id, createBookmarkFragment)
                    .commit();
        });

        // 如果 navigation 控件有在你的 bookmark layout
        navigation = view.findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
    }

    private final BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = menuItem -> {
        if (menuItem.getItemId() == R.id.action_create_bookmark) {
            CreateBookmark createBookmarkFragment = new CreateBookmark();
            getActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.action_create_bookmark, createBookmarkFragment)
                    .commit();
            return true;
        }
        return false;
    };


    @Override
    public void onResume() {
        super.onResume();


        //建立CardView在toolbar
        CardView cardViewtitle = new CardView(requireContext());
        cardViewtitle.setLayoutParams(new CardView.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT));
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
        ImageView mark = new ImageView(requireContext());
        mark.setImageResource(R.drawable.bookmark1);
        mark.setPadding(10,10,5,10);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                100, // 设置宽度为 100 像素
                100 // 设置高度为 100 像素
        );
        params.setMarginStart(10); // 设置左边距
        mark.setLayoutParams(params);

        // 創建TextView
        TextView bookTitle = new TextView(requireContext());
        bookTitle.setText("書籤");
        bookTitle.setTextSize(15);
        bookTitle.setTextColor(getResources().getColor(R.color.green)); // 更改文字颜色
        bookTitle.setPadding(10, 10, 10, 10); // 设置内边距

        linearLayout.addView(mark);
        linearLayout.addView(bookTitle);
        cardViewtitle.addView(linearLayout);

        // 將cardview新增到actionBar
        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false); // 隐藏原有的标题
            actionBar.setDisplayShowCustomEnabled(true);
            actionBar.setCustomView(cardViewtitle, new ActionBar.LayoutParams(
                    ActionBar.LayoutParams.WRAP_CONTENT, // 宽度设置为 WRAP_CONTENT
                    ActionBar.LayoutParams.WRAP_CONTENT, // 高度设置为 WRAP_CONTENT
                    Gravity.END)); // 将包含 TextView 的 CardView 设置为自定义视图
            actionBar.show();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        ActionBar actionBar = ((AppCompatActivity) requireActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowCustomEnabled(false);
            actionBar.setCustomView(null);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CREATE_BOOKMARK_REQUEST) {
            if (resultCode == Activity.RESULT_OK) {
                ArrayList<String> returnedList1 = data.getStringArrayListExtra("list1");
                ArrayList<String> returnedList2 = data.getStringArrayListExtra("list2");

                list1.clear();
                list1.addAll(returnedList1);
                adapter1.notifyDataSetChanged();

                list2.clear();
                list2.addAll(returnedList2);
                adapter2.notifyDataSetChanged();
            }


        }


    }
}
