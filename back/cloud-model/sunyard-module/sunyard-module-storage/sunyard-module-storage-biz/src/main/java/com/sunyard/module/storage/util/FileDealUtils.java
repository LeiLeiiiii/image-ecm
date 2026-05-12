package com.sunyard.module.storage.util;


import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FilenameUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;

import com.spire.pdf.PdfDocument;
import com.spire.pdf.PdfDocumentBase;
import com.sunyard.framework.common.exception.SunyardException;
import com.sunyard.framework.common.result.ResultCode;
import com.sunyard.framework.common.util.FileUtils;
import com.sunyard.framework.spire.constant.OnlineConstants;
import com.sunyard.module.storage.constant.FileConstants;
import com.sunyard.module.storage.constant.StateConstants;
import com.sunyard.module.storage.dto.StFileDTO;
import com.sunyard.module.storage.po.StEquipment;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * 处理文件工具类
 *
 * @author PJW
 */
@Slf4j
public class FileDealUtils {
    static String COMMA = ".";
    private final static String RANGE = "range";

    /**
     * MultipartFile转为file
     *
     * @return
     * @throws IOException
     */
    public static File multipartFileToFile(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        String extension = FilenameUtils.getExtension(originalFilename);
        String filenameWithoutExtension = FilenameUtils.removeExtension(originalFilename);
        File tempFile = null;
        try {
            tempFile = Files.createTempFile(filenameWithoutExtension, extension).toFile();

            try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                fos.write(file.getBytes());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return tempFile;

    }

    /**
     * 获取oss服务器的文件流
     *
     * @param stFile stFile
     * @return Result
     */
    public static InputStream getOssFileStream(StFileDTO stFile) {
        InputStream inputStream;
        inputStream = FileUtils.getInputStreamFromUrl(stFile.getUrl());
        try {
            if (inputStream == null || 0 == inputStream.available()) {
                throw new SunyardException(ResultCode.PARAM_ERROR, "原文件在存储设备中不存在!");
            }
        } catch (IOException e) {
            log.error("异常描述", e);
            throw new RuntimeException(e);
        }
        return inputStream;
    }


    /**
     * 获取nas服务器的文件流
     *
     * @param stFile stFile
     * @return Result
     */
    public static InputStream getNasFileStream(StFileDTO stFile, StEquipment stEquipment) {
        File file = new File(stFile.getUrl());
        if (file.exists()) {
            try {
                return new FileInputStream(file);
            } catch (FileNotFoundException e) {
                log.error("异常描述", e);
                throw new RuntimeException(e);
            }
        }
        return null;
    }


    /**
     * 获取文件流
     *
     * @param stFile          stFile
     * @param stEquipmentList 存储设备集
     * @return Result
     */
    public static InputStream getFileStream(StFileDTO stFile, List<StEquipment> stEquipmentList) {
        if (ObjectUtils.isEmpty(stEquipmentList)) {
            throw new SunyardException(ResultCode.PARAM_ERROR, "存储设备信息有误");
        }
        InputStream inputStream = null;
        StEquipment stEquipment = stEquipmentList.get(0);
        if (StateConstants.COMMON_STORAGE_TYPE_LOCAL.equals(stEquipment.getStorageType())) {
            if (StateConstants.IS_ENCRYPT.equals(stFile.getIsEncrypt())) {
                inputStream = FileEncryptUtils.decrypt(getNasFileStream(stFile, stEquipment), stFile.getEncryptKey(), stFile.getEncryptType(), stFile.getEncryptLen()==null?0:stFile.getEncryptLen().intValue());
            } else {
                //获取nas服务器的文件流
                inputStream = getNasFileStream(stFile, stEquipment);
            }
            log.info("nas获取文件流:,{}", inputStream);
        } else if (StateConstants.COMMON_STORAGE_TYPE_OBJ.equals(stEquipment.getStorageType())) {
            //获取oss服务器的文件流
            if (StateConstants.IS_ENCRYPT.equals(stFile.getIsEncrypt())) {
                inputStream = FileEncryptUtils.decrypt(getOssFileStream(stFile), stFile.getEncryptKey(), stFile.getEncryptType(), stFile.getEncryptLen()==null?0:stFile.getEncryptLen().intValue());
            } else {
                //获取nas服务器的文件流
                inputStream = getOssFileStream(stFile);
            }
            log.info("oss获取文件流:,{}", inputStream);
        }

        return inputStream;
    }

    /**
     * 判断是否是pdf
     *
     * @param ext 后缀
     * @return
     */
    public static boolean isPdf(String ext) {
        return OnlineConstants.DOCLIST.contains(ext)
                || OnlineConstants.XLSLIST.contains(ext)
                || OnlineConstants.PPTLIST.contains(ext)
                || OnlineConstants.TXTLIST.contains(ext)
                || OnlineConstants.TIFFLIST.contains(ext)
                || OnlineConstants.OFDLIST.contains(ext)
                || OnlineConstants.HEIFLIST.contains(ext);

    }

    /**
     * 将多个pdf文件流合成一个pdf
     *
     * @param pdfStreams pdf文件流
     * @return Result
     */
    public static InputStream mergePdfStreams(List<InputStream> pdfStreams) {
        InputStream[] inputStreamArray = pdfStreams.toArray(new InputStream[0]);
        //合并这些文档并返回一个 PdfDocumentBase 类对象
        PdfDocumentBase doc = PdfDocument.mergeFiles(inputStreamArray);
        // 将PDF流合并为单个流
        ByteArrayOutputStream mergedStream = new ByteArrayOutputStream();
        doc.save(mergedStream);
        return new ByteArrayInputStream(mergedStream.toByteArray());
    }

    public static String getPath(String endpoint, String bucket, String objectKey) {
        return StrUtil.format("{}/{}/{}", endpoint, bucket, objectKey);
    }

    /**
     * 获取nas上传路径
     *
     * @param basePath       基础路径
     * @param storageAddress 节点地址
     * @param fileName1      文件名称(随机)
     * @param suffix         文件后缀
     * @return Result
     */
    public static String getLocalUploadPath(String basePath, String storageAddress, String fileName1, String suffix) {
        String url = basePath + File.separator + DateUtil.format(new Date(), "yyyy-MM-dd") + File.separator;
        String fileName = StrUtil.format("{}.{}", fileName1, suffix);
        return StrUtil.format("{}/{}{}", storageAddress, url, fileName);
    }


    /**
     * 根据“文件名的后缀”获取文件内容类型（而非根据File.getContentType()读取的文件类型）
     *
     * @param returnFileName 带验证的文件名
     * @return Result 返回文件类型
     */
    public static String getContentType(String returnFileName) {
        String contentType = "application/octet-stream";
        if (returnFileName.lastIndexOf(COMMA) < 0) {
            return contentType;
        }
        returnFileName = returnFileName.toLowerCase();
        returnFileName = returnFileName.substring(returnFileName.lastIndexOf(".") + 1);
        switch (returnFileName) {
            case "html":
            case "htm":
            case "shtml":
                contentType = "text/html";
                break;
            case "apk":
                contentType = "application/vnd.android.package-archive";
                break;
            case "sis":
                contentType = "application/vnd.symbian.install";
                break;
            case "sisx":
                contentType = "application/vnd.symbian.install";
                break;
            case "exe":
                contentType = "application/x-msdownload";
                break;
            case "msi":
                contentType = "application/x-msdownload";
                break;
            case "css":
                contentType = "text/css";
                break;
            case "xml":
                contentType = "text/xml";
                break;
            case "gif":
                contentType = "image/gif";
                break;
            case "jpeg":
            case "jpg":
                contentType = "image/jpeg";
                break;
            case "js":
                contentType = "application/x-javascript";
                break;
            case "atom":
                contentType = "application/atom+xml";
                break;
            case "rss":
                contentType = "application/rss+xml";
                break;
            case "mml":
                contentType = "text/mathml";
                break;
            case "txt":
                contentType = "text/plain";
                break;
            case "jad":
                contentType = "text/vnd.sun.j2me.app-descriptor";
                break;
            case "wml":
                contentType = "text/vnd.wap.wml";
                break;
            case "htc":
                contentType = "text/x-component";
                break;
            case "png":
                contentType = "image/png";
                break;
            case "tif":
            case "tiff":
                contentType = "image/tiff";
                break;
            case "wbmp":
                contentType = "image/vnd.wap.wbmp";
                break;
            case "ico":
                contentType = "image/x-icon";
                break;
            case "jng":
                contentType = "image/x-jng";
                break;
            case "bmp":
                contentType = "image/x-ms-bmp";
                break;
            case "svg":
                contentType = "image/svg+xml";
                break;
            case "jar":
            case "var":
            case "ear":
                contentType = "application/java-archive";
                break;
            case "doc":
                contentType = "application/msword";
                break;
            case "pdf":
                contentType = "application/pdf";
                break;
            case "rtf":
                contentType = "application/rtf";
                break;
            case "xls":
                contentType = "application/vnd.ms-excel";
                break;
            case "ppt":
                contentType = "application/vnd.ms-powerpoint";
                break;
            case "7z":
                contentType = "application/x-7z-compressed";
                break;
            case "rar":
                contentType = "application/x-rar-compressed";
                break;
            case "swf":
                contentType = "application/x-shockwave-flash";
                break;
            case "rpm":
                contentType = "application/x-redhat-package-manager";
                break;
            case "der":
            case "pem":
            case "crt":
                contentType = "application/x-x509-ca-cert";
                break;
            case "xhtml":
                contentType = "application/xhtml+xml";
                break;
            case "zip":
                contentType = "application/zip";
                break;
            case "mid":
            case "midi":
            case "kar":
                contentType = "audio/midi";
                break;
            case "mp3":
                contentType = "audio/mpeg";
                break;
            case "ogg":
                contentType = "audio/ogg";
                break;
            case "m4a":
                contentType = "audio/x-m4a";
                break;
            case "ra":
                contentType = "audio/x-realaudio";
                break;
            case "3gpp":
            case "3gp":
                contentType = "video/3gpp";
                break;
            case "mp4":
                contentType = "video/mp4";
                break;
            case "mpeg":
            case "mpg":
                contentType = "video/mpeg";
                break;
            case "mov":
                contentType = "video/quicktime";
                break;
            case "flv":
                contentType = "video/x-flv";
                break;
            case "m4v":
                contentType = "video/x-m4v";
                break;
            case "mng":
                contentType = "video/x-mng";
                break;
            case "asx":
            case "asf":
                contentType = "video/x-ms-asf";
                break;
            case "wmv":
                contentType = "video/x-ms-wmv";
                break;
            case "avi":
                contentType = "video/x-msvideo";
                break;
            default:
        }
        return contentType;
    }


    /**
     * 资源懒加载
     *
     * @param request     请求头
     * @param response    响应头
     * @param stFile      文件对象
     * @param inputStream 输入流
     * @throws IOException 异常
     */
    public static void loadVideosAudisoPaf(HttpServletRequest request, HttpServletResponse response, StFileDTO stFile, InputStream inputStream, String sftpDirectory,String fomat) throws IOException {
        log.info("视频或音频分段获取资源");
        //视频
        //通过url获取的InputStream  inputStream.available()并不总是返回文件的实际长度。 所以需要获取其实际长度 inputStream.available()返回int类型 文件超出2147483647大小会有问题
        long fileLen = FileUtils.getFileSize(stFile.getUrl(), sftpDirectory + stFile.getId() + fomat);
        if (log.isInfoEnabled()) {
            log.info("文件大小fileLen:{}", fileLen);
        }
        String range = request.getHeader("Range");
        response.setHeader("Accept-Ranges", "bytes");
        ServletOutputStream out = response.getOutputStream();
        if (range == null) {
            range = "bytes=0-";
        }
        long start = Long.parseLong(range.substring(range.indexOf("=") + 1, range.indexOf("-")));
        long count = fileLen - start;
        long end = 0;
        if (range.endsWith(FileConstants.SPLIT)) {
            end = fileLen - 1;
        } else {
            end = Long.parseLong(range.substring(range.indexOf("-") + 1));
        }
        String contentRange = "bytes " + String.valueOf(start) + "-" + end + "/" + String.valueOf(fileLen);
        response.setStatus(206);
        response.setContentType("application/octet-stream");
        response.setHeader("Content-Range", contentRange);

        byte[] buffer = new byte[1024 * 10];
        int length = 0;
        long skip = inputStream.skip(start);

        while ((length = inputStream.read(buffer)) != -1) {
            out.write(buffer, 0, length);
        }

        inputStream.close();
        out.close();
    }


    /**
     * 加载pdf
     *
     * @param response 响应头
     * @param request  请求头
     * @param filePath pdf文件路径
     * @throws FileNotFoundException 异常
     */
    public static void loadPdf(HttpServletResponse response, HttpServletRequest request, String filePath,InputStream inputStream) throws IOException {
        // 以下为pdf分片的代码
        try (BufferedInputStream bis = new BufferedInputStream(inputStream);
             OutputStream os = response.getOutputStream();
             BufferedOutputStream bos = new BufferedOutputStream(os)) {

            // 下载的字节范围
            int startByte;
            int endByte;
            int totalByte;
            // 根据HTTP请求头的Range字段判断是否为断点续传
            if (request == null || request.getHeader(RANGE) == null) {
                // 如果是首次请求，返回全部字节范围 bytes 0-7285040/7285041
                totalByte = inputStream.available();
                startByte = 0;
                endByte = totalByte - 1;
                // 跳过输入流中指定的起始位置
                response.setStatus(HttpServletResponse.SC_OK);
            } else {
                // 断点续传逻辑
                String[] range = request.getHeader(RANGE).replaceAll("[^0-9\\-]", "").split("-");
                // 文件总大小
                totalByte = inputStream.available();
                // 下载起始位置
                startByte = Integer.parseInt(range[0]);
                // 下载结束位置
                endByte = range.length > 1 ? Integer.parseInt(range[1]) : totalByte - 1;

                // 跳过输入流中指定的起始位置
                long skip = bis.skip(startByte);

                // 表示服务器成功处理了部分 GET 请求，返回了客户端请求的部分数据。
                response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
            }
            // 表明服务器支持分片加载
            response.setHeader("Accept-Ranges", "bytes");
            // Content-Range: bytes 0-65535/408244，表明此次返回的文件范围
            response.setHeader("Content-Range", "bytes " + startByte + "-" + endByte + "/" + totalByte);
            // 告知浏览器这是一个字节流，浏览器处理字节流的默认方式就是下载
            response.setContentType("application/octet-stream");
            // 表明该文件的所有字节大小
            response.setContentLength(endByte - startByte + 1);
            // 需要设置此属性，否则浏览器默认不会读取到响应头中的Accept-Ranges属性，
            // 因此会认为服务器端不支持分片，所以会直接全文下载
            response.setHeader("Access-Control-Expose-Headers", "Accept-Ranges,Content-Range");
            // 第一次请求直接刷新输出流，返回响应
            int bytesRead;
            int length = endByte - startByte + 1;
            byte[] buffer = new byte[1024 * 10];
            while ((bytesRead = bis.read(buffer, 0, Math.min(buffer.length, length))) != -1 && length > 0) {
                bos.write(buffer, 0, bytesRead);
                length -= bytesRead;
            }
            response.flushBuffer();
            bos.close();
        } catch (IOException e) {
            log.error("异常描述", e);
            throw new RuntimeException(e);
        }finally {
            inputStream.close();
        }
    }


    /**
     * 获取水印内容
     *
     * @param checkList    水印配置内容
     * @param contentValue 自定义内容
     * @param userName     用户
     * @param userPhone    用户电话
     * @param instName     机构名
     * @param instPhone    机构电话
     * @return String
     */
    public static String getMarkValue(List<String> checkList, String contentValue, String userName, String userPhone, String instName, String instPhone) {

        StringBuilder value = new StringBuilder();
        if (checkList.contains(FileConstants.TEL)) {
            value.append(userPhone).append(" ");
        }
        if (checkList.contains(FileConstants.COMPANY)) {
            value.append(instName).append(" ");
        }
        if (checkList.contains(FileConstants.SYS_DATE)) {
            value.append(LocalDate.now()).append(" ");
        }
        if (checkList.contains(FileConstants.CUSTOM)) {
            value.append(instPhone).append(" ");
        }
        if (checkList.contains(FileConstants.USERNAME)) {
            value.append(userName).append(" ");
        }
        if (StrUtil.isNotEmpty(contentValue)) {
            value.append(contentValue);
        }
        return value.toString();
    }


    public static void getResponseByInputStream(HttpServletResponse response, Long fileId, Integer type, InputStream inputStream) {
        try (ServletOutputStream servletOutputStream = response.getOutputStream()) {
            if (inputStream.available() == 0) {
                log.info("存储设备中读取不到该文件信息");
            } else {
                // 将输入流转换为字节数组
                byte[] fileBytes;
                fileBytes = FileUtils.read(inputStream);
                servletOutputStream.write(fileBytes);
                FileUtils.printFile(response, servletOutputStream, fileId + ".png", type);
            }
        } catch (IOException e) {
            log.error("异常描述", e);
            throw new RuntimeException(e);
        } finally {
            try {
                if (ObjectUtil.isNotEmpty(inputStream)) {
                    inputStream.close();
                }
            } catch (IOException e) {
                log.error("异常描述", e);
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * 响应PDF图片文件流
     * @param response
     * @param fileName
     * @param type
     * @param inputStream
     */
    public static void getResponseByInputStream(HttpServletResponse response, String fileName, Integer type, InputStream inputStream) {
        try (ServletOutputStream servletOutputStream = response.getOutputStream()) {
            if (inputStream.available() == 0) {
                log.info("存储设备中读取不到该文件信息");
            } else {
                // 将输入流转换为字节数组
                byte[] fileBytes;
                fileBytes = FileUtils.read(inputStream);
                servletOutputStream.write(fileBytes);
                FileUtils.printFile(response, servletOutputStream, fileName, type);
            }
        } catch (IOException e) {
            log.error("异常描述", e);
            throw new RuntimeException(e);
        } finally {
            try {
                if (ObjectUtil.isNotEmpty(inputStream)) {
                    inputStream.close();
                }
            } catch (IOException e) {
                log.error("异常描述", e);
                throw new RuntimeException(e);
            }
        }
    }
}
