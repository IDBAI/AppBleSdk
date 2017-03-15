package com.revenco.blesdk.core;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;

import com.revenco.blesdk.R;
import com.revenco.blesdk.utils.XLog;

/**
 * Created by Administrator on 2016/11/3.
 */
public class CoreHelper {
//    public static final int intervalSecond = 10;//10秒
    private static final String TAG = "CoreHelper";
    private static PowerManager.WakeLock mWakeLock;

    /**
     * 开启轮询服务,
     *
     * @param context
     * @param seconds
     * @param cls
     */
    public static void startPollingService(Context context, int seconds, Class<?> cls) {
//        XLog.d(TAG, "AlertManager poll BleService " + intervalSecond + " seconds interval:" + cls.getName());
        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, cls);
        PendingIntent pendingIntent = PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        manager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime(), seconds * 1000, pendingIntent);
    }




    //停止轮询服务
    public static void stopPollingService(Context context, Class<?> cls, String... action) {
        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, cls);
        if (action != null)
            for (String s : action)
                intent.setAction(s);
        PendingIntent pendingIntent = PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        //取消正在执行的服务
        manager.cancel(pendingIntent);
    }

    public static Notification getForgroundNotification(Context context) {
        Notification notification;
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder.setSmallIcon(R.drawable.ic_launcher);
        builder.setAutoCancel(true);
        builder.setContentTitle("蓝牙开门服务");
        builder.setContentText("如果该通知不可见，无法进行蓝牙开门。");
        builder.setWhen(System.currentTimeMillis());
        notification = builder.build();
        notification.flags |= Notification.FLAG_NO_CLEAR;
        return notification;
    }

    /**
     * 保持持久唤醒状态 http://www.cnblogs.com/joseph-linux/p/3445477.html
     * <p>Flag Value                   CPU     Screen      Keyboard</p>
     * <p>PARTIAL_WAKE_LOCK            On      can-off      Off</p>
     * <p>SCREEN_DIM_WAKE_LOCK         On       Dim          Off</p>
     * <p>PROXIMITY_SCREEN_OFF_WAKE_LOCK on      距离传感器时关闭  off</p>
     * <p>SCREEN_BRIGHT_WAKE_LOCK      On       Bright       Off</p>
     * <p>FULL_WAKE_LOCK               On       Bright       Bright</p>
     */
    public static synchronized void acquireWakeLock(Context context) {
        XLog.d(TAG, "acquireWakeLock() called with: context = [" + context + "]");
        if (mWakeLock == null) {
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, context.getClass().getCanonicalName());
        }
        mWakeLock.acquire();
    }

    /**
     * 释放持久唤醒锁
     */
    public static synchronized void releaseWakeLock() {
        XLog.d(TAG, "releaseWakeLock() called");
        if (mWakeLock != null && mWakeLock.isHeld()) {
            mWakeLock.release();
        }
    }
}
