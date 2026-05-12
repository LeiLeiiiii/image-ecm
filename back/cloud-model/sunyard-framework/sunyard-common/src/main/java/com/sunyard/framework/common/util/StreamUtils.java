package com.sunyard.framework.common.util;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @author PJW
 */
public class StreamUtils {

    /**
     * 将 List<InputStream> 转换为 List<byte[]>
     *
     * @param inputStreams 输入流列表
     * @return Result 字节数组列表
     * @throws IOException 如果读取数据出错，则抛出 IOException 异常
     */
    public static List<byte[]> inputStreams2ByteArrays(List<InputStream> inputStreams) throws IOException {
        List<byte[]> byteArrays = new ArrayList<>();
        for (InputStream is : inputStreams) {
            BufferedInputStream bis = new BufferedInputStream(is);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len;
            try {
                while ((len = bis.read(buffer)) != -1) {
                    bos.write(buffer, 0, len);
                }
                bos.flush();
                byteArrays.add(bos.toByteArray());
            } catch (IOException e) {
                throw e;
            } finally {
                // 关闭输入流
                if (bis != null) {
                    try {
                        bis.close();
                    } catch (IOException ignored) {
                    }
                }
                // 不需要手动关闭 ByteArrayOutputStream
            }
        }
        return byteArrays;
    }

    /**
     * 将 List<byte[]> 转换为 List<InputStream>
     *
     * @param byteArrays 字节数组列表
     * @return Result 输入流列表
     */
    public static List<InputStream> byteArrays2InputStreams(List<byte[]> byteArrays) {
        List<InputStream> inputStreams = new ArrayList<>();
        for (byte[] bytes : byteArrays) {
            InputStream is = new ByteArrayInputStream(bytes);
            BufferedInputStream bis = new BufferedInputStream(is);
            inputStreams.add(bis);
        }
        return inputStreams;
    }

    /**
     * InputStream 转 byte[]
     * @param inputStream 输入流
     * @return Result
     * @throws IOException 异常
     */
    public static byte[] inputStreamToByteArray(InputStream inputStream) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int n = 0;
        while (-1 != (n = inputStream.read(buffer))) {
            output.write(buffer, 0, n);
        }
        return output.toByteArray();
    }

    /**
     *   byte[] 转 InputStream
     * @param bytes byte数组
     * @return Result
     */
    public static InputStream byteArray2InputStream(byte[] bytes) {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
        return inputStream;
    }
}
