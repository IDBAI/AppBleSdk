package com.revenco.blesdk.core;

import com.revenco.blesdk.interfaces.oniBeaconStatusListener;
import com.revenco.blesdk.utils.XLog;

import static com.revenco.blesdk.callback.BleConnectGattCallback.currentGattStatus;
import static com.revenco.blesdk.core.iBeaconManager.GattStatusEnum.GATT_STATUS_CONNECTED;
import static com.revenco.blesdk.core.iBeaconManager.GattStatusEnum.GATT_STATUS_CONNECTTING;
import static com.revenco.blesdk.core.iBeaconManager.GattStatusEnum.GATT_STATUS_DISCONNECTED;
import static com.revenco.blesdk.core.iBeaconManager.GattStatusEnum.GATT_STATUS_DISCONNECTTING;
import static com.revenco.blesdk.core.iBeaconManager.GattStatusEnum.GATT_STATUS_SENDDATA_FAILED;
import static com.revenco.blesdk.core.iBeaconManager.GattStatusEnum.GATT_STATUS_SENDDATA_SUCCESS;
import static com.revenco.blesdk.core.iBeaconManager.GattStatusEnum.GATT_STATUS_SENDDING_DATA;
import static com.revenco.blesdk.core.iBeaconManager.GattStatusEnum.GATT_STATUS_SERVICE_DISCOVERED;
import static com.revenco.blesdk.core.iBeaconManager.GattStatusEnum.GATT_STATUS_SERVICE_DISCOVERING;

/**
 * <p>PROJECT : WeShare</p>
 * <p>COMPANY : wanzhong</p>
 * <p>AUTHOR : Administrator on 2017/3/9 11:45.</p>
 * <p>CLASS DESCRIBE :gatt 状态机</p>
 * <p>CLASS_VERSION : 1.0.0</p>
 */
public class GattStatusMachine {
    private static final String TAG = "GattStatusMachine";

    /**
     * 发布状态机状态转移情况——状态转移
     *
     * @param listener
     * @param newStatus
     * @return true:正常合法运行，false：非法状态转移，不能处理。
     */
    public static synchronized boolean publicMachineStatus(oniBeaconStatusListener listener, iBeaconManager.GattStatusEnum newStatus) {
        XLog.d(TAG, "publicMachineStatus() called with: newStatus = [" + newStatus + "]");
        boolean isLegal = true;
        switch (newStatus) {
            case GATT_STATUS_DISCONNECTED:
                if (currentGattStatus == GATT_STATUS_CONNECTTING
                        || currentGattStatus == GATT_STATUS_DISCONNECTTING
                        || currentGattStatus == GATT_STATUS_CONNECTED
                        || currentGattStatus == GATT_STATUS_SERVICE_DISCOVERING
                        || currentGattStatus == GATT_STATUS_SENDDING_DATA//v1.6
                        || currentGattStatus == GATT_STATUS_SENDDATA_SUCCESS//v1.7
                        || currentGattStatus == GATT_STATUS_DISCONNECTED//v1.7
                        )
                    currentGattStatus = newStatus;
                else
                    isLegal = false;
                break;
            case GATT_STATUS_CONNECTTING:
                if (currentGattStatus == GATT_STATUS_DISCONNECTED)
                    currentGattStatus = newStatus;
                else
                    isLegal = false;
                break;
            case GATT_STATUS_CONNECTED:
                if (currentGattStatus == GATT_STATUS_CONNECTTING
                        || currentGattStatus == GATT_STATUS_CONNECTED
                        || currentGattStatus == GATT_STATUS_SERVICE_DISCOVERING
                        || currentGattStatus == GATT_STATUS_DISCONNECTED)
                    currentGattStatus = newStatus;
                else {
//                    XLog.d(TAG, "处理了异常。");
//                    connectGatt.disconnect();
                    isLegal = false;
                }
                break;
            case GATT_STATUS_SERVICE_DISCOVERING:
                if (currentGattStatus == GATT_STATUS_CONNECTED ||
                        currentGattStatus == GATT_STATUS_SERVICE_DISCOVERED)
                    currentGattStatus = newStatus;
                else
                    isLegal = false;
                break;
            case GATT_STATUS_SERVICE_DISCOVERED:
                if (currentGattStatus == GATT_STATUS_SERVICE_DISCOVERING)
                    currentGattStatus = newStatus;
                else
                    isLegal = false;
                break;
            case GATT_STATUS_SENDDING_DATA:
                if (currentGattStatus == GATT_STATUS_SERVICE_DISCOVERED
                        || currentGattStatus == GATT_STATUS_SENDDATA_SUCCESS
                        || currentGattStatus == GATT_STATUS_SENDDATA_FAILED
                        || currentGattStatus == GATT_STATUS_SENDDING_DATA)
                    currentGattStatus = newStatus;
                else
                    isLegal = false;
                break;
            case GATT_STATUS_SENDDATA_SUCCESS:
                if (currentGattStatus == GATT_STATUS_SENDDING_DATA)
                    currentGattStatus = newStatus;
                else
                    isLegal = false;
                break;
            case GATT_STATUS_SENDDATA_FAILED:
                if (currentGattStatus == GATT_STATUS_SENDDING_DATA)
                    currentGattStatus = newStatus;
                else
                    isLegal = false;
                break;
            case GATT_STATUS_DISCONNECTTING:
                if (currentGattStatus == GATT_STATUS_SENDDATA_SUCCESS
                        || currentGattStatus == GATT_STATUS_SERVICE_DISCOVERED
                        || currentGattStatus == GATT_STATUS_SERVICE_DISCOVERING
                        || currentGattStatus == GATT_STATUS_SENDDATA_FAILED
                        || currentGattStatus == GATT_STATUS_DISCONNECTTING
                        || currentGattStatus == GATT_STATUS_CONNECTTING)
                    currentGattStatus = newStatus;
                else
                    isLegal = false;
                break;
        }
        XLog.d(TAG, "currentGattStatus = " + currentGattStatus);
        if (isLegal) {
            if (listener != null)
                listener.onStatusChange(currentGattStatus);
            else {
                XLog.d(TAG, "# listener is null ? why.");
            }
        } else {
            XLog.e(TAG, "*machine status changed illegal.*");
        }
        return isLegal;
    }
}
