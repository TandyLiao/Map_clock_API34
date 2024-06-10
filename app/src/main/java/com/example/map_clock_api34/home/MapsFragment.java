package com.example.map_clock_api34.home;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.example.map_clock_api34.SharedViewModel;
import com.example.map_clock_api34.home.CreateLocation;
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
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;

import android.Manifest;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Arrays;

import com.example.map_clock_api34.R;

public class MapsFragment extends Fragment {

    private Toolbar toolbar;
    private AutocompleteSupportFragment start_autocompleteSupportFragment;

    private LocationManager locationManager;
    private String commandstr = LocationManager.GPS_PROVIDER;
    Location lastLocation;
    private Marker destiantion_Marker;
    private GoogleMap mMap;
    double destination_latitude, destination_longitude;
    String destiantion_Name;

    private View overlayView;


    private boolean isPopupShowing = false;

    private OnMapReadyCallback callback = new OnMapReadyCallback() {

        @SuppressLint("MissingPermission")
        public void onMapReady(GoogleMap googleMap) {

            mMap=googleMap;

            //一直更新目前位置
            //locationManager.requestLocationUpdates(commandStr,1000,0,locationListener);
            // 跑出藍色定位點
            mMap.setMyLocationEnabled(true);
            //取得最後的定位位置
            lastLocation = locationManager.getLastKnownLocation(commandstr);

            // 移动地图视图到最后已知的位置
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude()), 15));




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
                    String placeName = "我們仍未知道哪天google能看見的地名";
                    // Remove all marker
                    mMap.clear();
                    showPopupWindow(placeName, latLng.latitude, latLng.longitude);
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

        View v =inflater.inflate(R.layout.fragment_maps, container, false);

        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);

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
        Fragment createLocationFragment = getActivity().getSupportFragmentManager().findFragmentById(R.id.fragment_container);

        // 如果 CreateLocation Fragment 存在并且是 CreateLocation 的实例
        if (createLocationFragment instanceof CreateLocation) {
            // 清除数据
            //((CreateLocation) createLocationFragment).clearData();
        }

        //建立CardView在toolbar
       /* CardView cardmap = new CardView(requireContext());

        int widthInPixels = getResources().getDimensionPixelSize(R.dimen.map_width);
        int heightInPixels = getResources().getDimensionPixelSize(R.dimen.map_height);
        cardmap.setLayoutParams(new CardView.LayoutParams(widthInPixels, heightInPixels));

        Drawable drawable = ContextCompat.getDrawable(requireContext(), R.drawable.cardviewmap_shape);
        cardmap.setBackground(drawable);*/

        //建立LinearLayout在CardView等等放圖案和文字
        /*LinearLayout linearLayout = new LinearLayout(requireContext());
        linearLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        ));
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);
        linearLayout.addView(fragmentcontainerView);*/



        /* ImageView mapmark = new ImageView(requireContext());
        mapmark.setImageResource(R.drawable.magnifier_search);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                100, // 设置宽度为 100 像素
                100 // 设置高度为 100 像素
        );
        params.setMarginStart(10); // 设置左边距
        mapmark.setLayoutParams(params);*/

        // 創建EditText
       /* EditText edmap = new EditText(requireContext());
        edmap.setHint("搜尋地點");
        edmap.setTextColor(getResources().getColor(R.color.green));
        edmap.setLayoutParams(new CardView.LayoutParams(widthInPixels, heightInPixels));

        edmap.setSingleLine(true);
        edmap.setImeOptions(EditorInfo.IME_ACTION_SEARCH);


        linearLayout.addView(mapmark);
        linearLayout.addView(edmap);
        cardmap.addView(linearLayout);*/

        // 將cardview新增到actionBar

    }
    public void clearData() {
        // 清除地点信息
        //destinationNameTextView.setText("");
        //destinationLatitudeTextView.setText("");
        //destinationLongitudeTextView.setText("");
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

    private void showPopupWindow(String destiantion_Name,double destination_latitude,double destination_longitude) {
        if (popupWindow != null && popupWindow.isShowing()) {
            popupWindow.dismiss();
        }
        // Get layout inflater service
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        // Inflate the popup layout
        View popupView = inflater.inflate(R.layout.popview, null);

        // Set the content of the popup window
        TextView destinationNameTextView = popupView.findViewById(R.id.DestinationName);
        TextView destination_latitudeTextView = popupView.findViewById(R.id.destination_latitude);
        TextView destination_longitudeTextView = popupView.findViewById(R.id.destination_longitude);

        Button btnCancel = popupView.findViewById(R.id.btnCancel);
        Button btnNext = popupView.findViewById(R.id.btnNext);

        // Set the destination name
        destinationNameTextView.setText(destiantion_Name);
        destination_latitudeTextView.setText(String.valueOf( destination_latitude));
        destination_longitudeTextView.setText(String.valueOf(destination_longitude));

        // Set button click event to navigate to the next page
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Close the popup window
                closePopupWindow();
                // Navigate to the next page
                SharedViewModel sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
                sharedViewModel.setDestination(destiantion_Name, destination_latitude, destination_longitude);

                CreateLocation createFragment = new CreateLocation();
                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.fragment_container, createFragment);
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Close the popup window
                closePopupWindow();


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

        // 禁用与底层视图的交互
        overlayView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 什么也不做，以防止点击覆盖视图时关闭弹出窗口
            }
        });

    }

    private void closePopupWindow() {
        if (popupWindow != null && popupWindow.isShowing()) {
            popupWindow.dismiss();
            // 关闭弹窗时将 isPopupShowing 置为 false
            isPopupShowing = false;
            destiantion_Name = null;
            destination_latitude = 0;
            destination_longitude = 0;
            if (overlayView != null && overlayView.getParent() != null) {
                ((ViewGroup) overlayView.getParent()).removeView(overlayView);
            }
        }
    }






}
