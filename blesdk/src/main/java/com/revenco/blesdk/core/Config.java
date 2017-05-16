package com.revenco.blesdk.core;

/**
 * Created by Administrator on 2017/2/27.
 * 硬编码配置----跟A20 的BLE-HCI库的硬编码配置一一对应
 */
public class Config {
    //写入的特征值uuid-------------start
    public static final String WRITE_UUID1 = "11111111-96E2-119E-9E11-E29611111111";
    public static final String WRITE_UUID2 = "22222222-96E2-119E-9E11-E29622222222";
    public static final String WRITE_UUID3 = "33333333-96E2-119E-9E11-E29633333333";
    public static final String WRITE_UUID4 = "44444444-96E2-119E-9E11-E29644444444";
    public static final String WRITE_UUID5 = "55555555-96E2-119E-9E11-E29655555555";
    public static final String WRITE_UUID6 = "66666666-96E2-119E-9E11-E29666666666";
    public static final String WRITE_UUID7 = "77777777-96E2-119E-9E11-E29677777777";
    //写入的特征值uuid-------------end
    //通知的特征值uuid-------------start
    public static final String NOTIFY_UUID = "88888888-96E2-119E-9E11-E29688888888";
    //通知的特征值uuid-------------end
    //暂不需要！
    public static final String CHAR_DESC_UUID = "88888888-8888-8888-8888-888888888888";
    /**
     * 标准设定，特征值数量为8，在兼容获取service时候使用判断
     */
    public static final int CHAR_SIZE = 8;

    ////////////////////////////////////////以下发包相关
    /**
     * 每个发包间隔
     */
    public static final long SEND_INTERVAL = 5L;
    /**
     * 发送数据之前间隔停留，用于等待更新参数成功
     */
    public static final long BEFORE_SEND_INTEVAL = 0L;
}
