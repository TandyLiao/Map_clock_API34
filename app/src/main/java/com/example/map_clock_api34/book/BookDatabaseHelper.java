package com.example.map_clock_api34.book;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.ContentValues;

public class BookDatabaseHelper extends SQLiteOpenHelper {

    // 定義資料庫名稱
    private static final String DATABASE_NAME = "map_clock_book";
    // 定義資料庫版本號，升級資料庫時使用
    private static final int DATABASE_VERSION = 10;

    public BookDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // 當資料庫第一次創建時調用此方法，負責建立所需的資料表
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(LocationTable2.CREATE_TABLE);    // 執行 SQL 語句創建 LocationTable2 表
        db.execSQL(BookTable.CREATE_TABLE);         // 執行 SQL 語句創建 BookTable 表
    }

    // 當資料庫需要支持外鍵時，會調用此方法
    @Override
    public void onConfigure(SQLiteDatabase bookDB) {
        super.onConfigure(bookDB);
        // 啟用外鍵約束功能
        bookDB.setForeignKeyConstraintsEnabled(true);
    }

    // 當資料庫升級時（版本號改變），會調用此方法
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + BookTable.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + LocationTable2.TABLE_NAME);
        onCreate(db);
    }

    // 清空所有表中的數據
    public void clearAllTables() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        try {
            db.execSQL("DELETE FROM " + BookTable.TABLE_NAME);
            db.execSQL("DELETE FROM " + LocationTable2.TABLE_NAME);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
            db.close();
        }
    }

    // 定義 BookTable 表的結構
    public static class BookTable {
        public static final String TABLE_NAME = "book";  // 表名
        public static final String COLUMN_ROUTE_ID = "route_id";        // 列路線 ID
        public static final String COLUMN_START_TIME = "start_time";    // 開始時間
        public static final String COLUMN_LOCATION_ID = "location_id";  // 位置 ID
        public static final String COLUMN_ALARM_NAME = "alarm_name";    // 鬧鐘名稱
        public static final String COLUMN_ARRANGED_ID = "arranged_id";  // 安排順序 ID

        //創建 BookTable 表
        public static final String CREATE_TABLE =
                "CREATE TABLE " + TABLE_NAME + "("
                        + COLUMN_ROUTE_ID + " INTEGER PRIMARY KEY,"  // 定義 route_id 為主鍵
                        + COLUMN_START_TIME + " DATETIME,"  // 開始時間
                        + COLUMN_LOCATION_ID + " INTEGER,"  // 位置 ID
                        + COLUMN_ALARM_NAME + " TEXT,"      // 鬧鐘名稱
                        + COLUMN_ARRANGED_ID + " TEXT,"     // 安排順序 ID
                        + "FOREIGN KEY(" + COLUMN_LOCATION_ID + ") REFERENCES " + LocationTable2.TABLE_NAME + "(" + LocationTable2.COLUMN_LOCATION_ID + ") ON DELETE CASCADE"  // 外鍵約束，參考 LocationTable2 表，且設置聯級刪除
                        + ")";
    }

    // 定義 LocationTable2 表的結構
    public static class LocationTable2 {
        public static final String TABLE_NAME = "location";             // 表名
        public static final String COLUMN_LOCATION_ID = "location_id";  // 位置 ID
        public static final String COLUMN_LONGITUDE = "longitude";      // 經度
        public static final String COLUMN_LATITUDE = "latitude";        // 緯度
        public static final String COLUMN_PLACE_NAME = "place_name";    // 地點名稱
        public static final String COLUMN_ALARM_NAME = "alarm_name";    // 鬧鐘名稱
        public static final String COLUMN_CITY_NAME = "city_name";      // 城市名稱
        public static final String COLUMN_AREA_NAME = "area_name";      // 地區名稱
        public static final String COLUMN_NOTE_INFO = "note_detail";    // 備註訊息
        public static final String COLUMN_VIBRATE = "vibrate";          // 震動設定
        public static final String COLUMN_RINGTONE = "ringtone";        // 鈴聲設定
        public static final String COLUMN_notificationTime = "notificationTime";  // 通知時間

        // SQL 語句：創建 LocationTable2 表
        public static final String CREATE_TABLE =
                "CREATE TABLE " + TABLE_NAME + "("
                        + COLUMN_LOCATION_ID + " INTEGER PRIMARY KEY,"  // 定義 location_id 為主鍵
                        + COLUMN_LONGITUDE + " REAL,"   // 經度
                        + COLUMN_LATITUDE + " REAL,"    // 緯度
                        + COLUMN_PLACE_NAME + " TEXT,"  // 地點名稱
                        + COLUMN_ALARM_NAME + " TEXT,"  // 鬧鐘名稱
                        + COLUMN_CITY_NAME + " TEXT,"   // 城市名稱
                        + COLUMN_AREA_NAME + " TEXT,"   // 地區名稱
                        + COLUMN_NOTE_INFO + " TEXT,"   // 備註信息
                        + COLUMN_VIBRATE + " INTEGER,"  // 震動設定
                        + COLUMN_RINGTONE + " INTEGER," // 鈴聲設定
                        + COLUMN_notificationTime + " INTEGER"  // 通知時間
                        + ")";
    }
}
