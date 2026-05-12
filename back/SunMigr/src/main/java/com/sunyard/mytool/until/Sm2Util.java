package com.sunyard.mytool.until;

import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.asn1.gm.GMNamedCurves;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.crypto.engines.SM2Engine;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.params.ECPublicKeyParameters;
import org.bouncycastle.crypto.params.ParametersWithRandom;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPrivateKey;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPublicKey;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.spec.ECParameterSpec;
import org.bouncycastle.jce.spec.ECPrivateKeySpec;
import org.bouncycastle.math.ec.ECPoint;
import org.bouncycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.ECGenParameterSpec;
import java.util.Base64;

/**
 * @description
 */
@Slf4j
public class Sm2Util {

    public static final String PUBLIC_KEY = "048102c1388ac22b56fe45cfde5959f93b7ab15149ca7322d2a938465f9d745743a" +
            "e5de9424976343ce2fa92e8d60c84865e66ce117a8fb5d325705bd067f4031f";

    private static final String PRIVATE_KEY = "8ed4c14dfa90ae82050c668bccee89fe6e4f0453f1b66c2755e575235cefe17d";

    /**
     * 加密
     *
     * @param data 内容
     * @return Result 加密后的内容
     */
    public static String encrypt(byte[] data) {
        return encrypt(data, PUBLIC_KEY);
    }

    /**
     * 解密
     *
     * @param cipherData 加密后的byte
     * @return Result 解密后的bte
     */
    public static byte[] decrypt(byte[] cipherData) {
        return decrypt(cipherData, PRIVATE_KEY);
    }

    /**
     * SM2加密算法
     *
     * @param data            byte数组
     * @param pubKeyHexString 公钥
     * @return Result
     */
    public static String encrypt(byte[] data, String pubKeyHexString) {
        // 获取一条SM2曲线参数
        X9ECParameters sm2ECParameters = GMNamedCurves.getByName("sm2p256v1");
        // 构造ECC算法参数，曲线方程、椭圆曲线G点、大整数N
        ECDomainParameters domainParameters = new ECDomainParameters(sm2ECParameters.getCurve(), sm2ECParameters.getG(), sm2ECParameters.getN());
        //提取公钥点
        ECPoint pukPoint = sm2ECParameters.getCurve().decodePoint(Hex.decode(pubKeyHexString));
        // 公钥前面的02或者03表示是压缩公钥，04表示未压缩公钥, 04的时候，可以去掉前面的04
        ECPublicKeyParameters publicKeyParameters = new ECPublicKeyParameters(pukPoint, domainParameters);

        SM2Engine sm2Engine = new SM2Engine(SM2Engine.Mode.C1C3C2);
        // 设置sm2为加密模式
        sm2Engine.init(true, new ParametersWithRandom(publicKeyParameters, new SecureRandom()));

        byte[] arrayOfBytes = null;
        try {
            arrayOfBytes = sm2Engine.processBlock(data, 0, data.length);
        } catch (Exception e) {
            log.error(e.toString());
            throw new RuntimeException(e);
        }
        // 将密文进行Base64编码，方便传输
        return Hex.toHexString(arrayOfBytes);

    }

    /**
     * SM2解密算法
     *
     * @param cipherData      密文数据
     * @param priKeyHexString 私钥（16进制字符串）
     * @return Result
     */
    public static byte[] decrypt(byte[] cipherData, String priKeyHexString) {
        //获取一条SM2曲线参数
        X9ECParameters sm2ECParameters = GMNamedCurves.getByName("sm2p256v1");
        //构造domain参数
        ECDomainParameters domainParameters = new ECDomainParameters(sm2ECParameters.getCurve(), sm2ECParameters.getG(), sm2ECParameters.getN());

        BigInteger privateKeyD = new BigInteger(priKeyHexString, 16);
        ECPrivateKeyParameters privateKeyParameters = new ECPrivateKeyParameters(privateKeyD, domainParameters);

        SM2Engine sm2Engine = new SM2Engine(SM2Engine.Mode.C1C3C2);
        // 设置sm2为解密模式
        sm2Engine.init(false, privateKeyParameters);

        byte[] arrayOfBytes = null;
        try {
            arrayOfBytes = sm2Engine.processBlock(cipherData, 0, cipherData.length);
        } catch (Exception e) {
            log.error(e.toString());
            throw new RuntimeException(e);
        }
        return arrayOfBytes;
    }

    /**
     * 生成 SM2 公私钥对
     *
     * @return Result
     * @throws NoSuchAlgorithmException           异常
     * @throws InvalidAlgorithmParameterException 异常
     */
    public static KeyPair geneSM2KeyPair() throws NoSuchAlgorithmException, InvalidAlgorithmParameterException {
        final ECGenParameterSpec sm2Spec = new ECGenParameterSpec("sm2p256v1");
        // 获取一个椭圆曲线类型的密钥对生成器
        final KeyPairGenerator kpg = KeyPairGenerator.getInstance("EC", new BouncyCastleProvider());
        // 产生随机数
        SecureRandom secureRandom = new SecureRandom();
        // 使用SM2参数初始化生成器
        kpg.initialize(sm2Spec, secureRandom);
        // 获取密钥对
        KeyPair keyPair = kpg.generateKeyPair();
        return keyPair;
    }

    /**
     * 生产hex秘钥对
     */
    public static void geneSM2HexKeyPair() {
        try {
            KeyPair keyPair = geneSM2KeyPair();
            PrivateKey privateKey = keyPair.getPrivate();
            PublicKey publicKey = keyPair.getPublic();
        } catch (Exception e) {
            log.error(e.toString());
            throw new RuntimeException(e);
        }
    }

    /**
     * 获取私钥（16进制字符串，头部不带00长度共64）
     *
     * @param privateKey 私钥PrivateKey型
     * @return Result
     */
    public static String getPriKeyHexString(PrivateKey privateKey) {
        BCECPrivateKey key = (BCECPrivateKey) privateKey;
        BigInteger intPrivateKey = key.getD();
        String priKeyHexString = intPrivateKey.toString(16);
        return priKeyHexString;
    }

    /**
     * 获取私钥 base64字符串
     *
     * @param privateKey 私钥PrivateKey型
     * @return Result
     */
    public static String getPriKeyBase64String(PrivateKey privateKey) {
        return new String(Base64.getEncoder().encode(privateKey.getEncoded()), StandardCharsets.UTF_8);
    }

    /**
     * 获取公钥（16进制字符串，头部带04长度共130）
     *
     * @param publicKey 公钥PublicKey型
     * @return Result
     */
    public static String getPubKeyHexString(PublicKey publicKey) {
        BCECPublicKey key = (BCECPublicKey) publicKey;
        return Hex.toHexString(key.getQ().getEncoded(false));
    }

    /**
     * 获取公钥 base64字符串
     *
     * @param publicKey 公钥PublicKey型
     * @return Result
     */
    public static String getPubKeyBase64String(PublicKey publicKey) {
        return new String(Base64.getEncoder().encode(publicKey.getEncoded()), StandardCharsets.UTF_8);
    }

    /**
     * 私钥生成公钥
     *
     * @param priKeyHexString 私钥Hex格式，必须64位
     * @return Result 公钥Hex格式，04开头，130位
     * @throws Exception 异常
     */
    public static String getPubKeyByPriKey(String priKeyHexString) throws Exception {
        if (priKeyHexString == null || priKeyHexString.length() != 64) {
            System.err.println("priKey 必须是Hex 64位格式，例如：11d0a44d47449d48d614f753ded6b06af76033b9c3a2af2b8b2239374ccbce3a");
            return "";
        }
        String pubKeyHexString = null;
        X9ECParameters sm2ECParameters = GMNamedCurves.getByName("sm2p256v1");
        //构造domain参数
        BigInteger privateKeyD = new BigInteger(priKeyHexString, 16);

        ECParameterSpec ecParameterSpec = new ECParameterSpec(sm2ECParameters.getCurve(), sm2ECParameters.getG(), sm2ECParameters.getN());
        ECPrivateKeySpec ecPrivateKeySpec = new ECPrivateKeySpec(privateKeyD, ecParameterSpec);
        PrivateKey privateKey = null;
        privateKey = KeyFactory.getInstance("EC", new BouncyCastleProvider()).generatePrivate(ecPrivateKeySpec);

        // 临时解决办法
        String pointString = privateKey.toString();
        String pointStringX = pointString.substring(pointString.indexOf("X: ") + "X: ".length(), pointString.indexOf("Y: ")).trim();
        String pointStringY = pointString.substring(pointString.indexOf("Y: ") + "Y: ".length()).trim();

        pubKeyHexString = "04" + pointStringX + pointStringY;
        return pubKeyHexString;

    }
}
