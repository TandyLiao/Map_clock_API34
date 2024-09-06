package com.example.map_clock_api34;
public class Distance {

    //輸入起始點的經緯度，計算出直線距離in公里
    private static final double EARTH_RADIUS_KM = 6371.0;
    public static double getDistanceBetweenPointsNew(double latitude1, double longitude1, double latitude2, double longitude2) {

            // 將經度和緯度轉換為弧度
            double radLat1 = toRadians(latitude1);
            double radLon1 = toRadians(longitude1);
            double radLat2 = toRadians(latitude2);
            double radLon2 = toRadians(longitude2);

            // 計算兩個點的緯度和經度的差值
            double deltaLat = radLat2 - radLat1;
            double deltaLon = radLon2 - radLon1;

            // 使用 Haversine 公式計算球面距離
            double a = Math.pow(Math.sin(deltaLat / 2), 2) +
                    Math.cos(radLat1) * Math.cos(radLat2) *
                            Math.pow(Math.sin(deltaLon / 2), 2);
            double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
            double distance = EARTH_RADIUS_KM * c;

            return Math.round(distance*1000);

    }
    private static double toRadians(double degree) {
        return degree * Math.PI / 180.0;
    }

}
