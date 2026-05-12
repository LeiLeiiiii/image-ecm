
/*
 * Project: Sunyard
 *
 * File Created at 2023/9/26
 *
 * Copyright 2016 Corporation Limited. All rights reserved.
 *
 * This software is the confidential and proprietary information of Company. ("Confidential Information"). You shall not
 * disclose such Confidential Information and shall use it only in accordance with the terms of the license.
 */

import com.sunyard.framework.img.util.RemoveBackgroudUtils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @author Leo
 * @Desc
 * @date 2023/9/26 9:28
 */

public class OpencvUtils {

    public static Mat method5(Mat sourceMat) {

        // Mat linePic = Mat.zeros(sourceImg.size(), CvType.CV_8UC3);
        // Imgproc.cvtColor(sourceImg, linePic, Imgproc.COLOR_BGR2HSV);
        // Imgcodecs.imwrite("d:\\linePic.jpg", linePic);

        // 重置图片，将电脑计算的边缘和自己画的边缘重合，用于画出roi区域
        // Mat roiImg = Mat.zeros(drawContoursImg.size(), CvType.CV_8UC3);
        // for (int i = 0; i < drawContoursImg.rows(); i++){
        // for (int j = 0; j < drawContoursImg.cols(); j++){
        // double[] drawPixel =drawContoursImg.get(i,j);
        // Scalar drawColor = new Scalar(drawPixel);
        // double[] sourcePixel =drawContoursImg.get(i,j);
        // Scalar sourceColor = new Scalar(sourcePixel);
        // if ((sourceColor.val[0] == 255) && (drawColor.val[0] != 0)){
        // roiImg.setTo(drawColor);
        // }
        // }
        // }
        // Imgcodecs.imwrite("d:\\roiImg.jpg", roiImg);
        // //5、接着对这张图片进行漫水填充（OpenCV中的floodFill函数）
        // //设置种子，用于水漫填充，即找到roi区域内的任意一点黑点
        // Point seed=new Point();
        // int flag=0;
        // for(int i=0;i<roiImg.rows();i++)
        // {
        // for(int j=0;j<roiImg.cols();j++)
        // {
        // if((contour.ptr<uchar>(i)[j]==255)&&(contour.ptr<uchar>(i+5)[j]==0))
        // {
        // seed.y=5+i;
        // seed.x=j;
        // flag=1;
        // break;
        // }
        // }
        // if(flag==1)
        // break;
        // }

        // Imgcodecs.imwrite("d:\\roi_before.jpg", roiImg);
        //
        // Imgproc.floodFill(roiImg, seed, new Scalar(255,255,255), nullptr,new Scalar(20, 20, 20),new Scalar(20, 20,
        // 20));
        // Imgcodecs.imwrite("d:\\roi.jpg", roiImg);
        // //6、显示最终效果图片，底色为白色，用漫水填充的图片，如果值为0（黑色），那么就在原图上将原值改为255（白色），即为白底
        // for (int i=0;i<sourceImg.rows();i++) {
        // for(int j=0;j<sourceImg.cols();j++){
        // if(roiImg.ptr<uchar>(i)[j]==0)
        // {
        // base.ptr<uchar>(i)[sourceImg.channels()*j]=255;
        // base.ptr<uchar>(i)[sourceImg.channels()*j+1]=255;
        // base.ptr<uchar>(i)[sourceImg.channels()*j+2]=255;
        // }
        // }
        // }
        // Imgcodecs.imwrite("result.jpg",sourceImg);

        //// Mat cannyImg = thresholdImg.clone();
        //// // 3.Canny边缘检测
        //// Imgproc.Canny(thresholdImg, cannyImg, 20, 60, 3, false);
        //// Mat hierarchy = Mat.zeros(img.size(), img.type());
        // List<MatOfPoint> contours = new ArrayList<>();
        // Imgproc.findContours(cannyImg, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_NONE);
        // Imgcodecs.imwrite("d:\\hierarchy.jpg", hierarchy);
        //
        // int index = 0;
        // // 找出匹配到的最大轮廓
        // for (int i = 0; i < contours.size(); i++) {
        // MatOfPoint2f matOfPoint2f = new MatOfPoint2f(contours.get(index).toArray());
        // RotatedRect rect = Imgproc.minAreaRect(matOfPoint2f);
        // Imgproc.boxPoints(rect,contours.get(i).clone());
        //
        // }
        return null;
    }

    // 勉强可以用的去黑边方法
    public static Mat method4(Mat sourceMat) {
        Mat img = sourceMat;

        // 高斯滤波，降噪
        Mat gaussianBlurImg = new Mat();
        Imgproc.GaussianBlur(img, gaussianBlurImg, new Size(3, 3), 2, 2);
        //
        Mat kernel = new Mat();
        Mat tmp = new Mat();
        Imgproc.morphologyEx(gaussianBlurImg, tmp, Imgproc.MORPH_OPEN, kernel);
        Imgproc.morphologyEx(tmp, gaussianBlurImg, Imgproc.MORPH_CLOSE, kernel);
        // 彩色转灰色
        Mat grayImg = new Mat();
        Imgproc.cvtColor(gaussianBlurImg, grayImg, Imgproc.COLOR_BGR2GRAY);
        // Imgcodecs.imwrite("d:\\grayImg.jpg", grayImg);

        // 2、二值化
        Mat thresholdImg = new Mat();
        Imgproc.threshold(grayImg, thresholdImg, 235, 255, Imgproc.THRESH_BINARY_INV);
        // Imgcodecs.imwrite("d:\\thresholdImg.jpg", thresholdImg);

        // 寻找最外围轮廓
        Mat hierarchy = new Mat();
        List<MatOfPoint> contours = new ArrayList<>();
        Imgproc.findContours(thresholdImg, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
        // 初始化白底图
        Mat mask = Mat.zeros(img.clone().size(), CvType.CV_8UC3);
        mask.setTo(new Scalar(255, 255, 255));
        // Imgcodecs.imwrite("d:\\mask.jpg", mask);
        // 绘制最小边界矩阵
        Mat dst = img.clone();
        Point[] points = new Point[4];
        List<Mat> list = new ArrayList<>();
        List<Rect> listRect = new ArrayList<>();
        for (int i = 0; i < contours.size(); i++) {
            MatOfPoint2f matOfPoint2f = new MatOfPoint2f(contours.get(i).toArray());
            RotatedRect rects = Imgproc.minAreaRect(matOfPoint2f);
            rects.points(points);
            // 确定旋转矩阵的四个顶点
            Rect rect = rects.boundingRect();
            if (Math.round(rects.size.width) > 3 && Math.round(rects.size.height) > 3) {
                int total = 0;
                for (Point point : points) {
                    if (point.x > 5 && point.y > 5) {
                        total++;
                    } else {
                        break;
                    }
                }
                // 判断是否是4变形
                if (total == 4) {
                    // OImgproc.line(dst, points[0], points[(0 + 1) % 4], new Scalar(0, 255, 0), 2);
                    // Imgproc.line(dst, points[1], points[(1 + 1) % 4], new Scalar(0, 255, 0), 2);
                    // Imgproc.line(dst, points[2], points[(2 + 1) % 4], new Scalar(0, 255, 0), 2);
                    // Imgproc.line(dst, points[3], points[(3 + 1) % 4], new Scalar(0, 255, 0), 2);
                    // Imgcodecs.imwrite("d:\\rect" + System.currentTimeMillis() + ".jpg", dst.submat(rect));
                    list.add(dst.submat(rect).clone());
                    listRect.add(rect);
                    Mat roi = new Mat(mask, rect);
                    dst.submat(rect).clone().copyTo(roi);
                }
            }
        }
        // Imgcodecs.imwrite("d:\\mask3.jpg", mask);
        // Core.merge(list,mask );
        // Imgcodecs.imwrite("d:\\rectangle" + System.currentTimeMillis() + ".jpg", dst);
        return mask;
    }

    // 绘制轮廓直线
    public static Mat method3(Mat sourceMat) {
        Mat img = sourceMat;

        // 高斯滤波，降噪
        Mat gaussianBlurImg = new Mat();
        Imgproc.GaussianBlur(img, gaussianBlurImg, new Size(3, 3), 2, 2);
        //
        Mat kernel = new Mat();
        Mat tmp = new Mat();
        Imgproc.morphologyEx(gaussianBlurImg, tmp, Imgproc.MORPH_OPEN, kernel);
        Imgproc.morphologyEx(tmp, gaussianBlurImg, Imgproc.MORPH_CLOSE, kernel);
        // 彩色转灰色
        Mat grayImg = new Mat();
        Imgproc.cvtColor(gaussianBlurImg, grayImg, Imgproc.COLOR_BGR2GRAY);
        Imgcodecs.imwrite("d:\\grayImg.jpg", grayImg);

        // 2、二值化
        Mat thresholdImg = new Mat();
        Imgproc.threshold(grayImg, thresholdImg, 235, 255, Imgproc.THRESH_BINARY_INV);
        Imgcodecs.imwrite("d:\\thresholdImg.jpg", thresholdImg);

        // 寻找最外围轮廓
        Mat hierarchy = new Mat();
        List<MatOfPoint> contours = new ArrayList<>();
        Imgproc.findContours(thresholdImg, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
        // 绘制最小边界矩阵
        Mat dst = img.clone();
        Point[] pts = new Point[4];
        Random r = new Random();
        for (int i = 0; i < contours.size(); i++) {
            MatOfPoint2f matOfPoint2f = new MatOfPoint2f(contours.get(i).toArray());
            RotatedRect rects = Imgproc.minAreaRect(matOfPoint2f);
            rects.points(pts);// 确定旋转矩阵的四个顶点
            if (Math.round(rects.size.width) > 5 && Math.round(rects.size.height) > 5) {
                for (int j = 0; j < 4; j++) {
                    if (pts[j].x < 10 || pts[j].y < 10) {
                        break;
                    }
                    if ((pts[j].x > img.width() - 10 && pts[j].x <= img.width())
                        || (pts[j].y > img.height() - 10 && pts[j].y <= img.height())) {
                        break;
                    }
                    Imgproc.line(dst, pts[j], pts[(j + 1) % 4], new Scalar(0, 255, 0), 2);
                }
            }
        }
        Imgcodecs.imwrite("d:\\" + System.currentTimeMillis() + ".jpg", dst);
        return grayImg;
    }

    // 纯抠图
    public static Mat method22(Mat sourceMat) {
        Mat img = sourceMat;
        // 高斯滤波，降噪
        Mat gaussianBlurImg = new Mat();
        Imgproc.GaussianBlur(img, gaussianBlurImg, new Size(3, 3), 2, 2);
        //
        Mat kernel = new Mat();
        Mat tmp = new Mat();
        Imgproc.morphologyEx(gaussianBlurImg, tmp, Imgproc.MORPH_OPEN, kernel);
        Imgproc.morphologyEx(tmp, gaussianBlurImg, Imgproc.MORPH_CLOSE, kernel);
        // 彩色转灰色
        Mat grayImg = new Mat();
        Imgproc.cvtColor(gaussianBlurImg, grayImg, Imgproc.COLOR_BGR2GRAY);
        Imgcodecs.imwrite("d:\\grayImg.jpg", grayImg);

        // 2、二值化
        Mat thresholdImg = new Mat();
        Imgproc.threshold(grayImg, thresholdImg, 240, 255, Imgproc.THRESH_BINARY);
        Imgcodecs.imwrite("d:\\thresholdImg.jpg", thresholdImg);
        // 抠图用
        // 2、二值化取反
        Mat maskInvImg = new Mat(img.size(), img.type(), new Scalar(0, 0, 0));
        Core.bitwise_not(thresholdImg, maskInvImg);
        Imgcodecs.imwrite("d:\\maskInvImg.jpg", maskInvImg);
        // 提取
        Mat img2_fg = new Mat(img.size(), img.type(), new Scalar(0, 0, 255));
        Core.bitwise_and(img, img, img2_fg, maskInvImg);
        Imgcodecs.imwrite("d:\\img2_fg.jpg", img2_fg);

        return null;
    }

    /**
     * 绘制了所有边缘检测小方块的轮廓 1、边缘检测(彩色转灰色 + 高斯滤波，降噪 + Canny边缘检测 + 膨胀，连接边缘) 2、获取轮廓 3、绘制轮廓
     */
    public static Mat method2(Mat sourceMat) {
        // 1、边缘检测(彩色转灰色 + 高斯滤波，降噪 + Canny边缘检测 + 膨胀，连接边缘)
        Mat cannyImg = RemoveBackgroudUtils.getMat(sourceMat);
        // 2、获取轮廓
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(cannyImg, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
        // 3、找出轮廓对应凸包的四边形拟合
        List<MatOfPoint> squares = getMatOfPoints(contours);
        // 4、绘制轮廓、这里是把提取出来的轮廓通过不同颜色的线描述出来，具体效果可以自己去看
        Random r = new Random();
        Mat linePic = Mat.zeros(cannyImg.rows(), cannyImg.cols(), CvType.CV_8UC3);
        for (int i = 0; i < squares.size(); i++) {
            Mat outPic = linePic.clone();
            Imgproc.drawContours(outPic, squares, i, new Scalar(r.nextInt(255), r.nextInt(255), r.nextInt(255)), 4);
            // 分轮廓出图
            Imgcodecs.imwrite("d:\\linePic" + i + ".jpg", outPic);
        }
        return linePic;
    }

    /**
     *
     * 1、边缘检测(彩色转灰色 + 高斯滤波，降噪 + Canny边缘检测 + 膨胀，连接边缘) 2、获取轮廓 3、绘制轮廓
     */
    public static Mat method1(Mat sourceMat) {
        // 1、边缘检测(彩色转灰色 + 高斯滤波，降噪 + Canny边缘检测 + 膨胀，连接边缘)
        Mat cannyImg = RemoveBackgroudUtils.getMat(sourceMat);
        // 2、获取轮廓
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(cannyImg, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
        // 3、绘制轮廓
        Mat linePic = Mat.zeros(cannyImg.rows(), cannyImg.cols(), CvType.CV_8UC3);
        for (int i = 0; i < contours.size(); i++) {
            Imgproc.drawContours(linePic, contours, i, new Scalar(255, 255, 255), 4);
        }
        return linePic;
    }

    /**
     *
     * @param contours
     * @return Result
     */
    public static List<MatOfPoint> getMatOfPoints(List<MatOfPoint> contours) {

        List<MatOfPoint> oldsquares = new ArrayList<>();
        List<MatOfPoint> oldhulls = new ArrayList<>();
        MatOfInt oldhull = new MatOfInt();
        MatOfPoint2f oldapprox = new MatOfPoint2f();
        oldapprox.convertTo(oldapprox, CvType.CV_32F);
        List<MatOfPoint> oldContours = new ArrayList<>();

        for (int i = 0; i < contours.size(); i++) {
            MatOfPoint contour = contours.get(i);
            // 边框的凸包
            Imgproc.convexHull(contour, oldhull);
            // 用凸包计算出新的轮廓点
            Point[] contourPoints = contour.toArray();
            int[] indices = oldhull.toArray();
            List<Point> newPoints = new ArrayList<>();
            for (int index : indices) {
                newPoints.add(contourPoints[index]);
            }
            MatOfPoint2f contourHull = new MatOfPoint2f();
            contourHull.fromList(newPoints);
            // 多边形拟合凸包边框(此时的拟合的精度较低)
            Imgproc.approxPolyDP(contourHull, oldapprox, Imgproc.arcLength(contourHull, true) * 0.000001, true);
            MatOfPoint mat = new MatOfPoint();
            mat.fromArray(oldapprox.toArray());
            oldContours.add(mat);
            // 筛选出面积大于某一阈值的，且四边形的各个角度都接近直角的凸四边形
            MatOfPoint approxf1 = new MatOfPoint();
            oldapprox.convertTo(approxf1, CvType.CV_32S);
            if (oldapprox.rows() == 4 && Math.abs(Imgproc.contourArea(oldapprox)) > 40000
                && Imgproc.isContourConvex(approxf1)) {
                double maxCosine = 0;
                for (int j = 2; j < 5; j++) {
                    double cosine = Math.abs(OpencvUtils.getAngle(approxf1.toArray()[j % 4], approxf1.toArray()[j - 2],
                        approxf1.toArray()[j - 1]));
                    maxCosine = Math.max(maxCosine, cosine);
                }
                // 角度大概72度
                if (maxCosine < 0.3) {
                    MatOfPoint tmp = new MatOfPoint();
                    contourHull.convertTo(tmp, CvType.CV_32S);
                    oldsquares.add(approxf1);
                    oldhulls.add(tmp);
                }
            }

        }
        return oldContours;
    }

    // 根据三个点计算中间那个点的夹角 pt1 pt0 pt2
    public static double getAngle(Point pt1, Point pt2, Point pt0) {
        double dx1 = pt1.x - pt0.x;
        double dy1 = pt1.y - pt0.y;
        double dx2 = pt2.x - pt0.x;
        double dy2 = pt2.y - pt0.y;
        return (dx1 * dx2 + dy1 * dy2) / Math.sqrt((dx1 * dx1 + dy1 * dy1) * (dx2 * dx2 + dy2 * dy2) + 1e-10);
    }
}
/**
 * Revision history ------------------------------------------------------------------------- Date Author Note
 * ------------------------------------------------------------------------- 2023/9/26 Leo creat
 */