package com.zui.opendeviceidlibrary;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.zui.deviceidservice.IDeviceidInterface;

public class OpenDeviceId {
    private Context mContext = null;
    private IDeviceidInterface mDeviceidInterface;
    private static String TAG = "OpenDeviceId library";
    private static boolean DBG = false;
    private ServiceConnection mConnection;
    private CallBack mCallerCallBack = null;

    public interface CallBack<T> {
        void serviceConnected(T status, OpenDeviceId service);
        //void serviceDisconnected(T service);
    }

    public int init(Context context, OpenDeviceId.CallBack<String> listener) {
        if (context == null) {
            throw new NullPointerException("Context can not be null.");
        }
        // this is app context.
        mContext = context;
        mCallerCallBack = listener;

        mConnection = new ServiceConnection() {
            public synchronized void onServiceConnected(ComponentName className, IBinder service) {
                mDeviceidInterface = IDeviceidInterface.Stub.asInterface(service);
                if (mCallerCallBack != null) {
                    mCallerCallBack.serviceConnected("Deviceid Service Connected", OpenDeviceId.this);
                }
                logPrintI("Service onServiceConnected");
            }
            public void onServiceDisconnected(ComponentName className) {
                mDeviceidInterface = null;
                logPrintI("Service onServiceDisconnected");
            }
        };
        Intent intent = new Intent();
        intent.setClassName("com.zui.deviceidservice", "com.zui.deviceidservice.DeviceidService");
        boolean bindSuccessful = mContext.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        if (bindSuccessful) {
            logPrintI("bindService Successful!");
            return 1;
        } else {
            logPrintI("bindService Failed!");
            return -1;
        }
    }

    public OpenDeviceId() {

    }

    public String getOAID() {
        if (mContext == null) {
            logPrintE("Context is null.");
            throw new IllegalArgumentException("Context is null, must be new OpenDeviceId first");
        }

        try {
            if (mDeviceidInterface != null) {
                return mDeviceidInterface.getOAID();
            }
        } catch (RemoteException e) {
            logPrintE("getOAID error, RemoteException!");
            e.printStackTrace();
        }
        return null;
    }

    public String getUDID() {
        if (mContext == null) {
            logPrintE("Context is null.");
            throw new IllegalArgumentException("Context is null, must be new OpenDeviceId first");
        }

        try {
            if (mDeviceidInterface != null) {
                return mDeviceidInterface.getUDID();
            }
        } catch (RemoteException e) {
            logPrintE("getUDID error, RemoteException!");
            e.printStackTrace();
        } catch (Exception e) {
            logPrintE("getUDID error, Exception!");
            e.printStackTrace();
        }
        return null;
    }

    public boolean isSupported() {
        try {
            if (mDeviceidInterface != null) {
                logPrintI("Device support opendeviceid");
                return mDeviceidInterface.isSupport();
            }
        } catch (RemoteException e) {
            logPrintE("isSupport error, RemoteException!");
            return false;
        }
        return false;
    }

    public String getVAID() {
        if (mContext == null) {
            logPrintI("Context is null.");
            throw new IllegalArgumentException("Context is null, must be new OpenDeviceId first");
        }

        String packagename = mContext.getPackageName();
        logPrintI("liufeng, getVAID package：" + packagename);
        if (packagename != null && !packagename.equals("")) {
            try {
                if (mDeviceidInterface != null) {
                    return mDeviceidInterface.getVAID(packagename);
                }
            } catch (RemoteException e) {
                logPrintE("getVAID error, RemoteException!");
                e.printStackTrace();
            }
        } else {
            logPrintI("input package is null!");
        }
        return null;
    }

    public String getAAID() {
        if (mContext == null) {
            logPrintI("Context is null.");
            throw new IllegalArgumentException("Context is null, must be new OpenDeviceId first");
        }

        String packagename = mContext.getPackageName();
        logPrintI("liufeng, getAAID package：" + packagename);
        String retStr = null;
        if (packagename != null && !packagename.equals("")) {
            try {
                if (mDeviceidInterface != null) {
                    retStr = mDeviceidInterface.getAAID(packagename);
                    if (retStr == null || "".equals(retStr)) {
                        boolean ifCreate = mDeviceidInterface.createAAIDForPackageName(packagename);
                        if (ifCreate) {
                            retStr = mDeviceidInterface.getAAID(packagename);
                        }
                    }
                }
            } catch (RemoteException e) {
                logPrintE("getAAID error, RemoteException!");
            }
        } else {
            logPrintI("input package is null!");
        }
        return retStr;
    }

    public void shutdown() {
        try {
            mContext.unbindService(mConnection);
            logPrintI("unBind Service successful");
        } catch (IllegalArgumentException e) {
            logPrintE("unBind Service exception");
        }
        mDeviceidInterface = null;
    }

    public void setLogEnable(boolean enable) {
        this.DBG = enable;
    }

    private void logPrintI(String str) {
        if (DBG) Log.i(TAG, str);
    }

    private void logPrintE(String str) {
        if (DBG) Log.e(TAG, str);
    }
}
