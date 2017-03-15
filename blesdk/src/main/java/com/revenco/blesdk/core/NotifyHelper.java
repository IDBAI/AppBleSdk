package com.revenco.blesdk.core;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.os.Build;
import android.support.annotation.RequiresApi;

import com.revenco.blesdk.callback.BleConnectGattCallback;
import com.revenco.blesdk.callback.CallbackConnectHelper;
import com.revenco.blesdk.interfaces.oniBeaconStatusListener;
import com.revenco.blesdk.utils.ConvertUtil;
import com.revenco.blesdk.utils.XLog;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static com.revenco.blesdk.core.iBeaconManager.GattStatusEnum.GATT_STATUS_SENDDING_DATA;

/**
 * <p>PROJECT : WeShare</p>
 * <p>COMPANY : wanzhong</p>
 * <p>AUTHOR : Administrator on 2017/3/9 15:52.</p>
 * <p>CLASS DESCRIBE :设置 notify 帮助类</p>
 * <p>CLASS_VERSION : 1.0.0</p>
 */
@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
public class NotifyHelper {
    private static final String TAG = "NotifyHelper";
    private static NotifyHelper instance;
    private Map<String, Boolean> notifyuuidmap = new HashMap<>();

    public synchronized static NotifyHelper getInstance() {
        if (instance == null)
            instance = new NotifyHelper();
        return instance;
    }

    public void fillmap(BluetoothGattDescriptor desc) {
        if (desc.getUuid() != null) {
            XLog.d(TAG, "desc.getUuid().toString() = " + desc.getUuid().toString());
            notifyuuidmap.put(desc.getUuid().toString(), false);
        }
    }

    public int getnotifyuuidmapSize() {
        return notifyuuidmap.size();
    }

    public void setNextNotify(oniBeaconStatusListener listener, BluetoothGatt gatt) {
        XLog.d(TAG, "setNextNotify() called with: listener = [" + listener + "], gatt = [" + gatt + "]");
        BluetoothGattService service = CallbackConnectHelper.getInstance().getBluetoothGattService(gatt);
        if (service == null) {
            XLog.d(TAG, "    service is null");
            return;
        }
        BluetoothGattCharacteristic characteristic = service.getCharacteristic(UUID.fromString(Config.NOTIFY_UUID));
        if (characteristic == null) {
            XLog.d(TAG, "   characteristic is null");
            return;
        }
        GattStatusMachine.publicMachineStatus(listener, GATT_STATUS_SENDDING_DATA);
        //经测试：5.0.2不设置描述符不能回调onCharacteristicChanged，4.4.2 可以不设置描述符，
        //猜测：5.0以上系统必须要设置描述符，否则不能回调 onCharacteristicChanged 。
        boolean setCharacteristicNotification = gatt.setCharacteristicNotification(characteristic, true);
        XLog.d("GOOD", "   setCharacteristicNotification: " + setCharacteristicNotification);
        try {
            UUID uuid = UUID.fromString(getnotifyUuid());
            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(uuid);
            debug(descriptor);
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            boolean writeDescriptor = gatt.writeDescriptor(descriptor);
            XLog.d("GOOD", "   writeDescriptor: " + writeDescriptor);
            BleConnectGattCallback.isWritting = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getnotifyUuid() {
        XLog.d(TAG, "getnotifyUuid() called");
        String uuid = "";
        Set<String> keySet = notifyuuidmap.keySet();
        for (String key : keySet) {
            Boolean aBoolean = notifyuuidmap.get(key);
            if (!aBoolean) {
                uuid = key;
                break;
            }
        }
        XLog.d(TAG, "uuid = " + uuid);
        return uuid;
    }

    private void debug(BluetoothGattDescriptor descriptor) {
        if (descriptor == null)
            return;
        String descriptor_uuid = descriptor.getUuid().toString();
        XLog.d("GOOD", "descriptor_uuid = " + descriptor_uuid);
        byte[] value = descriptor.getValue();
        if (value != null) {
            String descriptor_values = ConvertUtil.byte2HexStrWithSpace(value);
            XLog.d("GOOD", "descriptor_values = " + descriptor_values);
        }
    }

    /**
     * 设置标识
     *
     * @param uuid
     */
    public void setMark(String uuid) {
        XLog.d(TAG, "setMark() called with: uuid = [" + uuid + "]");
        Set<String> keySet = notifyuuidmap.keySet();
        for (String key : keySet) {
            if (key.equalsIgnoreCase(uuid)) {
                notifyuuidmap.put(key, true);
                break;
            }
        }
    }

    /**
     * 是否完成设置
     *
     * @return
     */
    public boolean isfinishset() {
        XLog.d(TAG, "isfinishset() called");
        boolean result = true;
        Set<String> keySet = notifyuuidmap.keySet();
        for (String key : keySet) {
            Boolean aBoolean = notifyuuidmap.get(key);
            if (!aBoolean) {
                result = false;
                break;
            }
        }
        XLog.d(TAG, "result = " + result);
        if (result) {
            notifyuuidmap.clear();
        }
        return result;
    }

    public void debuginfo(String string) {
        XLog.d(TAG, string);
    }
}
