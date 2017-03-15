package com.revenco.blesdk.bean;

import android.bluetooth.BluetoothGatt;
import android.os.Parcel;
import android.os.Parcelable;

import com.revenco.blesdk.bluetooth.BleBluetooth;

import java.io.Serializable;

/**
 * Created by Administrator on 2016/11/9.
 */
public class TransData implements Serializable, Parcelable {
    public static final Creator<TransData> CREATOR = new Creator<TransData>() {
        @Override
        public TransData createFromParcel(Parcel in) {
            return new TransData(in);
        }

        @Override
        public TransData[] newArray(int size) {
            return new TransData[size];
        }
    };
    private BluetoothGatt gatt;
    private Object object;
    private BleBluetooth bleBluetooth;
    public TransData(BluetoothGatt gatt) {
        this.gatt = gatt;
    }

    public TransData(BluetoothGatt gatt, Object object) {
        this.gatt = gatt;
        this.object = object;
    }

    public TransData() {
    }

    public TransData(Parcel in) {
    }

    public TransData(BleBluetooth bleBluetooth) {
        this.bleBluetooth = bleBluetooth;
    }

    public BluetoothGatt getGatt() {
        return gatt;
    }

    public Object getObject() {
        return object;
    }

    public BleBluetooth getBleBluetooth() {
        return bleBluetooth;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
    }
}
