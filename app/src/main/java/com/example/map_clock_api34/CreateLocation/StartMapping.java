package com.example.map_clock_api34.CreateLocation;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.graphics.drawable.ColorDrawable;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import android.annotation.SuppressLint;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
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
import android.content.Intent;
import android.widget.Toast;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class StartMapping extends Fragment {

    // 位置服務變數
    private LocationManager locationManager;
    private String commandstr = LocationManager.NETWORK_PROVIDER;
    private Location userLocation;

    // Google 地圖相關變數
    private GoogleMap mMap;
    private SharedViewModel sharedViewModel;
    private String[] destinationName = new String[7];
    private double[] latitude = new double[7];
    private double[] longitude = new double[7];
    private String[] note = new String[7];
    private boolean[] vibrate = new boolean[7];
    private boolean[] ringtone = new boolean[7];
    private int[] notification = new int[7];

    // 地圖邊界與相關視圖變數
    private LatLngBounds.Builder builder;
    private LatLng destiantion_LatLng;
    private LatLngBounds bounds;
    private PopupWindow popupWindow;
    private TextView txtTime;
    private TextView locationTitle;
    private View rootView;
    private View overlayView;
    private Button btnPre, btnNext;
    private int nowIndex = 0;

    @SuppressLint("MissingPermission")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.home_start_mapping, container, false);

        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);

        // 初始化地圖片段
        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.mapp);

        // 取得 LocationManager 來定位
        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        userLocation = locationManager.getLastKnownLocation(commandstr);

        // 檢查是否能取得當前位置，若無法，顯示錯誤訊息
        if (userLocation == null) {
            makeToast("無法取得當前位置，請查看您的GPS設備",1000);
            getActivity().getSupportFragmentManager().popBackStack();
            return null;
        }

        // 設定地圖
        if (mapFragment != null) {
            mapFragment.getMapAsync(callback);
        }

        // 從 ShareViewModel 抓取資料
        for (int i = 0; i <= sharedViewModel.getLatitude(i); i++) {
            destinationName[i] = sharedViewModel.getDestinationName(i);
            latitude[i] = sharedViewModel.getLatitude(i);
            longitude[i] = sharedViewModel.getLongitude(i);
            notification[i] = sharedViewModel.getNotification(i);
            vibrate[i] = sharedViewModel.getVibrate(i);
            ringtone[i] = sharedViewModel.getRingtone(i);
            note[i] = sharedViewModel.getNote(i);
        }

        // 設定顯示的時間和位置標題
        txtTime = rootView.findViewById(R.id.txtTime);
        locationTitle = rootView.findViewById(R.id.locationTitle);

        // 設定按鈕
        setupButton();

        // 開始背景執行位置更新
        startLocationUpdates();

        return rootView;
    }

    // 地圖準備好後的回呼
    private OnMapReadyCallback callback = new OnMapReadyCallback() {

        @SuppressLint("MissingPermission")
        public void onMapReady(GoogleMap googleMap) {
            mMap = googleMap;

            mMap.clear();
            userLocation = locationManager.getLastKnownLocation(commandstr);
            mMap.setMyLocationEnabled(true);

            // 初始化地圖邊界
            builder = new LatLngBounds.Builder();
            builder.include(new LatLng(userLocation.getLatitude(), userLocation.getLongitude()));

            destiantion_LatLng = new LatLng(latitude[0], longitude[0]);
            mMap.addMarker(new MarkerOptions().position(destiantion_LatLng).title(destinationName[0]));

            builder.include(new LatLng(latitude[0], longitude[0]));
            bounds = builder.build();
            int padding = 300;  // 設置地圖邊界的偏移量
            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding));

            // 粗略估算到達目的地所需時間
            double trip_distance = Distance.getDistanceBetweenPointsNew(latitude[0], longitude[0], userLocation.getLatitude(), userLocation.getLongitude()) / 1000;
            double time = Math.round(trip_distance / 4 * 60);
            locationTitle.setText("地點:" + destinationName[0]);
            txtTime.setText("\n剩餘公里為: " + trip_distance + " 公里" + "\n預估走路時間為: " + time + " 分鐘");
        }
    };

    // 顯示 PopupWindow 的方法
    @SuppressLint("MissingPermission")
    private void ShowPopupWindow(int destinationIndex) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.popupwindow_reset_button, null, false);
        popupWindow = new PopupWindow(view, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        popupWindow.setWidth(700);
        popupWindow.setFocusable(false);
        popupWindow.setOutsideTouchable(false);
        popupWindow.showAtLocation(rootView, Gravity.CENTER, 0, 0);
        popupWindow.setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        // 在底部加上覆蓋層，防止點擊其他區域
        overlayView = new View(getContext());
        overlayView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        overlayView.setBackgroundColor(ContextCompat.getColor(getContext(), android.R.color.transparent));
        overlayView.setClickable(true);
        ((ViewGroup) rootView).addView(overlayView);

        // 設置 PopupWindow 的標題
        TextView title = view.findViewById(R.id.txtNote);
        title.setTextSize(18);

        if (sharedViewModel.getNote(destinationIndex) == null) {
            title.setText("即將抵達：\n" + destinationName[destinationIndex]);
        } else {
            title.setText("即將抵達：\n" + destinationName[destinationIndex] + "\n\n代辦事項：\n" + sharedViewModel.getNote(destinationIndex));
        }

        // 設定停止震鈴按鈕
        Button BTNPopup = view.findViewById(R.id.PopupCancel);
        BTNPopup.setText("停止震鈴");
        BTNPopup.setOnClickListener(v -> {
            sendBroadcastWithDestinationIndex(3, destinationIndex, 0);
        });

        // 確認按鈕處理
        Button btnsure = view.findViewById(R.id.Popupsure);
        btnsure.setOnClickListener(v -> {
            sendBroadcastWithDestinationIndex(5, 0, -1);
            if (destinationIndex == sharedViewModel.getLocationCount()) {
                sendBroadcastWithDestinationIndex(3, destinationIndex, 0);

                EndMapping enfFragment = new EndMapping();
                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.fl_container, enfFragment);
                transaction.addToBackStack(null);
                transaction.commit();

                removeOverlayView();
                popupWindow.dismiss();
                return;
            }
            removeOverlayView();  // 移除覆蓋層
            popupWindow.dismiss();
            moveCameraAndCalculateTime(destinationIndex);
            sendBroadcastWithDestinationIndex(3, destinationIndex, 0);
        });
    }

    // 移除疊加在底層的View
    private void removeOverlayView() {
        if (overlayView != null && overlayView.getParent() != null) {
            ((ViewGroup) overlayView.getParent()).removeView(overlayView);
            overlayView = null;
        }
    }

    // 設定按鈕的處理邏輯
    @SuppressLint("MissingPermission")
    private void setupButton() {
        Button btnBack = rootView.findViewById(R.id.routeCancel);
        btnBack.setOnClickListener(v -> {
            sendBroadcastWithDestinationIndex(5, 0, -1);

            EndMapping endMapping = new EndMapping();
            FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fl_container, endMapping);
            transaction.addToBackStack(null);
            transaction.commit();
        });

        // 設定上一路線的按紐
        btnPre = rootView.findViewById(R.id.btnPre);
        btnPre.setOnClickListener(v -> {
            if (nowIndex > 0) {
                nowIndex--;
                sendBroadcastWithDestinationIndex(2, 0, -1);

                // 在次計算預估時間
                userLocation = locationManager.getLastKnownLocation(commandstr);
                double trip_distance = Distance.getDistanceBetweenPointsNew(latitude[nowIndex], longitude[nowIndex], userLocation.getLatitude(), userLocation.getLongitude()) / 1000;
                double time = Math.round(trip_distance / 4 * 60);
                locationTitle.setText("地點:" + destinationName[nowIndex]);
                txtTime.setText("\n剩餘公里為: " + trip_distance + " 公里" + "\n預估走路時間為: " + time + " 分鐘");

                // 重新移動Map視角
                mMap.clear();
                builder = new LatLngBounds.Builder();
                builder.include(new LatLng(userLocation.getLatitude(), userLocation.getLongitude()));
                destiantion_LatLng = new LatLng(latitude[nowIndex], longitude[nowIndex]);
                //加入新地點的地標
                mMap.addMarker(new MarkerOptions().position(destiantion_LatLng).title(destinationName[nowIndex]));
                builder.include(new LatLng(latitude[nowIndex], longitude[nowIndex]));
                bounds = builder.build();
                int padding = 300;
                mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding));
            }
            if (nowIndex == 0) {
                makeToast("這是第一個地點囉",1000);
            }
        });

        // 設定下一地點的按紐
        btnNext = rootView.findViewById(R.id.btnNext);
        btnNext.setOnClickListener(v -> {
            if (nowIndex < sharedViewModel.getLocationCount()) {
                nowIndex++;
                sendBroadcastWithDestinationIndex(2, 0, 1);

                //重新預估時間
                userLocation = locationManager.getLastKnownLocation(commandstr);
                double trip_distance = Distance.getDistanceBetweenPointsNew(latitude[nowIndex], longitude[nowIndex], userLocation.getLatitude(), userLocation.getLongitude()) / 1000;
                double time = Math.round(trip_distance / 4 * 60);
                locationTitle.setText("地點:" + destinationName[nowIndex]);
                txtTime.setText("\n剩餘公里為: " + trip_distance + " 公里" + "\n預估走路時間為: " + time + " 分鐘");

                // 重新移動Map視角
                mMap.clear();
                builder = new LatLngBounds.Builder();
                builder.include(new LatLng(userLocation.getLatitude(), userLocation.getLongitude()));
                destiantion_LatLng = new LatLng(latitude[nowIndex], longitude[nowIndex]);
                //加入新地點的地標
                mMap.addMarker(new MarkerOptions().position(destiantion_LatLng).title(destinationName[nowIndex]));
                builder.include(new LatLng(latitude[nowIndex], longitude[nowIndex]));
                bounds = builder.build();
                int padding = 300;
                mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding));
            }
            if (nowIndex == sharedViewModel.getLocationCount()) {
                makeToast("這是最後一個地點囉",1000);
            }
        });
    }

    // 更新地圖並計算到達時間
    @SuppressLint("MissingPermission")
    private void moveCameraAndCalculateTime(int destinationIndex) {
        if (destinationIndex < sharedViewModel.getLocationCount() && nowIndex == destinationIndex) {
            mMap.clear();
            int desNextIndex = destinationIndex + 1;

            // 移動Map視角
            sendBroadcastWithDestinationIndex(1, desNextIndex, 0);
            userLocation = locationManager.getLastKnownLocation(commandstr);
            builder = new LatLngBounds.Builder();
            builder.include(new LatLng(userLocation.getLatitude(), userLocation.getLongitude()));
            destiantion_LatLng = new LatLng(latitude[desNextIndex], longitude[desNextIndex]);
            mMap.addMarker(new MarkerOptions().position(destiantion_LatLng).title(destinationName[desNextIndex]));
            builder.include(new LatLng(latitude[desNextIndex], longitude[desNextIndex]));
            bounds = builder.build();
            int padding = 300;
            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding));

            // 更新預估時間
            double trip_distance = Distance.getDistanceBetweenPointsNew(latitude[desNextIndex], longitude[desNextIndex], userLocation.getLatitude(), userLocation.getLongitude()) / 1000;
            double time = Math.round(trip_distance / 4 * 60);
            locationTitle.setText("地點:" + destinationName[desNextIndex]);
            txtTime.setText("\n剩餘公里為: " + trip_distance + " 公里" + "\n預估時間為: " + time + " 分鐘");
        }
    }

    // 開始背景位置更新
    @SuppressLint("MissingPermission")
    private void startLocationUpdates() {
        Intent serviceIntent = new Intent(getActivity(), LocationService.class);
        serviceIntent.putExtra("latitude", latitude);
        serviceIntent.putExtra("longitude", longitude);
        serviceIntent.putExtra("destinationName", destinationName);
        serviceIntent.putExtra("notification", notification);
        serviceIntent.putExtra("vibrate", vibrate);
        serviceIntent.putExtra("ringtone", ringtone);
        serviceIntent.putExtra("note", note);
        ContextCompat.startForegroundService(getActivity(), serviceIntent);
    }

    // 停止位置更新服務
    private void stopLocationUpdates() {
        Intent serviceIntent = new Intent(getActivity(), LocationService.class);
        getActivity().stopService(serviceIntent);
    }

    // 接收來自 LocationService 的廣播
    private BroadcastReceiver destinationUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if ("DESTINATION_UPDATE".equals(intent.getAction())) {

                if (intent.hasExtra("destinationIndex")) {
                    int destinationIndex = intent.getIntExtra("destinationIndex", 0);
                    if (popupWindow != null && popupWindow.isShowing()) {
                        sendBroadcastWithDestinationIndex(3, destinationIndex, 0);
                        popupWindow.dismiss();
                        removeOverlayView();
                    }
                    ShowPopupWindow(destinationIndex);
                    sendBroadcastWithDestinationIndex(4, destinationIndex, 0);
                }

                if (intent.hasExtra("mapInfo")) {
                    String mapInfo = intent.getStringExtra("mapInfo");
                    txtTime.setText(mapInfo);
                }

                if (intent.hasExtra("nowIndex")) {
                    nowIndex = intent.getIntExtra("nowIndex", 0);
                }
                if (intent.hasExtra("nextDestination")) {
                    int destinationIndex = intent.getIntExtra("nextDestination", 0) - 1;
                    mMap.clear();
                    moveCameraAndCalculateTime(destinationIndex);
                }
            }
        }
    };

    // 廣播資料到 LocationService
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
            case 4:
                intent.putExtra("startVibrateAndRing", destinationIndex);
                break;
            case 5:
                intent.putExtra("destroy notification", 0);
                break;
        }

        LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(intent);
    }

    // 當 Fragment 被銷毀時觸發
    @Override
    public void onDestroy() {
        super.onDestroy();
        stopLocationUpdates();
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(destinationUpdateReceiver);
    }

    // 當 Fragment 重回前景時觸發
    @Override
    public void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(destinationUpdateReceiver,
                new IntentFilter("DESTINATION_UPDATE"));
    }

    // 顯示自定義的 Toast
    public void makeToast(String message, int durationInMillis) {
        Toast toast = Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT);
        toast.show();
        new Handler().postDelayed(toast::cancel, durationInMillis);
    }
}
