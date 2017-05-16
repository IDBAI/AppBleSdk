package com.revenco.blesdk.core;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.os.Build;
import android.os.Handler;

import com.revenco.blesdk.callback.BleConnectGattCallback;
import com.revenco.blesdk.callback.CallbackConnectHelper;
import com.revenco.blesdk.utils.ConvertUtil;
import com.revenco.blesdk.utils.XLog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import static com.revenco.blesdk.callback.BleConnectGattCallback.MSG_READ_RESULT;
import static com.revenco.blesdk.callback.BleConnectGattCallback.WAIT_READ_RESULT_DELAY;
import static com.revenco.blesdk.callback.BleConnectGattCallback.isFinishSendData;
import static com.revenco.blesdk.core.Config.WRITE_UUID1;
import static com.revenco.blesdk.core.Config.WRITE_UUID2;
import static com.revenco.blesdk.core.Config.WRITE_UUID3;
import static com.revenco.blesdk.core.Config.WRITE_UUID4;
import static com.revenco.blesdk.core.Config.WRITE_UUID5;
import static com.revenco.blesdk.core.Config.WRITE_UUID6;
import static com.revenco.blesdk.core.Config.WRITE_UUID7;
import static com.revenco.blesdk.core.iBeaconManager.GattStatusEnum.GATT_STATUS_SENDDING_DATA;
import static com.revenco.blesdk.core.iBeaconManager.GattStatusEnum.GATT_STATUS_WAITTING_NOTIFY;

/**
 * <p>PROJECT : WeShare</p>
 * <p>COMPANY : wanzhong</p>
 * <p>AUTHOR : Administrator on 2017/3/9 14:23.</p>
 * <p>CLASS DESCRIBE :</p>
 * <p>CLASS_VERSION : 1.0.0</p>
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class DataHelper {
    private static final String TAG = "DataHelper";
    private static final int SIZE = 20;
    private static DataHelper instance;
    private Queue<Object> mWrittingQueue;
    private byte[] data = new byte[]{
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x02,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x03,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x04,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x05,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x06,
    };
    private Map<String, Boolean> WRITEUUIDMAP = new HashMap<>();

    public synchronized static DataHelper getInstance() {
        if (instance == null)
            instance = new DataHelper();
        return instance;
    }

    public void initUUIDMark() {
        WRITEUUIDMAP.put(WRITE_UUID1, false);
        WRITEUUIDMAP.put(WRITE_UUID2, false);
        WRITEUUIDMAP.put(WRITE_UUID3, false);
        WRITEUUIDMAP.put(WRITE_UUID4, false);
        WRITEUUIDMAP.put(WRITE_UUID5, false);
        WRITEUUIDMAP.put(WRITE_UUID6, false);
        WRITEUUIDMAP.put(WRITE_UUID7, false);
    }

    /**
     * 设置UUID写入成功的标志
     * <p>
     * //        11111111-2000-0896-9EE2-119E11111111 -> MIUI8 错误的UUID
     * <p>
     * //        11111111-96E2-119E-9E11-E29611111111 ->正确
     *
     * @param charUuid
     */
    public void setUUIDHasWrited(String charUuid) {
        XLog.d(TAG, "setUUIDHasWrited() called with: desUuid = [" + charUuid + "]");
        if (WRITEUUIDMAP.containsKey(charUuid)) {
            WRITEUUIDMAP.put(charUuid, true);
        } else {
            //兼容UUID读取出错问题
            String uuid = "";
            if (charUuid.startsWith("11111111"))
                uuid = Config.WRITE_UUID1;
            else if (charUuid.startsWith("22222222"))
                uuid = Config.WRITE_UUID2;
            else if (charUuid.startsWith("33333333"))
                uuid = Config.WRITE_UUID3;
            else if (charUuid.startsWith("44444444"))
                uuid = Config.WRITE_UUID4;
            else if (charUuid.startsWith("55555555"))
                uuid = Config.WRITE_UUID5;
            else if (charUuid.startsWith("66666666"))
                uuid = Config.WRITE_UUID6;
            else if (charUuid.startsWith("77777777"))
                uuid = Config.WRITE_UUID7;
            WRITEUUIDMAP.put(uuid, true);
        }
    }

    private String getNextWriteUUID() {
        XLog.d(TAG, "getNextWriteUUID() called");
        if (!WRITEUUIDMAP.get(WRITE_UUID1)) {
            return WRITE_UUID1;
        } else if (!WRITEUUIDMAP.get(WRITE_UUID2)) {
            return WRITE_UUID2;
        } else if (!WRITEUUIDMAP.get(WRITE_UUID3)) {
            return WRITE_UUID3;
        } else if (!WRITEUUIDMAP.get(WRITE_UUID4)) {
            return WRITE_UUID4;
        } else if (!WRITEUUIDMAP.get(WRITE_UUID5)) {
            return WRITE_UUID5;
        } else if (!WRITEUUIDMAP.get(WRITE_UUID6)) {
            return WRITE_UUID6;
        } else if (!WRITEUUIDMAP.get(WRITE_UUID7)) {
            return WRITE_UUID7;
        } else
            return "";
    }

    public synchronized void nextWrite(BluetoothGatt gatt, Handler mHandler) {
        if (!mWrittingQueue.isEmpty()) {
            XLog.d(TAG, "isWritting = " + BleConnectGattCallback.isWritting);
            if (BleConnectGattCallback.isWritting) {
                XLog.d(TAG, "is Writting.");
            } else {
                printWriteQueue();
                doWrite(gatt, mWrittingQueue.poll(), mHandler);
            }
        } else {
            XLog.d(TAG, "mWrittingQueue is empty.");
            isFinishSendData = true;
            GattStatusMachine.publicMachineStatus(CallbackConnectHelper.getbleConnectGattCallback().getListener(), GATT_STATUS_WAITTING_NOTIFY);
            if (mHandler != null) {
                XLog.d("result_timeout", "设置了" + WAIT_READ_RESULT_DELAY + " ms 等待notify，否则主动读取状态!");
                mHandler.sendEmptyMessageDelayed(MSG_READ_RESULT, WAIT_READ_RESULT_DELAY);
            }else {
                XLog.d("result_timeout", "mHandler == null !");
            }
        }
    }

    private synchronized void printWriteQueue() {
        XLog.d(TAG, "printWriteQueue() called");
        XLog.d(TAG, "mWrittingQueue size is " + mWrittingQueue.size());
    }

    public synchronized void doWrite(BluetoothGatt gatt, Object o, Handler mHandler) {
        XLog.d(TAG, "doWrite() called with:  o = [" + o + "]");
        if (o instanceof byte[]) {//队列写入特征值
            if (!GattStatusMachine.publicMachineStatus(CallbackConnectHelper.getbleConnectGattCallback().getListener(), GATT_STATUS_SENDDING_DATA))
                return;
            writeBytes(gatt, (byte[]) o);
        } else {
            nextWrite(gatt, mHandler);
        }
    }

    /**
     * @param gatt
     * @param bytes 写入byte[]
     */
    private synchronized void writeBytes(BluetoothGatt gatt, byte[] bytes) {
        XLog.d(TAG, "writeBytes() called with: bytes = [" + bytes + "]");
        BluetoothGattService service = CallbackConnectHelper.getInstance().getBluetoothGattService(gatt);
        if (service == null) {
            XLog.d(TAG, "    service is null");
            return;
        }
        BluetoothGattCharacteristic character = CallbackConnectHelper.getInstance().getGattCharByConfigUUID(service, getNextWriteUUID());
        if (character == null) {
            XLog.d(TAG, "    character is null");
            return;
        }
        BleConnectGattCallback.isWritting = true;
        try {
            Thread.sleep(Config.SEND_INTERVAL);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        character.setValue(bytes);
        String s = ConvertUtil.byte2HexStr(bytes);
        XLog.d(TAG, "   writing string is " + s);
        boolean b = gatt.writeCharacteristic(character);
        XLog.d(TAG, "   writeCharacteristic: " + b);
    }

    /**
     * 填充写的队列
     */
    public void fillWriteQueue() {
        mWrittingQueue = new ConcurrentLinkedQueue<>();//初始化队列
        ArrayList<byte[]> arrayList = spiltCertificate(data);
        for (byte[] list : arrayList) {
            mWrittingQueue.add(list);
        }
        XLog.d(TAG, "mWrittingQueue.size = " + mWrittingQueue.size());
    }

    /**
     * 将byte数组分包成size大小的array
     *
     * @param bytes
     * @param size
     * @param isneedpolish 是否需要末尾不足，补齐不足
     * @return
     */
    public ArrayList<byte[]> spiltCertificate(byte[] bytes, int size, boolean isneedpolish) {
        XLog.d(TAG, "spiltCertificate() called with: bytes = [" + bytes + "], size = [" + size + "]");
        XLog.d(TAG, ConvertUtil.byte2HexStrWithSpace(bytes));
        int arrSize = (int) Math.ceil(bytes.length * 1.0f / size);
        //如果要补齐
        if (isneedpolish) {
            if (bytes.length % size != 0) {
                int polishSize = size - bytes.length % size;//需要补齐的位数
                bytes = polishByte(bytes, polishSize);
                XLog.d(TAG, ConvertUtil.byte2HexStrWithSpace(bytes));
            }
        }
        ArrayList<byte[]> arrayList = new ArrayList<>(arrSize);
        for (int i = 0; i < bytes.length; i += size) {
            if (i / size == arrSize - 1 && bytes.length % size != 0) {//最后一包，并且不足size 大小，重新计算size
                size = bytes.length % size;
                XLog.d(TAG, "last size is : " + size);
            }
            byte[] des = new byte[size];
            System.arraycopy(bytes, i, des, 0, size);
            arrayList.add(des);
            XLog.d(TAG, ConvertUtil.byte2HexStrWithSpace(des));
        }
        return arrayList;
    }

    /**
     * 分包，默认20字节一包，不足一包不需要补齐
     *
     * @param bytes
     * @return
     */
    public ArrayList<byte[]> spiltCertificate(byte[] bytes) {
        return spiltCertificate(bytes, SIZE, false);
    }

    /**
     * 补齐
     *
     * @param bytes
     * @param polishSize
     * @return
     */
    private byte[] polishByte(byte[] bytes, int polishSize) {
        return ConvertUtil.merge(bytes, new byte[polishSize]);
    }
}
