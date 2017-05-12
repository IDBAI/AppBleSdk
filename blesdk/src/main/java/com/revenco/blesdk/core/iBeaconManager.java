package com.revenco.blesdk.core;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.annotation.RequiresApi;
import android.widget.Toast;

import com.revenco.blesdk.bluetooth.BleBluetooth;
import com.revenco.blesdk.callback.CallbackConnectHelper;
import com.revenco.blesdk.interfaces.BluetoothExceptionListener;
import com.revenco.blesdk.interfaces.oniBeaconStatusListener;
import com.revenco.blesdk.utils.ConvertUtil;
import com.revenco.blesdk.utils.XLog;
import com.revenco.database.bean.BleOpenRecordBean;
import com.revenco.database.bean.StatisticalBean;
import com.revenco.database.buss.BleOpenRecordBuss;
import com.revenco.database.buss.StatisticalBuss;
import com.revenco.database.helper.BussHelper;

import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.revenco.blesdk.core.iBeaconManager.OpenResult.result_failed;

/**
 * Created by Administrator on 2016/11/17.
 */
public class iBeaconManager implements BluetoothExceptionListener, oniBeaconStatusListener {
    private static final String TAG = "iBeaconManager";
    private static final int MSG_EXCEPTION = 2001;
    /**
     * 每一次扫描周期 4.8 秒扫描不到信号，返回超时失败
     */
    private static final long SCAN_PERIOD = 4800;
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
    private int succeed, failure, timeout, rssiTotal, rssiCount;
    private BleBluetooth bleBluetooth;
    private oniBeaconStatusListener listener;
    private Context context;
    private Handler mhandler;
    private boolean isinitialize = false;
    /**
     * 开始时间
     */
    private long startmsTime;
    private long scanConsumeTime;
    private ExecutorService singleThreadExecutor;

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
     * 销毁listener，防止引起内存泄露
     */
    public void destoryListener() {
        this.listener = null;
    }

    /**
     * @param context
     * @param listener
     * @return
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public boolean init(Context context, oniBeaconStatusListener listener) {
        singleThreadExecutor = Executors.newSingleThreadExecutor();
        this.listener = listener;
        this.context = context.getApplicationContext();
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

    /**
     * 放置子线程中，使用了单个线程的线程池
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void startScan() {
        if (singleThreadExecutor == null)
            singleThreadExecutor = Executors.newSingleThreadExecutor();
        singleThreadExecutor.execute(new Runnable() {
            @Override
            public void run() {
                if (bleBluetooth != null) {
                    try {
                        startmsTime = SystemClock.elapsedRealtime();
                        bleBluetooth.startBLEScan(iBeaconManager.this, SCAN_PERIOD, TIMEOUT);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    @Override
    public void onBlueThoodException() {
        mhandler.sendEmptyMessage(MSG_EXCEPTION);
    }

    /**
     *
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void stopScan() {
        if (bleBluetooth != null) {
            try {
                bleBluetooth.stopScan(false);
                scanConsumeTime = SystemClock.elapsedRealtime() - startmsTime;
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
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
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

    @Override
    public void onIbeaconHadDetect(BluetoothDevice device, ScanResult scanResult) {
        if (listener != null)
            listener.onIbeaconHadDetect(device, scanResult);
    }

    @Override
    public void onStatusChange(GattStatusEnum statusEnum, String... attr) {
        if (listener != null)
            listener.onStatusChange(statusEnum);
        switch (statusEnum) {
            case GATT_STATUS_DISCONNECTTING:
                XLog.d(TAG, "断开连接中...");
                break;
            case GATT_STATUS_DISCONNECTED:
                XLog.d(TAG, "连接断开");
                break;
            case GATT_STATUS_CONNECTTING:
                XLog.d(TAG, "连接中...");
                break;
            case GATT_STATUS_CONNECTED:
                XLog.d(TAG, "已连接上");
                break;
            case GATT_STATUS_SERVICE_DISCOVERING:
                XLog.d(TAG, "发现服务中...");
                break;
            case GATT_STATUS_SERVICE_DISCOVERED:
                XLog.d(TAG, "服务被发现");
                break;
            case GATT_STATUS_SENDDING_DATA:
                XLog.d(TAG, "发送数据中...");
                break;
            case GATT_STATUS_SENDDATA_SUCCESS:
                XLog.d(TAG, "发送数据成功");
                break;
            case GATT_STATUS_SENDDATA_FAILED:
                XLog.d(TAG, "发送数据失败");
                break;
            case GATT_STATUS_WAITTING_NOTIFY:
                XLog.d(TAG, "等待通知中...");
                break;
            case GATT_STATUS_NOTIFY_SUCCESS:
                XLog.d(TAG, "开门成功！");
                saveStatisticalData(OpenResult.result_success);
                break;
            case GATT_STATUS_NOTIFY_FAILED:
                XLog.d(TAG, "开门失败！");
                saveStatisticalData(result_failed, attr);
                break;
        }
    }

    @Override
    public void onRssiCallback(double distance, int rssi) {
        if (listener != null)
            listener.onRssiCallback(distance, rssi);
        rssiCount++;
        rssiTotal += rssi;
    }

    @Override
    public void timeout() {
        if (listener != null)
            listener.timeout();
        XLog.d(TAG, "开锁超时失败");
        saveStatisticalData(OpenResult.result_timeout);
    }

    /**
     * 耗时操作
     *
     * @param result
     * @param attr
     */
    private void saveStatisticalData(final OpenResult result, final String... attr) {
        XLog.d(TAG, "saveStatisticalData() called ");
        new Thread(new Runnable() {
            @Override
            public void run() {
                BleOpenRecordBean bean = new BleOpenRecordBean();
                long openConsumeTime = SystemClock.elapsedRealtime() - startmsTime;
                switch (result) {
                    case result_success:
                        bean.openResult = "success";
                        bean.reason = "result_success";
                        break;
                    case result_failed:
                        bean.openResult = "failed";
                        bean.reason = attr[0];
                        break;
                    case result_timeout:
                        bean.openResult = "timeout";
                        bean.reason = "result_timeout";
                        break;
                }
                bean.scanTime = scanConsumeTime / 1000.0f;
                bean.RSSI = rssiCount != 0 ? rssiTotal / rssiCount : -1;
                Date date = new Date();
                DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.sss");
                bean.currentDate = format.format(date).toString();
                bean.openConsumeTime = openConsumeTime / 1000.0f;
                bean.certificateIndex = -1;
                bean.deviceAddress = "none";
                bean.deviceId = "none";
                bean.userId = "测试";
                int id = BleOpenRecordBuss.insertRow(context, bean);
                if (id % 20 == 0) {
                    //批量统计
                    List<BleOpenRecordBean> bleOpenRecordBeen = BussHelper.queryAll(context, BleOpenRecordBean.class, BleOpenRecordBuss.tableName);
                    int successCount = 0, failedCount = 0, timeoutCount = 0;
                    float totalOpenConsumeTime = 0, totalRSSI = 0;
                    for (BleOpenRecordBean recordBean : bleOpenRecordBeen) {
                        switch (recordBean.openResult) {
                            case "success":
                                successCount++;
                                totalOpenConsumeTime += recordBean.openConsumeTime;
                                totalRSSI += recordBean.RSSI;
                                break;
                            case "failed":
                                failedCount++;
                                break;
                            case "timeout":
                                timeoutCount++;
                                break;
                        }
                    }
                    StatisticalBean statisticalBean = new StatisticalBean();
                    statisticalBean.successRate = successCount * 1.0f / bleOpenRecordBeen.size() * 100.0f;
                    statisticalBean.totalCount = bleOpenRecordBeen.size();
                    statisticalBean.timeoutCount = timeoutCount;
                    statisticalBean.successCount = successCount;
                    statisticalBean.failedCount = failedCount;
                    statisticalBean.averageOpenTime = totalOpenConsumeTime * 1.0f / successCount;
                    statisticalBean.averageRSSI = (int) (totalRSSI / successCount);
                    statisticalBean.currentDate = format.format(date).toString();
                    statisticalBean.deviceAddress = "none";
                    statisticalBean.deviceId = "none";
                    StatisticalBuss.insertRow(context, statisticalBean);
                }
            }
        }).start();
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
        GATT_STATUS_WAITTING_NOTIFY,
        GATT_STATUS_NOTIFY_SUCCESS,
        GATT_STATUS_NOTIFY_FAILED,
    }

    public enum OpenResult {
        result_success,
        result_failed,
        result_timeout
    }
}
