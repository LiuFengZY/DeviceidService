package com.zui.deviceidservice;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import com.zui.deviceidservice.db.AppInfo;
import com.zui.deviceidservice.db.DBController;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.HashMap;
import java.util.Map;
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
    private static final String ITEM_OAID_FOR_ZUI = "item_oaid_for_zui";
    private static final String ITEM_START_TIME = "item_starttime";
    private int mSpMode = 0;
    private static String sIMEI = "";
    public static final String ZUI_SIGNATURE_MD5 = "62b075c56bf65c5c1fc47defd8550dbd";
    public static final String ZUI_SIGNATURE_MD5_2 = "8ddb342f2da5408402d7568af21e29f9";

    private static final String PRI_KEY = "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQC+pUXQDG2Pvuuuisz1TlVouBMQAvUzcM1f3ykwLNQjBLdZ4IgkUlT3Np/bowrykE4lz12ehTeOA6WPA2bzoJJaKX1T8804WZfi6h9XWzFm3IrKwi2yExiM9BVkv3G0ENMNxfK5er6duVO0wcdqeCLN1vPXweIZJoIFrZ99cZUwYyMcGmkc76yIxLaXHV92BU4ZqSB1C3FQ/GKVESuyf4pkdcaKqOe0xyeTGcruFHqU11x3ObKIn5kwmBn3+faaqiV3IGI2N0W7j+u+yLF4PF35GFidOjKhs91ar8H381QqqLb8s+bxPlt+Sq6HL4YnE8dXnF06aHNrOHhVBKZhZ1DnAgMBAAECggEAcMQ3yDkhsU4gAC0v0MXV9Uw0Uls9iIDnqhbJOSq7DD+k6G/Md5ePlG7mHbeSKSo8X1cKLFLmKoVzr2U2x1KqXtR9wmPKdtNf6SPNCBHz5cD8A7TZ7KQo+G8EHIS1D+qHvHTi/t1g1NCjd57LgumuxIypzWxsfa3uaMdrOHsv4N8tpSqWRix8j4HN9aP5y4xIukfaPbYUMCG4K4KjZrHZu0splmq3AeshmHZCnDfPhvJGgeVguk3bkZVIrFtX4K05xy8HGYNlC9GJ6osUnO11eeWDW939ScZt7A1KJOpJ0Y7bhiCHcEP1ghqQOvpNnnGd0HLcyB7tnf3+FwTnAAWi6QKBgQDhZZiga4j+MJSMgYWA5w9bVA1kYKDPlsCBTW1BcTUNabMkmD6J64V6Bxmx5DtebSyw6wL+iePik08Viq9vq/63ZURSjSVNo2H5UCnVjmu8Z+b0dzDR54Nwp9ZgubJD1/w9pG+i9JJKBxNZ+C/EidL4SlNzc9c10Ri+q9JxilvJqwKBgQDYh8hqk2oVc34qoSkPSq665+RVn4nm4B7+GQSfgoyLTWwERm2h6hO2HxVSfuz++VQVXFzUct2Q1KIpXWic5Y/r9rZYLks9srafeuP+gtcHEsLtGP+M2yhc/cucBDkOFCFgKFrktRVSlhbARxsDZAdi34CMqt2tBFLg41LbHrgxtQKBgHxsqy6TblJz2u0daudXpiCSa7onpV4zKB248kEYD2NSIDRpXsygGVTdqo+LIELmHa+kbEi7MfOXwiZwIpyQ49G1s1um0xriwGjymcVsE4k0CkiVq3uUQ/jijfNjT0coafRVW9MnE8KN2V7nJOdn9fBeh2bKYdkxjmljTI6lBDp1AoGBAL0e85yqftiXlFX1hyBVEYIsMlHa0560mD1FarVLWCf/il29idoG0gqa4Yu5UpRs/tTdZDMm1IDAR5argEixdOAbDy672HneEwX+Vw6gBuGlsF1YHTRQ4tM91M3DHnY+fNw4wxLJWwNUFjEAqgZvIshoACZcwttwUFceFetOzICVAoGAVCrTugqBQ3cXChtvmWoYKbE9bb8aWwAhekYiMCtJsMeqJLS+3rX+hspJ3O096/vxFFB8UNmG7tdcoUnPHDNWvc/Wj39j80Zta1muA/+4bpnN8AsvTJBDXcmgv7i1eG5McFNIpKkLasEqXxk4okrg0q7nvWseaaZPS7nrrB0m92I=";
    private static final String SIGN_TYPE = "RSA2";
    private static final String MERCHANT_ID = "20191010181920";

    private static final long defaultDelayTime = (10*60*1000L);
    public static final boolean DBG_EANBLED = false;
    public static final boolean DBG_LOG = true;

    private TelephonyManager mMSimTelephonyManager = null;
    private DBController dbc = null;

    private static final String GET_USERID_URL = "https://adapi.lenovomm.com/gwouter/appbiz/lestoreDeveloperInfo/api/get?pkgname=";
    private static final String UPLOAD_OAID_IMEI_MAPPING_URL = "http://10.119.126.58:8902/zui/v1/imeimapping/";
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

    private Signature[] getRawSignature(Context paramContext, String paramString) {
        if ((paramString == null) || (paramString.length() == 0)) {

            return null;
        }
        PackageManager localPackageManager = paramContext.getPackageManager();
        PackageInfo localPackageInfo;
        try {
            localPackageInfo = localPackageManager.getPackageInfo(paramString, PackageManager.GET_SIGNATURES);
            if (localPackageInfo == null) {

                return null;
            }
        } catch (PackageManager.NameNotFoundException localNameNotFoundException) {

            return null;
        }
        return localPackageInfo.signatures;
    }

    private String getSign(String packageName) {
        Signature[] arrayOfSignature = getRawSignature(context, packageName);
        if ((arrayOfSignature == null) || (arrayOfSignature.length == 0)){
            return null;
        }

        return getMD5MessageDigest(arrayOfSignature[0].toByteArray());
    }

    public boolean isZuiApp(String strpackage) {
        String strSignature = getSign(strpackage);
        Log.d(TAG, "liufeng, package:" + strpackage + " .signature:" + strSignature);
        if (ZUI_SIGNATURE_MD5.equals(strSignature)
                || ZUI_SIGNATURE_MD5_2.equals(strSignature)) {
            return true;
        } else {
            return false;
        }
    }

    public static String rsaSign(String strdata) throws Exception {
        Log.d(TAG, "liufeng, strData:" + strdata);
        PrivateKey priKey  = loadPrivateKey(PRI_KEY);
        java.security.Signature signature = java.security.Signature.getInstance("SHA256WithRSA");

        signature.initSign(priKey);
        signature.update(strdata.getBytes());
        byte[] signed = signature.sign();

        return new String(Base64Utils.encode(signed));

    }

    /**
     * load pri key.
     * @param privateKeyStr
     * @throws Exception
     */
    public static PrivateKey loadPrivateKey(String privateKeyStr) throws Exception {
        try {
            byte[] buffer = Base64Utils.decode(privateKeyStr);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(buffer);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return (RSAPrivateKey) keyFactory.generatePrivate(keySpec);
        } catch (NoSuchAlgorithmException e) {
            throw new Exception("No this algorithm");
        } catch (InvalidKeySpecException e) {
            throw new Exception("invalid pri key.");
        } catch (NullPointerException e) {
            throw new Exception("null private key.");
        }
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

    public static final String getMD5MessageDigest(byte[] paramArrayOfByte)
    {
        char[] arrayOfChar1 = { 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 97, 98, 99, 100, 101, 102 };
        try
        {
            MessageDigest localMessageDigest = MessageDigest.getInstance("MD5");
            localMessageDigest.update(paramArrayOfByte);
            byte[] arrayOfByte = localMessageDigest.digest();
            int i = arrayOfByte.length;
            char[] arrayOfChar2 = new char[i * 2];
            int j = 0;
            int k = 0;
            while (true)
            {
                if (j >= i)
                    return new String(arrayOfChar2);
                int m = arrayOfByte[j];
                int n = k + 1;
                arrayOfChar2[k] = arrayOfChar1[(0xF & m >>> 4)];
                k = n + 1;
                arrayOfChar2[n] = arrayOfChar1[(m & 0xF)];
                j++;
            }
        }
        catch (Exception localException)
        {
        }
        return null;
    }

    private String getImei1String() {
        String imei1String = "";
//        final Phone phone = PhoneFactory.getPhone(0) {
//            if (phone.getPhoneType() == TelephonyManager.PHONE_TYPE_CDMA) {
//                imei1String = phone.getImei();
//            } else {
//                imei1String = phoen.getDeviceId();
//            }
//        }
        return imei1String;
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

    public void updateOAIDForZui(String oaid) {
        SharedPreferences sp = context.getSharedPreferences(PREFERENCE_DEVICE_ID, mSpMode);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(ITEM_OAID_FOR_ZUI, oaid);
        editor.commit();
    }

    public String getOAIDStringForZui() {
        SharedPreferences sp = context.getSharedPreferences(PREFERENCE_DEVICE_ID, mSpMode);
        String oaid = sp.getString(ITEM_OAID_FOR_ZUI, "");
        return oaid;
    }

    private long getRandom() {
        final long seed = System.currentTimeMillis();
        Random r = new Random(seed);
        long rr = r.nextLong();
        return rr;
    }

    public String createOAIDString() {
        if (sIMEI == null || "".equals(sIMEI)) {
            sIMEI = getImeiString();
        }

        String str = sIMEI + "zui" + Long.toHexString(getRandom()) + System.currentTimeMillis();
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

                insertVAIDAndAAID(response, strpackage);
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "liufeng, get userid error.http. onError response:" + error);
                Log.e(TAG, "NO userid or developerId, so only create aaid.");
                insertOnlyAAID(strpackage);

            }
        };
        // data should be one json data.
        //SimpleAsyncHttpClient.doHttpRequest(SimpleAsyncHttpClient.HTTP_REQUEST_METHOD.HTTP_GET,
         //       GET_USERID_URL, httpcallback, "package:" + strpackage);
        SimpleAsyncHttpClient.doHttpRequest(SimpleAsyncHttpClient.HTTP_REQUEST_METHOD.HTTP_GET,
                GET_USERID_URL + strpackage, httpcallback, null);
    }


    public void uploadImeiAndOaidToServer(final DeviceidService.UploadImeiStatus callback) {

        String strImei1 = getImei1String();
        String strOaid = getOAIDString();
        //imeiId=strImei1&merchantId=MERCHANT_ID&oaId=strOaid&signType=RSA2
        String rsaSignText = "imeiId=" + strImei1 + "&merchantId=" + MERCHANT_ID + "&oaId=" + strOaid + "&signType=RSA2";
        String rsaSign = "";
        Log.d(TAG, "liufeng, rsa,rsaSignText:" + rsaSignText);
        try {
            rsaSign = rsaSign(rsaSignText);
        } catch (Exception e) {
            e.printStackTrace();
        }
        String uploadData = "";
        try {
            JSONObject jsobj = new JSONObject();
            jsobj.put("imeiId", strImei1);
            jsobj.put("merchantId", "20191010181920");
            jsobj.put("oaId", strOaid);
            jsobj.put("sign", rsaSign);
            jsobj.put("signType", "RSA2");
            Log.d(TAG, "liufeng, json,data:" + jsobj.toString());
            uploadData = jsobj.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        SimpleAsyncHttpClient.HttpCallback<String> httpcallback = new SimpleAsyncHttpClient.HttpCallback<String>() {
            @Override
            public void onSuccess(String response) {
                Log.d(TAG, "liufeng, uploadImeiAndOaidToServer. onSuccess response:" + response);
                callback.uploadSuccuss(0);
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "liufeng, get userid error.http. onError response:" + error);
                Log.e(TAG, "NuploadImeiAndOaidToServer error.");
                callback.uploadFailed(-1);
            }
        };
        SimpleAsyncHttpClient.doHttpRequest(SimpleAsyncHttpClient.HTTP_REQUEST_METHOD.HTTP_POST,
                UPLOAD_OAID_IMEI_MAPPING_URL, httpcallback, uploadData);
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

    private void insertVAIDAndAAID(String userid, String strpackage) {
        // response is userid
        AppInfo appinfo = new AppInfo();
        String strVaid = getVAID(strpackage);
        if (strVaid != null && !"".equals(strVaid)) {
            appinfo.vaid = strVaid;
        } else {
            appinfo.vaid = md5(userid +  Long.toHexString(getRandom()) + System.currentTimeMillis());
        }
        String str = strpackage  + Long.toHexString(getRandom()) + System.currentTimeMillis();
        appinfo.aaid = md5(str);
        appinfo.developer = userid;
        appinfo.packagename = strpackage;
        dbc.insert(appinfo);
        Log.d(TAG, "do insert aaid: " + appinfo.aaid + " .package:" + strpackage);
        Log.d(TAG, "do insert vaid: " + appinfo.vaid + " .userid:" + userid);
    }

    private void insertOnlyAAID(String strpackage) {
        String str = strpackage + Long.toHexString(getRandom()) + System.currentTimeMillis();
        AppInfo appinfo = new AppInfo();
        appinfo.aaid = md5(str);
        appinfo.vaid = md5(userid1 + Long.toHexString(getRandom()) + System.currentTimeMillis());
        appinfo.developer = userid1;
        appinfo.packagename = strpackage;
        dbc.insert(appinfo);
        Log.d(TAG, "only aaid insert aaid:" + appinfo.aaid + " .packages:" + strpackage);

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

    public boolean createAAIDForPackageName(String strpackage) {
        insertOnlyAAID(strpackage);
        Log.d(TAG, "only aaid insert successful for package name." + " .packages:" + strpackage);
        return true;
    }

    public static void LogPrint(String tag, String msg) {
        if (DBG_LOG) Log.d(tag, msg);
    }


}
