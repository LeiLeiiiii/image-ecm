package com.sunyard.framework.img.util;

import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;
import com.sunyard.framework.common.util.AssertUtils;
import lombok.extern.slf4j.Slf4j;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.features2d.AKAZE;
import org.opencv.features2d.BFMatcher;
import org.opencv.features2d.Features2d;
import org.opencv.highgui.HighGui;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.utils.Converters;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.AlphaComposite;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * javacv opencv 图片操作工具类（去黑边、图片旋转）
 *
 * @author ascend
 * @date 2018/12/14
 */
@Slf4j
public class ImageUtils {
    /**
     * 去黑边"全黑"阈值
     */
    private static final double BLACK_VALUE = 5;
    /**
     * 去白边“全白”阈值
     */
    private static final double WHITE_VALUE = 254.0;
    /**
     * 相似度阈值
     */
    private static final double SIMILARITY_VALVE = 60.0;

    private ImageUtils() {
    }

    /**
     * 保存图片
     *
     * @param fileName 绝对路径
     * @param mat      源Mat
     */
    public static void save(String fileName, Mat mat) {
        Imgcodecs.imwrite(fileName, mat);
    }

    /**
     * 显示图片， 阻塞。默认标题“结果”
     *
     * @param mat Mat
     */
    public static void showImg(Mat mat) {
        showImg(mat, "结果", 0);
    }

    /**
     * 显示图片，阻塞指定时长。默认标题“结果”
     *
     * @param mat    Mat
     * @param second int
     */
    public static void showImg(Mat mat, int second) {
        showImg(mat, "结果", second);
    }

    /**
     * 显示图片与标题，阻塞指定时长
     *
     * @param mat    Mat
     * @param title  标题
     * @param second int
     */
    public static void showImg(Mat mat, String title, int second) {
        HighGui.imshow(title, mat);
        HighGui.waitKey(second * 1000);
    }

    /**
     * 按照指定的尺寸截取Mat，截取宽高自动计算（对称）。坐标原点为左上角
     *
     * @param src 源Mat
     * @param x   x
     * @param y   y
     * @return Result 截取后的Mat
     */
    public static Mat cut(Mat src, int x, int y) {
        // 截取尺寸
        int width = src.width() - 2 * x;
        int height = src.height() - 2 * y;
        return cut(src, x, y, width, height);
    }

    /**
     * 按照指定的尺寸截取Mat，坐标原点为左上角
     *
     * @param src    源Mat
     * @param x      x
     * @param y      y
     * @param width  width
     * @param height height
     * @return Result 截取后的Mat
     */
    public static Mat cut(Mat src, int x, int y, int width, int height) {
        if (x < 0) {
            x = 0;
        }
        if (y < 0) {
            y = 0;
        }
        if (width > src.width()) {
            width = src.width();
        }
        if (height > src.height()) {
            height = src.height();
        }
        // 截取尺寸
        Rect rect = new Rect(x, y, width, height);
        return new Mat(src, rect);
    }

    /**
     * 缩放
     *
     * @param srcMat 源Mat
     * @param dSize  目标大小
     * @return Result 缩放结果Mat
     */
    public static Mat resize(Mat srcMat, Size dSize) {
        Mat retMat = new Mat();
        Imgproc.resize(srcMat, retMat, dSize);
        return retMat;
    }

    /**
     * 逆时针旋转,但是图片宽高不用变,此方法与rotateLeft和rotateRight不兼容
     *
     * @param src    源
     * @param angle 旋转的角度
     * @return Result 旋转后的对象
     */
    public static Mat rotate(Mat src, double angle, Integer direction) {
        // 根据方向调整角度，0为顺时针，1为逆时针
        if (direction == 0) {
            angle = -angle; // 顺时针旋转使用负角度
        }

        // 计算旋转后的图像尺寸
        double radians = Math.toRadians(angle);
        double cos = Math.abs(Math.cos(radians));
        double sin = Math.abs(Math.sin(radians));

        // 计算新的宽度和高度
        int newWidth = (int) Math.round(src.width() * cos + src.height() * sin);
        int newHeight = (int) Math.round(src.width() * sin + src.height() * cos);

        // 获取旋转矩阵并调整平移量
        Point center = new Point(src.width() / 2.0, src.height() / 2.0);
        Mat affineTrans = Imgproc.getRotationMatrix2D(center, angle, 1.0);

        // 调整平移，使旋转后的图像居中显示
        affineTrans.put(0, 2, affineTrans.get(0, 2)[0] + (newWidth / 2.0 - center.x));
        affineTrans.put(1, 2, affineTrans.get(1, 2)[0] + (newHeight / 2.0 - center.y));

        // 执行旋转操作，使用新的图像尺寸
        Mat dst = new Mat();
        Imgproc.warpAffine(src, dst, affineTrans, new Size(newWidth, newHeight),
                Imgproc.INTER_LINEAR, Core.BORDER_CONSTANT, new Scalar(255, 255, 255));

        return dst;
    }

    /**
     * 图像整体向左旋转90度
     *
     * @param src Mat
     * @return Result 旋转后的Mat
     */
    public static Mat rotateLeft(Mat src) {
        Mat tmp = new Mat();
        // 此函数是转置、（即将图像逆时针旋转90度，然后再关于x轴对称）
        Core.transpose(src, tmp);
        Mat result = new Mat();
        // flipCode = 0 绕x轴旋转180， 也就是关于x轴对称
        // flipCode = 1 绕y轴旋转180， 也就是关于y轴对称
        // flipCode = -1 此函数关于原点对称
        Core.flip(tmp, result, 0);
        return result;
    }

    /**
     * 图像整体向左旋转90度
     *
     * @param src Mat
     * @return Result 旋转后的Mat
     */
    public static Mat rotateRight(Mat src) {
        Mat tmp = new Mat();
        // 此函数是转置、（即将图像逆时针旋转90度，然后再关于x轴对称）
        Core.transpose(src, tmp);
        Mat result = new Mat();
        // flipCode = 0 绕x轴旋转180， 也就是关于x轴对称
        // flipCode = 1 绕y轴旋转180， 也就是关于y轴对称
        // flipCode = -1 此函数关于原点对称
        Core.flip(tmp, result, 1);
        return result;
    }

    /**
     * 灰度处理 BGR灰度处理
     *
     * @param src 原图Mat
     * @return Result Mat 灰度后的Mat
     */
    public static Mat gray(Mat src) {
        Mat gray = new Mat();
        Imgproc.cvtColor(src, gray, Imgproc.COLOR_BGR2GRAY);
        return gray;
    }

    /**
     * 去除图片黑边，若无黑边，则原图副本返回。默认“全黑”阈值为 {@code BLACK_VALUE}
     *
     * @param srcMat 预去除黑边的Mat
     * @return Result 去除黑边之后的Mat
     */
    public static Mat removeBlackEdge(Mat srcMat) {
        return removeBlackEdge(srcMat, BLACK_VALUE);
    }

    /**
     * 去除图片黑边，若无黑边，则原图副本返回。
     *
     * @param blackValue 一般低于5的已经是很黑的颜色了
     * @param srcMat     源Mat对象
     * @return Result Mat对象
     */
    public static Mat removeBlackEdge(Mat srcMat, double blackValue) {
        // 预截取，默认播放条等情况的处理
        Mat smallMat = cut(srcMat, (int) (srcMat.width() * 0.02), (int) (srcMat.height() * 0.02));
        // 灰度
        Mat grayMat = gray(smallMat);
        int topRow = 0;
        int leftCol = 0;
        int rightCol = grayMat.width() - 1;
        int bottomRow = grayMat.height() - 1;

        // 上方黑边判断
        for (int row = 0; row < grayMat.height(); row++) {
            // 判断当前行是否基本“全黑”，阈值自定义；
            if (sum(grayMat.row(row)) / grayMat.width() < blackValue) {
                // 更新截取条件
                topRow = row;
            } else {
                break;
            }
        }
        // 左边黑边判断
        for (int col = 0; col < grayMat.width(); col++) {
            // 判断当前列是否基本“全黑”，阈值自定义；
            if (sum(grayMat.col(col)) / grayMat.height() < blackValue) {
                // 更新截取条件
                leftCol = col;
            } else {
                break;
            }
        }
        // 右边黑边判断
        for (int col = grayMat.width() - 1; col > 0; col--) {
            // 判断当前列是否基本“全黑”，阈值自定义；
            if (sum(grayMat.col(col)) / grayMat.height() < blackValue) {
                // 更新截取条件
                rightCol = col;
            } else {
                break;
            }
        }
        // 下方黑边判断
        for (int row = grayMat.height() - 1; row > 0; row--) {
            // 判断当前行是否基本“全黑”，阈值自定义；
            if (sum(grayMat.row(row)) / grayMat.width() < blackValue) {
                // 更新截取条件
                bottomRow = row;
            } else {
                break;
            }
        }

        int x = leftCol;
        int y = topRow;
        int width = rightCol - leftCol;
        int height = bottomRow - topRow;

        if (leftCol == 0 && rightCol == grayMat.width() - 1 && topRow == 0 && bottomRow == grayMat.height() - 1) {
            return srcMat.clone();
        }
        return cut(smallMat, x, y, width, height);
    }

    /**
     * 求和
     *
     * @param mat mat
     * @return Result sum
     */
    private static double sum(Mat mat) {
        double sum = 0;
        for (int row = 0; row < mat.height(); row++) {
            for (int col = 0; col < mat.width(); col++) {
                sum += mat.get(row, col)[0];
            }
        }
        return sum;
    }

    /**
     * 去除图片白边（纯色，灰度值为255.0），若无白边，则原图副本返回。
     *
     * @param srcMat 源Mat对象
     * @return Result Mat对象
     */
    public static Mat removeWhiteEdge(Mat srcMat) {
        return removeWhiteEdge(srcMat, WHITE_VALUE);
    }

    /**
     * 去除图片白边（纯色，灰度值为255.0），若无白边，则原图副本返回。
     *
     * @param srcMat     源Mat对象
     * @param whiteValue 一般高于254.0的已经是很白的颜色了
     * @return Result Mat对象
     */
    public static Mat removeWhiteEdge(Mat srcMat, double whiteValue) {
        // 灰度
        Mat grayMat = gray(srcMat);
        int topRow = 0;
        int leftCol = 0;
        int rightCol = grayMat.width() - 1;
        int bottomRow = grayMat.height() - 1;

        // 上方白边判断
        for (int row = 0; row < grayMat.height(); row++) {
            // 判断当前行是否全白
            if (sum(grayMat.row(row)) / grayMat.width() > whiteValue) {
                // 更新截取条件
                topRow = row;
            } else {
                break;
            }
        }
        // 左边白边判断
        for (int col = 0; col < grayMat.width(); col++) {
            // 判断当前列是否全白
            if (sum(grayMat.col(col)) / grayMat.height() > whiteValue) {
                // 更新截取条件
                leftCol = col;
            } else {
                break;
            }
        }
        // 右边白边判断
        for (int col = grayMat.width() - 1; col > 0; col--) {
            // 判断当前列是否基本全白
            if (sum(grayMat.col(col)) / grayMat.height() > whiteValue) {
                // 更新截取条件
                rightCol = col;
            } else {
                break;
            }
        }
        // 下方白边判断
        for (int row = grayMat.height() - 1; row > 0; row--) {
            // 判断当前行是否基本全白
            if (sum(grayMat.row(row)) / grayMat.width() > whiteValue) {
                // 更新截取条件
                bottomRow = row;
            } else {
                break;
            }
        }

        int x = leftCol;
        int y = topRow;
        int width = rightCol - leftCol;
        int height = bottomRow - topRow;

        if (leftCol == 0 && rightCol == grayMat.width() - 1 && topRow == 0 && bottomRow == grayMat.height() - 1) {
            return srcMat.clone();
        }
        return cut(srcMat, x, y, width, height);
    }

    /**
     * 直方图均衡化，入参必须为灰度图
     *
     * @param grayMat 灰度图
     * @return Result 均衡化后的Mat
     * @throws IllegalArgumentException 如果入参不是灰度图
     */
    public static Mat equalizeHist(Mat grayMat) {
        if (grayMat.channels() != 1) {
            throw new IllegalArgumentException("入参必须为灰度图");
        }
        Mat retMat = new Mat();
        Imgproc.equalizeHist(grayMat, retMat);
        return retMat;
    }

    /**
     * 判断两个mat是否符合系统设定的阈值 SIMILARITY_VALVE 采用 AKAZE 算法，后续可扩展其他算法 默认resize尺寸为 src.width -> 512.0 然后按照 4:3
     * 得到src的width，height，然后dest同尺度resize 若去黑边 {@code ImageUtils#removeBlackEdge}
     *
     * @param src  源
     * @param dest 目标
     * @return Result boolean true 实际相似度 >= SIMILARITY_VALVE 返回true
     */
    public static boolean isSimilar(Mat src, Mat dest) {
        return isSimilar(src, dest, 512.0);

    }

    /**
     * 判断两个mat是否符合系统设定的阈值 SIMILARITY_VALVE 采用 AKAZE 算法，后续可扩展其他算法 默认resize尺寸为 src.width -> width 然后按照 4:3
     * 得到src的width，height，然后dest同尺度resize 若去黑边 {@code ImageUtils#removeBlackEdge}
     *
     * @param src   源
     * @param dest  目标
     * @param width 宽
     * @return Result boolean true 实际相似度 >= SIMILARITY_VALVE 返回true
     */
    public static boolean isSimilar(Mat src, Mat dest, double width) {
        double height = width * 3 / 4;
        return isSimilar(src, dest, width, height, SIMILARITY_VALVE);

    }

    /**
     * 判断两个mat是否符合系统设定的阈值 SIMILARITY_VALVE 采用 AKAZE 算法，后续可扩展其他算法 给啥就比啥
     *
     * @param src             源
     * @param dest            目标
     * @param width           图片宽（px）
     * @param height          图片高（px）
     * @param similarityValue 相似度阈值
     * @return Result boolean true 实际相似度 >= SIMILARITY_VALVE 返回true
     */
    public static boolean isSimilar(Mat src, Mat dest, double width, double height, double similarityValue) {
        boolean isSimilar = false;
        // 缩放
        Size size = new Size(width, height);
        Mat smallSrcMat = resize(src, size);
        Mat smallDestMat = resize(dest, size);

        Mat srcGrayMat = gray(smallSrcMat);
        Mat destGrayMat = gray(smallDestMat);

        AKAZE akaze = AKAZE.create();

        MatOfKeyPoint mokp = new MatOfKeyPoint();
        Mat desc = new Mat();
        akaze.detect(srcGrayMat, mokp);
        akaze.compute(srcGrayMat, mokp, desc);

        MatOfKeyPoint mokp2 = new MatOfKeyPoint();
        Mat desc2 = new Mat();
        akaze.detect(destGrayMat, mokp2);
        akaze.compute(destGrayMat, mokp2, desc2);

        BFMatcher matcher = BFMatcher.create(Core.NORM_L2, true);
        MatOfDMatch matOfdMatch = new MatOfDMatch();
        matcher.match(desc, desc2, matOfdMatch);
        double similarity =
                (double) (2 * matOfdMatch.toArray().length) / (mokp.toArray().length + mokp2.toArray().length) * 100;
        if (similarity >= similarityValue) {
            isSimilar = true;
        }
        return isSimilar;
    }

    /**
     * 画出图片中的特征点，默认采用 AKAZE 算法
     *
     * @param imageMat 源图片
     * @return Result 画出特征点的 Mat
     */
    public static Mat drawKeyPoints(Mat imageMat) {
        AKAZE akaze = AKAZE.create();
        MatOfKeyPoint mokp = new MatOfKeyPoint();
        akaze.detect(imageMat, mokp);
        return drawKeyPoints(imageMat, mokp);
    }

    /**
     * 画出图片中的特征点
     *
     * @param imageMat 源图片
     * @param mokp     特征点
     * @return Result 画出特征点的 Mat
     */
    public static Mat drawKeyPoints(Mat imageMat, MatOfKeyPoint mokp) {
        Mat retMat = new Mat();
        Features2d.drawKeypoints(imageMat, mokp, retMat);
        return retMat;
    }

    private static void drawPoint(Mat srcMat, MatOfKeyPoint mokp, double width, double height, boolean src) {
        Mat outMat = new Mat();
        Features2d.drawKeypoints(srcMat, mokp, outMat, new Scalar(0, 0, 255));
        if (src) {
            save("/tmp/tmp/" + width + " X " + height + "_src.jpg", outMat);
        } else {

            save("/tmp/tmp/" + width + " X " + height + "_dest.jpg", outMat);
        }
    }

    /**
     * 未处理 反色处理
     *
     * @param image
     * @return Result
     */
    public static Mat inverse(Mat image) {
        int width = image.cols();
        int height = image.rows();
        int dims = image.channels();
        byte[] data = new byte[width * height * dims];
        image.get(0, 0, data);
        int index = 0;
        int r = 0;
        int g = 0;
        int b = 0;
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width * dims; col += dims) {
                index = row * width * dims + col;
                b = data[index] & 0xff;
                g = data[index + 1] & 0xff;
                r = data[index + 2] & 0xff;
                r = 255 - r;
                g = 255 - g;
                b = 255 - b;
                data[index] = (byte) b;
                data[index + 1] = (byte) g;
                data[index + 2] = (byte) r;
            }
        }
        image.put(0, 0, data);
        return image;
    }

    /**
     * Mat转换成BufferedImage
     *
     * @param img     要转换的Mat
     * @param extName 格式为 ".jpg", ".png", etc
     * @return Result BufferedImage
     */
    public static BufferedImage mat2BufferedImage(Mat img, String extName) {
        // 将矩阵转换为适合此文件扩展名的字节矩阵
        MatOfByte mob = new MatOfByte();
        Imgcodecs.imencode(extName, img, mob);
        // 将字节矩阵转换为字节数组
        byte[] byteArray = mob.toArray();
        BufferedImage bufImage = null;
        try (InputStream in = new ByteArrayInputStream(byteArray)) {
            bufImage = ImageIO.read(in);
        } catch (Exception e) {
            log.error("系统异常",e);
            throw new RuntimeException(e);
        }
        return bufImage;
    }

    /**
     * BufferedImage转换成Mat
     *
     * @param original 要转换的BufferedImage
     * @param imgType  bufferedImage的类型 如 BufferedImage.TYPE_3BYTE_BGR
     * @param matType  转换成mat的type 如 CvType.CV_8UC3
     */
    public static Mat bufImg2Mat(BufferedImage original, int imgType, int matType) {
        if (original == null) {
            throw new IllegalArgumentException("original == null");
        }

        // Don't convert if it already has correct type
        if (original.getType() != imgType) {

            // Create a buffered image
            BufferedImage image = new BufferedImage(original.getWidth(), original.getHeight(), imgType);

            // Draw the image onto the new buffer
            Graphics2D g = image.createGraphics();
            try {
                g.setComposite(AlphaComposite.Src);
                g.drawImage(original, 0, 0, null);
            } finally {
                g.dispose();
            }
        }

        byte[] pixels = ((DataBufferByte) original.getRaster().getDataBuffer()).getData();
        Mat mat = Mat.eye(original.getHeight(), original.getWidth(), matType);
        mat.put(0, 0, pixels);
        return mat;
    }

    /**
     * @Description: 判断图片是否被PS过
     * @Param: [picPath]
     * @return Result: java.lang.String
     * @Author: chuanyin.li
     * @Date: 2018/11/8
     */
    public static Boolean isPsFlag(FileInputStream fileInputStream) {
        boolean isPsFlag = false;
        try {
            Metadata metadata = ImageMetadataReader.readMetadata(fileInputStream);
            for (Directory directory : metadata.getDirectories()) {
                for (Tag tag : directory.getTags()) {
                    if (tag.getDescription().contains("Adobe Photoshop")) {
                        isPsFlag = true;
                    }
                }
            }
        } catch (Exception e) {
            log.error("系统异常",e);
            throw new RuntimeException(e);
        }
        return isPsFlag;
    }

    /**
     * 裁剪
     *
     * @param originalImage 原图片
     * @param region        四位 0x 1y 2宽 3高
     * @return Result
     */
    public static Mat cropImg(Mat originalImage, List<Integer> region) {
        int height = originalImage.height();
        int width = originalImage.width();

        //裁剪 高加y的偏移量要小于图片大小，否则图片尺寸不够
        if (region.get(3) + region.get(1) > height){
            region.set(3,height - region.get(1));
        }
        if (region.get(2) + region.get(0) > width){
            region.set(2,width - region.get(0));
        }
        AssertUtils.isTrue(region.get(3) + region.get(1) > height, "图片高尺寸不够");
        AssertUtils.isTrue(region.get(2) + region.get(0) > width, "图片宽尺寸不够");
        Mat cut = ImageUtils.cut(originalImage, region.get(0), region.get(1), region.get(2), region.get(3));
        return cut;
    }

    /**
     * 淡化
     *
     * @param originalImage 原图片
     * @param alpha         淡化值 0-1
     */
    public static Mat downplayImg(Mat originalImage, Double alpha) {
        // 创建和原始图片相同大小的空白图片
        Mat blurredImage = Mat.zeros(originalImage.size(), originalImage.type());
        // 淡化处理
        Core.addWeighted(originalImage, 1 - alpha, blurredImage, 1 - alpha, 0, blurredImage);
        return blurredImage;
    }

    /**
     * 亮化
     *
     * @param originalImage        原图片
     * @param brightnessPercentage 亮化程度 负值表示减小亮度，正值表示增加亮度 1为原图， 1.1为亮化10%
     */
    public static Mat brightenImg(Mat originalImage, Double brightnessPercentage) {
        brightnessPercentage = 1 + brightnessPercentage;
        // 创建和原始图片相同大小的空白图片
        Mat brightenedImage = Mat.zeros(originalImage.size(), originalImage.type());
        // 进行亮度增强操作
        Core.multiply(originalImage, new Scalar(brightnessPercentage, brightnessPercentage, brightnessPercentage), brightenedImage);
        return brightenedImage;
    }

    /**
     * 锐化
     *
     * @param originalImage        原图片
     * @param sharpeningPercentage 锐化的百分比0-1
     * @return Result
     */
    public static Mat sharpenImg(Mat originalImage, Double sharpeningPercentage) {
        // 创建和原始图片相同大小的空白图片
        Mat sharpenedImage = Mat.zeros(originalImage.size(), originalImage.type());

        // 定义锐化滤波核
        double centerWeight = 1+9*sharpeningPercentage;
        double surroundingWeight = -1 * sharpeningPercentage;
        // 定义锐化滤波核
        Mat kernel = new Mat(3, 3, CvType.CV_32F, new Scalar(surroundingWeight));
        // 设置中心位置的权重值
        kernel.put(1, 1, centerWeight);
        // 进行锐化滤波操作
        Imgproc.filter2D(originalImage, sharpenedImage, originalImage.depth(), kernel);

        return sharpenedImage;
    }

    /**
     * 去黑边 去黑边后图片会变暗
     * @param img 图片Mat对象
     * @return Result
     */
    public static Mat removeBlackEdge1(Mat img) {
        if (img.empty()) {
            return null;
        }
        Mat greyImg = img.clone();
        //1.彩色转灰色
        Imgproc.cvtColor(img, greyImg, Imgproc.COLOR_BGR2GRAY);
        Mat gaussianBlurImg = greyImg.clone();
        // 2.高斯滤波，降噪
        Imgproc.GaussianBlur(greyImg, gaussianBlurImg, new Size(3, 3), 2, 2);
        Mat cannyImg = gaussianBlurImg.clone();
        // 3.Canny边缘检测
        Imgproc.Canny(gaussianBlurImg, cannyImg, 20, 60, 3, false);
        // 4.膨胀，连接边缘
        Mat dilateImg = cannyImg.clone();
        Imgproc.dilate(cannyImg, dilateImg, new Mat(), new Point(-1, -1), 2, 1, new Scalar(1));
        //5.对边缘检测的结果图再进行轮廓提取
        List<MatOfPoint> contours = new ArrayList<>();
        List<MatOfPoint> drawContours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(dilateImg, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
        Mat linePic = Mat.zeros(dilateImg.rows(), dilateImg.cols(), CvType.CV_8UC3);
        //6.找出轮廓对应凸包的四边形拟合
        List<MatOfPoint> squares = new ArrayList<>();
        List<MatOfPoint> hulls = new ArrayList<>();
        MatOfInt hull = new MatOfInt();
        MatOfPoint2f approx = new MatOfPoint2f();
        approx.convertTo(approx, CvType.CV_32F);
        for (int i = 0; i < contours.size(); i++) {
            MatOfPoint contour = contours.get(i);
            // 边框的凸包
            Imgproc.convexHull(contour, hull);
            // 用凸包计算出新的轮廓点
            Point[] contourPoints = contour.toArray();
            int[] indices = hull.toArray();
            List<Point> newPoints = new ArrayList<>();
            for (int index : indices) {
                newPoints.add(contourPoints[index]);
            }
            MatOfPoint2f contourHull = new MatOfPoint2f();
            contourHull.fromList(newPoints);
            // 多边形拟合凸包边框(此时的拟合的精度较低)
            Imgproc.approxPolyDP(contourHull, approx, Imgproc.arcLength(contourHull, true) * 0.02, true);
            MatOfPoint mat = new MatOfPoint();
            mat.fromArray(approx.toArray());
            drawContours.add(mat);
            // 筛选出面积大于某一阈值的，且四边形的各个角度都接近直角的凸四边形
            MatOfPoint approxf1 = new MatOfPoint();
            approx.convertTo(approxf1, CvType.CV_32S);
            if (approx.rows() == 4 && Math.abs(Imgproc.contourArea(approx)) > 40000 &&
                    Imgproc.isContourConvex(approxf1)) {
                double maxCosine = 0;
                for (int j = 2; j < 5; j++) {
                    double cosine = Math.abs(getAngle(approxf1.toArray()[j % 4], approxf1.toArray()[j - 2], approxf1.toArray()[j - 1]));
                    maxCosine = Math.max(maxCosine, cosine);
                }
                // 角度大概72度
                if (maxCosine < 0.3) {
                    MatOfPoint tmp = new MatOfPoint();
                    contourHull.convertTo(tmp, CvType.CV_32S);
                    squares.add(approxf1);
                    hulls.add(tmp);
                }
            }
        }
        //这里是把提取出来的轮廓通过不同颜色的线描述出来，具体效果可以自己去看
        Random r = new Random();
        for (int i = 0; i < drawContours.size(); i++) {
            Imgproc.drawContours(linePic, drawContours, i, new Scalar(r.nextInt(255), r.nextInt(255), r.nextInt(255)));
        }
        //7.找出最大的矩形
        int index = findLargestSquare(squares);
        MatOfPoint largestSquare = null;
        if (squares != null && squares.size() > 0) {
            largestSquare = squares.get(index);
        } else {
            //返回原图
            return img;
        }
        Mat polyPic = Mat.zeros(img.size(), CvType.CV_8UC3);
        Imgproc.drawContours(polyPic, squares, index, new Scalar(0, 0, 255), 2);
        //存储矩形的四个凸点
        hull = new MatOfInt();
        Imgproc.convexHull(largestSquare, hull, false);
        List<Integer> hullList = hull.toList();
        List<Point> polyContoursList = largestSquare.toList();
        List<Point> hullPointList = new ArrayList<>();
        List<Point> lastHullPointList = new ArrayList<>();
        for (int i = 0; i < hullList.size(); i++) {
            Imgproc.circle(polyPic, polyContoursList.get(hullList.get(i)), 10, new Scalar(r.nextInt(255), r.nextInt(255), r.nextInt(255), 3));
            hullPointList.add(polyContoursList.get(hullList.get(i)));
        }
        Core.addWeighted(polyPic, 0.5, img, 0.5, 0, img);
        for (int i = 0; i < hullPointList.size(); i++) {
            lastHullPointList.add(hullPointList.get(i));
        }
        //dstPoints储存的是变换后各点的坐标，依次为左上，右上，右下， 左下
        //srcPoints储存的是上面得到的四个角的坐标
        Point[] dstPoints = {new Point(0, 0), new Point(img.cols(), 0), new Point(img.cols(), img.rows()), new Point(0, img.rows())};
        Point[] srcPoints = new Point[4];
        boolean sorted = false;
        int n = 4;
        //对四个点进行排序 分出左上 右上 右下 左下
        while (!sorted && n > 0) {
            for (int i = 1; i < n; i++) {
                sorted = true;
                if (lastHullPointList.get(i - 1).x > lastHullPointList.get(i).x) {
                    Point tempp1 = lastHullPointList.get(i);
                    Point tempp2 = lastHullPointList.get(i - 1);
                    lastHullPointList.set(i, tempp2);
                    lastHullPointList.set(i - 1, tempp1);
                    sorted = false;
                }
            }
            n--;
        }
        //即先对四个点的x坐标进行冒泡排序分出左右，再根据两对坐标的y值比较分出上下
        if (lastHullPointList.get(0).y < lastHullPointList.get(1).y) {
            srcPoints[0] = lastHullPointList.get(0);
            srcPoints[3] = lastHullPointList.get(1);
        } else {
            srcPoints[0] = lastHullPointList.get(1);
            srcPoints[3] = lastHullPointList.get(0);
        }
        if (lastHullPointList.get(2).y < lastHullPointList.get(3).y) {
            srcPoints[1] = lastHullPointList.get(2);
            srcPoints[2] = lastHullPointList.get(3);
        } else {
            srcPoints[1] = lastHullPointList.get(3);
            srcPoints[2] = lastHullPointList.get(2);
        }
        List<Point> listSrcs = java.util.Arrays.asList(srcPoints[0], srcPoints[1], srcPoints[2], srcPoints[3]);
        Mat srcPointsMat = Converters.vector_Point_to_Mat(listSrcs, CvType.CV_32F);

        List<Point> dstSrcs = java.util.Arrays.asList(dstPoints[0], dstPoints[1], dstPoints[2], dstPoints[3]);
        Mat dstPointsMat = Converters.vector_Point_to_Mat(dstSrcs, CvType.CV_32F);
        //参数分别为输入输出图像、变换矩阵、大小。
        //坐标变换后就得到了我们要的最终图像。
        //得到变换矩阵
        Mat transMat = Imgproc.getPerspectiveTransform(srcPointsMat, dstPointsMat);
        Mat outPic = new Mat();
        Imgproc.warpPerspective(img, outPic, transMat, img.size());
        return outPic;
    }

    /**
     * 根据三个点计算中间那个点的夹角   pt1 pt0 pt2
     *
     * @param pt1
     * @param pt2
     * @param pt0
     * @return Result
     */
    private static double getAngle(Point pt1, Point pt2, Point pt0) {
        double dx1 = pt1.x - pt0.x;
        double dy1 = pt1.y - pt0.y;
        double dx2 = pt2.x - pt0.x;
        double dy2 = pt2.y - pt0.y;
        return (dx1 * dx2 + dy1 * dy2) / Math.sqrt((dx1 * dx1 + dy1 * dy1) * (dx2 * dx2 + dy2 * dy2) + 1e-10);
    }

    /**
     * 找到最大的正方形轮廓
     *
     * @param squares mat像素点集合
     * @return Result
     */
    private static int findLargestSquare(List<MatOfPoint> squares) {
        if (squares.size() == 0) {
            return -1;
        }

        int maxWidth = 0;
        int maxHeight = 0;
        int maxSquareIdx = 0;
        int currentIndex = 0;
        for (MatOfPoint square : squares) {
            Rect rectangle = Imgproc.boundingRect(square);
            if (rectangle.width >= maxWidth && rectangle.height >= maxHeight) {
                maxWidth = rectangle.width;
                maxHeight = rectangle.height;
                maxSquareIdx = currentIndex;
            }
            currentIndex++;
        }
        return maxSquareIdx;
    }

}
