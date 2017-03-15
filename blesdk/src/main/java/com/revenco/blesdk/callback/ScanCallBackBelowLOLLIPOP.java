package com.revenco.blesdk.callback;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.Build;

import com.revenco.blesdk.core.iBeaconManager;
import com.revenco.blesdk.interfaces.oniBeaconStatusListener;
import com.revenco.blesdk.utils.ConvertUtil;
import com.revenco.commonlibrary.log.XLog;

import java.util.Arrays;
import java.util.concurrent.ExecutorService;

/**
 * Created by Administrator on 2016/11/9.
 * API 18——21
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class ScanCallBackBelowLOLLIPOP implements BluetoothAdapter.LeScanCallback {
    private static final String TAG = "ScanCallBackBelowLOLLIP";
    private static int MAX_REOPEN = 3;
    private final Context context;
    private ExecutorService mExecutor;
    private oniBeaconStatusListener listener;
    private volatile int times = 0;
    private BluetoothAdapter bluetoothAdapter;

    public ScanCallBackBelowLOLLIPOP(Context context, BluetoothAdapter bluetoothAdapter) {
        this.context = context;
        this.bluetoothAdapter = bluetoothAdapter;
    }

    @Override
    public synchronized void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
        XLog.d(TAG, "API [18,21): T(" + Build.VERSION.SDK_INT + ") device = " + device.toString() + " ,rssi = " + rssi + " , scanRecord = " + ConvertUtil.byte2HexStr(scanRecord));
//
//        scanRecord = 02 01 06 11 07  11 22 33 44 55 66 33 55 77 99 88 88 11 33 55 77                             05 12 06 00 06 00
//
//         device = 66:66:66:66:66:66 ,rssi = -51 , scanRecord = 02 01 06 11 07         66 9A 0C 20 00 08 99 77 55 33 66 55 44 33 22 11             05 12 06 00 06 00 02 0A 08 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00
//        : onScanResult() - Device=66:66:66:66:66:66 RSSI=-48
//        截取广播的service_uuid
        byte[] service_UUID = new byte[16];
        try {
            System.arraycopy(scanRecord, 5, service_UUID, 0, 16);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (Arrays.equals(service_UUID, iBeaconManager.getInstance().SERVICE_UUID_1)
                || Arrays.equals(service_UUID, iBeaconManager.getInstance().SERVICE_UUID_2)) {
            //find my devices,pause scan
            if (listener != null) {
                listener.onIbeaconHadDetect(device, null);
                //// TODO: 2016/11/17
                CallbackConnectHelper.getInstance().connect(context, device, listener);
            }
        }
    }

    public void setListener(oniBeaconStatusListener listener) {
        this.listener = listener;
    }
}
