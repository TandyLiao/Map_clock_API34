package com.example.map_clock_api34;

public class Distance {
    //輸入起始點的經緯度，計算出直線距離in公里
    public static double getDistanceBetweenPointsNew(double latitude1, double longitude1, double latitude2, double longitude2) {

        double theta = Math.abs(longitude1 - longitude2);

        double distance = 60 * 1.1515 * (180/Math.PI) * Math.acos(
                Math.sin(latitude1 * (Math.PI/180)) * Math.sin(latitude2 * (Math.PI/180)) +
                        Math.cos(latitude1 * (Math.PI/180)) * Math.cos(latitude2 * (Math.PI/180)) * Math.cos(theta * (Math.PI/180))
        );

        //因為想讓四捨五入到小數第三位，再用的時候記得除以1000回去
        return Math.round(distance * 1.609344*1000);
    }
}
