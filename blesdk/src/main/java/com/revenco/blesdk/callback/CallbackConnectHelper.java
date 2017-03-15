package com.revenco.blesdk.callback;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;

import com.revenco.blesdk.core.Config;
import com.revenco.blesdk.core.iBeaconManager;
import com.revenco.blesdk.interfaces.oniBeaconStatusListener;
import com.revenco.blesdk.utils.ClsUtils;
import com.revenco.blesdk.utils.Constants;
import com.revenco.blesdk.utils.MySharedPreferences;
import com.revenco.commonlibrary.log.XLog;

import java.util.List;
import java.util.UUID;

/**
 * Created by Administrator on 2016/11/17.
 */
@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
public class CallbackConnectHelper {
    private static final String TAG = "CallbackConnectHelper";
    private static CallbackConnectHelper INSTANCE = new CallbackConnectHelper();
    private static BleConnectGattCallback mbleConnectGattCallback;
    private BluetoothDevice device;
    private oniBeaconStatusListener listener;
    private BluetoothGatt connectGatt;
    private Context context;

    public static BleConnectGattCallback getbleConnectGattCallback() {
        return mbleConnectGattCallback;
    }

    public static CallbackConnectHelper getInstance() {
        return INSTANCE;
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public synchronized void connect(Context context, BluetoothDevice device, oniBeaconStatusListener listener) {
        XLog.d(TAG, "connect() called with: context = [" + context + "], device = [" + device + "], listener = [" + listener + "]");
        this.context = context;
        this.device = device;
        this.listener = listener;
        if (mbleConnectGattCallback == null)
            mbleConnectGattCallback = new BleConnectGattCallback();
        mbleConnectGattCallback.init(context, device, listener);//reset callback
        iBeaconManager.getInstance().stopScan();
        if (mbleConnectGattCallback.canNewDeviceComeiin()) {
            XLog.d(TAG, "new device is come in.");
            MySharedPreferences.setStringPreference(context, Constants.TAG_MyDeviceAddress, device.getAddress());
            connectGatt = device.connectGatt(context, true, mbleConnectGattCallback);
            if (connectGatt == null)
                XLog.e(TAG, "connectGatt == null 请修复。。");
            mbleConnectGattCallback.setGattForFirstTime(connectGatt);
        }
    }

    public boolean autoSetPin(BluetoothDevice device) {
//        XLog.d(TAG, "临时屏蔽自动匹配pin码。");
        XLog.d(TAG, "autoSetPin() called with: device = [" + device + "]");
        if (device == null)
            return false;
        if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
            try {
                String strPsw = "368368";
                return ClsUtils.setPin(device.getClass(), device, strPsw); // 手机和蓝牙采集器配对
//                ClsUtils.cancelPairingUserInput(device.getClass(), device); //一般调用不成功
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * 关闭gatt连接，释放资源,非常重要，必须要调用
     */
    public synchronized void closeGatt() {
        if (connectGatt != null) {
            XLog.d(TAG, "closeGatt() called");
            try {
                Thread.sleep(400);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            XLog.d(TAG, "closeGatt() -> connectGatt.close()");
            connectGatt.close();
        }
    }

    public synchronized BluetoothGatt getBluetoothGatt() {
        return connectGatt;
    }

    @Nullable
    public BluetoothGattService getBluetoothGattService(BluetoothGatt gatt) {
        XLog.d(TAG, "getBluetoothGattService() called with: gatt = [" + gatt + "]");
        BluetoothGattService service = gatt.getService(UUID.fromString(iBeaconManager.getInstance().SERVICE_UUID_STR_1));
        if (service == null) {
            service = gatt.getService(UUID.fromString(iBeaconManager.getInstance().SERVICE_UUID_STR_2));
            if (service == null) {
                XLog.e(TAG, "if like xiaomi error,resolve it!");
                List<BluetoothGattService> services = gatt.getServices();
                if (services != null && services.size() > 0) {
                    for (BluetoothGattService gattService : services) {
                        if (gattService.getCharacteristics().size() == Config.CHAR_SIZE) {
                            XLog.d(TAG, "i get the gattService by Config.CHAR_SIZE!");
                            service = gattService;
                            break;
                        }
                    }
                }
            }
        }
        return service;
    }
}
