package com.sunyard.framework.common.util;

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
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.sunyard.framework.common.exception.SunyardException;
import com.sunyard.framework.common.result.ResultCode;

import cn.hutool.core.io.FileTypeUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.io.file.FileNameUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

/**
 * @author liugang
 * @Type com.sunyard.sunam.utils
 * @Desc
 * @date 14:23 2021/11/16
 */
@Slf4j
public class FileUtils {

    private static final String RANGE = "range";
    public static final String FILE_SEPARATOR = System.getProperty("file.separator");
    public static final List<String> CANSPLITFILETYPE = Arrays.asList("pdf", "ofd", "tiff", "tif",
            "heif", "txt", "ppt", "pptx", "xls", "xlsx", "docx", "doc", "wps");
    public static final List<String> VIDEOS = Arrays.asList("wmv", "asf", "rm", "rmvb", "avi",
            "mov", "mpg", "flv", "mp4");
    public static final List<String> AUDIOS = Arrays.asList("amr", "ogg", "m4a", "mp3", "wav");
    public static final List<String> DOCS = Arrays.asList("txt", "doc", "wps", "docx", "xls", "ppt",
            "pptx", "xlsx", "ini", "pdf");

    /**
     * 文件下载
     *
     * @param response 请求回执
     * @param filePath 文件地址
     */
    public static void fileWriter(HttpServletResponse response, String filePath) {
        File file = new File(filePath);
        if (file.exists()) {
            FileInputStream inputStream;
            try {
                inputStream = new FileInputStream(file);
            } catch (FileNotFoundException e) {
                log.error("文件异常：", e);
                throw new RuntimeException(e);
            }
            response.setContentType("application/octet-stream");
            response.setHeader("Content-disposition", "attachment;filename=" + file.getName());
            OutputStream outputStream = null;
            try {
                outputStream = response.getOutputStream();
                byte[] bytes = new byte[1024];
                while (inputStream.read(bytes) != -1) {
                    outputStream.write(bytes);
                }
                outputStream.flush();
            } catch (IOException e) {
                log.error("意外错误：", e);
                throw new RuntimeException(e);
            } finally {
                try {
                    inputStream.close();
                    if (outputStream != null) {
                        outputStream.close();
                    }
                } catch (IOException e) {
                    log.error("意外错误：", e);
                    throw new RuntimeException(e);
                }
            }
        }
    }

    /**
     * 文件下载
     *
     * @param response     响应头
     * @param outputStream 输出流
     * @param fileName     文件名
     */
    public static void fileWriter(HttpServletResponse response, OutputStream outputStream,
                                  String fileName) {
        if (outputStream != null) {
            response.setContentType("application/octet-stream");
            response.setHeader("Content-disposition", "attachment;filename=" + fileName);
            try {
                outputStream.flush();
            } catch (IOException e) {
                log.error("意外错误：", e);
                throw new RuntimeException(e);
            } finally {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    log.error("意外错误：", e);
                    throw new RuntimeException(e);
                }
            }
        }
    }

    /**
     * 返回附件
     *
     * @param response 响应
     * @param filename 文件名
     * @param content  附件内容
     * @throws IOException 异常
     */
    public static void writeAttachment(HttpServletResponse response, String filename,
                                       byte[] content)
            throws IOException {
        response.setHeader("Content-Disposition",
                "attachment;filename=" + URLEncoder.encode(filename, "UTF-8"));
        response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
        IoUtil.write(response.getOutputStream(), false, content);
    }

    /**
     * 下载
     *
     * @param response 响应
     * @param filename 文件名
     * @param input  附件内容
     * @throws IOException 异常
     */
    public static void writeAttachment(HttpServletResponse response, String filename,
                                       InputStream input) {
        if (response == null || filename == null || input == null) {
            throw new IllegalArgumentException("response,filename,input不能为null");
        }
        try (ServletOutputStream output = response.getOutputStream()) {
            String encodedFilename = URLEncoder.encode(filename, StandardCharsets.UTF_8.toString())
                    .replace("+", "%20");
            response.setHeader("Content-Disposition", "attachment; filename=" + encodedFilename);

            response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());

            // 使用更大的缓冲区提高性能（8KB）
            byte[] buffer = new byte[8192];
            int bytesRead;

            while ((bytesRead = input.read(buffer)) != -1) {
                output.write(buffer, 0, bytesRead);
            }
            output.flush();
        } catch (IOException e) {
            log.error("系统异常", e);
            throw new RuntimeException("下载文件失败 " + filename, e);
        } finally {
            try {
                input.close();
            } catch (IOException e) {
                log.error("输入流关闭异常:{}", e);
            }
        }
    }

    /**
     * 文件打印
     *
     * @param response     响应头
     * @param outputStream 输出流
     * @param fileName     文件名
     * @param type         是否进行缓存 null为不缓存
     */
    public static void printFile(HttpServletResponse response, OutputStream outputStream,
                                 String fileName, Integer type) {
        if (outputStream != null) {
            response.setHeader("Content-disposition", "inline;filename=" + fileName);
            if (type != null) {
                response.setContentType("application/octet-stream");
                response.setHeader("Cache-Control", "public, max-age=604800");
                response.setHeader("Pragma", "cache");
                response.setHeader("Accept-Ranges", "bytes");
            }
            try {
                outputStream.flush();
            } catch (IOException e) {
                log.error("意外错误：", e);
            } finally {
                try {
                    if (outputStream != null) {
                        outputStream.close();
                    }
                } catch (IOException e) {
                    log.error("意外错误：", e);
                }
            }
        }
    }

    /**
     * 输出文件流到前端
     */
    public static void printFile(HttpServletResponse response, OutputStream outputStream,
                                 String fileName) {
        printFile(response, outputStream, fileName, null);
    }

    /**
     * 判断ContentType
     *
     * @param fileExtension contentType
     * @return String
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
            default:
                contentType = "application/octet-stream";
                break;
        }

        return contentType;
    }

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
     * @param file 文件对象
     */
    public static void creatFile(File file) {
        creatFile(file.getAbsolutePath());
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
            IoUtils.close(fi);
        }

    }

    /**
     * 拷贝文件
     *
     * @param srcFileName  待复制文件名
     * @param destFileName 目标文件名
     * @param overlay      如果目标文件存在，是否覆盖
     * @return Result
     * @throws IOException 异常
     */
    public static boolean copyFile(String srcFileName, String destFileName, boolean overlay)
            throws IOException {
        File srcFile = new File(srcFileName);
        // 判断源文件是否存在
        if (!srcFile.exists()) {
            log.debug("源文件：{}不存在！", srcFileName);
            return false;
        } else if (!srcFile.isFile()) {
            log.debug("复制文件失败，源文件：{}不是一个文件！", srcFileName);
            return false;
        }
        File destFile = new File(destFileName);
        // 判断目标文件是否存在
        if (destFile.exists()) {
            if (overlay) {
                new File(destFileName).delete();
            }
        } else {
            // 如果目标文件所在目录不存在，则创建目录
            if (!destFile.getParentFile().exists()) {
                if (!destFile.getParentFile().exists()) {
                    // 复制文件失败;创建目标文件所在目录失败
                    return false;
                }
            }
        }
        // 复制文件
        // 读取的字节数
        int byteRead = 0;
        InputStream in = null;
        OutputStream out = null;
        try {
            in = new FileInputStream(srcFile);
            out = new BufferedOutputStream(new FileOutputStream(destFile));
            byte[] buffer = new byte[1024];
            while ((byteRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, byteRead);
            }
            //确保缓冲区数据被立即刷新到文件
            out.flush();
            return true;
        } catch (FileNotFoundException e) {
            log.error("系统异常", e);
            throw new RuntimeException(e);
        } catch (IOException e) {
            log.error("系统异常", e);
            throw new RuntimeException(e);
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
                if (in != null) {
                    in.close();
                }
            } catch (IOException e) {
                log.error("系统异常", e);
            }
        }
    }

    /**
     * 删除文件夹
     *
     * @param folder 文件夹
     * @throws Exception 异常
     */
    public static void deleteFolder(File folder) throws Exception {
        if (!folder.exists()) {
            throw new Exception("文件不存在");
        }
        File[] files = folder.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    // 递归直到目录下没有文件
                    deleteFolder(file);
                } else {
                    // 删除
                    file.delete();
                }
            }
        }
        // 删除
        folder.delete();
    }

    /**
     * 根据输入流、生成文件
     *
     * @param ins  输入流
     * @param file 文件
     */
    public static void cpFile(InputStream ins, File file) {
        BufferedOutputStream bos = null;
        BufferedInputStream bis = new BufferedInputStream(ins);
        try {
            bos = new BufferedOutputStream(new FileOutputStream(file));
            int bytesRead = 0;
            byte[] buffer = new byte[8192];
            while ((bytesRead = bis.read(buffer, 0, 8192)) != -1) {
                bos.write(buffer, 0, bytesRead);
            }
            //确保缓冲区数据被立即刷新到文件
            bos.flush();
        } catch (Exception e) {
            log.error("系统异常", e);
            throw new RuntimeException(e);
        } finally {
            try {
                ins.close();
                bos.close();
                bis.close();
            } catch (Exception e) {
                log.error("系统异常", e);
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * 删除文件，可以是文件或文件夹
     *
     * @param fileName 要删除的文件名
     * @return Result 删除成功返回true，否则返回false
     */
    public static boolean delete(String fileName) {
        File file = new File(fileName);
        if (!file.exists()) {
            return false;
        } else {
            if (file.isFile()) {
                return deleteFile(fileName);
            } else {
                return deleteDirectory(fileName);
            }
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

    /**
     * 根据url下载文件流
     *
     * @param urlStr 文件url
     * @return Result
     */
    public static InputStream getInputStreamFromUrl(String urlStr) {
        InputStream inputStream = null;
        HttpURLConnection conn = null;
        try {
            // url解码
            URL url = new URL(java.net.URLDecoder.decode(urlStr, "UTF-8"));
            conn = (HttpURLConnection) url.openConnection();
            // 设置超时间为3秒
            conn.setConnectTimeout(3 * 1000);
            // 设置请求属性，防止返回403错误
            conn.setRequestProperty("User-Agent",
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 "
                            + "(KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.3");
            // 得到输入流
            inputStream = conn.getInputStream();
        } catch (IOException e) {
            log.info("url获取文件流出错", e);
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
        return inputStream;
    }

    /**
     * 根据url获取文件大小
     *
     * @param urlStr  文件在存储设备中的地址(路径+文件名)
     * @param tempUrl 文件缓存地址(路径+文件名)
     * @return Result
     */
    public static long getFileSize(String urlStr, String tempUrl) {
        long fileSize = 0;
        try {
            if (log.isInfoEnabled()) {
                log.info("从http中获取文件大小");
            }
            URL url = new URL(java.net.URLDecoder.decode(urlStr, "UTF-8"));
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("HEAD");
            fileSize = conn.getContentLengthLong();
            conn.disconnect();
            //是否缓存中有文件 如果有则获取缓存中的文件大小
            File fileTemp = new File(tempUrl);
            if (fileTemp.exists()) {
                fileSize = fileTemp.length();
                if (log.isInfoEnabled()) {
                    log.info("从缓存路径中获取文件大小{}", fileSize);
                }
            }
        } catch (IOException e) {
            if (log.isInfoEnabled()) {
                log.info("从本地路径中获取文件大小");
            }
            //先从缓存中取
            File fileTemp = new File(tempUrl);
            if (fileTemp.exists()) {
                fileSize = fileTemp.length();
                if (log.isInfoEnabled()) {
                    log.info("从缓存路径中获取文件大小{}", fileSize);
                }
            } else {
                File file = new File(urlStr);
                if (file.exists()) {
                    fileSize = file.length();
                    log.info("从本地路径中获取文件大小{}", fileSize);
                }
            }
        }
        return fileSize;
    }

    /**
     * @param inputStream 输入流
     * @return Result
     * @throws IOException 异常
     */
    public static byte[] read(InputStream inputStream) {
        byte[] buffer = new byte[2048];
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            int num = inputStream.read(buffer);
            while (num != -1) {
                baos.write(buffer, 0, num);
                num = inputStream.read(buffer);
            }
            baos.flush();
            return baos.toByteArray();
        } catch (IOException e) {
            if (log.isErrorEnabled()) {
                log.error("未读取到文件", e);
            }
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    log.error("关闭流失败", e);
                }
            }
        }
        return buffer;
    }

    /**
     * InputStream转ByteArrayOutputStream
     *
     * @param inputStreamFromUrl 输入流
     * @return Result
     */
    public static ByteArrayOutputStream getByteOutputStream(InputStream inputStreamFromUrl)
            throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int nRead;
        byte[] data = new byte[1024];
        try {
            while ((nRead = inputStreamFromUrl.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }
            buffer.flush();
            return buffer;
        } finally {
            if (inputStreamFromUrl != null) {
                inputStreamFromUrl.close();
            }
        }
    }

    /**
     * 统一转换
     *
     * @param request 请求头
     * @throws IOException 异常
     */
    public static List<MultipartFile> transhandleFile(MultipartHttpServletRequest request) {
        try {
            MultipartHttpServletRequest multipartRequest = request;
            Iterator<String> itr = multipartRequest.getFileNames();
            List<MultipartFile> files = new ArrayList<>();
            while (itr.hasNext()) {
                List<MultipartFile> files1 = multipartRequest.getFiles(itr.next());
                files.addAll(files1);
            }
            return files;
        } catch (Exception e) {
            log.error("获取文件错误", e);
        }
        return new ArrayList<>();
    }

    /**
     * 创建临时文件
     * 该文件会在 JVM 退出时，进行删除
     *
     * @param data 文件内容
     * @return Result 文件
     */
    @SneakyThrows
    public static File createTempFile(String data) {
        File file = createTempFile();
        // 写入内容
        FileUtil.writeUtf8String(data, file);
        return file;
    }

    /**
     * 创建临时文件
     * 该文件会在 JVM 退出时，进行删除
     *
     * @param data 文件内容
     * @return Result 文件
     */
    @SneakyThrows
    public static File createTempFile(byte[] data) {
        File file = createTempFile();
        // 写入内容
        FileUtil.writeBytes(data, file);
        return file;
    }

    /**
     * 创建临时文件，无内容
     * 该文件会在 JVM 退出时，进行删除
     *
     * @return Result 文件
     */
    @SneakyThrows
    public static File createTempFile() {
        // 创建文件，通过 UUID 保证唯一
        File file = File.createTempFile(IdUtil.simpleUUID(), null);
        // 标记 JVM 退出时，自动删除
        file.deleteOnExit();
        return file;
    }

    /**
     * 生成文件路径
     *
     * @param content      文件内容
     * @param originalName 原始文件名
     * @return Result path，唯一不可重复
     */
    public static String generatePath(byte[] content, String originalName) {
        String sha256Hex = DigestUtil.sha256Hex(content);
        // 情况一：如果存在 name，则优先使用 name 的后缀
        if (StrUtil.isNotBlank(originalName)) {
            String extName = FileNameUtil.extName(originalName);
            return StrUtil.isBlank(extName) ? sha256Hex : sha256Hex + "." + extName;
        }
        // 情况二：基于 content 计算
        return sha256Hex + '.' + FileTypeUtil.getType(new ByteArrayInputStream(content));
    }

    /**
     * 清理XML文件
     * @param imagefile 图片文件
     */
    public static void deleteXml(File imagefile) {
        File[] files = imagefile.listFiles();
        for (File file : files) {
            if (file.getName().indexOf("xml") > -1) {
                file.delete();
            }
        }
    }

    /**
     * 预览所有类型文件 文档类型、视频、音频、图片
     * @param request          请求(必填)
     * @param response         响应(必填)
     * @param fileLen          文件大小(预览视频必填其他非必填)
     * @param type             是否进行缓存(非必填,null为不缓存)
     * @param originalFilename 文件全名(包含后缀)(必填)
     * @param inputStream      输入流(必填)
     */
    public static void printAllFile(HttpServletRequest request, HttpServletResponse response,
                                    Long fileLen, Integer type, String originalFilename,
                                    InputStream inputStream) {
        try (ServletOutputStream servletOutputStream = response.getOutputStream()) {
            String ext = getFileExtension(originalFilename).toLowerCase();
            if (inputStream == null || inputStream.available() == 0) {
                log.info("存储设备中找不到文件");
                throw new SunyardException(ResultCode.PARAM_ERROR, "文件不存在");
            } else {
                if (VIDEOS.contains(ext)) {
                    String name = originalFilename.replaceFirst("[.][^.]+$", "");
                    response.setHeader("Content-disposition",
                            "inline;filename=" + URLEncoder.encode(name + ".mp4", "UTF-8"));
                    loadVideosAudisoPaf(request, response, fileLen, inputStream);
                } else if (AUDIOS.contains(ext)) {
                    String name = originalFilename.replaceFirst("[.][^.]+$", "");
                    response.setHeader("Content-disposition",
                            "inline;filename=" + URLEncoder.encode(name + ".mp3", "UTF-8"));
                    loadVideosAudisoPaf(request, response, fileLen, inputStream);
                } else if (DOCS.contains(ext)) {
                    String name = originalFilename.replaceFirst("[.][^.]+$", "");
                    response.setHeader("Content-disposition",
                            "inline;filename=" + URLEncoder.encode(name + ".pdf", "UTF-8"));
                    loadPdf(response, request, inputStream);
                } else {
                    response.setHeader("Content-disposition",
                            "inline;filename=" + URLEncoder.encode(originalFilename, "UTF-8"));
                    servletOutputStream.write(FileUtils.read(inputStream));
                    FileUtils.printFile(response, servletOutputStream, originalFilename, type);
                }
            }
        } catch (IOException e) {
            log.error("异常描述", e);
            throw new RuntimeException(e);
        } finally {
            try {
                if (null != inputStream) {
                    inputStream.close();
                }
            } catch (IOException e) {
                log.error("异常描述", e);
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * 资源懒加载
     *
     * @param request     请求头
     * @param response    响应头
     * @param fileLen      大小
     * @param inputStream 输入流
     * @throws IOException 异常
     */
    public static void loadVideosAudisoPaf(HttpServletRequest request, HttpServletResponse response,
                                           Long fileLen, InputStream inputStream)
            throws IOException {
        log.info("视频或音频分段获取资源");
        //inputStream.available()返回int类型 文件超出2147483647大小会有问题
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
        if (range.endsWith("-")) {
            end = fileLen - 1;
        } else {
            end = Long.parseLong(range.substring(range.indexOf("-") + 1));
        }
        String contentRange = "bytes " + String.valueOf(start) + "-" + end + "/"
                + String.valueOf(fileLen);
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
     * @throws FileNotFoundException 异常
     */
    public static void loadPdf(HttpServletResponse response, HttpServletRequest request,
                               InputStream inputStream)
            throws IOException {
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
            response.setHeader("Content-Range",
                    "bytes " + startByte + "-" + endByte + "/" + totalByte);
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
            while ((bytesRead = bis.read(buffer, 0, Math.min(buffer.length, length))) != -1
                    && length > 0) {
                bos.write(buffer, 0, bytesRead);
                length -= bytesRead;
            }
            response.flushBuffer();
        } catch (IOException e) {
            log.error("异常描述", e);
            throw new RuntimeException(e);
        } finally {
            inputStream.close();
        }
    }

    public static String getFileExtension(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return "";
        }
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex == -1 || lastDotIndex == fileName.length() - 1) {
            return "";
        }
        return fileName.substring(lastDotIndex + 1);
    }
}
