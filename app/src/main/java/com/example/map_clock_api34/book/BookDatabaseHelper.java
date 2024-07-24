package com.example.map_clock_api34.book;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.ContentValues;

public class BookDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "map_clock_book";
    private static final int DATABASE_VERSION = 6;

    public BookDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(LocationTable2.CREATE_TABLE);
        db.execSQL(NoteTable.CREATE_TABLE);
        db.execSQL(SettingTable.CREATE_TABLE);
        db.execSQL(BookTable.CREATE_TABLE);
    }
    @Override
    public void onConfigure(SQLiteDatabase bookDB) {
        super.onConfigure(bookDB);
        // Enable foreign key constraints
        bookDB.setForeignKeyConstraintsEnabled(true);
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + BookTable.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + LocationTable2.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + NoteTable.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + SettingTable.TABLE_NAME);
        onCreate(db);
    }

    public void clearAllTables() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        try {
            db.execSQL("DELETE FROM " + BookTable.TABLE_NAME);
            db.execSQL("DELETE FROM " + LocationTable2.TABLE_NAME);
            db.execSQL("DELETE FROM " + NoteTable.TABLE_NAME);
            db.execSQL("DELETE FROM " + SettingTable.TABLE_NAME);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
            db.close();
        }
    }

    public void addBookmark(String alarmName) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(BookTable.COLUMN_ALARM_NAME, alarmName);
        db.insert(BookTable.TABLE_NAME, null, values);
        db.close();
    }

    public static class BookTable {
        public static final String TABLE_NAME = "book";
        public static final String COLUMN_ROUTE_ID = "route_id";
        public static final String COLUMN_START_TIME = "start_time";
        public static final String COLUMN_LOCATION_ID = "location_id";
        public static final String COLUMN_ALARM_NAME = "alarm_name";
        public static final String COLUMN_ARRANGED_ID = "arranged_id";
        public static final String COLUMN_NOTE_ID = "note_id";
        public static final String COLUMN_SETTING_ID = "setting_id";

        public static final String CREATE_TABLE =

                "CREATE TABLE " + TABLE_NAME + "("
                        + COLUMN_ROUTE_ID + " INTEGER PRIMARY KEY,"
                        + COLUMN_START_TIME + " DATETIME,"
                        + COLUMN_LOCATION_ID + " INTEGER,"
                        + COLUMN_ALARM_NAME + " TEXT,"
                        + COLUMN_ARRANGED_ID + " TEXT,"
                        + COLUMN_NOTE_ID + " INTEGER,"
                        + COLUMN_SETTING_ID + " INTEGER,"
                        + "FOREIGN KEY(" + COLUMN_NOTE_ID + ") REFERENCES " + NoteTable.TABLE_NAME + "(" + NoteTable.COLUMN_NOTE_ID + "),"
                        + "FOREIGN KEY(" + COLUMN_SETTING_ID + ") REFERENCES " + SettingTable.TABLE_NAME + "(" + SettingTable.COLUMN_SETTING_ID + "),"
                        + "FOREIGN KEY(" + COLUMN_LOCATION_ID + ") REFERENCES " + LocationTable2.TABLE_NAME + "(" + LocationTable2.COLUMN_LOCATION_ID + ")ON DELETE CASCADE"
                        + ")";
    }

    public static class LocationTable2 {
        public static final String TABLE_NAME = "location";
        public static final String COLUMN_LOCATION_ID = "location_id";
        public static final String COLUMN_LONGITUDE = "longitude";
        public static final String COLUMN_LATITUDE = "latitude";
        public static final String COLUMN_PLACE_NAME = "place_name";
        public static final String COLUMN_ALARM_NAME = "alarm_name";
        public static final String COLUMN_CITY_NAME = "city_name";
        public static final String COLUMN_AREA_NAME = "area_name";



        public static final String CREATE_TABLE =
                "CREATE TABLE " + TABLE_NAME + "("
                        + COLUMN_LOCATION_ID + " INTEGER PRIMARY KEY,"
                        + COLUMN_LONGITUDE + " REAL,"
                        + COLUMN_LATITUDE + " REAL,"
                        + COLUMN_PLACE_NAME + " TEXT,"
                        + COLUMN_ALARM_NAME + " TEXT,"
                        + COLUMN_CITY_NAME + " TEXT,"
                        + COLUMN_AREA_NAME + " TEXT"
                        + ")";
    }

    public static class NoteTable {
        public static final String TABLE_NAME = "note";
        public static final String COLUMN_NOTE_ID = "note_id";
        public static final String COLUMN_CONTENT = "content";
        public static final String COLUMN_SETTING_ID = "setting_id";

        public static final String CREATE_TABLE =
                "CREATE TABLE " + TABLE_NAME + " ("
                        + COLUMN_NOTE_ID + " INTEGER PRIMARY KEY, "
                        + COLUMN_CONTENT + " TEXT, "
                        + COLUMN_SETTING_ID + " INTEGER, "
                        + "FOREIGN KEY(" + COLUMN_SETTING_ID + ") REFERENCES " + SettingTable.TABLE_NAME + "(" + SettingTable.COLUMN_SETTING_ID + "))";
    }

    public static class SettingTable {
        public static final String TABLE_NAME = "setting";
        public static final String COLUMN_SETTING_ID = "setting_id";
        public static final String COLUMN_REMINDER_TIME = "reminder_time";
        public static final String COLUMN_REMINDER_DISTANCE = "reminder_distance";
        public static final String COLUMN_VIBRATE = "vibrate";
        public static final String COLUMN_RINGTONE = "ringtone";

        public static final String CREATE_TABLE =
                "CREATE TABLE " + TABLE_NAME + " ("
                        + COLUMN_SETTING_ID + " INTEGER PRIMARY KEY, "
                        + COLUMN_REMINDER_TIME + " TEXT, "
                        + COLUMN_REMINDER_DISTANCE + " REAL, "
                        + COLUMN_VIBRATE + " INTEGER, "
                        + COLUMN_RINGTONE + " TEXT)";
    }


}
