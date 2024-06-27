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
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.map_clock_api34.R;
import com.example.map_clock_api34.SharedViewModel;
import com.example.map_clock_api34.Database.AppDatabaseHelper;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HistoryEditFragment extends Fragment {

    private AppDatabaseHelper dbHelper;
    private SharedViewModel sharedViewModel;
    private RecyclerView recyclerView;
    private HistoryEditAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.history_fragment_history_edit, container, false);

        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        dbHelper = new AppDatabaseHelper(getContext(), sharedViewModel);

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // 初始化适配器，传入空数据
        adapter = new HistoryEditAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);

        // 观察 SharedViewModel 中 i 的变化
        sharedViewModel.getI().observe(getViewLifecycleOwner(), new Observer<Integer>() {
            @Override
            public void onChanged(@Nullable final Integer i) {
                if (i != null && i >= 0) {
                    // 更新适配器数据
                    adapter.setData(Arrays.asList(sharedViewModel.getDestinationNameArray().getValue()));
                }
            }
        });

        Button clearButton = view.findViewById(R.id.Clearbutton);
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(requireContext())
                        .setTitle("确认")
                        .setMessage("确定要删除所有数据吗？")
                        .setPositiveButton("确定", (dialog, which) -> {
                            // 清除 ViewModel 中的数据
                            sharedViewModel.clearData();
                        })
                        .setNegativeButton("取消", (dialog, which) -> {
                            // 用户选择取消
                        })
                        .show();
            }
        });

        return view;
    }
}
