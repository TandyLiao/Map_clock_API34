package com.example.map_clock_api34.book;

public class BookmarkModel {
    private int bookmarkId;
    private String bookmarkName;
    private int routeId;

    public BookmarkModel(int bookmarkId, String bookmarkName, int routeId){
        this.bookmarkId = bookmarkId;
        this.bookmarkName = bookmarkName;
        this.routeId = routeId;
    }

    public int getBookmarkId(){
        return this.bookmarkId;
    }

    public String getBookmarkName(){
        return this.bookmarkName;
    }

    public int getRouteId(){
        return this.routeId;
    }
}