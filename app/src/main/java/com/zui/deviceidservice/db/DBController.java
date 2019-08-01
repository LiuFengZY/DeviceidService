package com.zui.deviceidservice.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class DBController {
    private static final String TAG = "Deviceid-DBController";
    private DBHelper dbh;

    public DBController(Context context) {
        dbh = new DBHelper(context);
    }

    //insert.
    public int insert(AppInfo appinfo) {
        SQLiteDatabase db = dbh.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(AppInfo.KEY_PACKAGE, appinfo.packagename);
        values.put(AppInfo.KEY_DEVELOPER, appinfo.developer);
        values.put(AppInfo.KEY_VAID, appinfo.vaid);
        values.put(AppInfo.KEY_AAID, appinfo.aaid);

        long app_id = db.insert(AppInfo.TABLE, null, values);
        db.close();
        return (int)app_id;
    }

    //delete
    public void delete(AppInfo appinfo) {
        SQLiteDatabase db = dbh.getWritableDatabase();
        db.delete(AppInfo.TABLE, AppInfo.KEY_PACKAGE + "=?", new String[]{String.valueOf(appinfo.packagename)});
        db.close();
    }

    public String getVAIDByPackageName(String strpackage) {
        SQLiteDatabase db = dbh.getWritableDatabase();
        String strsql = "SELECT " +
                AppInfo.KEY_ID + "," +
                AppInfo.KEY_VAID + "," +
                AppInfo.KEY_DEVELOPER + " FROM " + AppInfo.TABLE
                + " WHERE " + AppInfo.KEY_PACKAGE + "=?";
        AppInfo appinfo = new AppInfo();
        Cursor cr = db.rawQuery(strsql, new String[]{strpackage});
        if (cr.moveToFirst()) {
            do {
                appinfo.vaid = cr.getString(cr.getColumnIndex(AppInfo.KEY_VAID));
                appinfo.developer = cr.getString((cr.getColumnIndex(AppInfo.KEY_DEVELOPER)));
            } while (cr.moveToNext());
        }
        cr.close();
        db.close();
        Log.e(TAG, "DB Select, vaid: " + appinfo.vaid + " .developer:" + appinfo.developer);
        return appinfo.vaid;
    }

    public String getAAIDByPackageName(String strpackage) {
        SQLiteDatabase db = dbh.getWritableDatabase();
        String strsql = "SELECT " +
                AppInfo.KEY_ID + "," +
                AppInfo.KEY_AAID + "," +
                AppInfo.KEY_DEVELOPER + " FROM " + AppInfo.TABLE
                + " WHERE " + AppInfo.KEY_PACKAGE + "=?";
        AppInfo appinfo = new AppInfo();
        Cursor cr = db.rawQuery(strsql, new String[]{strpackage});
        if (cr.moveToFirst()) {
            do {
                appinfo.aaid = cr.getString(cr.getColumnIndex(AppInfo.KEY_AAID));
                appinfo.developer = cr.getString((cr.getColumnIndex(AppInfo.KEY_DEVELOPER)));
            } while (cr.moveToNext());
        }
        cr.close();
        db.close();
        Log.e(TAG, "DB Select, vaid: " + appinfo.aaid + " .developer:" + appinfo.developer);
        return appinfo.aaid;
    }

    public boolean isPackageNameExisted(String str) {
        boolean isExisted = false;
        SQLiteDatabase db = dbh.getReadableDatabase();
        String strsql = "SELECT " +
                AppInfo.KEY_ID + " FROM " + AppInfo.TABLE
                + " WHERE " + AppInfo.KEY_PACKAGE + "=?";
        Cursor cr = db.rawQuery(strsql, new String[]{str});
        if (cr.getCount() > 0) {
            isExisted = true;
       }
        cr.close();
        db.close();
        return isExisted;
    }

    public boolean isUserIdExisted(String userid) {
        boolean isExisted = false;
        SQLiteDatabase db = dbh.getReadableDatabase();
        String strsql = "SELECT " +
                AppInfo.KEY_ID + " FROM " + AppInfo.TABLE
                + " WHERE " + AppInfo.KEY_DEVELOPER + "=?";
        Cursor cr = db.rawQuery(strsql, new String[]{userid});
        if (cr.getCount() > 0) {
            isExisted = true;
        }
        cr.close();
        db.close();
        return isExisted;
    }

}
