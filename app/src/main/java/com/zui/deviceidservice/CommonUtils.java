package com.zui.deviceidservice;

import android.content.Context;
import android.content.SharedPreferences;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.telephony.TelephonyManager;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;


//import com.android.internal.telephony.Phone;
//import com.android.internal.telephony.PhoneFactory;

public class CommonUtils {
    private static final String TAG = "Deviceid_CommonUtils";
    private Context context;
    private static CommonUtils sInstance;

    private static final String PREFERENCE_DEVICE_ID = "device_id";
    private static final String ITEM_UDID = "item_udid";
    private static final String ITEM_OAID = "item_oaid";
    private static final String ITEM_START_TIME = "item_starttime";
    private int mSpMode = 0;
    private static String sIMEI = "";

    private static final long defaultDelayTime = (30*1000L);

    private TelephonyManager mMSimTelephonyManager = null;

    public static final String userid1 = "liufeng23@lenovo.com";

    private CommonUtils(Context c) {
        context = c;
        mMSimTelephonyManager = (TelephonyManager) context
                .getSystemService(Context.TELEPHONY_SERVICE);
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

    private String getUserIdForPackage(String strpackage) {
        String userid = "";

        return userid;
    }

    public String getVAID(String strpackage) {
        Log.d(TAG, "getVAID, strpackage:" + strpackage);
        //find userid.

        return md5(userid1 + sIMEI);
    }

    public String getAAID(String strpackage) {
        Log.d(TAG, "getAAID, strpackage: " + strpackage);
        return md5(strpackage + sIMEI);
    }

}
