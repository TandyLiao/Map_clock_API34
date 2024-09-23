package com.example.map_clock_api34.MRTStationFinder;

import java.util.ArrayList;
import java.util.List;

public class StaionRecord {

        private String name;
        private float distance;
        private double lat;
        private double lon;
        private List<TransferInfo> transferInfoList = new ArrayList<>();  // 支援多個轉乘資訊

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

        public void setTransferInfo(String stationName, String fromLine, String toLine, double lat, double lon){
            TransferInfo transferInfo = new TransferInfo(stationName, fromLine, toLine, lat, lon);
            transferInfoList.add(transferInfo);  // 加入轉乘資訊列表
        }

        public String toString(){
            return "站名:"+ getName()+"  距離:"+getDistance();
        }
}

class TransferInfo {
    private String stationName;
    private String fromLine;
    private String toLine;
    private double lat;
    private double lon;

    // 建構子
    public TransferInfo(String stationName, String fromLine, String toLine, double lat, double lon) {
        this.stationName = stationName;
        this.fromLine = fromLine;
        this.toLine = toLine;
        this.lat = lat;
        this.lon = lon;
    }

    public String getStationName() {
        return stationName;
    }

    public String getFromLine() {
        return fromLine;
    }

    public String getToLine() {
        return toLine;
    }

    public double getLat() {
        return lat;
    }

    public double getLon() {
        return lon;
    }

    @Override
    public String toString() {
        return "轉乘站: " + stationName + "，從 " + fromLine + " 線轉乘到 " + toLine + " 線，經緯度: (" + lat + ", " + lon + ")";
    }
}