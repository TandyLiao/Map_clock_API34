package com.example.map_clock_api34.home;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.map_clock_api34.R;
import com.example.map_clock_api34.SharedViewModel;

import java.util.ArrayList;
import java.util.HashMap;

public class CreateLocation extends Fragment {
    private View v;
    RecyclerView recyclerView;
    ListAdapter listAdapter;

    //新增一個HashMap存放每筆資料
    ArrayList<HashMap<String, String>> arrayList = new ArrayList<>();
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        v = inflater.inflate(R.layout.fragment_creatlocation, container, false);

        SharedViewModel sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        if(sharedViewModel.getI()!=-1){

            for(int j =0; j<=sharedViewModel.getI(); j++){
                HashMap<String, String> hashMap = new HashMap<>();
                hashMap.put("number",String.format("%02d",  j + 1));
                hashMap.put("data",sharedViewModel.getDestinationName(j));
                arrayList.add(hashMap);
            }

        }


        Button btnA = v.findViewById(R.id.btn_addItem);
        btnA.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //確定有無開定位權限，沒有就請求，如果用戶拒絕，只能糗她手動開啟權限，不然不能運行程地圖
                if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {

                    if(sharedViewModel.getI()<7){

                        MapsFragment mapFragment = new MapsFragment();
                        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                        transaction.replace(R.id.fragment_container, mapFragment);
                        transaction.addToBackStack(null);
                        transaction.commit();

                    }
                }else{
                    requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                            LOCATION_PERMISSION_REQUEST_CODE);

                    Toast.makeText(getActivity(), "請開啟定位權限", Toast.LENGTH_SHORT).show();
                }


            }
        });

        Button btnD = v.findViewById(R.id.btn_DropItem);
        btnD.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(sharedViewModel.getI()>=0){

                    arrayList.remove(sharedViewModel.getI());
                    sharedViewModel.setI();
                    recyclerView.setAdapter(listAdapter);
                }

            }
        });


        //設置RecycleView
        recyclerView = v.findViewById(R.id.recycleView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));
        listAdapter = new ListAdapter();
        recyclerView.setAdapter(listAdapter);


        return v;
    }

    //ListAdapter的class
    private class ListAdapter extends RecyclerView.Adapter<ListAdapter.ViewHolder> {

        class ViewHolder extends RecyclerView.ViewHolder {
            private TextView tx1,tx2;
            public ViewHolder(View itemView) {
                super(itemView);
                tx1 = itemView.findViewById(R.id.txtVID);
                tx2 = itemView.findViewById(R.id.textVLocateionName);
            }
        }
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.recycleviewitem, parent, false);
            return new ViewHolder(view);
        }

        //從HashMap中抓取資料並將其印出
        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            holder.tx1.setText(arrayList.get(position).get("number"));
            holder.tx2.setText(arrayList.get(position).get("data"));
            ViewGroup.LayoutParams layoutParams = holder.itemView.getLayoutParams();
            layoutParams.height = 150;

            holder.itemView.setLayoutParams(layoutParams);

        }

        //回傳arrayList的大小
        @Override
        public int getItemCount() {
            return arrayList.size();
        }
    }
}
