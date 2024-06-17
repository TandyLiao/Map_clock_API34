// DBHelper.java
package com.example.map_clock_api34.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;

public class DBHelper extends SQLiteOpenHelper {

    private static final int DB_VERSION = 1;
    private static final String TABLE_NAME = "MySample";
    public static final String DB_NAME = "SampleList.db"; // 移到這裡

    public DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTableQuery = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + "(" +
                "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "Name TEXT," +
                "Phone TEXT," +
                "Hobby TEXT," +
                "ElseInfo TEXT" +
                ");";
        db.execSQL(createTableQuery);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public void addData(DBData data) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("Name", data.getName());
        values.put("Phone", data.getPhone());
        values.put("Hobby", data.getHobby());
        values.put("ElseInfo", data.getElseInfo());
        db.insert(TABLE_NAME, null, values);
    }

    public void deleteData(String id) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_NAME, "_id = ?", new String[]{id});
    }

    public void modifyData(DBData data) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("Name", data.getName());
        values.put("Phone", data.getPhone());
        values.put("Hobby", data.getHobby());
        values.put("ElseInfo", data.getElseInfo());
        db.update(TABLE_NAME, values, "_id = ?", new String[]{data.getId()});
    }

    public ArrayList<DBData> searchData(String query) {
        SQLiteDatabase db = getReadableDatabase();
        String[] columns = {"_id", "Name", "Phone", "Hobby", "ElseInfo"};
        Cursor cursor = db.query(TABLE_NAME, columns, "Name LIKE ?", new String[]{"%" + query + "%"}, null, null, null);

        ArrayList<DBData> dbDataArrayList = new ArrayList<>();
        while (cursor.moveToNext()) {
            int idIndex = cursor.getColumnIndex("_id");
            int nameIndex = cursor.getColumnIndex("Name");
            int phoneIndex = cursor.getColumnIndex("Phone");
            int hobbyIndex = cursor.getColumnIndex("Hobby");
            int elseInfoIndex = cursor.getColumnIndex("ElseInfo");

            if (idIndex >= 0 && nameIndex >= 0 && phoneIndex >= 0 && hobbyIndex >= 0 && elseInfoIndex >= 0) {
                String id = cursor.getString(idIndex);
                String name = cursor.getString(nameIndex);
                String phone = cursor.getString(phoneIndex);
                String hobby = cursor.getString(hobbyIndex);
                String elseInfo = cursor.getString(elseInfoIndex);

                DBData dbData = new DBData();
                dbData.setId(id);
                dbData.setName(name);
                dbData.setPhone(phone);
                dbData.setHobby(hobby);
                dbData.setElseInfo(elseInfo);
                dbDataArrayList.add(dbData);
            }
        }
        cursor.close();
        return dbDataArrayList;
    }
}