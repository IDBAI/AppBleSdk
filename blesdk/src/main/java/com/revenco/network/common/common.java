//package com.revenco.network.common;
//
///**
// * Created by Administrator on 2017/2/10.
// */
//public class common {
//
//    /**
//     * 合并数据
//     *
//     * @param bytes
//     * @return
//     */
//    public static byte[] merge(byte[]... bytes) {
//        int count = 0;
//        for (byte[] mb : bytes) {
//            count += mb.length;
//        }
//        byte[] result = new byte[count];
//        int leng = 0;
//        for (int i = 0; i < bytes.length; i++) {
//            System.arraycopy(bytes[i], 0, result, leng, bytes[i].length);
//            leng += bytes[i].length;
//        }
//        return result;
//    }
//
//
//}
