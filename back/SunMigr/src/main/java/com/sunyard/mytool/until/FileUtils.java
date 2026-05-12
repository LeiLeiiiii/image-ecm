package com.sunyard.mytool.until;

import cn.hutool.core.io.FileTypeUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.io.file.FileNameUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * @author liugang
 * @Type com.sunyard.sunam.utils
 * @Desc
 * @date 14:23 2021/11/16
 */
@Slf4j
public class FileUtils {

    /**
     * url转file
     *
     * @param url      文件url
     * @param tempPath 临时地址
     * @return file
     */
    public static File getFileByUrl(String url, String tempPath) {
        url = url.replaceAll("\\\\", "//");
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        BufferedOutputStream stream = null;
        InputStream inputStream = null;
        File file = null;
        try {
            URL fileUrl = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) fileUrl.openConnection();
            conn.setRequestProperty("User-Agent",
                    "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");
            inputStream = conn.getInputStream();
            byte[] buffer = new byte[1024];
            int len = 0;
            while ((len = inputStream.read(buffer)) != -1) {
                outStream.write(buffer, 0, len);
            }

            file = new File(tempPath);
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }

            FileOutputStream fileOutputStream = new FileOutputStream(file);
            stream = new BufferedOutputStream(fileOutputStream);
            stream.write(outStream.toByteArray());
        } catch (Exception e) {
            log.error("系统异常", e);
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
                if (stream != null) {
                    stream.close();
                }
                outStream.close();
            } catch (Exception e) {
                log.error("系统异常", e);
            }
        }
        return file;
    }

    /**
     * 文件磁盘路径转file
     *
     * @param url      文件url
     * @param tempPath 临时路径
     * @return file对象
     */
    public static File getFileByPath(String url, String tempPath) {
        url = url.replaceAll("\\\\", "//");
        // 对本地文件命名，path是http的完整路径，主要得到资源的名字
        String newUrl = url;
        newUrl = newUrl.split("[?]")[0];
        String[] bb = newUrl.split("/");
        // 得到最后一个分隔符后的名字
        String fileName = bb[bb.length - 1];
        // 保存到本地的路径
        String filePath = tempPath + fileName;
        File file = null;

        URL urlfile;
        InputStream inputStream = null;
        OutputStream outputStream = null;
        try {
            // 判断文件的父级目录是否存在，不存在则创建
            file = new File(filePath);
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            // 创建文件
            file.createNewFile();
            // 下载
            urlfile = new URL(newUrl);
            inputStream = urlfile.openStream();
            outputStream = new BufferedOutputStream(new FileOutputStream(file));

            int bytesRead = 0;
            byte[] buffer = new byte[8192];
            while ((bytesRead = inputStream.read(buffer, 0, 8192)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            //确保缓冲区数据被立即刷新到文件
            outputStream.flush();
        } catch (Exception e) {
            log.error("系统异常", e);
        } finally {
            try {
                if (null != outputStream) {
                    outputStream.close();
                }
                if (null != inputStream) {
                    inputStream.close();
                }

            } catch (Exception e) {
                log.error("系统异常", e);
            }
        }
        return file;
    }

    /**
     * 创建文件
     *
     * @param name 文件名
     */
    public static void creatFile(String name) {
        try {
            File file = new File(name);
            if (!file.exists()) {
                sureParentFile(file);
                file.createNewFile();
            }
        } catch (Exception e) {
            log.error("系统异常", e);
        }
    }

    /**
     * 确保上级文件夹存在
     *
     * @param file 文件夹路径
     */
    private static void sureParentFile(File file) {
        File par = file.getParentFile();
        if (!par.exists()) {
            sureParentFile(par);
            par.mkdir();
        }
    }

    /**
     * 文件转base64
     *
     * @param file 文件obj
     * @return Result
     * @throws IOException 异常
     */
    public static byte[] getContent(File file) {

        long fileSize = file.length();
        if (fileSize > Integer.MAX_VALUE) {
            log.warn("file too big...");
            return null;
        }
        byte[] buffer = new byte[(int) fileSize];
        FileInputStream fi = null;
        try {
            // 确保所有数据均被读取
            fi = new FileInputStream(file);
            int offset = 0;
            int numRead = 0;
            while (offset < buffer.length
                    && (numRead = fi.read(buffer, offset, buffer.length - offset)) >= 0) {
                offset += numRead;
            }
            if (offset != buffer.length) {
                log.warn("Could not completely read file " + file.getName());
                return null;
            }
            return buffer;
        } catch (IOException e) {
            log.error("Could not completely read file{}", file.getName(), e);
            return null;
        } finally {
            close(fi);
        }

    }

    /**
     * 删除单个文件
     *
     * @param fileName 要删除的文件的文件名
     * @return Result 单个文件删除成功返回true，否则返回false
     */
    public static boolean deleteFile(String fileName) {
        File file = new File(fileName);
        // 如果文件路径所对应的文件存在，并且是一个文件，则直接删除
        if (file.exists() && file.isFile()) {
            return file.delete();
        } else {
            return false;
        }
    }

    /**
     * 删除目录及目录下的文件
     *
     * @param dir 要删除的目录的文件路径
     * @return Result 目录删除成功返回true，否则返回false
     */
    public static boolean deleteDirectory(String dir) {
        // 如果dir不以文件分隔符结尾，自动添加文件分隔符
        if (!dir.endsWith(File.separator)) {
            dir = dir + File.separator;
        }
        File dirFile = new File(dir);
        // 如果dir对应的文件不存在，或者不是一个目录，则退出
        if ((!dirFile.exists()) || (!dirFile.isDirectory())) {
            return false;
        }
        boolean flag = true;
        // 删除文件夹中的所有文件包括子目录
        File[] files = dirFile.listFiles();
        for (int i = 0; i < Objects.requireNonNull(files).length; i++) {
            // 删除子文件
            if (files[i].isFile()) {
                flag = FileUtils.deleteFile(files[i].getAbsolutePath());
                if (!flag) {
                    break;
                }
            } else if (files[i].isDirectory()) {
                // 删除子目录
                flag = FileUtils.deleteDirectory(files[i].getAbsolutePath());
                if (!flag) {
                    break;
                }
            }
        }
        if (!flag) {
            return false;
        }
        // 删除当前目录
        return dirFile.delete();
    }


    public static void close(Closeable... io) {
        for (Closeable closeable : io) {
            if (closeable != null) {
                try {
                    closeable.close();
                } catch (IOException e) {
                    log.error("系统异常",e);
                    throw new RuntimeException(e);
                }
            }
        }
    }

    /**
     * 判断路径最后一段是否为文件
     */
    public static boolean isFile(String fileName) {
        // 检查是否包含点号且点号不在开头，且不是以/结尾
        int lastDotIndex = fileName.lastIndexOf('.');
        return lastDotIndex > 0 && // 点号不在开头
                lastDotIndex < fileName.length() - 1 && // 点号不在末尾
                !fileName.endsWith("/"); // 不以/结尾
    }
}
