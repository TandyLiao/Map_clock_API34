package com.example.map_clock_api34.book;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
/*
public class BookmarkDAO{
    private SQLiteDatabase db;
    private AppDatabaseHelper appDatabaseHelper;

    public BookmarkDAO (Context context){
        appDatabaseHelper = new AppDatabaseHelper(context);
        db = appDatabaseHelper.getWritableDatabase();
    }

    public void insertBookmark(BookmarkModel bookmark) {
        ContentValues values = new ContentValues();
        values.put(AppDatabaseHelper.BookmarkTable.COLUMN_BOOKMARK_ID, bookmark.getBookmarkId());
        values.put(AppDatabaseHelper.BookmarkTable.COLUMN_BOOKMARK_NAME, bookmark.getBookmarkName());
        values.put(AppDatabaseHelper.BookmarkTable.COLUMN_ROUTE_ID, bookmark.getRouteId());
        db.insert(AppDatabaseHelper.BookmarkTable.TABLE_NAME, null, values);
    }

    public void deleteBookmarkByName(String bookmarkName) {
        db.delete(AppDatabaseHelper.BookmarkTable.TABLE_NAME, AppDatabaseHelper.BookmarkTable.COLUMN_BOOKMARK_NAME + " = ?",
                new String[]{bookmarkName});
    }

}*/