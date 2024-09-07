package com.example.map_clock_api34.CreateLocation;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.example.map_clock_api34.BuildConfig;
import com.example.map_clock_api34.R;
import com.example.map_clock_api34.SharedViewModel;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class SelectPlace extends Fragment {

    private View overlayView;
    private PopupWindow popupWindow;  // 用於顯示地點資訊的彈出窗口

    private AutocompleteSupportFragment start_autocompleteSupportFragment;  // Google 自動完成片段
    private Geocoder geocoder;  // 用於反向地理編碼的 Geocoder
    private GoogleMap mMap;     // Google 地圖對象
    private FusedLocationProviderClient fusedLocationProviderClient;  // 使用 FusedLocationProviderClient 來替代 LocationManager
    private Location lastLocation;      // 保存最後一次已知的位置信息
    private Marker destiantion_Marker;  // 地圖上的目標標記

    private SharedViewModel sharedViewModel;

    private String cityName;
    private String areaName;  // 目的地的名稱、城市和區域
    private boolean isUnknown = false;  // 是否未知的地點

    // 當地圖準備好時會調用此回調方法
    private OnMapReadyCallback callback = new OnMapReadyCallback() {
        @SuppressLint("MissingPermission")
        public void onMapReady(GoogleMap googleMap) {

            mMap = googleMap;
            // 設置藍色定位點
            mMap.setMyLocationEnabled(true);

            // 獲取最近的位置信息
            fusedLocationProviderClient.getLastLocation()
                    .addOnSuccessListener(location -> {
                        if (location != null) {
                            lastLocation = location;
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude()), 15));
                        } else {
                            // 當無法獲取位置時，提示用戶檢查 GPS 設備
                            makeToast("無法取得當前位置，請查看您的GPS設備",1000);
                            getActivity().getSupportFragmentManager().popBackStack();
                        }
                    });

            // 當點擊地圖時，在該點顯示一個標記
            googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                @Override
                public void onMapClick(LatLng latLng) {
                    MarkerOptions markerOptions = new MarkerOptions();
                    markerOptions.position(latLng);  // 設置標記位置
                    markerOptions.title(latLng.latitude + " : " + latLng.longitude);  // 設置標記標題

                    // 根據經緯度獲取地區名稱
                    String nuKnownName = "座標： " + Math.round(latLng.latitude * 1000) / 1000.0 + "  " + Math.round(latLng.longitude * 1000) / 1000.0;
                    cityName = getCityNameCustom(getContext(), latLng.latitude, latLng.longitude);
                    areaName = getAreaNameCustom(getContext(), latLng.latitude, latLng.longitude);
                    isUnknown = true;  // 設置為未知地點

                    mMap.clear();  // 清除地圖上所有標記
                    showPopupWindow(nuKnownName, latLng.latitude, latLng.longitude);  // 顯示彈出窗口
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));  // 放大到該位置
                    mMap.addMarker(markerOptions);  // 在地圖上添加標記
                }
            });
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.home_fragment_maps, container, false);

        // 獲取 Google 地圖片段
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);

        // 初始化 FusedLocationProviderClient
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        start_autocompleteSupportFragment = (AutocompleteSupportFragment) getChildFragmentManager()
                .findFragmentById(R.id.start_autocomplete_fragment2);

        // 如果地圖片段不為空，設置地圖回調
        if (mapFragment != null) {
            mapFragment.getMapAsync(callback);
        }

        // 初始化 Google 自動完成功能
        iniAutocomplete();

        // 設置自動完成選擇監聽器
        start_autocompleteSupportFragment.setOnPlaceSelectedListener(start_autocomplete_Listener);

        // 隱藏 ActionBar
        ActionBar actionBar = ((AppCompatActivity) requireActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        return rootView;
    }

    // 當 Fragment 的視圖創建完成後，設置返回按鈕的點擊事件
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 設置返回按鈕的點擊事件
        ImageView returnButton = getActivity().findViewById(R.id.returnpage);
        returnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closePopupWindow();  // 關閉彈出窗口
                getActivity().getSupportFragmentManager().popBackStack();  // 返回上一個 Fragment
            }
        });
    }

    // 初始化 Google 自動完成功能
    private void iniAutocomplete() {
        String apiKey = BuildConfig.GOOGLE_DISTANCE_API_KEY;  // 從配置文件中獲取 Google API 金鑰
        if (!Places.isInitialized()) {
            Places.initialize(getActivity().getApplicationContext(), apiKey);
        }

        // 設置自動完成的搜索區域為特定範圍
        start_autocompleteSupportFragment.setLocationBias(RectangularBounds.newInstance(
                new LatLng(25.1678643, 121.4434853),
                new LatLng(25.1678643, 121.4434853)
        ));

        start_autocompleteSupportFragment.setCountries("TW");  // 限定國家為台灣
        start_autocompleteSupportFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG));  // 設置需要獲取的地點字段
    }

    // 自動完成選擇的監聽器
    private PlaceSelectionListener start_autocomplete_Listener = new PlaceSelectionListener() {
        @Override
        public void onError(@NonNull Status status) {
            // 錯誤處理
        }

        @Override
        public void onPlaceSelected(@NonNull Place place) {
            if (destiantion_Marker != null)
                destiantion_Marker.remove();  // 移除上一個標記

            // 獲取選中的地點資訊
            String destiantion_Name = place.getName();
            // 目的地的緯度和經度
            double destination_latitude = place.getLatLng().latitude;
            double destination_longitude = place.getLatLng().longitude;

            LatLng destiantion_LatLng = new LatLng(destination_latitude, destination_longitude);
            cityName = getCityNameCustom(getContext(), destination_latitude, destination_longitude);  // 獲取城市名稱
            areaName = getAreaNameCustom(getContext(), destination_latitude, destination_longitude);  // 獲取區域名稱
            isUnknown = false;  // 已知的地點

            // 在地圖上標記選擇的地點
            destiantion_Marker = mMap.addMarker(new MarkerOptions().position(destiantion_LatLng).title(place.getName()));
            if (destiantion_Marker != null) {
                showPopupWindow(destiantion_Name, destination_latitude, destination_longitude);  // 顯示彈出窗口
            }

            // 移動地圖相機到選擇的地點
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(destiantion_LatLng, 15));
        }
    };

    // 顯示彈出窗口以顯示選擇的地點資訊
    private void showPopupWindow(String destiantion_Name, double destination_latitude, double destination_longitude) {
        if (popupWindow != null && popupWindow.isShowing()) {
            popupWindow.dismiss();
        }

        // 獲取佈局加載器
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.popupwindow_select_place, null);  // 加載彈出窗口佈局

        // 設置彈出窗口內容
        TextView destinationNameTextView = popupView.findViewById(R.id.DestinationName);
        TextView destinationAreaTextView = popupView.findViewById(R.id.DestinationArea);
        TextView destination_latitudeTextView = popupView.findViewById(R.id.destination_latitude);
        TextView destination_longitudeTextView = popupView.findViewById(R.id.destination_longitude);

        Button btnCancel = popupView.findViewById(R.id.btnCancel);
        Button btnNext = popupView.findViewById(R.id.btnNext);

        // 如果地點已知，設置地點名稱和區域
        if (!isUnknown) {
            destinationNameTextView.setText(destiantion_Name);
            destinationAreaTextView.setText(areaName);
        }

        destination_latitudeTextView.setText(String.valueOf(destination_latitude));
        destination_longitudeTextView.setText(String.valueOf(destination_longitude));

        // "下一步" 按鈕的點擊事件
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closePopupWindow();  // 關閉彈出窗口

                // 將選擇的地點資訊保存到 ShareViewModel 中
                sharedViewModel.setDestination(destiantion_Name, destination_latitude, destination_longitude);
                sharedViewModel.setCapital(cityName);
                sharedViewModel.setArea(areaName);

                sharedViewModel.setNowLocation(lastLocation.getLatitude(), lastLocation.getLongitude());

                getActivity().getSupportFragmentManager().popBackStack();  // 返回上一個 Fragment
            }
        });

        // "取消" 按鈕的點擊事件
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMap.clear();  // 清除地圖標記
                closePopupWindow();  // 關閉彈出窗口
            }
        });

        // 創建彈出窗口
        popupWindow = new PopupWindow(popupView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        popupWindow.setFocusable(false);
        popupWindow.setOutsideTouchable(false);

        popupWindow.showAtLocation(getView(), Gravity.BOTTOM, 0, 0);  // 顯示彈出窗口

        // 創建一個透明的 View 來阻擋其他點擊操作
        overlayView = new View(getContext());
        overlayView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        overlayView.setBackgroundColor(ContextCompat.getColor(getContext(), android.R.color.transparent));
        overlayView.setClickable(true);
        ((ViewGroup) getView()).addView(overlayView);
    }

    // 關閉彈出窗口
    private void closePopupWindow() {
        if (popupWindow != null && popupWindow.isShowing()) {
            popupWindow.dismiss();
            if (overlayView != null && overlayView.getParent() != null) {
                ((ViewGroup) overlayView.getParent()).removeView(overlayView);
            }
        }
    }

    // 使用 Geocoder 獲取城市名稱
    public String getCityNameCustom(Context context, double latitude, double longitude) {
        // 特殊處理金門縣和馬祖縣
        if (latitude >= 24.4 && latitude <= 24.6 && longitude >= 118.2 && longitude <= 118.5) {
            return "金門縣";
        }
        if (latitude >= 26.0 && latitude <= 26.3 && longitude >= 119.9 && longitude <= 120.5) {
            return "連江縣";
        }

        // 使用 Geocoder 獲取其他地區名稱
        geocoder = new Geocoder(context, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                String cityName = address.getAdminArea();
                if (cityName != null && !cityName.isEmpty()) {
                    if (cityName.contains("台")) {
                        cityName = cityName.replace("台", "臺");
                    }
                    return cityName;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return "未知地區";  // 默認為 "未知地區"
    }

    // 使用 Geocoder 獲取區域名稱
    public String getAreaNameCustom(Context context, double latitude, double longitude) {
        geocoder = new Geocoder(context, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                String areaName = address.getSubAdminArea();
                if (areaName != null && !areaName.isEmpty()) {
                    if (areaName.contains("台")) {
                        areaName = areaName.replace("台", "臺");
                    }
                    return areaName;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return "";  // 默認返回空字符串
    }

    public void makeToast(String message, int durationInMillis) {
        // 創建 Toast
        Toast toast = Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT);
        toast.show();

        // 使用 Handler 來控制顯示時長
        new Handler().postDelayed(toast::cancel, durationInMillis);
    }
}
