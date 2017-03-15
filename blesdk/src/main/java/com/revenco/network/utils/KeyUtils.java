//package com.revenco.network.utils;
//
//import android.text.TextUtils;
//
//import com.revenco.commonlibrary.log.XLog;
//
//import org.bouncycastle.util.encoders.Base64;
//
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.FileOutputStream;
//import java.io.IOException;
//import java.security.InvalidAlgorithmParameterException;
//import java.security.InvalidKeyException;
//import java.security.KeyFactory;
//import java.security.KeyPair;
//import java.security.KeyPairGenerator;
//import java.security.NoSuchAlgorithmException;
//import java.security.NoSuchProviderException;
//import java.security.PrivateKey;
//import java.security.PublicKey;
//import java.security.SecureRandom;
//import java.security.Security;
//import java.security.Signature;
//import java.security.SignatureException;
//import java.security.spec.ECGenParameterSpec;
//import java.security.spec.InvalidKeySpecException;
//import java.security.spec.PKCS8EncodedKeySpec;
//import java.security.spec.X509EncodedKeySpec;
//
///**
// * Created by Administrator on 2017/2/10.
// */
//public class KeyUtils {
//    /**
//     * 签名算法
//     */
//    private static final String ALGORITHM = "SHA256withECDSA";
//    /**
//     * 曲线名称
//     */
//    private static final String CURVESNAME = "secp256k1";
//    private static final String TAG = "KeyUtils";
//    private static final String PROVIDER = "SC";
//    private static final String ECDSA = "ECDSA";
//
//    //加载算法库
//    static {
//        Security.insertProviderAt(new org.spongycastle.jce.provider.BouncyCastleProvider(), 1);
//    }
//
//    /**
//     * 生成椭圆曲线密钥对
//     *
//     * @return
//     * @throws NoSuchProviderException
//     * @throws NoSuchAlgorithmException
//     * @throws InvalidAlgorithmParameterException
//     */
//    public static KeyPair getECDSAKeyPair() throws NoSuchProviderException, NoSuchAlgorithmException, InvalidAlgorithmParameterException {
////        XLog.d(TAG, "getECDSAKeyPair() called");
//        KeyPairGenerator pairGenerator = KeyPairGenerator.getInstance(ECDSA, PROVIDER);
//        ECGenParameterSpec ecSpec = new ECGenParameterSpec(CURVESNAME);//使用secp256k1曲线
//        pairGenerator.initialize(ecSpec, new SecureRandom());
//        return pairGenerator.generateKeyPair();
//    }
//
//    /**
//     * 获取公钥
//     *
//     * @param keyPair
//     * @return
//     * @throws NoSuchProviderException
//     * @throws NoSuchAlgorithmException
//     * @throws InvalidKeySpecException
//     */
//    public static PublicKey getPublickey(KeyPair keyPair) throws NoSuchProviderException, NoSuchAlgorithmException, InvalidKeySpecException {
////        XLog.d(TAG, "getPublickey() called with: keyPair = [" + keyPair + "]");
//        KeyFactory factory = KeyFactory.getInstance(ECDSA, PROVIDER);
//        X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(keyPair.getPublic().getEncoded());
//        return factory.generatePublic(x509EncodedKeySpec);
//    }
//
//    /**
//     * 获取私钥
//     *
//     * @param keyPair
//     * @return
//     * @throws NoSuchProviderException
//     * @throws NoSuchAlgorithmException
//     * @throws InvalidKeySpecException
//     */
//    public static PrivateKey getPrivatekey(KeyPair keyPair) throws NoSuchProviderException, NoSuchAlgorithmException, InvalidKeySpecException {
////        XLog.d(TAG, "getPrivatekey() called with: keyPair = [" + keyPair + "]");
//        KeyFactory factory = KeyFactory.getInstance(ECDSA, PROVIDER);
//        PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(keyPair.getPrivate().getEncoded());
//        return factory.generatePrivate(pkcs8EncodedKeySpec);
//    }
//
//    /**
//     * 生成签名
//     *
//     * @param privateKey
//     * @param content
//     * @return
//     * @throws NoSuchAlgorithmException
//     * @throws InvalidKeyException
//     * @throws SignatureException
//     */
//    public static byte[] generateSign(PrivateKey privateKey, byte[] content) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
////        XLog.d(TAG, "generateSign() called with: privateKey = [" + privateKey + "], content = [" + content + "]");
//        Signature signature = Signature.getInstance(ALGORITHM);
//        signature.initSign(privateKey);
//        signature.update(content);
//        byte[] sign = signature.sign();
//        return sign;
//    }
//
//    /**
//     * 校验签名
//     *
//     * @param publicKey
//     * @param content
//     * @param sign
//     * @return
//     * @throws NoSuchAlgorithmException
//     * @throws InvalidKeyException
//     * @throws SignatureException
//     */
//    public static boolean verifySign(PublicKey publicKey, byte[] content, byte[] sign) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
//        Signature signature = Signature.getInstance(ALGORITHM);
//        signature.initVerify(publicKey);
//        signature.update(content);
//        return signature.verify(sign);
//    }
//
//    /**
//     * 将privateKey转换为PEM格式的私钥文件
//     *
//     * @param privateKey
//     * @param filePath
//     * @throws IOException
//     */
//    public static void writePrivateKeyToPem(PrivateKey privateKey, String filePath) throws IOException {
//        XLog.d(TAG, "writePrivateKeyToPem() called with: privateKey = [" + privateKey + "], filePath = [" + filePath + "]");
//        PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(privateKey.getEncoded());
//        FileOutputStream out = new FileOutputStream(new File(filePath));
//        String asB64 = Base64.toBase64String(pkcs8EncodedKeySpec.getEncoded());
//        out.write("-----BEGIN PRIVATE KEY-----\n".getBytes());
//        for (int i = 0; i < asB64.length(); i += 64) {
//            int actLen = ((i + 64) < asB64.length()) ? 64 : (asB64.length() - i);
//            String str = asB64.substring(i, i + actLen);
//            out.write(str.getBytes());
//            out.write("\n".getBytes());
//        }
//        out.write("-----END PRIVATE KEY-----\n".getBytes());
//        out.close();
//    }
//
//    /**
//     * 将publicKey转换为PEM格式的私钥文件
//     *
//     * @param publicKey
//     * @param filePath
//     * @throws IOException
//     */
//    public static void writePublicKeyToPem(PublicKey publicKey, String filePath) throws IOException {
//        XLog.d(TAG, "writePublicKeyToPem() called with: publicKey = [" + publicKey + "], filePath = [" + filePath + "]");
//        X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(publicKey.getEncoded());
//        FileOutputStream out = new FileOutputStream(new File(filePath));
//        String asB64 = Base64.toBase64String(x509EncodedKeySpec.getEncoded());
//        out.write("-----BEGIN PUBLIC KEY-----\n".getBytes());
//        for (int i = 0; i < asB64.length(); i += 64) {
//            int actLen = ((i + 64) < asB64.length()) ? 64 : (asB64.length() - i);
//            String str = asB64.substring(i, i + actLen);
//            out.write(str.getBytes());
//            out.write("\n".getBytes());
//        }
//        out.write("-----END PUBLIC KEY-----\n".getBytes());
//        out.close();
//    }
//
//    /**
//     * 从PEM中加载私钥
//     *
//     * @param filePath
//     * @return
//     * @throws IOException
//     * @throws NoSuchProviderException
//     * @throws NoSuchAlgorithmException
//     * @throws InvalidKeySpecException
//     */
//    public static PrivateKey loadPrivateKeyFromPem(String filePath) throws IOException, NoSuchProviderException, NoSuchAlgorithmException, InvalidKeySpecException {
//        XLog.d(TAG, "loadPrivateKeyFromPem() called with: filePath = [" + filePath + "]");
//        File file = new File(filePath);
//        FileInputStream inputStream = new FileInputStream(file);
//        byte[] content = new byte[(int) file.length()];
//        inputStream.read(content);
//        inputStream.close();
//        String strContent = new String(content);
//        if (TextUtils.isEmpty(strContent))
//            return null;
//        String privateKeyPEM = strContent.replace("-----BEGIN PRIVATE KEY-----\n", "")
//                .replace("-----END PRIVATE KEY-----\n", "").replace("\n", "");
//        byte[] bytes = Base64.decode(privateKeyPEM);
//        PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(bytes);
//        KeyFactory factory = KeyFactory.getInstance(ECDSA, PROVIDER);
//        PrivateKey privateKey = factory.generatePrivate(pkcs8EncodedKeySpec);
//        return privateKey;
//    }
//
//    /**
//     * 从PEM加载公钥
//     *
//     * @param filePath
//     * @return
//     * @throws IOException
//     * @throws NoSuchProviderException
//     * @throws NoSuchAlgorithmException
//     * @throws InvalidKeySpecException
//     */
//    public static PublicKey loadPublicKeyFromPem(String filePath) throws IOException, NoSuchProviderException, NoSuchAlgorithmException, InvalidKeySpecException {
//        XLog.d(TAG, "loadPublicKeyFromPem() called with: filePath = [" + filePath + "]");
//        File file = new File(filePath);
//        FileInputStream inputStream = new FileInputStream(file);
//        byte[] content = new byte[(int) file.length()];
//        inputStream.read(content);
//        inputStream.close();
//        String strContent = new String(content);
//        if (TextUtils.isEmpty(strContent))
//            return null;
//        String strPublicKey = strContent.replace("-----BEGIN PUBLIC KEY-----\n", "")
//                .replace("-----END PUBLIC KEY-----\n", "").replace("\n", "");
//        byte[] bytes = Base64.decode(strPublicKey);
//        X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(bytes);
//        KeyFactory factory = KeyFactory.getInstance(ECDSA, PROVIDER);
//        PublicKey publicKey = factory.generatePublic(x509EncodedKeySpec);
//        return publicKey;
//    }
//}
