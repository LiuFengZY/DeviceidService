package com.zui.opendeviceidlibrary;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ApplicationInfo;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.zui.deviceidservice.IDeviceidInterface;

public class OpenDeviceId {
    private Context mContext = null;
    private IDeviceidInterface mDeviceidInterface;
    private static String TAG = "OpenDeviceId library";
    private ServiceConnection mConnection;

    public OpenDeviceId(Context context) {
        if (context == null) {
            throw new NullPointerException("Context can not be null.");
        }
        // this is app context.
        mContext = context;
        mConnection = new ServiceConnection() {
            public synchronized void onServiceConnected(ComponentName className, IBinder service) {
                mDeviceidInterface = IDeviceidInterface.Stub.asInterface(service);
                Log.i(TAG, "Service onServiceConnected");
            }
            public void onServiceDisconnected(ComponentName className) {
                mDeviceidInterface = null;
                Log.i(TAG, "Service onServiceDisconnected");
            }
        };
        Intent intent = new Intent();
        intent.setClassName("com.zui.deviceidservice", "com.zui.deviceidservice.DeviceidService");
        boolean bindSuccessful = mContext.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        if (bindSuccessful) {
            Log.i(TAG, "bindService Successful!");
        } else {
            Log.i(TAG, "bindService Failed!");
        }
    }

    public String getOAID() {
        if (mContext == null) {
            Log.i(TAG, "Context is null.");
            throw new IllegalArgumentException("Context is null, must be new OpenDeviceId first");
        }
        String packagename = mContext.getPackageName();
        Log.d(TAG, "liufeng, getOAID package：" + packagename);
        ApplicationInfo appinfo = mContext.getApplicationInfo();
        Log.d(TAG, "liufeng, getOAID appinfo:" + appinfo.className + " ." + appinfo.packageName);

        try {
            if (mDeviceidInterface != null) {
                return mDeviceidInterface.getOAID();
            }
        } catch (RemoteException e) {
            Log.i(TAG, "getOAID error, RemoteException!");
        }
        return null;
    }

    public String getUDID() {
        if (mContext == null) {
            Log.i(TAG, "Context is null.");
            throw new IllegalArgumentException("Context is null, must be new OpenDeviceId first");
        }

        String packagename = mContext.getPackageName();
        Log.d(TAG, "liufeng, getUDID package：" + packagename);
        ApplicationInfo appinfo = mContext.getApplicationInfo();
        Log.d(TAG, "liufeng, getUDID appinfo:" + appinfo.className + " ." + appinfo.packageName);
        try {
            if (mDeviceidInterface != null) {
                return mDeviceidInterface.getUDID();
            }
        } catch (RemoteException e) {
            Log.i(TAG, "getUDID error, RemoteException!");
        }
        return null;
    }

    public boolean isSupported() {
        try {
            if (mDeviceidInterface != null) {
                Log.i(TAG, "Device support opendeviceid");
                return mDeviceidInterface.isSupport();
            }
        } catch (RemoteException e) {
            Log.i(TAG, "isSupport error, RemoteException!");
            return false;
        }
        return false;
    }

    public void shutdown() {
        try {
            mContext.unbindService(mConnection);
            Log.i(TAG, "unBind Service successful");
        } catch (IllegalArgumentException e) {
            Log.i(TAG, "unBind Service exception");
        }
        mDeviceidInterface = null;
    }

    public static void testJar() {
        Log.i(TAG, "This is test jar output");
    }
}
