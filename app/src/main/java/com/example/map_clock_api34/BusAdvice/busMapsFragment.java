package com.example.map_clock_api34.BusAdvice;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.DrawableRes;
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
import com.example.map_clock_api34.home.SelectPlace;
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

    SelectPlace selectPlace;

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
                Toast.makeText(getContext(), "無法取得目前位置訊息，請查看您的GPS設備", Toast.LENGTH_LONG).show();
            }

            LatLng latlon = new LatLng(sharedViewModel.getLatitude(0), sharedViewModel.getLongitude(0));
            Bitmap customMarker = createCustomMarker(getContext(), R.drawable.custom_destination_marker, sharedViewModel.getDestinationName(0), true);

            Marker destination = mMap.addMarker(new MarkerOptions()
                    .position(latlon)
                    .title(sharedViewModel.getDestinationName(0))
                    .icon(BitmapDescriptorFactory.fromBitmap(customMarker))
                    .alpha(1.0f));

            // 设置标记点击事件
            mMap.setOnMarkerClickListener(marker -> {
                if(marker.equals(destination)) {
                    Toast.makeText(getActivity(), "這是你的目的地", Toast.LENGTH_SHORT).show();
                }
                else if (nearbyMarkers.contains(marker)) {
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

        selectPlace = new SelectPlace();

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

    private void displayBusStopsOnMap(List<BusStationFinderHelper.BusStation> busStops, float color, Set<Marker> markerSet) {
        if (busStops == null || busStops.isEmpty()) {
            return;
        }

        for (BusStationFinderHelper.BusStation station : busStops) {
            Map<String, Map<BusStationFinderHelper.BusStation.LatLng, String>> routes = station.getRoutes();

            // 過濾掉沒有目的地站牌的站點
            boolean hasDestination = false;
            if (routes != null) {
                for (Map<BusStationFinderHelper.BusStation.LatLng, String> destinationMap : routes.values()) {
                    if (!destinationMap.isEmpty()) {
                        hasDestination = true;
                        break;
                    }
                }
            }

            if (!hasDestination) {
                continue; // 跳過沒有目的地站牌的站點
            }

            Log.d("Bus", station.toString());
            LatLng position = new LatLng(station.getStopLat(), station.getStopLon());

            Bitmap customMarker = createCustomMarker(getContext(), R.drawable.custom_marker, station.getStopName(), false);
            Marker marker = mMap.addMarker(new MarkerOptions()
                    .position(position)
                    .title(station.getStopName())
                    .icon(BitmapDescriptorFactory.fromBitmap(customMarker))
                    .alpha(1.0f));
            markerSet.add(marker);
        }
        if(nearbyMarkers.isEmpty()){
            notification.setText("附近沒有可用直達車");
        }
    }
    private Bitmap createCustomMarker(Context context, @DrawableRes int resource, String title, Boolean isDestination) {
        View markerView = LayoutInflater.from(context).inflate(R.layout.bus_advice_bus_custom_marker, null);

        ImageView markerImage = markerView.findViewById(R.id.marker_image);
        markerImage.setImageResource(resource);

        TextView markerText = markerView.findViewById(R.id.marker_text);
        markerText.setText(title);
        if(isDestination)   markerText.setBackgroundResource(R.drawable.btn_unclickable);

        markerView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        markerView.layout(0, 0, markerView.getMeasuredWidth(), markerView.getMeasuredHeight());

        Bitmap bitmap = Bitmap.createBitmap(markerView.getMeasuredWidth(), markerView.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        markerView.draw(canvas);

        return bitmap;
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
    private AlertDialog dialog;

    private void showRoutesDialog(String stopName, BusStationFinderHelper.BusStation station) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        // 套用XML的布局
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View customView = inflater.inflate(R.layout.dialog_bus_fragment, null);

        TextView showTitle = customView.findViewById(R.id.txtNote);
        showTitle.setText(stopName + "站 - 請選擇可搭路線");

        // 获取routes_container，动态添加路線選項
        LinearLayout routesContainer = customView.findViewById(R.id.routes_container);

        final Map<String, Map<BusStationFinderHelper.BusStation.LatLng, String>> routes = station.getRoutes();
        final List<String> routeNames = new ArrayList<>();

        // 过滤掉没有目的地的路线
        for (Map.Entry<String, Map<BusStationFinderHelper.BusStation.LatLng, String>> entry : routes.entrySet()) {
            if (!entry.getValue().isEmpty()) {
                routeNames.add(entry.getKey());
            }
        }

        // 获取每条路线的到站时间信息
        List<String> routeDetails = new ArrayList<>();
        for (String routeName : routeNames) {
            String arrivalTime = station.getArrivalTimes().get(routeName);
            routeDetails.add(routeName + " - 到站時間: " + arrivalTime);
        }

        Log.d("BusStationFinderHelper", "Routes for stop " + station.getStopName() + ": " + routeDetails);

        // 动态添加 TextView 到 routesContainer
        for (String detail : routeDetails) {
            View itemView = inflater.inflate(R.layout.dialog_bus_item, routesContainer, false);
            TextView routeTextView = itemView.findViewById(R.id.routeTextView);
            routeTextView.setText(detail);
            routeTextView.setOnClickListener(v -> {
                int which = routeDetails.indexOf(detail);
                String selectedRoute = routeNames.get(which);
                highlightRouteDestinations(routes.get(selectedRoute));
                dialog.dismiss(); // 在選擇路線後關閉對話框
            });

            routesContainer.addView(itemView);
        }
        builder.setView(customView);

        // 创建并显示对话框
        dialog = builder.create();
        // 設置點擊對話框外部不會關閉對話框
        dialog.setCanceledOnTouchOutside(false);

        // 设置取消按钮的点击事件
        Button cancelBTN = customView.findViewById(R.id.PopupCancel);
        cancelBTN.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
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
            Bitmap customMarker = createCustomMarker(getContext(), R.drawable.custom_destination_near_marker, entry.getValue().toString(), false);
            Marker marker = mMap.addMarker(new MarkerOptions()
                    .position(position)
                    .title(entry.getValue())  // 直接顯示目的地站牌名稱
                    .icon(BitmapDescriptorFactory.fromBitmap(customMarker))
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
            displayBusStopsOnMap(secondNearByStop, BitmapDescriptorFactory.HUE_BLUE, nearbyMarkers);
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
        stationFinder.stopSearching();
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

            if(sharedViewModel.getLocationCount()<7){
                String busArea = selectPlace.getAreaNameCustom(getContext(), stopLatLng.latitude, stopLatLng.longitude);
                String busCity = selectPlace.getCityNameCustom(getContext(), stopLatLng.latitude, stopLatLng.longitude);

                sharedViewModel.setFirstDestination("公車站："+stopName, busArea, busCity, stopLatLng.latitude, stopLatLng.longitude);
                Toast.makeText(getActivity(),"已加入路線",Toast.LENGTH_SHORT).show();
            }
            else{
                Toast.makeText(getActivity(),"路線已滿，請刪除後再試",Toast.LENGTH_SHORT).show();
            }
            //移除疊加在底下防止點擊其他區域的View
            removeOverlayView();
            popupWindow.dismiss();

        });
    }

    public void onToastShown(String message) {
        // 更新 TextView 的文本
        if (notification != null && message.equals("路線已更新")) {
            notification.setText("請選擇下列紅色地標");
        }else{
            notification.setText("發生錯誤，請關閉再開");
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
