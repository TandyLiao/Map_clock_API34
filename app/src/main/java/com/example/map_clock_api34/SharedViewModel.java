package com.example.map_clock_api34;

import androidx.lifecycle.ViewModel;

// SharedViewModel 類繼承自 ViewModel，用於共享應用程式內的數據
public class SharedViewModel extends ViewModel {

    // 當前存儲的目的地數量，初始為 -1 表示尚未設定
    private int locationCount = -1;

    // 存儲目的地的相關信息，最多可存儲 7 個目的地
    private String[] destinationName = new String[7];  // 目的地名稱
    private String[] destinationCapital = new String[7];  // 目的地所在城市
    private String[] destinationArea = new String[7];  // 目的地所在區域

    // 存儲每個目的地的經緯度
    private double[] latitude = new double[7];
    private double[] longitude = new double[7];

    // 存儲當前使用者的位置經緯度
    private double nowLantitude;
    private double nowLontitude;

    // 記事備註
    private String[] note = new String[7];  // 每個目的地的備註

    // 當前選擇的位置索引
    private int position = 0;

    // 鈴聲和震動設置，初始設置為 true
    private boolean[] vibrate = {true, true, true, true, true, true, true};  // 震動開關
    private boolean[] ringtone = {true, true, true, true, true, true, true};  // 鈴聲開關

    // 通知提醒的時間，初始為 5 分鐘
    private int[] notificationTime = {5, 5, 5, 5, 5, 5, 5};  // 每個目的地的通知提醒時間

    // 其他附加信息
    public String time, routeName, uuid;

    // 設置新的目的地
    public void setDestination(String name, double latitude, double longitude) {
        locationCount++;
        this.destinationName[locationCount] = name;
        this.latitude[locationCount] = latitude;
        this.longitude[locationCount] = longitude;
    }

    // 設置當前選中的位置索引
    public void setPosition(int position) {
        this.position = position;
    }

    // 設置指定位置的記事備註
    public void setNote(String note, int position) {
        this.note[position] = note;
    }

    // 設置指定位置的通知提醒時間
    public void setNotification(int whichTime, int position) {
        this.notificationTime[position] = whichTime;
    }

    // 設置指定位置的震動開關
    public void setVibrate(Boolean check, int position) {
        this.vibrate[position] = check;
    }

    // 設置指定位置的鈴聲開關
    public void setRingtone(Boolean check, int position) {
        this.ringtone[position] = check;
    }

    // 設置目的地所在的城市
    public void setCapital(String capital) {
        this.destinationCapital[locationCount] = capital;
    }

    // 設置目的地所在的區域
    public void setArea(String area) {
        this.destinationArea[locationCount] = area;
    }

    // 設置當前使用者的位置經緯度
    public void setNowLocation(double latitude, double longitude) {
        this.nowLantitude = latitude;
        this.nowLontitude = longitude;
    }

    // 將指定的目的地設為首選目的地
    public void setFirstDestination(String name, String busArea, String busCity, double latitude, double longitude) {
        if (locationCount >= 7) {
            return;
        }

        // 將所有已存在的目的地後移一個位置
        for (int i = locationCount + 1; i > 0; i--) {
            swap(i, i - 1);
        }

        // 將新的目的地插入到第一位
        this.destinationName[0] = name;
        this.latitude[0] = latitude;
        this.longitude[0] = longitude;
        this.destinationArea[0] = busArea;
        this.destinationCapital[0] = busCity;
        locationCount++;
    }

    // 返回當前選中的位置索引
    public int getPosition() {
        return position;
    }

    // 返回指定位置的記事備註
    public String getNote(int position) {
        return note[position];
    }

    // 返回指定位置的通知提醒時間
    public int getNotification(int position) {
        return notificationTime[position];
    }

    // 返回指定位置的震動開關狀態
    public Boolean getVibrate(int position) {
        return vibrate[position];
    }

    // 返回指定位置的鈴聲開關狀態
    public Boolean getRingtone(int position) {
        return ringtone[position];
    }

    // 返回指定位置的目的地所在城市
    public String getCapital(int position) {
        return destinationCapital[position];
    }

    // 返回指定位置的目的地所在區域
    public String getArea(int position) {
        return destinationArea[position];
    }

    // 返回當前使用者的緯度
    public Double getNowLantitude() {
        return nowLantitude;
    }

    // 返回當前使用者的經度
    public Double getNowLontitude() {
        return nowLontitude;
    }

    // 交換兩個位置的目的地信息
    public void swap(int start, int end) {
        double temp;
        int itemp;
        String stemp;
        Boolean btemp;

        // 交換經緯度
        temp = latitude[start];
        latitude[start] = latitude[end];
        latitude[end] = temp;

        temp = longitude[start];
        longitude[start] = longitude[end];
        longitude[end] = temp;

        // 交換目的地名稱
        stemp = destinationName[start];
        destinationName[start] = destinationName[end];
        destinationName[end] = stemp;

        // 交換目的地所在城市和區域
        stemp = destinationCapital[start];
        destinationCapital[start] = destinationCapital[end];
        destinationCapital[end] = stemp;

        stemp = destinationArea[start];
        destinationArea[start] = destinationArea[end];
        destinationArea[end] = stemp;

        // 交換記事備註
        stemp = note[start];
        note[start] = note[end];
        note[end] = stemp;

        // 交換鈴聲和震動設置
        btemp = ringtone[start];
        ringtone[start] = ringtone[end];
        ringtone[end] = btemp;

        // 交換震動設置
        btemp = vibrate[start];
        vibrate[start] = vibrate[end];
        vibrate[end] = btemp;

        // 交換通知時間
        itemp = notificationTime[start];
        notificationTime[start] = notificationTime[end];
        notificationTime[end] = itemp;
    }

    // 刪除指定位置的目的地
    public void delet(int position) {
        while (position != locationCount) {
            swap(position, position + 1);
            position++;
        }
        locationCount--;
    }

    // 清除所有目的地數據
    public void clearAll() {
        locationCount = -1;

        destinationName = new String[7];
        destinationCapital = new String[7];
        destinationArea = new String[7];
        latitude = new double[7];
        longitude = new double[7];
        note = new String[7];

        nowLantitude = 0;
        nowLontitude = 0;

        vibrate = new boolean[]{true, true, true, true, true, true, true};
        ringtone = new boolean[]{true, true, true, true, true, true, true};
        notificationTime = new int[]{5, 5, 5, 5, 5, 5, 5};
    }

    // 返回當前目的地數量
    public int getLocationCount() {
        return locationCount;
    }

    // 減少目的地數量
    public void setLocationCount() {
        locationCount--;
    }

    // 返回指定位置的目的地名稱
    public String getDestinationName(int position) {
        return destinationName[position];
    }

    // 返回指定位置的緯度
    public double getLatitude(int position) {
        return latitude[position];
    }

    // 返回指定位置的經度
    public double getLongitude(int position) {
        return longitude[position];
    }

    // 判斷是否存在備註
    public boolean hasNotes() {
        for (int i = 0; i <= locationCount; i++) {
            if (note[i] != null && !note[i].isEmpty()) {
                return true;  // 如果找到非空的 note，返回 true
            }
        }
        return false;  // 所有 note 都為空，返回 false
    }
}
