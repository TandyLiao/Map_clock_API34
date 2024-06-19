package com.example.map_clock_api34.history;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import com.example.map_clock_api34.R;

public class HistoryEditFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history_edit, container, false);

        Button clearButton = view.findViewById(R.id.Clearbutton);
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 建立並顯示確認對話框
                new AlertDialog.Builder(requireContext())
                        .setTitle("確認")
                        .setMessage("請問確定要全部刪除嗎?")
                        .setPositiveButton("確認", (dialog, which) -> {
                            // 在這裡處理清除操作
                            // 例如，清空RecyclerView的資料
                        })
                        .setNegativeButton("取消", (dialog, which) -> {
                            // 使用者選擇取消，不執行任何操作
                        })
                        .show();
            }
        });

        return view;
    }
}
