package com.example.map_clock_api34;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import android.util.Log;

public class SharedViewModel extends ViewModel {
    private MutableLiveData<String[]> destinationName = new MutableLiveData<>(new String[7]);
    private MutableLiveData<String[]> destinationCapital = new MutableLiveData<>(new String[7]);
    private MutableLiveData<double[]> latitude = new MutableLiveData<>(new double[7]);
    private MutableLiveData<double[]> longitude = new MutableLiveData<>(new double[7]);
    private MutableLiveData<Integer> i = new MutableLiveData<>(-1);

    public void setDestination(String name, double latitude, double longitude) {
        int index = i.getValue() + 1;
        this.i.setValue(index);

        String[] destNames = this.destinationName.getValue();
        double[] latitudes = this.latitude.getValue();
        double[] longitudes = this.longitude.getValue();

        if(destNames != null && latitudes != null && longitudes != null) {
            destNames[index] = name;
            latitudes[index] = latitude;
            longitudes[index] = longitude;

            this.destinationName.setValue(destNames);
            this.latitude.setValue(latitudes);
            this.longitude.setValue(longitudes);

            Log.d("SharedViewModel", "Set destination: " + name + ", Latitude: " + latitude + ", Longitude: " + longitude);
        }
    }


    public void setCapital(String capital) {
        String[] capitals = this.destinationCapital.getValue();
        Integer index = this.i.getValue();

        if(capitals != null && index != null) {
            capitals[index] = capital;
            this.destinationCapital.setValue(capitals);
        }
    }

    public String getCapital(int j) {
        String[] capitals = this.destinationCapital.getValue();
        return capitals != null ? capitals[j] : null;
    }

    public void swap(int start, int end) {
        if(start < 0 || end < 0 || start >= 7 || end >= 7) return;

        double[] latitudes = this.latitude.getValue();
        double[] longitudes = this.longitude.getValue();
        String[] destNames = this.destinationName.getValue();

        if(latitudes != null && longitudes != null && destNames != null) {
            double temp = latitudes[start];
            latitudes[start] = latitudes[end];
            latitudes[end] = temp;

            temp = longitudes[start];
            longitudes[start] = longitudes[end];
            longitudes[end] = temp;

            String stemp = destNames[start];
            destNames[start] = destNames[end];
            destNames[end] = stemp;

            this.latitude.setValue(latitudes);
            this.longitude.setValue(longitudes);
            this.destinationName.setValue(destNames);
        }
    }

    public void delet(int position) {
        Integer index = this.i.getValue();
        if (index == null || position < 0 || position >= index) return;

        while (position < index) {
            swap(position, position + 1);
            position++;
        }
        this.i.setValue(index - 1);
    }

    public void setI() {
        Integer index = this.i.getValue();
        if(index != null) {
            this.i.setValue(index - 1);
        }
    }

    public LiveData<String[]> getDestinationNameArray() {
        return destinationName;
    }

    public LiveData<double[]> getLatitudeArray() {
        return latitude;
    }

    public LiveData<double[]> getLongitudeArray() {
        return longitude;
    }

    public LiveData<Integer> getI() {
        return i;
    }

    public void clearData() {
        this.destinationName.setValue(new String[7]);
        this.destinationCapital.setValue(new String[7]);
        this.latitude.setValue(new double[7]);
        this.longitude.setValue(new double[7]);
        this.i.setValue(-1);
        Log.d("SharedViewModel", "All data cleared");
    }
}
