package com.revenco.blesdk.utils;

import android.content.Context;
import android.os.Build;

import java.util.ArrayList;

/**
 * Created by Administrator on 2017/1/23.
 */
public class SDKUtils {
    public static String getbleSDK(Context appContext) {
        if (appContext.getPackageManager().hasSystemFeature("android.hardware.bluetooth_le")) {
            System.out.println("BLESDK.ANDROID");
            return "BLESDK.ANDROID";//SDKUtils.BLESDK.ANDROID;
        } else {
            ArrayList libraries = new ArrayList();
            String[] libraryNames = appContext.getPackageManager().getSystemSharedLibraryNames();
            int length = libraryNames.length;
            for (int i = 0; i < length; ++i) {
                String string = libraryNames[i];
                libraries.add(string);
            }
            if (Build.VERSION.SDK_INT >= 17) {
                if (libraries.contains("com.samsung.android.sdk.bt")) {
                    System.out.println("BLESDK.SAMSUNG");
                    return "BLESDK.SAMSUNG";//SDKUtils.BLESDK.SAMSUNG;
                }
                if (libraries.contains("com.broadcom.bt")) {
                    System.out.println("BLESDK.BROADCOM");
                    return "BLESDK.BROADCOM";//SDKUtils.BLESDK.BROADCOM;
                }
            }
            System.out.println("BLESDK.NOT_SUPPORTED");
            return "BLESDK.NOT_SUPPORTED";//SDKUtils.BLESDK.NOT_SUPPORTED;
        }
    }

    public static enum BLESDK {
        NOT_SUPPORTED,
        ANDROID,
        SAMSUNG,
        BROADCOM;

        private BLESDK() {
        }
    }
}
