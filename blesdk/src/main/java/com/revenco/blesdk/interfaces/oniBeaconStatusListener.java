package com.revenco.blesdk.interfaces;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanResult;

import com.revenco.blesdk.core.iBeaconManager;

/**
 * Created by Administrator on 2016/11/14.
 * 对外上层接口
 */
public interface oniBeaconStatusListener {
    void onIbeaconHadDetect(BluetoothDevice device, ScanResult scanResult);

    void onStatusChange(iBeaconManager.GattStatusEnum statusEnum, String... attr);

    void onRssiCallback(double distance, int rssi);

    void timeout();
}
