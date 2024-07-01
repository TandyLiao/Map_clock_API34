package com.example.map_clock_api34;

import androidx.lifecycle.ViewModel;

public class SharedViewModel extends ViewModel {

    private int locationCount = -1;

    private String[] destinationName = new String[7];
    private String[] destinationCapital = new String[7];
    private String[] destinationArea = new String[7];

    private double[] latitude = new double[7];
    private double[] longitude = new double[7];

    private double nowLantitude;
    private double nowLontitude;


    public void setDestination(String name, double latitude, double longitude) {
        locationCount++;
        this.destinationName[locationCount] = name;
        this.latitude[locationCount] = latitude;
        this.longitude[locationCount] = longitude;
    }

    public void setCapital(String capital) {
        this.destinationCapital[locationCount] = capital;
    }

    public void setArea(String area) {
        this.destinationArea[locationCount] = area;
    }

    public void setnowLocation(double lantitude, double longtitude){
        this.nowLantitude=lantitude;
        this.nowLontitude=longtitude;
    }

    public String getCapital(int j) {
        return destinationCapital[j];
    }

    public String getArea(int j) {
        return destinationArea[j];
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

    public int getLocationCount() {
        return locationCount;
    }

    public void setLocationCount() {
        locationCount--;
    }

    public String getDestinationName(int j) {
        return destinationName[j];
    }

    public double getLatitude(int j) {
        return latitude[j];
    }

    public double getLongitude(int j) {
        return longitude[j];
    }

}
