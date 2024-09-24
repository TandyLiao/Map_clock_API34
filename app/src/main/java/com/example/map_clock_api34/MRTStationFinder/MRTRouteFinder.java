package com.example.map_clock_api34.MRTStationFinder;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

public class MRTRouteFinder {
    private Context context;
    private Map<String, List<String>> mrtRoutes;

    public MRTRouteFinder(Context context) {
        this.context = context;
        this.mrtRoutes = new HashMap<>();
        loadMRTDataFromAsset();
    }

    // 讀取 CSV 並解析路線和站點資料
    private void loadMRTDataFromAsset() {
        AssetManager assetManager = context.getAssets();
        try {
            InputStream is = assetManager.open("MRT_Route.csv");
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String line;
            while ((line = reader.readLine()) != null) {
                Log.d("MRT", "原始資料: " + line);
                line = line.replaceAll("'", "");
                String[] tokens = line.split(",");

                if (tokens.length < 3) {
                    Log.e("MRT", "資料解析錯誤: " + line);
                    continue;
                }

                String routeId = tokens[0];  // 路線 ID
                Log.d("MRT", "ID: " + routeId);
                List<String> stations = new ArrayList<>();

                // 解析站點資料 (從第三個 token 開始)
                for (int i = 2; i < tokens.length; i++) {
                    String stationName = tokens[i];  // 取得站點名稱
                    stations.add(stationName);  // 將站點加入路線
                    Log.d("MRT", "stationName: " + stationName);
                }

                mrtRoutes.put(routeId, stations);
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 查找站點在哪些路線中
    private List<String> findRoutesByStation(String station) {
        List<String> routes = new ArrayList<>();
        for (Map.Entry<String, List<String>> entry : mrtRoutes.entrySet()) {
            if (entry.getValue().contains(station)) {
                routes.add(entry.getKey());
            }
        }
        return routes;
    }

    // 找尋所有直達或換乘 4 次內的路線
    public List<String> findAllRoutesWithTransfer(String startStation, String endStation) {
        Queue<RouteInfo> queue = new LinkedList<>();
        List<String> allRoutes = new ArrayList<>();

        // 起始路徑初始化
        for (String route : findRoutesByStation(startStation)) {
            queue.add(new RouteInfo(startStation, route, new ArrayList<>(), new HashSet<>()));
        }

        while (!queue.isEmpty()) {
            RouteInfo routeInfo = queue.poll();
            String currentStation = routeInfo.currentStation;
            String currentRoute = routeInfo.currentRoute;

            // 超過換乘次數限制
            if (routeInfo.path.size() > 4) continue;

            // 到達終點
            if (currentStation.equals(endStation)) {
                Log.d("MRT_ROUTE", "找到路線：" + routeInfo.path);
                allRoutes.add(routeInfo.path.toString());
                continue;
            }

            // 遍歷當前路線上的所有站點，並為每條路徑獨立追蹤訪問過的站點
            Set<String> localVisitedStations = new HashSet<>(routeInfo.visitedStations);  // 為當前路徑的訪問站點複製一份
            for (String nextStation : mrtRoutes.get(currentRoute)) {
                if (!localVisitedStations.contains(nextStation)) {
                    localVisitedStations.add(nextStation);
                    RouteInfo newRouteInfo = new RouteInfo(nextStation, currentRoute, new ArrayList<>(routeInfo.path), new HashSet<>(routeInfo.visitedRoutes));
                    newRouteInfo.path.add("搭乘 " + currentRoute + " 到達 " + nextStation);
                    newRouteInfo.visitedStations = localVisitedStations;  // 更新當前路徑的訪問站點
                    queue.add(newRouteInfo);
                }
            }

            // 如果需要換乘，搜尋其他路線，並為每條路徑獨立追蹤訪問過的路線
            if (!routeInfo.visitedRoutes.contains(currentRoute)) {
                routeInfo.visitedRoutes.add(currentRoute);
                for (String newRoute : findRoutesByStation(currentStation)) {
                    if (!newRoute.equals(currentRoute)) {
                        RouteInfo newRouteInfo = new RouteInfo(currentStation, newRoute, new ArrayList<>(routeInfo.path), new HashSet<>(routeInfo.visitedRoutes));
                        newRouteInfo.path.add("換乘 " + newRoute + " 在 " + currentStation);
                        queue.add(newRouteInfo);
                    }
                }
            }
        }

        Log.d("MRT_ROUTE", "共找到 " + allRoutes.size() + " 條路線");
        return allRoutes;
    }

    // 用來存儲路線與換乘資訊的輔助類
    private static class RouteInfo {
        String currentStation;
        String currentRoute;
        List<String> path;
        Set<String> visitedRoutes;
        Set<String> visitedStations;

        RouteInfo(String currentStation, String currentRoute, List<String> path, Set<String> visitedRoutes) {
            this.currentStation = currentStation;
            this.currentRoute = currentRoute;
            this.path = path;
            this.visitedRoutes = visitedRoutes;
            this.visitedStations = new HashSet<>();  // 初始化訪問的站點集合
        }
    }
}
