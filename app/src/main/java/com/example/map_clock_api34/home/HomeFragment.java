package com.example.map_clock_api34.home;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.map_clock_api34.R;
import com.example.map_clock_api34.home.CreateLocation;
//此頁面為中繼頁面
//目的是為了在地圖頁面和排行程頁面中轉換

public class HomeFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_home, container, false);


        CreateLocation createFragment = new CreateLocation();
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, createFragment);
        transaction.addToBackStack(null);
        transaction.commit();


        return v;
    }

}
