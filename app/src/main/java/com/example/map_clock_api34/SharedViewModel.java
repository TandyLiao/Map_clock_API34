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

    public void swap(int start, int end){
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
    }
    public void delet(int position){

        while(position!=i){
            swap(position,position+1);
            position++;
        }
        i--;

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

    public double getLatitude(int j) {
        return latitude[j];
    }

    public double getLongitude(int j) {
        return longitude[j];
    }
}
