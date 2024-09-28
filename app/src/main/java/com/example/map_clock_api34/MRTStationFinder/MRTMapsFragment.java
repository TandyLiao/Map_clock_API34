package com.example.map_clock_api34.MRTStationFinder;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.map_clock_api34.CreateLocation.SelectPlaceFragment;
import com.example.map_clock_api34.R;
import com.example.map_clock_api34.SharedViewModel;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MRTMapsFragment extends Fragment {

    private LocationManager locationManager;
    private Location lastLocation; // 儲存最後的定位資訊
    private View rootView, overlayView;
    private DrawerLayout drawerLayout;
    private TextView MRTTitle1;
    private TextView MRTTitle2;
    private TextView MRTTitle3;
    private TextView MRTtxtTime;


    private SharedViewModel sharedViewModel;
    private GoogleMap googleMap;

    List<String> shortestRoute;
    int shortestTime;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.mrt_mrt_maps_fragment, container, false);

        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        MRTTitle1 = rootView.findViewById(R.id.MRTTitle1);
        MRTTitle2 = rootView.findViewById(R.id.MRTTitle2);
        MRTTitle3 = rootView.findViewById(R.id.MRTTitle3);

        MRTtxtTime = rootView.findViewById(R.id.MRTTime);

        drawerLayout = getActivity().findViewById(R.id.drawerLayout);
        if (drawerLayout != null) {
            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        }

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(callback); // 當地圖準備好時進行回調
        }

        ActionBar actionBar = ((AppCompatActivity) requireActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        return rootView;
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ImageView returnButton = getActivity().findViewById(R.id.returnpage);
        returnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 點擊返回按鈕時返回上一頁
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });
    }
    // 當地圖準備好時的回調函數
    private OnMapReadyCallback callback = new OnMapReadyCallback() {
        @SuppressLint("MissingPermission")
        public void onMapReady(GoogleMap googleMap) {
            MRTMapsFragment.this.googleMap = googleMap; // 初始化地圖物件
            MRTMapsFragment.this.googleMap.setMyLocationEnabled(true);  // 啟用藍色定位點

            String commandstr = LocationManager.GPS_PROVIDER;   // 用於定位的 GPS 提供
            lastLocation = locationManager.getLastKnownLocation(commandstr);    // 取得最後的定位資訊

            if (lastLocation != null) {
                MRTMapsFragment.this.googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                        new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude()), 15));
            } else {
                makeToast("無法取得目前位置訊息，請查看您的GPS設備", 1000);
            }

            MRTMapsFragment.this.googleMap.setOnMarkerClickListener(marker -> {
                return true; // 防止地圖默認的點擊行為
            });

            // 查找最短路線並顯示標記
            findAndDisplayShortestRoute();
            ShowInformationPopupWindow(shortestRoute, shortestTime);
        }
    };

    private void findAndDisplayShortestRoute() {
        StaionRecord userMRTStation = new StaionRecord();
        StaionRecord destinationMRTStation = new StaionRecord();

        // 查找最近的捷運站
        FindMRTStationStop findMrtStationStop = new FindMRTStationStop(getActivity(), sharedViewModel.getNowLantitude(), sharedViewModel.getNowLontitude(), userMRTStation);
        findMrtStationStop.findNearestStation();

        FindMRTStationStop StationDistanceCalculator = new FindMRTStationStop(getActivity(), sharedViewModel.getLatitude(0), sharedViewModel.getLongitude(0), destinationMRTStation);
        StationDistanceCalculator.findNearestStation();

        Log.d("MRT", userMRTStation.toString() + " " + destinationMRTStation);

        // MRTRouteFinder 找到所有路線
        MRTRouteFinder mrtRouteFinder = new MRTRouteFinder(getActivity());
        List<String> allRoutes = mrtRouteFinder.findAllRoutesWithTransfer(userMRTStation.getName(), destinationMRTStation.getName());

        // 用來保存所有處理後的路線資料
        List<List<String>> allProcessedRoutes = new ArrayList<>();

        // 遍歷所有路線並處理
        for (String route : allRoutes) {
            route = route.replace("[", "").replace("]", "");
            String[] splitRoute = route.split(",");
            String[] clearRoute = new String[splitRoute.length + 1];
            clearRoute[0] = userMRTStation.getName();
            System.arraycopy(splitRoute, 0, clearRoute, 1, splitRoute.length);
            List<String> processedRoute = Arrays.asList(clearRoute);
            allProcessedRoutes.add(processedRoute);
            String result = String.join(", ", processedRoute);
            Log.d("MRT_ROUTE", "處理後的路線: " + result);
        }

        MRTTime mrtTime = new MRTTime(getActivity());
        mrtTime.readCsvFile();

        shortestRoute = mrtTime.findShortestRoute(allProcessedRoutes);
        if (shortestRoute != null) {
            shortestTime = mrtTime.calculateRouteTime(shortestRoute);
            Log.d("MRTShortestRoute", "最短路線: " + shortestRoute + ", 總時間: " + shortestTime + " 秒");
            displayRouteOnMap(shortestRoute);
        } else {
            Log.d("MRTShortestRoute", "未找到任何路線");
        }
    }

    private void displayRouteOnMap(List<String> route) {
        MRTStationLocator mrtStationLocator = new MRTStationLocator(getActivity());
        String lastStation = "";

        LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();  // 用來包含所有地標

        for (int i = 0; i < route.size(); i += 2) {
            String stationName = route.get(i).trim();
            if (!stationName.equals(lastStation)) {
                double[] coordinates = mrtStationLocator.findStationCoordinates(stationName);
                if (coordinates != null) {
                    LatLng stationLatLng = new LatLng(coordinates[0], coordinates[1]);
                    boundsBuilder.include(stationLatLng);  // 把標記加入邊界

                    // 使用自訂的標記圖示
                    Bitmap markerIcon;

                    if (stationName.equals(route.get(0))) {
                        // 起點
                        markerIcon = createCustomMarker(getActivity(), R.drawable.custom_marker, stationName, false);
                    } else if (stationName.equals(route.get(route.size() - 1))) {
                        // 終點
                        markerIcon = createCustomMarker(getActivity(), R.drawable.custom_destination_marker, stationName, true);
                    } else {
                        // 轉乘站或普通站點
                        markerIcon = createCustomMarker(getActivity(), R.drawable.custom_destination_near_marker, stationName, false);
                    }

                    // 添加自訂的標記到地圖
                    googleMap.addMarker(new MarkerOptions()
                            .position(stationLatLng)
                            .title(stationName)
                            .icon(BitmapDescriptorFactory.fromBitmap(markerIcon))); // 使用自訂的圖示
                }
                lastStation = stationName;
            }
        }

        // 調整地圖的視圖，讓所有標記都能顯示出來
        LatLngBounds bounds = boundsBuilder.build();
        int padding = 300;  // 給地圖添加一些內邊距
        googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding));

        // 設定標記點擊事件
        googleMap.setOnMarkerClickListener(marker -> {
            String markerTitle = marker.getTitle();
            LatLng markerPosition = marker.getPosition();

            // 檢查點擊的是否為起點或轉乘站 (終點不顯示)
            if (markerTitle != null && !markerTitle.equals(route.get(route.size() - 1))) {
                // 顯示彈出視窗詢問是否將該站點加入行程
                ShowPopupWindow(markerTitle, markerPosition);
                return true;  // 返回 true 表示已處理點擊事件
            }
            return false;  // 返回 false 讓地圖處理默認行為
        });

    }
    // 顯示彈出視窗，詢問是否將該站點加入行程
    private void ShowPopupWindow(String stopName, LatLng stopLatLng) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.popupwindow_select_busstop, null, false);
        PopupWindow popupWindow = new PopupWindow(view, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        popupWindow.setWidth(800);
        popupWindow.setFocusable(false);
        popupWindow.setOutsideTouchable(false);

        // 顯示彈出視窗
        popupWindow.showAtLocation(rootView, Gravity.CENTER, 0, 0);
        popupWindow.setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        // 疊加透明遮罩，防止點擊其他區域
        overlayView = new View(getContext());
        overlayView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        overlayView.setBackgroundColor(ContextCompat.getColor(getContext(),R.color.transparent_black));
        overlayView.setClickable(true);
        ((ViewGroup) rootView).addView(overlayView);

        TextView showNotification = (TextView) view.findViewById(R.id.txtNote); // 取得彈窗的文字視圖
        showNotification.setTextSize(20);
        showNotification.setText(stopName + "\n"+"要加入行程中嗎?"); // 顯示站點名稱和提示

        Button BTNPopup = (Button) view.findViewById(R.id.PopupCancel); // 取消按鈕
        BTNPopup.setOnClickListener(v -> {
            popupWindow.dismiss(); // 點擊取消按鈕時關閉彈窗
            removeOverlayView(); // 移除透明遮罩
        });

        Button btnsure = (Button) view.findViewById(R.id.Popupsure); // 確認按鈕
        btnsure.setOnClickListener(v -> {
            // 檢查是否已滿行程
            if(sharedViewModel.getLocationCount()<7){
                SelectPlaceFragment selectPlaceFragment = new SelectPlaceFragment();
                // 取得該站點的地區和城市名稱
                String busArea = selectPlaceFragment.getAreaNameCustom(getContext(), stopLatLng.latitude, stopLatLng.longitude);
                String busCity = selectPlaceFragment.getCityNameCustom(getContext(), stopLatLng.latitude, stopLatLng.longitude);

                // 將站點加入目的地
                sharedViewModel.setFirstDestination("捷運站：" + stopName, busArea, busCity, stopLatLng.latitude, stopLatLng.longitude);
                makeToast("已加入路線",1000);
            } else {
                // 如果行程已滿，顯示提示
                makeToast("路線已滿，請刪除後再試",1000);
            }
            removeOverlayView(); // 移除透明遮罩
            popupWindow.dismiss(); // 關閉彈窗
        });
    }
    // 顯示彈出視窗，顯示捷運路線資訊
    // 顯示彈出視窗，顯示捷運路線資訊
    private void ShowInformationPopupWindow(List<String> route, int totalTime) {

        // 生成路線資訊
        StringBuilder routeInfo = new StringBuilder();
        String currentLine = route.get(1).trim();  // 起始的路線 (例如 R線)
        String startStation = route.get(0).trim(); // 起點站
        String previousStation = startStation;
        List<String> transferStations = new ArrayList<>();//放轉乘站


        // 從第二站開始迭代，i = 2 是第二站，跳過起點站
        for (int i = 2; i < route.size(); i += 2) {
            String station = route.get(i).trim(); // 當前站點

            // 如果當前站點和前一站點相同，則跳過，避免重複記錄
            if (station.equals(previousStation)) {
                continue; // 忽略重複的站點
            }

            // 確保 i + 1 不超出範圍，並且獲取下一段的路線
            if (i + 1 < route.size()) {
                String line = route.get(i + 1).trim(); // 下一段路線

                // 如果路線不同，表示需要轉乘
                if (!line.equals(currentLine)) {
                    transferStations.add(station);
                    currentLine = line;
                } else {
                    // 如果路線相同，表示這段路線是連續的
                    MRTTitle2.setText("轉乘站: "+station);
                }
            }
            previousStation = station;
        }

        if (!transferStations.isEmpty()) {
            StringBuilder transferList = new StringBuilder("轉乘站列表: ");
            for (String transferStation : transferStations) {
                transferList.append(transferStation).append("，");
            }
            // 移除最後一個逗號
            if (transferList.length() > 0) {
                transferList.setLength(transferList.length() - 1); // 刪除最後一個逗號
            }
            // 顯示轉乘站列表
            MRTTitle2.setText(transferList.toString());
        }

        // 終點站處理
        MRTTitle3.setText("終點站: "+previousStation);

        // 計算乘車時間
        int minutes = totalTime / 60;

        // 設定起點+搭乘時間
        if (MRTTitle1 != null) {
            MRTTitle1.setText("起點站: "+startStation);
            MRTtxtTime.setText("\n乘車時間約 "+minutes+" 分鐘");
        } else {
            Log.d("MRTTitle", "MRTTitle is null");
        }
    }


    // 移除疊加在底層的透明遮罩
    private void removeOverlayView() {
        if (overlayView != null && overlayView.getParent() != null) {
            ((ViewGroup) overlayView.getParent()).removeView(overlayView);
            overlayView = null;
        }
    }

    // 建立自訂的標記圖示
    private Bitmap createCustomMarker(Context context, @DrawableRes int resource, String title, Boolean isDestination) {
        View markerView = LayoutInflater.from(context).inflate(R.layout.bus_advice_bus_custom_marker, null);

        ImageView markerImage = markerView.findViewById(R.id.marker_image);
        markerImage.setImageResource(resource);

        TextView markerText = markerView.findViewById(R.id.marker_text);
        markerText.setText(title);
        if(isDestination) markerText.setBackgroundResource(R.drawable.btn_unclickable); // 如果是目的地，設定背景樣式

        markerView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        markerView.layout(0, 0, markerView.getMeasuredWidth(), markerView.getMeasuredHeight());

        Bitmap bitmap = Bitmap.createBitmap(markerView.getMeasuredWidth(), markerView.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        markerView.draw(canvas);

        return bitmap;
    }

    public void makeToast(String message, int durationInMillis) {
        Toast toast = Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT);
        toast.show();
        new Handler().postDelayed(toast::cancel, durationInMillis);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        drawerLayout = getActivity().findViewById(R.id.drawerLayout);
        if (drawerLayout != null) {
            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        }
    }
}
