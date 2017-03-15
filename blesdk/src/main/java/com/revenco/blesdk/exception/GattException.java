package com.revenco.blesdk.exception;

import android.bluetooth.BluetoothGatt;

public class GattException extends BleException {
    private String message;
    private int gattStatus;

    public BluetoothGatt getGatt() {
        return gatt;
    }

    private BluetoothGatt gatt;

    public GattException(BluetoothGatt gatt, int gattStatus) {
        super(ERROR_CODE_GATT, "Gatt Exception Occurred! ");
        this.gattStatus = gattStatus;
        this.gatt = gatt;
    }

    public GattException(BluetoothGatt gatt, String message) {
        super(ERROR_CODE_GATT, "Gatt Exception Occurred! ");
        this.message = message;
        this.gatt = gatt;
    }

    public int getGattStatus() {
        return gattStatus;
    }

    public GattException setGattStatus(int gattStatus) {
        this.gattStatus = gattStatus;
        return this;
    }

    @Override
    public String toString() {
        return "GattException{" +
                "gattStatus=" + gattStatus +
                "} " + super.toString();
    }
}
