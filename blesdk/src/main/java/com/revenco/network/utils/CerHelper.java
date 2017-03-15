//package com.revenco.network.utils;
//
//import android.content.Context;
//
//import com.revenco.blesdk.utils.ConvertUtil;
//import com.revenco.commonlibrary.log.XLog;
//import com.revenco.network.Bean.Certificate;
//import com.revenco.network.common.common;
//
//import java.io.File;
//import java.io.IOException;
//import java.security.InvalidAlgorithmParameterException;
//import java.security.KeyPair;
//import java.security.NoSuchAlgorithmException;
//import java.security.NoSuchProviderException;
//import java.security.PrivateKey;
//import java.security.PublicKey;
//import java.security.spec.InvalidKeySpecException;
//import java.util.ArrayList;
//
///**
// * Created by Administrator on 2017/2/10.
// */
//public class CerHelper {
//    private static final String TAG = "CerHelper";
//    private static KeyPair keyPair;
//    private static PrivateKey privatekey;
//    private static PublicKey publicKey;
//    private static boolean isinit;
//    private static File fileprivatekey;
//    private static File filepublicKey;
//
//    /**
//     * 初始化证书帮助类
//     */
//    public static void init(Context context) {
//        XLog.d(TAG, "init() called with: context = [" + context + "]");
//        if (fileprivatekey == null)
//            fileprivatekey = StorageUtils.getDataPath(context, "Certificate/privatekey.pem");
//        if (filepublicKey == null)
//            filepublicKey = StorageUtils.getDataPath(context, "Certificate/publicKey.pem");
//        boolean isNeedUpdate = false;
//        try {
//            loadLocalCertificateFromPem(fileprivatekey, filepublicKey);
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (NoSuchProviderException e) {
//            e.printStackTrace();
//        } catch (NoSuchAlgorithmException e) {
//            e.printStackTrace();
//        } catch (InvalidKeySpecException e) {
//            e.printStackTrace();
//        }
//        try {
//            if (privatekey == null || publicKey == null) {
//                keyPair = KeyUtils.getECDSAKeyPair();
//                privatekey = KeyUtils.getPrivatekey(keyPair);
//                publicKey = KeyUtils.getPublickey(keyPair);
//                isNeedUpdate = true;
//            }
//        } catch (NoSuchProviderException e) {
//            e.printStackTrace();
//            isinit = false;
//        } catch (NoSuchAlgorithmException e) {
//            e.printStackTrace();
//            isinit = false;
//        } catch (InvalidAlgorithmParameterException e) {
//            e.printStackTrace();
//            isinit = false;
//        } catch (InvalidKeySpecException e) {
//            e.printStackTrace();
//            isinit = false;
//        }
//        if (isNeedUpdate)
//            try {
//                writeLocalPem(fileprivatekey, filepublicKey);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        isinit = true;
//    }
//
//    private static void writeLocalPem(File fileprivatekey, File filepublicKey) throws IOException {
//        XLog.d(TAG, "writeLocalPem() called with: fileprivatekey = [" + fileprivatekey + "], filepublicKey = [" + filepublicKey + "]");
//        KeyUtils.writePrivateKeyToPem(privatekey, fileprivatekey.getAbsolutePath());
//        KeyUtils.writePublicKeyToPem(publicKey, filepublicKey.getAbsolutePath());
//    }
//
//    private static void loadLocalCertificateFromPem(File fileprivatekey, File filepublicKey) throws IOException, NoSuchProviderException, NoSuchAlgorithmException, InvalidKeySpecException {
//        XLog.d(TAG, "loadLocalCertificateFromPem() called with: fileprivatekey = [" + fileprivatekey + "], filepublicKey = [" + filepublicKey + "]");
//        if (privatekey == null || publicKey == null) {
//            privatekey = KeyUtils.loadPrivateKeyFromPem(fileprivatekey.getAbsolutePath());
//            publicKey = KeyUtils.loadPublicKeyFromPem(filepublicKey.getAbsolutePath());
//        }
//    }
//
//    public static PrivateKey getPrivatekey(Context context) {
//        if (privatekey == null)
//            init(context);
//        return privatekey;
//    }
//
//    public static PublicKey getPublicKey(Context context) {
//        if (publicKey == null)
//            init(context);
//        return publicKey;
//    }
//
//    /**
//     * 生成开门凭证
//     *
//     * @param context
//     * @return
//     * @throws Exception
//     */
//    public static byte[] generateOpenDoorCertificate(Context context) throws Exception {
//        XLog.d(TAG, "generateOpenoorCertificate() called with: context = [" + context + "]");
//        if (!isinit) {
//            throw new Exception("CerHelper is not init!");
//        }
//        byte[] certificate = Certificate.getCertificateForTest();//证书
//        byte[] sign = KeyUtils.generateSign(getPrivatekey(context), certificate);//证书的签名
//        return common.merge(certificate, sign);
//    }
//
//    /**
//     * 将byte数组分包成size大小的array
//     *
//     * @param bytes
//     * @param size
//     * @return
//     */
//    public static ArrayList<byte[]> spiltCertificate(byte[] bytes, int size) {
//        XLog.d(TAG, "spiltCertificate() called with: bytes = [" + bytes + "], size = [" + size + "]");
//        printBytes(bytes);
//        int arrSize = (int) Math.ceil(bytes.length * 1.0f / size);
//        //如果要补齐
////        if (bytes.length % size != 0) {
////            int polishSize = size - bytes.length % size;//需要补齐的位数
////            bytes = polishByte(bytes, polishSize);
////            printBytes(bytes);
////        }
//        ArrayList<byte[]> arrayList = new ArrayList<>(arrSize);
//        for (int i = 0; i < bytes.length; i += size) {
//            if (i / size == arrSize - 1 && bytes.length % size != 0) {//最后一包，并且不足size 大小，重新计算size
//                size = bytes.length % size;
//                XLog.d(TAG, "last size is : " + size);
//            }
//            byte[] des = new byte[size];
//            System.arraycopy(bytes, i, des, 0, size);
//            arrayList.add(des);
//            printBytes(des);
//        }
//        return arrayList;
//    }
//
//    /**
//     * 补齐
//     *
//     * @param bytes
//     * @param polishSize
//     * @return
//     */
//    private static byte[] polishByte(byte[] bytes, int polishSize) {
//        return common.merge(bytes, new byte[polishSize]);
//    }
//
//    private static void printBytes(byte[] bytes) {
//        System.out.println(ConvertUtil.byte2HexStrWithSpace(bytes));
//        XLog.d(TAG, ConvertUtil.byte2HexStrWithSpace(bytes));
//    }
//}
