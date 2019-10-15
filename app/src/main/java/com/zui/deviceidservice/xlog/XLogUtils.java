package com.zui.deviceidservice.xlog;

import android.content.Context;
import android.content.pm.PackageInfo;

import com.zui.deviceidservice.CommonUtils;
import com.zui.xlog.sdk.ExAnalyticsTracker;

public class XLogUtils {
    private static final String TAG = "Deviceid-XLog";

    private static final String mAppKey = "D1K34ZWV867W";
    private static final String mAppChannel = "ZUI_NWACLR";

    public static final String CATEGORY_DEVICEID = "ZuiDeviceId";

    public static final String ACTION_DEVICEID_TYPE = "DeviceIdType";

    public static final String LABEL_INVOKE = "LabelInvoke";



    public static void initXlog(Context context) {
        CommonUtils.LogPrint(TAG, "initXlog");
        try {
            ExAnalyticsTracker.getInstance().initialize(context,
                    mAppKey,
                    getVersionName(context),
                    getVersionCode(context),
                    mAppChannel);
        } catch (Throwable e) {
            // ignore it
        }
    }

    private static String mVersionName = null;
    private static int mVersionCode = 0;

    private static String getVersionName(Context context) {
        if (mVersionName == null) {
            try {
                PackageInfo pi = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
                mVersionName = pi.versionName;
            } catch (Exception e) {
                e.printStackTrace();
                return "unknown_version";
            }
        }
        return mVersionName;
    }

    private static int getVersionCode(Context context) {
        if (mVersionCode == 0) {
            try {
                PackageInfo pi = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
                mVersionCode = pi.versionCode;
            } catch (Exception e) {
                e.printStackTrace();
                return 0;
            }
        }
        return mVersionCode;
    }

    public static void destroyXlog(Context context) {
        if (context != null) {

            try {
                ExAnalyticsTracker.getInstance().destroy(context);
            } catch (Throwable e) {
                // ignore it
            }
        }
    }

    public static void trackXlog(XLogInfo info) {
        if (null == info) return;
        CommonUtils.LogPrint(TAG, "catetory: " + info.getCategory() +
                "\naction: " + info.getAction() +
                "\nlabel: " + info.getLabel() +
                "\nvalue: " + info.getValue());
        for (int i = 0; i < 5; i++) {
            CommonUtils.LogPrint(TAG, "param" + i + " :" +
                    info.getParams().getKey(i) + ", " + info.getParams().getValue(i));
        }

        ExAnalyticsTracker.getInstance().trackEvent(info.getCategory(), info.getAction(),
                info.getLabel(), info.getValue(), info.getParams());
    }
}
