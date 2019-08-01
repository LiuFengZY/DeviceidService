package com.zui.deviceidservice;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;

public class DeviceidService extends Service {
    private static boolean DBG = true;
    private static String TAG = "Deviceid-MainService";
    private CommonUtils mCommon = null;
    private long mStarttime = 0;

    private BroadcastReceiver mDiServiceReceiver = null;
    private IntentFilter mDiServiceFilter = null;
    private static final String ACTION_ALARM_RESET = "zui.intent.action.ACTION_ALARM_RESET";

    private AppManageReceiver mAppReceiver = null;
    private IntentFilter mAppFilter = null;

    private PendingIntent mAlarmResetIntent = null;

    private void createDeviceidReceiver() {
        if (mDiServiceReceiver != null) return;

        mAppFilter = new IntentFilter();
        mAppFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
        mAppFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        mAppFilter.addDataScheme("package");

        mDiServiceFilter = new IntentFilter();
        mDiServiceFilter.addAction(ACTION_ALARM_RESET);
        mDiServiceFilter.addAction(Intent.ACTION_TIME_CHANGED);
        mAppReceiver = new AppManageReceiver();

        mDiServiceReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                Log.d(TAG, "DiService, liufeng, action:" + action);
                if (action != null && action.equals(ACTION_ALARM_RESET)) {
                    reCreateOAID();
                } else if (action != null && action.equals(Intent.ACTION_TIME_CHANGED)) {
                    long delaytime = mCommon.getOAIDResetDelayTime();
                    long currenttime = System.currentTimeMillis();
                    if ((currenttime - mStarttime > delaytime) && mStarttime != 0) {
                        Log.d(TAG, "currenttime:" + currenttime + " , reCreateOAID");
                        reCreateOAID();
                    }
                }
            }
        };
    }

    private void registeDeviceidReceiver() {
        if (CommonUtils.DBG_EANBLED && mDiServiceReceiver != null && mDiServiceFilter != null) {
            Log.v(TAG, "liufeng, registe device id sreceiver");
            this.registerReceiver(mDiServiceReceiver,mDiServiceFilter);
        }
        if (mAppFilter != null && mAppReceiver != null) {
            Log.v(TAG, "liufeng, registe app manager filter");
            this.registerReceiver(mAppReceiver, mAppFilter);
        }
    }

    @Override
    public void onDestroy() {
        unregisteDeviceidReceiver();
        super.onDestroy();
    }
    private void unregisteDeviceidReceiver() {
        if (mDiServiceReceiver != null) {
            Log.v(TAG, "unregister device id receiver.");
            this.unregisterReceiver(mDiServiceReceiver);
            mDiServiceReceiver = null;
            mDiServiceFilter = null;
        }
        if (mAppReceiver != null) {
            Log.v(TAG, "unregister app manager receiver");
            mAppReceiver = null;
            mAppFilter = null;
        }
    }

    private final IDeviceidInterface.Stub mDIInterfaceBinder = new IDeviceidInterface.Stub() {
        @Override
        public String getOAID() {
            String sOaid = mCommon.getOAIDString();
            Log.i(TAG, "sOaid: preference:" + sOaid);
            if (sOaid != null && !"".equals(sOaid)) {
                return sOaid;
            } else {
                sOaid = "unKnown oaid";
                return sOaid;
            }
        }
        @Override
        public String getUDID() {
            String sUdid = mCommon.getUDIDString();
            Log.i(TAG, "sOaid: preference:" + sUdid);
            if (sUdid != null && !"".equals(sUdid)) {
                return sUdid;
            } else {
                sUdid = "unKnown UDID";
                return sUdid;
            }
        }

        @Override
        public boolean isSupport() {
            return true;
        }

        @Override
        public String getVAID(String strpackage) {
            String vaid = mCommon.getVAID(strpackage);
            Log.v(TAG, "calling AVID package: " + strpackage + " .vaid: " + vaid);
            return vaid;
        }

        @Override
        public String getAAID(String strpackage) {
            String aaid = mCommon.getAAID(strpackage);
            Log.v(TAG, "calling AAid package: " + strpackage + " .aaid: " + aaid);
            return aaid;
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "liufeng, ======Service onCreate=======");
        mCommon = CommonUtils.getInstance(this.getApplicationContext());

        // register BoradcastReceiver.
        createDeviceidReceiver();
        registeDeviceidReceiver();

        //Create UDID and OAID.
        if (!checkUDID()) {
            mCommon.updateUDID(createUDID());
        }
        mStarttime  = mCommon.getStartTime();
        if (!checkOAID()) {
            mCommon.updateOAID(createOAID());
        }
        if (CommonUtils.DBG_EANBLED) {
            cancelAlarm();
            startAlarm();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        if (DBG) {
            Log.d(TAG, "Deviceid Service onBind");
        }
        return mDIInterfaceBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand()");
        return Service.START_NOT_STICKY;
    }

    private void startAlarm() {
        long delaytime = mCommon.getOAIDResetDelayTime();
        Log.d(TAG, "delaytime:" + delaytime);
        mStarttime = System.currentTimeMillis();
        mCommon.setStartTime(mStarttime);
        AlarmManager am = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(ACTION_ALARM_RESET);
        mAlarmResetIntent = PendingIntent.getBroadcast(this, 0, intent, 0);
        am.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + delaytime, mAlarmResetIntent);
    }

    private void cancelAlarm() {
        Log.d(TAG, "cancelAlarm");
        AlarmManager am = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        if (mAlarmResetIntent != null) {
            am.cancel(mAlarmResetIntent);
            mAlarmResetIntent = null;
        }
    }

    private boolean ifNeedResetOaid() {
        long delaytime = mCommon.getOAIDResetDelayTime();
        long currenttime = System.currentTimeMillis();
        if ((currenttime - mStarttime > delaytime) && mStarttime != 0) {
            Log.d(TAG, "currenttime:" + currenttime + " , reCreateOAID");
            return true;
        } else {
            return false;
        }
    }

    private boolean checkUDID() {
        String sUdid = mCommon.getUDIDString();
        if (sUdid == null || "".equals(sUdid)) {
            return false;
        } else {
            return true;
        }
    }

    private String createUDID() {
        return mCommon.createUDIDString();
    }

    private boolean checkOAID() {
        String sOAID = mCommon.getOAIDString();
        if (sOAID == null || "".equals(sOAID)) {
            return false;
        }

        if (ifNeedResetOaid()) {
            return false;
        }
        return true;
    }

    private String createOAID() {
        return mCommon.createOAIDString();
    }

    private void reCreateOAID() {
        String str = mCommon.createOAIDString();
        mCommon.updateOAID(str);
        cancelAlarm();
        startAlarm();
        Log.d(TAG, "liufeng, reCreate OAID successful");
    }
}
