package com.sunyard.module.storage.service;

import static com.sunyard.module.storage.constant.CachePrefixConstants.CACH_ING;

import java.awt.Color;
import java.awt.Font;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.Resource;

import org.apache.poi.util.IOUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import com.sunyard.framework.img.util.ImageToPngUtils;
import com.sunyard.framework.redis.constant.TimeOutConstants;
import com.sunyard.framework.redis.util.RedisUtils;
import com.sunyard.framework.spire.util.ExcelWaterMarkUtils;
import com.sunyard.framework.spire.util.ImageWaterUtils;
import com.sunyard.framework.spire.util.WatermarkUtils;
import com.sunyard.framework.spire.util.WordWaterMarkUtil;
import com.sunyard.module.storage.config.properties.StorageUploadProperties;
import com.sunyard.module.storage.constant.CachePrefixConstants;
import com.sunyard.module.storage.constant.SunCacheDelConstants;
import com.sunyard.module.storage.util.CacheFileUtils;
import com.sunyard.module.storage.util.SunCacheUtils;

import cn.hutool.core.util.ObjectUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * 缓存公共方法
 *
 * @author PJW
 */
@Slf4j
@Service
public class CacheCommonService {

    @Resource
    private SunCacheService sunCacheService;

    /**
     * 文件加水印线程池
     */
    public static final ExecutorService EXECUTOR = new ThreadPoolExecutor(5, 5, 0L,
            TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(), new ThreadFactory() {
                private final AtomicInteger threadNumber = new AtomicInteger(1);

                @Override
                public Thread newThread(Runnable r) {
                    return new Thread(r, "FileWaterMarkThreadPool-" + threadNumber.getAndIncrement());
                }
            });

    @Resource
    private StorageUploadProperties storageUploadProperties;
    @Resource
    private RedisUtils redisUtils;

    /**
     * 获取缓存文件流
     *
     * @param fileName 文件名字
     * @param ext 文件后缀
     * @return Result 文件输入流
     */
    public InputStream getFileCache(String fileName, String ext) {
        // 获取文件缓存绝对路径
        String filePath = getFileCachePath(fileName, ext);
        // 其他格式的不需要缓存，直接返回文件
        if (ObjectUtil.isEmpty(filePath)) {
            // 是否缓存
            return null;
        }
        // 判断缓存服务与存储服务服务器是否在同一服务器上
        // 获取存储服务服务器ip
        String ipAddress = CacheFileUtils.getIpAddress();
        InputStream fileInputStream = null;
        if (storageUploadProperties.getSftpHost().equals(ipAddress) || !storageUploadProperties.getSftpEnable()) {
            // 缓存服务与存储服务服务器在同一台服务器，获取文件路径
            long start = System.currentTimeMillis();
            // 获取本地缓存文件
            File file0 = new File(filePath);
            if (file0.exists()) {
                // 文件存在
                try {
                    fileInputStream = new FileInputStream(file0);
                } catch (IOException e) {
                    log.info("获取文件byte错误", e);
                    throw new RuntimeException(e);
                }
            }
            long end = System.currentTimeMillis();
            log.info("缓存服务器与存储服务服务器在同一服务器：获取缓存文件耗时：{}(毫秒)", end - start);
        } else {
            // 缓存在远程服务器 使用FTP连接获取 因为速度太慢先舍弃
        }
        return fileInputStream;
    }

    /**
     * 缓存文件
     *
     * @param url 文件url
     * @param fileId 文件id
     * @param fileExt 文件后缀
     * @param isEncrypt 是否加密
     * @param encryptKey 加密key
     * @param encryptType 加密标识
     * @param length 加密长度
     */
    public void cacheFile(InputStream inputStream, String url, Long fileId, String fileExt,
                          Integer isEncrypt, String encryptKey, Integer encryptType,
                          Integer length,String password) {
        String newExt = getNewExt(fileExt);
        if (ObjectUtil.isEmpty(inputStream)) {
            log.warn("缓存文件方法的文件流入参为空");
        }
        try {
            redisUtils.set(CachePrefixConstants.CACH_ING + fileId, CachePrefixConstants.CACH_ING, TimeOutConstants.ONE_HOURS);
            log.info("重新缓存文件2");
            sunCacheService.cacheFile(inputStream, url, storageUploadProperties.getSftpDirectory(), fileId, newExt, fileExt,
                    isEncrypt, encryptKey, encryptType, length,password);
        } catch (Exception e) {
            log.error("系统异常", e);
            throw new RuntimeException(e);
        } finally {
            String key = CACH_ING + fileId;
            String s = redisUtils.get(key);
            if (CACH_ING.equals(s)) {
                redisUtils.del(key);
            }
        }
    }

    /**
     * 异步缓存文件
     *
     * @param url 文件url
     * @param fileId 文件id
     * @param fileExt 文件后缀
     * @param isEncrypt 是否加密
     * @param encryptKey 加密key
     * @param encryptType 加密标识
     * @param length 加密长度
     */
    @Async("GlobalThreadPool")
    public void cacheFileAsync(InputStream inputStream, String url, Long fileId, String fileExt,
                               Integer isEncrypt, String encryptKey, Integer encryptType,
                               Integer length,String password) {
        if (!storageUploadProperties.getCacheEnable()) {
            return;
        }
        try {
            String newExt = getNewExt(fileExt);
            if (ObjectUtil.isEmpty(inputStream)) {
                log.info("缓存文件方法的文件流入参为空");
            }
            redisUtils.set(CachePrefixConstants.TRANSCODING + fileId, CachePrefixConstants.TRANSCODING,
                    TimeOutConstants.ONE_HOURS);
            log.info("重新缓存文件");
            Path path = Paths.get(storageUploadProperties.getSftpDirectory());
            if (Files.exists(path) && Files.isDirectory(path)) {
            } else {
                // 如果目录不存在，则创建新目录
                try {
                    Files.createDirectories(path);
                    log.info("目录已创建" + storageUploadProperties.getSftpDirectory());
                } catch (IOException e) {
                    log.error("无法创建目录", e);
                    throw new RuntimeException(e);
                }
            }
            sunCacheService.cacheFile(inputStream, url, storageUploadProperties.getSftpDirectory(), fileId, newExt, fileExt,
                    isEncrypt, encryptKey, encryptType, length,password);
        } catch (Exception e) {
            log.error("系统异常", e);
            throw new RuntimeException(e);
        } finally {
            // 把转码完成的文件id存到redis中
            redisUtils.del(CachePrefixConstants.TRANSCODING + fileId);
            // 关闭文件流
            if (ObjectUtil.isNotEmpty(inputStream)) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    log.error("系统异常",e);
                    throw new RuntimeException(e);
                }
            }
        }
    }

    /**
     * 获取水印资源缓存文件
     *
     * @param inputStream 文件输入流
     * @param fileId 文件id
     * @param ext 文件后缀
     * @param font 字体
     * @param color 颜色
     * @param degree 角度
     * @param markValue 水印内容
     * @param isEncrypt 是否加密
     * @param encryptKey 加密key
     * @param encryptType 加密标识
     * @param length 加密长度
     * @return Result 加水印文件输入流
     */
    public InputStream cacheWaterFile(InputStream inputStream, String sessionId, Long fileId,
                                      String ext, Font font, Color color, Integer degree,
                                      String markValue, Integer isEncrypt, String encryptKey,
                                      Integer encryptType, Integer length, String password) {
        long start = System.currentTimeMillis();
        InputStream fileInputStream = null;
        // 重新缓存文件
        String tempPathFileName = null;
        // 缓存转换后的文件
        try {
            if (SunCacheUtils.IMGS.contains(ext)) {
                // 处理图片
                tempPathFileName = handleImage(inputStream, sessionId, fileId, ext, font, color,
                        degree, markValue, tempPathFileName);
            } else if (SunCacheUtils.DOCS.contains(ext) || SunCacheUtils.OFD.contains(ext)
                    || SunCacheUtils.TIFFLIST.contains(ext) || SunCacheUtils.PDF.contains(ext)) {
                // 处理可转pdf类型文件
                tempPathFileName = handleDoc(inputStream, sessionId, fileId, ext, font, color,
                        degree, markValue, isEncrypt, encryptKey, encryptType, tempPathFileName,
                        length,password);
            }
            File file = new File(tempPathFileName);
            if (file.exists()) {
                fileInputStream = new FileInputStream(file);
            }
            long end = System.currentTimeMillis();
            log.info("缓存服务器与存储服务服务器在同一服务器：获取缓存文件耗时：{}(毫秒)", end - start);
        } catch (FileNotFoundException e) {
            log.info("获取格式转换后的文件错误", e);
            throw new RuntimeException(e);
        } catch (Exception e) {
            log.error("系统异常",e);
            throw new RuntimeException(e);
        } finally {
            String key = CACH_ING + fileId;
            String s = redisUtils.get(key);
            if (CACH_ING.equals(s)) {
                // 缓存完后向消息队列发送信息
                redisUtils.del(key);
            }
            // 关闭文件流
            if (ObjectUtil.isNotEmpty(inputStream)) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    log.error("系统异常",e);
                    throw new RuntimeException(e);
                }
            }
        }
        return fileInputStream;
    }

    /**
     * 查磁盘
     * @param sessionId
     * @param fileId
     * @param ext
     * @return
     */
    public InputStream getDownWaterFile(String sessionId, Long fileId, String ext) {
        InputStream fileInputStream = null;
        String tempPath = storageUploadProperties.getSftpDirectory() + fileId + "-" + sessionId + "." + ext;
        try {
            // 将缓存服务中的文件缓存到存储服务中的临时路径中
            // 检查本地目录是否存在，如果不存在则创建它
            File localDir = new File(storageUploadProperties.getSftpDirectory());
            if (!localDir.exists()) {
                localDir.mkdirs(); // 创建本地目录及其所有父目录
            }
            // 查看存储服务本地路径下有没有该文件
            File file = new File(tempPath);
            if (!file.exists()) {
                return null;
            } else {
                log.info("缓存中存在文件，{}", tempPath);
                fileInputStream = new FileInputStream(file);
            }
        } catch (Exception e) {
            log.error("文件获取失败", e);
            throw new RuntimeException(e);
        }
        return fileInputStream;
    }

    /**
     * 获取下载水印文件
     *
     * @param inputStream 文件流
     * @param sessionId sessionId
     * @param fileId 文件id
     * @param ext 文件后缀
     * @param font 字体
     * @param color 颜色
     * @param degree 角度
     * @param markValue 水印内容
     * @param isEncrypt 是否加密
     * @param encryptKey 加密key
     * @param encryptType 加密标识
     * @param length 加密长度
     * @return Result 加水印文件输入流
     */
    public InputStream getDownWaterFile(InputStream inputStream, String sessionId, Long fileId,
                                        String ext, Font font, Color color, Integer degree,
                                        String markValue, Integer isEncrypt, String encryptKey,
                                        Integer encryptType, Integer length,String password) {
        InputStream fileInputStream = null;
        String tempPath = storageUploadProperties.getSftpDirectory() + fileId + "-" + sessionId + "." + ext;
        try {
            // 需要缓存重复器中获取
            redisUtils.set(CachePrefixConstants.CACH_ING + fileId, CachePrefixConstants.CACH_ING, TimeOutConstants.ONE_HOURS);
            // 将缓存服务中的文件缓存到存储服务中的临时路径中
            // 检查本地目录是否存在，如果不存在则创建它
            File localDir = new File(storageUploadProperties.getSftpDirectory());
            if (!localDir.exists()) {
                localDir.mkdirs(); // 创建本地目录及其所有父目录
            }
            // 查看存储服务本地路径下有没有该文件
            File file = new File(tempPath);
            if (!file.exists()) {
                log.info("2缓存设备中不存在文件，{}", storageUploadProperties.getSftpDirectory() + fileId + "-" + sessionId + "." + ext);
                // 添加水印
                if (SunCacheUtils.IMGS.contains(ext) || SunCacheUtils.TIFFLIST.contains(ext)) {
                    // 图片
                    if(SunCacheUtils.HEIC.contains(ext)){
                        inputStream=ImageToPngUtils.specialImagesToJpg(inputStream);
                    }
                    ImageWaterUtils.markImgByConfig(inputStream, tempPath, degree, font, markValue,
                            color, ext);
                } else if (SunCacheUtils.PDF.contains(ext)) {
                    // pdf
                    WatermarkUtils.addPdfWaterMarkByConfig(inputStream, tempPath, degree, font,
                            color, markValue,password);
                } else if (SunCacheUtils.DOCX.contains(ext)) {
                    // word
                    WordWaterMarkUtil.addWaterMarkByConfig(inputStream, tempPath, degree, font,
                            color, markValue,password);
                } else if (SunCacheUtils.XLS.contains(ext)) {
                    // excel
                    ExcelWaterMarkUtils.addExcelWaterMark(inputStream, tempPath, font, color,
                            degree, markValue,password);
                }
                if (file.exists()) {
                    log.info("重新将文件缓存到缓存设备上，{}",
                            storageUploadProperties.getSftpDirectory() + fileId + "-" + sessionId + "." + ext);
                    fileInputStream = new FileInputStream(file);
                }
            } else {
                log.info("缓存中存在文件，{}", tempPath);
                fileInputStream = new FileInputStream(file);
            }
        } catch (Exception e) {
            log.error("文件获取失败", e);
            throw new RuntimeException(e);
        } finally {
            String key = CACH_ING + fileId;
            String s = redisUtils.get(key);
            if (CACH_ING.equals(s)) {
                // 缓存完后向消息队列发送信息
                redisUtils.del(key);
            }
            // 关闭文件流
            if (ObjectUtil.isNotEmpty(inputStream)) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    log.error("系统异常",e);
                    throw new RuntimeException(e);
                }
            }
        }
        return fileInputStream;
    }

    /**
     * 预览时，文件还未完成转码的话，需要等待
     *
     * @param fileId
     */
    public void waitTransBase(Long fileId) {
        // 判断是否在转码中
        if (CachePrefixConstants.TRANSCODING.equals(redisUtils.get(CachePrefixConstants.TRANSCODING + fileId))) {
            // 等待转码完成
            log.info("开始等待转码/缓存");
            waitTranscoding(CachePrefixConstants.TRANSCODING + fileId);
            log.info("转码/缓存完成可以预览");
        }
        // 先判断该文件是否正在被别的进程写入
        String key = CACH_ING + fileId;
        String s = redisUtils.get(key);
        if (CACH_ING.equals(s)) {
            // 该文件正在被写入
            // 等待文件写入完成
            log.info("等待文件完成写入中");
            waitTranscoding(key);
            log.info("文件完成写入");
            redisUtils.del(key);
        }
    }

    /**
     * 等待转码
     * @param key 文件id
     */
    private void waitTranscoding(String key) {

        try {
            int time = 1000;
            while (time < 1000 * 60 * 60) {
                //一个小时后自动放开
                // 检查Redis中对应的key是否存在
                if (!redisUtils.hasKey(key)) {
                    log.info("Redis中没有找到key: " + key + ", 释放CountDownLatch");
                    break; // 退出循环
                } else {
                    // 避免频繁请求Redis
                    Thread.sleep(time);
                    if (time < 3000) {
                        //高于10秒的话，就累加了
                        time = time + 500;
                    }

                }

            }
        } catch (InterruptedException e) {
            log.error("Redis检查线程被中断", e);
        }

    }

    /**
     * 获取文件缓存绝对路径
     *
     * @param fileName 文件id
     * @param ext 文件后缀
     * @return Result
     */
    private String getFileCachePath(String fileName, String ext) {
        String newExt = getNewExt(ext);
        if (newExt == null) {
            return null;
        }
        return storageUploadProperties.getSftpDirectory() + fileName + "." + newExt;
    }

    /**
     * 后缀转换
     *
     * @param ext 原后缀
     * @return String
     */
    private String getNewExt(String ext) {
        String newExt = ext;
        if (SunCacheUtils.VIDEOS.contains(ext.toLowerCase())) {
            newExt = "mp4";
        } else if (SunCacheUtils.IMGS.contains(ext.toLowerCase())
                && !SunCacheUtils.TIFFLIST.contains(ext.toLowerCase())) {
            // 图片生成缩略图处理
            newExt = "png";
        } else if (SunCacheUtils.DOCS.contains(ext.toLowerCase())
                || SunCacheUtils.OFD.contains(ext.toLowerCase())
                || SunCacheUtils.TIFFLIST.contains(ext.toLowerCase())
                || SunCacheUtils.PDF.contains(ext.toLowerCase())) {
            // 文档转换pdf处理
            newExt = "pdf";
        } else if (SunCacheUtils.AUDIOS.contains(ext.toLowerCase())) {
            newExt = "mp3";
        }
        return newExt;
    }

    /**
     * 处理文档类型水印
     *
     * @param inputStream 输入流
     * @param sessionId sessionId
     * @param fileId 文件id
     * @param ext 文件后缀
     * @param font 字体
     * @param color 颜色
     * @param degree 角度
     * @param markValue 内容
     * @param isEncrypt 是否加密
     * @param encryptKey 加密key
     * @param encryptType 加密标识
     * @param tempPathFileName 临时文件名
     * @return Result
     */
    private String handleDoc(InputStream inputStream, String sessionId, Long fileId, String ext,
                             Font font, Color color, Integer degree, String markValue,
                             Integer isEncrypt, String encryptKey, Integer encryptType,
                             String tempPathFileName, Integer length,String password) {
        String fileName = SunCacheDelConstants.DEL_SESSION_SUNYARD + fileId + "-" + sessionId;
        tempPathFileName = storageUploadProperties.getSftpDirectory() + fileName + ".pdf";
        // 先从缓存获取流
        InputStream fis = getFileCache(fileName, ext);
        if (fis == null) {
            fis = getFileCache(String.valueOf(fileId), ext);
            if (fis == null) {
                // 缓存中不存在pdf或者转码后的pdf文件 需要重新转换格式
                cacheFile(inputStream, null, fileId, ext, isEncrypt, encryptKey, encryptType,
                        length,password);
                // 从缓存获取转换格式后的文件
                fis = getFileCache(String.valueOf(fileId), ext);
                try {
                    if (ObjectUtils.isEmpty(fis) || fis.available() == 0) {
                        log.error("缓存失败,转换pdf失败");
                        return tempPathFileName;
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            CountDownLatch latch = new CountDownLatch(1);
            InputStream finalFis = fis;
            String finalTempPathFileName = tempPathFileName;
            try {
                EXECUTOR.execute(() -> {
                    try {
                        // 获取带水印PDF文件并保存临时路径
                        WatermarkUtils.addPdfWaterMarkByConfig(finalFis, finalTempPathFileName,
                                degree, font, color, markValue,password);
                        latch.countDown(); // 即使出错，也需要调用 countDown
                    } catch (Exception e) {
                        // 这里捕获并处理子线程中的异常
                        log.error("子线程异常:", e);
                        // 可以根据需要将异常传递给主线程
                        latch.countDown(); // 即使出错，也需要调用 countDown
                    }
                });
                // 用于阻塞当前线程，直到计数器减为零
                latch.await();
            } catch (InterruptedException e) {
                log.error("系统异常",e);
                throw new RuntimeException(e);
            }
            try {
                if (ObjectUtil.isNotEmpty(fis)) {
                    fis.close();
                }
                if (ObjectUtil.isNotEmpty(inputStream)) {
                    inputStream.close();
                }
                if (ObjectUtil.isNotEmpty(finalFis)) {
                    finalFis.close();
                }
            } catch (IOException e) {
                log.error("系统异常",e);
                throw new RuntimeException(e);
            }
        }
        return tempPathFileName;
    }

    /**
     * 处理图片水印
     *
     * @param inputStream 输入流
     * @param sessionId sessionId
     * @param fileId 文件id
     * @param ext 文件后缀
     * @param font 字体
     * @param color 颜色
     * @param degree 角度
     * @param markValue 内容
     * @param tempPathFileName 临时文件名
     * @return Result
     */
    private String handleImage(InputStream inputStream, String sessionId, Long fileId, String ext,
                               Font font, Color color, Integer degree, String markValue,
                               String tempPathFileName) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            // 复制一份输入流,来供下面两个方法各自使用
            IOUtils.copy(inputStream, baos);
            tempPathFileName = storageUploadProperties.getSftpDirectory() + SunCacheDelConstants.DEL_SESSION_SUNYARD + fileId
                    + "-" + sessionId + "." + ext;
            if (SunCacheUtils.SPECIALIMGS.contains(ext.toLowerCase())) {
                log.info("特殊图片加水印");
                // 转换个格式
                InputStream is = null;
                // 如果是一些特殊图片将特殊图片需要将图片格式改为jpg格式再次重新添加水印并缓存
                tempPathFileName = storageUploadProperties.getSftpDirectory() + SunCacheDelConstants.DEL_SESSION_SUNYARD + fileId
                        + "-" + sessionId + ".jpg";
                // 转换个格式
                is = new ByteArrayInputStream(baos.toByteArray());
                // 将特殊图片转为jpg
                if (SunCacheUtils.HEIC.contains(ext.toLowerCase())) {
                    inputStream = ImageToPngUtils.specialImagesToJpg(is);
                } else {
                    inputStream = ImageToPngUtils.specialImagesToPng(is);
                }
                ImageWaterUtils.markImgByConfig(inputStream, tempPathFileName, degree, font,
                        markValue, color, "jpg");
            } else {
                ImageWaterUtils.markImgByConfig(new ByteArrayInputStream(baos.toByteArray()),
                        tempPathFileName, degree, font, markValue, color, ext);
            }
        } catch (Exception e) {
            log.error("图片加水印失败", e);
            throw new RuntimeException(e);
        } finally {
            if (ObjectUtil.isNotEmpty(baos)) {
                try {
                    baos.close();
                } catch (IOException e) {
                    log.error("系统异常",e);
                    throw new RuntimeException(e);
                }
            }
            if (ObjectUtil.isNotEmpty(inputStream)) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    log.error("系统异常",e);
                    throw new RuntimeException(e);
                }
            }
        }
        return tempPathFileName;
    }

    //
    //    /**
    //     * 发送MQ消息
    //     *
    //     * @param fileId 文件id
    //     */
    //    private void sendMqMessage(Long fileId) {
    //        ConnectionFactory factory = new ConnectionFactory();
    //        // 设置 RabbitMQ 主机地址
    //        factory.setHost(host);
    //        factory.setPort(port);
    //        factory.setUsername(username);
    //        factory.setPassword(password);
    //
    //        MqProducerUtils rabbitMqUtil = new MqProducerUtils(queueNameTran, exchangeName);
    //        try {
    //            rabbitMqUtil.sendMessage(factory, fileId.toString());
    //            log.info("队列名称：{}", queueNameTran);
    //            log.info("交换机名称：{}", exchangeName);
    //        } catch (Exception e) {
    //            log.error("消息发送失败", e);
    //            throw new RuntimeException(e);
    //        }
    //    }

    //    /**
    //     * 向MQ发送消息
    //     *
    //     * @param fileId 文件id
    //     * @param queueName 管道名称
    //     */
    //    private void sendMqMessage(Long fileId, String queueName) {
    //        ConnectionFactory factory = new ConnectionFactory();
    //        // 设置 RabbitMQ 主机地址
    //        factory.setHost(host);
    //        factory.setPort(port);
    //        factory.setUsername(username);
    //        factory.setPassword(password);
    //
    //        MqProducerUtils rabbitMqUtil = new MqProducerUtils(queueName, exchangeName);
    //        try {
    //            rabbitMqUtil.sendMessage(factory, fileId.toString());
    //            log.info("队列名称：{}", queueNameTran);
    //            log.info("交换机名称：{}", exchangeName);
    //        } catch (Exception e) {
    //            log.error("消息发送失败", e);
    //            throw new RuntimeException(e);
    //        }
    //    }

}
