package com.example.map_clock_api34.Weather;

public class WeatherLocationAreaHelper {

    public String getWeatherLocationArea(String city){

        if(city.compareTo("宜蘭縣")==0)    return "F-D0047-001";
        else if(city.compareTo("桃園市")==0)    return "F-D0047-005";
        else if(city.compareTo("新竹縣")==0)    return "F-D0047-009";
        else if(city.compareTo("苗栗縣")==0)    return "F-D0047-013";
        else if(city.compareTo("宜蘭縣")==0)    return "F-D0047-001";
        else if(city.compareTo("彰化縣")==0)    return "F-D0047-017";
        else if(city.compareTo("南投縣")==0)    return "F-D0047-021";
        else if(city.compareTo("雲林縣")==0)    return "F-D0047-025";
        else if(city.compareTo("嘉義縣")==0)    return "F-D0047-029";
        else if(city.compareTo("屏東縣")==0)    return "F-D0047-033";
        else if(city.compareTo("台東縣")==0)    return "F-D0047-037";
        else if(city.compareTo("花蓮縣")==0)    return "F-D0047-041";
        else if(city.compareTo("澎湖縣")==0)    return "F-D0047-045";
        else if(city.compareTo("基隆市")==0)    return "F-D0047-049";
        else if(city.compareTo("新竹市")==0)    return "F-D0047-053";
        else if(city.compareTo("嘉義市")==0)    return "F-D0047-057";
        else if(city.compareTo("臺北市")==0)    return "F-D0047-061";
        else if(city.compareTo("高雄市")==0)    return "F-D0047-065";
        else if(city.compareTo("新北市")==0)    return "F-D0047-069";
        else if(city.compareTo("臺中市")==0)    return "F-D0047-073";
        else if(city.compareTo("台南市")==0)    return "F-D0047-077";
        else if(city.compareTo("連江縣")==0)    return "F-D0047-081";
        else if(city.compareTo("金門市")==0)    return "F-D0047-085";
        else return null;
    }
}
