package com.zui.deviceidservice;

import android.content.Context;
import android.content.SharedPreferences;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

public class CommonUtils {
    private static final String TAG = "Deviceid CommonUtils";
    private Context context;
    private static CommonUtils sInstance;

    private static final String PREFERENCE_DEVICE_ID = "device_id";
    private static final String ITEM_UDID = "item_udid";
    private static final String ITEM_OAID = "item_oaid";
    private static final String ITEM_START_TIME = "item_starttime";
    private int mSpMode = 0;
    private static final String sIMEI = "860574040054101";
    private static final String sNumber = "HKL4NMZW";
    private static final String sMac = "40:a1:08:1c:0a:90";

    private static final long defaultDelayTime = (60*1000L);

    private final static long seed = 1234567890;
    private CommonUtils(Context c) {
        context = c;
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
        String str = sIMEI + "zui" + sNumber + "zui" + sMac;
        return md5(str);
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
        Random r = new Random(seed);
        final int rr = r.nextInt(100000);
        String str = sIMEI + "zui" + sNumber + "zui" + sMac + "zui" + Integer.toHexString(rr);
        Log.i(TAG, "createOAIDString:" + str);
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
}
