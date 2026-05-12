package com.sunyard.module.storage.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


/**
 * @author yzy
 * @desc
 * @since 2025/7/21
 */
public class HashPathGeneratorUtils {
    private final String baseDirectory;


    /**
     * 构造哈希路径生成器
     * @param baseDirectory 基础存储目录
     */
    public HashPathGeneratorUtils(String baseDirectory) {
        this.baseDirectory = baseDirectory;
    }

    /**
     * 根据文件ID生成哈希分散的存储路径
     * @param fileId 文件唯一标识符
     * @param ext 文件后缀
     * @return 完整的文件存储路径
     */
    public String generatePath(String fileId,String ext) {
        // 计算文件ID的哈希值
        String hash = calculateHash(fileId);

        // 构建目录结构
        StringBuilder pathBuilder = new StringBuilder(baseDirectory);

        // 三级目录结构 (2-2-3)
        pathBuilder.append("/").append(hash.substring(0, 2));  // 第一级目录 (2个字符)
        pathBuilder.append("/").append(hash.substring(2, 4));  // 第二级目录 (2个字符)
        pathBuilder.append("/").append(hash.substring(4, 7));  // 第三级目录 (3个字符)

        // 添加文件名（使用原始ID或哈希的剩余部分）
        pathBuilder.append("/").append(fileId).append(".").append(ext);

        return pathBuilder.toString();
    }

    /**
     * 计算文件ID的哈希值
     * @param fileId 文件唯一标识符
     * @return 哈希字符串
     */
    private String calculateHash(String fileId) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(fileId.getBytes(StandardCharsets.UTF_8));

            // 转换为十六进制字符串
            StringBuilder hexString = new StringBuilder(2 * hashBytes.length);
            for (byte b : hashBytes) {
                String hex = String.format("%02x", b);
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("无法获取SHA-256算法", e);
        }
    }
}
