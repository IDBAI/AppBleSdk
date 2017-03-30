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
    private static final long serialVersionUID = 8322674129500560081L;


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
    public int scanTime;

    public int CertificateIndex;

    public String openResult;

    public String reason;
    /**
     * 开门耗费时间
     */
    public int openConsumeTime;


}
