package com.example.map_clock_api34.HistoryDatabase;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * 此類別負責管理應用程式中的 SQLite 資料庫，包括建立、升級和清除資料表。
 * 繼承自 SQLiteOpenHelper，並提供各種操作資料表的方法。
 */
public class HistoryDatabaseHelper extends SQLiteOpenHelper {

    // 定義資料庫名稱及版本
    private static final String DATABASE_NAME = "map_clock_database";
    private static final int DATABASE_VERSION = 13;

    // 建構子，初始化資料庫
    public HistoryDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * 當資料庫第一次被創建時執行，建立所需的資料表。
     * @param db SQLiteDatabase 物件，用來執行 SQL 指令。
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(HistoryTable.CREATE_TABLE);  // 創建歷史紀錄表
        db.execSQL(LocationTable.CREATE_TABLE); // 創建位置表
    }

    /**
     * 當資料庫升級時被執行，清除現有的資料表並重新創建。
     * @param db SQLiteDatabase 物件，用來執行 SQL 指令。
     * @param oldVersion 舊的資料庫版本號。
     * @param newVersion 新的資料庫版本號。
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // 刪除舊的資料表
        db.execSQL("DROP TABLE IF EXISTS " + HistoryTable.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + LocationTable.TABLE_NAME);
        onCreate(db); // 重新創建資料表
    }

    /**
     * 配置資料庫以支援外鍵約束，確保資料表之間的關聯完整性。
     * @param db SQLiteDatabase 物件，用來執行 SQL 指令。
     */
    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true); // 啟用外鍵約束
    }

    /**
     * 清空所有資料表的資料，但保留表結構。
     */
    public void clearAllTables() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction(); // 開始事務
        try {
            db.execSQL("DELETE FROM " + HistoryTable.TABLE_NAME);   // 清除歷史紀錄表資料
            db.execSQL("DELETE FROM " + LocationTable.TABLE_NAME);  // 清除位置表資料
            db.setTransactionSuccessful(); // 提交事務
        } catch (Exception e) {
            e.printStackTrace(); // 捕獲例外錯誤
        } finally {
            db.endTransaction(); // 結束事務
            db.close(); // 關閉資料庫
        }
    }

    // 歷史紀錄表的資料結構
    public static class HistoryTable {
        public static final String TABLE_NAME = "history";
        public static final String COLUMN_ROUTE_ID = "route_id";        // 路線 ID
        public static final String COLUMN_START_TIME = "start_time";    // 起始時間
        public static final String COLUMN_LOCATION_ID = "location_id";  // 位置 ID
        public static final String COLUMN_ALARM_NAME = "alarm_name";    // 鬧鐘名稱
        public static final String COLUMN_ARRANGED_ID = "arranged_id";  // 安排 ID
        public static final String COLUMN_SETTING_ID = "setting_id";    // 設定 ID

        // 創建歷史紀錄表的 SQL 語句
        public static final String CREATE_TABLE =
                "CREATE TABLE " + TABLE_NAME + "("
                        + COLUMN_ROUTE_ID + " INTEGER PRIMARY KEY,"
                        + COLUMN_START_TIME + " DATETIME,"
                        + COLUMN_LOCATION_ID + " INTEGER,"
                        + COLUMN_ALARM_NAME + " TEXT,"
                        + COLUMN_ARRANGED_ID + " TEXT,"
                        + COLUMN_SETTING_ID + " INTEGER,"
                        + "FOREIGN KEY(" + COLUMN_LOCATION_ID + ") REFERENCES " + LocationTable.TABLE_NAME + "(" + LocationTable.COLUMN_LOCATION_ID + ") ON DELETE CASCADE"
                        + ")";
    }

    // 位置表的資料結構
    public static class LocationTable {
        public static final String TABLE_NAME = "location";
        public static final String COLUMN_LOCATION_ID = "location_id";  // 位置 ID
        public static final String COLUMN_LONGITUDE = "longitude";      // 經度
        public static final String COLUMN_LATITUDE = "latitude";        // 緯度
        public static final String COLUMN_PLACE_NAME = "place_name";    // 地點名稱
        public static final String COLUMN_ALARM_NAME = "alarm_name";    // 鬧鐘名稱
        public static final String COLUMN_CITY_NAME = "city_name";      // 城市名稱
        public static final String COLUMN_AREA_NAME = "area_name";      // 地區名稱
        public static final String COLUMN_NOTE_INFO = "note_detail";    // 備註資訊
        public static final String COLUMN_VIBRATE = "vibrate";          // 震動設定
        public static final String COLUMN_RINGTONE = "ringtone";        // 鈴聲設定
        public static final String COLUMN_NOTIFICATION_TIME = "notificationTime"; // 通知時間

        // 創建位置表的 SQL 語句
        public static final String CREATE_TABLE =
                "CREATE TABLE " + TABLE_NAME + "("
                        + COLUMN_LOCATION_ID + " INTEGER PRIMARY KEY,"
                        + COLUMN_LONGITUDE + " REAL,"
                        + COLUMN_LATITUDE + " REAL,"
                        + COLUMN_PLACE_NAME + " TEXT,"
                        + COLUMN_ALARM_NAME + " TEXT,"
                        + COLUMN_CITY_NAME + " TEXT,"
                        + COLUMN_AREA_NAME + " TEXT,"
                        + COLUMN_NOTE_INFO + " TEXT,"
                        + COLUMN_VIBRATE + " INTEGER,"
                        + COLUMN_RINGTONE + " INTEGER,"
                        + COLUMN_NOTIFICATION_TIME + " INTEGER"
                        + ")";
    }
}
