package com.revenco.database.bean;

import java.io.Serializable;

/**
 * <p>PROJECT : AppBleSdk</p>
 * <p>COMPANY : wanzhong</p>
 * <p>AUTHOR : Administrator on 2017-03-30 15:09.</p>
 * <p>CLASS DESCRIBE :</p>
 * <p>CLASS_VERSION : 1.0.0</p>
 */
public class CertificateBean implements Serializable {
    private static final long serialVersionUID = 5402116815076720314L;
    /**
     * 本地自增长ID
     */
    public int ID;
    /**
     * 证书索引值
     */
    public int certificateIndex;
    /**
     * 设备ID
     */
    public String deviceId;
    /**
     * 设备物理地址
     */
    public String deviceAddress;
    /**
     * 手机appMac地址
     */
    public String appBleMac;
    /**
     * 证书内容
     */
    public String content;
    /**
     * 保留字段
     */
    public String tag;
}
