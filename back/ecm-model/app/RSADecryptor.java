package com.sunyard.ecm.util;

import javax.crypto.Cipher;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

/**
 * 专用相机解密方法
 */
public class RSADecryptor {

    /**
     * RSA 解密方法
     * @param encryptedBase64 Base64 编码的加密数据
     * @param privateKeyPem PEM 格式的私钥
     * @return 解密后的原始字符串
     */
    public static String decryptRSA(String encryptedBase64, String privateKeyPem) {
        try {
            // 1. 清理和准备私钥
            String cleanedPrivateKey = cleanPemKey(privateKeyPem, "PRIVATE");

            // 2. Base64 解码私钥
            byte[] keyBytes = Base64.getDecoder().decode(cleanedPrivateKey);

            // 3. 创建私钥规范
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);

            // 4. 获取 RSA KeyFactory
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");

            // 5. 生成私钥
            PrivateKey privateKey = keyFactory.generatePrivate(keySpec);

            // 6. 创建解密器 (PKCS1Padding 对应 Dart 端的默认配置)
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.DECRYPT_MODE, privateKey);

            // 7. Base64 解码加密数据
            byte[] encryptedData = Base64.getDecoder().decode(encryptedBase64);

            // 8. 解密
            byte[] decryptedData = cipher.doFinal(encryptedData);

            // 9. 转换为字符串
            return new String(decryptedData, "UTF-8");

        } catch (Exception e) {
            
            throw new RuntimeException("RSA 解密失败: " + e.getMessage(), e);
        }
    }

    /**
     * 清理 PEM 格式的密钥
     * @param pemKey PEM 密钥字符串
     * @param keyType 密钥类型 ("PUBLIC" 或 "PRIVATE")
     * @return 清理后的 Base64 字符串
     */
    private static String cleanPemKey(String pemKey, String keyType) {
        // 移除 PEM 头尾标记和换行符
        String beginMarker = "-----BEGIN " + keyType + " KEY-----";
        String endMarker = "-----END " + keyType + " KEY-----";

        return pemKey.replace(beginMarker, "")
                .replace(endMarker, "")
                .replaceAll("\\s", "")  // 移除所有空白字符
                .trim();
    }

    /**
     * 验证图片 MD5
     * @param encryptedMd5Base64 加密的 MD5
     * @param imageBytes 图片字节数组
     * @param privateKeyPem 私钥
     * @return 验证结果
     */
    public static boolean verifyImageMD5(
            String encryptedMd5Base64,
            byte[] imageBytes,
            String privateKeyPem) {
        try {
            // 1. 解密 MD5
            String decryptedMd5 = decryptRSA(encryptedMd5Base64, privateKeyPem);

            // 2. 计算图片的实际 MD5
            String actualMd5 = calculateMD5(imageBytes);

            // 3. 比较
            return actualMd5.equalsIgnoreCase(decryptedMd5);

        } catch (Exception e) {
            throw new RuntimeException("验证图片 MD5 失败: " + e.getMessage(), e);
        }
    }

    /**
     * 计算 MD5
     */
    public static String calculateMD5(byte[] data) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(data);

            // 转换为十六进制字符串
            StringBuilder hexString = new StringBuilder();
            for (byte b : digest) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("计算 MD5 失败", e);
        }
    }

    /**
     * 从 EXIF 数据中提取并验证
     */
    public static VerificationResult verifyFromExif(
            String userCommentFromExif,
            byte[] imageBytes,
            String privateKeyPem) {

        try {
            // 1. 从 userComment 中提取加密的 MD5
            // Dart 端格式: "ENCRYPTED_MD5:Base64字符串"
            if (userCommentFromExif == null || !userCommentFromExif.startsWith("ENCRYPTED_MD5:")) {
                return new VerificationResult(false, null, null,"无效的 EXIF 格式");
            }

            String encryptedMd5Base64 = userCommentFromExif.substring("ENCRYPTED_MD5:".length());

            // 2. 解密 MD5
            String decryptedMd5 = decryptRSA(encryptedMd5Base64, privateKeyPem);

            // 3. 计算图片实际 MD5
            String actualMd5 = calculateMD5(imageBytes);

            // 4. 比较验证
            boolean isValid = actualMd5.equalsIgnoreCase(decryptedMd5);

            return new VerificationResult(
                    isValid,
                    actualMd5,
                    decryptedMd5,
                    isValid ? "验证成功" : "MD5 不匹配"
            );

        } catch (Exception e) {
            return new VerificationResult(false, null, null,"验证失败: "+e.getMessage());
        }
    }

    /**
     * 验证结果类
     */
    public static class VerificationResult {
        private boolean valid;
        private String actualMd5;
        private String decryptedMd5;
        private String message;

        public VerificationResult(boolean valid, String actualMd5, String decryptedMd5, String message) {
            this.valid = valid;
            this.actualMd5 = actualMd5;
            this.decryptedMd5 = decryptedMd5;
            this.message = message;
        }

        // Getter 方法
        public boolean isValid() {
            return valid;
        }

        public String getActualMd5() {
            return actualMd5;
        }

        public String getDecryptedMd5() {
            return decryptedMd5;
        }

        public String getMessage() {
            return message;
        }

        @Override
        public String toString() {
            return String.format(
                    "验证结果: %s\n实际MD5: %s\n解密MD5: %s\n消息: %s",
                    valid ? "成功" : "失败",
                    actualMd5,
                    decryptedMd5,
                    message
            );
        }
    }

    /**
     * 测试方法
     */
    public static void main(String[] args) {
        try {
            // JDK 1.8 兼容的写法
            String privateKeyPem =
                    "-----BEGIN PRIVATE KEY-----\n" +
                            "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQDA0wr3oRu105cv\n" +
                            "8T6XONv7lYtzTPIShFkTsUSSlHuyE5SwKvJysLN184WU/MOQrtuQT9x+EUoQrG/A\n" +
                            "Cjgq8AEw5N/mnrgkQLlJzUIMhnVY8+Gcw9XCuBn6EgmI7FxY6p72AwJlrKUfykx9\n" +
                            "MY1gGLhtOiL5j7ShG3E1Hz3CfZzXpTUue9BH4UD1kIDPebwqYkr30+mA/jDsvAE7\n" +
                            "TihgGe+Itp3DPM+PFxSYgRrUfpLShsiByXqDM2iX12FZvbk4U7K6aE/NoFNndz9l\n" +
                            "p9ppm10dz8GWNLye/7kI+M76KFM3d00L1+YWRb0UEQHt2kFHrZvVXvluP3+gajHy\n" +
                            "AmmHTmBxAgMBAAECggEADXtL9WGXKIS3Wb+kJMpRziKn7fszoyIMANWYceeuCNa3\n" +
                            "4wPTsPtkBsOMo8yy5zUkjo/Y3psamrUVuJ92SyE98hNUiMEw5n78fXmbjDjj8EDX\n" +
                            "9cAW/F7nGQ4FCuTctY5M6C4wbxetsV3xcGDkLbN4xJFHo/t9UXTT2tcY+DXUXxrT\n" +
                            "gXv9D4E+M2K1Dw4ij9knzPUgnJiwj+wWHFBGcUMedVUgqu6zFJJBa01I2ATx8hWJ\n" +
                            "M3HFaSt+p+EPSrQivb8eGvGFLdv3UQdwcEUBcxmKL6Al/7D6nYNjr+vSkXBukT3J\n" +
                            "R3e6HbPWTyhqbW8P6XAtm6f5O/8E/DygjTWnmmlvSQKBgQD7YPtGj0E2AorrdsNo\n" +
                            "Mc5kdQacvSFYF1JGdN9fciC7np1yQ8hmyGZIv/0yGRHNZTpKT4RMA4n79EsFDDRr\n" +
                            "FUzXH2W5792ANIOkOrD8PKyXgYt754+0HABhKgLj+q4WZKyG+d1dW5I53ChAea4j\n" +
                            "aUrh0IpxF+tNr7KRRIowp4PmuQKBgQDEXn9Fe/ZM6OaQqOihul5+vAQf7KpTSS4I\n" +
                            "DETMfTnPbeVoMky4bjsUit3WJw/VqY4KJJycda4ybFLMda62eOffZOvwAtYOHtTS\n" +
                            "70VoZ3hnZEDF/rTiC9BlFm5oWp8IvJB1gar/390nzXVsOgUdVLf+mcXsALHuSEIX\n" +
                            "K+d7eT9reQKBgGNUJvXylSnqR0pTW1NImu1G8J3ufcZ5MKF2fO0SA60fN8d20TEL\n" +
                            "7p7AfiPVlBs87JOAaB5BJ7zPAvWzpOLLP9mhJmkaRsPp/Dpglp6Uuv4CykLjdP0O\n" +
                            "M3gxSYMcAr4GecW75aSGEnmabK1NZ2nGqTghVxLpCKlhy4VrN8+R4aTBAoGAHUky\n" +
                            "lfxM2V+Ks1xdhXE0Epabzt8O7+jkEpMx14V9j7AGHKoTwxW9fezP9NZSiD0HT//x\n" +
                            "02vBlQeOuat7aP9TJX/5qAvkPKrgdqz0nwN/c3wCJQU9mNDGPa1AvhN10Gm+6adz\n" +
                            "B0Ity3fa4U8t4Bf4cOIiIHZiEvV31djYZxPIH5ECgYEAsnG55TrSDaT2C4ZGbeyy\n" +
                            "lMSYgHKAv5I/7iXymh6x7FhWACSG9V8FePGnDnc0vZtGF4UAph9MEpOCr7ETHz1w\n" +
                            "I50SiFTW/0/AqUgIetQOAXPgYnfri0BoPeKhZCIpPvNhtmUAPXzAKW5ZfeJAQIRv\n" +
                            "wveW+kg4f93ZmFy+NXmF6A8=\n" +
                            "-----END PRIVATE KEY-----\n";
            // 测试加密数据（从 Dart 端获取）
            String encryptedBase64 = "mx9jQu9vy0955w68seMGQwUVTkjs4CnEZa1kW/3OiyoceBBHYtcXZQa4REo1PLyEsSh5zbJQFlAjgJNKMcS96d4kIX4js7obVN0wbH9X5FeglOLqhyOlxhme2P3XJz1QFSNLxZeFXuEYXJ8agiEAoQe6QyGcXWKZ1k5XIdVnR1AAM50wkq9e3DNSy5j2NBDGaEd5NGddRZouHPkjGVse0JKoMY7z9CLkXg5pK+1pnaGRYaR89CuI2yV4gi+lQW0LK8RrQq25hUNobfQKhEsrN69275uvGOfgIY8sIs+Md66J889YL3jKvzLip0BsHRhugSEHBWQcqXGhqzkrwvDsyA==";

            // 解密测试
            String decrypted = decryptRSA(encryptedBase64, privateKeyPem);
            System.out.println("解密结果: " + decrypted);

        } catch (Exception e) {
            
        }
    }
}