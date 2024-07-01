package com.example.map_clock_api34.BusAdvice;

public class CityInEnglishHelper {

    public String getCityInEnglish(String city){

        if(city.compareTo("宜蘭縣")==0)    return "F-D0047-001";
        else if(city.compareTo("桃園市")==0)    return "Taoyuan";
        else if(city.compareTo("新竹縣")==0)    return "HsinchuCounty";
        else if(city.compareTo("苗栗縣")==0)    return "MiaoliCounty";
        else if(city.compareTo("宜蘭縣")==0)    return "YilanCounty";
        else if(city.compareTo("彰化縣")==0)    return "ChanghuaCounty";
        else if(city.compareTo("南投縣")==0)    return "NantouCounty";
        else if(city.compareTo("雲林縣")==0)    return "YunlinCounty";
        else if(city.compareTo("嘉義縣")==0)    return "ChiayiCounty";
        else if(city.compareTo("屏東縣")==0)    return "PingtungCounty";
        else if(city.compareTo("台東縣")==0)    return "TaitungCounty";
        else if(city.compareTo("花蓮縣")==0)    return "HualienCounty";
        else if(city.compareTo("澎湖縣")==0)    return "PenghuCounty";
        else if(city.compareTo("基隆市")==0)    return "Keelung";
        else if(city.compareTo("新竹市")==0)    return "Hsinchu";
        else if(city.compareTo("嘉義市")==0)    return "Chiayi";
        else if(city.compareTo("臺北市")==0)    return "Taipei";
        else if(city.compareTo("高雄市")==0)    return "Kaohsiung";
        else if(city.compareTo("新北市")==0)    return "NewTaipei";
        else if(city.compareTo("臺中市")==0)    return "Taichung";
        else if(city.compareTo("台南市")==0)    return "Tainan";
        else if(city.compareTo("連江縣")==0)    return "Lienchiang County";
        else if(city.compareTo("金門市")==0)    return "KinmenCounty";
        else return null;
    }
}
