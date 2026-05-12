package com.sunyard.module.storage.util;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;
import org.springframework.util.ObjectUtils;

import com.sunyard.framework.common.util.AssertUtils;
import com.sunyard.framework.common.util.FileUtils;
import com.sunyard.framework.img.util.ImageUtils;
import com.sunyard.framework.img.util.RectifyImageUtils;
import com.sunyard.framework.img.util.RemoveBackgroudUtils;
import com.sunyard.module.storage.constant.StateConstants;
import com.sunyard.module.storage.vo.PictureRotatingInfoVO;

import cn.hutool.core.util.ObjectUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * 图片处理工具类
 *
 * @author PJW
 */
@Slf4j
public class ImgUpdateUtils {

    private final static Double ZERO = 0D;
    /**
     */
    public static ByteArrayInputStream handleImgByInputStream(InputStream inputStream, PictureRotatingInfoVO p) {
        byte[] byteArray;
        // 读取InputStream并转换为OpenCV的Mat对象
        byteArray = FileUtils.read(inputStream);
        // 使用OpenCV将字节数组转换为Mat
        Mat originalImage = Imgcodecs.imdecode(new MatOfByte(byteArray), Imgcodecs.IMREAD_COLOR);
        return handleImg(originalImage, p, byteArray);
    }

    /**
     * 处理图片
     *
     * @param originalImage 源文件名
     * @param p 处理信息
     * @param byteArray 源文件字节
     * @return Result
     */
    public static ByteArrayInputStream handleImg(Mat originalImage, PictureRotatingInfoVO p, byte[] byteArray) {
        // 一定是先旋转或镜像再去亮化淡化等
        Mat mat = new Mat();
        if (!ObjectUtils.isEmpty(p.getRotateTime()) && !ObjectUtils.isEmpty(p.getHorizontalMirrorTime())
            && !ObjectUtils.isEmpty(p.getVerticallyMirrorTime())) {
            long min = Math.min(Math.max(p.getRotateTime(), p.getHorizontalMirrorTime()), p.getVerticallyMirrorTime());
            if (min == p.getRotateTime()) {
                // 先旋转
                mat = rotate(originalImage, p);
                if (Math.min(p.getVerticallyMirrorTime(), p.getHorizontalMirrorTime()) == p.getVerticallyMirrorTime()) {
                    // 垂直镜像
                    mat = verticallyMirror(p, mat);
                    // 水平镜像
                    mat = horizontalMirror(p, mat);
                } else {
                    // 水平镜像
                    mat = horizontalMirror(p, mat);
                    // 垂直镜像
                    mat = verticallyMirror(p, mat);
                }
            } else if (min == p.getHorizontalMirrorTime()) {
                // 先水平镜像
                mat = horizontalMirror(p, originalImage);
                if (Math.min(p.getVerticallyMirrorTime(), p.getRotateTime()) == p.getVerticallyMirrorTime()) {
                    // 垂直镜像
                    mat = verticallyMirror(p, mat);
                    // 旋转
                    mat = rotate(mat, p);
                } else {
                    // 旋转
                    mat = rotate(mat, p);
                    // 垂直镜像
                    mat = verticallyMirror(p, mat);
                }
            } else {
                // 先垂直镜像
                mat = verticallyMirror(p, originalImage);
                if (Math.max(p.getHorizontalMirrorTime(), p.getRotateTime()) == p.getHorizontalMirrorTime()) {
                    // 水平镜像
                    mat = horizontalMirror(p, originalImage);
                    // 旋转
                    mat = rotate(mat, p);
                } else {
                    // 旋转
                    mat = rotate(mat, p);
                    // 水平镜像
                    mat = horizontalMirror(p, mat);
                }
            }
        }
        // 裁剪
        boolean b = !ObjectUtils.isEmpty(p.getX1()) && !ObjectUtils.isEmpty(p.getY1())
            && !ObjectUtils.isEmpty(p.getX2()) && !ObjectUtils.isEmpty(p.getY2());
        if (b) {
            mat = ImageUtils.cropImg(mat, p.getCrop());
        }
        // 先 去黑边 因为亮化 淡化 锐化 都会影响图片像素
        if (ObjectUtil.isNotEmpty(p.getBlackEdge()) && p.getBlackEdge()) {
            mat = RemoveBackgroudUtils.removeBlackEdgeTwo(mat);
        }
        // 纠偏
        if (ObjectUtil.isNotEmpty(p.getCorrective()) && p.getCorrective()) {
            mat = RectifyImageUtils.imgCorrection(mat, byteArray);
        }
        // 亮化 原图，后面操作图片对象均为处理后的图片
        if (!ObjectUtils.isEmpty(p.getBrighten()) && !ZERO.equals(p.getBrighten())) {
            mat = ImageUtils.brightenImg(mat, p.getBrighten());
        }
        // 淡化
        if (!ObjectUtils.isEmpty(p.getDownplay()) && !ZERO.equals(p.getDownplay())) {
            mat = ImageUtils.downplayImg(mat, p.getDownplay());
        }
        // 锐化
        if (!ObjectUtils.isEmpty(p.getSharpen()) && !ZERO.equals(p.getSharpen())) {
            mat = ImageUtils.sharpenImg(mat, p.getSharpen());
        }
        if (!ObjectUtils.isEmpty(mat)) {
            // 保存的二进制数据
            MatOfByte bt = new MatOfByte();
            // Mat转换成二进制数据。.png表示图片格式，格式不重要，基本不会对程序有任何影响。
            // 转时按照jpg格式转(为了解决图片旋转后大小变化太大问题)，不影响图片实际格式
            Imgcodecs.imencode(".jpg", mat, bt);
            // 二进制数据转换成Image
            return new ByteArrayInputStream(bt.toArray());
        }
        return null;
    }

    /**
     * 压缩图片
     *
     * @param inputStream 文件流
     * @param targetWidth 目标宽度
     * @param targetHeight 目标高度
     * @param quality 压缩比
     * @return Result
     */
    private static InputStream compressImage(InputStream inputStream, int targetWidth, int targetHeight,
        float quality) {
        try {
            // 读取原始图片
            BufferedImage sourceImage = ImageIO.read(inputStream);
            // 创建缩放后的图片
            BufferedImage compressedImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
            Graphics2D graphics = compressedImage.createGraphics();
            graphics.drawImage(sourceImage, 0, 0, targetWidth, targetHeight, null);
            graphics.dispose();
            // 设置压缩参数
            ImageWriter writer = ImageIO.getImageWritersByFormatName("jpg").next();
            ImageWriteParam params = writer.getDefaultWriteParam();
            params.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            params.setCompressionQuality(quality);
            // 写入压缩后的图片到字节数组输出流
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ImageOutputStream imageOutputStream = ImageIO.createImageOutputStream(outputStream);
            writer.setOutput(imageOutputStream);
            writer.write(null, new IIOImage(compressedImage, null, null), params);
            writer.dispose();
            imageOutputStream.close();
            // 将字节数组输出流转换为输入流
            byte[] compressedBytes = outputStream.toByteArray();
            return new ByteArrayInputStream(compressedBytes);
        } catch (IOException e) {
            log.error("异常描述", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 水平镜像
     *
     * @param p 处理信息
     * @param mat mat对象
     * @return Mat
     */
    private static Mat horizontalMirror(PictureRotatingInfoVO p, Mat mat) {
        if (!p.getHorizontalMirrorTime().equals(0L)) {
            AssertUtils.isNull(p.getHorizontalMirror(), "horizontalMirror: 水平镜像参数不能为空");
            if (!p.getHorizontalMirror().equals(0)) {
                Core.flip(mat, mat, 0);
            }
        }
        return mat;
    }

    /**
     * 垂直镜像
     *
     * @param p 处理信息
     * @param mat mat对象
     */
    private static Mat verticallyMirror(PictureRotatingInfoVO p, Mat mat) {
        if (!p.getMirrorVertically().equals(0)) {
            AssertUtils.isNull(p.getMirrorVertically(), "mirrorVertically: 垂直镜像参数不能为空");
            if (!p.getMirrorVertically().equals(0)) {
                Core.flip(mat, mat, p.getMirrorVertically());
            }
        }
        return mat;
    }

    /**
     * 旋转图片
     *
     * @param originalImage 源图片mat对象
     * @param p 处理信息
     * @return Result
     */
    private static Mat rotate(Mat originalImage, PictureRotatingInfoVO p) {
        Mat mat;
        if (p.getRotateTime().equals(0L)) {
            return originalImage;
        }
        if (p.getRotationAngle().equals(StateConstants.NINETY)) {
            mat = ImageUtils.rotateRight(originalImage);
        } else if (Objects.equals(p.getRotationAngle(), StateConstants.TWO_HUNDRED_SEVENTY)) {
            mat = ImageUtils.rotateLeft(originalImage);
        } else {
            mat = ImageUtils.rotate(originalImage, p.getRotationAngle(),p.getDirection()==null?0:p.getRotationAngle());
        }
        return mat;
    }

}
