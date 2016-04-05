package com.kindabear.radiople.util;

import android.content.Context;
import android.os.Build;
import android.telephony.TelephonyManager;

import com.kindabear.radiople.BuildConfig;

public class Constants {

    private final static String TAG = "Constants";

    private final static String UNKNOWN = "unknown";

    public static String getAppVersion() {
        return BuildConfig.VERSION_NAME;
    }

    public static String getOsVersion() {
        return Build.VERSION.RELEASE;
    }

    public static String getProvider(Context context) {
        TelephonyManager manager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        String provider = manager.getNetworkOperatorName();
        return provider != null && !provider.equals("") ? provider : UNKNOWN;
    }

    public static String getModel() {
        return Build.MODEL != null ? Build.MODEL : UNKNOWN;
    }


    public class Sort {
        public final static String POPULAR = "popular";
        public final static String RATING = "rating";
        public final static String SUBSCRIPTION_COUNT = "subscription_count";
        public final static String LATEST_AIR_DATE = "latest_air_date";

    }

}
