package com.example.map_clock_api34.home;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

public class backgroundRun extends Fragment {

    private LocationManager locationManager;
    private LocationListener locationListener;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 初始化位置監聽器
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                // 在這裡處理位置更新的邏輯
                updateLocation(location);
            }

            @Override
            public void onProviderEnabled(@NonNull String provider) {
            }

            @Override
            public void onProviderDisabled(@NonNull String provider) {
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
            }
        };

        // 初始化位置管理器
        locationManager = (LocationManager) requireActivity().getSystemService(Context.LOCATION_SERVICE);

        // 檢查定位權限並啟動位置更新
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 0, locationListener);
        } else {
            // 如果沒有定位權限，可以在這裡要求權限或者提醒用戶授予權限
            Toast.makeText(requireContext(), "需要定位權限以獲取位置更新", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // 停止位置更新
        if (locationManager != null) {
            locationManager.removeUpdates(locationListener);
        }

    }

    // 在這裡處理位置更新的邏輯
    private void updateLocation(Location location) {
        // 在此處更新UI或執行其他任務
    }
}