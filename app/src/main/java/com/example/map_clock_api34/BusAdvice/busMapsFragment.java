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

    private SharedViewModel sharedViewModel;
    private LocationManager locationManager;
    private Location lastLocation; // 儲存最後的定位資訊
    private View overlayView; // 疊加在視圖上的透明遮罩，用於防止背景互動
    private View rootView;
    private TextView notification; // 用來顯示通知的文字
    private AlertDialog dialog;

    private GoogleMap googleMap;

    private SelectPlace selectPlace; // 用來選擇地點的自定類別

    private BusStationFinderHelper stationFinder; // 公車站點查找的輔助工具

    private List<BusStationFinderHelper.BusStation> secondNearByStop; // 附近的公車站點
    private Set<Marker> nearbyMarkers = new HashSet<>(); // 附近站點的標記集合
    private Set<Marker> destinationMarkers = new HashSet<>(); // 目的地站點的標記集合

    // 當地圖準備好時的回調函數
    private OnMapReadyCallback callback = new OnMapReadyCallback() {

        @SuppressLint("MissingPermission") // 壓制缺少權限的警告
        public void onMapReady(GoogleMap googleMap) {

            busMapsFragment.this.googleMap = googleMap; // 初始化地圖物件
            busMapsFragment.this.googleMap.setMyLocationEnabled(true);  // 啟用藍色定位點

            String commandstr = LocationManager.GPS_PROVIDER;   // 用於定位的 GPS 提供
            lastLocation = locationManager.getLastKnownLocation(commandstr);    // 取得最後的定位資訊

            // 如果有定位資訊，將地圖視圖移動到該位置
            if (lastLocation != null) {
                busMapsFragment.this.googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                        new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude()), 15));
            } else {
                // 如果無法取得位置資訊，顯示提示訊息
                Toast.makeText(getContext(), "無法取得目前位置訊息，請查看您的GPS設備", Toast.LENGTH_SHORT).show();
            }

            // 設定目的地位置
            LatLng latlon = new LatLng(sharedViewModel.getLatitude(0), sharedViewModel.getLongitude(0));
            Bitmap customMarker = createCustomMarker(getContext(), R.drawable.custom_destination_marker, sharedViewModel.getDestinationName(0), true);

            // 在地圖上添加目的地標記
            Marker destination = busMapsFragment.this.googleMap.addMarker(new MarkerOptions()
                    .position(latlon)
                    .title(sharedViewModel.getDestinationName(0))
                    .icon(BitmapDescriptorFactory.fromBitmap(customMarker))
                    .alpha(1.0f));

            // 設定標記點擊事件
            busMapsFragment.this.googleMap.setOnMarkerClickListener(marker -> {
                if(marker.equals(destination)) {
                    // 如果點擊的是目的地，顯示提示訊息
                    Toast.makeText(getActivity(), "這是你的目的地", Toast.LENGTH_SHORT).show();
                }
                else if (nearbyMarkers.contains(marker)) {
                    // 如果點擊的是附近站牌，查找可搭乘的公車路線
                    String stopName = marker.getTitle();
                    LatLng position = marker.getPosition();
                    findBusRoutesForStop(stopName, position.latitude, position.longitude);
                } else {
                    // 點擊其他標記，顯示彈出視窗
                    ShowPopupWindow(marker.getTitle(), marker.getPosition());
                }

                return true; // 防止地圖默認的點擊行為
            });

        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.home_bus_maps_fragment, container, false);

        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);

        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

        selectPlace = new SelectPlace(); // 初始化選擇地點的物件

        notification = rootView.findViewById(R.id.textinfo);

        stationFinder = new BusStationFinderHelper(getActivity(), sharedViewModel, this); // 初始化公車站點查找器
        stationFinder.setToastCallback(this::onToastShown); // 設定 Toast 回調
        stationFinder.findNearbyStations(rootView); // 查找附近站點

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);

        if (mapFragment != null) {
            mapFragment.getMapAsync(callback); // 當地圖準備好時進行回調
        }

        // 隱藏標題欄
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

    // 顯示附近的公車站點在地圖上
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

            LatLng position = new LatLng(station.getStopLat(), station.getStopLon()); // 取得站牌位置

            Bitmap customMarker = createCustomMarker(getContext(), R.drawable.custom_marker, station.getStopName(), false); // 建立自訂標記
            Marker marker = googleMap.addMarker(new MarkerOptions()
                    .position(position)
                    .title(station.getStopName()) // 顯示站牌名稱
                    .icon(BitmapDescriptorFactory.fromBitmap(customMarker)) // 設定自訂標記
                    .alpha(1.0f));
            markerSet.add(marker); // 將標記加入集合
        }

        // 如果沒有附近站牌，更新通知文字
        if (nearbyMarkers.isEmpty()) {
            notification.setText("附近沒有可用直達車");
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

    // 查找該站點的可搭乘公車路線
    private void findBusRoutesForStop(String stopName, double lat, double lon) {
        if (stopName == null || stopName.isEmpty()) {
            return;
        }

        for (BusStationFinderHelper.BusStation station : secondNearByStop) {
            // 比對站名和經緯度是否符合
            if (station.getStopName().equals(stopName.split(" \\(")[0]) && Math.abs(station.getStopLat() - lat) < 0.00001 && Math.abs(station.getStopLon() - lon) < 0.00001) {
                showRoutesDialog(stopName, station); // 顯示該站點的路線對話框
                break;
            }
        }
    }

    // 顯示公車站點的路線選擇對話框
    private void showRoutesDialog(String stopName, BusStationFinderHelper.BusStation station) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        // 套用 XML 的佈局
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View customView = inflater.inflate(R.layout.dialog_bus_fragment, null);

        TextView showTitle = customView.findViewById(R.id.txtNote);
        showTitle.setText(stopName + "站 - 請選擇可搭路線"); // 顯示站名和提示文字

        LinearLayout routesContainer = customView.findViewById(R.id.routes_container);

        final Map<String, Map<BusStationFinderHelper.BusStation.LatLng, String>> routes = station.getRoutes();
        final List<String> routeNames = new ArrayList<>();

        // 過濾掉沒有目的地的路線
        for (Map.Entry<String, Map<BusStationFinderHelper.BusStation.LatLng, String>> entry : routes.entrySet()) {
            if (!entry.getValue().isEmpty()) {
                routeNames.add(entry.getKey());
            }
        }

        List<String> routeDetails = new ArrayList<>();
        for (String routeName : routeNames) {
            // 取得每條路線的到站時間
            String arrivalTime = station.getArrivalTimes().get(routeName);
            routeDetails.add(routeName + " - 到站時間: " + arrivalTime);
        }

        Log.d("BusStationFinderHelper", "Routes for stop " + station.getStopName() + ": " + routeDetails);

        // 動態將路線文字加入到容器中
        for (String detail : routeDetails) {
            View itemView = inflater.inflate(R.layout.dialog_bus_item, routesContainer, false); // 載入每條路線的佈局
            TextView routeTextView = itemView.findViewById(R.id.routeTextView);
            routeTextView.setText(detail); // 設定路線資訊
            routeTextView.setOnClickListener(v -> {
                int which = routeDetails.indexOf(detail); // 取得點擊的路線索引
                String selectedRoute = routeNames.get(which); // 取得點擊的路線名稱
                highlightRouteDestinations(routes.get(selectedRoute)); // 高亮顯示該路線的目的地
                dialog.dismiss(); // 選擇路線後關閉對話框
            });

            routesContainer.addView(itemView); // 將每個路線加入容器中
        }
        builder.setView(customView);

        dialog = builder.create(); // 建立對話框
        dialog.setCanceledOnTouchOutside(false); // 禁止點擊對話框外部時關閉對話框

        Button cancelBTN = customView.findViewById(R.id.PopupCancel); // 取消按鈕
        cancelBTN.setOnClickListener(v -> dialog.dismiss()); // 點擊取消按鈕時關閉對話框

        dialog.show(); // 顯示對話框
    }

    // 重置目的地標記
    private void resetDestinationMarkers() {
        for (Marker marker : destinationMarkers) {
            marker.remove(); // 移除標記
        }
        destinationMarkers.clear(); // 清空集合
    }

    // 高亮顯示某條路線的目的地
    private void highlightRouteDestinations(Map<BusStationFinderHelper.BusStation.LatLng, String> destinations) {
        resetDestinationMarkers(); // 重置目的地標記
        LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder(); // 經緯度邊界建構器

        for (Map.Entry<BusStationFinderHelper.BusStation.LatLng, String> entry : destinations.entrySet()) {
            Log.d("highlightRouteDestinations", "Destination stop: " + entry.getValue() + " at " + entry.getKey().getLat() + ", " + entry.getKey().getLon());
            LatLng position = new LatLng(entry.getKey().getLat(), entry.getKey().getLon()); // 取得目的地位置
            Bitmap customMarker = createCustomMarker(getContext(), R.drawable.custom_destination_near_marker, entry.getValue().toString(), false); // 建立自訂目的地標記
            Marker marker = googleMap.addMarker(new MarkerOptions()
                    .position(position)
                    .title(entry.getValue()) // 顯示目的地站牌名稱
                    .icon(BitmapDescriptorFactory.fromBitmap(customMarker)) // 設定自訂標記
                    .alpha(1.0f));
            destinationMarkers.add(marker); // 加入目的地標記集合
            boundsBuilder.include(position); // 加入邊界計算
        }

        for (Marker marker : nearbyMarkers) {
            boundsBuilder.include(marker.getPosition()); // 將附近標記也加入邊界計算
        }

        LatLngBounds bounds = boundsBuilder.build(); // 建立邊界
        googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 300)); // 調整地圖視圖以適應邊界
    }

    @Override
    public void onBusStationsFound(List<BusStationFinderHelper.BusStation> nearbyStops) {
        secondNearByStop = nearbyStops; // 儲存查找到的附近站點
        if (googleMap != null) {
            displayBusStopsOnMap(secondNearByStop, BitmapDescriptorFactory.HUE_BLUE, nearbyMarkers); // 顯示附近站點在地圖上
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        stationFinder.stopSearching(); // 停止查找站點
    }

    // 顯示彈出視窗，詢問是否將該站點加入行程
    private void ShowPopupWindow(String stopName, LatLng stopLatLng) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.popupwindow_reset_button, null, false);
        PopupWindow popupWindow = new PopupWindow(view, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        popupWindow.setWidth(700);
        popupWindow.setFocusable(false);
        popupWindow.setOutsideTouchable(false);

        // 顯示彈出視窗
        popupWindow.showAtLocation(rootView, Gravity.CENTER, 0, 0);
        popupWindow.setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        // 疊加透明遮罩，防止點擊其他區域
        overlayView = new View(getContext());
        overlayView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        overlayView.setBackgroundColor(ContextCompat.getColor(getContext(), android.R.color.transparent));
        overlayView.setClickable(true);
        ((ViewGroup) rootView).addView(overlayView);

        TextView showNotification = (TextView) view.findViewById(R.id.txtNote); // 取得彈窗的文字視圖
        showNotification.setTextSize(15);
        showNotification.setText(stopName + " - 要加入行程中嗎?"); // 顯示站點名稱和提示

        Button BTNPopup = (Button) view.findViewById(R.id.PopupCancel); // 取消按鈕
        BTNPopup.setOnClickListener(v -> {
            popupWindow.dismiss(); // 點擊取消按鈕時關閉彈窗
            removeOverlayView(); // 移除透明遮罩
        });

        Button btnsure = (Button) view.findViewById(R.id.Popupsure); // 確認按鈕
        btnsure.setOnClickListener(v -> {
            // 檢查是否已滿行程
            if(sharedViewModel.getLocationCount()<7){
                // 取得該站點的地區和城市名稱
                String busArea = selectPlace.getAreaNameCustom(getContext(), stopLatLng.latitude, stopLatLng.longitude);
                String busCity = selectPlace.getCityNameCustom(getContext(), stopLatLng.latitude, stopLatLng.longitude);

                // 將站點加入目的地
                sharedViewModel.setFirstDestination("公車站：" + stopName, busArea, busCity, stopLatLng.latitude, stopLatLng.longitude);
                Toast.makeText(getActivity(), "已加入路線", Toast.LENGTH_SHORT).show();
            } else {
                // 如果行程已滿，顯示提示
                Toast.makeText(getActivity(), "路線已滿，請刪除後再試", Toast.LENGTH_SHORT).show();
            }
            removeOverlayView(); // 移除透明遮罩
            popupWindow.dismiss(); // 關閉彈窗
        });
    }

    public void onToastShown(String message) {
        // 更新通知文字
        if (notification != null && message.equals("路線已更新")) {
            notification.setText("請選擇下列紅色地標");
        } else {
            notification.setText("發生錯誤，請關閉再開");
        }
    }

    // 移除疊加在底層的透明遮罩
    private void removeOverlayView() {
        if (overlayView != null && overlayView.getParent() != null) {
            ((ViewGroup) overlayView.getParent()).removeView(overlayView);
            overlayView = null;
        }
    }
}
