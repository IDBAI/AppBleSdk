package com.revenco.database.bean;

import java.io.Serializable;

/**
 * <p>PROJECT : AppBleSdk</p>
 * <p>COMPANY : wanzhong</p>
 * <p>AUTHOR : Administrator on 2017-03-30 15:37.</p>
 * <p>CLASS DESCRIBE :统计ble开门数据</p>
 * <p>CLASS_VERSION : 1.0.0</p>
 */
public class StatisticalBean implements Serializable {
    public static final long serialVersionUID = 7478586367122566387L;
    public int ID;
    public String deviceId;
    public String deviceAddress;
    /**
     * 总次数
     */
    public int totalCount;
    public int successCount;
    public int timeoutCount;
    public int failedCount;
    /**
     * 平均信号强度
     */
    public int averageRSSI;
    /**
     * 平均开锁时间
     */
    public float averageOpenTime;
    /**
     * 成功率
     */
    public float successRate;
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

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public void setDeviceAddress(String deviceAddress) {
        this.deviceAddress = deviceAddress;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    public void setSuccessCount(int successCount) {
        this.successCount = successCount;
    }

    public void setTimeoutCount(int timeoutCount) {
        this.timeoutCount = timeoutCount;
    }

    public void setFailedCount(int failedCount) {
        this.failedCount = failedCount;
    }

    public void setAverageRSSI(int averageRSSI) {
        this.averageRSSI = averageRSSI;
    }

    public void setAverageOpenTime(float averageOpenTime) {
        this.averageOpenTime = averageOpenTime;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public void setSuccessRate(float successRate) {
        this.successRate = successRate;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    @Override
    public String toString() {
        return "StatisticalBean{" +
                "ID=" + ID +
                ", deviceId='" + deviceId + '\'' +
                ", deviceAddress='" + deviceAddress + '\'' +
                ", totalCount=" + totalCount +
                ", successCount=" + successCount +
                ", timeoutCount=" + timeoutCount +
                ", failedCount=" + failedCount +
                ", averageRSSI=" + averageRSSI +
                ", averageOpenTime=" + averageOpenTime +
                ", successRate=" + successRate +
                ", tag='" + tag + '\'' +
                '}';
    }
}
