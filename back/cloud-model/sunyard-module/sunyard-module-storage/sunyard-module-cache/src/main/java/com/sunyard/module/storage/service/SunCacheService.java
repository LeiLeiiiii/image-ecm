package com.sunyard.module.storage.service;


import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.sunyard.framework.common.util.FileUtils;
import com.sunyard.framework.img.util.ImageToPngUtils;
import com.sunyard.framework.spire.constant.OnlineConstants;
import com.sunyard.framework.spire.util.ConvertPdfUtils;
import com.sunyard.framework.video.util.VideoUtil;
import com.sunyard.module.storage.constant.SunCacheDelConstants;
import com.sunyard.module.storage.util.FileEncryptUtils;
import com.sunyard.module.storage.util.SunCacheUtils;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.apache.commons.compress.utils.IOUtils;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

/**
 * 判断文件类型工具类
 *
 * @author panjiazhu
 * @date 2022/7/13
 */
@Service
@Slf4j
public class SunCacheService {

    @Resource(name = "FileCacheThreadPool")
    private  TaskExecutor staticTaskExecutor;

    /**
     * 缓存文件
     *
     * @param url      文件url
     * @param tempDir  临时文件夹
     * @param fileName 文件名称
     * @param ext      文件后缀
     */
    public void cacheFile(String url, String tempDir, Long fileName, String ext,
                                 String host, Integer port, String username, String password,
                                 String newExt, Integer isEncrypt, String encryptKey, Integer encryptType,Integer length,String filePassword) {
        String extLowerCase = ext.toLowerCase();
        ext = ext.toLowerCase();
        boolean isDoc = "pdf".equals(newExt) && !"pdf".equals(extLowerCase);
        if (SunCacheUtils.VIDEOS.contains(extLowerCase)) {
            if (StrUtil.isNotBlank(host)) {
                videoDownTranscoding(url, tempDir, fileName + "." + ext, host, port, username, password, isEncrypt, encryptKey, encryptType,length);
            } else {
                videoDownTranscoding(url, tempDir, fileName + "." + ext, isEncrypt,encryptKey, encryptType,length);
            }
            //下载转码视频文件.wmv,.asf,.rm,.rmvb,.mov,.mp4,.avi
        } else if (SunCacheUtils.IMGS.contains(extLowerCase) || "png".equals(newExt)) {
            // 图片生成缩略图处理
            if (StrUtil.isNotBlank(host)) {
                imgDownTranscoding(url, tempDir, fileName + "." + ext, host, port, username, password, isEncrypt, encryptKey, encryptType,length);
            } else {
                imgDownTranscoding(url, tempDir, fileName + "." + ext, isEncrypt, encryptKey, encryptType,length);
            }
        } else if (SunCacheUtils.DOCS.contains(extLowerCase) || SunCacheUtils.OFD.contains(extLowerCase) || isDoc) {
            //文档转换pdf处理
            if (StrUtil.isNotBlank(host)) {
                docDownTranscoding(url, tempDir, fileName + ".pdf", ext, host, port, username, password, isEncrypt, encryptKey, encryptType,length,filePassword);
            } else {
                docDownTranscoding(url, tempDir, fileName + ".pdf", ext, isEncrypt, encryptKey, encryptType,length,filePassword);
            }
        } else if (SunCacheUtils.AUDIOS.contains(extLowerCase)) {
            //下载转码音频文件 amr、ogg、m4a
            if (StrUtil.isNotBlank(host)) {
                audioDownTranscoding(url, tempDir, fileName + "." + ext, host, port, username, password, isEncrypt, encryptKey, encryptType,length);
            } else {
                audioDownTranscoding(url, tempDir, fileName + "." + ext, isEncrypt, encryptKey, encryptType,length);
            }
        } else {
            if (StrUtil.isNotBlank(host)) {
            } else {
                getInputByUrl(url, tempDir, fileName + "." + ext, isEncrypt, encryptKey, encryptType,length);
            }
        }
    }

    /**
     * 转pdf
     *
     * @param ext             文件后缀
     * @param inputStream     输入流
     * @param filePath        文件路径
     * @param fileCharsetName 文件小写名
     */
    static void toPdf(String ext, InputStream inputStream, String filePath, String fileCharsetName,String password) {
        if (OnlineConstants.DOCLIST.contains(ext)) {
            //转pdf
            ConvertPdfUtils.toWordPdf(inputStream, filePath,password);
        } else if (OnlineConstants.XLSLIST.contains(ext)) {
            ConvertPdfUtils.toExcelPdf(inputStream, filePath,password);
        } else if (OnlineConstants.PPTLIST.contains(ext)) {
            ConvertPdfUtils.toPptPdf(inputStream, filePath,password);
        } else if (OnlineConstants.TXTLIST.contains(ext)) {
            ConvertPdfUtils.toTxtPdf(inputStream, filePath, "utf-8");
        } else if (OnlineConstants.TIFFLIST.contains(ext)) {
            try {
                ConvertPdfUtils.toTifPdf(inputStream, filePath);
            } catch (IOException e) {
                log.error("tif转pdf失败", e);
                throw new RuntimeException(e);
            }
        } else if (OnlineConstants.OFDLIST.contains(ext)) {
            ConvertPdfUtils.toOfdPdf(inputStream, filePath);
        } else if (OnlineConstants.HEIFLIST.contains(ext)) {
            ConvertPdfUtils.toHeifPdf(inputStream, filePath);
        }
    }

    /**
     * @param ext             文件后缀
     * @param sourceFilePath  文件路径
     * @param filePath        文件路径
     * @param fileCharsetName 文件小写名
     */
    static void toPdf(String ext, String sourceFilePath, String filePath, String fileCharsetName,String password) {
        if (OnlineConstants.DOCLIST.contains(ext)) {
            //转pdf
            ConvertPdfUtils.toWordPdf(sourceFilePath, filePath,password);
        } else if (OnlineConstants.XLSLIST.contains(ext)) {
            ConvertPdfUtils.toExcelPdf(sourceFilePath, filePath,password);
        } else if (OnlineConstants.PPTLIST.contains(ext)) {
            ConvertPdfUtils.toPptPdf(sourceFilePath, filePath,password);
        } else if (OnlineConstants.TXTLIST.contains(ext)) {
            ConvertPdfUtils.toTxtPdf(sourceFilePath, filePath);
        } else if (OnlineConstants.TIFFLIST.contains(ext)) {
            try {
                ConvertPdfUtils.toTifPdf(sourceFilePath, filePath);
            } catch (IOException e) {
                log.error("tif转pdf失败", e);
                throw new RuntimeException("tif转pdf失败");
            }
        } else if (OnlineConstants.OFDLIST.contains(ext)) {
            ConvertPdfUtils.toOfdPdf(sourceFilePath, filePath);
        } else if (OnlineConstants.HEIFLIST.contains(ext)) {
            ConvertPdfUtils.toHeifPdf(sourceFilePath, filePath);
        }
    }

    /**
     * 文档处理
     *
     * @param url      文件url
     * @param tempDir  临时文件夹
     * @param fileName 文件名
     * @param ext      文件后缀
     */
    private void docDownTranscoding(String url, String tempDir, String fileName, String ext, Integer isEncrypt, String encryptKey, Integer encryptType
            ,Integer length,String password) {
        String path = tempDir + fileName;
        File file = new File(path);
        InputStream in = null;
        try {
            if (!file.exists()) {
                file.createNewFile();
                //在线预览(暂无水印)
                if (FileEncryptUtils.shouldEncrypt(isEncrypt)) {
                    in = FileEncryptUtils.decrypt(FileUtils.getInputStreamFromUrl(url), encryptKey, encryptType,length);
                } else {
                    in = FileUtils.getInputStreamFromUrl(url);
                }
                CountDownLatch latch = new CountDownLatch(1);
                InputStream finalIn = in;
                staticTaskExecutor.execute(() -> {
                    //转pdf
                    toPdf(ext, finalIn, path, null,password);
                    latch.countDown();
                });
                try {
                    //用于阻塞当前线程，直到计数器减为零
                    latch.await();
                } catch (InterruptedException e) {
                    log.error("系统异常",e);
                    throw new RuntimeException(e);
                }finally {
                    if(finalIn!=null){
                        finalIn.close();
                    }
                }
            }
        } catch (IOException e) {
            log.error("系统异常",e);
            throw new RuntimeException(e);
        } catch (NullPointerException e) {
            log.error("系统异常",e);
            throw new NullPointerException("文件内容有不能识别的符号");
        } finally {
            try {
                if (ObjectUtil.isNotEmpty(in)) {
                    in.close();
                    in = null;
                }
            } catch (IOException e) {
                log.error("系统异常",e);
                throw new RuntimeException(e);
            }
        }

    }

    /**
     * 文档处理
     *
     * @param inputStream  输入流
     * @param url          文件url
     * @param tempDir      临时文件夹
     * @param fileId       文件id
     * @param ext          文件后缀
     * @param isEncrypt    是否加密
     * @param encryptKey   加密key
     * @param encryptType 加密标识
     */
    private void docDownTranscoding(InputStream inputStream, String url, String tempDir, Long fileId, String ext, Integer isEncrypt, String encryptKey, Integer encryptType
            ,Integer length,String password) {
        String path = tempDir + fileId + ".pdf";
        File file = new File(path);
        // 解码后文件绝对路径
        String serverFilePath = tempDir + fileId + "." + ext;
        try {
            if (!file.exists()) {
                file.createNewFile();
                //在线预览(暂无水印)
                if (FileEncryptUtils.shouldEncrypt(isEncrypt)) {
                    FileEncryptUtils.saveDecryptFile(inputStream, serverFilePath, encryptKey, encryptType,length);
                    CountDownLatch latch = new CountDownLatch(1);
                    staticTaskExecutor.execute(() -> {
                        //转pdf
                        try {
                            toPdf(ext, serverFilePath, path, null,password);
                        }catch (Exception e){
                            log.error("(加密)转pdf出错",e);
                            file.delete();
                        }
                        latch.countDown();
                    });
                    try {
                        //用于阻塞当前线程，直到计数器减为零
                        latch.await();
                    } catch (InterruptedException e) {
                        log.error("系统异常",e);
                        throw new RuntimeException(e);
                    }
                } else {
                    CountDownLatch latch = new CountDownLatch(1);
                    InputStream finalInputStream = inputStream;
                    staticTaskExecutor.execute(() -> {
                        //转pdf
                        try {
                            toPdf(ext, finalInputStream, path, null,password);
                        }catch (Exception e){
                            log.error("转pdf出错",e);
                            file.delete();
                        }
                        latch.countDown();
                    });
                    try {
                        //用于阻塞当前线程，直到计数器减为零
                        latch.await();
                    } catch (InterruptedException e) {
                        log.error("系统异常",e);
                        throw new RuntimeException(e);
                    }finally {
                        finalInputStream.close();
                    }
                }
            }
        } catch (IOException e) {
            log.error("系统异常",e);
            throw new RuntimeException(e);
        } catch (NullPointerException e) {
            log.error("系统异常",e);
            throw new NullPointerException("文件内容有不能识别的符号");
        } finally {
            if (FileEncryptUtils.shouldEncrypt(isEncrypt)) {
                FileUtil.del(serverFilePath);
            }
            try {
                assert inputStream != null;
                inputStream.close();
                inputStream = null;
            } catch (IOException e) {
                log.error("系统异常",e);
                throw new RuntimeException(e);
            }
        }

    }

    /**
     * 文档处理
     *
     * @param url      文件url
     * @param tempDir  临时文件夹
     * @param fileName 文件名
     * @param ext      文件后缀
     */
    private void docDownTranscoding(String url, String tempDir, String fileName, String ext, String host, Integer port, String username, String password, Integer isEncrypt, String encryptKey, Integer encryptType
            ,Integer length,String filePassword) {
        String path = tempDir + fileName;
        File file = new File(path);
        InputStream in = null;
        try {
            in = getNasFileStream(url, host, port, username, password, isEncrypt, encryptKey, encryptType,length);
        } catch (Exception e) {
            log.error("系统异常",e);
            throw new RuntimeException(e);
        }
        try {
            if (!file.exists()) {
                file.createNewFile();
                //在线预览(暂无水印)
                //转pdf
                CountDownLatch latch = new CountDownLatch(1);
                InputStream finalIn = in;
                String mainName = Thread.currentThread().getName();
                log.info("主线程名称:{}", mainName);
                staticTaskExecutor.execute(() -> {
                    String name = Thread.currentThread().getName();
                    log.info("转pdf线程名称:{}", name);
                    toPdf(ext, finalIn, path, null,password);
                    latch.countDown();
                });
                try {
                    //用于阻塞当前线程，直到计数器减为零
                    latch.await();
                } catch (InterruptedException e) {
                    log.error("系统异常",e);
                    throw new RuntimeException(e);
                }
                log.info("转pdf完成");
                finalIn.close();
            }
        } catch (IOException e) {
            log.error("系统异常",e);
            throw new RuntimeException(e);
        } catch (NullPointerException e) {
            log.error("系统异常",e);
            throw new NullPointerException("文件内容有不能识别的符号");
        } finally {
            try {
                in.close();
                in = null;
            } catch (IOException e) {
                log.error("系统异常",e);
                throw new RuntimeException(e);
            }
        }

    }

    /**
     * 图像处理
     *
     * @param url      文件url
     * @param tempDir  临时文件夹
     * @param fileName 文件名
     */
    private void imgDownTranscoding(String url, String tempDir, String fileName, Integer isEncrypt, String encryptKey, Integer encryptType,Integer length) {
        //生成缩略图
        //转码后的文件路径
        String resultThumbnailFilePath = tempDir + SunCacheDelConstants.THUMBNAIL + fileName.replaceFirst("[.][^.]+$", "") + ".png";
        File file = new File(resultThumbnailFilePath);
        //如果本地服务器上有转码前文件的文件直接拿本地服务器上的文件
        File sourceFile = new File(tempDir + fileName);
        if (!sourceFile.exists()) {
            InputStream in = null;
            try {
                if (FileEncryptUtils.shouldEncrypt(isEncrypt)) {
                    in = FileEncryptUtils.decrypt(FileUtils.getInputStreamFromUrl(url), encryptKey, encryptType,length);
                } else {
                    in = FileUtils.getInputStreamFromUrl(url);
                }
                if (SunCacheUtils.TIFFLIST.contains(fileName.replaceFirst("^.*[.]", "").toLowerCase())) {
                    Thumbnails.of(in)
                            .size(250, 250)
                            .outputFormat("png")
                            .toFile(file);
                } else {
                    Thumbnails.of(in)
                            .size(120, 120)
                            .outputFormat("png")
                            .toFile(file);
                }
            } catch (IOException e) {
                //特殊图片生成缩略图 将图片转为jpg 在生成缩略图
                if (FileEncryptUtils.shouldEncrypt(isEncrypt)) {
                    in = FileEncryptUtils.decrypt(FileUtils.getInputStreamFromUrl(url), encryptKey, encryptType,length);
                } else {
                    in = FileUtils.getInputStreamFromUrl(url);
                }
                try {
                    Thumbnails.of(in)
                            .size(120, 120)
                            .outputFormat("png")
                            .toFile(file);
                } catch (IOException ex) {
                    log.error(ex.toString());
                    throw new RuntimeException(ex);
                }
                log.error("系统异常",e);
                throw new RuntimeException(e);
            }finally {
                if(in!=null){
                    try {
                        in.close();
                    } catch (IOException e) {
                        log.error("系统异常",e);
                    }
                }
            }
        } else {
            try {
                if (SunCacheUtils.TIFFLIST.contains(fileName.replaceFirst("^.*[.]", "").toLowerCase())) {
                    Thumbnails.of(sourceFile)
                            .size(250, 250)
                            .outputFormat("png")
                            .toFile(file);
                } else {
                    Thumbnails.of(sourceFile)
                            .size(120, 120)
                            .outputFormat("png")
                            .toFile(file);
                }

            } catch (IOException e) {
                log.error("系统异常",e);
                throw new RuntimeException(e);
            }
        }

    }


    /**
     * 图像处理
     *
     * @param inputStream  输入流
     * @param url          文件url
     * @param tempDir      临时文件夹
     * @param fileId       文件id
     * @param fileExt      原文件后缀
     * @param isEncrypt    是否加密
     * @param encryptKey   加密key
     * @param encryptType 加密标识
     */
    private static void imgDownTranscoding(InputStream inputStream, String url, String tempDir, Long fileId, String fileExt, Integer isEncrypt, String encryptKey, Integer encryptType,Integer length) {
        //生成缩略图
        //转码后的文件路径
        String resultThumbnailFilePath = tempDir + SunCacheDelConstants.THUMBNAIL + fileId + ".png";
        File file = new File(resultThumbnailFilePath);
        if (!file.exists()) {
            //如果本地服务器上有转码前文件的文件直接拿本地服务器上的文件
            File sourceFile = new File(tempDir + fileId + fileExt);
            String serverFilePath = tempDir + UUID.randomUUID() + "." + fileExt;
            boolean isNas = false;
            if (!sourceFile.exists()) {
                try {
                    //缩略图临时路径
                    if (FileEncryptUtils.shouldEncrypt(isEncrypt)) {
                        FileEncryptUtils.saveDecryptFile(inputStream, serverFilePath, encryptKey, encryptType,length);
                    } else {
                        //保存文件到临时路径 用于转码
                        if (!new File(url).exists()) {
                            FileUtils.cpFile(inputStream, new File(serverFilePath));
                        } else {
                            //原文件保存在NAS中、并且没加密
                            serverFilePath = url;
                            isNas = true;
                        }
                    }
                    if (SunCacheUtils.TIFFLIST.contains(fileExt)) {
                        Thumbnails.of(serverFilePath)
                                .size(250, 250)
                                .outputFormat("png")
                                .toFile(file);
                    } else if (SunCacheUtils.HEIC.contains(fileExt)) {
                        Thumbnails.of(ImageToPngUtils.specialImagesToJpg(new FileInputStream(serverFilePath)))
                                .size(120, 120)
                                .outputFormat("png")
                                .toFile(file);
                    } else {
                        Thumbnails.of(serverFilePath)
                                .size(120, 120)
                                .outputFormat("png")
                                .toFile(file);
                    }
                } catch (IOException e) {
                    //特殊图片生成缩略图 将图片转为jpg 在生成缩略图
                    try {
                        inputStream = ImageToPngUtils.specialImagesToPng(new FileInputStream(serverFilePath));
                    } catch (FileNotFoundException ex) {
                        log.error(ex.toString());
                        throw new RuntimeException(ex);
                    }
                    try {
                        Thumbnails.of(inputStream)
                                .size(120, 120)
                                .outputFormat("png")
                                .toFile(file);
                    } catch (IOException ex) {
                        log.error(ex.toString());
                        throw new RuntimeException(ex);
                    }
                    log.error("系统异常",e);
                    throw new RuntimeException(e);
                }
            } else {
                try {
                    if (SunCacheUtils.TIFFLIST.contains(fileExt)) {
                        Thumbnails.of(sourceFile)
                                .size(250, 250)
                                .outputFormat("png")
                                .toFile(file);
                    } else {
                        Thumbnails.of(sourceFile)
                                .size(120, 120)
                                .outputFormat("png")
                                .toFile(file);
                    }

                } catch (IOException e) {
                    log.error("系统异常",e);
                    throw new RuntimeException(e);
                }
            }
            if (!isNas) {
                //删除临时文件
                FileUtil.del(serverFilePath);
            }
            //关闭文件流
            try {
                if (ObjectUtil.isNotEmpty(inputStream)) {
                    inputStream.close();
                }
            } catch (IOException e) {
                log.error("系统异常",e);
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * 图像处理
     *
     * @param url      文件url
     * @param tempDir  临时文件夹
     * @param fileName 文件名
     */
    private  void imgDownTranscoding(String url, String tempDir, String fileName, String host, Integer port, String username, String password, Integer isEncrypt, String encryptKey, Integer encryptType,Integer length) {
        //下载完成后进行转码
        String resultFilePath = tempDir + SunCacheDelConstants.THUMBNAIL + fileName.split("\\.")[0] + ".png";
        File file = new File(resultFilePath);
        try ( InputStream inputStream = getNasFileStream(url, host, port, username, password, isEncrypt, encryptKey, encryptType,length);){
            if (SunCacheUtils.TIFFLIST.contains(fileName.replaceFirst("^.*[.]", "").toLowerCase())) {
                Thumbnails.of(inputStream)
                        .size(250, 250)
                        .outputFormat("png")
                        .toFile(file);
            } else if (SunCacheUtils.HEIC.contains(fileName.replaceFirst("^.*[.]", "").toLowerCase())) {
                Thumbnails.of(ImageToPngUtils.specialImagesToJpg(inputStream))
                        .size(120, 120)
                        .outputFormat("png")
                        .toFile(file);
            } else {
                Thumbnails.of(inputStream)
                        .size(120, 120)
                        .outputFormat("png")
                        .toFile(file);
            }
        } catch (Exception e) {

            try ( //特殊图片生成缩略图 将图片转为jpg 在生成缩略图
                  InputStream inputStream = getNasFileStream(url, host, port, username, password, isEncrypt, encryptKey, encryptType,length);
                  InputStream in = ImageToPngUtils.specialImagesToPng(inputStream)){
                Thumbnails.of(in)
                        .size(120, 120)
                        .outputFormat("png")
                        .toFile(file);
            } catch (IOException ex) {
                log.error(ex.toString());
                throw new RuntimeException(ex);
            }
            log.error("系统异常",e);
            throw new RuntimeException(e);
        }
    }


    /**
     * 视屏处理
     *
     * @param url      文件url
     * @param tempDir  临时文件夹
     * @param fileName 文件名
     */
    private void videoDownTranscoding(String url, String tempDir, String fileName,
                                             Integer isEncrypt, String encryptKey, Integer encryptType,Integer length) {
        String serverFilePath = getInputByUrl(url, tempDir, fileName, isEncrypt, encryptKey, encryptType,length);
        if (ObjectUtil.isNotEmpty(serverFilePath)) {
            log.info("{}:下载成功", serverFilePath);
            //下载完成后进行转码
            String newFileName = fileName.substring(0, fileName.lastIndexOf(".")) + ".mp4";
            String resultFilePath = tempDir + newFileName;
            //转码
            //进行转码
            log.info("进行转码：{}->{}", serverFilePath, resultFilePath);
            CountDownLatch latch = new CountDownLatch(1);
            staticTaskExecutor.execute(() -> {
                VideoUtil.convertVideoToMP4(serverFilePath, resultFilePath, 1);
                latch.countDown();
            });
            try {
                //用于阻塞当前线程，直到计数器减为零
                latch.await();
            } catch (InterruptedException e) {
                log.error("系统异常",e);
                throw new RuntimeException(e);
            }
            log.info("转码完成：{}->{}", serverFilePath, resultFilePath);
            //删除转码前文件
            File file = new File(serverFilePath);
            if (file.exists()) {
                FileUtil.del(file);
            }
        }
    }

    /**
     * 视屏处理
     *
     * @param inputStream  输入流
     * @param url          文件url
     * @param tempDir      临时文件夹
     * @param fileId       文件id
     * @param ext          原文件后缀
     * @param isEncrypt    是否加密
     * @param encryptKey   加密key
     * @param encryptType 加密标识
     */
    private void videoDownTranscoding(InputStream inputStream, String url, String tempDir, Long fileId, String ext,
                                             Integer isEncrypt, String encryptKey,Integer encryptType,Integer length) {
        String serverFilePath = tempDir + fileId + "." + ext;
        if (new File(url).exists()) {
            //文件在NAS上
            String newFileName = fileId + ".mp4";
            String resultFilePath = tempDir + newFileName;
            //加密文件先解码在转码
            if (FileEncryptUtils.shouldEncrypt(isEncrypt)) {
                FileEncryptUtils.saveDecryptFile(inputStream, serverFilePath, encryptKey,encryptType,length);
            } else {
                serverFilePath = url;
            }
            //转码
            //进行转码
            log.info("进行转码：{}->{}", serverFilePath, resultFilePath);
            CountDownLatch latch = new CountDownLatch(1);
            String finalServerFilePath = serverFilePath;
            staticTaskExecutor.execute(() -> {
                VideoUtil.convertVideoToMP4(finalServerFilePath, resultFilePath, 1);
                latch.countDown();
            });
            try {
                //用于阻塞当前线程，直到计数器减为零
                latch.await();
            } catch (InterruptedException e) {
                log.error("系统异常",e);
                throw new RuntimeException(e);
            }
            log.info("转码完成：{}->{}", serverFilePath, resultFilePath);
            //删除临时解密文件
            if (FileEncryptUtils.shouldEncrypt(isEncrypt)) {
                FileUtil.del(serverFilePath);
            }
        } else {
            //文件在远程，需要先下载到本地
            // 服务器文件保存路径
            if (FileEncryptUtils.shouldEncrypt(isEncrypt)) {
                //解密并保存解密后的文件 用于转码
                FileEncryptUtils.saveDecryptFile(inputStream, serverFilePath, encryptKey, encryptType,length);
            } else {
                //保存文件到临时路径 用于转码
                FileUtils.cpFile(inputStream, new File(serverFilePath));
            }
            if (ObjectUtil.isNotEmpty(serverFilePath)) {
                log.info("{}:下载成功", serverFilePath);
                //下载完成后进行转码
                String newFileName = fileId + ".mp4";
                String resultFilePath = tempDir + newFileName;
                //转码
                //进行转码
                log.info("进行转码：{}->{}", serverFilePath, resultFilePath);
                CountDownLatch latch = new CountDownLatch(1);
                String finalServerFilePath1 = serverFilePath;
                staticTaskExecutor.execute(() -> {
                    VideoUtil.convertVideoToMP4(finalServerFilePath1, resultFilePath, 1);
                    latch.countDown();
                });
                try {
                    //用于阻塞当前线程，直到计数器减为零
                    latch.await();
                } catch (InterruptedException e) {
                    log.error("系统异常",e);
                    throw new RuntimeException(e);
                }
                log.info("转码完成：{}->{}", serverFilePath, resultFilePath);
                //删除转码前文件
                File file = new File(serverFilePath);
                if (file.exists()) {
                    FileUtil.del(file);
                }
            }
        }
    }

    /**
     * 视屏处理
     *
     * @param url      文件url
     * @param tempDir  临时文件夹
     * @param fileName 文件名
     */
    private  void videoDownTranscoding(String url, String tempDir, String fileName, String host, Integer port, String username, String password, Integer isEncrypt, String encryptKey, Integer encryptType,Integer length) {
        if (new File(url).exists()) {
            String newFileName = fileName.substring(0, fileName.lastIndexOf(".")) + ".mp4";
            String resultFilePath = tempDir + newFileName;
            //加密文件先解码在转码
            String temp = tempDir + UUID.randomUUID() + "." + url.replaceFirst("^.*[.]", "").toLowerCase();
            temp = isEncrypt(url, tempDir, isEncrypt, encryptKey, encryptType, temp,length);
            //转码
            //进行转码
            String finalTemp = temp;
            log.info("进行转码：{}->{}", finalTemp, resultFilePath);
            CountDownLatch latch = new CountDownLatch(1);
            staticTaskExecutor.execute(() -> {
                VideoUtil.convertVideoToMP4(finalTemp, resultFilePath, 1);
                latch.countDown();
            });
            try {
                //用于阻塞当前线程，直到计数器减为零
                latch.await();
            } catch (InterruptedException e) {
                log.error("系统异常",e);
                throw new RuntimeException(e);
            }
            log.info("转码完成：{}->{}", finalTemp, resultFilePath);
            //删除临时解密文件
            if (FileEncryptUtils.shouldEncrypt(isEncrypt)) {
                FileUtil.del(temp);
            }
        } else {
            log.info("文件不存在：{}", url);
        }

    }

    /**
     * 如果是加密文件就要先解码
     *
     * @param url            文件url
     * @param tempDir        临时文件夹
     * @param isEncrypt      是否加密
     * @param encryptKey     加密key
     * @param encryptType   加密标识
     * @param resultFilePath 结果文件路径
     * @param length 加密长度
     * @return Result
     */
    private static String isEncrypt(String url, String tempDir, Integer isEncrypt, String encryptKey, Integer encryptType, String resultFilePath,Integer length) {
        if (FileEncryptUtils.shouldEncrypt(isEncrypt)) {
            try (FileOutputStream out = new FileOutputStream(resultFilePath);
                 BufferedOutputStream bos = new BufferedOutputStream(out)) {
                // 判断是否需要解密
                InputStream inputStream = FileEncryptUtils.decrypt(new FileInputStream(url), encryptKey, encryptType,length);
                if (inputStream == null || inputStream.available() == 0) {
                    log.info("从存储设备中获取的文件流为空1");
                    return null;
                } else {
                    byte[] buffer = new byte[8192];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        bos.write(buffer, 0, bytesRead);
                    }
                    //确保缓冲区数据被立即刷新到文件
                    bos.flush();
                }
            } catch (Exception e) {
                log.error("系统异常",e);
                throw new RuntimeException(e);
            }
        } else {
            return url;
        }
        return resultFilePath;
    }

    /**
     * 音频处理
     *
     * @param url      文件url
     * @param tempDir  临时文件夹
     * @param fileName 文件名
     */
    private void audioDownTranscoding(String url, String tempDir, String fileName, Integer isEncrypt, String encryptKey, Integer encryptType,Integer length) {
        String serverFilePath = getInputByUrl(url, tempDir, fileName, isEncrypt, encryptKey, encryptType,length);
        if (ObjectUtil.isNotEmpty(serverFilePath)) {
            log.info("{}:下载成功", serverFilePath);
            //下载完成后进行转码
            String newFileName = fileName.substring(0, fileName.lastIndexOf(".")) + ".mp3";
            String resultFilePath = tempDir + newFileName;
            CountDownLatch latch = new CountDownLatch(1);
            staticTaskExecutor.execute(() -> {
                //转码
                VideoUtil.convertAudioToMP3(serverFilePath, resultFilePath);
                latch.countDown();
            });
            try {
                //用于阻塞当前线程，直到计数器减为零
                latch.await();
            } catch (InterruptedException e) {
                log.error("系统异常",e);
                throw new RuntimeException(e);
            }
            //删除转码前文件
            File file = new File(serverFilePath);
            if (file.exists()) {
                FileUtil.del(file);
            }
        }
    }

    /**
     * 音频处理
     *
     * @param url      文件url
     * @param tempDir  临时文件夹
     * @param fileName 文件名
     */
    private void audioDownTranscoding(String url, String tempDir, String fileName, String host, Integer port,
                                             String username, String password, Integer isEncrypt, String encryptKey, Integer encryptType,Integer length) {
        //下载完成后进行转码
        String newFileName = fileName.substring(0, fileName.lastIndexOf(".")) + ".mp3";
        String resultFilePath = tempDir + newFileName;
        //加密文件先解码在转码
        String temp = tempDir + UUID.randomUUID() + "." + url.replaceFirst("^.*[.]", "").toLowerCase();
        temp = isEncrypt(url, tempDir, isEncrypt, encryptKey, encryptType, temp,length);
        CountDownLatch latch = new CountDownLatch(1);
        String finalTemp = temp;
        staticTaskExecutor.execute(() -> {
            //转码
            VideoUtil.convertAudioToMP3(finalTemp, resultFilePath);
            latch.countDown();
        });
        try {
            //用于阻塞当前线程，直到计数器减为零
            latch.await();
        } catch (InterruptedException e) {
            log.error("系统异常",e);
            throw new RuntimeException(e);
        }
        //删除临时解密文件
        if (FileEncryptUtils.shouldEncrypt(isEncrypt)) {
            FileUtil.del(temp);
        }
    }

    /**
     * 音频处理
     *
     * @param inputStream  输入流
     * @param url          文件url
     * @param tempDir      临时文件夹
     * @param fileId       文件id
     * @param fileExt      文件后缀
     * @param isEncrypt    是否加密
     * @param encryptKey   加密key
     * @param encryptType 加密标识
     */
    public void audioDownTranscoding(InputStream inputStream, String url, String tempDir, Long fileId, String fileExt,
                                            Integer isEncrypt, String encryptKey, Integer encryptType,Integer length) {
        String serverFilePath = tempDir + fileId + "." + fileExt;
        if (new File(url).exists()) {
            //文件在NAS上
            String newFileName = fileId + ".mp3";
            String resultFilePath = tempDir + newFileName;
            //加密文件先解码在转码
            if (FileEncryptUtils.shouldEncrypt(isEncrypt)) {
                FileEncryptUtils.saveDecryptFile(inputStream, serverFilePath, encryptKey, encryptType,length);
            } else {
                serverFilePath = url;
            }
            //转码
            //进行转码
            log.info("进行转码：{}->{}", serverFilePath, resultFilePath);
            CountDownLatch latch = new CountDownLatch(1);
            String finalServerFilePath = serverFilePath;
            staticTaskExecutor.execute(() -> {
                VideoUtil.convertAudioToMP3(finalServerFilePath, resultFilePath);
                latch.countDown();
            });
            try {
                //用于阻塞当前线程，直到计数器减为零
                latch.await();
            } catch (InterruptedException e) {
                log.error("系统异常",e);
                throw new RuntimeException(e);
            }
            log.info("转码完成：{}->{}", serverFilePath, resultFilePath);
            //删除临时解密文件
            if (FileEncryptUtils.shouldEncrypt(isEncrypt)) {
                FileUtil.del(serverFilePath);
            }
        } else {
            //文件在远程，需要先下载到本地
            // 服务器文件保存路径
            if (FileEncryptUtils.shouldEncrypt(isEncrypt)) {
                //解密并保存解密后的文件 用于转码
                FileEncryptUtils.saveDecryptFile(inputStream, serverFilePath, encryptKey, encryptType,length);
            } else {
                //保存文件到临时路径 用于转码
                FileUtils.cpFile(inputStream, new File(serverFilePath));
            }
            if (ObjectUtil.isNotEmpty(serverFilePath)) {
                log.info("{}:下载成功", serverFilePath);
                //下载完成后进行转码
                String newFileName = fileId + ".mp3";
                String resultFilePath = tempDir + newFileName;
                //转码
                //进行转码
                log.info("进行转码：{}->{}", serverFilePath, resultFilePath);
                CountDownLatch latch = new CountDownLatch(1);
                String finalServerFilePath1 = serverFilePath;
                staticTaskExecutor.execute(() -> {
                    VideoUtil.convertAudioToMP3(finalServerFilePath1, resultFilePath);
                    latch.countDown();
                });
                try {
                    //用于阻塞当前线程，直到计数器减为零
                    latch.await();
                } catch (InterruptedException e) {
                    log.error("系统异常",e);
                    throw new RuntimeException(e);
                }
                log.info("转码完成：{}->{}", serverFilePath, resultFilePath);
                //删除转码前文件
                File file = new File(serverFilePath);
                if (file.exists()) {
                    FileUtil.del(file);
                }
            }
        }
        //关闭文件流
        try {
            if (inputStream != null) {
                inputStream.close();
            }
        } catch (IOException e) {
            log.error("系统异常",e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 根据url获取源文件(将文件流下载到本地)
     *
     * @param url      文件url
     * @param tempDir  临时文件夹
     * @param fileName 文件名
     * @return Result
     */
    public  String getInputByUrl(String url, String tempDir, String fileName,
                                       Integer isEncrypt, String encryptKey, Integer encryptType, Integer length) {

        // 1. 创建安全临时文件（自动设置600权限）
        Path filePath = Paths.get(tempDir).resolve(fileName).normalize();
        try {
            Files.createDirectories(filePath.getParent());
            File file = filePath.toFile();

            if (!file.exists()) {
                // 2. 使用安全的内存缓冲区（DirectByteBuffer减少内存残留）
                try (InputStream in = getSecureInputStream(url, isEncrypt, encryptKey, encryptType, length);
                     FileChannel channel = FileChannel.open(
                             filePath,
                             StandardOpenOption.CREATE,
                             StandardOpenOption.WRITE,
                             StandardOpenOption.TRUNCATE_EXISTING)) {

                    // 3. 使用NIO传输数据（可配置直接缓冲区）
                    ByteBuffer buffer = ByteBuffer.allocateDirect(1024);
                    while (in.read(buffer.array()) != -1) {
                        buffer.flip();
                        channel.write(buffer);
                        buffer.clear();
                    }
                    // 4. 强制刷盘并同步元数据
                    channel.force(true);
                }
            }
            return filePath.toString();
        } catch (IOException e) {
            // 5. 安全异常处理（不泄露密钥和文件内容）
            log.error("文件操作失败（原因：{}）", e.getClass().getSimpleName());
            try { Files.deleteIfExists(filePath); } catch (IOException ignored) {}
            throw new RuntimeException("文件处理失败", e);
        }
    }

    // 安全输入流封装方法
    private static InputStream getSecureInputStream(String url, Integer isEncrypt,
                                                    String encryptKey, Integer encryptType, Integer length)
            throws IOException {
        InputStream in = FileUtils.getInputStreamFromUrl(url);
        if (FileEncryptUtils.shouldEncrypt(isEncrypt)) {
            try {
                in = FileEncryptUtils.decrypt(in, encryptKey, encryptType, length);
                // 7. 关键安全点：立即清除内存中的密钥引用
                Arrays.fill(encryptKey.toCharArray(), '\0');
            } catch (Exception e) {
                IOUtils.closeQuietly(in);
                throw new IOException("解密失败", e);
            }
        }
        return in;
    }

    /**
     * 根据url获取源文件(将文件流下载到本地)
     *
     * @param inputStream  输入流
     * @param url          文件url
     * @param tempDir      临时文件夹
     * @param fileId       文件id
     * @param ext          文件后缀
     * @param isEncrypt    是否加密
     * @param encryptKey   加密key
     * @param encryptType 加密标识
     * @return Result
     */
    public static String saveCacheFile(InputStream inputStream, String url, String tempDir, Long fileId, String ext,
                                       Integer isEncrypt, String encryptKey, Integer encryptType,Integer length) {
        /**
         * 服务器文件保存路径
         */
        String serverFilePath = tempDir + fileId + "." + ext;
        File file = new File(serverFilePath);
        if (!file.exists()) {
            if (FileEncryptUtils.shouldEncrypt(isEncrypt)) {
                //解密并保存解密后的文件 用于转码
                FileEncryptUtils.saveDecryptFile(inputStream, serverFilePath, encryptKey, encryptType,length);
            } else {
                FileUtils.cpFile(inputStream, new File(serverFilePath));
            }
        }
        //关闭文件流
//        try {
//            if (ObjectUtil.isNotEmpty(inputStream)) {
//                inputStream.close();
//            }
//        } catch (IOException e) {
//            log.error("系统异常",e);
//            throw new RuntimeException(e);
//        }
        return serverFilePath;
    }

    /**
     * 获取nas服务器的文件流
     *
     * @param url      文件url
     * @param host     ip
     * @param port     端口
     * @param username 账号
     * @param password 密码
     * @return Result
     */
    private static InputStream getNasFileStream(String url, String host, Integer port, String username, String password, Integer isEncrypt, String encryptKey, Integer encryptType,Integer length) {
        try {
            if (FileEncryptUtils.shouldEncrypt(isEncrypt)) {
                return FileEncryptUtils.decrypt(new FileInputStream(url), encryptKey, encryptType,length);
            } else {
                //加密文件先解码
                File file = new File(url);
                if (file.exists()) {
                    try {
                        return new FileInputStream(url);
                    } catch (FileNotFoundException e) {
                        log.error("系统异常",e);
                        throw new RuntimeException(e);
                    }
                }
            }
        } catch (Exception e) {
            log.error("系统异常",e);
            throw new RuntimeException(e);
        }
        return null;
    }

    /**
     * 生产缩略图和pdf
     * @param inputStream
     * @param url
     * @param tempDir
     * @param fileId
     * @param fileExt
     * @param isEncrypt
     * @param encryptKey
     * @param encryptType
     */
    private void imgDocDownTranscoding(InputStream inputStream, String url, String tempDir, Long fileId, String fileExt, Integer isEncrypt, String encryptKey, Integer encryptType,Integer length) {
        //生成缩略图
        // 解码后文件绝对路径
        String serverFilePath = tempDir + fileId + "." + fileExt;
        File file = new File(serverFilePath);
        //文件缩略图路径
        String resultThumbnailFilePath = tempDir + SunCacheDelConstants.THUMBNAIL + fileId + ".png";
        File fileT = new File(resultThumbnailFilePath);
        //文件pdf路径
        String path = tempDir + fileId + ".pdf";
        File fileP = new File(path);
        if (!fileP.exists()) {
            try {
                fileP.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        //解码并缓存到本地
        if (!file.exists()) {
            if (FileEncryptUtils.shouldEncrypt(isEncrypt)) {
                FileEncryptUtils.saveDecryptFile(inputStream, serverFilePath, encryptKey, encryptType,length);
            } else {
                FileUtils.cpFile(inputStream, file);
            }
        }
        try {
            //生成pdf
            creatPdf(fileExt, fileP, serverFilePath, path);
            //生成缩略图
            creatThum(inputStream,fileExt, fileT, serverFilePath);
        } catch (Exception e) {
            log.error("系统异常",e);
            throw new NullPointerException("文件内容有不能识别的符号");
        } finally {
            //删除临时文件
            file.delete();
            try {
                assert inputStream != null;
                inputStream.close();
                inputStream = null;
            } catch (IOException e) {
                log.error("系统异常",e);
                throw new RuntimeException(e);
            }
        }

    }

    /**
     * 生成缩略图
     * @param inputStream
     * @param fileExt
     * @param fileT
     * @param serverFilePath
     */
    private void creatThum(InputStream inputStream,String fileExt, File fileT, String serverFilePath) {
        if (!fileT.exists()) {
            try {
//                fileT.createNewFile();
                if (SunCacheUtils.TIFFLIST.contains(fileExt)) {
                    Thumbnails.of(serverFilePath)
                            .size(250, 250)
                            .outputFormat("png")
                            .toFile(fileT);
                } else if (SunCacheUtils.HEIC.contains(fileExt)) {
                    Thumbnails.of(ImageToPngUtils.specialImagesToJpg(new FileInputStream(serverFilePath)))
                            .size(120, 120)
                            .outputFormat("png")
                            .toFile(fileT);
                } else {
                    Thumbnails.of(serverFilePath)
                            .size(120, 120)
                            .outputFormat("png")
                            .toFile(fileT);
                }
            } catch (IOException e) {
                //特殊图片生成缩略图 将图片转为jpg 在生成缩略图
                try(InputStream is = new FileInputStream(serverFilePath)) {
                    inputStream = ImageToPngUtils.specialImagesToPng(is);
                } catch (FileNotFoundException ex) {
                    log.error(ex.toString());
                    throw new RuntimeException(ex);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
                try {
                    Thumbnails.of(inputStream)
                            .size(120, 120)
                            .outputFormat("png")
                            .toFile(fileT);
                } catch (IOException ex) {
                    log.error(ex.toString());
                    throw new RuntimeException(ex);
                }
                log.error("系统异常",e);
                throw new RuntimeException(e);

            }
            //关闭文件流
            try {
                if (ObjectUtil.isNotEmpty(inputStream)) {
                    inputStream.close();
                }
            } catch (IOException e) {
                log.error("系统异常",e);
                throw new RuntimeException(e);
            }
        }
    }
    /**
     * 生产pdf
     * @param fileExt
     * @param fileP
     * @param serverFilePath
     * @param path
     * @throws IOException
     */
    private void creatPdf(String fileExt, File fileP, String serverFilePath, String path) throws IOException {
        //在线预览(暂无水印)
        CountDownLatch latch = new CountDownLatch(1);
        staticTaskExecutor.execute(() -> {
            //转pdf
            try {
                toPdf(fileExt, serverFilePath, path, null,null);
            }catch (Exception e){
                log.error("(加密)转pdf出错",e);
            }
            latch.countDown();
        });
        try {
            //用于阻塞当前线程，直到计数器减为零
            latch.await();
        } catch (InterruptedException e) {
            log.error("系统异常",e);
            throw new RuntimeException(e);
        }

    }

    /**
     * 缓存文件
     *
     * @param inputStream  输入流
     * @param url          文件url
     * @param tempDir      临时文件夹
     * @param fileId       文件id
     * @param fileExt      文件后缀
     * @param isEncrypt    是否加密
     * @param encryptKey   加密key
     * @param encryptType 加密类型
     */
    public void cacheFile(InputStream inputStream, String url, String tempDir, Long fileId, String newExt, String fileExt, Integer isEncrypt, String encryptKey, Integer encryptType
            ,Integer length,String password) {
        File tempDirectory = new File(tempDir);
        // 如果目录不存在，则创建所有父级目录
        if (!tempDirectory.exists()) {
            boolean isCreated = tempDirectory.mkdirs();
            if (!isCreated) {
                throw new RuntimeException("无法创建临时目录: " + tempDir);
            }
        }
        String extLowerCase = fileExt.toLowerCase();
        boolean isDoc = "pdf".equals(newExt) && !"pdf".equals(extLowerCase);
        if (SunCacheUtils.VIDEOS.contains(extLowerCase)) {
            //下载转码视频文件.wmv,.asf,.rm,.rmvb,.mov,.mp4,.avi
            videoDownTranscoding(inputStream, url, tempDir, fileId, fileExt, isEncrypt, encryptKey, encryptType,length);
        } else if (SunCacheUtils.HEIF.contains(extLowerCase) || SunCacheUtils.TIFFLIST.contains(extLowerCase)) {
            // 图片生成缩略图处理和文档转换pdf处理
            imgDocDownTranscoding(inputStream, url, tempDir, fileId, extLowerCase, isEncrypt, encryptKey, encryptType,length);
        }else if (SunCacheUtils.IMGS.contains(extLowerCase) || "png".equals(newExt)) {
            // 图片生成缩略图处理
            imgDownTranscoding(inputStream, url, tempDir, fileId, extLowerCase, isEncrypt, encryptKey, encryptType,length);
        } else if (SunCacheUtils.DOCS.contains(extLowerCase) || SunCacheUtils.OFD.contains(extLowerCase) || isDoc) {
            //文档转换pdf处理
            docDownTranscoding(inputStream, url, tempDir, fileId, extLowerCase, isEncrypt, encryptKey, encryptType,length,password);
        } else if (SunCacheUtils.AUDIOS.contains(extLowerCase)) {
            //下载转码音频文件 amr、ogg、m4a
            audioDownTranscoding(inputStream, url, tempDir, fileId, fileExt, isEncrypt, encryptKey, encryptType,length);
        } else {
            saveCacheFile(inputStream, url, tempDir, fileId, extLowerCase, isEncrypt, encryptKey, encryptType,length);
        }
    }
}
