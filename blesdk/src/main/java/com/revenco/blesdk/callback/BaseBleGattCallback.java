package com.revenco.blesdk.callback;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.os.Build;

import com.revenco.blesdk.exception.ConnectException;

/**
 * Created by Administrator on 2016/11/11.
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public abstract class BaseBleGattCallback extends BluetoothGattCallback {
    public abstract void onConnectSuccess(BluetoothGatt gatt, int status, int newState);

    public abstract void onConnectFailure(ConnectException exception);

    @Override
    public abstract void onServicesDiscovered(BluetoothGatt gatt, int status);

    @Override
    public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
        super.onMtuChanged(gatt, mtu, status);
    }
}
