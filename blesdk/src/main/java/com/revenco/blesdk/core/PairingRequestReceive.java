package com.revenco.blesdk.core;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.RequiresApi;

import com.revenco.blesdk.callback.BleConnectGattCallback;
import com.revenco.blesdk.callback.CallbackConnectHelper;
import com.revenco.blesdk.utils.Constants;
import com.revenco.blesdk.utils.MySharedPreferences;
import com.revenco.blesdk.utils.XLog;

public class PairingRequestReceive extends BroadcastReceiver {
    private static final String TAG = "PairingRequestReceive";

    public PairingRequestReceive() {
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    public void onReceive(Context context, Intent intent) {
        BleConnectGattCallback mbleConnectGattCallback = CallbackConnectHelper.getbleConnectGattCallback();
        if (mbleConnectGattCallback != null) {
            iBeaconManager.GattStatusEnum gattStatus = mbleConnectGattCallback.getCurrentGattStatus();
            if (gattStatus == iBeaconManager.GattStatusEnum.GATT_STATUS_DISCONNECTED) {

                    //出去重启了扫描,不要弹窗
                    abortBroadcast();
                    return;

            }
        }
        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
        String address = device.getAddress();
        String myDeviceAddress = MySharedPreferences.getStringPreference(context, Constants.TAG_MyDeviceAddress);

        XLog.d(TAG, "onReceive() called with: context = [" + context + "], intent = [" + intent + "]");
        if (intent.getAction().equals(BluetoothDevice.ACTION_PAIRING_REQUEST)) {
             if (address.equals(myDeviceAddress)) {
                int mType = intent.getIntExtra(BluetoothDevice.EXTRA_PAIRING_VARIANT, BluetoothDevice.ERROR);
                switch (mType) {
                    case BluetoothDevice.PAIRING_VARIANT_PIN:

                        boolean isSuccessSetPin = CallbackConnectHelper.getInstance().autoSetPin(device);
                        XLog.d(TAG, "isSuccessSetPin = " + isSuccessSetPin);
                        if (isSuccessSetPin)//拦截掉广播，防止系统弹窗
                            abortBroadcast();
                        break;
                    case BluetoothDevice.PAIRING_VARIANT_PASSKEY_CONFIRMATION:
                        break;
                }
            }
        }else if (intent.getAction().equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)){
            if (address.equals(myDeviceAddress)) {
                switch (device.getBondState()){
                    case BluetoothDevice.BOND_BONDED:
                        XLog.d(TAG,"匹配成功.");
                        //去连接

                        break;
                    case BluetoothDevice.BOND_BONDING:
                        XLog.d(TAG,"正在匹配...");
                        break;
                    case BluetoothDevice.BOND_NONE:
                        XLog.d(TAG,"取消匹配.");
                        break;
                }

            }
        }
    }
//    int mType = intent.getIntExtra(BluetoothDevice.EXTRA_PAIRING_VARIANT, BluetoothDevice.ERROR);
//    // 蓝牙有两种配对类型一种是pin，一种是密钥 先搞清楚你的设备是哪一种在做，不然瞎忙活
//    boolean isSuccess;
//    switch (mType) {
//        case 0:
//            // 反射 不会自己收ClsUtils
//            isSuccess = ClsUtils.setPin(device.getClass(), device, 123456);
//            break;
//        case 1:
//            // 这个方法是我自己加的 ,不会的可以照着setPin写一个setPasskey boolean
//            // Bluetooth.setPasskey(int)
//            isSuccess = ClsUtils.setPassKey(device.getClass(), device, 123456);
//    }
}
