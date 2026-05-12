package com.sunyard.framework.img.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

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
import org.opencv.imgproc.Imgproc;
import org.opencv.utils.Converters;

import lombok.extern.slf4j.Slf4j;


/**
 * 去黑边工具类
 * @author PJW
 */
@Slf4j
public class RemoveBackgroudUtils {

    /**
     * 重新调优后的去黑边方法，没有广泛测试，可在测试目录下的opencvutils 下测试
     * @param img Mat
     * @return
     */
    public static Mat removeBlackEdge(Mat img) {
        // 这个必须配置，否则会报错
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        if (img.empty()) {
            return img;
        }

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

        // 2、二值化
        Mat thresholdImg = new Mat();
        Imgproc.threshold(grayImg, thresholdImg, 235, 255, Imgproc.THRESH_BINARY_INV);

        // 寻找最外围轮廓
        Mat hierarchy = new Mat();
        List<MatOfPoint> contours = new ArrayList<>();
        Imgproc.findContours(thresholdImg, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
        // 初始化白底图
        Mat mask = Mat.zeros(img.clone().size(), CvType.CV_8UC3);
        mask.setTo(new Scalar(255, 255, 255));
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
                    list.add(dst.submat(rect).clone());
                    listRect.add(rect);
                    Mat roi = new Mat(mask, rect);
                    dst.submat(rect).clone().copyTo(roi);
                }
            }
        }
        return mask;
    }

    /**
     * 去黑边
     * @param img Mat
     * @return Result
     */
    public static Mat removeBlackEdgeTwo(Mat img) {
        // 加载OpenCV库
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        if (img.empty()) {
            return img;
        }

        // 高斯滤波
        Mat gaussianBlurImg = new Mat();
        Imgproc.GaussianBlur(img, gaussianBlurImg, new Size(3, 3), 2, 2);

        // 转为灰度、二值化
        Mat grayImg = new Mat();
        Imgproc.cvtColor(gaussianBlurImg, grayImg, Imgproc.COLOR_BGR2GRAY);
        Mat thresholdImg = new Mat();
        Imgproc.threshold(grayImg, thresholdImg, 100, 255, Imgproc.THRESH_BINARY_INV);

        // 形态学闭运算，消除内部黑点
        Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(3, 3));
        Imgproc.morphologyEx(thresholdImg, thresholdImg, Imgproc.MORPH_CLOSE, kernel);

        // 寻找最外围轮廓
        Mat hierarchy = new Mat();
        List<MatOfPoint> contours = new ArrayList<>();
        Imgproc.findContours(thresholdImg, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        // 原图上填充非黑边区域
        for (MatOfPoint contour : contours) {
            Rect rect = Imgproc.boundingRect(contour);
            if (rect.width > 20 && rect.height > 20) {
                // 设定MIN_SIZE
                // 在原图img上填充白色
                Imgproc.drawContours(img, Arrays.asList(contour), -1, new Scalar(255, 255, 255), -1);
            }
        }
        // 返回处理后去除了黑边的原图
        return img;
    }

    /**
     *
     * @param img Mat
     * @return Result
     * @author 饶昌妹
     */
    @Deprecated
    public static Mat removeBlackEdge2(Mat img) {
        //这个必须配置，否则会报错
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        if(img.empty()){
            return img;
        }
        Mat dilateImg = getMat(img);

        //5.对边缘检测的结果图再进行轮廓提取
        List<MatOfPoint> contours = new ArrayList<>();
        List<MatOfPoint> drawContours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(dilateImg, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
        Mat linePic = Mat.zeros(dilateImg.rows(), dilateImg.cols(), CvType.CV_8UC3);

        //6.找出轮廓对应凸包的四边形拟合
        List<MatOfPoint> squares = getMatOfPoints(contours, drawContours);
        MatOfInt hull;
        //这里是把提取出来的轮廓通过不同颜色的线描述出来，具体效果可以自己去看
        Random r = new Random();
        for (int i = 0; i < drawContours.size(); i++) {
            Imgproc.drawContours(linePic, drawContours, i, new Scalar(r.nextInt(255),r.nextInt(255), r.nextInt(255)));
        }
//        createImage(linePic, "D:\\orc\\drawContours1.jpg");
        //7.找出最大的矩形
        int index = findLargestSquare(squares);
        MatOfPoint largestSquare = null;
        if(squares != null && squares.size() > 0){
            largestSquare = squares.get(index);
        }else{
            log.debug("图片无法识别");
            return img;
        }
        Mat polyPic = Mat.zeros(img.size(), CvType.CV_8UC3);
        Imgproc.drawContours(polyPic, squares, index, new Scalar(0, 0,0), 0);
        List<Point> lastHullPointList = getPoints(img, r, largestSquare, polyPic);
        //dstPoints储存的是变换后各点的坐标，依次为左上，右上，右下， 左下
        //srcPoints储存的是上面得到的四个角的坐标
        Point[] dstPoints = {new Point(0,0), new Point(img.cols(),0), new Point(img.cols(),img.rows()), new Point(0,img.rows())};
        Point[] srcPoints = new Point[4];
        boolean sorted = false;
        int n = 4;
        //对四个点进行排序 分出左上 右上 右下 左下
        while (!sorted && n > 0){
            for (int i = 1; i < n; i++){
                sorted = true;
                if (lastHullPointList.get(i-1).x > lastHullPointList.get(i).x){
                    Point tempp1 = lastHullPointList.get(i);
                    Point tempp2 = lastHullPointList.get(i-1);
                    lastHullPointList.set(i, tempp2);
                    lastHullPointList.set(i-1, tempp1);
                    sorted = false;
                }
            }
            n--;
        }
        Mat outPic = getMat(img, lastHullPointList, dstPoints, srcPoints);
        return outPic;
    }

    /**
     *
     * 获取变换后得矩阵
     * @param img Mat
     * @param lastHullPointList lastHullPointList
     * @param dstPoints dstPoints
     * @param srcPoints srcPoints
     * @return Result
     */
    private static Mat getMat(Mat img, List<Point> lastHullPointList, Point[] dstPoints, Point[] srcPoints) {
        //即先对四个点的x坐标进行冒泡排序分出左右，再根据两对坐标的y值比较分出上下
        if (lastHullPointList.get(0).y < lastHullPointList.get(1).y){
            srcPoints[0] = lastHullPointList.get(0);
            srcPoints[3] = lastHullPointList.get(1);
        }else{
            srcPoints[0] = lastHullPointList.get(1);
            srcPoints[3] = lastHullPointList.get(0);
        }
        if (lastHullPointList.get(2).y < lastHullPointList.get(3).y){
            srcPoints[1] = lastHullPointList.get(2);
            srcPoints[2] = lastHullPointList.get(3);
        }else{
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
     * 获取特征点
     * @param img Mat
     * @param r r
     * @param largestSquare largestSquare
     * @param polyPic polyPic
     * @return Result
     */
    private static List<Point> getPoints(Mat img, Random r, MatOfPoint largestSquare, Mat polyPic) {
        MatOfInt hull;
        //存储矩形的四个凸点
        hull = new MatOfInt();
        Imgproc.convexHull(largestSquare, hull, false);
        List<Integer> hullList =  hull.toList();
        List<Point> polyContoursList = largestSquare.toList();
        List<Point> hullPointList = new ArrayList<>();
        List<Point> lastHullPointList = new ArrayList<>();
        for(int i = 0; i < hullList.size();i++){
            Imgproc.circle(polyPic, polyContoursList.get(hullList.get(i)), 10, new Scalar(r.nextInt(255), r.nextInt(255), r.nextInt(255), 3));
            hullPointList.add(polyContoursList.get(hullList.get(i)));
        }
        Core.addWeighted(polyPic, 0.5, img, 0.5, 0, img);
        for(int i = 0; i < hullPointList.size(); i++){
            lastHullPointList.add(hullPointList.get(i));
        }
        return lastHullPointList;
    }

    /**
     * 获取含有特征的mat对象
     * @param contours contours
     * @param drawContours drawContours
     * @return Result
     */
    private static List<MatOfPoint> getMatOfPoints(List<MatOfPoint> contours, List<MatOfPoint> drawContours) {
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
            Imgproc.approxPolyDP(contourHull, approx, Imgproc.arcLength(contourHull, true)*0.02, true);
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
                    double cosine = Math.abs(getAngle(approxf1.toArray()[j%4], approxf1.toArray()[j-2], approxf1.toArray()[j-1]));
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
        return squares;
    }

    /**
     * 获取mat对象
     * @param img Mat
     * @return Result
     */
    public static Mat getMat(Mat img) {
        Mat greyImg = img.clone();
        //1.彩色转灰色
        Imgproc.cvtColor(img, greyImg, Imgproc.COLOR_BGR2GRAY);

        Mat gaussianBlurImg = greyImg.clone();
        // 2.高斯滤波，降噪
        Imgproc.GaussianBlur(greyImg, gaussianBlurImg, new Size(3,3),2,2);

        Mat cannyImg = gaussianBlurImg.clone();
        // 3.Canny边缘检测
        Imgproc.Canny(gaussianBlurImg, cannyImg, 20, 60, 3, false);
        // 4.膨胀，连接边缘
        Mat dilateImg = cannyImg.clone();
        Imgproc.dilate(cannyImg, dilateImg, new Mat(), new Point(-1, -1), 2, 1, new Scalar(1));
        return dilateImg;
    }

    /**
     * getAngle
     * @param pt1 pt1
     * @param pt2 pt2
     * @param pt0 pt0
     * @return double
     */
    private static double getAngle(Point pt1, Point pt2, Point pt0) {
        double dx1 = pt1.x - pt0.x;
        double dy1 = pt1.y - pt0.y;
        double dx2 = pt2.x - pt0.x;
        double dy2 = pt2.y - pt0.y;
        return (dx1*dx2 + dy1*dy2)/Math.sqrt((dx1*dx1 + dy1*dy1)*(dx2*dx2 + dy2*dy2) + 1e-10);
    }

    /**
     * 找到最大的正方形轮廓
     * @param squares squares
     * @return int
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

