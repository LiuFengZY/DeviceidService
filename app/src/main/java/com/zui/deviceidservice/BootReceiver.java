package com.zui.deviceidservice;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootReceiver extends BroadcastReceiver {

    private static final String BOOT_COMPLETED = "android.intent.action.BOOT_COMPLETED";

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        if (intent == null)
            return;

        String action = intent.getAction();
        if (action == null) return;

        if (BOOT_COMPLETED.equals(action)) {
            Intent startService = new Intent(context, DeviceidService.class);
            context.startForegroundService(startService);
            return;
        }
    }
}
