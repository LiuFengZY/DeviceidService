package com.zui.deviceidservice;

import android.content.Context;
import android.content.SharedPreferences;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.telephony.TelephonyManager;
import com.zui.deviceidservice.db.AppInfo;
import com.zui.deviceidservice.db.DBController;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

//These are for system app System/app/DeviceidService.
//import com.android.internal.telephony.Phone;
//import com.android.internal.telephony.PhoneFactory;

public class CommonUtils {
    private static final String TAG = "Deviceid-CommonUtils";
    private Context context;
    private static CommonUtils sInstance = null;

    private static final String PREFERENCE_DEVICE_ID = "device_id";
    private static final String ITEM_UDID = "item_udid";
    private static final String ITEM_OAID = "item_oaid";
    private static final String ITEM_START_TIME = "item_starttime";
    private int mSpMode = 0;
    private static String sIMEI = "";

    private static final long defaultDelayTime = (10*60*1000L);
    public static final boolean DBG_EANBLED = false;

    private TelephonyManager mMSimTelephonyManager = null;
    private DBController dbc = null;

    private static final String GET_USERID_URL = "https://adapi.lenovomm.com/gwouter/appbiz/lestoreDeveloperInfo/api/get?pkgname=";

    public static final String userid1 = "liufeng23@lenovo.com";

    private CommonUtils(Context c) {
        context = c;
        mMSimTelephonyManager = (TelephonyManager) context
                .getSystemService(Context.TELEPHONY_SERVICE);
        dbc = new DBController(context);
    }

    public static CommonUtils getInstance(Context c) {
        if (sInstance == null) {
            sInstance = new CommonUtils(c);
        }
        return sInstance;
    }

    public static String md5(String string) {
        if (TextUtils.isEmpty(string)) {
            return "";
        }
        MessageDigest md5 = null;
        try {
            md5 = MessageDigest.getInstance("MD5");
            byte[] bytes = md5.digest(string.getBytes());
            String result = "";
            for (byte b : bytes) {
                String temp = Integer.toHexString(b & 0xff);
                if (temp.length() == 1) {
                    temp = "0" + temp;
                }
                result += temp;
            }
            return result;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

    private String getImeiString() {
        String imeiString = "";
        //These are for system app. system/app/DeviceidService.
//        int slotCount = mMSimTelephonyManager.getSimCount();
//        for (int slotId = 0; slotId < slotCount; slotId ++) {
//            final Phone phone = PhoneFactory.getPhone(slotId);
//            if (phone != null) {
//                if(phone.getPhoneType() == TelephonyManager.PHONE_TYPE_CDMA){
//                    imeiString = imeiString + phone.getImei();
//                }
//                else{
//                    imeiString = imeiString + phone.getDeviceId();
//                }
//            }
//        }
        return imeiString;
    }

    public void updateUDID(String udid) {
        SharedPreferences sp = context.getSharedPreferences(PREFERENCE_DEVICE_ID, mSpMode);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(ITEM_UDID, udid);
        editor.commit();
    }

    public String getUDIDString() {
        SharedPreferences sp = context.getSharedPreferences(PREFERENCE_DEVICE_ID, mSpMode);
        String udid = sp.getString(ITEM_UDID, "");
        return udid;
    }

    public String createUDIDString() {
        sIMEI = getImeiString();
        Log.e(TAG, "sIMEI: " + sIMEI);
        return md5(sIMEI);
    }

    public void updateOAID(String oaid) {
        SharedPreferences sp = context.getSharedPreferences(PREFERENCE_DEVICE_ID, mSpMode);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(ITEM_OAID, oaid);
        editor.commit();
    }

    public String getOAIDString() {
        SharedPreferences sp = context.getSharedPreferences(PREFERENCE_DEVICE_ID, mSpMode);
        String oaid = sp.getString(ITEM_OAID, "");
        return oaid;
    }

    public String createOAIDString() {
        final long seed = System.currentTimeMillis();
        Random r = new Random(seed);
        final int rr = r.nextInt(999999);
        if (sIMEI == null || "".equals(sIMEI)) {
            sIMEI = getImeiString();
        }

        String str = sIMEI + "zui" + Integer.toHexString(rr);
        Log.i(TAG, "new , createOAIDString:" + str);
        return md5(str);
    }

    public long getOAIDResetDelayTime() {
        long delaytime = Settings.Global.getLong(context.getContentResolver(), "oadi_delay_time" ,defaultDelayTime);
        return delaytime;
    }

    public long getStartTime() {
        SharedPreferences sp = context.getSharedPreferences(PREFERENCE_DEVICE_ID, mSpMode);
        long starttime = sp.getLong(ITEM_START_TIME, 0);
        return starttime;
    }

    public void setStartTime(long starttime) {
        SharedPreferences sp = context.getSharedPreferences(PREFERENCE_DEVICE_ID, mSpMode);
        SharedPreferences.Editor editor = sp.edit();
        editor.putLong(ITEM_START_TIME, starttime);
        editor.commit();
    }

    //http interface get userid.
    private void insertAAIDThread(final String strpackage) {
        String userid = "";
        SimpleAsyncHttpClient.HttpCallback<String> httpcallback = new SimpleAsyncHttpClient.HttpCallback<String>() {
            @Override
            public void onSuccess(String response) {
                Log.d(TAG, "liufeng, http. onSuccess response:" + response);

                insertVAIDAndVVID(response, strpackage);
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "liufeng, get userid error.http. onError response:" + error);
            }
        };
        // data should be one json data.
        //SimpleAsyncHttpClient.doHttpRequest(SimpleAsyncHttpClient.HTTP_REQUEST_METHOD.HTTP_GET,
         //       GET_USERID_URL, httpcallback, "package:" + strpackage);
        SimpleAsyncHttpClient.doHttpRequest(SimpleAsyncHttpClient.HTTP_REQUEST_METHOD.HTTP_GET,
                GET_USERID_URL + strpackage, httpcallback, null);
    }

    public String getVAID(String strpackage) {
        String strVaid = dbc.getVAIDByPackageName(strpackage);
        Log.d(TAG, "getVAID, strpackage:" + strpackage + " .strVaid: " + strVaid);
        return strVaid;
    }

    public void insertAAID(String strpackage) {
        if (dbc.isPackageNameExisted(strpackage)) {
            Log.d(TAG, " aaid already isExisted! return.");
            return;
        }
        // this is async method, so wait the call back.
        insertAAIDThread(strpackage);
    }

    private void insertVAIDAndVVID(String userid, String strpackage) {
        // response is userid
        String str = strpackage + getUDIDString();
        AppInfo appinfo = new AppInfo();
        appinfo.aaid = md5(str);
        appinfo.vaid = md5(userid + getUDIDString());
        appinfo.developer = userid;
        appinfo.packagename = strpackage;
        dbc.insert(appinfo);
        Log.d(TAG, "do insert aaid: " + appinfo.aaid + " .package:" + strpackage);
        Log.d(TAG, "do insert vaid: " + appinfo.vaid + " .userid:" + userid);
    }

    public String getAAID(String strpackage) {
        String strAaid = dbc.getAAIDByPackageName(strpackage);
        Log.d(TAG, "getVAID, strpackage:" + strpackage + " .strAaid: " + strAaid);
        return strAaid;
    }

    public void deleteAAID(String strpackage) {
        AppInfo appinfo = new AppInfo();
        appinfo.packagename = strpackage;
        dbc.delete(appinfo);
    }

}
