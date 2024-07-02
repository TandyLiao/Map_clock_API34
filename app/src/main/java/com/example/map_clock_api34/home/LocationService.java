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
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import com.example.map_clock_api34.R;
import com.example.map_clock_api34.Distance;
import android.util.Log;
import java.util.Arrays;

public class LocationService extends Service {

    private static final String CHANNEL_ID = "location_service_channel";
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

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                handleLocationUpdate(location);
            }

            @Override
            public void onProviderEnabled(@NonNull String provider) {}

            @Override
            public void onProviderDisabled(@NonNull String provider) {}
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
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            latitude = intent.getDoubleArrayExtra("latitude");
            longitude = intent.getDoubleArrayExtra("longitude");
            destinationName = intent.getStringArrayExtra("destinationName");

            Log.d("LocationService", "Received latitude: " + Arrays.toString(latitude));
            Log.d("LocationService", "Received longitude: " + Arrays.toString(longitude));
            Log.d("LocationService", "Received destinationName: " + Arrays.toString(destinationName));
        } else {
            stopSelf();
            return START_NOT_STICKY;
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 60000, 0, locationListener);
        } else {
            stopSelf();
        }

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (locationManager != null) {
            locationManager.removeUpdates(locationListener);
        }
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
            manager.createNotificationChannel(serviceChannel);
        }
    }

    private void handleLocationUpdate(Location nowLocation) {
        if (startLocation == null) {
            startLocation = nowLocation;
        }

        totalTime += 60;
        pre_distance = Distance.getDistanceBetweenPointsNew(startLocation.getLatitude(), startLocation.getLongitude(), nowLocation.getLatitude(), nowLocation.getLongitude()) / 1000;
        last_distance = Distance.getDistanceBetweenPointsNew(latitude[destinationIndex], longitude[destinationIndex], nowLocation.getLatitude(), nowLocation.getLongitude()) / 1000;

        if (pre_distance > 0.020) {
            speed = pre_distance / (totalTime / 60 / 60);
            time = Math.round(last_distance / speed * 60);
        } else {
            totalTime -= 60;
        }

        if (last_distance < 0.05 && time < 3) {
            sendNotification("快到了!(背景執行的)");
        }
        destinationIndex++;
        if (destinationIndex < latitude.length && destinationIndex < longitude.length && destinationIndex < destinationName.length) {
            // 更新 startLocation 到当前位置，并继续追踪
            startLocation = nowLocation;
        }
    }

    private void sendNotification(String message) {
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
        notificationManager.notify(0, builder.build());
    }
}
