package com.example.map_clock_api34.Database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.example.map_clock_api34.SharedViewModel;
import android.content.ContentValues;

public class AppDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "map_clock_database";
    private static final int DATABASE_VERSION = 1;
    private SQLiteDatabase database;
    private SharedViewModel sharedViewModel;

    public AppDatabaseHelper(Context context, SharedViewModel sharedViewModel) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.database = this.getWritableDatabase();
        this.sharedViewModel = sharedViewModel;
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
        db.execSQL("DROP TABLE IF EXISTS " + HistoryTable.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + LocationTable.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + BookmarkTable.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + NoteTable.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + SettingTable.TABLE_NAME);
        onCreate(db);
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
                        + COLUMN_START_TIME + " DATETIME,"
                        + "FOREIGN KEY(" + COLUMN_LOCATION_ID + ") REFERENCES " + LocationTable.TABLE_NAME + "(" + LocationTable.COLUMN_LOCATION_ID + ")"
                        + ")";
    }

    public static class LocationTable {
        public static final String TABLE_NAME = "location";
        public static final String COLUMN_LOCATION_ID = "location_id";
        public static final String COLUMN_LONGITUDE = "longitude";
        public static final String COLUMN_LATITUDE = "latitude";
        public static final String COLUMN_PLACE_NAME = "place_name";
        public static final String COLUMN_NOTE_ID = "note_id";
        public static final String COLUMN_SETTING_ID = "setting_id";
        public static final String COLUMN_SORT_ID = "sort_id";

        public static final String CREATE_TABLE =
                "CREATE TABLE " + TABLE_NAME + "("
                        + COLUMN_LOCATION_ID + " INTEGER PRIMARY KEY,"
                        + COLUMN_LONGITUDE + " REAL,"
                        + COLUMN_LATITUDE + " REAL,"
                        + COLUMN_PLACE_NAME + " TEXT,"
                        + COLUMN_NOTE_ID + " INTEGER,"
                        + COLUMN_SETTING_ID + " INTEGER,"
                        + COLUMN_SORT_ID + " INTEGER,"
                        + "FOREIGN KEY(" + COLUMN_NOTE_ID + ") REFERENCES " + NoteTable.TABLE_NAME + "(" + NoteTable.COLUMN_NOTE_ID + "),"
                        + "FOREIGN KEY(" + COLUMN_SETTING_ID + ") REFERENCES " + SettingTable.TABLE_NAME + "(" + SettingTable.COLUMN_SETTING_ID + ")"
                        + ")";
    }

    public static class BookmarkTable {
        public static final String TABLE_NAME = "bookmark";
        public static final String COLUMN_BOOKMARK_ID = "bookmark_id";
        public static final String COLUMN_BOOKMARK_NAME = "bookmark_name";
        public static final String COLUMN_ROUTE_ID = "route_id";

        public static final String CREATE_TABLE =
                "CREATE TABLE " + TABLE_NAME + "("
                        + COLUMN_BOOKMARK_ID + " INTEGER PRIMARY KEY,"
                        + COLUMN_BOOKMARK_NAME + " TEXT,"
                        + COLUMN_ROUTE_ID + " INTEGER,"
                        + "FOREIGN KEY(" + COLUMN_ROUTE_ID + ") REFERENCES " + HistoryTable.TABLE_NAME + "(" + HistoryTable.COLUMN_ROUTE_ID + ")"
                        + ")";
    }

    public static class NoteTable {
        public static final String TABLE_NAME = "note";
        public static final String COLUMN_NOTE_ID = "note_id";
        public static final String COLUMN_CONTENT = "content";
        public static final String COLUMN_SETTING_ID = "setting_id";

        public static final String CREATE_TABLE =
                "CREATE TABLE " + TABLE_NAME + "("
                        + COLUMN_NOTE_ID + " INTEGER PRIMARY KEY,"
                        + COLUMN_CONTENT + " TEXT,"
                        + COLUMN_SETTING_ID + " INTEGER,"
                        + "FOREIGN KEY(" + COLUMN_SETTING_ID + ") REFERENCES " + SettingTable.TABLE_NAME + "(" + SettingTable.COLUMN_SETTING_ID + ")"
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
                        + COLUMN_SETTING_ID + " INTEGER PRIMARY KEY,"
                        + COLUMN_REMINDER_TIME + " TEXT,"
                        + COLUMN_REMINDER_DISTANCE + " REAL,"
                        + COLUMN_VIBRATE + " INTEGER,"
                        + COLUMN_RINGTONE + " TEXT"
                        + ")";
    }

    public void insertDataToDatabase() {
        // Get data from SharedViewModel
        String[] names = sharedViewModel.getDestinationNameArray();
        double[] latitudes = sharedViewModel.getLatitudeArray();
        double[] longitudes = sharedViewModel.getLongitudeArray();

        // Open database in write mode
        SQLiteDatabase db = this.getWritableDatabase();

        // Insert data into the database
        for (int i = 0; i < names.length; i++) {
            if (names[i] != null && latitudes[i] != 0 && longitudes[i] != 0) {
                ContentValues values = new ContentValues();
                values.put(LocationTable.COLUMN_PLACE_NAME, names[i]);
                values.put(LocationTable.COLUMN_LATITUDE, latitudes[i]);
                values.put(LocationTable.COLUMN_LONGITUDE, longitudes[i]);
                db.insert(LocationTable.TABLE_NAME, null, values);
            }
        }

        // Close the database
        db.close();
    }
}