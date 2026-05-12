package com.sunyard.mytool.until;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Slf4j
public class MD5Util {



    /**
     * 根据流计算MD5
     * @param inputStream
     * @return
     */
    public static String getMD5(InputStream inputStream) {
        long handleFilesTime = System.currentTimeMillis();
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                md.update(buffer, 0, bytesRead);
            }
            byte[] digest = md.digest();
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            String result = sb.toString();
            log.info("计算MD5完成，结果: {}, 耗时: {} ms", result, System.currentTimeMillis() - handleFilesTime);
            return result;
        } catch (Exception e) {
            throw new RuntimeException("根据输入流计算MD5失败 ", e);
        }
    }


    public static String getMD5ByFileContent(String filePath) throws Exception {
        long handleFilesTime = System.currentTimeMillis();
        Path path = Paths.get(filePath);
        try (FileChannel fileChannel = FileChannel.open(path)) {
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            ByteBuffer buffer = ByteBuffer.allocate(1024 * 1024);

            while (fileChannel.read(buffer) > 0 || buffer.position() > 0) {
                buffer.flip(); // Prepare the buffer for reading
                messageDigest.update(buffer); // Update the digest with the buffer's content
                buffer.compact(); // Clear the buffer for the next read operation
            }
            log.info(filePath + ":" + fileChannel.size() + "*getMD5ByFileContent处理文件MD5码*耗时:{}", System.currentTimeMillis() - handleFilesTime);
            return toHexString(messageDigest.digest());
        } catch (IOException | NoSuchAlgorithmException e) {
            throw new Exception("根据文件内容获取文件[" + filePath + "]MD5码异常!", e);
        }

    }

    public static String toHexString(byte[] b) {
        StringBuilder sb = new StringBuilder(b.length * 2);
        for (int i = 0; i < b.length; i++) {
            sb.append(hexChar[(b[i] & 0xf0) >>> 4]);
            sb.append(hexChar[b[i] & 0x0f]);
        }
        return sb.toString();
    }

    private static String[] hexChar = {"0", "1", "2", "3", "4", "5", "6", "7",
            "8", "9", "a", "b", "c", "d", "e", "f"};

}
