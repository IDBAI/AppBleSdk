package com.revenco.blesdk.core;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.RequiresApi;
import android.widget.Toast;

import com.revenco.blesdk.bluetooth.BleBluetooth;
import com.revenco.blesdk.callback.CallbackConnectHelper;
import com.revenco.blesdk.interfaces.BluetoothExceptionListener;
import com.revenco.blesdk.interfaces.oniBeaconStatusListener;
import com.revenco.blesdk.utils.ConvertUtil;
import com.revenco.blesdk.utils.XLog;

import java.lang.reflect.Method;

/**
 * Created by Administrator on 2016/11/17.
 */
public class iBeaconManager implements BluetoothExceptionListener {
    private static final String TAG = "iBeaconManager";
    private static final int MSG_EXCEPTION = 2001;
    /**
     * 每一次扫描周期 3.5 秒扫描不到信号，返回超时失败
     */
    private static final long SCAN_PERIOD = 3500;
    /**
     * 超时
     */
    private static final long TIMEOUT = 5500;
    private static iBeaconManager INSTANCE;
    /**
     * beacon 的mac地址
     */
    public byte[] BLE_PUBLIC_MAC_ADDRESS;
    /**
     * 唯一的一个自己定义的服务UUID,16字节长，用于APP逻辑写入和接收通知
     */
    public byte[] SERVICE_UUID_1;
    /**
     *
     */
    public String SERVICE_UUID_STR_1;
    /**
     * 1 和 2  分别为大小端模式，这样省去匹配大小端考虑
     */
    public byte[] SERVICE_UUID_2;
    /**
     *
     */
    public String SERVICE_UUID_STR_2;
    private BleBluetooth bleBluetooth;
    private oniBeaconStatusListener listener;
    private Context context;
    private Handler mhandler;
    private boolean isinitialize = false;

    public static iBeaconManager getInstance() {
        if (INSTANCE == null)
            synchronized (iBeaconManager.class) {
                if (INSTANCE == null)
                    INSTANCE = new iBeaconManager();
            }
        return INSTANCE;
    }

    public void setSERVICE_UUID(byte[] SERVICE_UUID) throws Exception {
        if (SERVICE_UUID.length != 16)
            throw new Exception("SERVICE_UUID length is not 16 byte!");
        //大小端1
        this.SERVICE_UUID_1 = SERVICE_UUID;
        this.SERVICE_UUID_STR_1 = ConvertUtil.byteServiceUUID2string(SERVICE_UUID);
        //大小端2
        this.SERVICE_UUID_2 = new byte[16];
        for (int i = 0; i < SERVICE_UUID.length; i++) {
            this.SERVICE_UUID_2[i] = SERVICE_UUID[SERVICE_UUID.length - 1 - i];
        }
        this.SERVICE_UUID_STR_2 = ConvertUtil.byteServiceUUID2string(this.SERVICE_UUID_2);
    }

    public boolean isinitialize() {
        return isinitialize;
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public String getBleAddress() {
        if (bleBluetooth != null && bleBluetooth.getBluetoothAdapter() != null) {
            return bleBluetooth.getBluetoothAdapter().getAddress();
        } else {
            return "";
        }
    }

    /**
     * @param context
     * @param listener
     * @return
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public boolean init(Context context, oniBeaconStatusListener listener) {
        this.listener = listener;
        this.context = context;
        initHandler();
        bleBluetooth = BleBluetooth.getInStance();
        bleBluetooth.init(context, this);
        if (bleBluetooth.getBluetoothAdapter() == null) {
            Toast.makeText(context, "此设备不支持蓝牙", Toast.LENGTH_SHORT).show();
            return false;
        }
        boolean hasSystemFeature = context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);
        if (!hasSystemFeature) {
            Toast.makeText(context, "此设备不支持BLE", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!bleBluetooth.isOpenble()) {
            bleBluetooth.openBle();
            return false;
        }
        isinitialize = true;
        return true;
    }

    private void initBleBroadCast() {
        IntentFilter filter = new IntentFilter("android.bluetooth.adapter.action.STATE_CHANGED");
        filter.addAction(BluetoothDevice.ACTION_PAIRING_REQUEST);
        filter.setPriority(Integer.MAX_VALUE);
        context.registerReceiver(new PairingRequestReceive(), filter);
    }

    private void initHandler() {
        mhandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                switch (msg.what) {
                    case MSG_EXCEPTION:
                        Toast.makeText(context, "蓝牙可能出现了异常，尝试重启手机恢复。", Toast.LENGTH_LONG).show();
                        break;
                }
                return false;
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void startScan() {
        if (bleBluetooth != null) {
            try {
                bleBluetooth.startBLEScan(listener, SCAN_PERIOD, TIMEOUT);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (listener != null)
            listener.onReScan();
    }

    @Override
    public void onBlueThoodException() {
        mhandler.sendEmptyMessage(MSG_EXCEPTION);
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void stopScan() {
        if (bleBluetooth != null) {
            try {
                bleBluetooth.stopScan(false);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void closeConnectGatt() {
        CallbackConnectHelper.getInstance().closeGatt();
    }

    /**
     * There is a refresh() method in BluetoothGatt class but for now it's hidden. We will call it using reflections.
     *
     * @return
     */
    public boolean refreshDeviceCache() {
        XLog.d(TAG, "refreshDeviceCache() called");
        try {
            final Method refresh = BluetoothGatt.class.getMethod("refresh");
            BluetoothGatt bluetoothGatt = CallbackConnectHelper.getInstance().getBluetoothGatt();
            if (refresh != null && bluetoothGatt != null) {
                final boolean success = (Boolean) refresh.invoke(bluetoothGatt);
                XLog.i(TAG, "Refreshing result: " + success);
                return success;
            }
        } catch (Exception e) {
            XLog.e(e, TAG, "An exception occured while refreshing device");
        }
        return false;
    }

    public enum GattStatusEnum {
        //connect status
        GATT_STATUS_DISCONNECTED,
        GATT_STATUS_CONNECTED,
        GATT_STATUS_CONNECTTING,
        GATT_STATUS_DISCONNECTTING,
        //Service status
        GATT_STATUS_SERVICE_DISCOVERING,
        GATT_STATUS_SERVICE_DISCOVERED,
        //send data status
        GATT_STATUS_SENDDING_DATA,
        GATT_STATUS_SENDDATA_SUCCESS,
        GATT_STATUS_SENDDATA_FAILED,
        //
        GATT_STATUS_NOTIFY_SUCCESS,
        GATT_STATUS_NOTIFY_FAILED,
    }
}
