package com.zui.deviceidservice;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.Log;

import com.zui.deviceidservice.xlog.XLogInfo;
import com.zui.deviceidservice.xlog.XLogUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DeviceidService extends Service {
    private static boolean DBG = true;
    public static String TAG = "ZuiDeviceid-MainService";
    private CommonUtils mCommon = null;
    private long mStarttime = 0;

    private boolean mOaidEnabled = true;
    private static final String ZUI_OAID_ENABLED = "zui_deviceid_oaid_enabled";
    private static final String GET_UID_PERMISSION = "com.zui.permission.GET_UDID";

    private BroadcastReceiver mDiServiceReceiver = null;
    private IntentFilter mDiServiceFilter = null;
    private static final String ACTION_ALARM_RESET = "zui.intent.action.ACTION_ALARM_RESET";

    private AppManageReceiver mAppReceiver = null;
    private IntentFilter mAppFilter = null;

    private PendingIntent mAlarmResetIntent = null;

    private OaidEnableObserver mOaidObserver = null;

    Map<String, ArrayList<String>> mXlogMap = new HashMap<String,ArrayList<String>>();

    private static boolean isNeedImeiToServerLater = false;

    private final static int REUPLOAD_IMEI_MESSAGE = 101;
    private final static int MESSAGE_DELAY_TIME = 10000;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case REUPLOAD_IMEI_MESSAGE:
                Log.d(TAG, "upload imei mapping again.");
                if (isNetworkNormal()) {
                    mCommon.uploadImeiAndOaidToServer(uploadimeicallback);
                } else {
                    isNeedImeiToServerLater = true;
                }
                break;
            default:
                break;
            }
        }
    };

    public interface UploadImeiStatus {
        void uploadSuccuss(int status);
        //void serviceDisconnected(T service);
        void uploadFailed(int status);
    }

    DeviceidService.UploadImeiStatus uploadimeicallback = new DeviceidService.UploadImeiStatus() {
        @Override
        public void uploadSuccuss(int status) {
            Log.d(TAG, "upload IMEI to server success.");
            isNeedImeiToServerLater = false;
        }

        @Override
        public void uploadFailed(int status) {
            Log.d(TAG, "upload IMEI to server failed:" + status);
            //need to restart. send delay message.
            if (!mHandler.hasMessages(REUPLOAD_IMEI_MESSAGE)) {
                mHandler.sendMessageDelayed(mHandler.obtainMessage(REUPLOAD_IMEI_MESSAGE), MESSAGE_DELAY_TIME);
            }

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

        //init xlog.
        XLogUtils.initXlog(this.getApplicationContext());

        //Create UDID and OAID.
        if (!checkUDID()) {
            mCommon.updateUDID(createUDID());
        }
        mStarttime  = mCommon.getStartTime();
        if (!checkOAID()) {
            Log.d(TAG, "No OAID string, createOAID.");
            mCommon.updateOAID(createOAID());
            mCommon.updateOAIDForZui(mCommon.getOAIDString());
            //上传IMEI和OAID到服务器。
            if (isNetworkNormal()) {
                mCommon.uploadImeiAndOaidToServer(uploadimeicallback);
            } else {
                isNeedImeiToServerLater = true;
            }
        }
        if (CommonUtils.DBG_EANBLED) {
            cancelAlarm();
            startAlarm();
        }

        mOaidObserver = new OaidEnableObserver(this.getApplicationContext(), mHandler);
        getContentResolver().registerContentObserver(Settings.Global.getUriFor(ZUI_OAID_ENABLED), true, mOaidObserver);
    }

    private boolean isNetworkNormal() {
        ConnectivityManager cm = (ConnectivityManager)ConnectivityManager.from(this.getApplicationContext());
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return (activeNetwork != null) &&
                    (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI || activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE);
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

    private boolean isNeedUploadImei() {
        return isNeedImeiToServerLater;
    }

    private void createDeviceidReceiver() {
        if (mDiServiceReceiver != null) return;

        mAppFilter = new IntentFilter();
        mAppFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
        mAppFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        mAppFilter.addDataScheme("package");

        mDiServiceFilter = new IntentFilter();
        mDiServiceFilter.addAction(ACTION_ALARM_RESET);
        mDiServiceFilter.addAction(Intent.ACTION_TIME_CHANGED);
        mDiServiceFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        mAppReceiver = new AppManageReceiver();

        mDiServiceReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                Log.d(TAG, "DiService, action:" + action);
                if (action != null && action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
                    NetworkInfo ni = (NetworkInfo) intent.getParcelableExtra(
                            ConnectivityManager.EXTRA_NETWORK_INFO);
                    if (ni != null && ni.getState() == NetworkInfo.State.CONNECTED
                            && isNeedUploadImei()) {
                        if (DBG) {
                            Log.d(TAG, "network state receiver wifi or data got CONNECTIVITY_ACTION ni: " + ni);
                        }
                        mCommon.uploadImeiAndOaidToServer(uploadimeicallback);
                    } else {
                        Log.d(TAG, "Connectivity Changed, But NO need upload imei to server.");
                    }
                } else if (action != null && action.equals(ACTION_ALARM_RESET)) {
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
        if (mDiServiceReceiver != null && mDiServiceFilter != null) {
            if (DBG) Log.v(TAG, "liufeng, registe device id sreceiver");
            this.registerReceiver(mDiServiceReceiver,mDiServiceFilter);
        }
        if (mAppFilter != null && mAppReceiver != null) {
            if (DBG) Log.v(TAG, "liufeng, registe app manager filter");
            this.registerReceiver(mAppReceiver, mAppFilter);
        }
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

    @Override
    public void onDestroy() {
        unregisteDeviceidReceiver();
        //destroy xlog.
        XLogUtils.destroyXlog(this.getApplicationContext());
        super.onDestroy();
    }

    private String getAppPkg(int pid) {
        String processName = "";
        ActivityManager activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        if (activityManager != null) {
            List<ActivityManager.RunningAppProcessInfo> list = activityManager.getRunningAppProcesses();
            for (ActivityManager.RunningAppProcessInfo info : list) {
                if (info.pid == pid) {
                    processName = info.processName;
                    break;
                }
            }
        }
        return processName;
    }

    private void saveToMap(String strpackage, String method) {
        Set<String> keySet = mXlogMap.keySet();
        Iterator<String> it =keySet.iterator();
        while(it.hasNext()) {
            String key = it.next();
            if (strpackage.equals(key)) {
                ArrayList<String> tmp = mXlogMap.get(key);
                if (!tmp.contains(method)) {
                    tmp.add(method);
                    mXlogMap.put(key,tmp);
                    Log.i(TAG, "mXlogMap:" + mXlogMap.toString());
                    return;
                }
                Log.i(TAG, "mXlogMap:" + mXlogMap.toString());
                return;
            }
        }
        ArrayList<String> newList = new ArrayList<String>();
        newList.add(method);
        mXlogMap.put(strpackage, newList);
        Log.i(TAG, "mXlogMap:" + mXlogMap.toString());
    }

    private boolean ifNeedToTrackLog(String strpackage, String method) {
        Set<String> keySet = mXlogMap.keySet();
        Iterator<String> it =keySet.iterator();
        while(it.hasNext()){
            String key = it.next();
            if (strpackage.equals(key)) {
                ArrayList<String> value = mXlogMap.get(key);
                for(String tmp:value){
                    if (method.equals(tmp)) {
                        Log.i(TAG, "this package and method has exist, no need to track xlog.");
                        Log.i(TAG, "strpackage: " + key + " .method:" + tmp);
                        return false;
                    }
                }
            }
        }
        Log.i(TAG, "No package or no package method in the map. need track xlog.");
        return true;
    }

    private final IDeviceidInterface.Stub mDIInterfaceBinder = new IDeviceidInterface.Stub() {
        @Override
        public String getOAID() {
            String invokerPkg = getAppPkg(Binder.getCallingPid());
            String sOaid = mCommon.getOAIDString();
            Log.i(TAG, "oaid: preference: " + sOaid + " .calling pkg: " + invokerPkg);
            if (ifNeedToTrackLog(invokerPkg, "getOAID")) {
                XLogUtils.trackXlog(new XLogInfo.Builder()
                        .category(XLogUtils.CATEGORY_DEVICEID)
                        .action(XLogUtils.ACTION_DEVICEID_TYPE)
                        .label(XLogUtils.LABEL_INVOKE)
                        .param(0, "getOAID", invokerPkg)
                        .value(0).build());
                saveToMap(invokerPkg, "getOAID");
            }
            if (!mOaidEnabled && mCommon.isZuiApp(invokerPkg)) {
                Log.d(TAG, "liufeng, oaid close, but This is zui app.");
                return mCommon.getOAIDStringForZui();
            } else {
                Log.d(TAG, "liufeng, oaid is enable.");
                return sOaid;
            }
        }

        @Override
        public String getUDID() {
            String invokerPkg = getAppPkg(Binder.getCallingPid());
            Log.i(TAG, "getUDID: " + invokerPkg);
            if (ifNeedToTrackLog(invokerPkg, "getUDID")) {
                XLogUtils.trackXlog(new XLogInfo.Builder()
                        .category(XLogUtils.CATEGORY_DEVICEID)
                        .action(XLogUtils.ACTION_DEVICEID_TYPE)
                        .label(XLogUtils.LABEL_INVOKE)
                        .param(0, "getUDID", invokerPkg)
                        .value(0).build());
                saveToMap(invokerPkg, "getUDID");
            }
            try {
                checkPermissions();
            } catch (SecurityException e) {
                Log.d(TAG, "NO GET UDID permission.");
                return "00000000000000000000000000000000";
            }
            Log.d(TAG, "This app is zroaming,Grant GET UDID permission.");
            String sUdid = mCommon.getUDIDString();
            Log.i(TAG, "udid: preference:" + sUdid);
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
            String invokerPkg = getAppPkg(Binder.getCallingPid());
            Log.i(TAG, "getVAID: " + invokerPkg);
            if (ifNeedToTrackLog(invokerPkg, "getVAID")) {
                XLogUtils.trackXlog(new XLogInfo.Builder()
                        .category(XLogUtils.CATEGORY_DEVICEID)
                        .action(XLogUtils.ACTION_DEVICEID_TYPE)
                        .label(XLogUtils.LABEL_INVOKE)
                        .param(0, "getVAID", invokerPkg)
                        .value(0).build());
                saveToMap(invokerPkg, "getVAID");
            }
            String vaid = mCommon.getVAID(strpackage);
            Log.v(TAG, "calling AVID package: " + strpackage + " .vaid: " + vaid);
            return vaid;
        }

        @Override
        public String getAAID(String strpackage) {
            String invokerPkg = getAppPkg(Binder.getCallingPid());
            Log.i(TAG, "getAAID: " + invokerPkg);
            if (ifNeedToTrackLog(invokerPkg, "getAAID")) {
                XLogUtils.trackXlog(new XLogInfo.Builder()
                        .category(XLogUtils.CATEGORY_DEVICEID)
                        .action(XLogUtils.ACTION_DEVICEID_TYPE)
                        .label(XLogUtils.LABEL_INVOKE)
                        .param(0, "getAAID", invokerPkg)
                        .value(0).build());
                saveToMap(invokerPkg, "getAAID");
            }
            String aaid = mCommon.getAAID(strpackage);
            Log.v(TAG, "calling AAid package: " + strpackage + " .aaid: " + aaid);
            return aaid;
        }

        @Override
        public boolean createAAIDForPackageName(String strpackage) {
            return mCommon.createAAIDForPackageName(strpackage);
        }
    };

    private void checkPermissions() {
        this.enforceCallingPermission(GET_UID_PERMISSION, "DeviceidService");
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

    private void disableOAID() {
        mCommon.updateOAID("00000000000000000000000000000000");
    }

    private void enableOAID() {
        String str = mCommon.createOAIDString();
        mCommon.updateOAID(str);
        mCommon.updateOAIDForZui(str);
        //上传服务器IMIE和OAID的映射。
        if (isNetworkNormal()) {
            mCommon.uploadImeiAndOaidToServer(uploadimeicallback);
        } else {
            isNeedImeiToServerLater = true;
        }
    }

    private class OaidEnableObserver extends ContentObserver {
        ContentResolver mResolver;
        Uri oaidUri = Settings.Global.getUriFor(ZUI_OAID_ENABLED);

        public OaidEnableObserver(Context context, Handler handler) {
            super(handler);
            mResolver = context.getContentResolver();
            mOaidEnabled = Settings.Global.getInt(mResolver, ZUI_OAID_ENABLED, 1) != 0;
            Log.d(TAG, "oaid status :" + mOaidEnabled);
            if (!mOaidEnabled) {
                disableOAID();
            }
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            if (oaidUri.equals(uri)) {
                mOaidEnabled = Settings.Global.getInt(mResolver, ZUI_OAID_ENABLED, 1) != 0;
                Log.d(TAG, "oaid status onchange: " + mOaidEnabled);
                if (mOaidEnabled) {
                    enableOAID();
                } else {
                    disableOAID();
                }
            }
        }
    }
}
