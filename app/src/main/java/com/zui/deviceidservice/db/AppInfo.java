package com.zui.deviceidservice.db;

public class AppInfo {
    // table
    public static final String TABLE = "AppInfo";

    public static final String KEY_ID = "id";
    public static final String KEY_PACKAGE = "package";
    public static final String KEY_DEVELOPER = "developer";
    public static final String KEY_VAID = "vaid";
    public static final String KEY_AAID = "aaid";

    public int app_id;
    public String packagename;
    public String developer;
    public String vaid;
    public String aaid;
}
