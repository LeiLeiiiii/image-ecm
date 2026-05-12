package com.sunyard.framework.img.util;

import com.alibaba.excel.util.FileUtils;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.filters.Canvas;
import net.coobird.thumbnailator.geometry.Positions;
import org.apache.commons.collections4.CollectionUtils;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 图片自动矫正工具类
 *
 * @author lqt
 * @version 1.0
 * @date 2023/3/24 9:48
 */
@Slf4j
public class RectifyImageUtils {
    /**
     * 获取倾斜角度，只能矫正小角度倾斜，大于90度没法判断文字朝向
     *
     * @param cannyMat Mat对象
     * @return Result
     * @author lyzhou2
     * @date 2022/3/4 15:05
     */
    public static Map getAngle(Mat cannyMat) {
        Mat lines = new Mat();
        //累加器阈值参数，小于设置值不返回（检测一条直线所需的最少曲线交点）
        int threshold = 100;
        //最低线段长度，低于设置值则不返回（能组成之间的最少的点数）
        double minLineLength = 130;
        //间距小于该值的线当成同一条线
        double maxLineGap = 10;
        Imgproc.HoughLinesP(cannyMat, lines, 1, Math.PI / 180, threshold, minLineLength, maxLineGap);
//        //倾斜角度
        double angle = 0;
        //所有线倾斜角度之和
        double totalAngle = 0;
        for (int i = 0; i < lines.rows(); i++) {
            double[] line = lines.get(i, 0);
            //计算每条线弧度
            //这个计算是给Graphics2D旋转使用
            double radian = Math.atan((line[3] - line[1]) * (-1.0) / (line[2] - line[0]));
            //计算每条线的倾斜角度
            double lineAngle = 360 * radian / (2 * Math.PI);

            //表格类图片要过滤掉竖线，不然取得角度会有问题
            if (Math.abs(lineAngle) > 45) {
                lineAngle = 90 - lineAngle;
            }
            totalAngle += lineAngle;
        }

        //取角度平均数
        angle = totalAngle / lines.rows();
        Map<String,Object> map1 = new HashMap<>(6);
        map1.put("angle", angle);
        map1.put("num", lines.rows());
        return map1;
    }

    /**
     * 求最大值
     * @param list list
     * @return int
     */
    public static int getMax(List<Integer> list) {
        int max = 0;
        for (int i = 0; i < list.size(); i++) {
            if (max < list.get(i)) {
                max = list.get(i);
            }
        }
        return max;
    }

    /**
     * 求每一个属性的众数
     * @param array array
     * @return Double
     */
    public static List<Double> getModeIris(List<Double> array) {
        Map<Double, Integer> map = new HashMap<>(6);
        Set<Map.Entry<Double, Integer>> set = map.entrySet();
        List<Integer> list = new ArrayList<>();
        List<Double> listMode = new ArrayList<>();
        //统计元素出现的次数，存入Map集合
        for (double item : array) {
            if (!map.containsKey(item)) {
                map.put(item, 1);
            } else {
                map.put(item, map.get(item) + 1);
            }
        }
        //将出现的次数存入List集合
        for (Map.Entry<Double, Integer> entry : set) {
            list.add(entry.getValue());
        }
        //得到最大值
        int max = getMax(list);
        //根据最大值获取众数
        for (Map.Entry<Double, Integer> entry : set) {
            if (entry.getValue() == max) {
                listMode.add(entry.getKey());
            }
        }
        return listMode;
    }

    /**
     * 偏离矫正
     *
     * @param srcImage Mat对象
     * @param byteArray byteArray
     * @return Result
     */
    public static Mat imgCorrection(Mat srcImage, byte[] byteArray) {
        Mat binary = canny(srcImage);
        //计算倾斜角度

        // 旋转矩形
        // 获取最大矩形
        //计算倾斜角度
        Map angle1 = getAngle(binary);

        double angle = (Double) angle1.get("angle");
        int num = (Integer) angle1.get("num");

        if (num==0){
            return srcImage;
        }

        Integer max = srcImage.height();
        if (srcImage.width() > srcImage.height()) {
            max = srcImage.width();
        }
        // 获取最大矩形
        RotatedRect rect = findMaxRect(binary);
        if (max / num > 10) {
            angle = -rect.angle;
        }

        //偏离角度过小，不旋转
        if(angle>-1.5&&angle<1.5){
            return srcImage;
        }
        // 旋转矩形
        int i = new BigDecimal(angle).intValue();

        //放大倍数
//        double v = 1 + angle * 0.01;
        // 图片对象
        File file1 = new File(System.currentTimeMillis() + ".jpg");
        Mat nativeCorrectImg = null;
        //旋转
        try {
            BufferedImage sourceImage = ImageIO.read(new ByteArrayInputStream(byteArray));
            Thumbnails.of(sourceImage).scale(1.0).rotate(angle)
                    .toFile(file1);

            BufferedImage image = ImageIO.read(file1);
            Thumbnails.of(sourceImage).scale(1.0).rotate(angle)
                    .addFilter(new Canvas(image.getWidth(), image.getHeight(), Positions.CENTER, Color.WHITE))
                    .toFile(file1);

            byte[] bytes = FileUtils.readFileToByteArray(file1);
            nativeCorrectImg = Imgcodecs.imdecode(new MatOfByte(bytes), Imgcodecs.IMREAD_COLOR);
            //删除缓存数据
            FileUtils.delete(file1);
        } catch (IOException e) {
            log.error("系统异常",e);
            throw new RuntimeException(e);
        }

        return nativeCorrectImg;
    }


    /**
     * 把矫正后的图像切割出来
     *
     * @param correctMat 图像矫正后的Mat矩阵
     * @param nativeCorrectMat 图像矫正后的Mat矩阵
     */
    public static void cutRect(Mat correctMat, Mat nativeCorrectMat) {
        // 获取最大矩形
        RotatedRect rect = findMaxRect(correctMat);

        Point[] rectPoint = new Point[4];
        rect.points(rectPoint);

        int startLeft = (int) Math.abs(rectPoint[0].x);
        int startUp = (int) Math.abs(rectPoint[0].y < rectPoint[1].y ? rectPoint[0].y : rectPoint[1].y);
        int width = (int) Math.abs(rectPoint[2].x - rectPoint[0].x);
        int height = (int) Math.abs(rectPoint[1].y - rectPoint[0].y);

        Mat temp = new Mat(nativeCorrectMat, new Rect(startLeft, startUp, width, height));
        Mat t = new Mat();
        temp.copyTo(t);

    }


    /**
     * 根据坐标点，切割文件
     * @param correctMat 图像矫正后的Mat矩阵
     * @param region region
     */
    public static Mat cutRect(Mat correctMat, List<Integer> region) {
        if (!CollectionUtils.isEmpty(region) && region.size() == 4) {
            // 获取最大矩形
            Rect rect1 = new Rect(region.get(0), region.get(1), region.get(2), region.get(3));
            //图片裁剪
            if (rect1.x < 0) {
                rect1.x = 0;
            }
            if (rect1.y < 0) {
                rect1.y = 0;
            }
            if (rect1.x + rect1.width > correctMat.cols()) {
                rect1.width = correctMat.cols() - rect1.x;
            }
            if (rect1.y + rect1.height > correctMat.rows()) {
                rect1.height = correctMat.rows() - rect1.y;
            }

            Mat srcRoi = new Mat(correctMat, rect1);
            Mat cutImage = new Mat();
            srcRoi.copyTo(cutImage);
            return cutImage;
        } else {
            return null;
        }
    }

    /**
     * 旋转矩形
     *
     * @param cannyMat 矩形
     * @param rect 矩形
     * @return Result
     */
    public static Mat rotation(Mat cannyMat, RotatedRect rect) {
        // 获取矩形的四个顶点
        Point[] rectPoint = new Point[4];
        rect.points(rectPoint);

        double angle = rect.angle;

        Point center = rect.center;

        Mat correctImg = new Mat(cannyMat.size(), cannyMat.type());

        cannyMat.copyTo(correctImg);

        // 得到旋转矩阵算子
        Mat matrix = Imgproc.getRotationMatrix2D(center, angle, 0.8);

        Imgproc.warpAffine(correctImg, correctImg, matrix, correctImg.size(), 1, 0, new Scalar(0, 0, 0));

        return correctImg;
    }

    /**
     * 返回边缘检测之后的最大矩形,并返回
     *
     * @param cannyMat Canny之后的mat矩阵
     * @return Result
     */
    public static RotatedRect findMaxRect(Mat cannyMat) {

        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Mat hierarchy = new Mat();

        // 寻找轮廓
        Imgproc.findContours(cannyMat, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_NONE,
                new Point(0, 0));

        // 找出匹配到的最大轮廓
        double area = Imgproc.boundingRect(contours.get(0)).area();
        int index = 0;

        // 找出匹配到的最大轮廓
        for (int i = 0; i < contours.size(); i++) {
            double tempArea = Imgproc.boundingRect(contours.get(i)).area();
            if (tempArea > area) {
                area = tempArea;
                index = i;
            }
        }

        MatOfPoint2f matOfPoint2f = new MatOfPoint2f(contours.get(index).toArray());
        RotatedRect rect = Imgproc.minAreaRect(matOfPoint2f);
        return rect;
    }

    /**
     * canny算法，边缘检测
     *
     * @param src mat对象
     * @return Result
     */
    public static Mat canny(Mat src) {
        Mat mat = src.clone();
        Imgproc.Canny(src, mat, 60, 200);
        return mat;
    }

}
