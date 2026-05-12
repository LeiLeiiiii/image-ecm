package com.sunyard.module.storage.util;


import cn.hutool.core.util.ObjectUtil;
import com.jcraft.jsch.ChannelSftp;
import com.sunyard.framework.common.util.FileUtils;
import com.sunyard.framework.common.util.FtpUtils;
import org.springframework.util.ObjectUtils;

import javax.activation.MimetypesFileTypeMap;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * 判断文件类型工具类
 *
 * @author panjiazhu
 * @date 2022/7/13
 */
public class FileCheckUtils {

    private static MimetypesFileTypeMap mtftp;

    /**
     * 判断是否图片
     *
     * @param file file
     * @return Result
     */
    public static boolean isImage(File file) {
        mtftp = new MimetypesFileTypeMap();
        mtftp.addMimeTypes("image png tif jpg jpeg bmp");
        String mimetype = mtftp.getContentType(file);
        String type = mimetype.split("/")[0];
        return "image".equals(type);
    }

    /**
     * 判断是否是图片
     *
     * @param urlStr url集
     * @return boolean
     */
    public static boolean isImage(List<String> urlStr) {
        for (String s : urlStr) {
            try(InputStream inputStream1 = FileUtils.getInputStreamFromUrl(s)) {
                BufferedImage image = ImageIO.read(inputStream1);
                if (ObjectUtils.isEmpty(image)) {
                    return false;
                }
            } catch (IOException e) {
                return true;
            }
        }
        return true;
    }

    /**
     * 判断是否是图片
     *
     * @param inputStream 输入流
     * @return boolean
     */
    public static boolean isImage(InputStream inputStream) {
        try {
            BufferedImage image = ImageIO.read(inputStream);
            if (ObjectUtil.isNotEmpty(image)) {
                return true;
            }
        } catch (Exception e) {
            return false;
        }

        return false;
    }

    /**
     * 判断是否是图片
     *
     * @param urlStr   文件url
     * @param host     地址
     * @param port     端口
     * @param username 账号
     * @param password 密码
     * @return boolean
     */
    public static boolean isImage(String urlStr, String host, Integer port, String username, String password) {
        ChannelSftp connect = null;
        InputStream inputStream = null;
        try {
            connect = FtpUtils.getConnect(host, port, username, password);
            inputStream = connect.get(urlStr);
            BufferedImage image = ImageIO.read(inputStream);
            if (ObjectUtils.isEmpty(image)) {
                return false;
            }
        } catch (Exception e) {
            return true;
        } finally {
            connect.disconnect();
            if(inputStream!=null){
                try {
                    inputStream.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return true;
    }

}
