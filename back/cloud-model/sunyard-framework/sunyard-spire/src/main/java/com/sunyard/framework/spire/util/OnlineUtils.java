package com.sunyard.framework.spire.util;

import cn.hutool.core.util.ObjectUtil;
import com.sunyard.framework.common.util.FileUtils;
import com.sunyard.framework.spire.constant.OnlineConstants;
import com.sunyard.framework.spire.dto.FileCloudDTO;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 *
 */
/**
 * @author zhouleibin
 * 在线预览工具类
 */
@Slf4j
public class OnlineUtils {

    /***
     * 在线预览
     * 
     * @param response
     * @param fileCloudDTO 文件主体
     * @param water 是否有水印
     */
    public static void online(HttpServletResponse response, FileCloudDTO fileCloudDTO, Integer water,
        String waterContent, String fileCharsetName) {
        InputStream inputStreamFromUrl = null;
        try {
            ServletOutputStream outputStream = response.getOutputStream();
            String fileName = fileCloudDTO.getOriginalFilename();

            if (OnlineConstants.IMGLIST.contains(fileCloudDTO.getExt())) {
                // 图片不需要转换
                if (OnlineConstants.WATER_FLAG_HAVE.equals(water)) {
                    inputStreamFromUrl = FileUtils.getInputStreamFromUrl(fileCloudDTO.getUrl());
                    ImageWaterUtils.markImageByText(waterContent, inputStreamFromUrl, outputStream, null);
                } else {
                    inputStreamFromUrl = FileUtils.getInputStreamFromUrl(fileCloudDTO.getUrl());
                    byte[] bytes = new byte[1024];
                    while (inputStreamFromUrl.read(bytes) != -1) {
                        outputStream.write(bytes);
                    }
                }
            } else if (OnlineConstants.PDFLIST.contains(fileCloudDTO.getExt())) {
                // pdf不需要转换
                // 加水印
                if (OnlineConstants.WATER_FLAG_HAVE.equals(water)) {
                    inputStreamFromUrl = FileUtils.getInputStreamFromUrl(fileCloudDTO.getUrl());
                    WatermarkUtils.pdfwaterMark(inputStreamFromUrl, waterContent, 22, outputStream);
                } else {
                    inputStreamFromUrl = FileUtils.getInputStreamFromUrl(fileCloudDTO.getUrl());
                    byte[] bytes = new byte[1024];
                    while (inputStreamFromUrl.read(bytes) != -1) {
                        outputStream.write(bytes);
                    }
                }
            } else {
                // 加水印
                if (OnlineConstants.WATER_FLAG_HAVE.equals(water)) {
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    inputStreamFromUrl = FileUtils.getInputStreamFromUrl(fileCloudDTO.getUrl());
                    // 转pdf
                    toPdf(fileCloudDTO, inputStreamFromUrl, byteArrayOutputStream, fileCharsetName,null);
                    ByteArrayInputStream byteArrayInputStream =
                        new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
                    WatermarkUtils.pdfwaterMark(byteArrayInputStream, waterContent, 22, outputStream);
                } else {
                    inputStreamFromUrl = FileUtils.getInputStreamFromUrl(fileCloudDTO.getUrl());
                    // 转pdf
                    toPdf(fileCloudDTO, inputStreamFromUrl, outputStream, fileCharsetName,null);
                }
            }

            FileUtils.fileWriter(response, outputStream, fileName);
        } catch (Exception e) {
            log.error("系统异常", e);
            throw new RuntimeException(e);
        }finally {
            if (ObjectUtil.isNotEmpty(inputStreamFromUrl)) {
                try {
                    inputStreamFromUrl.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }

    }

    /**
     * 转pdf
     * @param fileCloudDTO 文件上传DTO
     * @param inputStream 输入流
     * @param outputStream 输出流
     * @param fileCharsetName 文件名称
     */
    static void toPdf(FileCloudDTO fileCloudDTO, InputStream inputStream, OutputStream outputStream,
        String fileCharsetName,String password) {
        if (OnlineConstants.DOCLIST.contains(fileCloudDTO.getExt())) {
            // 转pdf
            ConvertPdfUtils.toWordPdf(inputStream, outputStream,password);
        } else if (OnlineConstants.XLSLIST.contains(fileCloudDTO.getExt())) {
            ConvertPdfUtils.toExcelPdf(inputStream, outputStream,password);
        } else if (OnlineConstants.PPTLIST.contains(fileCloudDTO.getExt())) {
            ConvertPdfUtils.toPptPdf(inputStream, outputStream,password);
        } else if (OnlineConstants.TXTLIST.contains(fileCloudDTO.getExt())) {
            ConvertPdfUtils.toTxtPdf(inputStream, outputStream, fileCharsetName);
        }
    }

}
