package com.example.map_clock_api34.home;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.preference.PreferenceManager;

import com.example.map_clock_api34.Distance;
import com.example.map_clock_api34.MainActivity;
import com.example.map_clock_api34.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.util.Arrays;

public class LocationService extends Service {

    private static final String CHANNEL_ID = "location_service_channel";
    private static final String WORK_TAG = "location_update_work";
    private static final String LOG_FILE = "/path/to/location_service.log";

    private FusedLocationProviderClient fusedLocationProviderClient; // 使用 FusedLocationProviderClient
    private LocationCallback locationCallback;

    private boolean notificationSent = false;
    private boolean isVibrating = false;
    private boolean isPlayingRingtone = false;

    private double[] latitude;
    private double[] longitude;
    private String[] destinationName;
    private String[] note;
    private boolean[] vibrate;
    private boolean[] ringtone;
    private int[] notification;

    private int destinationIndex = 0;
    private Ringtone mRingtone;

    private double pre_distance, last_distance, speed, time, totalTime = 0;
    private Location startLocation;

    int temp;
    boolean isPause = false;

    @SuppressLint("MissingPermission")
    @Override
    public void onCreate() {
        super.onCreate();

        // 註冊廣播接收器
        LocalBroadcastManager.getInstance(this).registerReceiver(destinationServiceReceiver, new IntentFilter("DESTINATIONINDEX_UPDATE"));

        resetNotificationSent();
        Log.d("+", "Service is being created");

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        // 請求位置更新
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(10000);  // 設定更新間隔為 10 秒
        locationRequest.setFastestInterval(10000);  // 最快的更新間隔
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);  // 設定為高精度

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                for (Location location : locationResult.getLocations()) {
                    handleLocationUpdate(location);
                }
            }
        };

        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);

        createNotificationChannel();

        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.putExtra("show_start_mapping", true);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("地圖鬧鐘正在運行")
                .setContentText("我們正在背景中運行，監控您的位置變化。")
                .setSmallIcon(R.drawable.appicon_tem6)
                .setContentIntent(pendingIntent)
                .build();

        startForeground(1, notification);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("LocationService", "Service is starting");

        if (intent != null) {
            if ("STOP_VIBRATION".equals(intent.getAction())) {
                stopVibrate();
                stopRingtone();
                return START_NOT_STICKY;
            }

            latitude = intent.getDoubleArrayExtra("latitude");
            longitude = intent.getDoubleArrayExtra("longitude");
            destinationName = intent.getStringArrayExtra("destinationName");
            note = intent.getStringArrayExtra("note");
            notification = intent.getIntArrayExtra("notification");
            vibrate = intent.getBooleanArrayExtra("vibrate");
            ringtone = intent.getBooleanArrayExtra("ringtone");

            if (latitude == null || longitude == null || destinationName == null) {
                Log.e("LocationService", "Invalid data received, stopping service");
                stopSelf();
                return START_NOT_STICKY;
            }

            Log.d("LocationService", "Received latitude: " + Arrays.toString(latitude));
            Log.d("LocationService", "Received longitude: " + Arrays.toString(longitude));
            Log.d("LocationService", "Received destinationName: " + Arrays.toString(destinationName));
            Log.d("LocationService", "Received vibrate: " + Arrays.toString(vibrate));
            Log.d("LocationService", "Received ringtone: " + Arrays.toString(ringtone));
            Log.d("LocationService", "Received notification: " + Arrays.toString(notification));
        } else {
            Log.d("LocationService", "Intent is null, stopping service");
            stopSelf();
            return START_NOT_STICKY;
        }

        return START_STICKY;
    }

    // 處理位置更新
    private void handleLocationUpdate(Location nowLocation) {
        checkGPSStatus();
        if (!isPause && destinationIndex < getValidDestinationCount()) {
            Log.d("LocationService", "Handling location update");

            if (startLocation == null) {
                startLocation = nowLocation;
            }

            totalTime += 10;

            pre_distance = Distance.getDistanceBetweenPointsNew(startLocation.getLatitude(), startLocation.getLongitude(), nowLocation.getLatitude(), nowLocation.getLongitude()) / 1000;
            last_distance = Distance.getDistanceBetweenPointsNew(latitude[destinationIndex], longitude[destinationIndex], nowLocation.getLatitude(), nowLocation.getLongitude()) / 1000;

            int notificationTime = notification[destinationIndex];
            Log.d("LocationService", "Pre-distance: " + pre_distance + " km");
            Log.d("LocationService", "Last-distance: " + last_distance + " km");

            if (pre_distance > 0.020) {
                speed = pre_distance / (totalTime / 60 / 60);
                time = Math.round(last_distance / speed * 60);
                sendBroadcast(2);
                Log.d("LocationService", "Speed: " + speed + " km/h");
                Log.d("LocationService", "Estimated time: " + time + " minutes");
            } else {
                totalTime -= 10;
            }

            if ((last_distance < 0.15 && time < notificationTime) && !notificationSent) {
                temp = destinationIndex;
                sendBroadcast(1);
                sendNotification("快到了!");
            }

            if (last_distance < 0.05 && time < 1) {
                if (temp == destinationIndex && destinationIndex != getValidDestinationCount() - 1) {
                    destinationIndex++;
                    resetNotificationSent();
                    startLocation = nowLocation;
                    sendBroadcast(2);
                    sendBroadcast(4);
                }
            }

            if (destinationIndex < getValidDestinationCount()) {
                Log.d("LocationService", "Moving to next destination: " + destinationName[destinationIndex]);
            } else {
                Log.d("LocationService", "All destinations reached, stopping service");
                sendNotification("到達最後一個目的地");
                stopSelf();
            }
            sendBroadcast(3);
        } else {
            isPause = false;
        }
    }
    // 檢查 GPS 狀態的方法
    private void checkGPSStatus() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        if (!isGPSEnabled) {
            sendBroadcast(5);
        }
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("LocationService", "Service is being destroyed");

        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        stopVibrate();
        stopRingtone();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(destinationServiceReceiver);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(CHANNEL_ID, "Location Service Channel", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(serviceChannel);
                Log.d("LocationService", "Notification channel created");
            }
        }
    }

    private void sendNotification(String message) {
        if (notificationSent) {
            return;
        }

        Context context = getApplicationContext();
        if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) {
            Toast.makeText(context, "未啟用通知權限", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            Intent intent = new Intent(context, MainActivity.class);
            if (!message.equals("到達最後一個目的地")) {
                intent.putExtra("show_start_mapping", true);
                intent.putExtra("triggerSendBroadcast", true);
            }
            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

            boolean isRingtoneEnabled = ringtone[destinationIndex];
            boolean isVibrationEnabled = vibrate[destinationIndex];

            String fullMessage = (note[temp] == null) ? "即將抵達: " + destinationName[temp] : "即將抵達：\n" + destinationName[temp] + "\n\n代辦事項：\n" + note[temp];
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.drawable.appicon_tem6)
                    .setContentTitle("地圖鬧鐘")
                    .setContentText(fullMessage)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(fullMessage));

            if (isRingtoneEnabled) {
                playRingtone();
            }
            if (isVibrationEnabled) {
                startVibrate();
            }

            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            notificationManager.notify(0, builder.build());

            notificationSent = true;

        } catch (SecurityException e) {
            Toast.makeText(context, "無法發送通知，請求被拒絕", Toast.LENGTH_SHORT).show();
        }
    }

    public void resetNotificationSent() {
        notificationSent = false;
    }

    private void cancelNotification() {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());
        notificationManager.cancelAll();
    }

    private void startVibrate() {
        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator != null && vibrator.hasVibrator()) {
            final long vibrationDuration = 3000;
            final long restDuration = 1000;
            final long totalDuration = 5 * 60 * 1000;

            final Handler handler = new Handler();
            final long endTime = System.currentTimeMillis() + totalDuration;
            isVibrating = true;

            Runnable vibrationRunnable = new Runnable() {
                @Override
                public void run() {
                    if (System.currentTimeMillis() < endTime && isVibrating) {
                        vibrator.vibrate(VibrationEffect.createOneShot(vibrationDuration, VibrationEffect.DEFAULT_AMPLITUDE));
                        handler.postDelayed(this, vibrationDuration + restDuration);
                    } else {
                        stopVibrate();
                    }
                }
            };

            handler.post(vibrationRunnable);
        }
    }

    private void stopVibrate() {
        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator != null && isVibrating) {
            vibrator.cancel();
            isVibrating = false;
        }
    }

    private int getValidDestinationCount() {
        int count = 0;
        for (int i = 0; i < latitude.length; i++) {
            if (destinationName[i] != null) {
                count++;
            }
        }
        return count;
    }

    private void playRingtone() {
        Uri ringtoneUri = loadRingtoneUri();
        if (ringtoneUri != null) {
            mRingtone = RingtoneManager.getRingtone(getApplicationContext(), ringtoneUri);
            if (mRingtone != null) {
                final long ringtoneDuration = 3000;
                final long restDuration = 1000;
                final long totalDuration = 5 * 60 * 1000;

                final Handler handler = new Handler();
                final long endTime = System.currentTimeMillis() + totalDuration;
                isPlayingRingtone = true;

                Runnable ringtoneRunnable = new Runnable() {
                    @Override
                    public void run() {
                        if (System.currentTimeMillis() < endTime && isPlayingRingtone) {
                            mRingtone.play();
                            handler.postDelayed(() -> mRingtone.stop(), ringtoneDuration);
                            handler.postDelayed(this, ringtoneDuration + restDuration);
                        } else {
                            stopRingtone();
                        }
                    }
                };

                handler.post(ringtoneRunnable);
            }
        }
    }

    private void stopRingtone() {
        if (mRingtone != null && mRingtone.isPlaying()) {
            mRingtone.stop();
        }
        isPlayingRingtone = false;
    }

    private Uri loadRingtoneUri() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String uriString = preferences.getString("ringtone_uri", null);
        return uriString != null ? Uri.parse(uriString) : null;
    }

    // 廣播接收器，處理 StartMapping 發送的更新
    private BroadcastReceiver destinationServiceReceiver = new BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        @Override
        public void onReceive(Context context, Intent intent) {
            if ("DESTINATIONINDEX_UPDATE".equals(intent.getAction())) {
                if (intent.hasExtra("destinationFinalIndex")) {
                    if (intent.getIntExtra("destinationFinalIndex", 0) < destinationIndex) {
                        destinationIndex++;
                        sendBroadcast(3);
                        resetNotificationSent();
                    }
                }

                if (intent.hasExtra("destinationIndexChange")) {
                    destinationIndex += intent.getIntExtra("destinationIndexChange", 0);
                    sendBroadcast(3);
                    resetNotificationSent();
                    if (Distance.getDistanceBetweenPointsNew(latitude[destinationIndex], longitude[destinationIndex], startLocation.getLatitude(), startLocation.getLongitude()) / 1000 < 0.18) {
                        isPause = true;
                    }
                }

                if (intent.hasExtra("stopVibrateAndRing")) {
                    stopRingtone();
                    stopVibrate();
                }
                if (intent.hasExtra("startVibrateAndRing")) {
                    if (vibrate[intent.getIntExtra("startVibrateAndRing", 0)]) {
                        startVibrate();
                    }
                    if (ringtone[intent.getIntExtra("startVibrateAndRing", 0)]) {
                        playRingtone();
                    }
                }
                if (intent.hasExtra("triggerSendBroadcast")) {
                    if (temp != destinationIndex) {
                    } else {
                        sendBroadcast(1);
                        sendBroadcast(3);
                    }
                }
                if (intent.hasExtra("destroy notification")) {
                    cancelNotification();
                }
            }
        }
    };

    private void sendBroadcast(int dataType) {
        Intent intent = new Intent("DESTINATION_UPDATE");
        switch (dataType) {
            case 1:
                intent.putExtra("destinationIndex", destinationIndex);
                break;
            case 2:
                intent.putExtra("mapInfo", "\n剩餘公里為: " + last_distance + " 公里" + "\n預估走路時間為: " + time + " 分鐘");
                break;
            case 3:
                intent.putExtra("nowIndex", destinationIndex);
                break;
            case 4:
                intent.putExtra("nextDestination", destinationIndex);
                break;
            case 5:
                Log.d("GELO","PASS");
                intent.putExtra("GPSisGone", destinationIndex);
                break;
        }
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
}
