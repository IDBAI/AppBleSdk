package com.revenco.blesdk.interfaces;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanResult;

/**
 * Created by Administrator on 2016/11/9.
 */
public interface onDetectBeaconListener {
    /**
     * @param device
     * @param beacons    api 18~21
     * @param scanResult api 21~
     */
    void onDetectBeacon(BluetoothDevice device,   ScanResult scanResult);
}
