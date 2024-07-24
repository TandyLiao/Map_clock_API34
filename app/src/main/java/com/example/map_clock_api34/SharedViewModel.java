package com.example.map_clock_api34;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
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
    private Boolean[] vibrate = new Boolean[7];
    private Boolean[] ringtone = new Boolean[7];
    //7個地點有3個提醒時間可供選擇;
    private Boolean[][] notification = new Boolean[7][3];

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
    public void setNotification(Boolean check, int position, int whichTime){ this.notification[position][whichTime] = check; }
    public void setVibrate(Boolean check, int position){ this.vibrate[position] = check; }
    public void setRingtone(Boolean check, int position){ this.ringtone[position] = check; }

    public void setCapital(String capital) {    this.destinationCapital[locationCount] = capital;   }

    public void setArea(String area) {  this.destinationArea[locationCount] = area; }

    public void setnowLocation(double lantitude, double longtitude){
        this.nowLantitude=lantitude;
        this.nowLontitude=longtitude;
    }
    public int getPosition(){ return position; }
    //benson
    public String getNote(int position){    return note[position];  }
    //benson
    public Boolean getNotification(int position, int whichTime){ return notification[position][whichTime]; }
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
        String stemp;

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

}
