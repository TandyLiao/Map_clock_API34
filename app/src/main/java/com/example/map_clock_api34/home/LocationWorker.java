package com.example.map_clock_api34.home;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import android.util.Log;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import com.example.map_clock_api34.Distance;
import android.Manifest;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import com.example.map_clock_api34.R;
import com.example.map_clock_api34.R;
import androidx.work.Data;
import java.util.Arrays;
import android.os.Handler;
import android.os.Looper;

public class LocationWorker extends Worker {

    private LocationManager locationManager;
    private LocationListener locationListener;
    private Context appContext; // 使用應用程式上下文

    private double[] latitude;
    private double[] longitude;
    private String[] destinationName;
    private int destinationIndex = 0;
    private static final String CHANNEL_ID = "location_service_channel";
    private static final String WORK_TAG = "location_update_work";

    private double pre_distance, last_distance, speed, time, totalTime = 0;
    private Location startLocation;

    public LocationWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        this.appContext = context.getApplicationContext(); // 使用應用程式上下文初始化
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.d("LocationWorker", "Worker is doing work");

        // Retrieve input data from Data object
        Data inputData = getInputData();
        latitude = inputData.getDoubleArray("latitude");
        longitude = inputData.getDoubleArray("longitude");
        destinationName = inputData.getStringArray("destinationName");

        if (latitude == null || longitude == null || destinationName == null) {
            Log.e("LocationWorker", "Invalid data received, stopping work");
            return Result.failure();
        }

        Log.d("LocationWorker", "Received latitude: " + Arrays.toString(latitude));
        Log.d("LocationWorker", "Received longitude: " + Arrays.toString(longitude));
        Log.d("LocationWorker", "Received destinationName: " + Arrays.toString(destinationName));

        // Initialize location manager and listener
        locationManager = (LocationManager) appContext.getSystemService(Context.LOCATION_SERVICE);
        if (locationManager == null) {
            Log.e("LocationWorker", "LocationManager is null, stopping work");
            return Result.failure();
        }

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                // 使用 Handler 或主線程操作來處理位置更新
                new Handler(Looper.getMainLooper()).post(() -> handleLocationUpdate(location));
            }

            @Override
            public void onProviderEnabled(@NonNull String provider) {
                // 使用 Handler 或主線程操作來處理提供者啟用事件
                new Handler(Looper.getMainLooper()).post(() -> Log.d("LocationWorker", "Provider enabled: " + provider));
            }

            @Override
            public void onProviderDisabled(@NonNull String provider) {
                // 使用 Handler 或主線程操作來處理提供者禁用事件
                new Handler(Looper.getMainLooper()).post(() -> Log.d("LocationWorker", "Provider disabled: " + provider));
            }
        };

        // 檢查位置權限並請求更新
        if (ContextCompat.checkSelfPermission(appContext, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // 使用 Handler 或主線程操作來請求位置更新
            new Handler(Looper.getMainLooper()).post(() -> {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 60000, 0, locationListener);
                Log.d("LocationWorker", "Location updates requested");
            });
        } else {
            Log.d("LocationWorker", "Location permission not granted, stopping work");
            return Result.failure();
        }

        // 執行您的位置更新邏輯，例如處理距離計算、通知等

        // 完成工作並返回成功
        return Result.success();
    }

    private void handleLocationUpdate(Location nowLocation) {
        Log.d("LocationWorker", "Handling location update");

        if (startLocation == null) {
            startLocation = nowLocation;
        }

        totalTime += 60;
        pre_distance = Distance.getDistanceBetweenPointsNew(startLocation.getLatitude(), startLocation.getLongitude(), nowLocation.getLatitude(), nowLocation.getLongitude()) / 1000;
        last_distance = Distance.getDistanceBetweenPointsNew(latitude[destinationIndex], longitude[destinationIndex], nowLocation.getLatitude(), nowLocation.getLongitude()) / 1000;

        Log.d("LocationWorker", "Pre-distance: " + pre_distance + " km");
        Log.d("LocationWorker", "Last-distance: " + last_distance + " km");

        if (pre_distance > 0.020) {
            speed = pre_distance / (totalTime / 60 / 60);
            time = Math.round(last_distance / speed * 60);
            Log.d("LocationWorker", "Speed: " + speed + " km/h");
            Log.d("LocationWorker", "Estimated time: " + time + " minutes");
        } else {
            totalTime -= 60;
        }

        if (last_distance < 0.05 && time < 3) {
            sendNotification("快到了!(背景執行的)");
        }

        destinationIndex++;
        if (destinationIndex < latitude.length && destinationIndex < longitude.length && destinationIndex < destinationName.length) {
            startLocation = nowLocation;
            Log.d("LocationWorker", "Moving to next destination: " + destinationName[destinationIndex]);
        } else {
            Log.d("LocationWorker", "All destinations reached, stopping work and service");
            stopLocationUpdates(); // 停止位置更新
            stopWorker(); // 停止工作
            stopSelf(); // 停止服务
        }
    }
    private void stopLocationUpdates() {
        if (locationManager != null && locationListener != null) {
            locationManager.removeUpdates(locationListener);
            Log.d("LocationWorker", "Location updates stopped");
        }
    }
    private void stopWorker() {
        Log.d("LocationWorker", "Stopping worker");
        // 在这里添加停止工作的逻辑，例如调用 `cancelWork` 方法
        // 如果您有任何需要清理或取消的工作任务，可以在这里进行处理
    }

    private void stopSelf() {
        Log.d("LocationWorker", "Stopping service");
        // 在这里添加停止服务的逻辑，例如调用 `stopSelf` 方法
        // 如果需要执行清理工作或关闭服务的其他操作，可以在这里完成
    }

    private void sendNotification(String message) {
        Log.d("LocationWorker", "Sending notification: " + message);

        Intent intent = new Intent(appContext, StartMapping.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(appContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(appContext, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("地圖鬧鐘")
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManager notificationManager = (NotificationManager) appContext.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.notify((int) System.currentTimeMillis(), builder.build());
        }
    }
}