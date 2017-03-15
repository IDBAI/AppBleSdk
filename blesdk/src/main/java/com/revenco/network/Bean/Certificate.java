//package com.revenco.network.Bean;
//
//import com.revenco.network.common.common;
//
//import java.io.Serializable;
//
///**
// * Created by Administrator on 2017/2/10.
// * 证书57字节长度
// */
//public class Certificate implements Serializable {
//    private static final long serialVersionUID = 7302996699800663326L;
//    private byte[] type;//类型 1字节
//    private byte[] BleMac;//ble mac地址 6字节
//    private byte[] deviceId;//设备id 16字节
//    private byte[] channelMask;//通道掩码 1字节
//    private byte[] userId;//用户id 16字节
//    private byte[] issueTime;//签发时间 4字节
//    private byte[] timeout;//失效时间 4字节
//    private byte[] counter;//计数器 8字节
//    private byte[] times;//使用次数 1字节
//
//
//
//    /**
//     * 获取测试证书
//     * @return
//     */
//    public static byte[] getCertificateForTest() {
//        Certificate cert = new Certificate();
//        cert.type = new byte[]{0x01};
//        cert.BleMac = new byte[]{0x4A, (byte) 0xD2, (byte) 0xF1, 0x30, (byte) 0xC5, (byte) 0x8E};//mi2s ble mac
//        byte[] deviceId = new byte[16];
//        for (int i = 0; i < 16; i++) {
//            deviceId[i] = (byte) i;
//        }
//        cert.deviceId = deviceId;
//        cert.channelMask = new byte[]{0x00};
//        byte[] userId = new byte[16];
//        for (int i = 0; i < 16; i++) {
//            userId[i] = (byte) ((byte) i + 16);
//        }
//        cert.userId = userId;
//        cert.issueTime = new byte[]{0x00, 0x01, 0x02, 0x03};
//        cert.timeout = new byte[]{0x03, 0x02, 0x01, 0x00};
//        cert.counter = new byte[]{0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07};
//        cert.times = new byte[]{0x00};
//        return common.merge(cert.type, cert.BleMac, cert.deviceId, cert.channelMask, cert.userId, cert.issueTime, cert.timeout
//                , cert.counter, cert.times);
//    }
//
//
//}
