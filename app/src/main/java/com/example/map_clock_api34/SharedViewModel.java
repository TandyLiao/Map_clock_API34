package com.example.map_clock_api34;

import androidx.lifecycle.ViewModel;

public class SharedViewModel extends ViewModel {

    private int locationCount = -1;

    private String[] destinationName = new String[7];
    private String[] destinationCapital = new String[7];
    private String[] destinationArea = new String[7];

    private double[] latitude = new double[7];
    private double[] longitude = new double[7];

    //使用者現在經緯度
    private double nowLantitude;
    private double nowLontitude;

    //記事
    private String[] note = new String[7];
    private int position=0;

    //震動鈴聲
    // 震動鈴聲預設為 false
    private boolean[] vibrate = {true, true, true, true, true, true, true};
    private boolean[] ringtone = {true, true, true, true, true, true, true};

    // 提醒時間預設為 5
    private int[] notificationTime = {5, 5, 5, 5, 5, 5, 5};


    public String time, routeName, uuid;

    public void setDestination(String name, double latitude, double longitude) {
        locationCount++;
        this.destinationName[locationCount] = name;
        this.latitude[locationCount] = latitude;
        this.longitude[locationCount] = longitude;
    }

    public void setPosition(int position){ this.position = position;}
    public void setNote(String note, int position){    this.note[position]=note;  }
    //benson
    public void setNotification(int whichTime, int position){ this.notificationTime[position] = whichTime; }
    public void setVibrate(Boolean check, int position){ this.vibrate[position] = check; }
    public void setRingtone(Boolean check, int position){ this.ringtone[position] = check; }

    public void setCapital(String capital) {    this.destinationCapital[locationCount] = capital;   }

    public void setArea(String area) {  this.destinationArea[locationCount] = area; }

    public void setnowLocation(double lantitude, double longtitude){
        this.nowLantitude=lantitude;
        this.nowLontitude=longtitude;
    }

    public void setFirstDestination(String name, String busArea, String busCity, double latitude, double longitude){
        if(locationCount>=7){ return; }

        for (int i = locationCount+1; i > 0; i--) {
            swap(i, i - 1);
        }

        this.destinationName[0] = name;
        this.latitude[0] = latitude;
        this.longitude[0] = longitude;
        this.destinationArea[0] = busArea;
        this.destinationCapital[0] = busCity;
        locationCount++;
    }

    public int getPosition(){ return position; }
    //benson
    public String getNote(int position){    return note[position];  }
    //benson
    public int getNotification(int position){ return notificationTime[position]; }
    public Boolean getVibrate(int position){ return vibrate[position]; }
    public Boolean getRingtone(int position){ return ringtone[position]; }

    public String getCapital(int position) {
        return destinationCapital[position];
    }

    public String getArea(int position) {
        return destinationArea[position];
    }

    public Double getNowLantitude(){ return nowLantitude; }

    public Double getNowLontitude(){ return nowLontitude; }

    public void swap(int start, int end) {
        double temp;
        int itemp;
        String stemp;
        Boolean btemp;

        temp = latitude[start];
        latitude[start] = latitude[end];
        latitude[end] = temp;

        temp = longitude[start];
        longitude[start] = longitude[end];
        longitude[end] = temp;

        stemp = destinationName[start];
        destinationName[start] = destinationName[end];
        destinationName[end] = stemp;

        stemp = destinationCapital[start];
        destinationCapital[start] = destinationCapital[end];
        destinationCapital[end] = stemp;

        stemp = destinationArea[start];
        destinationArea[start] = destinationArea[end];
        destinationArea[end] = stemp;

        stemp = note[start];
        note[start] = note[end];
        note[end] = stemp;

        btemp = ringtone[start];
        ringtone[start] = ringtone[end];
        ringtone[end] = btemp;

        btemp = vibrate[start];
        vibrate[start] = vibrate[end];
        vibrate[end] = btemp;

        itemp = notificationTime[start];
        notificationTime[start] = notificationTime[end];
        notificationTime[end] = itemp;
    }

    public void delet(int position) {
        while (position != locationCount) {
            swap(position, position + 1);
            position++;
        }
        locationCount--;
    }

    public void clearAll(){
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

    public int getLocationCount() {
        return locationCount;
    }

    public void setLocationCount() {
        locationCount--;
    }

    public String getDestinationName(int position) {
        return destinationName[position];
    }

    public double getLatitude(int position) {
        return latitude[position];
    }

    public double getLongitude(int position) {
        return longitude[position];
    }

    public boolean hasNotes() {
        for (int i = 0; i <= locationCount; i++) {
            if (note[i] != null && !note[i].isEmpty()) {
                return true;  // 如果找到空的 note就返回 true
            }
        }
        return false;  // 如果所有 note 都空返回 false
    }


}
