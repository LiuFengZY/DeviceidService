package com.zui.deviceidservice;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class AppManageReceiver extends BroadcastReceiver {

    private static final String APP_ADDED = "android.intent.action.PACKAGE_ADDED";
    private static final String APP_REMOVED = "android.intent.action.PACKAGE_REMOVED";
    private static final String APP_REPLACED = "android.intent.action.PACKAGE_REPLACED";

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        //throw new UnsupportedOperationException("Not yet implemented");
        Log.i("liufeng", "liufeng,app action :" + intent.getAction());
        Log.i("liufeng", "liufeng,app action :" + intent.getDataString());

        if (intent == null)
            return;

        String action = intent.getAction();
        if (action == null) return;

        if (action.equals(APP_ADDED)) {
            Log.i("liufeng", "APP ADDED" + intent.getDataString());
        } else if (action.equals(APP_REMOVED)) {
            Log.i("liufeng", "APP REMOVED：" + intent.getDataString());
        } else if (action.equals(APP_REPLACED)) {
            Log.i("liufeng" , "APP REPLACED:" + intent.getDataString());
        } else {
            return;
        }
    }
}
