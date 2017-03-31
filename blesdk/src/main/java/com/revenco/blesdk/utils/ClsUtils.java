package com.revenco.blesdk.utils;

import android.bluetooth.BluetoothDevice;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Created by Administrator on 2016/12/1.
 */
public class ClsUtils {
    private static final String TAG = "ClsUtils";

    /**
     * 与设备配对 参考源码：platform/packages/apps/Settings.git
     * /Settings/src/com/android/settings/bluetooth/CachedBluetoothDevice.java
     */
    @SuppressWarnings("unchecked")
    static public boolean createBond(@SuppressWarnings("rawtypes") Class btClass, BluetoothDevice btDevice) throws Exception {
        XLog.d(TAG, "createBond() called with: btClass = [" + btClass + "], btDevice = [" + btDevice + "]");
        Method createBondMethod = btClass.getMethod("createBond");
        Boolean returnValue = (Boolean) createBondMethod.invoke(btDevice);
        XLog.d(TAG, "returnValue = " + returnValue.booleanValue());
        return returnValue.booleanValue();
    }

    /**
     * 与设备解除配对 参考源码：platform/packages/apps/Settings.git
     * /Settings/src/com/android/settings/bluetooth/CachedBluetoothDevice.java
     */
    @SuppressWarnings("unchecked")
    static public boolean removeBond(Class btClass, BluetoothDevice btDevice) throws Exception {
        Method removeBondMethod = btClass.getMethod("removeBond");
        Boolean returnValue = (Boolean) removeBondMethod.invoke(btDevice);
        return returnValue.booleanValue();
    }

    @SuppressWarnings("unchecked")
    static public boolean setPin(Class btClass, BluetoothDevice btDevice, String str) throws Exception {
        XLog.d(TAG, "setPin() called with: btClass = [" + btClass + "], btDevice = [" + btDevice + "], str = [" + str + "]");
        Boolean returnValue = false;
        try {
            Method removeBondMethod = btClass.getDeclaredMethod("setPin", new Class[]{byte[].class});
            returnValue = (Boolean) removeBondMethod.invoke(btDevice, new Object[]{str.getBytes()});
            XLog.d(TAG, "setPin is result_success " + btDevice.getAddress() + returnValue.booleanValue());
        } catch (SecurityException e) {
            // throw new RuntimeException(e.getMessage());
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            // throw new RuntimeException(e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return returnValue;
    }

    // 取消用户输入
    @SuppressWarnings("unchecked")
    static public boolean cancelPairingUserInput(Class btClass, BluetoothDevice device) throws Exception {
        XLog.d(TAG, "cancelPairingUserInput() called with: btClass = [" + btClass + "], device = [" + device + "]");
        Method createBondMethod = btClass.getMethod("cancelPairingUserInput");
        // cancelBondProcess()
        Boolean returnValue = (Boolean) createBondMethod.invoke(device);
        XLog.d(TAG, "cancelPairingUserInput is result_success " + returnValue.booleanValue());
        return returnValue.booleanValue();
    }

    // 取消配对
    @SuppressWarnings("unchecked")
    static public boolean cancelBondProcess(Class btClass, BluetoothDevice device) throws Exception {
        Method createBondMethod = btClass.getMethod("cancelBondProcess");
        Boolean returnValue = (Boolean) createBondMethod.invoke(device);
        return returnValue.booleanValue();
    }

    /**
     * 确认配对
     * @param btClass
     * @param device
     * @param isConfirm
     * @throws Exception
     */
    static public void setPairingConfirmation(Class<?> btClass, BluetoothDevice device, boolean isConfirm) throws Exception {
        Method setPairingConfirmation = btClass.getDeclaredMethod("setPairingConfirmation", boolean.class);
        setPairingConfirmation.invoke(device, isConfirm);
    }

    /**
     * @param clsShow
     */
    @SuppressWarnings("unchecked")
    static public void printAllInform(Class clsShow) {
        try {
            // 取得所有方法
            Method[] hideMethod = clsShow.getMethods();
            int i = 0;
            for (; i < hideMethod.length; i++) {
                //XLog.e("method name", hideMethod.getName() + ";and the i is:"
                //      + i);
            }
            // 取得所有常量
            Field[] allFields = clsShow.getFields();
            for (i = 0; i < allFields.length; i++) {
                //XLog.e("Field name", allFields.getName());
            }
        } catch (SecurityException e) {
            // throw new RuntimeException(e.getMessage());
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            // throw new RuntimeException(e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
