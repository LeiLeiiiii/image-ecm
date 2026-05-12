package com.sunyard.module.storage.util;

import com.alibaba.ttl.TransmittableThreadLocal;
import com.sunyard.module.storage.dto.StFileDTO;
import com.sunyard.module.storage.vo.DownFileVO;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.springframework.util.ObjectUtils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

/**
 * 文件类型 Utils
 *
 * @author zyl
 */
@Slf4j
public class FileTypeUtils {

    private static final ThreadLocal<Tika> TIKA = TransmittableThreadLocal.withInitial(Tika::new);

    /**
     * 获得文件的 mineType，对于doc，jar等文件会有误差
     *
     * @param data 文件内容
     * @return Result mineType 无法识别时会返回“application/octet-stream”
     */
    @SneakyThrows
    public static String getMineType(byte[] data) {
        return TIKA.get().detect(data);
    }

    /**
     * 已知文件名，获取文件类型，在某些情况下比通过字节数组准确，例如使用jar文件时，通过名字更为准确
     *
     * @param name 文件名
     * @return Result mineType 无法识别时会返回“application/octet-stream”
     */
    public static String getMineType(String name) {
        return TIKA.get().detect(name);
    }

    /**
     * 在拥有文件和数据的情况下，最好使用此方法，最为准确
     *
     * @param data 文件内容
     * @param name 文件名
     * @return Result mineType 无法识别时会返回“application/octet-stream”
     */
    public static String getMineType(byte[] data, String name) {
        return TIKA.get().detect(data, name);
    }

    /**
     * byte[]转换File
     * @param fileBytes byte数组
     * @return Result
     */
    public static File convert(byte[] fileBytes) {
        OutputStream outputStream = null;
        try {
            File tempFile = File.createTempFile("temp", null);
            outputStream = new BufferedOutputStream(new FileOutputStream(tempFile));
            outputStream.write(fileBytes);
            //确保缓冲区数据被立即刷新到文件
            outputStream.flush();
            return tempFile;
        } catch (FileNotFoundException e) {
            log.error("异常描述",e);
            throw new RuntimeException(e);
        } catch (IOException e) {
            log.error("异常描述",e);
            throw new RuntimeException(e);
        }finally {
            try {
                outputStream.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * 得到文件原始名称
     * @param downFileVO downFileVO
     * @param stFile stFile
     * @return Result
     */
    public static String getOriginalFilename(DownFileVO downFileVO, StFileDTO stFile) {
        String originalFilename = null;
        Map<String, String> fileIdNameMap = downFileVO.getFileIdNameMap();
        if (ObjectUtils.isEmpty(fileIdNameMap)) {
            originalFilename = stFile.getOriginalFilename();
        } else {
            String fileName = fileIdNameMap.get(String.valueOf(stFile.getId()));
            originalFilename = ObjectUtils.isEmpty(fileName) ? stFile.getOriginalFilename() : fileName;
        }
        return originalFilename;
    }

    /**
     * 本地上传
     * @param mergedPdfStream 输入流
     * @param channelSftp sftp连接
     * @param storageAddress 存储地址
     * @param basePath 根路径
     * @param fileName 文件名
     * @return Result
     */
//    public static String localUpload(InputStream mergedPdfStream, ChannelSftp channelSftp, String storageAddress, String basePath, String fileName) {
//        AssertUtils.isNull(ResultCode.PARAM_ERROR, "无对应存储设备连接");
//        String url = basePath + File.separator + DateUtil.format(new Date(), "yyyy-MM-dd") + File.separator;
//        String[] urlParts = url.split(File.separatorChar == '\\' ? "\\\\" : "/");
//        FtpUtils.mkdirDir(channelSftp, urlParts, "", urlParts.length, 0);
//        FtpUtils.upload(channelSftp, url, mergedPdfStream, fileName);
//        return StrUtil.format("{}/{}{}", storageAddress, url, fileName);
//    }

    /**
     * 获取文件类型
     * @param fileExtension fileExtension
     * @return Result
     */
    public static String getContentTypeByExtension(String fileExtension) {
        if (fileExtension == null) {
            return null;
        }
        String contentType;
        switch (fileExtension.toLowerCase()) {
            case "pdf":
                contentType = "application/pdf";
                break;
            case "txt":
                contentType = "text/plain";
                break;
            case "jpg":
            case "jpeg":
                contentType = "image/jpeg";
                break;
            case "png":
                contentType = "image/png";
                break;
            case "doc":
            case "docx":
                contentType = "application/vnd.ms-word";
                break;
            case "xls":
            case "xlsx":
                contentType = "application/vnd.ms-excel";
                break;
            // 添加更多文件扩展名和对应的 MIME 类型
            default:
                contentType = "application/octet-stream";
                break;
        }
        return contentType;
    }

    /**
     * 清除当前线程中保存的值
     */
    public static void remove() {
        TIKA.remove();
    }
}
