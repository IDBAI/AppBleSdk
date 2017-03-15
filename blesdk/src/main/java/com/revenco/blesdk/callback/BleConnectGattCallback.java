package com.revenco.blesdk.callback;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;

import com.revenco.blesdk.bean.TransData;
import com.revenco.blesdk.core.Config;
import com.revenco.blesdk.core.DataHelper;
import com.revenco.blesdk.core.GattOperations;
import com.revenco.blesdk.core.GattStatusMachine;
import com.revenco.blesdk.core.NotifyHelper;
import com.revenco.blesdk.core.iBeaconManager.GattStatusEnum;
import com.revenco.blesdk.exception.ConnectException;
import com.revenco.blesdk.exception.GattException;
import com.revenco.blesdk.interfaces.bleCharacterCallback;
import com.revenco.blesdk.interfaces.oniBeaconStatusListener;
import com.revenco.blesdk.utils.Constants;
import com.revenco.blesdk.utils.ConvertUtil;
import com.revenco.blesdk.utils.MySharedPreferences;
import com.revenco.blesdk.utils.XLog;

import java.util.List;
import java.util.UUID;

import static com.revenco.blesdk.core.iBeaconManager.GattStatusEnum.GATT_STATUS_CONNECTED;
import static com.revenco.blesdk.core.iBeaconManager.GattStatusEnum.GATT_STATUS_CONNECTTING;
import static com.revenco.blesdk.core.iBeaconManager.GattStatusEnum.GATT_STATUS_DISCONNECTED;
import static com.revenco.blesdk.core.iBeaconManager.GattStatusEnum.GATT_STATUS_DISCONNECTTING;
import static com.revenco.blesdk.core.iBeaconManager.GattStatusEnum.GATT_STATUS_SENDDATA_FAILED;
import static com.revenco.blesdk.core.iBeaconManager.GattStatusEnum.GATT_STATUS_SENDDATA_SUCCESS;
import static com.revenco.blesdk.core.iBeaconManager.GattStatusEnum.GATT_STATUS_SERVICE_DISCOVERED;
import static com.revenco.blesdk.core.iBeaconManager.GattStatusEnum.GATT_STATUS_SERVICE_DISCOVERING;

/**
 * Created by Administrator on 2016/11/11.
 * 此回调方法在子线程中运行
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class BleConnectGattCallback extends BaseBleGattCallback implements bleCharacterCallback {
    private static final String TAG = "BleConnectGattCallback";
    //
    private static final int MSG_WRITE_CHARACTERISTIC = 100;
    private static final int MSG_WRITE_CHARACTERISTIC_BYTE = 101;
    private static final int MSG_WRITE_DESCRIPTOR = 109;
    private static final int MSG_READ_RSSI = 102;
    private static final int MSG_CLOSE_GATT_CONNECT = 103;
    private static final int MSG_GATT_RECONNECT = 104;
    private static final int MSG_SERVICE_DISCOVER = 105;
    private static final int MSG_NEW_GATT_RECONNECT = 107;
    private static final int MSG_COMMING_TIMEOUT = 108;
    private static final int MSG_BELOW_LOLLIPOP_RECONNECT_LAST_DEVICE = 200;
    //
    private static final int RETRY_READ_RSSI_MAX = 3;//读远程RSSI，最大重试次数
    private static final int RETRY_CONNECT_MAX = 1;//connectGatt 直接重连，可尝试最大次数
    private static final int RETRY_NEW_GATT_CONNECT_MAX = 6;//新建Gatt 重连，可尝试最大次数
    private static final int LOGIC_FAILED_RETRY_MAX = 3;//写入数据逻辑失败，可尝试最大次数
    private static final int RETRY_DISCOVER_SERVICE_MAX = 3;//发现服务超时，最大可尝试次数
    //
    private static final long SLEEP_TIME_BEFORE_RECONNECT = 10L;//断开连接与再次连接之间间隔时间
    private static final long READ_RSSI_INTEVAL = 300L;//读取RSSI时间间隔
    private static final int RE_DISCOVERED_SERVICE_MAX = 3;
    private static final long DISCONNECT_INVAL = 500L;//断开间隔
    private static final int RE_DISCOVER_SERVICE_MAX = 3;//connectGatt.discoverServices() 为false时候，重试最大次数
    public static GattStatusEnum currentGattStatus = GATT_STATUS_DISCONNECTED;
    public static boolean isReceiveNotify = false;
    public static boolean isFinishSendData = false;
    //写队列
    public static volatile boolean isWritting = false;
    //
    private static long GATT_FIRST_CONNECT_TIMEOUT = 1800L;//首次进来连接超时
    private static long GATT_RECONNECT_TIMEOUT = 1000L;//connectGatt 直接重连超时
    private static long NEW_GATT_CONNECT_TIMEOUT = 1500L;//新建Gatt 重连超时
    private static long WAIT_SERVICE_DISCOVER_TIMEOUT = 1000L;//连接成功 超时没有回调发现服务 ->重新发现服务,最多 RETRY_DISCOVER_SERVICE_MAX 次 -> 断开重连
    private static long WAIT_DISCONNECT_TIMEOUT = 1000L;//connectGatt.disConnect 回调 onConnectFailure 超时
    private int reDiscoveredServiceTime = 0;
    private oniBeaconStatusListener listener;
    private BluetoothDevice device;
    private int retry_readRssi;
    private int retry_gatt_reconnect;
    private int retry_newgatt_connect;
    private int retry_logic_fail;
    private int retry_discovered_service;
    private Context context;
    private Handler mHandler;
    //当前写的对象
//    private Object currentWriteObject;
    private BluetoothGatt connectGatt;
    private boolean isTimeout = false;

    public oniBeaconStatusListener getListener() {
        return listener;
    }

    public void init(Context context, BluetoothDevice device, oniBeaconStatusListener listener) {
        XLog.d(TAG, "init() called with: context = [" + context + "], listener = [" + listener + "]");
        this.context = context;
        this.listener = listener;
        this.device = device;
        isTimeout = false;
        //create handler in thread.
        inithandler();
        resetCounter();
        resetWriting();
        initparams();
        //初始化
        DataHelper.getInstance().initUUIDMark();
    }

    private void initparams() {
        String manufacturer = Build.MANUFACTURER;
        if (manufacturer.equals("samsung")) {//三星
            XLog.d(TAG, "针对三星修改了超时配置");
            GATT_FIRST_CONNECT_TIMEOUT = 3000L;//首次进来连接超时
            GATT_RECONNECT_TIMEOUT = 2000L;//connectGatt 直接重连超时
            NEW_GATT_CONNECT_TIMEOUT = 2000L;//新建Gatt 重连超时
            WAIT_SERVICE_DISCOVER_TIMEOUT = 2500L;//连接成功 超时没有回调发现服务 ->重新发现服务,最多 RETRY_DISCOVER_SERVICE_MAX 次 -> 断开重连
            WAIT_DISCONNECT_TIMEOUT = 1500L;//connectGatt.disConnect 回调 onConnectFailure 超时
        } else {//默认配置
        }
    }

    /**
     * 新设备是否能够进入,入口
     *
     * @return
     */
    public boolean canNewDeviceComeiin() {
        reDiscoveredServiceTime = 0;
        isReceiveNotify = false;
        isFinishSendData = false;
        XLog.d(TAG, "canNewDeviceComeiin() called");
        currentGattStatus = GATT_STATUS_DISCONNECTED;//reset
        boolean connectting = GattStatusMachine.publicMachineStatus(listener, GATT_STATUS_CONNECTTING);
        if (connectting) {
            startFirstTimeConnectTimeout();
        }
        return connectting;
    }

    private void startFirstTimeConnectTimeout() {
        mHandler.removeMessages(MSG_COMMING_TIMEOUT);
        Message msg = mHandler.obtainMessage();
        TransData data = new TransData();
        msg.obj = data;
        msg.what = MSG_COMMING_TIMEOUT;
        mHandler.sendMessageDelayed(msg, GATT_FIRST_CONNECT_TIMEOUT);
    }

    private void inithandler() {
        //inner create looper and message queue add to the current thread
        HandlerThread thread = new HandlerThread("timeoutlooper");
        thread.start();
        mHandler = new Handler(thread.getLooper(), new Handler.Callback() {
            private BluetoothGatt firstConnectGatt;
            private TransData transData;

            @Override
            public synchronized boolean handleMessage(Message msg) {
                XLog.d(TAG, "handleMessage() called with: msg = [" + msg + "]" + "\n" +
                        "Thread name = " + Thread.currentThread().getName() + "\n" +
                        "Thread id = " + Thread.currentThread().getId());
                transData = (TransData) msg.obj;
                switch (msg.what) {
                    case MSG_READ_RSSI:
                        if (transData == null) {
                            XLog.d(TAG, "mHandler -> transData is null.");
                            break;
                        }
                        if (connectGatt == null) {
                            XLog.d(TAG, "mHandler -> connectGatt is null.");
                            break;
                        }
                        boolean readRemoteRssi = connectGatt.readRemoteRssi();
                        XLog.d(TAG, "readRemoteRssi  = " + readRemoteRssi);
                        if (!readRemoteRssi) {//容错机制，读取失败最多重试3次，否则认为ble异常
                            if (retry_readRssi < RETRY_READ_RSSI_MAX) {
                                retry_readRssi++;
                                XLog.d(TAG, "start Read RSSI retry_readRssi = " + retry_readRssi);
                                startReadRssi();
                            } else {
                                retry_readRssi = 0;
                                XLog.d(TAG, "retry_readRssi = 0;");
                                XLog.d(TAG, "read Remote RSSI 3 times ,ble is maybe exception,i will disConnect ble.");
                                initiativeDisConnect();
                            }
                        } else {
                            retry_readRssi = 0;
                            XLog.d(TAG, "retry_readRssi = 0;");
                            startReadRssi();
                        }
                        break;
                    case MSG_CLOSE_GATT_CONNECT://主动断开连接超时
                        timeoutFailure(true);
                        break;
                    case MSG_SERVICE_DISCOVER://连接成功 1.5秒没有回调发现服务 -> 断开重连
                        if (retry_discovered_service < RETRY_DISCOVER_SERVICE_MAX) {
                            retry_discovered_service++;
                            XLog.d(TAG, "retry_discovered_service = " + retry_discovered_service);
                            if (firstConnectGatt != null) {
                                XLog.d(TAG, "发现服务超时，重试！");
                                boolean discoverServices = firstConnectGatt.discoverServices();
                                //当discoverServices为false的时候重试3次
                                retryDiscoverServicesIfFalse(discoverServices);
                                XLog.d(TAG, "discoverServices = " + discoverServices);
                            }
                        } else {
                            retry_discovered_service = 0;
                            XLog.d(TAG, "retry_discovered_service = " + retry_discovered_service);
                            if (isLegalTimeOut(msg.what))
                                timeoutFailure(true);
                        }
                        break;
                    case MSG_GATT_RECONNECT://connectGatt 重连超时
                        if (isLegalTimeOut(msg.what))
                            timeoutFailure(true);
                        break;
                    case MSG_NEW_GATT_RECONNECT://new connectGatt 重连超时
                        if (isLegalTimeOut(msg.what))
                            timeoutFailure(false);
                        break;
                    case MSG_BELOW_LOLLIPOP_RECONNECT_LAST_DEVICE:
                        if (transData == null) {
                            XLog.d(TAG, "mHandler -> transData is null.");
                            break;
                        }
                        XLog.d(TAG, "below 5.0 OS ,reConnect last Device Timeout.");
                        //超时
                        finallySetTimeout();
                        break;
                    case MSG_COMMING_TIMEOUT://首次进来连接超时
//                        if (connectGatt != null) {
//                            if (publicMachineStatus( GATT_STATUS_DISCONNECTTING))
//                                onConnectFailure(new ConnectException(connectGatt, 0));
//                        } else {
//                            XLog.e(TAG, "首次进来连接超时，connectGatt 为null,直接开锁超时。");
//                            publicMachineStatus( GATT_STATUS_DISCONNECTED);
//                            reMoveAllMsgForTimeout();
//                            if (listener != null)
//                                listener.timeout();
//                        }
                        timeoutFailure(false);
                        break;
                }
                return false;
            }
        });
    }

    /**
     * 判断是否是合法的timeout，每个timeout 应该与其发出的msg对应
     * 解释：由于mhandle和ble回调方法处于不同的子线程，在多线程环境下，很难保障事件流的逻辑正确性，所以需要将timeout
     * 与其发出的msg一一对应起来，才认为此timeout是合法的。
     * 非法timeout情况：
     *
     * @param what
     * @return
     */
    private boolean isLegalTimeOut(int what) {
        XLog.d(TAG, "isLegalTimeOut() called with: what = [" + what + "]");
        boolean isLegal = true;
        GattStatusEnum currentGattStatus = getCurrentGattStatus();
        switch (what) {
            case MSG_GATT_RECONNECT://connectGatt 重连超时
            case MSG_NEW_GATT_RECONNECT://new connectGatt 重连超时
                if (currentGattStatus == GATT_STATUS_CONNECTTING)
                    isLegal = true;
                else
                    isLegal = false;
                break;
            case MSG_SERVICE_DISCOVER://连接成功 0.5秒没有回调发现服务 -> 断开重连
                if (currentGattStatus == GATT_STATUS_SERVICE_DISCOVERING)
                    isLegal = true;
                else
                    isLegal = false;
                break;
        }
        XLog.d(TAG, "isLegalTimeOut() - > isLegal =" + isLegal);
        return isLegal;
    }

    public synchronized GattStatusEnum getCurrentGattStatus() {
        return currentGattStatus;
    }

    @Override
    public synchronized void onConnectionStateChange(BluetoothGatt gatt1, int status, int newState) {
        super.onConnectionStateChange(gatt1, status, newState);
        XLog.d(TAG, "onConnectionStateChange() called with: connectGatt = [" + connectGatt + "], status = [" + status + "], newState = [" + newState + "]");
        resetWriting();
        mHandler.removeMessages(MSG_COMMING_TIMEOUT);//首次进入连接超时，失败与否都要移除
        mHandler.removeMessages(MSG_BELOW_LOLLIPOP_RECONNECT_LAST_DEVICE);//移除5.0以下系统重新连接上一个的消息,成功失败都需要移除
        if (newState == BluetoothGatt.STATE_CONNECTED) {
            onConnectSuccess(connectGatt, status, newState);
        } else if (newState == BluetoothGatt.STATE_DISCONNECTED) {
            onConnectFailure(new ConnectException(connectGatt, newState));
        }
    }

    @Override
    public synchronized void onConnectSuccess(BluetoothGatt gatt, int status, int newState) {
        if (!GattStatusMachine.publicMachineStatus(listener, GATT_STATUS_CONNECTED))
            return;
        XLog.d(TAG, "onConnectSuccess() called with: connectGatt = [" + connectGatt + "], status = [" + status + "], newState = [" + newState + "]");
        if (connectGatt != null) {
            mHandler.removeMessages(MSG_GATT_RECONNECT);//移除连接超时消息
            mHandler.removeMessages(MSG_NEW_GATT_RECONNECT);//移除new connectGatt 连接超时消息
            connectGatt.readRemoteRssi();//即时读取RSSI
            startReadRssi();
            if (!GattStatusMachine.publicMachineStatus(listener, GATT_STATUS_SERVICE_DISCOVERING))
                return;
            boolean discoverServices = connectGatt.discoverServices();//将回调 onServicesDiscovered()
            //当discoverServices为false的时候重试3次
            retryDiscoverServicesIfFalse(discoverServices);
            XLog.d(TAG, "discoverServices = " + discoverServices);
            startWaitServiceDiscoverTimout();
        }
    }

    @Override
    public synchronized void onReadRemoteRssi(BluetoothGatt gatt1, int rssi, int status) {
        super.onReadRemoteRssi(gatt1, rssi, status);
        if (rssi > 0)//127 is exception
            return;
        XLog.d(TAG, "onReadRemoteRssi() called with: connectGatt = [" + connectGatt + "], rssi = [" + rssi + "], status = [" + status + "]");
        if (listener != null) {
            Double distance = -1.0;
            listener.onRssiCallback(distance, rssi);
        }
    }

    private void resetCounter() {
        XLog.d(TAG, "resetCounter() called");
        retry_gatt_reconnect = 0;
        retry_newgatt_connect = 0;
        retry_discovered_service = 0;
        XLog.d(TAG, "retry_gatt_reconnect = 0");
        XLog.d(TAG, "retry_newgatt_connect = 0");
        XLog.d(TAG, "retry_discovered_service = 0");
    }

    /**
     * 等待服务发现超时
     */
    private synchronized void startWaitServiceDiscoverTimout() {
        mHandler.removeMessages(MSG_SERVICE_DISCOVER);
        Message msg = mHandler.obtainMessage();
        TransData data = new TransData(connectGatt);
        msg.obj = data;
        msg.what = MSG_SERVICE_DISCOVER;
        mHandler.sendMessageDelayed(msg, WAIT_SERVICE_DISCOVER_TIMEOUT);
    }

    private synchronized void startReadRssi() {
        //TODO 临时屏蔽
//        XLog.d(TAG, "startReadRssi() called with: connectGatt = [" + connectGatt + "]");
//        if (mHandler != null) {
//            mHandler.removeMessages(MSG_READ_RSSI);
//            Message msg = mHandler.obtainMessage();
//            TransData data = new TransData(connectGatt);
//            msg.obj = data;
//            msg.what = MSG_READ_RSSI;
//            mHandler.sendMessageDelayed(msg, READ_RSSI_INTEVAL);
//        } else {
//            XLog.d(TAG, "mHandler is null.");
//        }
    }

    private synchronized void stopReadRssi() {
        XLog.d(TAG, "stopReadRssi() called");
        mHandler.removeMessages(MSG_READ_RSSI);
    }

    @Override
    public synchronized void onConnectFailure(ConnectException exception) {
        XLog.d(TAG, "onConnectFailure() called with: exception = [" + exception + "]");
        //优先判断超时的处理
        if (isTimeout) {
            timoutMethod();
            return;
        }
        mHandler.removeMessages(MSG_CLOSE_GATT_CONNECT);//移除关闭gatt连接的消息
        if (!GattStatusMachine.publicMachineStatus(listener, GATT_STATUS_DISCONNECTED)) {
            XLog.d(TAG, "connectGatt.close();");
            connectGatt.close();
            return;
        }
        //停止rssi读取
        stopReadRssi();
        //如果可以的话，先Reconnect，直到超过最大次数
        if (retry_gatt_reconnect < RETRY_CONNECT_MAX) {
            if (isFinishSendData) {
                XLog.d(TAG, "数据发送完成!已经断开了连接，不判断 isReceiveNotify，connectGatt.close();");
                connectGatt.close();
            } else {
                XLog.d(TAG, "数据未发送完成，onConnectFailure ->ReConnectGatt()");
                ReConnectGatt();
            }
        } else {
            //reconnect超过最大次数，尝试new 出新的gatt对象。
            if (connectGatt != null) {
                XLog.d(TAG, "onConnectFailure  -> connectGatt.close()");
                connectGatt.close();
            }
            if (!isFinishSendData) {//数据未发送完成
                XLog.d(TAG, "onConnectFailure ->newGattReConnect()");
                newGattReConnect();
            }
        }
    }

    private void timoutMethod() {
        XLog.d("timeout", "timoutMethod() called.");
        reMoveAllMsgForTimeout();
        if (connectGatt != null) {
            XLog.d(TAG, "timeout  -> connectGatt.close()");
            XLog.d("timeout", "timeout  -> connectGatt.close()");
            connectGatt.close();
        }
        if (listener != null)
            listener.timeout();
        GattStatusMachine.publicMachineStatus(listener, GATT_STATUS_DISCONNECTED);
    }

    /**
     * 超时失败，需要调用disconnect
     *
     * @param isNeedDisconnect
     */
    public synchronized void timeoutFailure(boolean isNeedDisconnect) {
        XLog.d(TAG, "timeoutFailure() called with: isNeedDisconnect = [" + isNeedDisconnect + "]");
        if (!GattStatusMachine.publicMachineStatus(listener, GATT_STATUS_DISCONNECTTING))
            return;
        if (!GattStatusMachine.publicMachineStatus(listener, GATT_STATUS_DISCONNECTED))
            return;
        mHandler.removeMessages(MSG_CLOSE_GATT_CONNECT);//移除关闭gatt连接的消息
        ReConnectGatt();
    }

    /**
     * 超时重新连接，请勿close，新建对象之前必须要close上一个gatt以释放资源
     * * connect 对应 disconnect ,
     * close 对应  device.connectGatt(context, true, this) 新建对象
     * 重连方案
     */
    private void ReConnectGatt() {
        XLog.d(TAG, "ReConnectGatt() called");
        if (retry_gatt_reconnect < RETRY_CONNECT_MAX) {
            retry_gatt_reconnect++;
            //方案1 为了省电，直接再次连接
            if (connectGatt != null) {
                mHandler.removeMessages(MSG_GATT_RECONNECT);
                try {
                    XLog.d(TAG, "----start sleep " + SLEEP_TIME_BEFORE_RECONNECT + " ms before connectGatt.connect()----");
                    Thread.sleep(SLEEP_TIME_BEFORE_RECONNECT);
                    XLog.d(TAG, "----end sleep " + SLEEP_TIME_BEFORE_RECONNECT + " ms before connectGatt.connect()----");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (!GattStatusMachine.publicMachineStatus(listener, GATT_STATUS_CONNECTTING))
                    return;
                boolean connect = connectGatt.connect();//
                XLog.d(TAG, "onConnectFailure -> reconnect = " + connect);
                XLog.d(TAG, "retry_gatt_reconnect = " + retry_gatt_reconnect);
                startGattReConnectTimout(connectGatt);
            } else {
                XLog.d(TAG, "connectGatt is null,make new Gatt.");
                retry_gatt_reconnect = RETRY_CONNECT_MAX;//make max
                newGattReConnect();
            }
        } else {
            XLog.d(TAG, "retry_gatt_reconnect 超过最大重试次数");
            if (connectGatt != null) {
                XLog.d(TAG, "newGattReConnect() 之前 connectGatt.close();");
                connectGatt.close();
            }
            newGattReConnect();
        }
    }

    /**
     * connect 对应 disconnect ,
     * close 对应  device.connectGatt(context, true, this) 新建对象
     * 新建对象重连方案
     */
    private void newGattReConnect() {
        XLog.d(TAG, "newGattReConnect() called");
        if (retry_newgatt_connect < RETRY_NEW_GATT_CONNECT_MAX) {
            //方案2， 尝试直接新建对象重连3次，
            retry_newgatt_connect++;
            if (device != null) {
                mHandler.removeMessages(MSG_NEW_GATT_RECONNECT);
                try {
                    XLog.d(TAG, "----start sleep " + SLEEP_TIME_BEFORE_RECONNECT + " ms before device.connectGatt(context, true, this);----");
                    Thread.sleep(SLEEP_TIME_BEFORE_RECONNECT);
                    XLog.d(TAG, "----end sleep " + SLEEP_TIME_BEFORE_RECONNECT + " ms before device.connectGatt(context, true, this);----");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (!GattStatusMachine.publicMachineStatus(listener, GATT_STATUS_CONNECTTING))
                    return;
                connectGatt = device.connectGatt(context, true, this);
                XLog.d(TAG, "onConnectFailure -> device.connectGatt(context, true, this)");
                XLog.d(TAG, "retry_newgatt_connect = " + retry_newgatt_connect);
                startNewGattReConnectTimeout(connectGatt);
            }
        } else {
            finallySetTimeout();
        }
    }

    /**
     * 最终超时结束一个回合
     */
    private void finallySetTimeout() {
        XLog.d(TAG, "finallySetTimeout() called with: connectGatt = [" + connectGatt + "]");
        resetCounter();
        //方案3  方案1,2 各重试3次失败后，彻底超时
        if (connectGatt != null) {//此处释放资源
            XLog.d(TAG, "finallySetTimeout() -> connectGatt.disconnect()");
            connectGatt.disconnect();
            try {
                Thread.sleep(400);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            XLog.d(TAG, "finallySetTimeout() -> connectGatt.close()");
            connectGatt.close();
        }
        reMoveAllMsgForTimeout();
        if (listener != null)
            listener.timeout();
    }

    private void reMoveAllMsgForTimeout() {
        if (mHandler != null) {
            XLog.d(TAG, "reMoveAllMsgForTimeout() called");
            mHandler.removeMessages(MSG_WRITE_CHARACTERISTIC);
            mHandler.removeMessages(MSG_WRITE_CHARACTERISTIC_BYTE);
            mHandler.removeMessages(MSG_WRITE_DESCRIPTOR);
            mHandler.removeMessages(MSG_READ_RSSI);
            mHandler.removeMessages(MSG_CLOSE_GATT_CONNECT);
            mHandler.removeMessages(MSG_GATT_RECONNECT);
            mHandler.removeMessages(MSG_SERVICE_DISCOVER);
            mHandler.removeMessages(MSG_NEW_GATT_RECONNECT);
            mHandler.removeMessages(MSG_BELOW_LOLLIPOP_RECONNECT_LAST_DEVICE);
            mHandler.removeMessages(MSG_COMMING_TIMEOUT);
        }
    }

    private synchronized void startNewGattReConnectTimeout(BluetoothGatt newGatt) {
        XLog.d(TAG, "startNewGattReConnectTimeout() called with: newGatt = [" + newGatt + "]");
        mHandler.removeMessages(MSG_NEW_GATT_RECONNECT);
        Message msg = mHandler.obtainMessage();
        TransData data = new TransData(newGatt);
        msg.obj = data;
        msg.what = MSG_NEW_GATT_RECONNECT;
        mHandler.sendMessageDelayed(msg, NEW_GATT_CONNECT_TIMEOUT);
    }

    /**
     * @param firstConnectGatt
     */
    private synchronized void startGattReConnectTimout(BluetoothGatt firstConnectGatt) {
        XLog.d(TAG, "startGattReConnectTimout() called with: connectGatt = [" + firstConnectGatt + "]");
        mHandler.removeMessages(MSG_GATT_RECONNECT);
        Message msg = mHandler.obtainMessage();
        TransData data = new TransData(firstConnectGatt);
        msg.obj = data;
        msg.what = MSG_GATT_RECONNECT;
        mHandler.sendMessageDelayed(msg, GATT_RECONNECT_TIMEOUT);
    }

    @Override
    public synchronized void onServicesDiscovered(BluetoothGatt gatt1, int status) {
        if (!GattStatusMachine.publicMachineStatus(listener, GATT_STATUS_SERVICE_DISCOVERED))
            return;
        mHandler.removeMessages(MSG_SERVICE_DISCOVER);//移除服务超时消息
        mHandler.removeMessages(MSG_GATT_RECONNECT);//移除连接超时消息
        mHandler.removeMessages(MSG_NEW_GATT_RECONNECT);//移除new connectGatt 连接超时消息
        resetCounter();
        resetWriting();
        XLog.d(TAG, "onServicesDiscovered() called with: connectGatt = [" + connectGatt + "], status = [" + status + "]");
        if (connectGatt != null) {
            String write_uuid = MySharedPreferences.getStringPreference(context, Constants.TAG_Write_UUID);
            String notify_uuid = MySharedPreferences.getStringPreference(context, Constants.TAG_Notify_UUID);
            List<BluetoothGattService> services = connectGatt.getServices();
            if (services.isEmpty()) {
                XLog.d(TAG, "services is empty.");
                emptyReDiscoveredService();
            } else {
                //先设置notify
                startNotify(true);
                //填充数据
                DataHelper.getInstance().fillWriteQueue();
            }
        }
    }

    /**
     * 重新发现服务
     */
    private synchronized void emptyReDiscoveredService() {
        XLog.d(TAG, "emptyReDiscoveredService() called");
        if (!GattStatusMachine.publicMachineStatus(listener, GATT_STATUS_SERVICE_DISCOVERING))
            return;
        XLog.d(TAG, "reDiscoveredServiceTime = " + reDiscoveredServiceTime);
        if (reDiscoveredServiceTime > RE_DISCOVERED_SERVICE_MAX) {
            XLog.d(TAG, "重新发现服务重试超过最大次数，断开连接。");
            GattStatusMachine.publicMachineStatus(listener, GATT_STATUS_DISCONNECTED);
            finallySetTimeout();
        } else {
            if (connectGatt != null) {
                reDiscoveredServiceTime++;
                XLog.d(TAG, "reDiscoveredServiceTime = " + reDiscoveredServiceTime);
                boolean discoverServices = connectGatt.discoverServices();//将回调 onServicesDiscovered(),有时偶发不回调
                XLog.d(TAG, "discoverServices = " + discoverServices);
                //当discoverServices为false的时候重试3次
                retryDiscoverServicesIfFalse(discoverServices);
                startWaitServiceDiscoverTimout();
            }
        }
    }

    /**
     * 当discoverServices为false的时候重试3次
     *
     * @param discoverServices
     */
    private void retryDiscoverServicesIfFalse(boolean discoverServices) {
        XLog.d(TAG, "retryDiscoverServicesIfFalse() called with: connectGatt = [" + connectGatt + "], discoverServices = [" + discoverServices + "]");
        int i = 0;
        while (i < RE_DISCOVER_SERVICE_MAX) {
            XLog.d(TAG, "i = " + i);
            XLog.d(TAG, "discoverServices = " + discoverServices);
            if (!discoverServices) {
                discoverServices = connectGatt.discoverServices();
            } else
                break;
            i++;
        }
        if (i == 3 && !discoverServices) {
            XLog.d(TAG, "发现服务重试3次都失败，调用firstConnectGatt.disconnect();");
            initiativeDisConnect();
        }
    }

    /**
     * 复位writing 为false
     */
    private synchronized void resetWriting() {
        XLog.d(TAG, "resetWriting() called");
        isWritting = false;
    }

    /**
     * //    Without setting this descriptor, you never receive updates to a characteristic.
     * //    Calling setCharacteristicNotification is not enough. This is a common mistake.
     */
    private synchronized void startNotify(boolean enable) {
        XLog.d(TAG, "startNotify() called with: enable = [" + enable + "]");
        BluetoothGattService service = CallbackConnectHelper.getInstance().getBluetoothGattService(connectGatt);
        if (service == null) {
            XLog.e(TAG, "    service is null");
            return;
        }
        BluetoothGattCharacteristic characteristic = service.getCharacteristic(UUID.fromString(Config.NOTIFY_UUID));
        if (characteristic == null) {
            XLog.e(TAG, "   characteristic is null");
            return;
        }
        List<BluetoothGattDescriptor> descriptorList = characteristic.getDescriptors();
        if (descriptorList != null && descriptorList.size() > 0) {
            for (BluetoothGattDescriptor desc : descriptorList)
                NotifyHelper.getInstance().fillmap(desc);
            //
            XLog.d(TAG, "mapSize = " + NotifyHelper.getInstance().getnotifyuuidmapSize());
            NotifyHelper.getInstance().setNextNotify(listener, connectGatt);
        } else {
            XLog.e(TAG, "!(descriptorList != null && descriptorList.size() > 0)");
        }
    }

    @Override
    public synchronized void onCharacteristicRead(BluetoothGatt gatt1, BluetoothGattCharacteristic characteristic, int status) {
        super.onCharacteristicRead(gatt1, characteristic, status);
        XLog.d(TAG, "onCharacteristicRead() called with: connectGatt = [" + connectGatt + "], characteristic = [" + characteristic + "], status = [" + status + "]");
    }

    @Override
    public synchronized void onCharacteristicWrite(BluetoothGatt gatt1, BluetoothGattCharacteristic characteristic, int status) {
        super.onCharacteristicWrite(gatt1, characteristic, status);
        resetWriting();
        mHandler.removeMessages(MSG_WRITE_CHARACTERISTIC);
        mHandler.removeMessages(MSG_WRITE_CHARACTERISTIC_BYTE);
        XLog.d(TAG, "Thread name = " + Thread.currentThread().getName());
        XLog.d(TAG, "Thread id = " + Thread.currentThread().getId());
        XLog.d(TAG, "onCharacteristicWrite() called with: connectGatt = [" + connectGatt + "], characteristic = [" + characteristic + "], status = [" + status + "]");
        String desUuid = characteristic.getUuid().toString();
        String hexStr = ConvertUtil.byte2HexStr(characteristic.getValue());
        XLog.d("GOOD", "onCharacteristicWrite() -> desUuid = " + desUuid + "\n" +
                " hexStr = " + hexStr);
        if (status == BluetoothGatt.GATT_SUCCESS) {
            retry_logic_fail = 0;
            XLog.d(TAG, "onCharacteristicWrite succeed and set retry_logic_fail = 0.");
            XLog.d(TAG, "CharacteristicUuid =" + characteristic.getUuid() + "  -> startWrite data success .");
            onWriteDataSuccess(connectGatt, characteristic);
            DataHelper.getInstance().setUUIDHasWrited(desUuid.toUpperCase());
            DataHelper.getInstance().nextWrite(connectGatt);
        } else {
            //写入失败，重试3次
            if (retry_logic_fail < LOGIC_FAILED_RETRY_MAX) {
                retry_logic_fail++;
                XLog.d(TAG, "onCharacteristicWrite logic failed,and retry it the " + retry_logic_fail + " times.");
                DataHelper.getInstance().doWrite(connectGatt, characteristic);
            } else
                onWriteDataFailure(new GattException(connectGatt, status));
        }
    }

    @Override
    public synchronized void onCharacteristicChanged(BluetoothGatt gatt1, BluetoothGattCharacteristic characteristic) {
        super.onCharacteristicChanged(gatt1, characteristic);
        XLog.d(TAG, "onCharacteristicChanged() called with: connectGatt = [" + connectGatt + "], characteristic = [" + characteristic + "]");
        byte[] value = characteristic.getValue();
        if (value != null) {
            String hexStr = ConvertUtil.byte2HexStr(value);
            String string = "onCharacteristicChanged -> charUuid = " + characteristic.getUuid().toString() + "   receive:" + hexStr;
            XLog.d(TAG, string);
            NotifyHelper.getInstance().debuginfo(string);
            GattOperations.dealNotify(connectGatt, value, listener);
        }
    }

    @Override
    public synchronized void onDescriptorRead(BluetoothGatt gatt1, BluetoothGattDescriptor descriptor, int status) {
        super.onDescriptorRead(gatt1, descriptor, status);
        XLog.d(TAG, "onDescriptorRead() called with: connectGatt = [" + connectGatt + "], descriptor = [" + descriptor + "], status = [" + status + "]");
    }

    @Override
    public synchronized void onDescriptorWrite(BluetoothGatt gatt1, BluetoothGattDescriptor descriptor, int status) {
        super.onDescriptorWrite(gatt1, descriptor, status);
        //// TODO: 2016/11/16
        XLog.d(TAG, "onDescriptorWrite() called with: connectGatt = [" + connectGatt + "], descriptor = [" + descriptor + "], status = [" + status + "]");
        resetWriting();
        mHandler.removeMessages(MSG_WRITE_DESCRIPTOR);
        String desUuid = descriptor.getUuid().toString();
        String hexStr = ConvertUtil.byte2HexStr(descriptor.getValue());
        XLog.d(TAG, "onDescriptorWrite() -> desUuid = " + desUuid + " hexStr = " + hexStr);
        if (status == BluetoothGatt.GATT_SUCCESS) {
            retry_logic_fail = 0;
            XLog.d(TAG, "onDescriptorWrite succeed and set retry_logic_fail = 0.");
            onWriteDataSuccess(connectGatt, descriptor);
            NotifyHelper.getInstance().setMark(desUuid);
            if (!NotifyHelper.getInstance().isfinishset()) {
                NotifyHelper.getInstance().setNextNotify(listener, connectGatt);
            } else {
                DataHelper.getInstance().nextWrite(connectGatt);
            }
        } else {
            //写入失败，重试3次
            if (retry_logic_fail < LOGIC_FAILED_RETRY_MAX) {
                retry_logic_fail++;
                XLog.d(TAG, "onDescriptorWrite logic failed,and retry it the " + retry_logic_fail + " times.");
                DataHelper.getInstance().doWrite(connectGatt, descriptor);
            } else
                onWriteDataFailure(new GattException(connectGatt, status));
        }
    }

    @Override
    public synchronized void onReliableWriteCompleted(BluetoothGatt gatt1, int status) {
        super.onReliableWriteCompleted(gatt1, status);
        XLog.d(TAG, "onReliableWriteCompleted() called with: connectGatt = [" + connectGatt + "], status = [" + status + "]");
    }

    @Override
    public synchronized void onMtuChanged(BluetoothGatt gatt1, int mtu, int status) {
        super.onMtuChanged(gatt1, mtu, status);
        XLog.d(TAG, "onMtuChanged() called with: connectGatt = [" + connectGatt + "], mtu = [" + mtu + "], status = [" + status + "]");
    }

    //--------------------------customer callback---------------
    @Override
    public synchronized void onWriteDataSuccess(BluetoothGatt firstConnectGatt, Object object) {
        if (!GattStatusMachine.publicMachineStatus(listener, GATT_STATUS_SENDDATA_SUCCESS))
            return;
    }

    @Override
    public synchronized void onWriteDataFailure(GattException gattException) {
        if (!GattStatusMachine.publicMachineStatus(listener, GATT_STATUS_SENDDATA_FAILED))
            return;
        XLog.d(TAG, "onWriteDataFailure() called with: bleException = [" + gattException + "]");
        //写入失败
        initiativeDisConnect();
    }

    /**
     * 主动立马断开连接，释放资源,很快回调 onConnectFailure
     */
    private synchronized void initiativeDisConnect() {
        if (!GattStatusMachine.publicMachineStatus(listener, GATT_STATUS_DISCONNECTTING))
            return;
        XLog.d(TAG, "initiativeDisConnect() called");
        if (mHandler != null)
            mHandler.removeMessages(MSG_READ_RSSI);
        try {
            Thread.sleep(DISCONNECT_INVAL);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        connectGatt.disconnect();//将会触发 onConnectFailure
        //修复偶发不回调onConnectFailure的BUG
        startWaitDisconnectTimeout(connectGatt);
    }

    /**
     * 修复偶发不回调onConnectFailure的BUG
     *
     * @param firstConnectGatt
     */
    private synchronized void startWaitDisconnectTimeout(BluetoothGatt firstConnectGatt) {
        XLog.d(TAG, "startWaitDisconnectTimeout() called with: connectGatt = [" + firstConnectGatt + "]");
        mHandler.removeMessages(MSG_CLOSE_GATT_CONNECT);
        Message msg = mHandler.obtainMessage();
        TransData data = new TransData(firstConnectGatt);
        msg.obj = data;
        msg.what = MSG_CLOSE_GATT_CONNECT;
        mHandler.sendMessageDelayed(msg, WAIT_DISCONNECT_TIMEOUT);
    }

    public void setGattForFirstTime(BluetoothGatt connectGatt) {
        this.connectGatt = connectGatt;
    }

    /**
     * 断开连接，在onConnectFailed 中close 资源
     */
    public void setTimeoutToStop() {
        if (isReceiveNotify) {
            XLog.d("timeout", "isReceiveNotify is true,return.");
            return;
        }
        XLog.d("timeout", "setTimeoutToStop() called");
        isTimeout = true;
        XLog.d("timeout", "currentGattStatus = " + currentGattStatus.toString());
        if (currentGattStatus != GATT_STATUS_DISCONNECTED
                && currentGattStatus != GATT_STATUS_CONNECTTING) {//已经连接上
            if (connectGatt != null) {
                XLog.d(TAG, "//已经连接上");
                XLog.d("timeout", "connectGatt.disconnect();");
                connectGatt.disconnect();
            }
        } else {
            XLog.d("timeout", "timoutMethod() called.");
            reMoveAllMsgForTimeout();
            if (connectGatt != null) {//处于连接中...
                XLog.d(TAG, "//处于连接中...");
                XLog.d(TAG, "timeout  -> connectGatt.disconnect()");
                XLog.d("timeout", "timeout  -> connectGatt.disconnect()");
                connectGatt.disconnect();
                XLog.d(TAG, "timeout  -> connectGatt.close()");
                XLog.d("timeout", "timeout  -> connectGatt.close()");
                connectGatt.close();
            }
            if (listener != null)
                listener.timeout();
            GattStatusMachine.publicMachineStatus(listener, GATT_STATUS_DISCONNECTED);
        }
    }
}
