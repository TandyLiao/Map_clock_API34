package com.example.map_clock_api34.note;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.map_clock_api34.R;
import com.example.map_clock_api34.SharedViewModel;

public class NoteEnterContent extends Fragment {

    View rootView;
    private SharedViewModel sharedViewModel;
    private EditText detailTextView;
    private TextView title;
    private TextView counttextview;
    
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.note_note_enter_content, container, false);

        detailTextView = rootView.findViewById(R.id.textView3);//benson
        title = rootView.findViewById(R.id.textView4);
        //改尺寸
        title.setTextColor(getResources().getColor(R.color.black)); // 更改文字颜色
        counttextview=rootView.findViewById(R.id.textcount);


        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        setupActionBar();
        title.setText(sharedViewModel.getDestinationName(sharedViewModel.getPosition()));
        Button verify = rootView.findViewById(R.id.verifyButton);
        verify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!detailTextView.getText().toString().isEmpty()) {
                    // 如果不為空，更新 ViewModel
                    sharedViewModel.setNote(detailTextView.getText().toString(), sharedViewModel.getPosition());
                }
                getActivity().getSupportFragmentManager().popBackStack();
                hideKeyboard();

            }
        });
        detailTextView.setFilters(new InputFilter[]{
                new InputFilter.LengthFilter(100)
        });
        detailTextView.addTextChangedListener(new LineLimitWatcher(20, detailTextView));
        // 設置 TextWatcher 來監聽輸入變化
        detailTextView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // 輸入前的操作
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // 當輸入內容改變時
                int length = s.length();
                counttextview.setText(length + "/100"); // 更新字數顯示
            }

            @Override
            public void afterTextChanged(Editable s) {
                // 輸入後的操作
            }
        });

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
        mark.setImageResource(R.drawable.note);
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

        returnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });


    }
    private void hideKeyboard() {
        View view = getActivity().getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }
    }
    @Override
    public void onResume() {
        super.onResume();
        detailTextView.setText(sharedViewModel.getNote(sharedViewModel.getPosition()));
    }


}

