package com.revenco.appblesdk.activities;

import android.Manifest;
import android.annotation.TargetApi;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanResult;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.github.dfqin.grantor.PermissionListener;
import com.github.dfqin.grantor.PermissionsUtil;
import com.revenco.appblesdk.R;
import com.revenco.blesdk.core.iBeaconManager;
import com.revenco.blesdk.interfaces.oniBeaconStatusListener;
import com.revenco.blesdk.utils.SDKUtils;
import com.revenco.blesdk.utils.XLog;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Date;

import static com.revenco.appblesdk.BleApplication.SERVICE_UUID;

public class ShakeActivity extends AppCompatActivity implements SensorEventListener, oniBeaconStatusListener {
    private static final int START_SHAKE = 0x1;
    private static final int AGAIN_SHAKE = 0x2;
    private static final int END_SHAKE = 0x3;
    private static final int OPEN_SUCCEED = 0x4;
    private static final int OPEN_FAILURE = 0x5;
    private static final int TIMEOUT = 0x6;
    private static final int PERMISSION_REQUEST_LIST = 100;
    private static final String TAG = "AutoTestActivity";
    private static final int STAT_COUNT = 5;//统计数据截取阈值
    private SensorManager mSensorManager;
    private Sensor mAccelerometerSensor;
    private Vibrator mVibrator;//手机震动
    private SoundPool mSoundPool;//摇一摇音效
    //记录摇动状态
    private boolean isShake = false;
    private int mWeShareAudio = -1;
    private int mWeShareOpenSucceed = -1;
    private int mWeShareOpenFailure = -1;
    private int mWeShareOpenTimeout = -1;
    private MyHandler mHandler;
    private long startTime;
    private EditText editTime;
    private TextView text_timeout;
    private TextView text_failure;
    private TextView text_succeed;
    private int succeed, failure, timeout, rssiTotal, rssiCount;
    private TextView text_rate;
    private TextView text_tongji;
    private long totaltime;
    private Object lock;
    private TextView text_Rssi;
    private TextView tv_sdk;
    private PermissionListener permissionListener = new PermissionListener() {
        @Override
        public void permissionGranted(@NonNull String[] permissions) {
            switch (permissions[0]) {
                case Manifest.permission.ACCESS_COARSE_LOCATION:
                    Toast.makeText(ShakeActivity.this, "已获取位置权限！", Toast.LENGTH_SHORT).show();
                    break;
                case Manifest.permission.WRITE_EXTERNAL_STORAGE:
                    Toast.makeText(ShakeActivity.this, "已获取SD卡读写权限！", Toast.LENGTH_SHORT).show();
                    break;
            }
        }

        @Override
        public void permissionDenied(@NonNull String[] permissions) {
            switch (permissions[0]) {
                case Manifest.permission.ACCESS_COARSE_LOCATION:
                    Toast.makeText(ShakeActivity.this, "位置权限被拒绝！", Toast.LENGTH_SHORT).show();
                    break;
                case Manifest.permission.WRITE_EXTERNAL_STORAGE:
                    Toast.makeText(ShakeActivity.this, "SD卡读写权限被拒绝！", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    /**
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_shake);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2)
            iBeaconManager.getInstance().init(ShakeActivity.this, ShakeActivity.this);
        try {
            iBeaconManager.getInstance().setSERVICE_UUID(SERVICE_UUID);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //
        //
        editTime = (EditText) findViewById(R.id.edit_time);
        text_succeed = (TextView) findViewById(R.id.text_succeed);
        text_failure = (TextView) findViewById(R.id.text_failure);
        text_timeout = (TextView) findViewById(R.id.text_timeout);
        text_rate = (TextView) findViewById(R.id.text_rate);
        text_tongji = (TextView) findViewById(R.id.text_tongji);
        text_Rssi = (TextView) findViewById(R.id.text_Rssi);
        tv_sdk = (TextView) findViewById(R.id.tv_sdk);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR2) {
            String bleAddress = iBeaconManager.getInstance().getBleAddress();
            ((TextView) findViewById(R.id.text_addr)).setText("BleMac: " + bleAddress);
        }
        reset();
        lock = new Object();
        String getbleSDK = SDKUtils.getbleSDK(getApplicationContext());
        XLog.d(TAG, "ble SDK :" + getbleSDK);
        tv_sdk.setText(getbleSDK);
        gantPermission();
    }

    private void reset() {
        succeed = 0;
        failure = 0;
        timeout = 0;
        totaltime = 0;
        rssiCount = 0;
        rssiTotal = 0;
    }

    @Override
    protected void onPause() {
        XLog.d(TAG, "onPause() called");
        super.onPause();
        unRegistSensor();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        XLog.d(TAG, "onDestroy() called");
        destoryBleGatt();
    }

    private void unRegistSensor() {
        if (mSensorManager != null) {
            mSensorManager.unregisterListener(this);
        }
    }

    private void registSensor() {
        initSensorValues();
        mSensorManager = ((SensorManager) getSystemService(SENSOR_SERVICE));
        if (mSensorManager != null) {
            mAccelerometerSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            if (mAccelerometerSensor != null) {
                mSensorManager.registerListener(this, mAccelerometerSensor, SensorManager.SENSOR_DELAY_UI);
            }
        }
    }

    private void initSensorValues() {
        if (mHandler == null)
            mHandler = new MyHandler(ShakeActivity.this);
        if (mSoundPool == null)
            mSoundPool = new SoundPool(1, AudioManager.STREAM_SYSTEM, 5);
        if (mWeShareAudio == -1)
            mWeShareAudio = mSoundPool.load(this, R.raw.shake_audio, 1);
        if (mWeShareOpenSucceed == -1)
            mWeShareOpenSucceed = mSoundPool.load(this, R.raw.shake_open_succeed, 1);
        if (mWeShareOpenFailure == -1)
            mWeShareOpenFailure = mSoundPool.load(this, R.raw.shake_open_failure, 1);
        if (mWeShareOpenTimeout == -1)
            mWeShareOpenTimeout = mSoundPool.load(this, R.raw.shake_timeout, 1);
        if (mVibrator == null)
            mVibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
    }

    @Override
    protected void onResume() {
        XLog.d(TAG, "onResume() called ");
        super.onResume();
        registSensor();
    }

    /**
     * @return true:有权限
     */
    private boolean gantPermission() {
        XLog.d(TAG, "gantPermission() called ");
        if (!PermissionsUtil.hasPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)) {
            PermissionsUtil.TipInfo tipInfo = new PermissionsUtil.TipInfo("权限请求被拒绝", "位置权限请求被残忍拒绝", "取消", "打开设置");
            PermissionsUtil.requestPermission(this, permissionListener, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, true, tipInfo);
            return false;
        } else if (!PermissionsUtil.hasPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {//sdcard权限
            PermissionsUtil.TipInfo tipInfo = new PermissionsUtil.TipInfo("权限请求被拒绝", "SD卡读写权限请求被残忍拒绝", "取消", "打开设置");
            PermissionsUtil.requestPermission(this, permissionListener, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, true, tipInfo);
            return false;
        } else
            return true;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        int type = event.sensor.getType();
        if (type == Sensor.TYPE_ACCELEROMETER) {
            //获取三个方向值
            float[] values = event.values;
            float x = values[0];
            float y = values[1];
            float z = values[2];
            if ((Math.abs(x) > 17 || Math.abs(y) > 17 || Math.abs(z) > 17) && !isShake) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {//6.0动态获取权限
                    if (gantPermission()) {//有权限
                        startShake();
                    }
                } else {
                    startShake();
                }
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void startShake() {
        XLog.d(TAG, "startShake() called ");
        if (!iBeaconManager.getInstance().init(this, this))
            return;
        // TODO: 实现摇动逻辑, 摇动后进行震动
        startTime = SystemClock.elapsedRealtime();
        startBle();
        isShake = true;
        startShakeThread();
    }

    private void startShakeThread() {
        Thread thread = new Thread() {
            @Override
            public void run() {
                super.run();
                try {
                    //开始震动 发出提示音 展示动画效果
                    mHandler.obtainMessage(START_SHAKE).sendToTarget();
                    Thread.sleep(500);
                    //再来一次震动提示
                    mHandler.obtainMessage(AGAIN_SHAKE).sendToTarget();
                    Thread.sleep(500);
                    mHandler.obtainMessage(END_SHAKE).sendToTarget();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        thread.start();
    }

    /**
     * 核心方法入口
     */
    private void startBle() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            if (iBeaconManager.getInstance().isinitialize()) {
                iBeaconManager.getInstance().startScan();
            }
        }
    }

    private void destoryBleGatt() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            iBeaconManager.getInstance().closeConnectGatt();
            iBeaconManager.getInstance().refreshDeviceCache();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    //---------------------------------------callback start---------------
    @Override
    public void onIbeaconHadDetect(BluetoothDevice device, ScanResult scanResult) {
    }

    @Override
    public void onStatusChange(iBeaconManager.GattStatusEnum statusEnum) {
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
            case GATT_STATUS_NOTIFY_SUCCESS:
                XLog.d(TAG, "开门成功！");
                if (mHandler != null)
                    mHandler.obtainMessage(OPEN_SUCCEED).sendToTarget();
                break;
            case GATT_STATUS_NOTIFY_FAILED:
                XLog.d(TAG, "开门失败！");
                if (mHandler != null)
                    mHandler.obtainMessage(OPEN_FAILURE).sendToTarget();
                break;
        }
    }

    @Override
    public void onRssiCallback(double distance, final int rssi) {
        XLog.d(TAG, "onRssiCallback() called with: distance = [" + distance + "], rssi = [" + rssi + "]");
        rssiCount++;
        rssiTotal += rssi;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                text_Rssi.setText("rssi：" + rssi);
            }
        });
    }

    @Override
    public void onReScan() {
    }

    @Override
    public void timeout() {
        XLog.d(TAG, "开锁超时失败");
        if (mHandler != null)
            mHandler.obtainMessage(TIMEOUT).sendToTarget();
    }

    //---------------------------------------callback end---------------
    private static class MyHandler extends Handler {
        //弱引用解决内存泄露问题
        private WeakReference<ShakeActivity> mReference;
        private ShakeActivity activity;

        public MyHandler(ShakeActivity service) {
            mReference = new WeakReference<>(service);
            if (mReference != null) {
                activity = mReference.get();
            }
        }

        @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss:sss");
            Date date = new Date();
            String dateString = format.format(date);
            long endtime = SystemClock.elapsedRealtime();
            long longinval = endtime - activity.startTime;
            XLog.d(TAG, "结束时间：" + endtime + ", 间隔：(" + endtime + " - " + activity.startTime + " = " + longinval + " )");
            float inval = longinval / 1000.0f;
            switch (msg.what) {
                case START_SHAKE:
                    activity.mVibrator.vibrate(300);
                    //发出提示音
                    activity.mSoundPool.play(activity.mWeShareAudio, 1, 1, 0, 0, 1);
//                    activity.startAnimation(false);
                    break;
                case AGAIN_SHAKE:
                    activity.mVibrator.vibrate(300);
                    break;
                case END_SHAKE:
                    //整体效果结束, 将震动设置为false
                    // 展示上下两种图片回来的效果
//                    activity.startAnimation(true);
                    break;
                case OPEN_SUCCEED:
                    //发出提示音
                    activity.mSoundPool.play(activity.mWeShareOpenSucceed, 1, 1, 0, 0, 1);
                    Toast.makeText(activity.getApplication(), "开门成功！", Toast.LENGTH_SHORT).show();
                    activity.editTime.append("[" + dateString + "] succeed : " + inval + "s\n");
                    activity.text_succeed.setText("成功：" + (++activity.succeed) + "次");
                    float rate = (activity.succeed * 1.0f / (activity.succeed + activity.failure + activity.timeout)) * 100.0f;
                    activity.text_rate.setText("成功率：" + rate + "%");
                    activity.totaltime += longinval;
                    activity.text_tongji.setText("开门平均耗时：" + (activity.totaltime * 1.0f / (activity.succeed)) * 1.0f / 1000.0f + " 秒");
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    activity.isShake = false;
                    synchronized (activity.lock) {
                        activity.lock.notify();
                    }
                    break;
                case OPEN_FAILURE:
                    //发出提示音
                    activity.mSoundPool.play(activity.mWeShareOpenFailure, 1, 1, 0, 0, 1);
                    Toast.makeText(activity.getApplication(), "开门失败！", Toast.LENGTH_SHORT).show();
                    activity.editTime.append("[" + dateString + "] failure : " + inval + "s\n");
                    activity.text_failure.setText("失败：" + (++activity.failure) + "次");
                    float rate1 = (activity.succeed * 1.0f / (activity.succeed + activity.failure + activity.timeout)) * 100.0f;
                    activity.text_rate.setText("成功率：" + rate1 + "%");
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    activity.isShake = false;
                    synchronized (activity.lock) {
                        activity.lock.notify();
                    }
                    break;
                case TIMEOUT:
                    //发出提示音
                    activity.mSoundPool.play(activity.mWeShareOpenTimeout, 1, 1, 0, 0, 1);
                    Toast.makeText(activity.getApplication(), "开门超时！", Toast.LENGTH_SHORT).show();
                    activity.editTime.append("[" + dateString + "] timeout : " + inval + "s\n");
                    activity.text_timeout.setText("超时：" + (++activity.timeout) + "次");
                    float rate2 = (activity.succeed * 1.0f / (activity.succeed + activity.failure + activity.timeout)) * 100.0f;
                    activity.text_rate.setText("成功率：" + rate2 + "%");
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    activity.isShake = false;
                    synchronized (activity.lock) {
                        activity.lock.notify();
                    }
                    break;
            }
            saveStatData();
        }

        /**
         * 保存统计数据
         */
        private void saveStatData() {
            XLog.d(TAG, "saveStatData() called");
            int totalCount = activity.succeed + activity.failure + activity.timeout;
            if (totalCount % activity.STAT_COUNT == 0) {
                StringBuffer sb = new StringBuffer();
                if (activity.rssiCount != 0)
                    sb.append("平均信号强度：" + (activity.rssiTotal / activity.rssiCount) + "， ");
                sb.append("开门总次数：" + totalCount);
                sb.append("， 成功：" + activity.succeed);
                sb.append("， 失败：" + activity.failure);
                sb.append("， 超时：" + activity.timeout);
                float rate = (activity.succeed * 1.0f / (activity.succeed + activity.failure + activity.timeout)) * 100.0f;
                sb.append("， 成功率：" + rate + "%");
                if (activity.succeed != 0)
                    sb.append("， 平均开门耗时：" + (activity.totaltime * 1.0f / (activity.succeed)) * 1.0f / 1000.0f + " 秒");
                XLog.writeLog(TAG, sb.toString());
            }
        }
    }
}
