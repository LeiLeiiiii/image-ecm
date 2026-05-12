package com.sunyard.ecm.util;

import com.sunyard.framework.common.util.encryption.AesUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Value;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * @author P-JWei
 * @date 2023/12/4 10:35:35
 * @title
 * @description 文件加密工具类
 */
@Slf4j
public class FileEncryptUtils {

    /**
     * 字节加密最大范围：单位MB
     */
    @Value("${upload.encrypt.bytes-max-size:200}")
    private static Integer BYTES_MAX_SIZE = 200;

    /**
     * 加密最大范围： 单位KB
     */
    @Value("${upload.encrypt.part-encrypt-size:8}")
    private static Integer PART_ENCRYPT_SIZE = 8;

    /**
     * 加密文件
     *
     * @param oldInputStream 源文件流
     * @param encryptKey     密钥
     * @param encryptIndex   加密标识符
     * @return
     * @throws IOException
     */
    public static InputStream encrypt(InputStream oldInputStream, String encryptKey, String encryptIndex) {
        InputStream inputStream = null;
        try {
            if (oldInputStream.available() == 0) {
                log.debug("源文件流oldInputStream为空");
                return null;
            }
            //小于BYTES_MAX_SIZE 走字节加密；大于走流加密
            if (oldInputStream.available() < BYTES_MAX_SIZE * 1024 * 1024) {
                inputStream = encryptBytes(oldInputStream, encryptKey, encryptIndex);
            } else {
                inputStream = encryptStream(oldInputStream, encryptKey, encryptIndex);
            }
        } catch (IOException e) {
            log.error("加密失败：{}", e);
        } catch (IllegalBlockSizeException e) {
            log.error("加解密失败:{}", e);
        } catch (BadPaddingException e) {
            log.error("加解密失败:{}", e);
        } catch (NoSuchPaddingException e) {
            log.error("填充方式错误:{}", e);
        } catch (NoSuchAlgorithmException e) {
            log.error("加密方式错误:{}", e);
        } catch (InvalidKeyException e) {
            log.error("密钥错误:{}", e);
        }
        return inputStream;
    }

    /**
     * 解密文件
     *
     * @param oldInputStream 加密后文件流
     * @param encryptKey     密钥
     * @param encryptIndex   标识符
     * @return
     * @throws IOException
     */
    public static InputStream decrypt(InputStream oldInputStream, String encryptKey, String encryptIndex) {
        InputStream inputStream = null;
        try {
            if (oldInputStream.available() == 0) {
                log.debug("加密文件流oldInputStream为空");
                return null;
            }
            inputStream = decryptStreamOrBytes(oldInputStream, encryptKey, encryptIndex);
        } catch (IllegalBlockSizeException e) {
            log.error("加解密失败:{}", e);
        } catch (BadPaddingException e) {
            log.error("加解密失败:{}", e);
        } catch (NoSuchPaddingException e) {
            log.error("填充方式错误:{}", e);
        } catch (NoSuchAlgorithmException e) {
            log.error("加密方式错误:{}", e);
        } catch (InvalidKeyException e) {
            log.error("密钥错误:{}", e);
        } catch (IOException e) {
            log.error("加密失败：{}", e);
        }
        return inputStream;
    }


    /**
     * 字节加密
     *
     * @param oldInputStream 源文件流
     * @param encryptKey     密钥
     * @param encryptIndex   加密标识符
     * @return
     * @throws IOException
     * @throws IllegalBlockSizeException
     * @throws NoSuchPaddingException
     * @throws BadPaddingException
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeyException
     */
    private static InputStream encryptBytes(InputStream oldInputStream, String encryptKey, String encryptIndex) throws IOException, IllegalBlockSizeException, NoSuchPaddingException, BadPaddingException, NoSuchAlgorithmException, InvalidKeyException {
        //读取整个文件的byte[]数组
        byte[] bytes = IOUtils.toByteArray(oldInputStream);
        //输出流
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        //如果字节小于PART_ENCRYPT_SIZE整个文件加密；如果大于则只截取前PART_ENCRYPT_SIZE部分进行加密
        if (bytes.length < PART_ENCRYPT_SIZE * 1024) {
            //加密整个文件字节
            String encrypt = AesUtils.encrypt(bytes, encryptKey);
            //写入加密数据
            outputStream.write(encrypt.getBytes(StandardCharsets.UTF_8));
            //写入标识符
            outputStream.write(encryptIndex.getBytes(StandardCharsets.UTF_8));
        } else {
            //只取前PART_ENCRYPT_SIZE * 1024位进行加密
            byte[] needEncrypt = Arrays.copyOfRange(bytes, 0, (PART_ENCRYPT_SIZE * 1024));
            String encrypt = AesUtils.encrypt(needEncrypt, encryptKey);
            //后面不加密
            byte[] noNeedEncrypt = Arrays.copyOfRange(bytes, (PART_ENCRYPT_SIZE * 1024), bytes.length);
            //写入加密数据
            outputStream.write(encrypt.getBytes(StandardCharsets.UTF_8));
            //写入标识符
            outputStream.write(encryptIndex.getBytes(StandardCharsets.UTF_8));
            //写入未加密
            outputStream.write(noNeedEncrypt);
        }
        outputStream.close();
        return convertByteArrayToInputStream(outputStream);

    }

    /**
     * 流加密
     *
     * @param oldInputStream 源文件流
     * @param encryptKey     密钥
     * @param encryptIndex   加密标识符
     * @return
     * @throws IOException
     * @throws IllegalBlockSizeException
     * @throws NoSuchPaddingException
     * @throws BadPaddingException
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeyException
     */
    private static InputStream encryptStream(InputStream oldInputStream, String encryptKey, String encryptIndex) throws IOException, IllegalBlockSizeException, NoSuchPaddingException, BadPaddingException, NoSuchAlgorithmException, InvalidKeyException {
        //输出流
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        //截取配置加密范围进行加密
        byte[] part = new byte[PART_ENCRYPT_SIZE * 1024];
        oldInputStream.read(part);
        String encrypt = AesUtils.encrypt(part, encryptKey);
        //写入加密数据
        outputStream.write(encrypt.getBytes(StandardCharsets.UTF_8));
        //写入标识符
        outputStream.write(encryptIndex.getBytes(StandardCharsets.UTF_8));
        //写入未加密的部分
        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = oldInputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
        }
        outputStream.close();
        oldInputStream.close();
        //返回处理后的inputStream
        return convertByteArrayToInputStream(outputStream);
    }

    /**
     * 解密（字节加密、流加密解密方法一样）
     *
     * @param oldInputStream 加密后文件流
     * @param encryptKey     密钥
     * @param decryptIndex   加密标识符
     * @return
     * @throws NoSuchPaddingException
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeyException
     * @throws IOException
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException
     */
    private static InputStream decryptStreamOrBytes(InputStream oldInputStream, String encryptKey, String decryptIndex) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IOException, IllegalBlockSizeException, BadPaddingException {
        //输出流
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        //read的part
        byte[] part = new byte[PART_ENCRYPT_SIZE * 1024];
        //加密部分List<byte[]>，统一解密
        List<byte[]> list = new ArrayList<>();
        //区分read是加密还是未加密数据
        boolean decryptOver = true;
        // 读取标识并检查标识位置
        int data;
        while ((data = oldInputStream.read(part)) != -1) {
            //复制有效字节数(因最后一次read时，part不足（PART_ENCRYPT_SIZE * 1024）大小，所以得复制出有效字节数)
            byte[] validBytes = Arrays.copyOf(part, data);
            //检查标识符是否出现在part。出现返回对应index，未出现返回-1
            int i = indexOf(validBytes, decryptIndex.getBytes(StandardCharsets.UTF_8));
            if (i != -1) {
                //置为false，表明接下来read的都是未加密的。
                decryptOver = false;

                //获取decryptIndex前加密的部分数据
                byte[] bytesBeforeTarget = getBytesBeforeTarget(validBytes, i);
                //获取decryptIndex后未加密的部分数据
                byte[] bytesAfterTarget = getBytesAfterTarget(validBytes, i, decryptIndex.getBytes(StandardCharsets.UTF_8).length);

                //把加密部分添加到list
                list.add(bytesBeforeTarget);

                //把list转成整个byte进行解密操作
                byte[] decrypt = AesUtils.decrypt(mergeByteArrays(list), encryptKey);

                //写入解密后的数据
                outputStream.write(decrypt);
                //写入当前part中未加密的数据
                outputStream.write(bytesAfterTarget);
            } else {
                if (decryptOver) {
                    //添加加密进list
                    list.add(Arrays.copyOf(part, data));
                } else {
                    //写入未加密的part
                    outputStream.write(part, 0, data);
                }
            }
        }
        outputStream.close();
        oldInputStream.close();
        //返回处理后的inputStream
        return convertByteArrayToInputStream(outputStream);
    }

    /**
     * 获取byte前index数据
     *
     * @param source byte数据
     * @param index  分割角标
     * @return
     */
    private static byte[] getBytesBeforeTarget(byte[] source, int index) {
        if (index != -1) {
            return Arrays.copyOfRange(source, 0, index);
        }
        return null;
    }

    /**
     * 获取byte后index+targetLength数据
     *
     * @param source       byte数据
     * @param index        分割角标
     * @param targetLength 忽略的长度
     * @return
     */
    private static byte[] getBytesAfterTarget(byte[] source, int index, int targetLength) {
        if (index != -1) {
            return Arrays.copyOfRange(source, index + targetLength, source.length);
        }
        return null;
    }

    /**
     * 获取target在source中角标的位置
     *
     * @param source
     * @param target
     * @return
     */
    private static int indexOf(byte[] source, byte[] target) {
        for (int i = 0; i <= source.length - target.length; i++) {
            if (Arrays.equals(Arrays.copyOfRange(source, i, i + target.length), target)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * 把list<byte>转成一整个byte[]
     *
     * @param byteArrayList byte[]List
     * @return
     */
    private static byte[] mergeByteArrays(List<byte[]> byteArrayList) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        for (byte[] byteArray : byteArrayList) {
            outputStream.write(byteArray, 0, byteArray.length);
        }

        return outputStream.toByteArray();
    }

    /**
     * byte[]转InputStream
     *
     * @param outputStream
     * @return
     */
    private static InputStream convertByteArrayToInputStream(ByteArrayOutputStream outputStream) {
        return new ByteArrayInputStream(outputStream.toByteArray());
    }

    /**
     * @param filePath
     * @param fileSizeInBytes
     * @throws IOException
     */
    public static void generateRandomTextFile(String filePath, int fileSizeInBytes) throws IOException {
        Random random = new Random();
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filePath), StandardCharsets.UTF_8))) {
            int bytesWritten = 0;
            while (bytesWritten < fileSizeInBytes) {
                char randomChar = (char) ('a' + random.nextInt(26)); // 生成随机字母
                writer.write(randomChar);
                bytesWritten += 2; // 每个字符占用两个字节（UTF-16编码）
            }
        }
    }
}
