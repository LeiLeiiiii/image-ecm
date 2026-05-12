package com.sunyard.mytool.until;


import com.sunyard.mytool.dto.EncryptDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.shiro.codec.Hex;
import org.springframework.beans.factory.annotation.Value;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;


@Slf4j
public class FileEncryptUtils {

    /**
     * 字节加密最大范围：单位MB
     */
    private static final Integer BYTES_MAX_SIZE = 200;

    /**
     * 加密最大范围： 单位KB
     */
    private static final Integer PART_ENCRYPT_SIZE = 2;

    /**
     * aes加密标识符（用于区分加密、未加密数据）
     */
    private static final String ENCRYPT_INDEX = "<ENCRYPTED>";

    /**
     * #国密（SM2）加密标识符（用于区分加密部分、未加密部分）
     */
    private static final String SM2_ENCRYPT_INDEX = "<SM2_ENCRYPTED>";

    @Value("${encryptTempPath:/home/split}")
    private static final String fileHome = "/home/dcos/fireDir";

    private static final Integer SIZE = 1024;

    /**
     * 加密文件
     *
     * @param oldInputStream 源文件流
     * @param encryptKey     密钥
     * @param encryptType    加密标识符
     * @return Result
     */
    public static EncryptDTO encrypt(InputStream oldInputStream, String encryptKey, Integer encryptType) {
        EncryptDTO dto = new EncryptDTO();
        try {
            if (oldInputStream.available() == 0) {
                log.debug("源文件流oldInputStream为空");
                return null;
            }
            //小于BYTES_MAX_SIZE 走字节加密；大于走流加密
            if (oldInputStream.available() < BYTES_MAX_SIZE * SIZE * SIZE) {
                dto = encryptBytes(oldInputStream, encryptKey, encryptType);
            } else {
                dto = encryptStream(oldInputStream, encryptKey, encryptType);
            }
        } catch (IOException e) {
            log.error("加密失败：{}", e.toString());
            throw new RuntimeException(e);
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            log.error("加解密失败:{}", e.toString());
            throw new RuntimeException(e);
        } catch (NoSuchPaddingException e) {
            log.error("填充方式错误:{}", e.toString());
            throw new RuntimeException(e);
        } catch (NoSuchAlgorithmException e) {
            log.error("加密方式错误:{}", e.toString());
            throw new RuntimeException(e);
        } catch (InvalidKeyException e) {
            log.error("密钥错误:{}", e.toString());
            throw new RuntimeException(e);
        }
        return dto;
    }

    /**
     * 解密文件
     *
     * @param oldInputStream 加密后文件流
     * @param encryptKey     密钥
     * @param encryptType    加密类型
     * @return Result
     */
    public static InputStream decrypt(InputStream oldInputStream, String encryptKey, Integer encryptType, Integer length) {
        InputStream inputStream = null;
        try {
            if (oldInputStream.available() == 0) {
                log.info("加密文件流oldInputStream为空");
                return null;
            }
            long startTime = System.currentTimeMillis();
            inputStream = decryptStreamOrBytes(oldInputStream, encryptKey, encryptType, length);
            long passTime = System.currentTimeMillis() - startTime;
            log.info("解密文件耗时:" + passTime);
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            log.error("加解密失败:{}", e.toString());
            throw new RuntimeException(e);
        } catch (NoSuchPaddingException e) {
            log.error("填充方式错误:{}", e.toString());
            throw new RuntimeException(e);
        } catch (NoSuchAlgorithmException e) {
            log.error("加密方式错误:{}", e.toString());
            throw new RuntimeException(e);
        } catch (InvalidKeyException e) {
            log.error("密钥错误:{}", e.toString());
            throw new RuntimeException(e);
        } catch (IOException e) {
            log.error("加密失败：{}", e.toString());
            throw new RuntimeException(e);
        }
        return inputStream;
    }

    /**
     * 字节加密
     *
     * @param oldInputStream 源文件流
     * @param encryptKey     密钥
     * @param encryptType    加密标识符
     * @return Result
     * @throws IOException               异常
     * @throws IllegalBlockSizeException 异常
     * @throws NoSuchPaddingException    异常
     * @throws BadPaddingException       异常
     * @throws NoSuchAlgorithmException  异常
     * @throws InvalidKeyException       异常
     */
    private static EncryptDTO encryptBytes(InputStream oldInputStream, String encryptKey, Integer encryptType) throws IOException, IllegalBlockSizeException, NoSuchPaddingException, BadPaddingException, NoSuchAlgorithmException, InvalidKeyException {
        //读取整个文件的byte[]数组
        byte[] bytes = IOUtils.toByteArray(oldInputStream);
        //输出流
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] encryptByte;
        //如果字节小于PART_ENCRYPT_SIZE整个文件加密；如果大于则只截取前PART_ENCRYPT_SIZE部分进行加密
        if (bytes.length < PART_ENCRYPT_SIZE * SIZE) {
            //加密整个文件字节
            //根据标识判断是否是SM2、还是AES
            if (0 == encryptType) {
                String encrypt = AesUtils.encrypt(bytes, encryptKey);
                encryptByte = encrypt.getBytes(StandardCharsets.UTF_8);
            } else if (1 == encryptType) {
                String encrypt = Sm2Util.encrypt(bytes);
                encryptByte = Hex.decode(encrypt);
            } else {
                return null;
            }
            //写入加密数据
            outputStream.write(encryptByte);
        } else {
            //只取前PART_ENCRYPT_SIZE * 1024位进行加密
            byte[] needEncrypt = Arrays.copyOfRange(bytes, 0, (PART_ENCRYPT_SIZE * 1024));
            //根据标识判断是否是SM2、还是AES
            if (0 == encryptType) {
                String encrypt = AesUtils.encrypt(needEncrypt, encryptKey);
                encryptByte = encrypt.getBytes(StandardCharsets.UTF_8);
            } else if (1 == encryptType) {
                String encrypt = Sm2Util.encrypt(needEncrypt);
                encryptByte = Hex.decode(encrypt);
            } else {
                return null;
            }
            //后面不加密
            byte[] noNeedEncrypt = Arrays.copyOfRange(bytes, (PART_ENCRYPT_SIZE * 1024), bytes.length);
            //写入加密数据
            outputStream.write(encryptByte);
            //写入未加密
            outputStream.write(noNeedEncrypt);
        }
        outputStream.close();
        EncryptDTO dto = new EncryptDTO();
        dto.setInputStream(convertByteArrayToInputStream(outputStream));
        dto.setLength(encryptByte.length);

        return dto;

    }

    /**
     * 流加密
     *
     * @param oldInputStream 源文件流
     * @param encryptKey     密钥
     * @param encryptType    加密标识符
     * @return Result
     * @throws IOException               异常
     * @throws IllegalBlockSizeException 异常
     * @throws NoSuchPaddingException    异常
     * @throws BadPaddingException       异常
     * @throws NoSuchAlgorithmException  异常
     * @throws InvalidKeyException       异常
     */
    public static EncryptDTO encryptStream(InputStream oldInputStream, String encryptKey, Integer encryptType) throws IOException, IllegalBlockSizeException, NoSuchPaddingException, BadPaddingException, NoSuchAlgorithmException, InvalidKeyException {
        //输出流
        //临时文件路径
        UUID uuid = UUID.randomUUID();
        String remotePath = fileHome + uuid + ".dat";
        File file = new File(remotePath);
        FileOutputStream fos = new FileOutputStream(file);
        BufferedOutputStream bos = new BufferedOutputStream(fos);
        //截取配置加密范围进行加密
        ByteArrayOutputStream bos1 = new ByteArrayOutputStream();
        byte[] part = new byte[PART_ENCRYPT_SIZE * 1024];
        byte[] buffers = new byte[1024];
        int totalBytesRead = 0;
        int bytesRead1 = 0;
        while (totalBytesRead < PART_ENCRYPT_SIZE * SIZE && (bytesRead1 = oldInputStream.read(buffers)) != -1) {
            bos1.write(buffers, 0, bytesRead1);
            totalBytesRead += bytesRead1;
        }
        bos1.flush();
        part = bos1.toByteArray();
        byte[] encryptByte;
        //根据标识判断是否是SM2、还是AES
        if (0 == encryptType) {
            String encrypt = AesUtils.encrypt(part, encryptKey);
            encryptByte = encrypt.getBytes(StandardCharsets.UTF_8);
        } else if (1 == encryptType) {
            String encrypt = Sm2Util.encrypt(part);
            encryptByte = Hex.decode(encrypt);
        } else {
            return null;
        }
        //写入加密数据
        bos.write(encryptByte);
        //写入未加密的部分
        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = oldInputStream.read(buffer)) != -1) {
            bos.write(buffer, 0, bytesRead);
        }
        //确保缓冲区数据被立即刷新到文件
        bos.flush();
        bos.close();
        bos1.close();
//        oldInputStream.close();
        //返回处理后的inputStream
        InputStream fileInputStream = new FileInputStream(file);
        //删除临时文件
//        FileUtil.del(file);
        EncryptDTO dto = new EncryptDTO();
        dto.setInputStream(fileInputStream);
        dto.setLength(encryptByte.length);
        return dto;
    }

    /**
     * 解密（字节加密、流加密解密方法一样）
     *
     * @param oldInputStream 加密后文件流
     * @param encryptKey     密钥
     * @param encryptType    加密类型
     * @param length         加密长度
     * @return Result
     * @throws NoSuchPaddingException    异常
     * @throws NoSuchAlgorithmException  异常
     * @throws InvalidKeyException       异常
     * @throws IOException               异常
     * @throws IllegalBlockSizeException 异常
     * @throws BadPaddingException       异常
     */
    private static InputStream decryptStreamOrBytes(InputStream oldInputStream, String encryptKey, Integer encryptType, Integer length)
            throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IOException, IllegalBlockSizeException, BadPaddingException {

        // 输出流
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        // 保持原来的部分大小
        byte[] part = new byte[length];

        // 读取标识
        int data;

        //加密标识
        boolean flag = true;

        while ((data = oldInputStream.read(part)) != -1) {
            // 复制有效字节数
            byte[] validBytes = Arrays.copyOf(part, data);
            // 检查当前累计的数据长度
            if (flag) {
                flag = false;
                // 获取累积的字节数组
                byte[] decrypt;
                // 解密前 length 字节
                if (0 == encryptType) {
                    decrypt = AesUtils.decrypt(Arrays.copyOf(validBytes, length), encryptKey);
                } else if (1 == encryptType) {
                    decrypt = Sm2Util.decrypt(Arrays.copyOf(validBytes, length));
                } else {
                    decrypt = new byte[0];
                }
                // 写入解密后的数据
                outputStream.write(decrypt);
            }else{
                outputStream.write(validBytes);
            }
        }


        outputStream.close();
        oldInputStream.close();

        // 返回处理后的 inputStream
        return convertByteArrayToInputStream(outputStream);
    }


    /**
     * 将远程文件下载下来并解密
     *
     * @param inputStream    输入流
     * @param serverFilePath 解密文件绝对路径
     * @param encryptKey     加密key
     * @param encryptType    加密类型
     */
    public static void saveDecryptFile(InputStream inputStream, String serverFilePath, String encryptKey, Integer encryptType, Integer length) {
        File file = new File(serverFilePath);
        if (!file.exists()) {
            try (FileOutputStream out = new FileOutputStream(file);
                 BufferedOutputStream outputStream = new BufferedOutputStream(out)) {
                if (null == inputStream || inputStream.available() == 0) {
                    log.info("从存储设备中获取的文件流为空");
                }
                //read的part
                byte[] part = new byte[length];
                //区分read是加密还是未加密数据
                boolean flag = true;
                // 读取标识并检查标识位置
                int data;
                while ((data = inputStream.read(part)) != -1) {
                    // 复制有效字节数
                    byte[] validBytes = Arrays.copyOf(part, data);

                    // 检查是否已经解密过了
                    if (flag) {
                        flag = false;
                        // 获取累积的字节数组
                        byte[] decrypt;

                        // 解密前 length 字节
                        if (0 == encryptType) {
                            decrypt = AesUtils.decrypt(Arrays.copyOf(validBytes, length), encryptKey);
                        } else if (1 == encryptType) {
                            decrypt = Sm2Util.decrypt(Arrays.copyOf(validBytes, length));
                        } else {
                            decrypt = new byte[0];
                        }

                        // 写入解密后的数据
                        outputStream.write(decrypt);
                    }else{
                        outputStream.write(validBytes);
                    }
                }
                //确保缓冲区数据被立即刷新到文件
                outputStream.flush();
            } catch (IOException | NoSuchPaddingException
                     | NoSuchAlgorithmException | InvalidKeyException
                     | IllegalBlockSizeException | BadPaddingException e) {
                log.error(e.toString());
                throw new RuntimeException(e);
            } finally {
                //关闭文件流
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        log.error(e.toString());
                        throw new RuntimeException(e);
                    }
                }
            }
        }
    }

    /**
     * 获取byte前index数据
     *
     * @param source byte数据
     * @param index  分割角标
     * @return Result
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
     * @return Result
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
     * @param source 源byte
     * @param target 目标byte
     * @return Result
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
     * @return Result
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
     * @param outputStream 输出流
     * @return Result
     */
    private static InputStream convertByteArrayToInputStream(ByteArrayOutputStream outputStream) {
        return new ByteArrayInputStream(outputStream.toByteArray());
    }



    /**
     * 老影像解密
     * @param
     * @return
     * @throws IOException
     */
    public static InputStream reverseFile(InputStream input) throws IOException {
        int size = input.available();
        byte[] byt = new byte[size];
        input.read(byt);
        byte[] reverseByt = new byte[size];
        if (size >= 26) {
            for (int j = 0; j < size; j++) {
                if (byt[j] == -128) {
                    continue;
                }
                if (j < 8 || j == 12 || j == 18 || j == 22 || j == 25) {
                    byt[j] = (byte) (0 - byt[j]);
                }
            }
        }
        reverseByt = byt;
        InputStream reverseInput = new ByteArrayInputStream(reverseByt);
        return reverseInput;
    }

}
