package com.example.map_clock_api34.MRTStationFinder;

public class TWD97ToWGS84Converter {

    // 定義地球參數
    private static final double a = 6378137.0; // WGS84 橢球體的長半徑
    private static final double b = 6356752.314245; // WGS84 橢球體的短半徑
    private static final double lon0 = 121 * Math.PI / 180; // 台灣的中央經線，轉換為弧度
    private static final double k0 = 0.9999; // 比例因子
    private static final double dx = 250000; // X 偏移量（250,000 公尺）
    private static final double dy = 0; // Y 偏移量通常為 0

    public static double[] TWD97ToWGS84Converter(double x, double y) {
        // 偏移 TWD97 的座標
        x -= dx;
        y -= dy;

        // 計算偏心率
        double e = Math.sqrt(1 - Math.pow(b / a, 2));
        double e2 = Math.pow(e, 2) / (1 - Math.pow(e, 2)); // 第二偏心率

        // 計算墨卡托投影的子午弧長 (Meridional Arc)
        double M = y / k0;
        double mu = M / (a * (1 - Math.pow(e, 2) / 4 - 3 * Math.pow(e, 4) / 64 - 5 * Math.pow(e, 6) / 256));

        // 計算緯度 (Footprint Latitude)
        double e1 = (1 - Math.sqrt(1 - Math.pow(e, 2))) / (1 + Math.sqrt(1 - Math.pow(e, 2)));
        double J1 = (3 * e1 / 2 - 27 * Math.pow(e1, 3) / 32);
        double J2 = (21 * Math.pow(e1, 2) / 16 - 55 * Math.pow(e1, 4) / 32);
        double J3 = (151 * Math.pow(e1, 3) / 96);
        double J4 = (1097 * Math.pow(e1, 4) / 512);
        double fp = mu + J1 * Math.sin(2 * mu) + J2 * Math.sin(4 * mu) + J3 * Math.sin(6 * mu) + J4 * Math.sin(8 * mu);

        // 計算經度和緯度
        double C1 = e2 * Math.pow(Math.cos(fp), 2);
        double T1 = Math.pow(Math.tan(fp), 2);
        double R1 = a * (1 - Math.pow(e, 2)) / Math.pow(1 - Math.pow(e * Math.sin(fp), 2), 1.5);
        double N1 = a / Math.sqrt(1 - Math.pow(e * Math.sin(fp), 2));

        double D = x / (N1 * k0);

        // 計算緯度 (Latitude)
        double lat = fp - (N1 * Math.tan(fp) / R1) * (Math.pow(D, 2) / 2 - (5 + 3 * T1 + 10 * C1 - 4 * Math.pow(C1, 2) - 9 * e2) * Math.pow(D, 4) / 24 + (61 + 90 * T1 + 298 * C1 + 45 * Math.pow(T1, 2) - 252 * e2 - 3 * Math.pow(C1, 2)) * Math.pow(D, 6) / 720);

        // 計算經度 (Longitude)
        double lon = lon0 + (D - (1 + 2 * T1 + C1) * Math.pow(D, 3) / 6 + (5 - 2 * C1 + 28 * T1 - 3 * Math.pow(C1, 2) + 8 * e2 + 24 * Math.pow(T1, 2)) * Math.pow(D, 5) / 120) / Math.cos(fp);

        // 將結果轉換為度數 (Degrees)
        lat = Math.toDegrees(lat);
        lon = Math.toDegrees(lon);

        return new double[]{lat, lon}; // 回傳經緯度
    }
}
