package com.example.map_clock_api34.MRTStationFinder;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class MRTTime {

    private Context context;
    private List<MRTSegment> segments = new ArrayList<>();

    public MRTTime(Context context) {
        this.context = context;
    }

    // MRT 段落類別
    public static class MRTSegment {
        private String line;
        private String startStation;
        private String endStation;
        private int travelTime;
        private int stopTime;

        public MRTSegment(String line, String startStation, String endStation, int travelTime, int stopTime) {
            this.line = line;
            this.startStation = startStation;
            this.endStation = endStation;
            this.travelTime = travelTime;
            this.stopTime = stopTime;
        }

        public String getStartStation() {
            return startStation;
        }

        public String getEndStation() {
            return endStation;
        }

        public String getLine() {
            return line;
        }

        public int getTravelTime() {
            return travelTime;
        }

        public int getStopTime() {
            return stopTime;
        }
    }

    // 讀取 CSV 並儲存捷運段落資料
    public void readCsvFile() {
        AssetManager assetManager = context.getAssets();
        try {
            InputStream is = assetManager.open("MRT_Time.csv");
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] row = line.split(",");
                String routeId = row[0];
                String startStation = row[2];
                String endStation = row[3];
                int travelTime = Integer.parseInt(row[4]);
                int stopTime = Integer.parseInt(row[6]);

                MRTSegment segment = new MRTSegment(routeId, startStation, endStation, travelTime, stopTime);
                segments.add(segment);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 計算多段路線的總時間，包括換線等候時間
    public int calculateRouteTime(List<String> stationsWithLines) {
        int totalTime = 0;
        Log.d("MRT_TEST",stationsWithLines.toString());
        for (int i = 0; i < stationsWithLines.size() - 2; i += 2) {
            // 正確處理站名和路線標識
            String startStation = "捷運"+stationsWithLines.get(i).trim();
            Log.d("MRT_TEST",startStation);
            String lineId = stationsWithLines.get(i + 1).trim();
            Log.d("MRT_TEST",lineId);
            String endStation = "捷運"+stationsWithLines.get(i + 2).trim();
            Log.d("MRT_TEST",endStation);

            // 檢查是否有連續的相同站名
            if (startStation.equals(endStation)) {
                Log.d("MRTTime", "跳過路段: " + startStation + " 到 " + endStation + "（相同站名）");
                continue;
            }

            // 計算這段路線的時間
            totalTime += calculateSegmentTime(startStation, endStation, lineId);

            // 處理換線等候時間
            if (i + 3 < stationsWithLines.size()) {
                String nextLineId = stationsWithLines.get(i + 3).trim();
                if (!lineId.equals(nextLineId)) {
                    Log.d("MRTTime", "從 " + lineId + " 轉到 " + nextLineId + "，加上 300 秒等候時間");
                    totalTime += 300;
                }
            }
        }

        return totalTime;
    }



    // 查詢單段時間
    public int calculateSegmentTime(String startStation, String endStation, String lineId) {
        int totalTime = 0;
        boolean startCounting = false;

        // 確保去掉站名和路線標識的多餘空格和特殊字符
        startStation = startStation.trim();
        endStation = endStation.trim();
        lineId = lineId.trim();

        // 優先查找 O1 線
        if (lineId.equals("O")) {
            lineId = "O1";  // 優先查找 O1 線
        }

        for (MRTSegment segment : segments) {
            // 檢查是否匹配到起始站和路線標識
            if (segment.getStartStation().equals(startStation) && segment.getLine().equals(lineId)) {
                startCounting = true;
                Log.d("MRTSegment", "開始計算: " + segment.getStartStation() + " 到 " + segment.getEndStation() + " 路線: " + segment.getLine());
            }

            // 在匹配的路線上累加時間
            if (startCounting && segment.getLine().equals(lineId)) {
                totalTime += segment.getTravelTime() + segment.getStopTime();
                Log.d("MRTSegment", "累計時間: " + totalTime + " 秒 (行車: " + segment.getTravelTime() + ", 停留: " + segment.getStopTime() + ")");
            }

            // 找到終點站時結束計算
            if (segment.getEndStation().equals(endStation) && segment.getLine().equals(lineId)) {
                Log.d("MRTSegment", "到達終點站: " + endStation + " 結束計算");
                break;
            }
        }

        // 如果 O1 線沒有找到，嘗試查找 O2 線
        if (!startCounting && lineId.equals("O1")) {
            Log.d("MRTSegment", "在 O1 線中未找到，嘗試在 O2 線中查找 " + startStation + " 到 " + endStation);
            return calculateSegmentTimeO2(startStation, endStation);
        }

        if (!startCounting) {
            Log.d("MRTSegment", "無法找到起始站 " + startStation + " 和路線 " + lineId);
        }

        return totalTime;
    }

    // 查找 O2 線的時間
    public int calculateSegmentTimeO2(String startStation, String endStation) {
        int totalTime = 0;
        boolean startCounting = false;

        for (MRTSegment segment : segments) {
            if (segment.getStartStation().equals(startStation) && segment.getLine().equals("O2")) {
                startCounting = true;
                Log.d("MRTSegment", "開始計算 O2 線: " + segment.getStartStation() + " 到 " + segment.getEndStation());
            }

            if (startCounting && segment.getLine().equals("O2")) {
                totalTime += segment.getTravelTime() + segment.getStopTime();
                Log.d("MRTSegment", "累計時間 O2 線: " + totalTime + " 秒");
            }

            if (segment.getEndStation().equals(endStation) && segment.getLine().equals("O2")) {
                Log.d("MRTSegment", "到達終點站: " + endStation + " 結束 O2 線計算");
                break;
            }
        }

        if (!startCounting) {
            Log.d("MRTSegment", "在 O2 線中無法找到 " + startStation + " 到 " + endStation);
        }

        return totalTime;
    }



    // 找到最短路線
    public List<String> findShortestRoute(List<List<String>> allProcessedRoutes) {
        List<String> shortestRoute = null;
        int shortestTime = Integer.MAX_VALUE;

        for (List<String> route : allProcessedRoutes) {
            int routeTime = calculateRouteTime(route);
            if (routeTime < shortestTime) {
                shortestTime = routeTime;
                shortestRoute = route;
            }
        }

        return shortestRoute;
    }
}
