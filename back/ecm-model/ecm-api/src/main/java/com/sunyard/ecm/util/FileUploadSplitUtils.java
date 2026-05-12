package com.sunyard.ecm.util;

import com.sunyard.ecm.constant.ApiConstants;
import com.sunyard.ecm.dto.EcmBusExtendDTO;
import com.sunyard.ecm.dto.EcmDownloadByFileIdDTO;
import com.sunyard.ecm.dto.FileAndSortDTO;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author: lw
 * @Date: 2024/1/28
 * @Description: 文件上传工具类
 */
@Slf4j
public class FileUploadSplitUtils {

    /**
     * 从URL的查询字符串中提取指定参数的值
     *
     * @param url URL字符串
     * @param paramName 要提取的参数名
     * @return 参数的值，如果不存在则返回null
     */
    public static String getParameterValue(String url, String paramName) {
        if (url == null || paramName == null) {
            return null;
        }

        // 查找查询字符串的开始位置
        int queryIndex = url.indexOf('?');
        if (queryIndex == -1) {
            // 没有查询字符串
            return null;
        }

        // 提取查询字符串
        String queryString = url.substring(queryIndex + 1);

        // 使用"&"分割查询字符串，得到参数数组
        String[] params = queryString.split("&");

        for (String param : params) {
            // 使用"="分割每个参数，得到参数名和值
            String[] pair = param.split("=", 2);
            if (pair.length == 2 && pair[0].equalsIgnoreCase(paramName)) {
                // 找到了匹配的参数，进行URL解码并返回
                try {
                    return URLDecoder.decode(pair[1], "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        // 没有找到匹配的参数
        return null;
    }
    public static String getMd5(MultipartFile file) {
        try (InputStream inputStream = file.getInputStream()) {
            return Md5Utils.calculateMD5(inputStream);
        } catch (IOException | NoSuchAlgorithmException e) {
            log.error("获取md5异常",e);
            return null;
        }
    }


    /**
     * 获取文件md5并且压缩
     * @param dto
     * @param ecmBusExtendDTOS
     * @return
     */
    public static Map<String, FileAndSortDTO> getMd5(List<FileAndSortDTO> dto, EcmBusExtendDTO ecmBusExtendDTOS) {
        ArrayList<FileAndSortDTO> objects = new ArrayList<>();

        for(FileAndSortDTO f:dto) {
            MultipartFile file = null;
            try {
                if (f.getMultipartFile() != null) {
                    file = f.getMultipartFile();
                }
                if (file != null) {
                    String sourceFileMd5 = getMd5(file);
                    String fileMd5 = sourceFileMd5;
                    if (ApiConstants.COMPRESS.toString().equals(ecmBusExtendDTOS.getIsCompress())) {
                        compress(file, ecmBusExtendDTOS);
                        fileMd5 = getMd5(file);
                    }
                    f.setMultipartFile(file);
                    f.setFileMd5(fileMd5);
                    f.setSourceFileMd5(sourceFileMd5);
                    objects.add(f);
                }
            } catch (Exception e) {
                log.error("文件处理异常",e);
            }
        }
        Map<String, List<FileAndSortDTO>> collect = objects.stream().collect(Collectors.groupingBy(FileAndSortDTO::getSourceFileMd5));
        Map<String, FileAndSortDTO> ret = new HashMap<>();
        for(String md5:collect.keySet()){
            List<FileAndSortDTO> fileAndSortDTOS = collect.get(md5);
            ret.put(md5,fileAndSortDTOS.get(0));
        }
        return ret;
//        return dto.parallelStream()
//                .filter(f -> {
//                    MultipartFile file = null;
//                    try {
//                        if(f.getMultipartFile()!=null){
//                            file = f.getMultipartFile();
//                        }
//                        if (file != null) {
//                            String sourceFileMd5 = getMd5(file);
//                            String fileMd5 = sourceFileMd5;
//                            if (ecmBusExtendDTOS.getIsCompress().equals(ApiConstants.COMPRESS.toString())) {
//                                compress(file, ecmBusExtendDTOS);
//                                fileMd5 = getMd5(file);
//                            }
//                            f.setMultipartFile(file);
//                            f.setFileMd5(fileMd5);
//                            f.setSourceFileMd5(sourceFileMd5);
//                            return true;
//                        }
//                    } catch (Exception e) {
//                        
//                    }
//                    return false;
//                })
//                .collect(Collectors.toMap(
//                        f -> f.getSourceFileMd5(),
//                        f -> f,
//                        (existing, replacement) -> existing,
//                        HashMap::new
//                ));
    }



    /**
     * 文件按照压缩比和压缩质量进行压缩
     */
    public static MultipartFile compress(MultipartFile file, EcmBusExtendDTO ecmBusExtendDTOS) throws IOException {
        Integer compressSize = Integer.valueOf(ecmBusExtendDTOS.getCompressSize());
        Double compressValue = Double.valueOf(ecmBusExtendDTOS.getCompressValue());

        BufferedImage startOriginalImage;
        try (InputStream inputStream = file.getInputStream()) {
            startOriginalImage = ImageIO.read(inputStream);
        }

        // 特殊文件格式originalImage为null,所以不走压缩
        if (startOriginalImage != null) {
            int width = startOriginalImage.getWidth();
            int height = startOriginalImage.getHeight();

            // 计算宽高比例
            double ratio = (double) width / height;
            DecimalFormat decimalFormat = new DecimalFormat("#.00");
            String formattedResult = decimalFormat.format(ratio);
            Double newRatio = Double.valueOf(formattedResult);

            // 如果宽度大于压缩阈值，则按比例缩小宽度
            if (width > compressSize && height > compressSize) {
                if (height >= width) {
                    width = compressSize;
                    height = (int) (height * newRatio);
                } else {
                    height = compressSize;
                    width = (int) (height * newRatio);
                }
            }

            try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                 InputStream inputStream = file.getInputStream()) {

                Thumbnails.of(inputStream)
                        .size(width, height)
                        .outputQuality(compressValue)
                        .toOutputStream(outputStream);

                // 从ByteArrayOutputStream获取字节数组
                byte[] thumbnailBytes = outputStream.toByteArray();

                return new ByteArrayMultipartFile(
                        "file", // 表单中的文件参数名
                        file.getOriginalFilename(), // 原始文件名
                        "image/jpeg", // MIME类型
                        thumbnailBytes); // 字节数组内容
            }
        }
        return file;
    }



    /**
     * 输入流转文件
     *
     * @param ins
     * @param file
     */
    public static void inputStreamToFile(InputStream ins, File file) {
        BufferedOutputStream bos = null;
        BufferedInputStream bis = new BufferedInputStream(ins);
        try {
            //设置为true表示追加
            bos = new BufferedOutputStream(new FileOutputStream(file,true));
            int bytesRead = 0;
            byte[] buffer = new byte[8192];
            while ((bytesRead = bis.read(buffer, 0, 8192)) != -1) {
                bos.write(buffer, 0, bytesRead);
            }
        } catch (Exception e) {
        } finally {
            if (ins != null) {
                try {
                    ins.close();
                } catch (IOException e) {
                }
                ins = null;
            }
            if (bos != null) {
                try {
                    bos.close();
                } catch (IOException e) {
                }
                bos = null;
            }
            if (bis != null) {
                try {
                    bis.close();
                } catch (IOException e) {
                }
                bis = null;
            }
        }
    }


    /**
     * 文件下载重名问题
     * @param path
     * @param file
     * @return
     */
    public static File checkFileExists(String path, EcmDownloadByFileIdDTO file) {
        File targetFile = new File(path +"/"+ file.getNewFileName());
        if(targetFile.exists()){
            file.setNewFileName("(1)"+file.getNewFileName());
            checkFileExists(path,file);
        }
        return targetFile;
    }

}
