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

    // 定義常數，用於通知和記錄檔案
    private static final String CHANNEL_ID = "location_service_channel";

    // 位置更新相關變數
    private FusedLocationProviderClient fusedLocationProviderClient; // 使用 FusedLocationProviderClient 進行位置追蹤
    private LocationCallback locationCallback; // 位置更新的回呼函數

    // 設定初始狀態
    private boolean notificationSent = false;   // 通知是否已經發送
    private boolean isVibrating = false;        // 是否正在震動
    private boolean isPlayingRingtone = false;  // 是否正在播放鈴聲

    // 定位相關資料
    private double[] latitude; // 儲存目的地的緯度
    private double[] longitude; // 儲存目的地的經度
    private String[] destinationName; // 儲存目的地的名稱
    private String[] note; // 目的地附註
    private boolean[] vibrate; // 目的地是否震動
    private boolean[] ringtone; // 目的地是否播放鈴聲
    private int[] notification; // 目的地通知時間

    private int destinationIndex = 0; // 目前正在追蹤的目的地索引
    private Ringtone mRingtone; // 鈴聲物件

    private double pre_distance, last_distance, speed, time, totalTime = 0; // 用來計算的變數
    private Location startLocation; // 開始位置

    int temp; // 暫存變數
    boolean isPause = false; // 是否暫停更新

    @SuppressLint("MissingPermission")
    @Override
    public void onCreate() {
        super.onCreate();

        // 註冊廣播接收器，用於接收目標索引的更新
        LocalBroadcastManager.getInstance(this).registerReceiver(destinationServiceReceiver, new IntentFilter("DESTINATIONINDEX_UPDATE"));

        // 重置通知已發送狀態
        resetNotificationSent();
        Log.d("+", "Service is being created");

        // 初始化 FusedLocationProviderClient
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        // 建立位置請求，設定位置更新間隔
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(10000);  // 設定更新間隔為 10 秒
        locationRequest.setFastestInterval(10000);  // 最快的更新間隔
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);  // 設定為高精度

        // 定義位置更新回呼函數
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                // 處理每個位置更新
                for (Location location : locationResult.getLocations()) {
                    handleLocationUpdate(location);
                }
            }
        };

        // 開始請求位置更新
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);

        // 創建通知頻道
        createNotificationChannel();

        // 設定前台服務的通知
        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.putExtra("show_start_mapping", true);

        // PendingIntent 允許點擊通知後開啟主活動
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("地圖鬧鐘正在運行")
                .setContentText("我們正在背景中運行，監控您的位置變化。")
                .setSmallIcon(R.drawable.appicon_tem6) // 設置通知的小圖示
                .setContentIntent(pendingIntent)
                .build();

        // 開始前台服務
        startForeground(1, notification);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("LocationService", "Service is starting");

        if (intent != null) {
            // 處理來自 intent 的數據
            if ("STOP_VIBRATION".equals(intent.getAction())) {
                // 停止震動或鈴聲
                stopVibrate();
                stopRingtone();
                return START_NOT_STICKY;
            }

            // 取得位置、通知、震動等相關參數
            latitude = intent.getDoubleArrayExtra("latitude");
            longitude = intent.getDoubleArrayExtra("longitude");
            destinationName = intent.getStringArrayExtra("destinationName");
            note = intent.getStringArrayExtra("note");
            notification = intent.getIntArrayExtra("notification");
            vibrate = intent.getBooleanArrayExtra("vibrate");
            ringtone = intent.getBooleanArrayExtra("ringtone");

            // 驗證資料是否正確
            if (latitude == null || longitude == null || destinationName == null) {
                Log.e("LocationService", "Invalid data received, stopping service");
                stopSelf();
                return START_NOT_STICKY;
            }

            // 日誌記錄
            Log.d("LocationService", "Received latitude: " + Arrays.toString(latitude));
            Log.d("LocationService", "Received longitude: " + Arrays.toString(longitude));
            Log.d("LocationService", "Received destinationName: " + Arrays.toString(destinationName));
            Log.d("LocationService", "Received vibrate: " + Arrays.toString(vibrate));
            Log.d("LocationService", "Received ringtone: " + Arrays.toString(ringtone));
            Log.d("LocationService", "Received notification: " + Arrays.toString(notification));
        } else {
            // 如果 intent 為空，則停止服務
            Log.d("LocationService", "Intent is null, stopping service");
            stopSelf();
            return START_NOT_STICKY;
        }

        return START_STICKY;
    }

    // 處理位置更新
    private void handleLocationUpdate(Location nowLocation) {
        if (!isPause && destinationIndex < getValidDestinationCount()) {
            Log.d("LocationService", "Handling location update");

            if (startLocation == null) {
                startLocation = nowLocation; // 設定初始位置
            }

            totalTime += 10; // 更新總時間

            // 計算當前距離和剩餘距離
            pre_distance = Distance.getDistanceBetweenPointsNew(startLocation.getLatitude(), startLocation.getLongitude(), nowLocation.getLatitude(), nowLocation.getLongitude()) / 1000;
            last_distance = Distance.getDistanceBetweenPointsNew(latitude[destinationIndex], longitude[destinationIndex], nowLocation.getLatitude(), nowLocation.getLongitude()) / 1000;

            int notificationTime = notification[destinationIndex]; // 取得通知時間
            Log.d("LocationService", "Pre-distance: " + pre_distance + " km");
            Log.d("LocationService", "Last-distance: " + last_distance + " km");

            if (pre_distance > 0.020) {
                // 計算速度與預估時間
                speed = pre_distance / (totalTime / 60 / 60);
                time = Math.round(last_distance / speed * 60);
                sendBroadcast(2);
                Log.d("LocationService", "Speed: " + speed + " km/h");
                Log.d("LocationService", "Estimated time: " + time + " minutes");
            } else {
                totalTime -= 10; // 如果距離過短，減少總時間
            }

            // 當快到目的地時發送通知
            if ((last_distance < 0.15 && time < notificationTime) && !notificationSent) {
                temp = destinationIndex;
                sendBroadcast(1);
                sendNotification("快到了!"); // 發送通知
            }

            // 若接近目的地，更新目標索引
            if (last_distance < 0.05 && time < 1) {
                if (temp == destinationIndex && destinationIndex != getValidDestinationCount() - 1) {
                    destinationIndex++;
                    resetNotificationSent();
                    startLocation = nowLocation; // 更新開始位置
                    sendBroadcast(2);
                    sendBroadcast(4);
                }
            }

            // 如果還有目的地，記錄日誌
            if (destinationIndex < getValidDestinationCount()) {
                Log.d("LocationService", "Moving to next destination: " + destinationName[destinationIndex]);
            } else {
                Log.d("LocationService", "All destinations reached, stopping service");
                sendNotification("到達最後一個目的地");
                stopSelf();
            }
            sendBroadcast(3); // 廣播當前目的地索引
        } else {
            isPause = false; // 取消暫停狀態
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    // 取得有效目的地的數量
    private int getValidDestinationCount() {
        int count = 0;
        for (int i = 0; i < latitude.length; i++) {
            if (destinationName[i] != null) {
                count++;
            }
        }
        return count;
    }

    // 載入鈴聲 Uri
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

    // 發送廣播
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
        }
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
    // 創建通知頻道
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
    // 發送通知
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

    // 播放鈴聲
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

    // 停止鈴聲
    private void stopRingtone() {
        if (mRingtone != null && mRingtone.isPlaying()) {
            mRingtone.stop();
        }
        isPlayingRingtone = false;
    }

    // 開始震動
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

    // 停止震動
    private void stopVibrate() {
        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator != null && isVibrating) {
            vibrator.cancel();
            isVibrating = false;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("LocationService", "Service is being destroyed");

        // 移除位置更新和廣播接收器
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        stopVibrate();
        stopRingtone();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(destinationServiceReceiver);
    }
}
