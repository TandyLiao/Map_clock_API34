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


public class tutorial extends Fragment {
    View rootView;
    public int count =0;
    public String name, drawablename;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.activity_tutorial, container, false);

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("MyPrefs", MODE_PRIVATE);
        int page = sharedPreferences.getInt("WhichPage", -1);

        // 隱藏 ActionBar
        if (getActivity() != null && getActivity() instanceof AppCompatActivity) {
            AppCompatActivity activity = (AppCompatActivity) getActivity();
            if (activity.getSupportActionBar() != null) {
                activity.getSupportActionBar().hide();
            }
        }

        Button next = rootView.findViewById(R.id.nextpage);
        next.setOnClickListener(v ->{
            count++;
            getImage();
        });
        switch (page)
        {
            case 0:
                drawablename ="setting";
                getImage();
                break;
            case 1:
                drawablename ="tandy";
                getImage();
                break;
            case 2:
                drawablename ="rain";
                getImage();
                break;
            default:
                Log.d("tutorial","Page not found");
                break;

        }

        return rootView;
    }


    public void getImage()
    {
        ImageView imageView = rootView.findViewById(R.id.TutoImage);
        name= drawablename + count;
        int id= getResources().getIdentifier(name,"drawable",getActivity().getPackageName());
        if (id==0)
        {
            getActivity().getSupportFragmentManager().popBackStack();
        }
        else
        {
            imageView.setImageResource(id);
        }


    }
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
