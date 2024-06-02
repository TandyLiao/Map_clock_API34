//CreateBookmark
package com.example.map_clock_api34.book;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ListView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
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
import androidx.appcompat.app.ActionBar;
import android.content.Context;


import com.example.map_clock_api34.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import android.app.Activity;

public class CreateBookmark extends Fragment {

    private ListView listView1;
    private ListView listView2;
    private EditText editText;
    private ArrayAdapter<String> adapter1;
    private ArrayAdapter<String> adapter2;
    private SharedPreferences sharedPreferences;
    private boolean nextList = false;

    private TextView tx1;
    private Toolbar toolbar;

    private BookmarkDAO bookmarkDAO;



    @Nullable

    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.create_bookmark, container, false);
    }


    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sharedPreferences = getActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        sharedPreferences = getActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);

        final List<String> list1 = new ArrayList<>(sharedPreferences.getStringSet("list1", new HashSet<String>()));
        final List<String> list2 = new ArrayList<>(sharedPreferences.getStringSet("list2", new HashSet<String>()));

        bookmarkDAO = new BookmarkDAO(getActivity());

        listView1 = view.findViewById(R.id.list_view1);
        listView2 = view.findViewById(R.id.list_view2);
        editText = view.findViewById(R.id.my_textbox);

        adapter1 = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, list1);
        adapter2 = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, list2);

        listView1.setAdapter(adapter1);
        listView2.setAdapter(adapter2);

        listView1.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                String bookmarkName = list1.get(position);
                list1.remove(position);
                bookmarkDAO.deleteBookmarkByName(bookmarkName);
                adapter1.notifyDataSetChanged();
                return true;
            }
        });

        listView2.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                String bookmarkName = list2.get(position);
                list2.remove(position);
                bookmarkDAO.deleteBookmarkByName(bookmarkName);
                adapter2.notifyDataSetChanged();
                return true;
            }
        });

        Button addRoute = view.findViewById(R.id.AddRoute);
        addRoute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String input = editText.getText().toString();
                if (!input.equals("")) {
                    if (!nextList) {
                        list1.add(input);
                        adapter1.notifyDataSetChanged();
                        editText.setText("");
                    } else {
                        list2.add(input);
                        adapter2.notifyDataSetChanged();
                        editText.setText("");
                    }
                    nextList = !list1.isEmpty();
                }
            }
        });

        Button setRoute = view.findViewById(R.id.setRoute);
        setRoute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri gmmIntentUri = Uri.parse("geo:0,0?q=");
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                if (mapIntent.resolveActivity(getActivity().getPackageManager()) != null) {
                    startActivity(mapIntent);
                }
            }
        });

        Button confirmButton = view.findViewById(R.id.confirm_button);
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                Set<String> set1 = new HashSet<String>(list1);
                Set<String> set2 = new HashSet<String>(list2);
                editor.putStringSet("list1", set1);
                editor.putStringSet("list2", set2);
                editor.apply();

                Intent data = new Intent();
                data.putStringArrayListExtra("list1", new ArrayList<String>(list1));
                data.putStringArrayListExtra("list2", new ArrayList<String>(list2));
                getActivity().setResult(Activity.RESULT_OK, data);
                getActivity().finish();


                Intent dataToSendBack = new Intent();
                dataToSendBack.putStringArrayListExtra("list1", new ArrayList<String>(list1));
                dataToSendBack.putStringArrayListExtra("list2", new ArrayList<String>(list2));
                getActivity().setResult(Activity.RESULT_OK, dataToSendBack);
                getActivity().finish();
            }
        });

        Button cancelButton = view.findViewById(R.id.cancel_button);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Set<String> backup = sharedPreferences.getStringSet("list1_backup", new HashSet<String>());
                if (backup.size() > 0) {
                    list1.clear();
                    list1.addAll(backup);
                    adapter1.notifyDataSetChanged();
                }
                getActivity().finish();
            }
        });
    }

    public void onResume() {
        super.onResume();


        //建立CardView在toolbar
        CardView cardViewtitle = new CardView(getActivity());
        cardViewtitle.setLayoutParams(new CardView.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT));
        Drawable drawable = ContextCompat.getDrawable(getActivity(), R.drawable.cardviewtitle_shape);
        cardViewtitle.setBackground(drawable);
        //建立LinearLayout在CardView等等放圖案和文字
        LinearLayout linearLayout = new LinearLayout(getActivity());
        linearLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        ));
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);

        //
        ImageView mark = new ImageView(getActivity());
        mark.setImageResource(R.drawable.clock);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                100, // 设置宽度为 100 像素
                100 // 设置高度为 100 像素
        );
        params.setMarginStart(10); // 设置左边距
        mark.setLayoutParams(params);

        // 創建TextView
        TextView bookTitle = new TextView(getActivity());
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
        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowCustomEnabled(false);
            actionBar.setCustomView(null);
        }
    }
}

// 动态创建 Toolbar
//Toolbar toolbar = new Toolbar(this);
//toolbar.setLayoutParams(new Toolbar.LayoutParams(Toolbar.LayoutParams.MATCH_PARENT, getResources().getDimensionPixelSize(R.dimen.action_bar_default_height_material)));

// 动态创建 CardView
        /*CardView cardViewTitle = new CardView(this);
        cardViewTitle.setLayoutParams(new CardView.LayoutParams(ActionBar.LayoutParams.WRAP_CONTENT, ActionBar.LayoutParams.WRAP_CONTENT, Gravity.END));
        Drawable drawable = ContextCompat.getDrawable(this, R.drawable.cardviewtitle_shape);
        cardViewTitle.setBackground(drawable);

        // 动态创建 LinearLayout
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT));
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);

        // 动态创建 ImageView
        ImageView mark = new ImageView(this);
        mark.setImageResource(R.drawable.clock);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                100,
                100
        );
        params.setMarginStart(10);
        mark.setLayoutParams(params);

        // 动态创建 TextView
        TextView bookTitle = new TextView(this);
        bookTitle.setText("書籤");
        bookTitle.setTextSize(15);
        bookTitle.setTextColor(getResources().getColor(R.color.green));
        bookTitle.setPadding(10, 10, 10, 10);

        // 组装 TextView 和 ImageView 到 LinearLayout
        linearLayout.addView(mark);
        linearLayout.addView(bookTitle);

        // 组装 LinearLayout 到 CardView
        cardViewTitle.addView(linearLayout);

        // 设置 ActionBar
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setCustomView(cardViewTitle);

        setContentView(R.layout.create_bookmark);*/
