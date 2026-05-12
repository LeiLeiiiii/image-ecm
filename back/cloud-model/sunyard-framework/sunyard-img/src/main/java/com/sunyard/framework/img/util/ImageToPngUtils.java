package com.sunyard.framework.img.util;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.im4java.core.ConvertCmd;
import org.im4java.core.IM4JavaException;
import org.im4java.core.IMOperation;
import org.im4java.core.ImageMagickCmd;
import org.im4java.process.Pipe;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;

import com.sunyard.framework.common.util.FileUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * @author zyl
 * @Description
 * @since 2023/10/9 15:44
 */
@Slf4j
public class ImageToPngUtils {

    /**
     * 使用openCV将特别图片转jpg
     *
     * @param inputStream 输入流
     * @return Result
     */
    public static InputStream specialImagesToPng(InputStream inputStream) {
        try {
            System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
            //读取InputStream并转换为byte
            byte[] byteArray;
            byteArray = FileUtils.read(inputStream);
            //根据 byte 转 Mat
            Mat originalImage = Imgcodecs.imdecode(new MatOfByte(byteArray), Imgcodecs.IMREAD_COLOR);
            MatOfByte matOfByte = new MatOfByte();
            // 将图片以jpg格式输出
            Imgcodecs.imencode(".jpg", originalImage, matOfByte);
            return new ByteArrayInputStream(matOfByte.toArray());
        } catch (Exception e) {
            log.error("系统异常",e);
        }finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                log.error("系统异常",e);
            }
        }
        return null;
    }

    /**
     * 使用 im4java 调用 magick 命令处理图片（无临时文件，直接返回流）
     * @param inputStream 原始图片输入流（HEIC/TIF等）
     * @return 转换后的JPG输入流
     */
    public static InputStream specialImagesToJpg(InputStream inputStream) {
        // 1. 提前校验入参
        if (inputStream == null) {
            throw new IllegalArgumentException("输入流不能为空");
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            log.info("使用 im4java 调用 magick 命令处理图片（无临时文件）");

            // 2. 构建IM操作：输入从标准流读，输出写到标准流（格式为JPG）
            IMOperation operation = new IMOperation();
            operation.addImage("-");
            operation.quality(90.0);
            operation.addImage("jpg:-");

            // 3. 初始化 ImageMagickCmd（指定magick命令，避免找错）
            ImageMagickCmd cmd = new ImageMagickCmd("magick");

            // 4. 绑定输入/输出管道（核心：输入流→magick，magick输出→内存流）
            Pipe pipeIn = new Pipe(inputStream, null);
            Pipe pipeOut = new Pipe(null, outputStream);
            cmd.setInputProvider(pipeIn);
            cmd.setOutputConsumer(pipeOut);

            // 5. 执行转换（无文件写入，直接输出到内存流）
            cmd.run(operation);
            log.info("图片转换完成，输出流大小：{} 字节", outputStream.size());

            // 6. 将内存输出流转为输入流返回
            return new ByteArrayInputStream(outputStream.toByteArray());

        } catch (InterruptedException | IM4JavaException | IOException e) {
            log.error("im4java调用magick命令转换异常", e);
            throw new RuntimeException("图片转换失败", e);
        } catch (Exception e) {
            log.error("未知异常", e);
            throw new RuntimeException("图片处理出错", e);
        } finally {
            // 关闭临时内存流（ByteArrayOutputStream无需强制关闭，但养成习惯）
            try {
                outputStream.close();
            } catch (IOException e) {
                log.error("关闭内存流异常", e);
            }
            // 注意：不关闭入参inputStream，由调用方负责关闭
        }
    }


    /**
     * 使用 im4java 处理图片
     * im4java可以将多页tif文件转为多个图片
     *
     * @param inputStream 输入流
     * @return Result
     */
    public static InputStream specialImagesToJpgTwo(InputStream inputStream) {
        InputStream fileInputStream = null;
        try {
            // 初始化操作
            IMOperation operation = new IMOperation();
            // 从标准输入流中读取图像
            operation.addImage("-");
            // 设置输出格式为PNG
            operation.format("jpg");
            // 将转换后的图像输出到标准输出流
            operation.addImage("-");

            // 创建ConvertCmd对象，并设置其路径
            ConvertCmd cmd = new ConvertCmd();
            // 读取输入流并转换
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            Pipe pipeIn = new Pipe(inputStream, null);
            Pipe pipeOut = new Pipe(null, outputStream);
            //Windows需要设置，Linux不需要
            cmd.setInputProvider(pipeIn);
            cmd.setOutputConsumer(pipeOut);
            cmd.run(operation);

            // 返回转换后的图像作为InputStream
            fileInputStream = new ByteArrayInputStream(outputStream.toByteArray());
        } catch (InterruptedException | IM4JavaException | IOException e) {
            log.error("系统异常",e);
            throw new RuntimeException(e);
        }
        return fileInputStream;
    }

}
