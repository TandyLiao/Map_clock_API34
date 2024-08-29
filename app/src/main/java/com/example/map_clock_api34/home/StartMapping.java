package com.example.map_clock_api34.home;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.media.Ringtone;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import android.annotation.SuppressLint;

import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.example.map_clock_api34.Distance;
import com.example.map_clock_api34.R;
import com.example.map_clock_api34.SharedViewModel;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
//設定組新增
import android.content.Intent;

import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.util.Arrays;

public class StartMapping extends Fragment {

    private static final String CHANNEL_ID = "destination_alert_channel";//設定組新增
    private boolean notificationSent = false; // 新增的变量(by設定組)
    private Ringtone mRingtone;

    private LocationManager locationManager;
    private String commandstr = LocationManager.NETWORK_PROVIDER;
    Location userLocation;

    private GoogleMap mMap;
    private SharedViewModel sharedViewModel;
    private String[] destinationName = new String[7];
    private double[] latitude = new double[7];
    private double[] longitude = new double[7];
    private String[] note = new String[7];
    private boolean[] vibrate = new boolean[7];
    private boolean[] ringtone = new boolean[7];
    private int[] notification = new int[7];

    LatLngBounds.Builder builder;
    LatLng destiantion_LatLng;
    LatLngBounds bounds;

    TextView txtTime;
    View rootView;
    View overlayView;

    @SuppressLint("MissingPermission")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.home_start_mapping, container, false);

        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);

        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.mapp);

        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

        if (mapFragment != null) {
            mapFragment.getMapAsync(callback);
        }

        //把ShareViewModel的資料抓近來，稍後送進背景執行
        for (int i = 0; i <= sharedViewModel.getLatitude(i); i++) {
            destinationName[i] = sharedViewModel.getDestinationName(i);
            latitude[i] = sharedViewModel.getLatitude(i);
            longitude[i] = sharedViewModel.getLongitude(i);
            notification[i] = sharedViewModel.getNotification(i);
            vibrate[i] = sharedViewModel.getVibrate(i);
            ringtone[i] = sharedViewModel.getRingtone(i);
            note[i] = sharedViewModel.getNote(i);
        }

        txtTime = rootView.findViewById(R.id.txtTime);

        setupButton();
        //開始背景執行
        startLocationUpdates();

        return rootView;
    }

    private OnMapReadyCallback callback = new OnMapReadyCallback() {

        @SuppressLint("MissingPermission")
        public void onMapReady(GoogleMap googleMap) {
            mMap=googleMap;

            mMap.clear();
            //取得用戶的定位
            userLocation = locationManager.getLastKnownLocation(commandstr);
            //跑出藍色定位點
            mMap.setMyLocationEnabled(true);

            builder = new LatLngBounds.Builder();
            // 添加起點和目的地的位置
            builder.include(new LatLng(userLocation.getLatitude(), userLocation.getLongitude()));

            destiantion_LatLng= new LatLng(latitude[0],longitude[0]);
            mMap.addMarker(new MarkerOptions().position(destiantion_LatLng).title(destinationName[0]));

            builder.include(new LatLng(latitude[0], longitude[0]));
            bounds = builder.build();
            // 計算將這個邊界框移動到地圖中心所需的偏移量
            int padding = 300; // 偏移量（以像素為單位）
            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding));

            //第一次粗略估算到達目的地所需時間
            double trip_distance = Distance.getDistanceBetweenPointsNew(latitude[0],longitude[0], userLocation.getLatitude(), userLocation.getLongitude())/1000;
            double time = Math.round(trip_distance/4*60);
            txtTime.setText("目的:"+destinationName[0]+"\n公里為: "+trip_distance+" 公里"+"\n預估走路時間為: "+time+" 分鐘");

        }
    };

    private void ShowPopupWindow(int destinationIndex) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.popupwindow_reset_button, null, false);
        PopupWindow popupWindow = new PopupWindow(view, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        popupWindow.setWidth(700);
        popupWindow.setFocusable(false);
        popupWindow.setOutsideTouchable(false);
        //讓PopupWindow顯示出來的關鍵句
        popupWindow.showAtLocation(rootView, Gravity.CENTER, 0, 0);
        popupWindow.setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        //疊加View在底下，讓她不會按到底層就跳掉
        overlayView = new View(getContext());
        overlayView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        overlayView.setBackgroundColor(ContextCompat.getColor(getContext(), android.R.color.transparent));
        overlayView.setClickable(true);
        ((ViewGroup) rootView).addView(overlayView);

        TextView title = view.findViewById(R.id.txtNote);
        title.setTextSize(25);
        if(sharedViewModel.getNote(destinationIndex)==null){
            title.setText("目的地-\n"+destinationName[destinationIndex]+"\n即將抵達");

        }
        else{
            title.setText("目的地-\n"+destinationName[destinationIndex]+"\n即將抵達"+"\n記得要做: "+sharedViewModel.getNote(destinationIndex));
        }


        Button BTNPopup = (Button) view.findViewById(R.id.PopupCancel);
        BTNPopup.setOnClickListener(v -> {
            popupWindow.dismiss();
            //移除疊加在底下防止點擊其他區域的View
            removeOverlayView();
        });
        Button btnsure = (Button) view.findViewById(R.id.Popupsure);
        btnsure.setOnClickListener(v -> {
            if(destinationIndex==sharedViewModel.getLocationCount()){
                EndMapping enfFragment = new EndMapping();
                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.fl_container, enfFragment);
                transaction.addToBackStack(null);
                transaction.commit();
                removeOverlayView();
                popupWindow.dismiss();
                return;
            }
            int desNumber=destinationIndex+1;
            sendBroadcastWithDestinationIndex(desNumber);
            //移除疊加在底下防止點擊其他區域的View
            removeOverlayView();
            popupWindow.dismiss();
            mMap.clear();
            if(destinationIndex < sharedViewModel.getLocationCount()){
                builder = new LatLngBounds.Builder();
                // 添加起點和目的地的位置
                builder.include(new LatLng(userLocation.getLatitude(), userLocation.getLongitude()));

                destiantion_LatLng= new LatLng(latitude[destinationIndex+1],longitude[destinationIndex+1]);
                mMap.addMarker(new MarkerOptions().position(destiantion_LatLng).title(destinationName[destinationIndex+1]));

                builder.include(new LatLng(latitude[destinationIndex+1], longitude[destinationIndex+1]));
                bounds = builder.build();
                // 計算將這個邊界框移動到地圖中心所需的偏移量
                int padding = 300; // 偏移量（以像素為單位）
                mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding));
            }
        });
    }
    //把疊加在底層的View刪掉
    private void removeOverlayView() {
        if (overlayView != null && overlayView.getParent() != null) {
            ((ViewGroup) overlayView.getParent()).removeView(overlayView);
            overlayView = null;
        }
    }

    private void setupButton() {
        Button btnBack = rootView.findViewById(R.id.routeCancel);
        btnBack.setOnClickListener(v -> {
            EndMapping enfFragment = new EndMapping();
            FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fl_container, enfFragment);
            transaction.addToBackStack(null);
            transaction.commit();
        });
    }

    //背景執行
    @SuppressLint("MissingPermission")
    private void startLocationUpdates() {
        Intent serviceIntent = new Intent(getActivity(), LocationService.class);
        serviceIntent.putExtra("latitude", latitude);
        serviceIntent.putExtra("longitude", longitude);
        serviceIntent.putExtra("destinationName", destinationName);
        serviceIntent.putExtra("notification", notification);
        serviceIntent.putExtra("vibrate", vibrate);
        serviceIntent.putExtra("ringtone",ringtone);
        serviceIntent.putExtra("note", note);
        ContextCompat.startForegroundService(getActivity(), serviceIntent);

    }

    //停止背景執行的方法
    private void stopLocationUpdates() {
        Intent serviceIntent = new Intent(getActivity(), LocationService.class);
        getActivity().stopService(serviceIntent);
    }
    //收LocationService傳的資料
    private BroadcastReceiver destinationUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int destinationIndex = intent.getIntExtra("destinationIndex", 0);
            ShowPopupWindow(destinationIndex); // 显示 PopupWindow 并传递当前的 destinationIndex
        }
    };
    //送資料給LocationService
    private void sendBroadcastWithDestinationIndex(int destinationIndex) {
        Intent intent = new Intent("DESTINATIONINDEX_UPDATE");
        intent.putExtra("destinationFinalIndex", destinationIndex);
        LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(intent);
    }
    //切換Fragment才會觸發
    @Override
    public void onDestroy() {
        super.onDestroy();
        stopLocationUpdates();
    }
    @Override
    public void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(destinationUpdateReceiver,
                new IntentFilter("DESTINATION_UPDATE"));
    }
    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(destinationUpdateReceiver);
    }
}

