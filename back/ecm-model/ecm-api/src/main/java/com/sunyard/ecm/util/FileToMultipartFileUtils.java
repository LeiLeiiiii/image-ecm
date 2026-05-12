package com.sunyard.ecm.util;
/**
 * @program: sunicms
 * @description:
 * @author: 作者名称
 * @date: 2024-01-25 21:11
 **/

import org.apache.commons.fileupload.disk.DiskFileItem;
import org.apache.commons.io.FileUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

/**
 * @author: lw
 * @Date: 2024/1/28
 * @Description: 文件转换工具类
 */
public class FileToMultipartFileUtils {
    /**
     * File转为MultipartFile
     * @param file
     * @return
     * @throws IOException
     */
    public static MultipartFile convert(File file) throws IOException {
        byte[] fileContent = FileUtils.readFileToByteArray(file);
        DiskFileItem fileItem = new DiskFileItem("file", "text/plain", true, file.getName(),
                fileContent.length, file.getParentFile());

        try (OutputStream outputStream = fileItem.getOutputStream()) {
            outputStream.write(fileContent);
        }

        return new CommonsMultipartFile(fileItem);
    }


}
