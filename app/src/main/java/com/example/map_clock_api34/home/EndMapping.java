package com.example.map_clock_api34.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import com.example.map_clock_api34.R;
import com.example.map_clock_api34.SharedViewModel;
import com.example.map_clock_api34.SharedViewModel;

public class EndMapping extends Fragment {
    private View rootView;
    SharedViewModel sharedViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.home_end_creation, container, false);

        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);

        backButton();

        return rootView;
    }

    private void backButton() {
        Button btnBack = rootView.findViewById(R.id.back_creation);
        btnBack.setOnClickListener(v -> {
            // 清空 ViewModel 中的数据
            sharedViewModel.clearAll();

            // 切换回 CreateLocation Fragment
            CreateLocation createFragment = new CreateLocation();
            FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fl_container, createFragment);
            transaction.addToBackStack(null);
            transaction.commit();
        });
    }
}
