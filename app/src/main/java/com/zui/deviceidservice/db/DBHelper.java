package com.zui.deviceidservice.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBHelper extends SQLiteOpenHelper {
    private static final String TAG = "Deviceid-DBHelper";
    private static final String DATABASE_NAME = "zuideviceid.db";
    private static final int DATABASE_VERSION = 1;

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //create DB table.
        Log.d(TAG, "deviceid DB onCreate.");
        //String sql = "create table user(name varchar(20))";
        String strsql = "CREATE TABLE " + AppInfo.TABLE + "("
                + AppInfo.KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + AppInfo.KEY_PACKAGE +" TEXT, "
                + AppInfo.KEY_DEVELOPER + " TEXT, "
                + AppInfo.KEY_VAID + " TEXT, "
                + AppInfo.KEY_AAID + " TEXT)";
        db.execSQL(strsql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + AppInfo.TABLE);
        onCreate(db);
    }
}
