package com.example.map_clock_api34;

import static android.content.Context.MODE_PRIVATE;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Switch;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import com.example.map_clock_api34.home.CreateLocation;


public class tutorial extends Fragment {
    View rootView;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.activity_tutorial, container, false);
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("MyPrefs", MODE_PRIVATE);
        int page = sharedPreferences.getInt("WhichPage", -1);
        switch (page)
        {
            case 0:
                getImage(page);
                break;
            case 1:
                break;
            case 2:
                break;
            default:
                Log.d("tutorial","Page not found");
                break;

        }

        return rootView;
    }

    public void getImage(int page)
    {
        int n=1;
        String name="appicon_tem"+n;
        int id= getResources().getIdentifier(name,"drawable",getActivity().getPackageName());
        ImageView imageView = rootView.findViewById(R.id.TutoImage);
        imageView.setImageResource(id);

    }


}
