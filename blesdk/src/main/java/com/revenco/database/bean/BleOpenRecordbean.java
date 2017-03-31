package com.revenco.database.bean;

import java.io.Serializable;

/**
 * <p>PROJECT : AppBleSdk</p>
 * <p>COMPANY : wanzhong</p>
 * <p>AUTHOR : Administrator on 2017-03-30 15:17.</p>
 * <p>CLASS DESCRIBE :</p>
 * <p>CLASS_VERSION : 1.0.0</p>
 */
public class BleOpenRecordBean implements Serializable {
    public static final long serialVersionUID = 8322674129500560081L;
    public int ID;
    public String userId;
    /**
     * 设备ID
     */
    public String deviceId;
    /**
     * 设备物理地址
     */
    public String deviceAddress;
    public int RSSI;
    /**
     * 扫描时间
     */
    public float scanTime;
    public int certificateIndex;
    public String openResult;
    public String reason;
    /**
     * 开门耗费时间
     */
    public float openConsumeTime;

    public void setCurrentDate(String currentDate) {
        this.currentDate = currentDate;
    }

    /**

     * 当前时间
     */
    public String currentDate;
    /**
     * 保留字段
     */
    public String tag;

    @Override
    public String toString() {
        return "BleOpenRecordBean{" +
                "ID=" + ID +
                ", userId='" + userId + '\'' +
                ", deviceId='" + deviceId + '\'' +
                ", deviceAddress='" + deviceAddress + '\'' +
                ", RSSI=" + RSSI +
                ", scanTime=" + scanTime +
                ", certificateIndex=" + certificateIndex +
                ", openResult='" + openResult + '\'' +
                ", reason='" + reason + '\'' +
                ", openConsumeTime=" + openConsumeTime +
                ", tag='" + tag + '\'' +
                '}';
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public void setDeviceAddress(String deviceAddress) {
        this.deviceAddress = deviceAddress;
    }

    public void setRSSI(int RSSI) {
        this.RSSI = RSSI;
    }

    public void setScanTime(float scanTime) {
        this.scanTime = scanTime;
    }

    public void setCertificateIndex(int certificateIndex) {
        this.certificateIndex = certificateIndex;
    }

    public void setOpenResult(String openResult) {
        this.openResult = openResult;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public void setOpenConsumeTime(float openConsumeTime) {
        this.openConsumeTime = openConsumeTime;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }
}
