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
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.preference.PreferenceManager;

import com.example.map_clock_api34.Distance;
import com.example.map_clock_api34.MainActivity;
import com.example.map_clock_api34.R;
import java.util.Arrays;



public class LocationService extends Service {

    private static final String CHANNEL_ID = "location_service_channel";
    private static final String WORK_TAG = "location_update_work";

    private LocationManager locationManager;
    private String commandstr = LocationManager.GPS_PROVIDER;

    private boolean notificationSent = false; // 新增的变量
    private boolean isVibrating = false; // 震动状态标志

    private double[] latitude;
    private double[] longitude;
    private String[] destinationName;
    private String[] note;
    private boolean[] vibrate;
    private boolean[] ringtone;
    private int[] notification;

    int notificationTime;
    private int destinationIndex = 0;
    private Ringtone mRingtone;

    private double pre_distance, last_distance, speed, time, totalTime = 0;
    private Location startLocation;

    int temp;
    //背景執行創建第一個啟用的方法
    @SuppressLint("MissingPermission")
    @Override
    public void onCreate() {
        super.onCreate();

        resetNotificationSent(); // 初始化通知状态

        Log.d("+", "Service is being created");

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        createNotificationChannel();
        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.putExtra("show_start_mapping", true); // 添加额外信息

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("地圖鬧鐘正在運行")
                .setContentText("我們正在背景中運行，監控您的位置變化。")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentIntent(pendingIntent)
                .build();

        startForeground(1, notification);
    }

    //背景執行創建後被第二個調用的方法
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("LocationService", "Service is starting");

        if (intent != null) {
            if ("STOP_VIBRATION".equals(intent.getAction())) {
                stopVibrate();
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

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // 10秒更新一次定位
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 0, locationListener);

            Log.d("LocationService", "Location updates requested");
        } else {
            Log.d("LocationService", "Location permission not granted, stopping service");
            stopSelf();
        }

        return START_STICKY;
    }

    public LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(@NonNull Location nowLocation) {

            Log.d("LocationService", "Handling location update");

            if (startLocation == null) {
                startLocation = nowLocation;
            }

            totalTime += 10;
            //這邊~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
            pre_distance = Distance.getDistanceBetweenPointsNew(startLocation.getLatitude(), startLocation.getLongitude(), nowLocation.getLatitude(), nowLocation.getLongitude()) / 1000;
            last_distance = Distance.getDistanceBetweenPointsNew(latitude[destinationIndex], longitude[destinationIndex], nowLocation.getLatitude(), nowLocation.getLongitude()) / 1000;
            notificationTime = notification[destinationIndex];

            Log.d("LocationService", "Pre-distance: " + pre_distance + " km");
            Log.d("LocationService", "Last-distance: " + last_distance + " km");

            if (pre_distance > 0.020) {
                speed = pre_distance / (totalTime / 60 / 60);
                time = Math.round(last_distance / speed * 60);
                Log.d("LocationService", "Speed: " + speed + " km/h");
                Log.d("LocationService", "Estimated time: " + time + " minutes");
            } else {
                totalTime -= 10;
            }

            if ((last_distance < 0.15 && time < notificationTime) && !notificationSent) {
                temp = destinationIndex;
                sendBroadcastWithDestinationIndex();
                sendNotification("快到了!");
                resetNotificationSent(); // 重置通知状态
            }
            //自動更換地點
            if (last_distance < 0.01 && time < 1) {
                if(temp==destinationIndex){
                    destinationIndex++;
                }
            }

            //問~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
            if (destinationIndex < latitude.length && destinationIndex < longitude.length && destinationIndex < destinationName.length) {
                startLocation = nowLocation;
                Log.d("LocationService", "Moving to next destination: " + destinationName[destinationIndex]);
            } else {
                Log.d("LocationService", "All destinations reached, stopping service");
                sendNotification("到達最後一個目的地");
                stopSelf();
            }
        }
    };
    //送目前地點給StartMapping
    private void sendBroadcastWithDestinationIndex() {
        Intent intent = new Intent("DESTINATION_UPDATE");
        intent.putExtra("destinationIndex", destinationIndex);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
    //收StartMapping送來的資料
    private BroadcastReceiver destinationUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getIntExtra("destinationIndex", 0) == destinationIndex){
                destinationIndex++;
            }
        }
    };
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("LocationService", "Service is being destroyed");
        if (locationManager != null) {
            locationManager.removeUpdates(locationListener);
        }
        LocalBroadcastManager.getInstance(this).unregisterReceiver(destinationUpdateReceiver);
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

    private void sendNotification(String message) {
        Context context = getApplicationContext();

        if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) {
            Toast.makeText(context, "未啟用通知權限", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            Intent intent = new Intent(context, MainActivity.class);
            if (message.equals("到達最後一個目的地")) {
                intent.putExtra("show_end_map", true);
            } else {
                intent.putExtra("show_start_mapping", true);
            }
            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            PendingIntent pendingIntent = PendingIntent.getActivity(
                    context,
                    0,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            boolean isRingtoneEnabled = ringtone[destinationIndex];
            boolean isVibrationEnabled = vibrate[destinationIndex];

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

    public void resetNotificationSent() {
        notificationSent = false;
    }

    private void startVibrate() {
        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator != null && vibrator.hasVibrator()) {
            final long vibrationDuration = 3000; // 振动3秒
            final long restDuration = 2000; // 休息2秒
            final long totalDuration = 5 * 60 * 1000; // 总持续时间5分钟

            final Handler handler = new Handler();
            final long endTime = System.currentTimeMillis() + totalDuration;
            isVibrating = true; // 设置震动状态

            Runnable vibrationRunnable = new Runnable() {
                @Override
                public void run() {
                    if (System.currentTimeMillis() < endTime && isVibrating) {
                        vibrator.vibrate(vibrationDuration);
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                vibrator.cancel(); // 停止震动
                                handler.postDelayed(this, restDuration); // 休息2秒
                            }
                        }, vibrationDuration);
                        handler.postDelayed(this, vibrationDuration + restDuration); // 继续下一次振动
                    } else {
                        vibrator.cancel(); // 确保在结束时停止震动
                        isVibrating = false; // 震动结束
                    }
                }
            };

            handler.post(vibrationRunnable); // 开始震动循环
        }
    }

    private void stopVibrate() {
        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator != null && isVibrating) {
            vibrator.cancel(); // 取消震动
            isVibrating = false; // 更新震动状态
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
                }, 3000); // 播放3秒后停止
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