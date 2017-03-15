package com.revenco.blesdk;

import com.revenco.blesdk.utils.ConvertUtil;
import com.revenco.blesdk.utils.RSAUtils;

import junit.framework.TestSuite;

import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest extends TestSuite {
    @Test
    public void addition_isCorrect() throws Exception {
        assertEquals(4, 2 + 2);
    }

    /**
     * RSA加密解密测试
     * privateKey = MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBAJ1FjDbZxLRZ0QTpMGtV3QOBEU/b2QHl3jV16hNrZocUKjweorTOi7/ZVJ4IvTDYjQCVx9qlJ+zSXq8GnSLqPoEnDUQKRctLB/x4M+iw5etnZXmwhzcydAgbsKAlXGKW80z2lphb6HLkE8bCcusJ1/ZhdzsxB8U9VuAIB//yUveXAgMBAAECgYEAkSLdgsD1BnrcqeuJA4T5sYIqt8frPBUmO6/VlJZIx9+UGmcrQDBcR3AI5s6pyaoOdbvD88L2VVbOijdQTChgBNN9FWbUS0vu8xnJFjcg1WdZSywijkOmjE/pLMZahbSlmgKfjt/km2DUpelwYi6knAOsR2Vq3/9qZngT+ZEp8AECQQD+Rr42uKn63A3jQg32irKYaIyIxtoy5//ZTjvHdPK4CrZoOQI6efz7V6Ye5VKMfp7MpEU+oYg0gdMbl2JEB943AkEAnlZ328XOjWtTlyDJqQ6BGqRmOW/uCeN3GPsxgecdc/2n/ouQtwVfIWukTdrAuOsKebLynwU7xZrq6kvVmY4BoQJAR5CHhoUwqf73FagIoAPnQgfizbgRv1QWaQWgw4FBstSpA+pPmz+sGN1RTs7CDfKxJO1y46640/ZaASD5MiZ4vwJARpkdRhOTfkC9e6P15nf+HofwwGlkxGn0f2H4+Ae5fS87SWanNsOhYABjuQbxaUH7YLnmLfsiZIAngCIh8h58IQJAWdcZ57fw5mOaJb8rau48znvihOWPfrjp3P+f1RZ8W8qXbtCYeLVaWV/kDKqNSJjUxo9EVZP9DJ8zOaHhACIomA==
     * privateKey length = 848
     * publicKey = MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCdRYw22cS0WdEE6TBrVd0DgRFP29kB5d41deoTa2aHFCo8HqK0zou/2VSeCL0w2I0AlcfapSfs0l6vBp0i6j6BJw1ECkXLSwf8eDPosOXrZ2V5sIc3MnQIG7CgJVxilvNM9paYW+hy5BPGwnLrCdf2YXc7MQfFPVbgCAf/8lL3lwIDAQAB
     * publicKey length = 216
     * OriginalString = 00 01 02 03 04 05 06 07 08 09
     * OriginalString length = 10 byte
     * 私钥加密，公钥解密：
     * EncodeString = 18 55 69 BE B4 37 B5 2A F5 98 0F 50 D6 2D 7A 80 02 56 9F E3 42 2D 2F B3 76 62 3F 1E F2 00 A6 D6 10 B1 F4 70 97 3D 67 47 45 8C A8 74 FB 97 04 FA BC 27 EC C6 23 EA FD 42 2C 8D C8 D9 39 DD 5D 03 F2 62 12 39 F4 AE 93 AB 4F 3F 92 2A C9 71 29 1D D3 42 24 D0 EC 23 2D 45 16 D2 BE 9F 26 11 72 A0 64 E8 00 7C FA 3A 71 98 51 16 DC 76 46 68 AC 32 AC 52 8B 10 2D 06 4A 56 3D F7 26 14 05 4C 23 60
     * EncodeString length = 128 byte
     * DeCodeString = 00 01 02 03 04 05 06 07 08 09
     * 公钥加密，私钥解密：
     * EncodeString = 7A 48 AD C5 81 C4 1B D5 61 55 CF 03 6E 1E 69 81 06 62 8A A6 49 FD 54 2E 9B 50 BD E6 6C 43 74 C2 05 85 B2 2B 09 0E 7D 45 C1 94 33 24 71 18 59 4E C6 39 C8 B7 CD F0 2D 29 F4 45 E4 62 75 D1 F0 88 B5 EE 4E 64 C9 87 30 89 E1 99 D5 10 E9 E1 77 49 40 9D EE EB 9F 71 7E E6 5B 1C F9 82 9E 2B 26 7E D6 34 81 42 27 82 B2 A9 C3 7C 52 EF 69 5F 72 CA 19 91 9A 25 EB C2 96 26 75 C1 BA 5F 66 3D 88 15
     * EncodeString length = 128 byte
     * DeCodeString = 00 01 02 03 04 05 06 07 08 09
     */
    @Test
    public void testEncode() {
        byte[] data = {0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09,
                0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09,
                0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09,
                0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09,
                0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09,
                0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09,
                0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09,
                0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09,
                0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09,
                0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09,
                0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09};
//        byte[] data = {0x00, 0x01};
        String OriginalString = ConvertUtil.byte2HexStr(data);
        String privateKey = "";
        String publicKey = "";
        try {
            Map<String, Object> genKeyPair = RSAUtils.genKeyPair();
            privateKey = RSAUtils.getPrivateKey(genKeyPair);
            publicKey = RSAUtils.getPublicKey(genKeyPair);
            System.out.println("privateKey = " + privateKey);
            System.out.println("privateKey length = " + privateKey.length());
            System.out.println("publicKey = " + publicKey);
            System.out.println("publicKey length = " + publicKey.length());
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("OriginalString = " + OriginalString);
        System.out.println("OriginalString length = " + OriginalString.replace(" ", "").length() / 2 + " byte");
        try {
            System.out.println("私钥加密，公钥解密：");
            byte[] bytes = RSAUtils.encryptByPrivateKey(data, privateKey);
            String EncodeString = ConvertUtil.byte2HexStr(bytes);
            System.out.println("EncodeString = " + EncodeString);
            System.out.println("EncodeString length = " + EncodeString.replace(" ", "").length() / 2 + " byte");
            byte[] decryptByPublicKey = RSAUtils.decryptByPublicKey(bytes, publicKey);
            String DeCodeString = ConvertUtil.byte2HexStr(decryptByPublicKey);
            System.out.println("DeCodeString = " + DeCodeString);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("公钥加密，私钥解密：");
        try {
            byte[] bytes = RSAUtils.encryptByPublicKey(data, publicKey);
            String EncodeString = ConvertUtil.byte2HexStr(bytes);
            System.out.println("EncodeString = " + EncodeString);
            System.out.println("EncodeString length = " + EncodeString.replace(" ", "").length() / 2 + " byte");
            byte[] decryptByPublicKey = RSAUtils.decryptByPrivateKey(bytes, privateKey);
            String DeCodeString = ConvertUtil.byte2HexStr(decryptByPublicKey);
            System.out.println("DeCodeString = " + DeCodeString);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * /**
     * * "D973F2E8-B19E-11E2-9E96-0800200C9A66"
     * <p>
     * 11 22 33 44 55 66 33 55 77 99 88 88 11 33 55 77
     *
     * @return
     */
    @Test
    public void testCertificate() {
        byte[] CHAR_UUID_WRITE_00 = {0x11, 0x11, 0x11, 0x11, (byte) 0x96, (byte) 0xE2, 0x11, (byte) 0x9E, (byte) 0x9E, 0x11, (byte) 0xE2, (byte) 0x96, 0x11, 0x11, 0x11, 0x11};
        byte[] CHAR_UUID_WRITE_01 = {0x22, 0x22, 0x22, 0x22, (byte) 0x96, (byte) 0xE2, 0x11, (byte) 0x9E, (byte) 0x9E, 0x11, (byte) 0xE2, (byte) 0x96, 0x22, 0x22, 0x22, 0x22};
        byte[] CHAR_UUID_WRITE_02 = {0x33, 0x33, 0x33, 0x33, (byte) 0x96, (byte) 0xE2, 0x11, (byte) 0x9E, (byte) 0x9E, 0x11, (byte) 0xE2, (byte) 0x96, 0x33, 0x33, 0x33, 0x33};
        byte[] CHAR_UUID_WRITE_03 = {0x44, 0x44, 0x44, 0x44, (byte) 0x96, (byte) 0xE2, 0x11, (byte) 0x9E, (byte) 0x9E, 0x11, (byte) 0xE2, (byte) 0x96, 0x44, 0x44, 0x44, 0x44};
        byte[] CHAR_UUID_WRITE_04 = {0x55, 0x55, 0x55, 0x55, (byte) 0x96, (byte) 0xE2, 0x11, (byte) 0x9E, (byte) 0x9E, 0x11, (byte) 0xE2, (byte) 0x96, 0x55, 0x55, 0x55, 0x55};
        byte[] CHAR_UUID_WRITE_05 = {0x66, 0x66, 0x66, 0x66, (byte) 0x96, (byte) 0xE2, 0x11, (byte) 0x9E, (byte) 0x9E, 0x11, (byte) 0xE2, (byte) 0x96, 0x66, 0x66, 0x66, 0x66};
        byte[] CHAR_UUID_WRITE_06 = {0x77, 0x77, 0x77, 0x77, (byte) 0x96, (byte) 0xE2, 0x11, (byte) 0x9E, (byte) 0x9E, 0x11, (byte) 0xE2, (byte) 0x96, 0x77, 0x77, 0x77, 0x77};


        byte[] CHAR_UUID_NOTYFY = {(byte) 0x88, (byte) 0x88, (byte) 0x88, (byte) 0x88, (byte) 0x96, (byte) 0xE2, 0x11, (byte) 0x9E, (byte) 0x9E, 0x11, (byte) 0xE2, (byte) 0x96, (byte) 0x88, (byte) 0x88, (byte) 0x88, (byte) 0x88};

        System.out.println(ConvertUtil.byteServiceUUID2string(CHAR_UUID_WRITE_00));
        System.out.println(ConvertUtil.byteServiceUUID2string(CHAR_UUID_WRITE_01));
        System.out.println(ConvertUtil.byteServiceUUID2string(CHAR_UUID_WRITE_02));
        System.out.println(ConvertUtil.byteServiceUUID2string(CHAR_UUID_WRITE_03));
        System.out.println(ConvertUtil.byteServiceUUID2string(CHAR_UUID_WRITE_04));
        System.out.println(ConvertUtil.byteServiceUUID2string(CHAR_UUID_WRITE_05));
        System.out.println(ConvertUtil.byteServiceUUID2string(CHAR_UUID_WRITE_06));
        System.out.println(ConvertUtil.byteServiceUUID2string(CHAR_UUID_NOTYFY));

    }
}