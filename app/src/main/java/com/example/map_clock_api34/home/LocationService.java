package com.example.map_clock_api34.home;


import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Vibrator;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

import com.example.map_clock_api34.Distance;
import com.example.map_clock_api34.R;
import java.util.Arrays;


public class LocationService extends Service {


    private static final String CHANNEL_ID = "location_service_channel";
    private static final String WORK_TAG = "location_update_work";
    private LocationManager locationManager;
    private LocationListener locationListener;
    private boolean notificationSent = false; // 新增的变量(by設定組)

    private double[] latitude;
    private double[] longitude;
    private String[] destinationName;
    private int destinationIndex = 0;
    private Ringtone mRingtone;

    private double pre_distance, last_distance, speed, time, totalTime = 0;
    private Location startLocation;

    @Override
    public void onCreate() {
        super.onCreate();
        resetNotificationSent();//設定組新增

        Log.d("LocationService", "Service is being created");
        loadSettings();
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

    private void loadSettings() {
        SharedPreferences preferences = getApplicationContext().getSharedPreferences("settings", Context.MODE_PRIVATE);
        boolean isRingtoneEnabled = preferences.getBoolean("ringtone_enabled", false); // 默认值 false
        boolean isVibrationEnabled = preferences.getBoolean("vibration_enabled", false); // 默认值 false
        int notificationTime = preferences.getInt("notification_time", 5); // 默认值 1
        Log.d("Settings_BY_ForeGround", "Ringtone Enabled: " + isRingtoneEnabled);
        Log.d("Settings_BY_ForeGround", "Vibration Enabled: " + isVibrationEnabled);
        Log.d("Settings_BY_ForeGround", "Notification Time: " + notificationTime);
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
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 0, locationListener);
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
        SharedPreferences preferences = getApplicationContext().getSharedPreferences("settings", Context.MODE_PRIVATE);
        int notificationTime = preferences.getInt("notification_time", 5); // 默认值 1
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

        if ((last_distance < 0.5 && time < notificationTime) && !notificationSent) {
            sendNotification("快到了!");
            resetNotificationSent(); // 新增重製通知(6/2新增)

        }
        if (last_distance < 0.01 && time < 3) {
            destinationIndex++;
        }

        if (destinationIndex < latitude.length && destinationIndex < longitude.length && destinationIndex < destinationName.length) {
            startLocation = nowLocation;
            Log.d("LocationService", "Moving to next destination: " + destinationName[destinationIndex]);
        } else {
            Log.d("LocationService", "All destinations reached, stopping service");
            stopSelf();
        }
    }

    private void sendNotification(String message) {
        Context context = getApplicationContext();

        if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) {
            Toast.makeText(context, "未啟用通知權限", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            Intent intent = new Intent(context, StartMapping.class);

            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

            SharedPreferences preferences = context.getSharedPreferences("settings", Context.MODE_PRIVATE);
            boolean isRingtoneEnabled = preferences.getBoolean("ringtone_enabled", false); // 默认值 false
            boolean isVibrationEnabled = preferences.getBoolean("vibration_enabled", false); // 默认值 false

            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setContentTitle("地圖鬧鐘")
                    .setContentText(message)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true);

            if (isRingtoneEnabled) {
                playRingtone();

            }
            if (isVibrationEnabled) {
                startVibrate();
            }
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            notificationManager.notify(0, builder.build());

        } catch (SecurityException e) {
            Toast.makeText(context, "無法發送通知，請求被拒絕", Toast.LENGTH_SHORT).show();
        }
    }

    private void startLocationUpdateWorker() {

        Log.d("LocationService", "Location update worker started");
    }

    public void resetNotificationSent() {
        notificationSent = false;
    }

    private void startVibrate() {
        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator != null && vibrator.hasVibrator()) {
            vibrator.vibrate(1000);
        }
    }

    private void playRingtone() {
        Uri ringtoneUri = loadRingtoneUri();
        if (ringtoneUri != null) {
            mRingtone = RingtoneManager.getRingtone(getApplicationContext(), ringtoneUri);
            if (mRingtone != null) {
                mRingtone.play();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        stopRingtone();
                    }
                }, 3000);
            }
        }
    }

    private void stopRingtone() {
        if (mRingtone != null && mRingtone.isPlaying()) {
            mRingtone.stop();
        }
    }

    private Uri loadRingtoneUri() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String uriString = preferences.getString("ringtone_uri", null);
        return uriString != null ? Uri.parse(uriString) : null;
    }
}

