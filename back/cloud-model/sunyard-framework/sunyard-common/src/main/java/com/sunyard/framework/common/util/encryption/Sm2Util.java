package com.sunyard.framework.common.util.encryption;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.Signature;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1TaggedObject;
import org.bouncycastle.asn1.DERBitString;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.gm.GMNamedCurves;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.asn1.x9.X9ObjectIdentifiers;
import org.bouncycastle.crypto.engines.SM2Engine;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.params.ECPublicKeyParameters;
import org.bouncycastle.crypto.params.ParametersWithRandom;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPrivateKey;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPublicKey;
import org.bouncycastle.jcajce.spec.SM2ParameterSpec;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.spec.ECParameterSpec;
import org.bouncycastle.jce.spec.ECPrivateKeySpec;
import org.bouncycastle.jce.spec.ECPublicKeySpec;
import org.bouncycastle.math.ec.ECPoint;
import org.bouncycastle.util.encoders.Hex;

/**
 * @author P-JWei
 * @date 2024/1/22 14:19:49
 * @title
 * @description
 */
@Slf4j
public class Sm2Util {

    static {
        Security.addProvider(new BouncyCastleProvider());
        // 添加BouncyCastle作为安全提供者
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    public static final String PUBLIC_KEY = "048102c1388ac22b56fe45cfde5959f93b7ab15149ca7322d2a938465f9d745743a" +
            "e5de9424976343ce2fa92e8d60c84865e66ce117a8fb5d325705bd067f4031f";

    private static final String PRIVATE_KEY = "8ed4c14dfa90ae82050c668bccee89fe6e4f0453f1b66c2755e575235cefe17d";

    // SM2标准用户ID
    private static final byte[] DEFAULT_USER_ID = "1234567812345678".getBytes(StandardCharsets.UTF_8);

    // SM2 OID 常量，兼容不同版本的Bouncy Castle
    private static final ASN1ObjectIdentifier SM2_OID = new ASN1ObjectIdentifier("1.2.156.10197.1.301");
    private static final ASN1ObjectIdentifier SM2_SIGN_OID = new ASN1ObjectIdentifier("1.2.156.10197.1.301.1");

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

    public static String decrypt(String cipherData) {
        byte[] cipherBytes = Hex.decode(cipherData);
        byte[] decrypt = decrypt(cipherBytes, PRIVATE_KEY);
        return new String(decrypt);
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
            log.error("系统异常",e);
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
            log.error("系统异常",e);
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
            log.error("系统异常",e);
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

    /**
     *  获取sm2 密钥串
     * @return
     * @throws Exception
     */
    public static Map<String, Object> generateRsaKeyPairs() throws Exception {
        Map<String, Object> keyPairMap = new HashMap<String, Object>(6);
        KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("EC", "BC");
        keyPairGen.initialize(new ECGenParameterSpec("sm2p256v1"));
        KeyPair keyPair = keyPairGen.generateKeyPair();
        PublicKey publicKey = keyPair.getPublic();
        PrivateKey privateKey = keyPair.getPrivate();
        keyPairMap.put("publicKey", org.apache.commons.codec.binary.Base64.encodeBase64String(publicKey.getEncoded()));
        keyPairMap.put("privateKey", org.apache.commons.codec.binary.Base64.encodeBase64String(privateKey.getEncoded()));
        return keyPairMap;
    }


    /**
     * 使用JCA标准API实现的SM2签名（修复版）
     * @param privateKeyPem PEM格式的SM2私钥
     * @param content 要签名的内容
     * @return Base64编码的签名结果
     */
    public static String signWithSM2Standard(String content, String privateKeyPem) {
        try {
            log.debug("开始SM2签名，内容长度: {}", content.length());

            // 1. 解析私钥 - 使用修复后的解析方法
            PrivateKey privateKey = parseSm2PrivateKeyRobust(privateKeyPem);
            log.debug("私钥解析成功，类型: {}", privateKey.getClass().getSimpleName());

            // 2. 初始化签名
            Signature signature = Signature.getInstance("SM3withSM2", BouncyCastleProvider.PROVIDER_NAME);

            // 3. 正确设置用户ID - 使用SM2ParameterSpec
            signature.setParameter(new SM2ParameterSpec(DEFAULT_USER_ID));

            // 4. 初始化并更新签名
            signature.initSign(privateKey);
            signature.update(content.getBytes(StandardCharsets.UTF_8));

            // 5. 生成签名
            byte[] signed = signature.sign();

            // 6. 返回Base64编码
            String result = java.util.Base64.getEncoder().encodeToString(signed);
            log.debug("SM2签名成功，签名长度: {}", signed.length);
            return result;
        } catch (Exception e) {
            log.error("SM2签名失败，私钥前50字符: {}",
                    privateKeyPem.substring(0, Math.min(50, privateKeyPem.replaceAll("\\s", "").length())), e);
            throw new RuntimeException("SM2签名失败" ,e);
        }
    }

    /**
     * SM2验签方法
     * @param content 原始内容
     * @param signatureBase64 Base64编码的签名
     * @param publicKeyPem PEM格式的SM2公钥
     * @return 验签是否成功
     */
    public static boolean sm2CheckContent(String content, String signatureBase64, String publicKeyPem) {
        try {
            log.debug("开始SM2验签，内容长度: {}", content.length());

            // 1. 解析公钥
            PublicKey publicKey = parseSm2PublicKeyRobust(publicKeyPem);
            log.debug("公钥解析成功，类型: {}", publicKey.getClass().getSimpleName());

            // 2. 初始化签名验证
            Signature signature = Signature.getInstance("SM3withSM2", BouncyCastleProvider.PROVIDER_NAME);

            // 3. 设置用户ID
            signature.setParameter(new SM2ParameterSpec(DEFAULT_USER_ID));

            // 4. 初始化验证
            signature.initVerify(publicKey);
            signature.update(content.getBytes(StandardCharsets.UTF_8));

            // 5. 验证签名
            byte[] signatureBytes = decodeBase64(signatureBase64);
            boolean result = signature.verify(signatureBytes);

            log.debug("SM2验签完成，结果: {}", result);
            return result;
        } catch (Exception e) {
            log.error("SM2验签失败，公钥前50字符: {}",
                    publicKeyPem.substring(0, Math.min(50, publicKeyPem.replaceAll("\\s", "").length())), e);
            return false;
        }
    }

    /**
     * 健壮的SM2公钥解析方法，支持多种公钥格式
     */
    private static PublicKey parseSm2PublicKeyRobust(String publicKeyPem) throws Exception {
        // 1. 清理PEM格式
        String cleanPem = publicKeyPem
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replace("-----BEGIN EC PUBLIC KEY-----", "")
                .replace("-----END EC PUBLIC KEY-----", "")
                .replaceAll("\\s", "");

        log.debug("清理后的公钥(前50字符): {}", cleanPem.substring(0, Math.min(50, cleanPem.length())));

        // 2. Base64解码
        byte[] keyBytes = decodeBase64(cleanPem);

        // 3. 尝试多种解析策略
        try {
            // 策略1: 尝试标准X.509格式
            return tryX509Format(keyBytes);
        } catch (Exception e1) {
            log.debug("X.509格式解析失败，尝试SubjectPublicKeyInfo格式: {}", e1.getMessage());

            try {
                // 策略2: 尝试SubjectPublicKeyInfo格式
                return trySubjectPublicKeyInfoFormat(keyBytes);
            } catch (Exception e2) {
                log.debug("SubjectPublicKeyInfo格式解析失败，尝试裸公钥格式: {}", e2.getMessage());

                try {
                    // 策略3: 尝试裸公钥点
                    return tryRawPublicKeyExtraction(keyBytes);
                } catch (Exception e3) {
                    log.error("所有公钥解析策略都失败，原始错误: {}", e1.getMessage());
                    log.error("SubjectPublicKeyInfo错误: {}", e2.getMessage());
                    log.error("裸公钥错误: {}", e3.getMessage());
                    throw new RuntimeException("无法解析SM2公钥，请检查公钥格式", e3);
                }
            }
        }
    }

    /**
     * 策略1: 尝试X.509格式解析
     */
    private static PublicKey tryX509Format(byte[] keyBytes) throws Exception {
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("EC", BouncyCastleProvider.PROVIDER_NAME);
        return keyFactory.generatePublic(keySpec);
    }

    /**
     * 策略2: 尝试SubjectPublicKeyInfo格式解析
     */
    private static PublicKey trySubjectPublicKeyInfoFormat(byte[] keyBytes) throws Exception {
        SubjectPublicKeyInfo spki = SubjectPublicKeyInfo.getInstance(keyBytes);

        // 检查是否为SM2算法
        AlgorithmIdentifier algId = spki.getAlgorithm();
        if (!isSm2Algorithm(algId.getAlgorithm())) {
            log.warn("公钥算法不是SM2: {}", algId.getAlgorithm());
        }

        // 使用Bouncy Castle解析
        KeyFactory keyFactory = KeyFactory.getInstance("EC", BouncyCastleProvider.PROVIDER_NAME);
        return keyFactory.generatePublic(new X509EncodedKeySpec(spki.getEncoded()));
    }

    /**
     * 策略3: 尝试裸公钥点解析
     */
    private static PublicKey tryRawPublicKeyExtraction(byte[] keyBytes) throws Exception {
        // 处理可能的裸公钥格式
        byte[] pubKeyBytes = keyBytes;

        // 如果是64字节，添加04前缀（未压缩格式）
        if (pubKeyBytes.length == 64) {
            byte[] temp = new byte[65];
            temp[0] = 0x04;
            System.arraycopy(pubKeyBytes, 0, temp, 1, 64);
            pubKeyBytes = temp;
        }
        // 如果是65字节但没有04前缀，添加前缀
        else if (pubKeyBytes.length == 65 && pubKeyBytes[0] != 0x04) {
            byte[] temp = new byte[65];
            temp[0] = 0x04;
            System.arraycopy(pubKeyBytes, 0, temp, 1, 64);
            pubKeyBytes = temp;
        }
        // 尝试Base64解码
        else if (pubKeyBytes.length > 65) {
            try {
                byte[] decoded = decodeBase64(new String(pubKeyBytes));
                if (decoded.length == 64) {
                    byte[] temp = new byte[65];
                    temp[0] = 0x04;
                    System.arraycopy(decoded, 0, temp, 1, 64);
                    pubKeyBytes = temp;
                } else {
                    pubKeyBytes = decoded;
                }
            } catch (Exception e) {
                // 忽略，使用原始字节
            }
        }

        // 验证公钥长度
        if (pubKeyBytes.length != 65 || pubKeyBytes[0] != 0x04) {
            throw new IllegalArgumentException("无效的SM2公钥格式，期望65字节未压缩格式");
        }

        // 1. 获取SM2曲线
        X9ECParameters sm2Curve = GMNamedCurves.getByName("sm2p256v1");
        if (sm2Curve == null) {
            throw new RuntimeException("未找到SM2曲线参数");
        }

        // 2. 创建EC参数规范
        ECParameterSpec ecSpec = new ECParameterSpec(
                sm2Curve.getCurve(),
                sm2Curve.getG(),
                sm2Curve.getN(),
                sm2Curve.getH()
        );

        // 3. 解析公钥点
        ECPoint point = sm2Curve.getCurve().decodePoint(pubKeyBytes);

        // 4. 创建公钥
        ECPublicKeySpec pubKeySpec = new ECPublicKeySpec(point, ecSpec);
        KeyFactory keyFactory = KeyFactory.getInstance("EC", BouncyCastleProvider.PROVIDER_NAME);
        return keyFactory.generatePublic(pubKeySpec);
    }

    /**
     * 兼容的Base64解码
     */
    private static byte[] decodeBase64(String base64Str) {
        try {
            // 尝试标准Base64解码
            return org.bouncycastle.util.encoders.Base64.decode(base64Str);
        } catch (Exception e) {
            try {
                // 尝试JDK8+的Base64解码
                return java.util.Base64.getDecoder().decode(base64Str);
            } catch (Exception e2) {
                // 尝试处理URL安全的Base64
                String fixed = base64Str.replace('-', '+').replace('_', '/');
                // 确保长度是4的倍数
                int mod4 = fixed.length() % 4;
                if (mod4 > 0) {
                    fixed += "====".substring(mod4);
                }
                try {
                    return java.util.Base64.getDecoder().decode(fixed);
                } catch (Exception e3) {
                    throw new IllegalArgumentException("无效的Base64编码: " + base64Str, e3);
                }
            }
        }
    }

    /**
     * 健壮的SM2私钥解析方法，支持多种私钥格式
     */
    private static PrivateKey parseSm2PrivateKeyRobust(String privateKeyPem) throws Exception {
        // 1. 清理PEM格式
        String cleanPem = privateKeyPem
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replace("-----BEGIN EC PRIVATE KEY-----", "")
                .replace("-----END EC PRIVATE KEY-----", "")
                .replace("-----BEGIN RSA PRIVATE KEY-----", "")
                .replace("-----END RSA PRIVATE KEY-----", "")
                .replace("-----BEGIN ENCRYPTED PRIVATE KEY-----", "")
                .replace("-----END ENCRYPTED PRIVATE KEY-----", "")
                .replaceAll("\\s", "");

        log.debug("清理后的私钥(前50字符): {}", cleanPem.substring(0, Math.min(50, cleanPem.length())));

        // 2. Base64解码
        byte[] keyBytes = decodeBase64(cleanPem);

        // 3. 尝试多种解析策略
        try {
            // 策略1: 尝试标准PKCS#8格式
            return tryPkcs8Format(keyBytes);
        } catch (Exception e1) {
            log.debug("PKCS#8格式解析失败，尝试SM2特定格式: {}", e1.getMessage());

            try {
                // 策略2: 尝试PKCS#1格式 (常见国密私钥格式)
                return tryPkcs1Format(keyBytes);
            } catch (Exception e2) {
                log.debug("PKCS#1格式解析失败，尝试裸私钥格式: {}", e2.getMessage());

                try {
                    // 策略3: 尝试Bouncy Castle内部解析
                    return tryBcInternalParser(keyBytes);
                } catch (Exception e3) {
                    log.error("所有私钥解析策略都失败，原始错误: {}", e1.getMessage());
                    log.error("PKCS#1错误: {}", e2.getMessage());
                    log.error("BC内部错误: {}", e3.getMessage());
                    throw new RuntimeException("无法解析SM2私钥，请检查私钥格式", e3);
                }
            }
        }
    }

    /**
     * 策略1: 尝试PKCS#8格式解析
     */
    private static PrivateKey tryPkcs8Format(byte[] keyBytes) throws Exception {
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("EC", BouncyCastleProvider.PROVIDER_NAME);
        return keyFactory.generatePrivate(keySpec);
    }

    /**
     * 策略2: 尝试PKCS#1格式解析 (国密常用)
     */
    private static PrivateKey tryPkcs1Format(byte[] keyBytes) throws Exception {
        // 尝试解析ASN.1结构
        ASN1Sequence seq = ASN1Sequence.getInstance(keyBytes);

        // SM2私钥通常有两种格式:
        // 1. 简单格式: [version, privateKey, parameters, publicKey]
        // 2. 复杂格式: 可能包含更多字段

        if (seq.size() < 2) {
            throw new IllegalArgumentException("无效的PKCS#1私钥格式，元素数量: " + seq.size());
        }

        BigInteger version = null;
        BigInteger privateKeyValue = null;
        ASN1Object parameters = null;

        // 解析序列中的元素
        for (int i = 0; i < seq.size(); i++) {
            ASN1Encodable element = seq.getObjectAt(i);
            if (element instanceof ASN1Integer) {
                BigInteger value = ((ASN1Integer) element).getValue();
                if (i == 0) {
                    version = value; // 版本通常在第一位
                } else if (privateKeyValue == null) {
                    privateKeyValue = value; // 私钥值
                }
            } else if (element instanceof ASN1ObjectIdentifier || element instanceof ASN1TaggedObject) {
                // 可能是参数
                parameters = (ASN1Object) element;
            } else if (element instanceof DERBitString || element instanceof DEROctetString) {
                // 可能是公钥
            }
        }

        if (privateKeyValue == null) {
            throw new IllegalArgumentException("无法从PKCS#1格式中提取私钥值");
        }

        // 确保私钥值是正数且符合SM2曲线要求
        if (privateKeyValue.signum() < 0) {
            privateKeyValue = privateKeyValue.abs();
        }

        // 创建SM2参数规范
        ECParameterSpec ecSpec = createSm2ECParameterSpec();

        // 创建私钥
        ECPrivateKeySpec privateKeySpec = new ECPrivateKeySpec(privateKeyValue, ecSpec);
        KeyFactory keyFactory = KeyFactory.getInstance("EC", BouncyCastleProvider.PROVIDER_NAME);
        return keyFactory.generatePrivate(privateKeySpec);
    }

    /**
     * 策略3: 使用Bouncy Castle内部API解析
     */
    private static PrivateKey tryBcInternalParser(byte[] keyBytes) throws Exception {
        try {
            // 尝试作为PrivateKeyInfo解析
            PrivateKeyInfo pki = PrivateKeyInfo.getInstance(keyBytes);
            AlgorithmIdentifier algId = pki.getPrivateKeyAlgorithm();

            // 检查是否为SM2算法或EC算法 - 使用兼容方式检查OID
            if (isSm2Algorithm(algId.getAlgorithm())) {
                // 获取BC私钥工厂
                Class<?> keyFactorySpiClass = Class.forName("org.bouncycastle.jcajce.provider.asymmetric.ec.KeyFactorySpi");
                Constructor<?> constructor = keyFactorySpiClass.getConstructor();
                Object keyFactorySpi = constructor.newInstance();

                // 调用engineGeneratePrivate方法
                Method engineGeneratePrivateMethod = keyFactorySpiClass.getDeclaredMethod(
                        "engineGeneratePrivate", PrivateKeyInfo.class);
                engineGeneratePrivateMethod.setAccessible(true);

                return (PrivateKey) engineGeneratePrivateMethod.invoke(keyFactorySpi, pki);
            }

            throw new IllegalArgumentException("不支持的算法: " + algId.getAlgorithm());

        } catch (Exception e) {
            // 最后尝试：直接提取私钥值
            return tryRawPrivateKeyExtraction(keyBytes);
        }
    }

    /**
     * 检查是否为SM2相关算法OID，兼容不同版本的Bouncy Castle
     */
    private static boolean isSm2Algorithm(ASN1ObjectIdentifier algorithm) {
        // 检查标准SM2 OID: 1.2.156.10197.1.301
        if (algorithm.equals(SM2_OID)) {
            return true;
        }

        // 检查SM2签名算法OID: 1.2.156.10197.1.301.1
        if (algorithm.equals(SM2_SIGN_OID)) {
            return true;
        }

        // 检查是否为EC公钥类型，这可能是SM2
        if (algorithm.equals(X9ObjectIdentifiers.id_ecPublicKey)) {
            return true;
        }

        // 检查GMObjectIdentifiers中可能的其他SM2相关OID
        // 检查常见的SM2曲线名称
        String algorithmId = algorithm.getId();
        if (algorithmId.startsWith("1.2.156.10197.1.301")) {
            return true;
        }

        return false;
    }

    /**
     * 尝试直接提取私钥值（最后的手段）
     */
    private static PrivateKey tryRawPrivateKeyExtraction(byte[] keyBytes) throws Exception {
        BigInteger d = null;

        // 尝试多种方式提取私钥值
        try {
            // 方式1: 作为ASN1Integer
            d = ASN1Integer.getInstance(keyBytes).getValue();
        } catch (Exception e1) {
            try {
                // 方式2: 作为DEROctetString
                byte[] octets = DEROctetString.getInstance(keyBytes).getOctets();
                d = new BigInteger(1, octets);
            } catch (Exception e2) {
                // 方式3: 直接作为BigInteger
                d = new BigInteger(1, keyBytes);

                // 确保是32字节（SM2私钥长度）
                if (d.bitLength() > 256) {
                    d = d.mod(new BigInteger("FFFFFFFEFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF00000000FFFFFFFFFFFFFFFF", 16));
                }
            }
        }

        // 创建SM2参数
        ECParameterSpec ecSpec = createSm2ECParameterSpec();

        // 创建私钥
        ECPrivateKeySpec privateKeySpec = new ECPrivateKeySpec(d, ecSpec);
        KeyFactory keyFactory = KeyFactory.getInstance("EC", BouncyCastleProvider.PROVIDER_NAME);
        return keyFactory.generatePrivate(privateKeySpec);
    }

    /**
     * 创建SM2标准参数规范
     */
    private static ECParameterSpec createSm2ECParameterSpec() throws Exception {
        // 1. 获取SM2曲线参数
        X9ECParameters sm2Curve = GMNamedCurves.getByName("sm2p256v1");
        if (sm2Curve == null) {
            throw new RuntimeException("未找到SM2曲线参数，确保Bouncy Castle版本>=1.57");
        }

        // 2. 创建EC参数规范
        return new ECParameterSpec(
                sm2Curve.getCurve(),
                sm2Curve.getG(),
                sm2Curve.getN(),
                sm2Curve.getH()
        );
    }

    // 保留原方法，添加兼容性
    @Deprecated
    private static BCECPrivateKey parseSm2PrivateKey(String privateKeyPem) throws Exception {
        return (BCECPrivateKey) parseSm2PrivateKeyRobust(privateKeyPem);
    }

    // 保留原方法签名
    @Deprecated
    public static String signWithSM2(String privateKey, String content) {
        return signWithSM2Standard(privateKey, content);
    }
}
