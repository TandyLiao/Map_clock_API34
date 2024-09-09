package com.example.map_clock_api34;

import static android.content.Context.MODE_PRIVATE;

import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

public class TutorialFragment extends Fragment {

    private View rootView;
    private Button next,pre;

    private int count = 1; // 記錄當前頁數，初始為第一頁
    private String name, drawablename; // 儲存圖片名稱的變數

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.tutorial_fragment, container, false);

        setupButton();
        decideWhichPage();
        updateNextButtonState();
        updatePreButtonState();

        // 隱藏 ActionBar，如果目前的活動繼承自 AppCompatActivity
        if (getActivity() != null && getActivity() instanceof AppCompatActivity) {
            AppCompatActivity activity = (AppCompatActivity) getActivity();
            if (activity.getSupportActionBar() != null) {
                activity.getSupportActionBar().hide(); // 隱藏 ActionBar
            }
        }
        return rootView; // 返回根視圖
    }

    public void decideWhichPage(){

        // 讀取 SharedPreferences，檢查應該顯示哪個教學頁面
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("MyPrefs", MODE_PRIVATE);
        int page = sharedPreferences.getInt("WhichPage", -1); // 獲取頁面號，預設為 -1（表示無效）

        // 根據 SharedPreferences 中的頁面資訊設定要顯示的圖片集
        switch (page) {
            // 0 為路線規劃
            case 0:
                drawablename = "route_";
                getImage(); // 載入圖片
                break;
            // 1 為歷史頁面
            case 1:
                drawablename = "history_";
                getImage(); // 載入圖片
                break;
            // 2 為收藏路線
            case 2:
                drawablename = "markroute_";
                getImage(); // 載入圖片
                break;
            // 3 為路線設定
            case 3:
                drawablename = "locatesetting_";
                getImage(); // 載入圖片
                break;
            // 4 為記事頁面
            case 4:
                drawablename = "note_";
                getImage(); // 載入圖片
                break;
            default:
                Log.d("tutorial", "Page not found"); // 如果找不到對應的頁面號，則輸出錯誤日誌
                break;
        }
    }

    public void setupButton(){

        // 設置左上角的關閉按紐
        ImageView btnBack = rootView.findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> {
            getActivity().getSupportFragmentManager().popBackStack();
        });

        // 設置下一頁按鈕的點擊事件
        next = rootView.findViewById(R.id.nextpage);
        next.setOnClickListener(v -> {
            count++; // 增加頁數
            getImage(); // 根據當前頁數載入對應的圖片
        });

        // 設置上一頁按鈕的點擊事件
        pre = rootView.findViewById(R.id.prepage);
        pre.setOnClickListener(v -> {
            count--; // 增加頁數
            getImage(); // 根據當前頁數載入對應的圖片
        });
    }

    // 這個方法根據當前頁數載入對應的圖片
    public void getImage() {
        // 根據圖片名稱組合出資源 ID
        ImageView imageView = rootView.findViewById(R.id.TutoImage); // 獲取 ImageView
        name = drawablename + count; // 例如，"joking1", "tandy2" 等等
        int id = getResources().getIdentifier(name, "drawable", getActivity().getPackageName()); // 獲取資源 ID
        // 檢查當前資源 ID 是否存在
        if (id == 0) {
            // 如果資源 ID 為 0，表示圖片不存在，回到上一頁
            getActivity().getSupportFragmentManager().popBackStack();
        } else {
            // 否則，設定 ImageView 顯示該圖片
            // 設定圖片
            Drawable imageDrawable = getResources().getDrawable(id);

            // 加載邊框 drawable
            Drawable borderDrawable = getResources().getDrawable(R.drawable.border_image);

            // 創建 LayerDrawable，疊加圖片和邊框
            Drawable[] layers = new Drawable[2];
            layers[0] = imageDrawable; // 底層是圖片
            layers[1] = borderDrawable; // 上層是邊框

            // 設置 LayerDrawable 給 ImageView
            LayerDrawable layerDrawable = new LayerDrawable(layers);
            imageView.setImageDrawable(layerDrawable);

        }
        updateNextButtonState();
        updatePreButtonState();
    }

    // 更新上一頁重置按鈕的狀態
    private void updatePreButtonState() {

        int tempint = count-1;
        String tempstring = drawablename + tempint;
        int tempID = getResources().getIdentifier(tempstring, "drawable", getActivity().getPackageName()); // 獲取資源 ID

        if (tempID == 0) {
            // 按鈕不可點擊
            pre.setEnabled(false);
            // 改變按鈕文字顏色
            pre.setTextColor(ContextCompat.getColor(requireContext(), R.color.lightgreen));
            // 設定禁用狀態的背景
            pre.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.btn_unclickable));

        } else {
            // 按鈕可點擊
            pre.setEnabled(true);
            // 改變按鈕文字顏色
            pre.setTextColor(ContextCompat.getColor(requireContext(), R.color.green));
            // 設定啟用狀態的背景
            pre.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.btn_additem));

        }
    }
    // 更新下一頁重置按鈕的狀態
    private void updateNextButtonState() {

        int tempint = count+1;
        String tempstring = drawablename + tempint;
        int tempID = getResources().getIdentifier(tempstring, "drawable", getActivity().getPackageName()); // 獲取資源 ID

        if (tempID == 0) {
            next.setText("關閉頁面");
        } else {
            next.setText("下一頁");
        }
    }

    // 當 Fragment 被銷毀時，恢復 ActionBar
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (getActivity() != null && getActivity() instanceof AppCompatActivity) {
            AppCompatActivity activity = (AppCompatActivity) getActivity();
            if (activity.getSupportActionBar() != null) {
                activity.getSupportActionBar().show();  // 恢復顯示 ActionBar
            }
        }
    }
}
