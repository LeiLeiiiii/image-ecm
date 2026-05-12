package com.sunyard.framework.img.util;


import com.sunyard.framework.common.util.AssertUtils;
import com.sunyard.framework.common.util.FileUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * 图像处理需要掉用python脚本才能执行的服务
 */
@Slf4j
public class ImgPythonCheckUtils {

    public static final String PYTHON_3 = "python3";
    public static String PYTHONTYPE_RECTIFYPIC = "1";
    public static String PYTHONTYPE_COMPAREIMG = "2";
    public static String PYTHONTYPE_CHECKBLURRY = "3";
    public static String PYTHONTYPE_UPDATE = "5";

    /**
     * 将InputStream的内容写入到指定的文件路径。
     *
     * @param outputFilePath 文件路径
     * @param inputStream    输入流
     * @param basePath
     * @param ext
     * @throws IOException 如果发生I/O错误
     */
    public static String writeToFile(String outputFilePath, InputStream inputStream, String basePath, String ext) {
        if(outputFilePath==null){
            outputFilePath =  basePath+"/del-"+UUID.randomUUID() + "." +ext;
        }else{
            if (new File(outputFilePath).exists()) {
                return outputFilePath;
            }
        }

        try (FileOutputStream fileOutputStream = new FileOutputStream(outputFilePath);
             BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream)) {

            byte[] buffer = new byte[4096];
            int bytesRead;

            // 从BufferedInputStream读取数据并写入到FileOutputStream
            while ((bytesRead = bufferedInputStream.read(buffer)) != -1) {
                fileOutputStream.write(buffer, 0, bytesRead);
            }
            log.debug("文件写入完成:{}",outputFilePath);
        } catch (IOException e) {
            log.error("系统异常",e);
            throw new RuntimeException(e);
        }
        return outputFilePath;
    }


    /**
    *
    * */
    public static InputStream handleImgByInputStream(InputStream inputStream, String fileName, String ext, String pythonDir, String string) {
        fileName = writeToFile(fileName, inputStream, pythonDir, ext);
        String relativePath = pythonDir + "ImgUtils.py";
        String path = pythonDir + "temp_" + UUID.randomUUID() + "." + ext;
        // Python脚本的路径和要传递的参数
        List<String> command = Arrays.asList(PYTHON_3, relativePath, PYTHONTYPE_UPDATE, fileName, path,string);
        log.info(command.toString());
        String ret = basePostPython(command);
        if (!StringUtils.isEmpty(ret) && ret.contains("Succ")) {
            try {
                FileInputStream fileInputStream = new FileInputStream(path);
                return fileInputStream;
            } catch (FileNotFoundException e) {
                log.error("文件不存在", e);
                throw new RuntimeException(e);
            } finally {
                FileUtils.deleteFile(path);
            }
        } else {
            AssertUtils.isTrue(true, "脚本执行失败");
        }
        return null;
    }

    /**
     * 图像纠偏
     *
     * @param ext
     */
    public static FileInputStream rectifyPic(InputStream inputStream, String fileName, String basePath, String ext) {
        fileName = writeToFile(fileName, inputStream, basePath, ext);
        String relativePath = basePath + "/ImgUtils.py";
        String path = basePath + "temp_" + UUID.randomUUID() + "." + ext;
        // Python脚本的路径和要传递的参数
        List<String> command = Arrays.asList(PYTHON_3, relativePath, PYTHONTYPE_RECTIFYPIC, fileName, path);
        String ret = basePostPython(command);

        if (!StringUtils.isEmpty(ret) && "Succ".equals(ret)) {
            try {
                FileInputStream fileInputStream = new FileInputStream(path);
                return fileInputStream;
            } catch (FileNotFoundException e) {
                log.error("文件不存在", e);
                throw new RuntimeException(e);
            } finally {
                FileUtils.deleteFile(fileName);
                FileUtils.deleteFile(path);
            }
        } else {
            AssertUtils.isTrue(true, "脚本执行失败");
        }
        return null;
    }

    /**
     * 图片比对
     *
     * @return
     */
    public static String compareImg(String basePath, String filepath1, String filepath2, String filepath1Save, String filepath2Save) {
        String relativePath = basePath + "/ImgUtils.py";
        // Python脚本的路径和要传递的参数
        List<String> command = Arrays.asList(PYTHON_3, relativePath, PYTHONTYPE_COMPAREIMG,filepath1, filepath2, filepath1Save, filepath2Save);
        String ret = basePostPython(command);

        if (!StringUtils.isEmpty(ret) && "Succ".equals(ret)) {
            return ret;
        } else {
            AssertUtils.isTrue(true, "脚本执行失败");
        }

        return null;
    }

    /**
     * 图像模糊检测
     *
     * @param filepath
     * @return
     */
    public static Boolean checkBlurry(String filepath, String pythonPath, Integer threshold) {
        String relativePath = pythonPath + "/ImgUtils.py";
        // Python脚本的路径和要传递的参数
        List<String> command = Arrays.asList(PYTHON_3, relativePath,PYTHONTYPE_CHECKBLURRY, filepath, threshold.toString());
        String ret = basePostPython(command);

        if (!StringUtils.isEmpty(ret)) {
            return "True".equals(ret);
        } else {
            AssertUtils.isTrue(true, "脚本执行失败");
        }

        return false;
    }

    /**
     * 图像模糊检测
     *
     * @param filepath
     * @return
     */
    public static Boolean checkBlurry(String filepath, String pythonPath) {
        return checkBlurry(filepath, pythonPath, 1000000);
    }

    /**
     *
     * */
    public static String basePostPython(List<String> command) {
        Process process = null;
        try {
            // 使用ProcessBuilder构建命令
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.redirectErrorStream(true);
            // 启动进程
            process = processBuilder.start();

            // 读取Python脚本的标准输出
            try(InputStreamReader inputStreamReader = new InputStreamReader(process.getInputStream());
            BufferedReader reader = new BufferedReader(inputStreamReader);){
                String line;
                StringBuilder output = new StringBuilder();
                while ((line = reader.readLine()) != null) {
                    output.append(line);
                }

            // 等待进程结束并获取退出码
            int exitCode = process.waitFor();

            if (exitCode == 0) {
                return output.toString();
            } else {
                log.error("python脚本执行有误：" + output.toString());
            }
            }
        } catch (IOException | InterruptedException e) {
            log.error("系统异常",e);
            throw new RuntimeException(e);
        } finally {
            // 确保进程被正确关闭（如果已启动）
            if (process != null && process.isAlive()) {
                process.destroy();
            }
        }
        return null;
    }

}
