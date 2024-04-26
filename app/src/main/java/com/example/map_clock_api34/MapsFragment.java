package com.example.map_clock_api34;


import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentContainerView;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;

import android.Manifest;
import android.widget.Toast;

import java.util.Arrays;
import java.util.List;

public class MapsFragment extends Fragment {

    private Toolbar toolbar;

    // private PlacesClient placesClient;
    private GoogleMap googleMap;

    private AutocompleteSupportFragment start_autocompleteSupportFragment;

    private LocationManager locationManager;
    private String commandstr = LocationManager.GPS_PROVIDER;
    Location lastLocation;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private boolean locationPermissionGranted = false;
    private Marker destiantion_Marker;
    private GoogleMap mMap;
    double destination_latitude, destination_longitude;
    String destiantion_Name;

    FragmentManager fragmentManager;
    FragmentTransaction fragmentTransaction;
    AutocompleteSupportFragment autocompleteFragment;
    FragmentContainerView fragmentcontainerView;


    private OnMapReadyCallback callback = new OnMapReadyCallback() {

        @SuppressLint("MissingPermission")
        public void onMapReady(GoogleMap googleMap) {
            mMap = googleMap;
            mMap=googleMap;

            //一直更新目前位置
            //locationManager.requestLocationUpdates(commandStr,1000,0,locationListener);

            //取得最後的定位位置
            lastLocation = locationManager.getLastKnownLocation(commandstr);
            if (lastLocation != null) {
                // 跑出藍色定位點
                mMap.setMyLocationEnabled(true);
                // 移动地图视图到最后已知的位置
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude()), 15));
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
                    // Remove all marker
                    mMap.clear();
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
        //Places.initialize(requireContext(), getString(R.string.google_maps_key));
        //placesClient = Places.createClient(requireContext());

        commandstr=LocationManager.GPS_PROVIDER;//NETWORK_PROVIDER;


        ActionBar actionBar = ((AppCompatActivity) requireActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        return inflater.inflate(R.layout.fragment_maps, container, false);

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);

        start_autocompleteSupportFragment = (AutocompleteSupportFragment) getChildFragmentManager()
                .findFragmentById(R.id.start_autocomplete_fragment2);


        ImageView huButton = view.findViewById(R.id.huButton);
        huButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DrawerLayout drawerLayout = getActivity().findViewById(R.id.drawerLayout);
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });

        if (mapFragment != null) {
            mapFragment.getMapAsync(callback);
        }
        iniAutocomplete();
        //搜尋功能監聽器
        start_autocompleteSupportFragment.setOnPlaceSelectedListener(start_autocomplete_Listener);
        toolbar = getActivity().findViewById(R.id.toolbar);

        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

    }

    public void onResume() {
        super.onResume();

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

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted = true;
        } else {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
            Toast.makeText(getActivity(), "權限???", Toast.LENGTH_LONG).show();
        }
    }
    private void iniAutocomplete(){

        String apiKey="AIzaSyCf6tnPO_VbhDJ_EreXXRZes48c7X5giSM";
        if(!Places.isInitialized()){
            Places.initialize(getActivity().getApplicationContext(),apiKey);
        }
        PlacesClient placesClient = Places.createClient(getActivity());

        start_autocompleteSupportFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                // 在此处处理选定的地点
            }

            @Override
            public void onError(@NonNull Status status) {
                // 处理错误情况
            }
        });


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
            //在地圖上標示Marker
            destiantion_Marker=mMap.addMarker(new MarkerOptions().position(destiantion_LatLng).title(place.getName()));
            //把相機移動到選取地點
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(destiantion_LatLng,15));
        }
    };
}
