package com.example.map_clock_api34.home;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import android.annotation.SuppressLint;
import android.content.Context;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.example.map_clock_api34.Distance;
import com.example.map_clock_api34.R;
import com.example.map_clock_api34.SharedViewModel;
import com.example.map_clock_api34.home.CreateLocation;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;


public class mapping extends Fragment {

    private LocationManager locationManager;
    private String commandstr = LocationManager.GPS_PROVIDER;
    Location lastLocation;
    private GoogleMap mMap;
    TextView txtTime;
    private String[] destinationName = new String[7];
    private double[] latitude = new double[7];
    private double[] longitude = new double[7];
    int j,i=0;
    double pre_distance, last_distance, speed, time, totalTime=0;
    Location startLocation;
    LatLngBounds.Builder builder;
    LatLng destiantion_LatLng;
    LatLngBounds bounds;
    View v;
    private OnMapReadyCallback callback = new OnMapReadyCallback() {

        @SuppressLint("MissingPermission")
        public void onMapReady(GoogleMap googleMap) {
            mMap=googleMap;

            //一直更新目前位置
            locationManager.requestLocationUpdates(commandstr,10000,0,locationListener);

            //取得最後的定位位置
            lastLocation = locationManager.getLastKnownLocation(commandstr);

            builder = new LatLngBounds.Builder();

            // 添加起點和目的地的位置
            builder.include(new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude()));
            startLocation = locationManager.getLastKnownLocation(commandstr);
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(startLocation.getLatitude(),startLocation.getLongitude()),15));

            //跑出藍色定位點
            mMap.setMyLocationEnabled(true);

            destiantion_LatLng= new LatLng(latitude[i],longitude[i]);
            mMap.addMarker(new MarkerOptions().position(destiantion_LatLng).title(destinationName[i]));
            builder.include(new LatLng(latitude[i], longitude[i]));

            // 構建LatLngBounds對象
            bounds = builder.build();

            // 計算將這個邊界框移動到地圖中心所需的偏移量
            int padding = 100; // 偏移量（以像素為單位）
            // 移动地图视图到最后已知的位置
            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding));


            double trip_distance = Distance.getDistanceBetweenPointsNew(latitude[0],longitude[0],startLocation.getLatitude(),startLocation.getLongitude())/1000;
            time = Math.round(trip_distance/4*60);
            txtTime.setText("目的:"+destinationName[0]+"\n公里為: "+trip_distance+" 公里"+"\n預估時間為: "+time+" 分鐘");


        }
    };
    public LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(@NonNull Location nowLocation) {

            totalTime=totalTime+10;
            pre_distance = Distance.getDistanceBetweenPointsNew(startLocation.getLatitude(),startLocation.getLongitude(),nowLocation.getLatitude(),nowLocation.getLongitude())/1000;
            last_distance = Distance.getDistanceBetweenPointsNew(latitude[i],longitude[i],nowLocation.getLatitude(),nowLocation.getLongitude())/1000;
            if(pre_distance>0.020){

                speed =pre_distance/(totalTime/60/60);
                time=Math.round(last_distance/speed*60);
                txtTime.setText("目的地:"+destinationName[i]+"\nSpeed:"+speed+"\nPre_Km: "+pre_distance+"\n剩公里為: "+last_distance+" 公里"+"\n預估時間為: "+time+" 分鐘");
            }
            else{
                totalTime-=10;
            }

            if(last_distance<0.05 && time<3){
                initPopWindow();
            }

        }
    };
    private void initPopWindow(){

        View view = LayoutInflater.from(getActivity()).inflate(R.layout.popupwindow, null, false);
        PopupWindow popupWindow = new PopupWindow(view, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        TextView txt = view.findViewById(R.id.txtNote);

        startLocation.setLatitude(latitude[i]);
        startLocation.setLongitude(longitude[i]);
        i++;
        if(i<j){
            txt.setText("你到目的地囉\n記得做事...\n下個地點嗎?");
        }else{
            txt.setText("你到目的地囉\n記得做事...\n沒地點了");
        }
        popupWindow.setWidth(700);

        popupWindow.setFocusable(true);
        popupWindow.setOutsideTouchable(false);
        popupWindow.setTouchable(true);

        popupWindow.showAtLocation(v, Gravity.CENTER,0,0);

        Button BTNPopup = (Button) view.findViewById(R.id.PopupCancel);
        BTNPopup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
            }
        });

        Button btnSure = view.findViewById(R.id.Popupsure);
        btnSure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(i<j){

                    mMap.clear();
                    builder = new LatLngBounds.Builder();

                    // 添加起點和目的地的位置
                    builder.include(new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude()));

                    destiantion_LatLng= new LatLng(latitude[i],longitude[i]);
                    mMap.addMarker(new MarkerOptions().position(destiantion_LatLng).title(destinationName[i]));
                    builder.include(new LatLng(latitude[i], longitude[i]));

                    // 構建LatLngBounds對象
                    bounds = builder.build();
                    // 計算將這個邊界框移動到地圖中心所需的偏移量
                    int padding = 100; // 偏移量（以像素為單位）
                    // 移动地图视图到最后已知的位置
                    mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding));

                    double trip_distance = Distance.getDistanceBetweenPointsNew(latitude[i],longitude[i],startLocation.getLatitude(),startLocation.getLongitude())/1000;
                    time = Math.round(trip_distance/4*60);
                    txtTime.setText("目的:"+destinationName[i]+"\n公里為: "+trip_distance+" 公里"+"\n預估時間為: "+time+" 分鐘");
                }else{

                    getActivity().getSupportFragmentManager().popBackStack();
                }
                popupWindow.dismiss();
            }
        });

    }

    @SuppressLint("MissingPermission")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        v = inflater.inflate(R.layout.mapping, container, false);

        SharedViewModel sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);

        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.mapp);

        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

        txtTime = v.findViewById(R.id.txtTime);

        Button btnBack = v.findViewById(R.id.routeCancel);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CreateLocation createFragment = new CreateLocation();
                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.fragment_container, createFragment);
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });

        if (mapFragment != null) {
            mapFragment.getMapAsync(callback);
        }

        for(j=0 ; j<=sharedViewModel.getI() ; j++){
            destinationName[j]=sharedViewModel.getDestinationName(j);
            latitude[j]=sharedViewModel.getLatitude(j);
            longitude[j]=sharedViewModel.getLongitude(j);
        }

        return v;

    }

}