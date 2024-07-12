package com.example.map_clock_api34.home;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.example.map_clock_api34.Distance;
import com.example.map_clock_api34.R;

import java.util.Arrays;

public class LocationService extends Service {

    private static final String CHANNEL_ID = "location_service_channel";
    private static final String WORK_TAG = "location_update_work";
    private LocationManager locationManager;
    private LocationListener locationListener;

    private double[] latitude;
    private double[] longitude;
    private String[] destinationName;
    private int destinationIndex = 0;

    private double pre_distance, last_distance, speed, time, totalTime = 0;
    private Location startLocation;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("LocationService", "Service is being created");

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                Log.d("LocationService", "Location updated: " + location.toString());
                handleLocationUpdate(location);
            }

            @Override
            public void onProviderEnabled(@NonNull String provider) {
                Log.d("LocationService", "Provider enabled: " + provider);
            }

            @Override
            public void onProviderDisabled(@NonNull String provider) {
                Log.d("LocationService", "Provider disabled: " + provider);
            }
        };

        createNotificationChannel();

        Intent notificationIntent = new Intent(this, StartMapping.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("地圖鬧鐘正在運行")
                .setContentText("我們正在背景中運行，監控您的位置變化。")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentIntent(pendingIntent)
                .build();

        startForeground(1, notification);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("LocationService", "Service is being destroyed");
        if (locationManager != null) {
            locationManager.removeUpdates(locationListener);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("LocationService", "Service is starting");

        if (intent != null) {
            latitude = intent.getDoubleArrayExtra("latitude");
            longitude = intent.getDoubleArrayExtra("longitude");
            destinationName = intent.getStringArrayExtra("destinationName");

            if (latitude == null || longitude == null || destinationName == null) {
                Log.e("LocationService", "Invalid data received, stopping service");
                stopSelf();
                return START_NOT_STICKY;
            }

            Log.d("LocationService", "Received latitude: " + Arrays.toString(latitude));
            Log.d("LocationService", "Received longitude: " + Arrays.toString(longitude));
            Log.d("LocationService", "Received destinationName: " + Arrays.toString(destinationName));
        } else {
            Log.d("LocationService", "Intent is null, stopping service");
            stopSelf();
            return START_NOT_STICKY;
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // Request location updates
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 60000, 0, locationListener);
            Log.d("LocationService", "Location updates requested");

            // Start WorkManager for periodic location updates
            startLocationUpdateWorker();
        } else {
            Log.d("LocationService", "Location permission not granted, stopping service");
            stopSelf();
        }

        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Location Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(serviceChannel);
                Log.d("LocationService", "Notification channel created");
            }
        }
    }

    private void handleLocationUpdate(Location nowLocation) {
        Log.d("LocationService", "Handling location update");

        if (nowLocation == null) {
            Log.e("LocationService", "Current location is null");
            return;
        }

        if (startLocation == null) {
            startLocation = nowLocation;
        }

        totalTime += 60;
        pre_distance = Distance.getDistanceBetweenPointsNew(startLocation.getLatitude(), startLocation.getLongitude(), nowLocation.getLatitude(), nowLocation.getLongitude()) / 1000;
        last_distance = Distance.getDistanceBetweenPointsNew(latitude[destinationIndex], longitude[destinationIndex], nowLocation.getLatitude(), nowLocation.getLongitude()) / 1000;

        Log.d("LocationService", "Pre-distance: " + pre_distance + " km");
        Log.d("LocationService", "Last-distance: " + last_distance + " km");

        if (pre_distance > 0.020) {
            speed = pre_distance / (totalTime / 60 / 60);
            time = Math.round(last_distance / speed * 60);
            Log.d("LocationService", "Speed: " + speed + " km/h");
            Log.d("LocationService", "Estimated time: " + time + " minutes");
        } else {
            totalTime -= 60;
        }

        if (last_distance < 0.05 && time < 3) {
            sendNotification("快到了!(背景執行的)");
        }

        destinationIndex++;
        if (destinationIndex < latitude.length && destinationIndex < longitude.length && destinationIndex < destinationName.length) {
            startLocation = nowLocation;
            Log.d("LocationService", "Moving to next destination: " + destinationName[destinationIndex]);
        } else {
            Log.d("LocationService", "All destinations reached, stopping service");
            stopSelf();
        }
    }

    private void sendNotification(String message) {
        Log.d("LocationService", "Sending notification: " + message);

        Intent intent = new Intent(this, StartMapping.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("地圖鬧鐘")
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.notify((int) System.currentTimeMillis(), builder.build());
        }
    }

    private void startLocationUpdateWorker() {
        Data.Builder dataBuilder = new Data.Builder();
        dataBuilder.putDoubleArray("latitude", latitude);
        dataBuilder.putDoubleArray("longitude", longitude);
        dataBuilder.putStringArray("destinationName", destinationName);

        OneTimeWorkRequest locationUpdateWork = new OneTimeWorkRequest.Builder(LocationWorker.class)
                .setInputData(dataBuilder.build())
                .addTag(WORK_TAG)
                .build();

        WorkManager.getInstance(this).enqueueUniqueWork(
                WORK_TAG,
                ExistingWorkPolicy.REPLACE,
                locationUpdateWork
        );

        Log.d("LocationService", "Location update worker started");
    }

}
