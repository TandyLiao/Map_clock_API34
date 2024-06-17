package com.example.map_clock_api34.Database;

//DBData.java

public class DBData {
    private String id;
    private String name;
    private String phone;
    private String hobby;
    private String elseInfo;

    public DBData() {
        // 空的建構子
    }

    public DBData(String id, String name, String phone, String hobby, String elseInfo) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.hobby = hobby;
        this.elseInfo = elseInfo;
    }

    // Getter 和 Setter 方法
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getHobby() {
        return hobby;
    }

    public void setHobby(String hobby) {
        this.hobby = hobby;
    }

    public String getElseInfo() {
        return elseInfo;
    }

    public void setElseInfo(String elseInfo) {
        this.elseInfo = elseInfo;
    }
}