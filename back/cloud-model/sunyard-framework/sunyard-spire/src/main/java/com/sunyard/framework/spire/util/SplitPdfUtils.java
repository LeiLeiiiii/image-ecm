package com.sunyard.framework.spire.util;
/*
 * Project: Sunyard
 *
 * File Created at 2023/7/12
 *
 * Copyright 2016 Corporation Limited. All rights reserved.
 *
 * This software is the confidential and proprietary information of Company. ("Confidential Information"). You shall not
 * disclose such Confidential Information and shall use it only in accordance with the terms of the license.
 */

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.imageio.ImageIO;

import org.springframework.util.CollectionUtils;

import com.spire.pdf.PdfDocument;
import com.spire.pdf.graphics.PdfImageType;
import com.sunyard.framework.spire.dto.PdfSplitDTO;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Leo
 * @Desc
 * @date 2023/7/12 9:22
 */
@Slf4j
public class SplitPdfUtils {
    public static final ExecutorService executor = new ThreadPoolExecutor(3, 3, 0L,
            TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(), new ThreadFactory() {
        private final AtomicInteger threadNumber = new AtomicInteger(1);

        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, "MyThreadPool-" + threadNumber.getAndIncrement());
        }
    });

    /**
     * 一个pdf拆分成多个pdf
     *
     * @param inputStream     文件输入流
     * @param pdfSplitDTOList 分割obj
     * @param outputStream    输出流
     * @return Result
     */
    public static List<OutputStream> splitPdfByPageRange(InputStream inputStream, List<PdfSplitDTO> pdfSplitDTOList,
                                                         OutputStream outputStream) { // 指定输入文件路径
        if (CollectionUtils.isEmpty(pdfSplitDTOList)) {
            return null;
        }
        // 创建两个额外的 PdfDocument对象
        PdfDocument sourceDoc = new PdfDocument(inputStream);
        List<OutputStream> outList = new ArrayList<>();
        int i = 0;
        for (PdfSplitDTO pdfSplitDTO : pdfSplitDTOList) {
            OutputStream byteArrayOutputStream = null;
            if (pdfSplitDTOList.size() == 1) {
                byteArrayOutputStream = outputStream;
            } else {
                byteArrayOutputStream = new ByteArrayOutputStream();
            }
            PdfDocument newDoc = new PdfDocument();
            if (pdfSplitDTO.getEnd() != null) {
                newDoc.insertPageRange(sourceDoc, pdfSplitDTO.getStart(), pdfSplitDTO.getEnd());
            } else {
                newDoc.insertPage(sourceDoc, pdfSplitDTO.getStart());
            }
            newDoc.saveToStream(byteArrayOutputStream);
            newDoc.saveToFile("PDF_" + i++ + ".pdf");
            outList.add(byteArrayOutputStream);
        }
        sourceDoc.close();
        return outList;
    }

    /**
     * 一个pdf拆分成多张图片
     *
     * @param inputStream  输入流
     * @param outputStream 输出流
     * @return Result
     */
    public static List<OutputStream> splitPdfByImgRange(InputStream inputStream, OutputStream outputStream) {
        // 指定输入文件路径
        // 创建两个额外的 PdfDocument对象
        PdfDocument sourceDoc = new PdfDocument(inputStream);
        List<OutputStream> outList = new ArrayList<>();
        // 遍历PDF每一页，保存为图片
        for (int i = 0; i < sourceDoc.getPages().getCount(); i++) {
            OutputStream byteArrayOutputStream = null;
            if (sourceDoc.getPages().getCount() == 1) {
                byteArrayOutputStream = outputStream;
            } else {
                byteArrayOutputStream = new ByteArrayOutputStream();
            }
            // 将页面保存为图片，并设置DPI分辨率
            BufferedImage image = sourceDoc.saveAsImage(i, PdfImageType.Bitmap, 500, 500);
            // 将图片保存为png格式
            try {
                ImageIO.write(image, "PNG", byteArrayOutputStream);
                ImageIO.write(image, "PNG", new File("new_" + i + ".PNG"));
                outList.add(byteArrayOutputStream);
            } catch (IOException e) {
                log.error("系统异常", e);
                throw new RuntimeException(e);
            }
        }
        sourceDoc.close();
        return outList;
    }

    /**
     * 一个pdf拆分成多张图片
     *
     * @param inputStream 输出流
     * @return Result
     */
    public static List<ByteArrayOutputStream> splitPdfByImgRange(InputStream inputStream) {
        List<ByteArrayOutputStream> outList = new ArrayList<>();
        // 创建两个额外的 PdfDocument对象
        PdfDocument sourceDoc = new PdfDocument(inputStream);
        // 遍历PDF每一页，保存为图片
        for (int i = 0; i < sourceDoc.getPages().getCount(); i++) {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            // 将页面保存为图片，并设置DPI分辨率
            BufferedImage image = sourceDoc.saveAsImage(i, PdfImageType.Bitmap, 72, 96);
            // 将图片保存为png格式
            try {
                String s = "png";
                ImageIO.write(image, s, byteArrayOutputStream);
                outList.add(byteArrayOutputStream);
            } catch (IOException e) {
                log.error("系统异常",e);
                throw new RuntimeException(e);
            }
        }
        sourceDoc.close();
        return outList;
    }

    /**
     * 多线程拆分pdf
     */
    public static ArrayList<String> convertPdfToImagesParallel(byte[] pdfBytes, Integer splitPageSize, Integer splitPageNum, int totalPage, String filename, String prefix) throws Exception {
        // SortedMap线程安全
        ConcurrentSkipListMap<Integer, String> orderedResults = new ConcurrentSkipListMap<>();
        // 确保输出目录存在
        File outputDir = new File(prefix);
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }
        List<Future<?>> futures = new ArrayList<>();
        int startNum = splitPageSize * splitPageNum;
        for (int page = startNum; page < totalPage && page < startNum + splitPageSize; page++) {
            final int currentPage = page;

            futures.add(executor.submit(() -> {
                // 每个线程独立加载PDF（确保线程安全）
                PdfDocument sourceDoc1 = new PdfDocument(new ByteArrayInputStream(pdfBytes));
                try {
                    String newFilename =  filename + "_" + String.format("%d", currentPage) + ".png"; // 文件命名规则
                    String fullPath = prefix + newFilename;
                    //判断文件是否存在
                    File outputFile = new File(fullPath);
                    if (outputFile.exists()) {
                        log.info("图片已存在，跳过处理: {}", outputFile.getAbsolutePath());
                        return;
                    }
                    BufferedImage image = sourceDoc1.saveAsImage(currentPage, PdfImageType.Bitmap, 200, 200);
                    ImageIO.write(image, "PNG", new File(fullPath)); // 保存图片到磁盘
                }catch (Exception e){
                    throw new RuntimeException("Error processing page " + currentPage, e);
                }finally {
                    sourceDoc1.close();
                }
            }));
        }
        // 收集结果
        for (Future<?> future : futures) {
            future.get();
        }

        return new ArrayList<>(orderedResults.values());
    }

    /**
     * pdf大文件拆分
     *
     * @param startPage  开始页码
     * @param sourceDoc1 PDF文件
     * @param totalPage  总页数
     * @param endPage    结束页码
     * @param filename
     * @return
     * @throws Exception
     */
    public static ArrayList<String> convertPdfToImagesParallelWithOutExecutor(int startPage, PdfDocument sourceDoc1, int totalPage, int endPage , String prefix, String filename) throws Exception {
        // SortedMap线程安全
        ConcurrentSkipListMap<Integer, String> orderedResults = new ConcurrentSkipListMap<>();
        // 确保输出目录存在
        File outputDir = new File(prefix);
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }
        try {
            for (int page = startPage; page < endPage; page++) {
                final int currentPage = page;
                log.info("当前大文件处理第{}页的数据,总数据量为：{}", currentPage, totalPage);
                String newFilename =  filename + "_" + String.format("%d", page) + ".png"; // 文件命名规则
                String fullPath = prefix + newFilename;
                //判断文件是否存在
                File outputFile = new File(fullPath);
                if (outputFile.exists()) {
                    log.info("图片已存在，跳过处理: {}", outputFile.getAbsolutePath());
                    continue;
                }
                BufferedImage image = sourceDoc1.saveAsImage(currentPage, PdfImageType.Bitmap, 200, 200);
                ImageIO.write(image, "PNG", new File(fullPath)); // 保存图片到磁盘
            }
        } catch (Exception e) {
            throw new RuntimeException("Error processing page ", e);
        }
        return new ArrayList<>(orderedResults.values());
    }

    private static String imageToBase64(BufferedImage image, String format) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(image, format, baos);
            return "data:image/" + format.toLowerCase() + ";base64," +
                    Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("Image encode failed", e);
        }finally {
            image.flush();
        }
    }

    public static int getPdfPages(byte[] pdfBytes) {
        int page = 0;
        try {
            PdfDocument sourceDoc = new PdfDocument(pdfBytes);
            page = sourceDoc.getPages().getCount();
        }catch (Exception e){
            log.error("获取pdf页面异常:",e);
        }
        return page;
    }
}
/**
 * Revision history ------------------------------------------------------------------------- Date Author Note
 * ------------------------------------------------------------------------- 2023/7/12 Leo creat
 */
