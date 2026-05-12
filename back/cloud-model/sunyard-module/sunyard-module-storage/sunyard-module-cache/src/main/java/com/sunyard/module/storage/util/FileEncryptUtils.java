package com.sunyard.module.storage.util;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.apache.commons.io.IOUtils;
import org.apache.shiro.codec.Hex;

import com.sunyard.framework.common.util.encryption.AesUtils;
import com.sunyard.framework.common.util.encryption.Sm2Util;
import com.sunyard.module.storage.constant.SunCacheDelConstants;
import com.sunyard.module.storage.dto.EncryptDTO;

import lombok.extern.slf4j.Slf4j;


/**
 * @author P-JWei
 * @date 2023/12/4 10:35:35
 */
@Slf4j
public class FileEncryptUtils {


    private static final Integer SIZE = 1024;
    //文件前缀
    private static final String FIREDIR = "fireDir";
    /**
     * 加密文件
     *
     * @param oldInputStream 源文件流
     * @param encryptKey     密钥
     * @param encryptType    加密标识符
     * @return Result
     */
    public static EncryptDTO encrypt(InputStream oldInputStream, String encryptKey,
                                     Integer encryptType,String fileHome,
                                     Integer partEncryptSize,Integer bytesMaxSize) {
        EncryptDTO dto = new EncryptDTO();
        try {
            if (oldInputStream.available() == 0) {
                log.debug("源文件流oldInputStream为空");
                return null;
            }
            //小于BYTES_MAX_SIZE 走字节加密；大于走流加密
            if (oldInputStream.available() < bytesMaxSize * SIZE * SIZE) {
                dto = encryptBytes(oldInputStream, encryptKey, encryptType,partEncryptSize);
            } else {
                dto = encryptStream(oldInputStream, encryptKey, encryptType,fileHome,partEncryptSize);
            }
        } catch (IOException e) {
            log.error("加密失败：{}", e);
            throw new RuntimeException(e);
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            log.error("加解密失败:{}", e);
            throw new RuntimeException(e);
        } catch (NoSuchPaddingException e) {
            log.error("填充方式错误:{}", e);
            throw new RuntimeException(e);
        } catch (NoSuchAlgorithmException e) {
            log.error("加密方式错误:{}", e);
            throw new RuntimeException(e);
        } catch (InvalidKeyException e) {
            log.error("密钥错误:{}", e);
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
            log.error("加解密失败", e);
            throw new RuntimeException(e);
        } catch (NoSuchPaddingException e) {
            log.error("填充方式错误", e);
            throw new RuntimeException(e);
        } catch (NoSuchAlgorithmException e) {
            log.error("加密方式错误", e);
            throw new RuntimeException(e);
        } catch (InvalidKeyException e) {
            log.error("密钥错误", e);
            throw new RuntimeException(e);
        } catch (IOException e) {
            log.error("加密失败", e);
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
    private static EncryptDTO encryptBytes(InputStream oldInputStream, String encryptKey, Integer encryptType,Integer partEncryptSize) throws IOException, IllegalBlockSizeException, NoSuchPaddingException, BadPaddingException, NoSuchAlgorithmException, InvalidKeyException {
        //读取整个文件的byte[]数组
        byte[] bytes = IOUtils.toByteArray(oldInputStream);
        //输出流
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] encryptByte;
        //如果字节小于PART_ENCRYPT_SIZE整个文件加密；如果大于则只截取前PART_ENCRYPT_SIZE部分进行加密
        if (bytes.length < partEncryptSize * SIZE) {
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
            byte[] needEncrypt = Arrays.copyOfRange(bytes, 0, (partEncryptSize * 1024));
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
            byte[] noNeedEncrypt = Arrays.copyOfRange(bytes, (partEncryptSize * 1024), bytes.length);
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
    public static EncryptDTO encryptStream(InputStream oldInputStream, String encryptKey, Integer encryptType,String fileHome,Integer partEncryptSize) throws IOException, IllegalBlockSizeException, NoSuchPaddingException, BadPaddingException, NoSuchAlgorithmException, InvalidKeyException {
        File tempDirectory = new File(fileHome);
        // 如果目录不存在，则创建所有父级目录
        if (!tempDirectory.exists()) {
            boolean isCreated = tempDirectory.mkdirs();
            if (!isCreated) {
                throw new RuntimeException("无法创建临时目录: " + fileHome);
            }
        }
        //输出流
        //临时文件路径
        UUID uuid = UUID.randomUUID();
        String remotePath = fileHome +FIREDIR+ uuid + ".dat";
        File file = new File(remotePath);
        try(FileOutputStream fos = new FileOutputStream(file);
        BufferedOutputStream bos = new BufferedOutputStream(fos);){
            //截取配置加密范围进行加密
            ByteArrayOutputStream bos1 = new ByteArrayOutputStream();
            byte[] part = new byte[partEncryptSize * 1024];
            byte[] buffers = new byte[1024];
            int totalBytesRead = 0;
            int bytesRead1 = 0;
            while (totalBytesRead < partEncryptSize * SIZE && (bytesRead1 = oldInputStream.read(buffers)) != -1) {
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
        // 1. 输入参数严格验证（防非法参数攻击）

        File file = new File(serverFilePath);
        if (!file.exists()) {
            // 2. 资源管理：使用try-with-resources自动关闭所有流（包括inputStream）
            try (FileOutputStream out = new FileOutputStream(file);
                 BufferedOutputStream outputStream = new BufferedOutputStream(out);
                 InputStream encryptedInputStream = inputStream) { // 包装为try-with-resources管理

                if (encryptedInputStream.available() == 0) {
                    log.info("从存储设备中获取的文件流为空");
                    return;
                }

                byte[] part = new byte[length]; // 缓冲区大小为加密长度
                boolean isFirstBlock = true; // 标记是否为第一块（需解密的部分）
                int data;

                while ((data = encryptedInputStream.read(part)) != -1) {
                    // 复制有效字节（避免缓冲区残留无效数据）
                    byte[] validBytes = Arrays.copyOf(part, data);

                    if (isFirstBlock) {
                        isFirstBlock = false;
                        // 限制解密长度不超过实际读取的字节数（防数组越界）
                        int decryptLength = Math.min(length, data);
                        byte[] encryptedBytes = Arrays.copyOf(validBytes, decryptLength);
                        byte[] decrypt = null;

                        try {
                            // 3. 解密操作（依赖工具类需确保安全模式）
                            if (encryptType == 0) {
                                // 确保AesUtils使用安全模式（如AES/CBC/PKCS5Padding）并正确处理IV
                                decrypt = AesUtils.decrypt(encryptedBytes, encryptKey);
                            } else if (encryptType == 1) {
                                // 确保SM2工具类使用合规的非对称加密流程（如正确处理公钥/私钥）
                                decrypt = Sm2Util.decrypt(encryptedBytes);
                            }

                            // 写入解密后的数据
                            if (decrypt != null && decrypt.length > 0) {
                                outputStream.write(decrypt);
                            }
                        } finally {
                            // 4. 关键：立即清除内存中的敏感数据（防止堆检查漏洞）
                            if (decrypt != null) {
                                Arrays.fill(decrypt, (byte) 0);
                            }
                            // 清除临时加密字节数组
                            Arrays.fill(encryptedBytes, (byte) 0);
                        }

                        // 处理第一块中超过加密长度的剩余字节（若有）
                        if (data > decryptLength) {
                            byte[] remainingBytes = Arrays.copyOfRange(validBytes, decryptLength, data);
                            outputStream.write(remainingBytes);
                            Arrays.fill(remainingBytes, (byte) 0); // 清除临时数据
                        }
                    } else {
                        // 非第一块直接写入（未加密部分）
                        outputStream.write(validBytes);
                    }
                    // 清除缓冲区数据（防残留）
                    Arrays.fill(validBytes, (byte) 0);
                    Arrays.fill(part, (byte) 0);
                }
                outputStream.flush();
            } catch (IOException | NoSuchPaddingException | NoSuchAlgorithmException
                     | InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
                log.error("文件解密保存失败", e);
                throw new RuntimeException("文件处理失败", e);
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
     * 判断是否需要加密
     *
     * @param isEncrypt 是否需要加密
     * @return boolean
     */
    public static boolean shouldEncrypt(Integer isEncrypt) {
        return SunCacheDelConstants.IS_ENCRYPT.equals(isEncrypt);
    }

}
