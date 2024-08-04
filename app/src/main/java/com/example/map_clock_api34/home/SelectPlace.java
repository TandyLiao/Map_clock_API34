package com.example.map_clock_api34.home;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.example.map_clock_api34.SharedViewModel;
import com.google.android.gms.common.api.Status;
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

import android.widget.Switch;
import android.widget.RadioGroup;
import android.widget.RadioButton;
import android.widget.Button;
import android.widget.TextView;

import android.widget.PopupWindow;
import android.widget.Toast;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import com.example.map_clock_api34.R;

public class SelectPlace extends Fragment {

    boolean isUnknown = false;
    private Toolbar toolbar;
    private AutocompleteSupportFragment start_autocompleteSupportFragment;
    private SharedViewModel sharedViewModel;
    private LocationManager locationManager;
    private String commandstr = LocationManager.GPS_PROVIDER;
    Location lastLocation;
    private Marker destiantion_Marker;
    private GoogleMap mMap;
    double destination_latitude, destination_longitude;
    String destiantion_Name, cityName, areaName;
    private Geocoder geocoder;
    private View overlayView;
    private boolean isRingtoneEnabled;
    private boolean isVibrationEnabled;
    private int notificationTime;
    private OnMapReadyCallback callback = new OnMapReadyCallback() {

        @SuppressLint("MissingPermission")
        public void onMapReady(GoogleMap googleMap) {

            mMap=googleMap;

            // 跑出藍色定位點
            mMap.setMyLocationEnabled(true);
            //取得最後的定位位置
            lastLocation = locationManager.getLastKnownLocation(commandstr);

            // 移动地图视图到最后已知的位置
            if (lastLocation != null) {
                // 移动地图视图到最后已知的位置
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude()), 15));
            } else {
                // 处理 lastLocation 为 null 的情况
                Toast.makeText(getContext(), "無法取得當前位置，請查看您的GPS設備", Toast.LENGTH_LONG).show();
            }



            //點擊地圖會直接跑出標示
            googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                @Override
                public void onMapClick(LatLng latLng) {

                    // When clicked on map
                    // Initialize marker options
                    MarkerOptions markerOptions=new MarkerOptions();
                    // Set position of marker
                    markerOptions.position(latLng);
                    // Set title of marker
                    markerOptions.title(latLng.latitude+" : "+latLng.longitude);

                    String nuKnownName="座標： "+ Math.round(latLng.latitude * 1000)/1000.0+"  "+ Math.round(latLng.longitude * 1000)/1000.0;
                    cityName = getCityNameCustom(getContext(), latLng.latitude, latLng.longitude);
                    areaName= getAreaNameCustom(getContext(), latLng.latitude, latLng.longitude);

                    isUnknown = true;

                    // Remove all marker
                    mMap.clear();
                    showPopupWindow(nuKnownName, latLng.latitude, latLng.longitude);
                    // Animating to zoom the marker
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,15));
                    // Add marker on map
                    mMap.addMarker(markerOptions);

                }
            });

        }
    };
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View v =inflater.inflate(R.layout.home_fragment_maps, container, false);

        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        geocoder = new Geocoder(requireContext(), Locale.getDefault());

        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

        start_autocompleteSupportFragment = (AutocompleteSupportFragment) getChildFragmentManager()
                .findFragmentById(R.id.start_autocomplete_fragment2);

        if (mapFragment != null) {
            mapFragment.getMapAsync(callback);
        }
        fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction(); // 初始化 FragmentTransaction

        iniAutocomplete();
        //搜尋功能監聽器
        start_autocompleteSupportFragment.setOnPlaceSelectedListener(start_autocomplete_Listener);

        ActionBar actionBar = ((AppCompatActivity) requireActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        return v;

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        toolbar = getActivity().findViewById(R.id.toolbar);

        ImageView returnButton = getActivity().findViewById(R.id.returnpage);
        returnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 关闭弹出窗口
                closePopupWindow();
                // 返回上一个 Fragment
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });

    }


    public void onResume() {
        super.onResume();
    }

    private void iniAutocomplete(){

        String apiKey="AIzaSyCf6tnPO_VbhDJ_EreXXRZes48c7X5giSM";
        if(!Places.isInitialized()){
            Places.initialize(getActivity().getApplicationContext(),apiKey);
        }


        start_autocompleteSupportFragment.setLocationBias(RectangularBounds.newInstance(
                new LatLng(25.1678643,121.4434853),
                new LatLng(25.1678643,121.4434853)
        ));

        start_autocompleteSupportFragment.setCountries("TW");

        start_autocompleteSupportFragment.setPlaceFields(Arrays.asList(Place.Field.ID,Place.Field.NAME,Place.Field.LAT_LNG));
    }
    private PlaceSelectionListener start_autocomplete_Listener = new PlaceSelectionListener() {
        @Override
        public void onError(@NonNull Status status) {

        }

        @Override
        public void onPlaceSelected(@NonNull Place place) {

            if(destiantion_Marker != null)
                destiantion_Marker.remove();

            destiantion_Name = place.getName();
            destination_latitude = place.getLatLng().latitude;
            destination_longitude = place.getLatLng().longitude;

            LatLng destiantion_LatLng;
            destiantion_LatLng= new LatLng(place.getLatLng().latitude,place.getLatLng().longitude);
            cityName = getCityNameCustom(getContext(), destiantion_LatLng.latitude, destiantion_LatLng.longitude);
            areaName= getAreaNameCustom(getContext(), destiantion_LatLng.latitude, destiantion_LatLng.longitude);
            isUnknown = false;

            //在地圖上標示Marker和彈跳地點資訊
            destiantion_Marker=mMap.addMarker(new MarkerOptions().position(destiantion_LatLng).title(place.getName()));
            if (destiantion_Marker != null) {
                showPopupWindow(destiantion_Name, destination_latitude, destination_longitude);
            }

            //把相機移動到選取地點
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(destiantion_LatLng,15));
        }
    };
    private PopupWindow popupWindow;
    private FragmentTransaction fragmentTransaction;

    // 定义全局变量来保存目的地信息
    private String currentDestinationName;
    private double currentDestinationLatitude;
    private double currentDestinationLongitude;

    private void showPopupWindow(String destiantion_Name, double destination_latitude, double destination_longitude) {
        if (popupWindow != null && popupWindow.isShowing()) {
            popupWindow.dismiss();
        }

        // 更新全局变量
        currentDestinationName = destiantion_Name;
        currentDestinationLatitude = destination_latitude;
        currentDestinationLongitude = destination_longitude;

        // Get layout inflater service
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        // Inflate the popup layout
        View popupView = inflater.inflate(R.layout.popupwindow_select_place, null);

        // Set the content of the popup window
        TextView destinationNameTextView = popupView.findViewById(R.id.DestinationName);
        TextView destinationAreaTextView = popupView.findViewById(R.id.DestinationArea);
        TextView destination_latitudeTextView = popupView.findViewById(R.id.destination_latitude);
        TextView destination_longitudeTextView = popupView.findViewById(R.id.destination_longitude);

        Button btnCancel = popupView.findViewById(R.id.btnCancel);
        Button btnNext = popupView.findViewById(R.id.btnNext);
        Button btnSettings = popupView.findViewById(R.id.btnSettings);

        if (!isUnknown) {
            destinationNameTextView.setText(destiantion_Name);
            destinationAreaTextView.setText(areaName);
        }
        // Set the destination name
        destination_latitudeTextView.setText(String.valueOf(destination_latitude));
        destination_longitudeTextView.setText(String.valueOf(destination_longitude));

        // Set button click event to navigate to the next page
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Close the popup window
                closePopupWindow();
                // Navigate to the next page

                sharedViewModel.setDestination(destiantion_Name, destination_latitude, destination_longitude);
                sharedViewModel.setCapital(cityName);
                sharedViewModel.setArea(areaName);

                sharedViewModel.setnowLocation(lastLocation.getLatitude(), lastLocation.getLongitude());

                getActivity().getSupportFragmentManager().popBackStack();
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMap.clear();
                // Close the popup window
                closePopupWindow();
            }
        });

        btnSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        // Create the popup window object
        popupWindow = new PopupWindow(popupView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);

        popupWindow.setFocusable(false);
        popupWindow.setOutsideTouchable(false);

        // Show the popup window
        popupWindow.showAtLocation(getView(), Gravity.BOTTOM, 0, 0);

        overlayView = new View(getContext());
        overlayView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        overlayView.setBackgroundColor(ContextCompat.getColor(getContext(), android.R.color.transparent));
        overlayView.setClickable(true);
        ((ViewGroup) getView()).addView(overlayView);
    }

    /*private void showSettingsPopupWindow() {
        if (popupWindow != null && popupWindow.isShowing()) {
            popupWindow.dismiss();
        }

        // Get layout inflater service
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        // Inflate the popup layout
        View popupView = inflater.inflate(R.layout.popupwindow_settings, null);

        // Get the views
        Switch switchRingtone = popupView.findViewById(R.id.switchRingtone);
        Switch switchVibration = popupView.findViewById(R.id.switchVibration);
        RadioGroup radioGroupNotificationTime = popupView.findViewById(R.id.radioGroupNotificationTime);
        RadioButton OneMinute = popupView.findViewById(R.id.radioOneMinute);
        RadioButton radioThreeMinutes = popupView.findViewById(R.id.radioThreeMinutes);
        RadioButton radioFiveMinutes = popupView.findViewById(R.id.radioFiveMinutes);
        Button btnCancel = popupView.findViewById(R.id.PopupCancel);
        Button btnConfirm = popupView.findViewById(R.id.PopupConfirm);

        // Set the listeners
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closePopupWindow();
                showPopupWindow(currentDestinationName, currentDestinationLatitude, currentDestinationLongitude);
            }
        });

        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle the settings confirmation
                boolean isRingtoneEnabled = switchRingtone.isChecked();
                boolean isVibrationEnabled = switchVibration.isChecked();
                int selectedNotificationTime = 1; // Default to 1 minute


                closePopupWindow();
                showPopupWindow(currentDestinationName, currentDestinationLatitude, currentDestinationLongitude);
            }
        });
        SharedPreferences preferences = getContext().getSharedPreferences("MySettings", Context.MODE_PRIVATE);
        boolean isRingtoneEnabled = preferences.getBoolean("ringtone_enabled", false); // Default value if not found
        boolean isVibrationEnabled = preferences.getBoolean("vibration_enabled", false); // Default value if not found
        int notificationTime = preferences.getInt("notification_time",5); // Default value if not found

        // Apply settings to UI components
        switchRingtone.setChecked(isRingtoneEnabled);
        switchVibration.setChecked(isVibrationEnabled);
        // Apply notification time selection
        switch (notificationTime) {
            case 1:
                OneMinute.setChecked(true);
                break;
            case 3:
                radioThreeMinutes.setChecked(true);
                break;
            case 5:
                radioFiveMinutes.setChecked(true);
                break;

        }
        // Create the popup window object
        popupWindow = new PopupWindow(popupView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);

        popupWindow.setFocusable(false);
        popupWindow.setOutsideTouchable(false);

        // Show the popup window
        popupWindow.showAtLocation(getView(), Gravity.BOTTOM, 0, 0);

        // Add an overlay view to prevent interaction outside the popup
        overlayView = new View(getContext());
        overlayView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        overlayView.setBackgroundColor(ContextCompat.getColor(getContext(), android.R.color.transparent));
        overlayView.setClickable(true);
        ((ViewGroup) getView()).addView(overlayView);
    }

     */


    private void closePopupWindow() {
        if (popupWindow != null && popupWindow.isShowing()) {
            popupWindow.dismiss();
            if (overlayView != null && overlayView.getParent() != null) {
                ((ViewGroup) overlayView.getParent()).removeView(overlayView);
            }
        }
    }

    public String getCityNameCustom(Context context, double latitude, double longitude) {
        // 金門縣的經緯度範圍
        if (latitude >= 24.4 && latitude <= 24.6 && longitude >= 118.2 && longitude <= 118.5) {
            return "金門縣";
        }
        // 馬祖縣的經緯度範圍
        if (latitude >= 26.0 && latitude <= 26.3 && longitude >= 119.9 && longitude <= 120.5) {
            return "連江縣";
        }

        // 使用內置的 Geocoder 獲取其他地區名稱
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
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

        // 返回默認值
        return "未知地區";
    }
    public String getAreaNameCustom(Context context,double latitude, double longitude) {

        // 使用內置的 Geocoder 獲取其他地區名稱
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
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

        // 返回默認值
        return "";
    }
}
