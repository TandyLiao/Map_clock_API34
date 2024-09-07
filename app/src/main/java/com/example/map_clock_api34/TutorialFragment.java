package com.example.map_clock_api34;

import static android.content.Context.MODE_PRIVATE;

import android.content.SharedPreferences;
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
import androidx.fragment.app.Fragment;

public class TutorialFragment extends Fragment {

    View rootView;
    public int count = 1; // 記錄當前頁數，初始為第一頁
    public String name, drawablename; // 儲存圖片名稱的變數

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.tutorial_fragment, container, false);

        // 隱藏 ActionBar，如果目前的活動繼承自 AppCompatActivity
        if (getActivity() != null && getActivity() instanceof AppCompatActivity) {
            AppCompatActivity activity = (AppCompatActivity) getActivity();
            if (activity.getSupportActionBar() != null) {
                activity.getSupportActionBar().hide(); // 隱藏 ActionBar
            }
        }

        // 讀取 SharedPreferences，檢查應該顯示哪個教學頁面
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("MyPrefs", MODE_PRIVATE);
        int page = sharedPreferences.getInt("WhichPage", -1); // 獲取頁面號，預設為 -1（表示無效）

        // 設置下一頁按鈕的點擊事件
        Button next = rootView.findViewById(R.id.nextpage);
        next.setOnClickListener(v -> {
            count++; // 增加頁數
            getImage(); // 根據當前頁數載入對應的圖片
        });

        // 根據 SharedPreferences 中的頁面資訊設定要顯示的圖片集
        switch (page) {
            case 0:
                drawablename = "joking"; // "joking" 教學圖片集
                getImage(); // 載入圖片
                break;
            case 1:
                drawablename = "tandy"; // "tandy" 教學圖片集
                getImage(); // 載入圖片
                break;
            case 2:
                drawablename = "rain"; // "rain" 教學圖片集
                getImage(); // 載入圖片
                break;
            default:
                Log.d("tutorial", "Page not found"); // 如果找不到對應的頁面號，則輸出錯誤日誌
                break;
        }

        return rootView; // 返回根視圖
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
            imageView.setImageResource(id);
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
