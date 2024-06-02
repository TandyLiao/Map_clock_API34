package com.example.map_clock_api34.book;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;



public class AppDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "bookmark";
    private static final int DATABASE_VERSION = 1;

    public AppDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(HistoryTable.CREATE_TABLE);
        db.execSQL(LocationTable.CREATE_TABLE);
        db.execSQL(BookmarkTable.CREATE_TABLE);
        db.execSQL(NoteTable.CREATE_TABLE);
        db.execSQL(SettingTable.CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public static class HistoryTable {

        public static final String TABLE_NAME = "history";

        public static final String COLUMN_ROUTE_ID = "route_id";
        public static final String COLUMN_LOCATION_ID = "location_id";
        public static final String COLUMN_ALARM_NAME = "alarm_name";
        public static final String COLUMN_START_TIME = "start_time";

        public static final String CREATE_TABLE =
                "CREATE TABLE " + TABLE_NAME + "("
                        + COLUMN_ROUTE_ID + " INTEGER PRIMARY KEY,"
                        + COLUMN_LOCATION_ID + " INTEGER,"
                        + COLUMN_ALARM_NAME + " TEXT,"
                        + COLUMN_START_TIME + " DATETIME"
                        + ")";
    }

    public static class LocationTable {

        public static final String TABLE_NAME = "location";

        public static final String COLUMN_LOCATION_ID = "location_id";
        public static final String COLUMN_LONGITUDE = "longitude";
        public static final String COLUMN_LATITUDE = "latitude";
        public static final String COLUMN_PLACE_NAME = "place_name";

        public static final String CREATE_TABLE =
                "CREATE TABLE " + TABLE_NAME + "("
                        + COLUMN_LOCATION_ID + " INTEGER,"
                        + COLUMN_LONGITUDE + " REAL,"
                        + COLUMN_LATITUDE + " REAL,"
                        + COLUMN_PLACE_NAME + " TEXT"
                        + ")";
    }

    public static class BookmarkTable {

        public static final String TABLE_NAME = "bookmark";

        public static final String COLUMN_BOOKMARK_ID = "bookmark_id";
        public static final String COLUMN_BOOKMARK_NAME = "bookmark_name";
        public static final String COLUMN_ROUTE_ID = "route_id";

        public static final String CREATE_TABLE =
                "CREATE TABLE " + TABLE_NAME + "("
                        + COLUMN_BOOKMARK_ID + " INTEGER,"
                        + COLUMN_BOOKMARK_NAME + " TEXT,"
                        + COLUMN_ROUTE_ID + " INTEGER"
                        + ")";
    }

    public static class NoteTable {

        public static final String TABLE_NAME = "note";

        public static final String COLUMN_NOTE_ID = "note_id";
        public static final String COLUMN_CONTENT = "content";
        public static final String COLUMN_SETTING_ID = "setting_id";

        public static final String CREATE_TABLE =
                "CREATE TABLE " + TABLE_NAME + "("
                        + COLUMN_NOTE_ID + " INTEGER,"
                        + COLUMN_CONTENT + " TEXT,"
                        + COLUMN_SETTING_ID + " INTEGER"
                        + ")";
    }

    public static class SettingTable {

        public static final String TABLE_NAME = "setting";

        public static final String COLUMN_SETTING_ID = "setting_id";
        public static final String COLUMN_REMINDER_TIME = "reminder_time";
        public static final String COLUMN_REMINDER_DISTANCE = "reminder_distance";
        public static final String COLUMN_VIBRATE = "vibrate";
        public static final String COLUMN_RINGTONE = "ringtone";

        public static final String CREATE_TABLE =
                "CREATE TABLE " + TABLE_NAME + "("
                        + COLUMN_SETTING_ID + " INTEGER,"
                        + COLUMN_REMINDER_TIME + " TEXT,"
                        + COLUMN_REMINDER_DISTANCE + " REAL,"
                        + COLUMN_VIBRATE + " INTEGER,"
                        + COLUMN_RINGTONE + " TEXT"
                        + ")";
    }
}