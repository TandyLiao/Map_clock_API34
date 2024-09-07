package com.example.map_clock_api34.CreateLocation;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.map_clock_api34.R;
import com.example.map_clock_api34.SharedViewModel;

public class EndMappingFragment extends Fragment {
    private View rootView;
    private SharedViewModel sharedViewModel;

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

            // 發送 Intent 來停止鈴聲和震動
            Intent stopIntent = new Intent(getContext(), LocationService.class);
            stopIntent.setAction("STOP_VIBRATION"); // 這個 action 是用來告訴 LocationService 停止震動和鈴聲
            requireContext().startService(stopIntent);

            getActivity().getSupportFragmentManager().popBackStack();
            getActivity().getSupportFragmentManager().popBackStack();
        });
    }
}
