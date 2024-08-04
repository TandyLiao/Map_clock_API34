package com.example.map_clock_api34.BusAdvice;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import androidx.lifecycle.ViewModelProvider;

import com.example.map_clock_api34.R;
import com.example.map_clock_api34.SharedViewModel;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class busMapsFragment extends Fragment implements BusStationFinderHelper.BusStationFinderCallback {

    private Toolbar toolbar;
    private SharedViewModel sharedViewModel;
    private LocationManager locationManager;
    private String commandstr = LocationManager.GPS_PROVIDER;
    Location lastLocation;
    View overlayView;
    View rootView;
    TextView notification;

    private GoogleMap mMap;

    private BusStationFinderHelper stationFinder;

    private List<BusStationFinderHelper.BusStation> secondNearByStop;
    private List<BusStationFinderHelper.BusStation> secondDesStop;
    private Set<Marker> nearbyMarkers = new HashSet<>();
    private Set<Marker> destinationMarkers = new HashSet<>();

    private OnMapReadyCallback callback = new OnMapReadyCallback() {

        @SuppressLint("MissingPermission")
        public void onMapReady(GoogleMap googleMap) {
            mMap = googleMap;

            // 显示蓝色定位点
            mMap.setMyLocationEnabled(true);
            // 获取最后的定位位置
            lastLocation = locationManager.getLastKnownLocation(commandstr);

            // 移动地图视图到最后已知的位置
            if (lastLocation != null) {
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude()), 15));
            } else {
                Toast.makeText(getContext(), "无法取得当前位置信息，请查看您的GPS设备", Toast.LENGTH_LONG).show();
            }

            // 设置标记点击事件
            mMap.setOnMarkerClickListener(marker -> {
                if (nearbyMarkers.contains(marker)) {
                    String stopName = marker.getTitle();
                    LatLng position = marker.getPosition();
                    // 查找能搭乘的公车路线
                    findBusRoutesForStop(stopName, position.latitude, position.longitude);
                }else{
                    ShowPopupWindow(marker.getTitle(), marker.getPosition());
                    Log.d("Hello",marker.getPosition().toString());
                }

                return true;
            });
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.home_bus_maps_fragment, container, false);

        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);

        notification = rootView.findViewById(R.id.textinfo);

        stationFinder = new BusStationFinderHelper(getActivity(), sharedViewModel, this);
        stationFinder.setToastCallback(this::onToastShown);
        stationFinder.findNearbyStations(rootView);

        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);

        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

        if (mapFragment != null) {
            mapFragment.getMapAsync(callback);
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

        toolbar = getActivity().findViewById(R.id.toolbar);

        ImageView returnButton = getActivity().findViewById(R.id.returnpage);
        returnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });
    }

    private void displayBusStopsOnMap(List<BusStationFinderHelper.BusStation> busStops, float color, boolean transparent, Set<Marker> markerSet) {
        if (busStops == null || busStops.isEmpty()) {
            return;
        }

        for (BusStationFinderHelper.BusStation station : busStops) {
            LatLng position = new LatLng(station.getStopLat(), station.getStopLon());
            Marker marker = mMap.addMarker(new MarkerOptions()
                    .position(position)
                    .title(station.getStopName())
                    .icon(BitmapDescriptorFactory.defaultMarker(color))
                    .alpha(transparent ? 0.3f : 1.0f)); // 透明度设为0.3表示透明
            markerSet.add(marker);
        }
    }

    private void findBusRoutesForStop(String stopName, double lat, double lon) {
        if (stopName == null || stopName.isEmpty()) {
            return;
        }

        for (BusStationFinderHelper.BusStation station : secondNearByStop) {
            if (station.getStopName().equals(stopName.split(" \\(")[0]) && Math.abs(station.getStopLat() - lat) < 0.00001 && Math.abs(station.getStopLon() - lon) < 0.00001) {
                showRoutesDialog(stopName, station);
                break;
            }
        }
    }

    private void showRoutesDialog(String stopName, BusStationFinderHelper.BusStation station) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(stopName + "站 - 請選擇可搭路線");

        final Map<String, Map<BusStationFinderHelper.BusStation.LatLng, String>> routes = station.getRoutes();
        final List<String> routeNames = new ArrayList<>(routes.keySet());

        // 確認數據完整性
        if (routeNames.isEmpty()) {
            Log.e("BusStationFinderHelper", "No routes available for stop: " + station.getStopName());
            Toast.makeText(getContext(), "沒有可用的路線，請稍候再試", Toast.LENGTH_SHORT).show();
            return;
        }

        // 获取每条路线的到站时间信息
        List<String> routeDetails = new ArrayList<>();
        for (String routeName : routeNames) {
            String arrivalTime = station.getArrivalTimes().get(routeName);
            routeDetails.add(routeName + " - 到站時間: " + arrivalTime);
        }

        Log.d("BusStationFinderHelper", "Routes for stop " + station.getStopName() + ": " + routeDetails);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, routeDetails);
        builder.setAdapter(adapter, (dialog, which) -> {
            String selectedRoute = routeNames.get(which);
            highlightRouteDestinations(routes.get(selectedRoute));
        });

        builder.setNegativeButton("取消", (dialog, which) -> dialog.dismiss());

        builder.show();
    }

    private void resetDestinationMarkers() {
        for (Marker marker : destinationMarkers) {
            marker.remove();
        }
        destinationMarkers.clear();
    }
    private void highlightRouteDestinations(Map<BusStationFinderHelper.BusStation.LatLng, String> destinations) {
        resetDestinationMarkers();
        LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();

        for (Map.Entry<BusStationFinderHelper.BusStation.LatLng, String> entry : destinations.entrySet()) {
            Log.d("highlightRouteDestinations", "Destination stop: " + entry.getValue() + " at " + entry.getKey().getLat() + ", " + entry.getKey().getLon());
            LatLng position = new LatLng(entry.getKey().getLat(), entry.getKey().getLon());
            Marker marker = mMap.addMarker(new MarkerOptions()
                    .position(position)
                    .title(entry.getValue())  // 直接顯示目的地站牌名稱
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                    .alpha(1.0f));
            destinationMarkers.add(marker);
            boundsBuilder.include(position);
        }

        for (Marker marker : nearbyMarkers) {
            boundsBuilder.include(marker.getPosition());
        }

        LatLngBounds bounds = boundsBuilder.build();
        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 300));
    }
    @Override
    public void onBusStationsFound(List<BusStationFinderHelper.BusStation> nearbyStops) {
        secondNearByStop = nearbyStops;
        if (mMap != null) {
            displayBusStopsOnMap(secondNearByStop, BitmapDescriptorFactory.HUE_BLUE, false, nearbyMarkers);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        // 停止更新
        stationFinder.stopUpdating();
    }

    //按重置紐後PopupWindow跳出來的設定
    private void ShowPopupWindow(String stopName, LatLng stopLatLng) {
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

        TextView showNotification = (TextView) view.findViewById(R.id.txtNote);
        showNotification.setTextSize(15);
        showNotification.setText(stopName +" - 要加入行程中嗎?");

        Button BTNPopup = (Button) view.findViewById(R.id.PopupCancel);
        BTNPopup.setOnClickListener(v -> {
            popupWindow.dismiss();
            //移除疊加在底下防止點擊其他區域的View
            removeOverlayView();
        });
        Button btnsure = (Button) view.findViewById(R.id.Popupsure);
        btnsure.setOnClickListener(v -> {

            sharedViewModel.setBusInformation(stopName, String.valueOf(stopLatLng.latitude), String.valueOf(stopLatLng.longitude));
            //移除疊加在底下防止點擊其他區域的View
            removeOverlayView();
            popupWindow.dismiss();
        });
    }

    public void onToastShown(String message) {
        // 更新 TextView 的文本
        if (notification != null && message.equals("路線已更新")) {
            notification.setText("請選擇下列地標");
        }else{
            notification.setText("錯誤，請關閉在開");
        }
    }
    //把疊加在底層的View刪掉
    private void removeOverlayView() {
        if (overlayView != null && overlayView.getParent() != null) {
            ((ViewGroup) overlayView.getParent()).removeView(overlayView);
            overlayView = null;
        }
    }
}
