package com.revenco.appblesdk;

import android.app.Application;

import com.revenco.appblesdk.utils.CrashHandler;

/**
 * Created by Administrator on 2016/12/21.
 */
public class BleApplication extends Application {
    /**
     * beacon 的mac地址
     */
    public static final byte[] BLE_PUBLIC_MAC_ADDRESS = {0x66, 0x66, 0x66, 0x66, 0x66, 0x66};
    /**
     * 唯一的一个自己定义的服务UUID,16字节长，用于APP逻辑写入和接收通知
     * <p>
     * 66 9A 0C 20           00 08        99 77 55 33    66 55 44 33 22 11
     */
    public static final byte[] SERVICE_UUID = {0x11, 0x22, 0x33, 0x44, 0x55, 0x66, 0x33, 0x55, 0x77, (byte) 0x99, 0x08, 0x00, 0x20, 0x0C, (byte) 0x9A, 0x66};
//    /**
//     * iBeacon广播UUID
//     */
//    public static final String BROADCAST_FILTER_UUID = "D973F2E0-B19E-11E2-9E96-0800200C9A66";
//    /**
//     * 服务UUID
//     */
//    public static final String SERVICE_UUID_1 = BROADCAST_FILTER_UUID;
    /**
     * 特征值UUID notify功能
     */
//    public static final String NOTIFY_UUID = "D973F2E1-B19E-11E2-9E96-0800200C9A66";
    /**
     * 特征值UUID write功能
     */
//    public static final String WRITE_UUID = "D973F2E2-B19E-11E2-9E96-0800200C9A66";
    private static final String TAG = "BleApplication";
    private CrashHandler crashHandler;

    @Override
    public void onCreate() {
        super.onCreate();
        crashHandler = CrashHandler.getInstance();
        crashHandler.init(getApplicationContext());
    }
}
