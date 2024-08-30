package com.example.map_clock_api34.home;

import static androidx.core.content.ContextCompat.getSystemService;

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
import android.os.Vibrator;
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
import android.widget.Toast;

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

    PopupWindow popupWindow;
    TextView txtTime;
    View rootView;
    View overlayView;
    Button btnPre, btnNext;
    int nowIndex=0;

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
            txtTime.setText("目的:"+destinationName[0]+"\n剩餘公里為: "+trip_distance+" 公里"+"\n預估走路時間為: "+time+" 分鐘");

        }
    };

    @SuppressLint("MissingPermission")
    private void ShowPopupWindow(int destinationIndex) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.popupwindow_reset_button, null, false);
        popupWindow = new PopupWindow(view, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
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
        BTNPopup.setText("停止震鈴");
        BTNPopup.setOnClickListener(v -> {
            sendBroadcastWithDestinationIndex(3, destinationIndex, 0);
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
            //移除疊加在底下防止點擊其他區域的View
            removeOverlayView();
            popupWindow.dismiss();
            moveCameraAndCalculateTime(destinationIndex);
        });
    }
    //把疊加在底層的View刪掉
    private void removeOverlayView() {
        if (overlayView != null && overlayView.getParent() != null) {
            ((ViewGroup) overlayView.getParent()).removeView(overlayView);
            overlayView = null;
        }
    }

    @SuppressLint("MissingPermission")
    private void setupButton() {
        Button btnBack = rootView.findViewById(R.id.routeCancel);
        btnBack.setOnClickListener(v -> {
            getActivity().getSupportFragmentManager().popBackStack();
        });

        btnPre = rootView.findViewById(R.id.btnPre);
        btnPre.setOnClickListener(v -> {
            if(nowIndex>0){
                nowIndex--;
                if(nowIndex==0){
                    Toast.makeText(getActivity(),"這是第一個地點囉",Toast.LENGTH_SHORT).show();
                }
                sendBroadcastWithDestinationIndex(2, 0,-1);
                userLocation = locationManager.getLastKnownLocation(commandstr);
                double trip_distance = Distance.getDistanceBetweenPointsNew(latitude[nowIndex],longitude[nowIndex], userLocation.getLatitude(), userLocation.getLongitude())/1000;
                double time = Math.round(trip_distance/4*60);
                txtTime.setText("目的:"+destinationName[nowIndex]+"\n剩餘公里為: "+trip_distance+" 公里"+"\n預估走路時間為: "+time+" 分鐘");

                mMap.clear();
                builder = new LatLngBounds.Builder();
                // 添加起點和目的地的位置
                builder.include(new LatLng(userLocation.getLatitude(), userLocation.getLongitude()));

                destiantion_LatLng= new LatLng(latitude[nowIndex],longitude[nowIndex]);
                mMap.addMarker(new MarkerOptions().position(destiantion_LatLng).title(destinationName[nowIndex]));

                builder.include(new LatLng(latitude[nowIndex], longitude[nowIndex]));
                bounds = builder.build();
                // 計算將這個邊界框移動到地圖中心所需的偏移量
                int padding = 300; // 偏移量（以像素為單位）
                mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding));
                updateResetButtonState();
            }

        });

        btnNext = rootView.findViewById(R.id.btnNext);
        btnNext.setOnClickListener(v -> {
            if(nowIndex<sharedViewModel.getLocationCount()){
                nowIndex++;
                if(nowIndex==sharedViewModel.getLocationCount()){
                    Toast.makeText(getActivity(),"這是最後一個地點囉",Toast.LENGTH_SHORT).show();
                }

                sendBroadcastWithDestinationIndex(2, 0,1);
                userLocation = locationManager.getLastKnownLocation(commandstr);
                double trip_distance = Distance.getDistanceBetweenPointsNew(latitude[nowIndex],longitude[nowIndex], userLocation.getLatitude(), userLocation.getLongitude())/1000;
                double time = Math.round(trip_distance/4*60);
                txtTime.setText("目的:"+destinationName[nowIndex]+"\n剩餘公里為: "+trip_distance+" 公里"+"\n預估走路時間為: "+time+" 分鐘");

                mMap.clear();
                builder = new LatLngBounds.Builder();
                // 添加起點和目的地的位置
                builder.include(new LatLng(userLocation.getLatitude(), userLocation.getLongitude()));

                destiantion_LatLng= new LatLng(latitude[nowIndex],longitude[nowIndex]);
                mMap.addMarker(new MarkerOptions().position(destiantion_LatLng).title(destinationName[nowIndex]));

                builder.include(new LatLng(latitude[nowIndex], longitude[nowIndex]));
                bounds = builder.build();
                // 計算將這個邊界框移動到地圖中心所需的偏移量
                int padding = 300; // 偏移量（以像素為單位）
                mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding));
                updateResetButtonState();
            }
        });

    }

    @SuppressLint("MissingPermission")
    private void moveCameraAndCalculateTime(int destinationIndex){
        if(destinationIndex < sharedViewModel.getLocationCount() && nowIndex==destinationIndex){
            //尋找下個地點
            mMap.clear();
            int desNextIndex=destinationIndex+1;
            //傳回LocationService讓她換下個地點
            sendBroadcastWithDestinationIndex(1, desNextIndex,0);

            userLocation = locationManager.getLastKnownLocation(commandstr);

            builder = new LatLngBounds.Builder();
            // 添加起點和目的地的位置
            builder.include(new LatLng(userLocation.getLatitude(), userLocation.getLongitude()));

            destiantion_LatLng= new LatLng(latitude[desNextIndex],longitude[desNextIndex]);
            mMap.addMarker(new MarkerOptions().position(destiantion_LatLng).title(destinationName[desNextIndex]));

            builder.include(new LatLng(latitude[desNextIndex], longitude[desNextIndex]));
            bounds = builder.build();
            // 計算將這個邊界框移動到地圖中心所需的偏移量
            int padding = 300; // 偏移量（以像素為單位）
            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding));
            double trip_distance = Distance.getDistanceBetweenPointsNew(latitude[desNextIndex],longitude[desNextIndex], userLocation.getLatitude(), userLocation.getLongitude())/1000;
            double time = Math.round(trip_distance/4*60);
            txtTime.setText("目的:"+destinationName[desNextIndex]+"\n剩餘公里為: "+trip_distance+" 公里"+"\n預估走路時間為: "+time+" 分鐘");
        }
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
            if ("DESTINATION_UPDATE".equals(intent.getAction())) {

                if (intent.hasExtra("destinationIndex")) {
                    int destinationIndex = intent.getIntExtra("destinationIndex", 0);
                    if (popupWindow != null && popupWindow.isShowing()) {
                        // 如果已經顯示，關閉它
                        sendBroadcastWithDestinationIndex(3, destinationIndex, 0);
                        popupWindow.dismiss();
                        removeOverlayView();
                    }
                    ShowPopupWindow(destinationIndex);
                }

                if (intent.hasExtra("mapInfo")) {
                    String mapInfo = intent.getStringExtra("mapInfo");
                    txtTime.setText(mapInfo);
                }

                if (intent.hasExtra("nowIndex")) {
                    nowIndex = intent.getIntExtra("nowIndex",0);
                }
                if (intent.hasExtra("nextDestination")){
                    int destinationIndex = intent.getIntExtra("nextDestination", 0)-1;
                    mMap.clear();
                    moveCameraAndCalculateTime(destinationIndex);
                }
            }
        }
    };
    //送資料給LocationService
    private void sendBroadcastWithDestinationIndex(int dataType, int destinationIndex, int change) {
        Intent intent = new Intent("DESTINATIONINDEX_UPDATE");

        switch (dataType) {
            case 1:
                intent.putExtra("destinationFinalIndex", destinationIndex);
                break;
            case 2:
                intent.putExtra("destinationIndexChange", change);
                break;
            case 3:
                intent.putExtra("stopVibrateAndRing", "stop");
                break;

        }

        LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(intent);
    }
    //切換Fragment才會觸發
    @Override
    public void onDestroy() {
        super.onDestroy();
        stopLocationUpdates();
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(destinationUpdateReceiver);
    }
    @Override
    public void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(destinationUpdateReceiver,
                new IntentFilter("DESTINATION_UPDATE"));
        updateResetButtonState();

    }
    @Override
    public void onPause() {
        super.onPause();

    }
    private void updateResetButtonState() {
        if (nowIndex == 0) {
            //設置可點擊狀態
            btnPre.setEnabled(false);
            //改變按鈕文字顏色
            btnPre.setTextColor(ContextCompat.getColor(requireContext(), R.color.lightgreen));
            //改變按鈕的Drawable
            btnPre.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.btn_unclickable)); // 設定啟用時的背景顏色
        }else {
            btnPre.setEnabled(true);
            //改變按鈕文字顏色
            btnPre.setTextColor(ContextCompat.getColor(requireContext(), R.color.darkgreen));
            //改變按鈕的Drawable
            btnPre.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.btn_additem));
        }
        if(nowIndex == sharedViewModel.getLocationCount()){
            btnNext.setEnabled(false);
            //改變按鈕文字顏色
            btnNext.setTextColor(ContextCompat.getColor(requireContext(), R.color.lightgreen));
            //改變按鈕的Drawable
            btnNext.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.btn_unclickable)); // 設定啟用時的背景顏色
        }else{
            btnNext.setEnabled(true);
            //改變按鈕文字顏色
            btnNext.setTextColor(ContextCompat.getColor(requireContext(), R.color.darkgreen));
            //改變按鈕的Drawable
            btnNext.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.btn_additem));
        }
    }
}

