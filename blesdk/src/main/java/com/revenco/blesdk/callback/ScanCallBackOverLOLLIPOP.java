package com.revenco.blesdk.callback;

import android.annotation.TargetApi;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.os.Build;
import android.os.ParcelUuid;
import android.util.Log;

import com.revenco.blesdk.core.iBeaconManager;
import com.revenco.blesdk.interfaces.oniBeaconStatusListener;
import com.revenco.blesdk.utils.XLog;

import java.util.List;

/**
 * Created by Administrator on 2016/11/9.
 * Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
 * API 21 以及21以上
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class ScanCallBackOverLOLLIPOP extends ScanCallback {
    private static final String TAG = "ScanCallBackOverLOLLIPO";
    private oniBeaconStatusListener listener;
    private Context context;

    public ScanCallBackOverLOLLIPOP() {
        super();
    }

    public ScanCallBackOverLOLLIPOP(Context context) {
        this.context = context;
    }

    @Override
    public synchronized void onScanResult(int callbackType, ScanResult result) {
        super.onScanResult(callbackType, result);
        XLog.d(TAG, "API [21,+00):T(" + Build.VERSION.SDK_INT + ") callbackType = " + callbackType + " , result = " + result.toString());
        if (result != null) {
            ScanRecord scanRecord = result.getScanRecord();
            List<ParcelUuid> serviceUuids = scanRecord.getServiceUuids();
            if (serviceUuids != null) {
                ParcelUuid parcelUuid = serviceUuids.get(0);
                if (parcelUuid != null && parcelUuid.getUuid() != null) {
                    if (parcelUuid.getUuid().toString().equalsIgnoreCase(iBeaconManager.getInstance().SERVICE_UUID_STR_1)
                            || parcelUuid.getUuid().toString().equalsIgnoreCase(iBeaconManager.getInstance().SERVICE_UUID_STR_2)) {
                        if (listener != null) {
                            listener.onIbeaconHadDetect(result.getDevice(), null);
                            //// TODO: 2016/11/17
                            CallbackConnectHelper.getInstance().connect(context, result.getDevice(), listener);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void onBatchScanResults(List<ScanResult> results) {
        super.onBatchScanResults(results);
    }

    @Override
    public void onScanFailed(int errorCode) {
        super.onScanFailed(errorCode);
        Log.e(TAG, "BeaconService > onScanFailed() > Failed!");
        switch (errorCode) {
            case 0:
                XLog.e(TAG, "BeaconService > onScanFailed() > Error 0");
                break;
            case 1:
                XLog.e(TAG, "BeaconService > onScanFailed() > GATT error (1)");
                break;
            case 2:
                XLog.e(TAG, "BeaconService > onScanFailed() > Error 2 : ");
                break;
            case 3:
                XLog.e(TAG, "BeaconService > onScanFailed() > Error 3");
                break;
        }
    }

    public void setListener(oniBeaconStatusListener listener) {
        this.listener = listener;
    }
}
