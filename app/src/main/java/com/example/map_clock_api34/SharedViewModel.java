package com.example.map_clock_api34;

import androidx.lifecycle.ViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.map_clock_api34.Database.AppDatabaseHelper;

public class SharedViewModel extends ViewModel {
    private String[] destinationName = new String[7];
    private String[] destinationCapital = new String[7];
    private String[] destinationArea = new String[7];
    private double[] latitude = new double[7];
    private double[] longitude = new double[7];
    private int i = -1;

    private final MutableLiveData<Boolean> _isDataCleared = new MutableLiveData<>();
    public LiveData<Boolean> isDataCleared = _isDataCleared;


    public void setDestination(String name, double latitude, double longitude) {
        i++;
        this.destinationName[i] = name;
        this.latitude[i] = latitude;
        this.longitude[i] = longitude;
    }

    public void setCapital(String capital) {
        this.destinationCapital[i] = capital;
    }
    public void setArea(String area) {
        this.destinationArea[i] = area;
    }

    public String getCapital(int j) {
        return destinationCapital[j];
    }
    public String getArea(int j) {
        return destinationArea[j];
    }

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
        while (position != i) {
            swap(position, position + 1);
            position++;
        }
        i--;
    }

    public int getI() {
        return i;
    }

    public void setI() {
        i--;
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

    public String[] getDestinationNameArray() {
        return destinationName;
    }

    public double[] getLatitudeArray() {
        return latitude;
    }

    public double[] getLongitudeArray() {
        return longitude;
    }
    public void clearDatabase(AppDatabaseHelper dbHelper) {
        dbHelper.clearAllTables();
        _isDataCleared.setValue(true);
    }

}