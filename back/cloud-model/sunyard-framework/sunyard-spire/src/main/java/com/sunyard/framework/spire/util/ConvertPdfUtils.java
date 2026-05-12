
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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.media.jai.RenderedImageAdapter;

import com.spire.doc.Document;
import com.spire.pdf.FileFormat;
import com.spire.pdf.PdfDocument;
import com.spire.pdf.PdfDocumentBase;
import com.spire.pdf.PdfPageBase;
import com.spire.pdf.PdfPageSize;
import com.spire.pdf.conversion.OfdConverter;
import com.spire.pdf.graphics.LineInfo;
import com.spire.pdf.graphics.PdfGraphicsUnit;
import com.spire.pdf.graphics.PdfImage;
import com.spire.pdf.graphics.PdfLayoutType;
import com.spire.pdf.graphics.PdfMargins;
import com.spire.pdf.graphics.PdfRGBColor;
import com.spire.pdf.graphics.PdfSolidBrush;
import com.spire.pdf.graphics.PdfStringFormat;
import com.spire.pdf.graphics.PdfStringLayoutResult;
import com.spire.pdf.graphics.PdfStringLayouter;
import com.spire.pdf.graphics.PdfTextLayout;
import com.spire.pdf.graphics.PdfTrueTypeFont;
import com.spire.pdf.graphics.PdfUnitConvertor;
import com.spire.pdf.graphics.PdfWordWrapType;
import com.spire.presentation.Presentation;
import com.spire.xls.Workbook;
import com.sun.media.jai.codec.FileSeekableStream;
import com.sun.media.jai.codec.ImageCodec;
import com.sun.media.jai.codec.ImageDecoder;
import com.sun.media.jai.codec.TIFFEncodeParam;

/**
 * @author Leo
 * @Desc pdf处理类
 * @date 2023/7/12 9:25
 */
public class ConvertPdfUtils {

    // 常量定义
    private static final PdfMargins ZERO_MARGINS = new PdfMargins(0);
    private static final Font DEFAULT_FONT = new Font("宋体", Font.BOLD, 10);
    private static final String DEFAULT_ENCODING = "UTF-8";

    /**
     * word转pdf
     *
     * @param filePath word文件路径
     * @param pdfPath 生成pdf路径
     */
    public static void toWordPdf(String filePath, String pdfPath, String password) {
        Document doc = new Document();
        try {
            loadWordDocument(doc, filePath, password);
            doc.saveToFile(pdfPath, com.spire.doc.FileFormat.PDF);
        } finally {
            doc.close();
        }
    }

    /**
     * word转pdf
     *
     * @param inputStream word文件流
     * @param pdfPath 生成pdf路径
     */
    public static void toWordPdf(InputStream inputStream, String pdfPath, String password) {
        Document doc = new Document();
        try {
            loadWordDocumentFromStream(doc, inputStream, password);
            doc.saveToFile(pdfPath, com.spire.doc.FileFormat.PDF);
        } finally {
            doc.close();
        }
    }

    /**
     * word转pdf
     *
     * @param inputStream word文件流 生成pdf路径
     */
    public static void toWordPdf(InputStream inputStream, OutputStream outputStream,
                                 String password) {
        Document doc = new Document();
        try {
            loadWordDocumentFromStream(doc, inputStream, password);
            doc.saveToStream(outputStream, com.spire.doc.FileFormat.PDF);
        } finally {
            doc.close();
        }
    }

    /**
     * excel转pdf
     *
     * @param filePath excel文件路径
     * @param pdfPath 生成pdf路径
     */
    public static void toExcelPdf(String filePath, String pdfPath, String password) {
        Workbook workbook = new Workbook();
        try {
            loadExcelWorkbook(workbook, filePath, password);
            workbook.getConverterSetting().setSheetFitToPage(true);
            workbook.saveToFile(pdfPath, com.spire.xls.FileFormat.PDF);
        } finally {
            workbook.dispose();
        }
    }

    /**
     * excel转pdf
     *
     * @param inputStream excel文件流 生成pdf路径
     */
    public static void toExcelPdf(InputStream inputStream, OutputStream outputStream,
                                  String password) {
        Workbook workbook = new Workbook();
        try {
            loadExcelWorkbookFromStream(workbook, inputStream, password);
            workbook.getConverterSetting().setSheetFitToPage(true);
            workbook.saveToStream(outputStream, com.spire.xls.FileFormat.PDF);
        } finally {
            workbook.dispose();
        }
    }

    /**
     * excel转pdf
     *
     * @param inputStream excel文件流
     * @param pdfPath 生成pdf路径
     */
    public static void toExcelPdf(InputStream inputStream, String pdfPath, String password) {
        Workbook workbook = new Workbook();
        try {
            loadExcelWorkbookFromStream(workbook, inputStream, password);
            workbook.getConverterSetting().setSheetFitToPage(true);
            workbook.saveToFile(pdfPath, com.spire.xls.FileFormat.PDF);
        } finally {
            workbook.dispose();
        }
    }

    /**
     * ppt转pdf
     *
     * @param filePath ppt文件路径
     * @param pdfPath 生成pdf路径
     */
    public static void toPptPdf(String filePath, String pdfPath, String password) {
        Presentation ppt = new Presentation();
        try {
            loadPresentation(ppt, filePath, password);
            ppt.saveToFile(pdfPath, com.spire.presentation.FileFormat.PDF);
        } catch (Exception e) {
            throw new RuntimeException("文件【" + filePath + "】转换到【" + pdfPath + "】时，出错", e);
        } finally {
            ppt.dispose();
        }
    }

    /**
     * ppt转pdf
     *
     * @param inputStream ppt文件路径 生成pdf路径
     */
    public static void toPptPdf(InputStream inputStream, OutputStream outputStream,
                                String password) {
        Presentation ppt = new Presentation();
        try {
            loadPresentationFromStream(ppt, inputStream, password);
            ppt.saveToFile(outputStream, com.spire.presentation.FileFormat.PDF);
        } catch (Exception e) {
            throw new RuntimeException("ppt文件转换到pdf时，出错", e);
        } finally {
            ppt.dispose();
        }
    }

    /**
     * ppt转pdf
     *
     * @param inputStream ppt文件路径
     * @param pdfPath 生成pdf路径
     */
    public static void toPptPdf(InputStream inputStream, String pdfPath, String password) {
        Presentation ppt = new Presentation();
        try {
            loadPresentationFromStream(ppt, inputStream, password);
            ppt.saveToFile(pdfPath, com.spire.presentation.FileFormat.PDF);
        } catch (Exception e) {
            throw new RuntimeException("文件转换到【" + pdfPath + "】时，出错", e);
        } finally {
            ppt.dispose();
        }
    }

    /**
     * ofd 转pdf
     * @param inputStream ofd文件流
     * @param pdfPath 存储地址
     */
    public static void toOfdPdf(InputStream inputStream, String pdfPath) {
        OfdConverter ofdConverter = new OfdConverter(inputStream);
        try {
            ofdConverter.toPdf(pdfPath);
        } catch (Exception e) {
            throw new RuntimeException("文件转换到【" + pdfPath + "】时，出错", e);
        } finally {
            ofdConverter.dispose();
        }
    }

    /**
     * ofd 转pdf
     * @param inputStream odf文件流
     * @param outputStream 输出流
     */
    public static void toOfdPdf(InputStream inputStream, OutputStream outputStream) {
        OfdConverter ofdConverter = new OfdConverter(inputStream);
        try {
            ofdConverter.toPdf(outputStream);
        } catch (Exception e) {
            throw new RuntimeException("OFD文件转时出错", e);
        } finally {
            ofdConverter.dispose();
        }
    }

    /**
     * ofd 转pdf
     * @param filePath odf文件地址
     * @param pdfPath pdf文件地址
     */
    public static void toOfdPdf(String filePath, String pdfPath) {
        OfdConverter ofdConverter = new OfdConverter(filePath);
        try {
            ofdConverter.toPdf(pdfPath);
        } catch (Exception e) {
            throw new RuntimeException("文件转换到【" + pdfPath + "】时，出错", e);
        } finally {
            ofdConverter.dispose();
        }
    }

    /**
     * heif转pdf
     * @param inputStream heif文件流
     * @param pdfPath pdf文件地址
     */
    public static void toHeifPdf(InputStream inputStream, String pdfPath) {
        PdfDocument pdf = new PdfDocument();
        try {
            processHeifImage(pdf, inputStream, pdfPath, null);
        } finally {
            pdf.dispose();
        }
    }

    /**
     * heif转pdf
     * @param filePath heif文件流
     * @param pdfPath pdf文件地址
     */
    public static void toHeifPdf(String filePath, String pdfPath) {
        PdfDocument pdf = new PdfDocument();
        try {
            processHeifImageFromFile(pdf, filePath, pdfPath, null);
        } finally {
            pdf.dispose();
        }
    }

    /**
     * heif转pdf
     * @param inputStream heif输入流
     * @param outputStream pdf输出流
     */
    public static void toHeifPdf(InputStream inputStream, OutputStream outputStream) {
        PdfDocument pdf = new PdfDocument();
        try {
            processHeifImage(pdf, inputStream, null, outputStream);
        } finally {
            pdf.dispose();
        }
    }

    /**
     * tif转pdf
     *
     * @param filePath tif原始路径
     * @param pdfPath pdf目标存储路径
     * @throws IOException io异常
     */
    public static void toTifPdf(String filePath, String pdfPath) throws IOException {
        PdfDocument pdf = new PdfDocument();
        FileSeekableStream ss = new FileSeekableStream(filePath);
        try {
            processTiffImages(pdf, ss, pdfPath, null);
        } finally {
            pdf.close();
            ss.close();
        }
    }

    /**
     * tif转pdf
     *
     * @param inputStream tif文件流
     * @param pdfPath pdf目标存储路径
     * @throws IOException io异常
     */
    public static void toTifPdf(InputStream inputStream, String pdfPath) throws IOException {
        PdfDocument pdf = new PdfDocument();
        try {
            processTiffImages(pdf, inputStream, pdfPath, null);
        } finally {
            pdf.close();
        }
    }

    /**
     * 文本类文件转换成pdf预览
     *
     * @param filePath 文件文件路径
     * @param pdfPath pdf文件路径
     */
    public static void toTxtPdf(String filePath, String pdfPath) {
        try {
            String fileCharsetName = FileInfoUtils.getEncoding(new File(filePath));
            StringBuilder result = new StringBuilder();

            try (InputStream is = Files.newInputStream(Paths.get(filePath));
                 InputStreamReader isr = new InputStreamReader(is, fileCharsetName);
                 BufferedReader br = new BufferedReader(isr)) {

                String s;
                while ((s = br.readLine()) != null) {
                    result.append(System.lineSeparator()).append(s);
                }
            }

            processTextToPdf(result.toString(), pdfPath, null, DEFAULT_ENCODING);
        } catch (Exception e) {
            throw new RuntimeException("文件【" + filePath + "】转换到【" + pdfPath + "】时，出错", e);
        }
    }

    /**
     * 文本类文件转换成pdf预览
     * @param is 输入流
     * @param outputStream 输出流
     * @param fileCharsetName 文件格式
     */
    public static void toTxtPdf(InputStream is, OutputStream outputStream, String fileCharsetName) {
        try {
            StringBuilder result = new StringBuilder();

            try (InputStreamReader isr = new InputStreamReader(is, fileCharsetName);
                 BufferedReader br = new BufferedReader(isr)) {

                String s;
                while ((s = br.readLine()) != null) {
                    result.append(System.lineSeparator()).append(s);
                }
            }

            processTextToPdf(result.toString(), null, outputStream, fileCharsetName);
        } catch (Exception e) {
            throw new RuntimeException("文件转换时，出错", e);
        }
    }

    /**
     * 文本类文件转换成pdf预览
     * @param is 输出流
     * @param pdfPath pdf路径
     * @param fileCharsetName 文件格式
     */
    public static void toTxtPdf(InputStream is, String pdfPath, String fileCharsetName) {
        try {
            StringBuilder result = new StringBuilder();

            try (InputStreamReader isr = new InputStreamReader(is, fileCharsetName);
                 BufferedReader br = new BufferedReader(isr)) {

                String s;
                while ((s = br.readLine()) != null) {
                    result.append(System.lineSeparator()).append(s);
                }
            }

            processTextToPdf(result.toString(), pdfPath, null, fileCharsetName);
        } catch (Exception e) {
            throw new RuntimeException("文件转换时，出错", e);
        }
    }

    /**
     * 图片转pdf
     *
     * @param imageInputStream 图片输入流
     * @param pdfOutputStream pdf输出流
     */
    public static void toImagePdf(InputStream imageInputStream, OutputStream pdfOutputStream) {
        PdfDocument pdf = new PdfDocument();
        try {
            PdfImage image = PdfImage.fromStream(imageInputStream);
            processImageToPdf(image, pdf, null, pdfOutputStream);
        } catch (Exception e) {
            pdf.close();
            throw new RuntimeException("图片转PDF时出错", e);
        }
    }
    /**
     * 图片转pdf
     *
     * @param imageInputStream 图片输入流
     * @param pdfPath pdf输出路径
     */
    public static void toImagePdf(InputStream imageInputStream, String pdfPath) {
        PdfDocument pdf = new PdfDocument();
        try {
            PdfImage image = PdfImage.fromStream(imageInputStream);
            processImageToPdf(image, pdf, pdfPath, null);
        } catch (Exception e) {
            pdf.close();
            throw new RuntimeException("文件转换到【" + pdfPath + "】时，出错", e);
        }
    }

    /**
     * 图片转pdf
     *
     * @param imagePath 图片路径
     * @param pdfPath pdf输出路径
     */
    public static void toImagePdf(String imagePath, String pdfPath) {
        PdfDocument pdf = new PdfDocument();
        try {
            PdfImage image = PdfImage.fromFile(imagePath);
            processImageToPdf(image, pdf, pdfPath, null);
        } catch (Exception e) {
            pdf.close();
            throw new RuntimeException("文件转换到【" + pdfPath + "】时，出错", e);
        }
    }

    /**
     * pdf合并
     *
     * @param toPdfPath  路径 + 文件名
     */
    public static void pdfsToPdf(String[] files, String toPdfPath) {
        PdfDocumentBase doc = PdfDocument.mergeFiles(files);
        doc.save(toPdfPath, FileFormat.PDF);
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 加载Word文档
     */
    private static void loadWordDocument(Document doc, String filePath, String password) {
        if (password != null && !password.trim().isEmpty()) {
            doc.loadFromFile(filePath, com.spire.doc.FileFormat.Auto, password);
            doc.removeEncryption();
        } else {
            doc.loadFromFile(filePath, com.spire.doc.FileFormat.Auto);
        }
    }

    /**
     * 从流加载Word文档
     */
    private static void loadWordDocumentFromStream(Document doc, InputStream inputStream, String password) {
        if (password != null && !password.trim().isEmpty()) {
            doc.loadFromStream(inputStream, com.spire.doc.FileFormat.Auto, password);
            doc.removeEncryption();
        } else {
            doc.loadFromStream(inputStream, com.spire.doc.FileFormat.Auto);
        }
    }

    /**
     * 加载Excel工作簿
     */
    private static void loadExcelWorkbook(Workbook workbook, String filePath, String password) {
        if (password != null && !password.trim().isEmpty()) {
            workbook.setOpenPassword(password);
            workbook.loadFromFile(filePath);
            workbook.unProtect();
        } else {
            workbook.loadFromFile(filePath);
        }
    }

    /**
     * 从流加载Excel工作簿
     */
    private static void loadExcelWorkbookFromStream(Workbook workbook, InputStream inputStream, String password) {
        if (password != null && !password.trim().isEmpty()) {
            workbook.setOpenPassword(password);
            workbook.loadFromStream(inputStream);
            workbook.unProtect();
        } else {
            workbook.loadFromStream(inputStream);
        }
    }

    /**
     * 加载PPT演示文稿
     */
    private static void loadPresentation(Presentation ppt, String filePath, String password) throws Exception {
        if (password != null && !password.trim().isEmpty()) {
            ppt.loadFromFile(filePath, password);
            ppt.removeEncryption();
        } else {
            ppt.loadFromFile(filePath);
        }
    }

    /**
     * 从流加载PPT演示文稿
     */
    private static void loadPresentationFromStream(Presentation ppt, InputStream inputStream, String password) throws Exception {
        if (password != null && !password.trim().isEmpty()) {
            ppt.loadFromStream(inputStream, com.spire.presentation.FileFormat.PPT, password);
            ppt.removeEncryption();
        } else {
            ppt.loadFromStream(inputStream, com.spire.presentation.FileFormat.PPT);
        }
    }

    /**
     * 处理HEIF图像转换
     */
    private static void processHeifImage(PdfDocument pdf, InputStream inputStream, String outputPath, OutputStream outputStream) {
        try {
            PdfPageBase page = pdf.getPages().add();
            PdfImage image = PdfImage.fromStream(inputStream);
            drawImageOnPage(page, image);

            if (outputStream != null) {
                pdf.saveToStream(outputStream, FileFormat.PDF);
            } else if (outputPath != null) {
                pdf.saveToFile(outputPath, FileFormat.PDF);
            }
        } catch (Exception e) {
            throw new RuntimeException("HEIF文件转换PDF时出错", e);
        }
    }

    /**
     * 处理HEIF图像文件转换
     */
    private static void processHeifImageFromFile(PdfDocument pdf, String filePath, String outputPath, OutputStream outputStream) {
        try {
            PdfPageBase page = pdf.getPages().add();
            PdfImage image = PdfImage.fromFile(filePath);
            drawImageOnPage(page, image);

            if (outputStream != null) {
                pdf.saveToStream(outputStream, FileFormat.PDF);
            } else if (outputPath != null) {
                pdf.saveToFile(outputPath, FileFormat.PDF);
            }
        } catch (Exception e) {
            throw new RuntimeException("HEIF文件转换PDF时出错", e);
        }
    }

    /**
     * 处理TIFF图像转换
     */
    private static void processTiffImages(PdfDocument pdf, Object input, String outputPath, OutputStream outputStream) throws IOException {
        TIFFEncodeParam param = new TIFFEncodeParam();
        ImageDecoder dec = ImageCodec.createImageDecoder("tiff", (InputStream) input, null);
        int count = dec.getNumPages();
        param.setCompression(TIFFEncodeParam.COMPRESSION_GROUP4);
        param.setLittleEndian(false);
        PdfUnitConvertor convertor = new PdfUnitConvertor();

        try {
            for (int i = 0; i < count; i++) {
                RenderedImage page = dec.decodeAsRenderedImage(i);
                BufferedImage bufferedImage = new RenderedImageAdapter(page).getAsBufferedImage();
                PdfImage image = PdfImage.fromImage(bufferedImage);

                float widthPoint = convertor.convertUnits(bufferedImage.getWidth(),
                        PdfGraphicsUnit.Pixel, PdfGraphicsUnit.Point);
                float heightPoint = convertor.convertUnits(bufferedImage.getHeight(),
                        PdfGraphicsUnit.Pixel, PdfGraphicsUnit.Point);

                Dimension dimension = new Dimension();
                dimension.setSize(widthPoint, heightPoint);

                PdfPageBase pdfPage = pdf.getPages().add(dimension, ZERO_MARGINS);
                pdfPage.getCanvas().drawImage(image, 0, 0);
            }

            if (outputStream != null) {
                pdf.saveToStream(outputStream, com.spire.pdf.FileFormat.PDF);
            } else if (outputPath != null) {
                pdf.saveToFile(outputPath, com.spire.pdf.FileFormat.PDF);
            }
        } catch (Exception e) {
            throw new RuntimeException("TIFF文件转换PDF时出错", e);
        }
    }

    /**
     * 处理文本转换为PDF
     */
    private static void processTextToPdf(String content, String outputPath, OutputStream outputStream, String encoding) {
        PdfDocument pdf = new PdfDocument();
        try {
            PdfPageBase page = pdf.getPages().add(PdfPageSize.A4, ZERO_MARGINS);
            float y = 20;

            PdfTrueTypeFont font = new PdfTrueTypeFont(DEFAULT_FONT, true);
            PdfStringFormat format = new PdfStringFormat();
            format.setWordWrap(PdfWordWrapType.Character);
            format.setLineSpacing(font.getSize() * 1.5f);

            PdfStringLayouter textLayouter = new PdfStringLayouter();
            PdfSolidBrush brush = new PdfSolidBrush(new PdfRGBColor(Color.black));

            Dimension size = new Dimension();
            size.setSize(PdfPageSize.A4.getWidth() - 50, 22222);
            PdfStringLayoutResult result = textLayouter.layout(content, font, format, size);
            double height = PdfPageSize.A4.getHeight();

            for (LineInfo line : result.getLines()) {
                if (y + result.getLineHeight() > height - 40) {
                    page = pdf.getPages().add(PdfPageSize.A4, ZERO_MARGINS);
                    y = 20;
                }
                page.getCanvas().drawString(line.getText(), font, brush, 25, y, format);
                y = y + result.getLineHeight();
            }

            if (outputStream != null) {
                pdf.saveToStream(outputStream, FileFormat.PDF);
            } else if (outputPath != null) {
                pdf.saveToFile(outputPath, FileFormat.PDF);
            }
        } finally {
            pdf.close();
        }
    }

    /**
     * 处理图片转换为PDF
     */
    private static void processImageToPdf(PdfImage image, PdfDocument pdf, String outputPath, OutputStream outputStream) {
        try {
            PdfPageBase pdfPage = pdf.getPages().add(PdfPageSize.A4, ZERO_MARGINS);
            double widthFitRate = image.getPhysicalDimension().getWidth()
                    / pdfPage.getCanvas().getClientSize().getWidth();
            double heightFitRate = image.getPhysicalDimension().getHeight()
                    / pdfPage.getCanvas().getClientSize().getHeight();
            double fitRate = Math.max(widthFitRate, heightFitRate);

            double fitWidth = image.getPhysicalDimension().getWidth() / fitRate;
            double fitHeight = image.getPhysicalDimension().getHeight() / fitRate;

            PdfTextLayout layout = new PdfTextLayout();
            layout.setLayout(PdfLayoutType.One_Page);

            int x = (int) ((PdfPageSize.A4.getWidth() - fitWidth) / 2);
            int y = (int) ((PdfPageSize.A4.getHeight() - fitHeight) / 2);

            pdfPage.getCanvas().drawImage(image, x, y, fitWidth, fitHeight);

            if (outputStream != null) {
                pdf.saveToStream(outputStream);
            } else if (outputPath != null) {
                pdf.saveToFile(outputPath, FileFormat.PDF);
            }
        } finally {
            if (outputStream == null) {
                pdf.close();
            }
        }
    }

    /**
     * 在页面上绘制图像
     */
    private static void drawImageOnPage(PdfPageBase page, PdfImage image) {
        double widthFitRate = image.getPhysicalDimension().getWidth()
                / page.getCanvas().getClientSize().getWidth();
        double heightFitRate = image.getPhysicalDimension().getHeight()
                / page.getCanvas().getClientSize().getHeight();
        double fitRate = Math.max(widthFitRate, heightFitRate);

        double fitWidth = image.getPhysicalDimension().getWidth() / fitRate;
        double fitHeight = image.getPhysicalDimension().getHeight() / fitRate;

        int x = (int) ((page.getCanvas().getClientSize().getWidth() - fitWidth) / 2);
        int y = (int) ((page.getCanvas().getClientSize().getHeight() - fitHeight) / 2);

        page.getCanvas().drawImage(image, x, y, fitWidth, fitHeight);
    }
}
