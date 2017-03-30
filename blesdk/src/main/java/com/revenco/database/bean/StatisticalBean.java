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
    private static final long serialVersionUID = 7478586367122566387L;
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
    /**
     * 保留字段
     */
    public String tag;
}
