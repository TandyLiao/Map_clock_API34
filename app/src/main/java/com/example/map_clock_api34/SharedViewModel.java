package com.example.map_clock_api34;
import androidx.lifecycle.ViewModel;

public class SharedViewModel extends ViewModel {
    private String[] destinationName = new String[7];
    private double[] latitude = new double[7];
    private double[] longitude = new double[7];
    private int i=-1;
    public void setDestination(String name, double latitude, double longitude) {
        i++;
        this.destinationName[i] = name;
        this.latitude[i] = latitude;
        this.longitude[i] = longitude;

    }

    public int getI(){
        return i;
    }
    public void setI(){
        i--;
    }
    public String getDestinationName(int j) {
        return destinationName[j];
    }

    public double getLatitude() {
        return latitude[i];
    }

    public double getLongitude() {
        return longitude[i];
    }
}
