package com.revenco.blesdk.interfaces;

import android.bluetooth.BluetoothGatt;

import com.revenco.blesdk.exception.GattException;

/**
 * Created by Administrator on 2016/11/14.
 */
public interface bleCharacterCallback {
    void onWriteDataSuccess(BluetoothGatt gatt, Object object);

    void onWriteDataFailure(GattException gattException);
}
