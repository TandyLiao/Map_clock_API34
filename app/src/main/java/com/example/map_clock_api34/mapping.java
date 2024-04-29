package com.example.map_clock_api34;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import android.annotation.SuppressLint;
import android.content.Context;

import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

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
    private String[] destinationName = new String[7];
    private double[] latitude = new double[7];
    private double[] longitude = new double[7];
    int j;
    private OnMapReadyCallback callback = new OnMapReadyCallback() {

        @SuppressLint("MissingPermission")
        public void onMapReady(GoogleMap googleMap) {
            mMap=googleMap;

            //一直更新目前位置
            //locationManager.requestLocationUpdates(commandStr,1000,0,locationListener);

            //取得最後的定位位置
            lastLocation = locationManager.getLastKnownLocation(commandstr);

            LatLng[] destiantion_LatLng= new LatLng[7];

            for(int k=0 ; k<=j ; k++){
                destiantion_LatLng[k]= new LatLng(latitude[k],longitude[k]);
                //在地圖上標示Marker
                Marker destiantion_Marker=mMap.addMarker(new MarkerOptions().position(destiantion_LatLng[k]).title(destinationName[0]));
            }


            //跑出藍色定位點
            mMap.setMyLocationEnabled(true);

            LatLngBounds.Builder builder = new LatLngBounds.Builder();

            // 添加起點和目的地的位置
            builder.include(new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude()));
            builder.include(new LatLng(latitude[0], longitude[0]));

            // 構建LatLngBounds對象
            LatLngBounds bounds = builder.build();

            // 計算將這個邊界框移動到地圖中心所需的偏移量
            int padding = 100; // 偏移量（以像素為單位）
            // 移动地图视图到最后已知的位置
            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding));

        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.mapping, container, false);

        SharedViewModel sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);

        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.mapp);

        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

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

