package com.example.map_clock_api34.MRTStationFinder;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

public class MRTShortestPath {

    // 解析 MRT_Route.csv 文件，返回每條路線與其站點的映射
    public static Map<String, List<String>> parseRoutes(Context context, String filename) throws IOException {
        Map<String, List<String>> routes = new HashMap<>();
        AssetManager assetManager = context.getAssets();
        Log.d("MRT", "正在打開文件: " + filename);
        try (InputStream is = assetManager.open(filename);
             BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
            String line;
            br.readLine();  // 跳過表頭
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",", 5);  // 只切分前五個欄位，避免處理包含站點的長列表
                String routeId = parts[1].replace("'", "");
                String stations = parts[4].replace("'", "").replace("{", "").replace("}", "");
                List<String> stationList = Arrays.asList(stations.split(","));
                routes.put(routeId, stationList);

                // Log 調試信息
                Log.d("MRT", "讀取到路線: " + routeId + " 包含的站點: " + stationList);
            }
        } catch (IOException e) {
            Log.e("MRT", "讀取路線文件出錯: " + e.getMessage());
            throw e;
        }
        Log.d("MRT", "路線文件解析完成。總路線數量: " + routes.size());
        return routes;
    }

    public static String standardizeStationName(String station) {
        return station.replace("捷運", "").trim();  // 去掉 "捷運" 前綴並去除多餘空格
    }

    public static Map<String, Map<String, Integer>> parseTravelTimes(Context context, String filename) throws IOException {
        Map<String, Map<String, Integer>> travelTimes = new HashMap<>();
        AssetManager assetManager = context.getAssets();
        Log.d("MRT", "正在打開文件: " + filename);
        try (InputStream is = assetManager.open(filename);
             BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
            String line;
            br.readLine();  // 跳過表頭
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                String stationA = standardizeStationName(parts[2]);  // 標準化站名
                String stationB = standardizeStationName(parts[3]);  // 標準化站名
                int travelTime = Integer.parseInt(parts[4]);
                int stopTime = Integer.parseInt(parts[6]);
                int totalTime = travelTime + stopTime;

                travelTimes.putIfAbsent(stationA, new HashMap<>());
                travelTimes.get(stationA).put(stationB, totalTime);

                // 雙向添加
                travelTimes.putIfAbsent(stationB, new HashMap<>());
                travelTimes.get(stationB).put(stationA, totalTime);

                // Log 調試信息
                Log.d("MRT", "站點 " + stationA + " 到 " + stationB + " 的總時間: " + totalTime + " 秒");
            }
        } catch (IOException e) {
            Log.e("MRT", "讀取時間文件出錯: " + e.getMessage());
            throw e;
        }
        Log.d("MRT", "時間文件解析完成。總連接數量: " + travelTimes.size());
        return travelTimes;
    }

    // Dijkstra 算法來查找最短路徑，返回最短的路徑及其總時間
    public static List<String> findShortestPath(Map<String, Map<String, Integer>> travelTimes, String startStation, String endStation) {
        // 儲存每個車站的最短距離
        Map<String, Integer> distances = new HashMap<>();
        // 儲存前一個車站
        Map<String, String> previousStations = new HashMap<>();
        // 優先佇列 (最小優先隊列) 來選擇最近的車站
        PriorityQueue<String> queue = new PriorityQueue<>(Comparator.comparingInt(distances::get));
        // 已經訪問過的車站
        Set<String> visited = new HashSet<>();

        Log.d("MRT", "開始尋找從 " + startStation + " 到 " + endStation + " 的最短路徑");

        // 初始化起始車站的距離
        distances.put(startStation, 0);
        queue.add(startStation);

        while (!queue.isEmpty()) {
            String currentStation = queue.poll();
            Log.d("MRT", "處理站點: " + currentStation);  // 記錄處理的當前站點

            if (visited.contains(currentStation)) {
                Log.d("MRT", "站點 " + currentStation + " 已訪問，跳過");
                continue;  // 如果已經訪問過這個車站，跳過
            }
            visited.add(currentStation);

            if (currentStation.equals(endStation)) {
                Log.d("MRT", "已找到終點站 " + endStation);
                break;  // 找到終點站，退出循環
            }

            // 找到相鄰的車站
            Map<String, Integer> neighbors = travelTimes.getOrDefault(currentStation, new HashMap<>());
            Log.d("MRT", "站點 " + currentStation + " 的鄰近站點: " + neighbors.keySet());  // 這裡記錄鄰近站點

            for (Map.Entry<String, Integer> neighbor : neighbors.entrySet()) {
                String nextStation = neighbor.getKey();
                int newDist = distances.get(currentStation) + neighbor.getValue();

                // 如果發現更短的路徑，更新距離
                if (newDist < distances.getOrDefault(nextStation, Integer.MAX_VALUE)) {
                    distances.put(nextStation, newDist);
                    previousStations.put(nextStation, currentStation);
                    queue.add(nextStation);

                    // 記錄鄰近站點更新
                    Log.d("MRT", "更新站點 " + nextStation + " 的距離為 " + newDist + " (從站點 " + currentStation + ")");
                }
            }
        }

        // 構建最短路徑
        List<String> path = new ArrayList<>();
        String step = endStation;
        while (previousStations.containsKey(step)) {
            path.add(step);
            step = previousStations.get(step);
        }
        if (!path.isEmpty()) {
            path.add(startStation);  // 加入起始站
            Collections.reverse(path);  // 反轉路徑，因為我們是從終點往回追溯
        }

        Log.d("MRT", "最短路徑: " + path);  // 記錄最終最短路徑
        return path;  // 返回最短路徑
    }

}
