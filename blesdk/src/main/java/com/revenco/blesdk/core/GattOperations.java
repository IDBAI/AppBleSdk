package com.revenco.blesdk.core;

import android.bluetooth.BluetoothGatt;
import android.os.Build;
import android.support.annotation.RequiresApi;

import com.revenco.blesdk.callback.CallbackConnectHelper;
import com.revenco.blesdk.utils.XLog;

import static com.revenco.blesdk.core.iBeaconManager.GattStatusEnum.GATT_STATUS_NOTIFY_FAILED;
import static com.revenco.blesdk.core.iBeaconManager.GattStatusEnum.GATT_STATUS_NOTIFY_SUCCESS;

/**
 * <p>PROJECT : WeShare</p>
 * <p>COMPANY : wanzhong</p>
 * <p>AUTHOR : Administrator on 2017/3/9 11:29.</p>
 * <p>CLASS DESCRIBE :gatt 回调操作类</p>
 * <p>CLASS_VERSION : 1.0.0</p>
 */
@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
public class GattOperations {
    /**
     * 硬编码--通知开门成功 状态
     */
    public static final byte CHAR_NOTIFY_STATUS_SUCCESS_VALUE = (byte) 0x88;
    /**
     * 硬编码--通知开门失败 状态
     */
    public static final byte CHAR_NOTIFY_STATUS_FAILED_VALUE = (byte) 0xFF;
    /**
     * REASON——
     */
    public static final byte SUCCESS_REASON = (byte) 0x00;
    private static final String TAG = "GattOperations";

    /**
     * 处理通知操作
     *
     * @param gatt
     * @param values
     */
    public static void dealNotify(BluetoothGatt gatt, byte[] values) {
        switch (values[0]) {
            case CHAR_NOTIFY_STATUS_SUCCESS_VALUE:
                XLog.d(TAG, "开门成功！");
                GattStatusMachine.publicMachineStatus(CallbackConnectHelper.getbleConnectGattCallback().getListener(), GATT_STATUS_NOTIFY_SUCCESS);
                break;
            case CHAR_NOTIFY_STATUS_FAILED_VALUE:
                //TODO 解析原因
                GattStatusMachine.publicMachineStatus(CallbackConnectHelper.getbleConnectGattCallback().getListener(), GATT_STATUS_NOTIFY_FAILED, "这里是失败原因");
                break;
        }

        XLog.d(TAG, "处理通知操作 -->  gatt.disconnect();");
        gatt.disconnect();
    }
}
