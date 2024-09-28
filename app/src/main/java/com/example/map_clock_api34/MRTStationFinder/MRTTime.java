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
            InputStream is = assetManager.open("MRT_TimePAST.csv");
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
        Log.d("MRT_TEST", stationsWithLines.toString());

        // 逐步處理，每次處理三個元素（起點站、路線、終點站）
        for (int i = 0; i < stationsWithLines.size() - 2; i += 2) {
            String startStation = "捷運" + stationsWithLines.get(i).trim();
            startStation = startStation.replaceAll("[\\p{C}]", "").trim(); // 清理字符

            String lineId = stationsWithLines.get(i + 1).trim();
            lineId = lineId.replaceAll("[^\\x20-\\x7E]", "").trim();  // 清理字符

            String endStation = "捷運" + stationsWithLines.get(i + 2).trim();
            endStation = endStation.replaceAll("[\\p{C}]", "").trim();  // 清理字符

            // 計算這段路程的時間
            totalTime += calculateSegmentTime(startStation, endStation, lineId);

            // 處理換線等候時間
            // 如果下一站是相同站名，表示換線
            if (i + 3 < stationsWithLines.size()) {
                String nextStation = "捷運" + stationsWithLines.get(i + 2).trim();
                String nextLineId = stationsWithLines.get(i + 3).trim();

                // 如果是相同站名但不同路線，表示換線
                if (endStation.equals(nextStation)) {
                    totalTime += 300; // 假設換線等待時間是 300 秒
                    Log.d("MRT_TEST", "在站點：" + endStation + "換線，增加300秒等候時間");
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
        startStation = startStation.trim().replaceAll("[\\s\\p{C}]", "");
        endStation = endStation.trim().replaceAll("[\\s\\p{C}]", "");
        lineId = lineId.trim().replaceAll("[\\s\\p{C}]", "");


        // 優先查找 O1 線
        if (lineId.equals("O")) {
            lineId = "O1";  // 優先查找 O1 線
        }

        for (MRTSegment segment : segments) {
            // 檢查是否匹配到起始站和路線標識
            if (segment.getStartStation().equals(startStation) && segment.getLine().equals(lineId)) {
                startCounting = true;
            }

            // 在匹配的路線上累加時間
            if (startCounting && segment.getLine().equals(lineId)) {
                totalTime += segment.getTravelTime() + segment.getStopTime();
                Log.d("MRTTime", "匹配成功: " + startStation + " 到 " + endStation + "，累積時間: " + totalTime);
            }

            // 找到終點站時結束計算
            if (segment.getEndStation().equals(endStation) && segment.getLine().equals(lineId)) {
                break;
            }
        }

        // 如果找不到，打印更詳細的錯誤資訊
        if (!startCounting) {
            Log.d("MRTSegment", "無法找到起始站：" + startStation + " 和路線：" + lineId);
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
            }

            if (startCounting && segment.getLine().equals("O2")) {
                totalTime += segment.getTravelTime() + segment.getStopTime();
            }

            if (segment.getEndStation().equals(endStation) && segment.getLine().equals("O2")) {
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
