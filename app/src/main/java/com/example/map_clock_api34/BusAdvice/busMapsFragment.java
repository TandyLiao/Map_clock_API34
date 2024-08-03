package com.example.map_clock_api34.BusAdvice;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
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
    private GoogleMap mMap;

    private BusStationFinderHelper stationFinder;

    private List<BusStationFinderHelper.BusStation> secondNearByStop;
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

            // 初始化时显示所有站牌
            displayBusStopsOnMap(secondNearByStop, BitmapDescriptorFactory.HUE_BLUE, false);

            // 设置标记点击事件
            mMap.setOnMarkerClickListener(marker -> {
                String stopName = marker.getTitle();
                LatLng position = marker.getPosition();
                // 查找能搭乘的公车路线
                findBusRoutesForStop(stopName, position.latitude, position.longitude);
                return true;
            });
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.home_bus_maps_fragment, container, false);

        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);

        stationFinder = new BusStationFinderHelper(getActivity(), sharedViewModel, this);
        stationFinder.findNearbyStations(v);

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
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });
    }

    private void displayBusStopsOnMap(List<BusStationFinderHelper.BusStation> busStops, float color, boolean transparent) {
        if (busStops == null || busStops.isEmpty()) {
            return;
        }

        for (BusStationFinderHelper.BusStation station : busStops) {
            LatLng position = new LatLng(station.getStopLat(), station.getStopLon());
            Marker marker = mMap.addMarker(new MarkerOptions()
                    .position(position)
                    .title(station.getStopName() + " (" + station.getStopLat() + ", " + station.getStopLon() + ")")
                    .icon(BitmapDescriptorFactory.defaultMarker(color))
                    .alpha(transparent ? 0.3f : 1.0f)); // 透明度设为0.3表示透明
            if (transparent) {
                destinationMarkers.add(marker);
            }
        }
    }

    private void findBusRoutesForStop(String stopName, double lat, double lon) {
        if (stopName == null || stopName.isEmpty()) {
            return;
        }

        for (BusStationFinderHelper.BusStation station : secondNearByStop) {
            if (station.getStopName().equals(stopName.split(" \\(")[0]) && Math.abs(station.getStopLat() - lat) < 0.00001 && Math.abs(station.getStopLon() - lon) < 0.00001) {
                showRoutesDialog(station);
                break;
            }
        }
    }

    private void showRoutesDialog(BusStationFinderHelper.BusStation station) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("可搭路線");

        final Map<String, Map<BusStationFinderHelper.BusStation.LatLng, String>> routes = station.getRoutes();
        final List<String> routeNames = new ArrayList<>(routes.keySet());

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, routeNames);
        builder.setAdapter(adapter, (dialog, which) -> {
            String selectedRoute = routeNames.get(which);
            highlightRouteDestinations(routes.get(selectedRoute));
        });

        builder.setNegativeButton("取消", (dialog, which) -> dialog.dismiss());

        builder.show();
    }

    private void highlightRouteDestinations(Map<BusStationFinderHelper.BusStation.LatLng, String> destinations) {
        resetDestinationMarkers();
        for (Map.Entry<BusStationFinderHelper.BusStation.LatLng, String> entry : destinations.entrySet()) {
            Log.d("highlightRouteDestinations", "Destination stop: " + entry.getValue() + " at " + entry.getKey().getLat() + ", " + entry.getKey().getLon());
            LatLng position = new LatLng(entry.getKey().getLat(), entry.getKey().getLon());
            Marker marker = mMap.addMarker(new MarkerOptions()
                    .position(position)
                    .title(entry.getValue() + " (" + entry.getKey().getLat() + ", " + entry.getKey().getLon() + ")")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                    .alpha(1.0f));
            destinationMarkers.add(marker);
        }
    }

    private void resetDestinationMarkers() {
        for (Marker marker : destinationMarkers) {
            marker.remove();
        }
        destinationMarkers.clear();
    }

    @Override
    public void onBusStationsFound(List<BusStationFinderHelper.BusStation> nearbyStops, List<BusStationFinderHelper.BusStation> destinationStops) {
        secondNearByStop = nearbyStops;
        if (mMap != null) {
            displayBusStopsOnMap(secondNearByStop, BitmapDescriptorFactory.HUE_BLUE, false);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }
}
