package com.revenco.blesdk.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.RequiresApi;

import com.revenco.blesdk.callback.BleConnectGattCallback;
import com.revenco.blesdk.callback.CallbackConnectHelper;
import com.revenco.blesdk.callback.ScanCallBackBelowLOLLIPOP;
import com.revenco.blesdk.callback.ScanCallBackOverLOLLIPOP;
import com.revenco.blesdk.interfaces.BluetoothExceptionListener;
import com.revenco.blesdk.interfaces.oniBeaconStatusListener;
import com.revenco.blesdk.utils.XLog;

@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
public class BleBluetooth {
    private static final String TAG = BleBluetooth.class.getSimpleName();
    private static BleBluetooth INSTANCE = new BleBluetooth();
    private Context context;
    private BluetoothManager bluetoothManager;
    private BluetoothAdapter bluetoothAdapter;
    private Handler handler = new Handler(Looper.getMainLooper());
    private ScanCallBackOverLOLLIPOP scanCallOverLOLLIPOP;
    private ScanCallBackBelowLOLLIPOP scanCallBackBelowLOLLIPOP;
    private BluetoothExceptionListener exceptionListener;
    private oniBeaconStatusListener optionListener;
    private volatile boolean isTimeout = false;

    public static synchronized BleBluetooth getInStance() {
        return INSTANCE;
    }

    public void init(Context context, BluetoothExceptionListener listener) {
        this.exceptionListener = listener;
        this.context = context = context.getApplicationContext();
        bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            scanCallOverLOLLIPOP = new ScanCallBackOverLOLLIPOP(context);
        } else {
            scanCallBackBelowLOLLIPOP = new ScanCallBackBelowLOLLIPOP(context, bluetoothAdapter);
        }
    }

    public BluetoothAdapter getBluetoothAdapter() {
        return bluetoothAdapter;
    }

    public synchronized boolean isOpenble() {
        return bluetoothAdapter.isEnabled();
    }

    public synchronized boolean openBle() {
        return bluetoothAdapter.enable();
    }

    public synchronized void startBLEScan(oniBeaconStatusListener listener, long scanPeriod, long timeout) throws Exception {
        XLog.d(TAG, "startBLEScan() called with: listener = [" + listener + "], scanPeriod = [" + scanPeriod + "], result_timeout = [" + timeout + "]");
        this.optionListener = listener;
        openBle();
        scan(listener, scanPeriod, timeout);
    }

    private synchronized void scan(oniBeaconStatusListener listener, long scanPeriod, long timeout) {
        XLog.d(TAG, "scan() called with: listener = [" + listener + "], scanPeriod = [" + scanPeriod + "], result_timeout = [" + timeout + "]");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            XLog.d(TAG, "startBLEScan API > 21");
            scanCallOverLOLLIPOP.setListener(listener);
            bluetoothAdapter.getBluetoothLeScanner().startScan(scanCallOverLOLLIPOP);
        } else {
            XLog.d(TAG, "startBLEScan API <= 21");
            scanCallBackBelowLOLLIPOP.setListener(listener);
            boolean startLeScan = bluetoothAdapter.startLeScan(scanCallBackBelowLOLLIPOP);
            if (!startLeScan) {
                if (this.exceptionListener != null)
                    this.exceptionListener.onBlueThoodException();
            }
            XLog.d(TAG, "startLeScan = " + startLeScan);
        }
        //扫描超时
        isTimeout = true;
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                XLog.d("result_timeout", "first runnable.");
                try {
                    if (isTimeout)
                        stopScan(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, scanPeriod);
        //总超时
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                XLog.d("result_timeout", "run() called");
                BleConnectGattCallback gattCallback = CallbackConnectHelper.getbleConnectGattCallback();
                if (gattCallback != null) {
                    XLog.d("result_timeout", "gattCallback.setTimeoutToStop();");
                    gattCallback.setTimeoutToStop();
                } else {
                    XLog.d("result_timeout", "gattCallback is null.");
                }
            }
        }, timeout);
    }

    /**
     * @param isTimout
     * @throws Exception
     */
    public synchronized void stopScan(boolean isTimout) throws Exception {
        XLog.d(TAG, "stopScan() called with: isTimout = [" + isTimout + "]");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            XLog.d(TAG, "stopScan API > 21");
            bluetoothAdapter.getBluetoothLeScanner().stopScan(scanCallOverLOLLIPOP);
        } else {
            XLog.d(TAG, "stopScan API <= 21");
            bluetoothAdapter.stopLeScan(scanCallBackBelowLOLLIPOP);
        }
        this.isTimeout = isTimout;
        if (isTimout) {
            if (optionListener != null)
                optionListener.timeout();
        }
    }
}
