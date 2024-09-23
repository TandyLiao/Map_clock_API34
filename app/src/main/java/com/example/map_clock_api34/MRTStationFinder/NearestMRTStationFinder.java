package com.example.map_clock_api34.MRTStationFinder;

public class NearestMRTStationFinder {

        private String name;
        private float distance;
        private double lat;
        private double lon;

        public void setNearestMRTStationFinder(String name, float distance, double lat, double lon) {
            this.name = name;
            this.distance = distance;
            this.lat = lat;
            this.lon = lon;
        }

        public String getName() {
            return name;
        }

        public float getDistance() {
            return distance;
        }

        public double getLat() {
            return lat;
        }

        public double getLon() {
            return lon;
        }

        public String toString(){
            return "站名:"+ getName()+"  距離:"+getDistance();
        }

}
