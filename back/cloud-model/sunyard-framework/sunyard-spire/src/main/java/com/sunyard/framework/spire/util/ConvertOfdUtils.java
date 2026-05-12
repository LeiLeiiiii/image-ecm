package com.sunyard.framework.spire.util;

import java.io.InputStream;
import java.io.OutputStream;

import com.spire.doc.Document;
import com.spire.pdf.FileFormat;
import com.spire.pdf.PdfDocument;
import com.spire.pdf.PdfPageBase;
import com.spire.pdf.PdfPageSize;
import com.spire.pdf.conversion.OfdConverter;
import com.spire.pdf.graphics.PdfImage;
import com.spire.pdf.graphics.PdfLayoutType;
import com.spire.pdf.graphics.PdfMargins;
import com.spire.pdf.graphics.PdfTextLayout;
import com.spire.presentation.Presentation;
import com.spire.xls.Workbook;

/**
 * @author HRH
 * @date 2023/7/11
 * @describe spire 转ofd
 */
public class ConvertOfdUtils {

    /**
     * word\txt文件 转ofd
     *
     * @param ofdPath  ofd地址
     * @param wordPath word地址
     */
    public static void wordOrTxtToOfd(String wordPath, String ofdPath) {
        // 实例化Document类的对象
        Document doc = new Document();
        // 加载Word
        doc.loadFromFile(wordPath);
        // 保存为PDF格式
        doc.saveToFile(ofdPath, com.spire.doc.FileFormat.OFD);
        doc.close();
    }

    /**
     * word文件转ofd
     *
     * @param wordInputStream word文件输入流
     * @param ofdPath ofd输出文件路径
     */
    public static void wordOrTxtToOfd(InputStream wordInputStream, String ofdPath) {
        Document doc = new Document();
        doc.loadFromStream(wordInputStream, com.spire.doc.FileFormat.Auto);
        doc.saveToFile(ofdPath, com.spire.doc.FileFormat.OFD);
        doc.close();
    }

    /**
     * word文件转ofd
     *
     * @param wordInputStream word文件输入流
     * @param ofdOutputStream ofd输出流
     */
    public static void wordOrTxtToOfd(InputStream wordInputStream, OutputStream ofdOutputStream) {
        // 实例化Document类的对象
        Document doc = new Document();
        // 加载Word
        doc.loadFromStream(wordInputStream, com.spire.doc.FileFormat.Auto);
        // 保存为OFD格式
        doc.saveToStream(ofdOutputStream, com.spire.doc.FileFormat.OFD);
        doc.close();
    }

    /**
     * pdf文件 转ofd
     *
     * @param ofdPath ofd地址
     * @param pdfPath pdf地址
     */
    public static void pdfToOfd(String pdfPath, String ofdPath) {
        // 实例化PdfDocument类的对象
        PdfDocument pdf = new PdfDocument();
        // 加载PDF文档
        pdf.loadFromFile(pdfPath);
        // 保存为OFD格式
        pdf.saveToFile(ofdPath, com.spire.pdf.FileFormat.OFD);
        pdf.close();
    }

    /**
     * pdf文件转ofd
     *
     * @param pdfInputStream pdf文件输入流
     * @param ofdPath ofd输出文件路径
     */
    public static void pdfToOfd(InputStream pdfInputStream, String ofdPath) {
        PdfDocument pdf = new PdfDocument();
        pdf.loadFromStream(pdfInputStream);
        pdf.saveToFile(ofdPath, com.spire.pdf.FileFormat.OFD);
        pdf.close();
    }

    /**
     * pdf文件转ofd
     *
     * @param pdfInputStream pdf文件输入流
     * @param ofdOutputStream ofd输出流
     */
    public static void pdfToOfd(InputStream pdfInputStream, OutputStream ofdOutputStream) {
        // 实例化PdfDocument类的对象
        PdfDocument pdf = new PdfDocument();
        // 加载PDF文档
        pdf.loadFromStream(pdfInputStream);
        // 保存为OFD格式
        pdf.saveToStream(ofdOutputStream, com.spire.pdf.FileFormat.OFD);
        pdf.close();
    }

    /**
     * ppt 转ofd
     *
     * @param ofdPath ofd地址
     * @param pptPath ppt地址
     */
    public static void pptToOfd(String pptPath, String ofdPath) throws Exception {
        // 实例化PdfDocument类的对象
        Presentation ppt = new Presentation();
        ppt.loadFromFile(pptPath);
        // 保存为OFD格式
        ppt.saveToFile(ofdPath, com.spire.presentation.FileFormat.OFD);
        ppt.dispose();
    }

    /**
     * ppt转ofd
     *
     * @param pptInputStream ppt文件输入流
     * @param ofdPath ofd输出文件路径
     */
    public static void pptToOfd(InputStream pptInputStream, String ofdPath) throws Exception {
        Presentation ppt = new Presentation();
        ppt.loadFromStream(pptInputStream, com.spire.presentation.FileFormat.PPT);
        ppt.saveToFile(ofdPath, com.spire.presentation.FileFormat.OFD);
        ppt.dispose();
    }

    /**
     * ppt转ofd
     *
     * @param pptInputStream ppt文件输入流
     * @param ofdOutputStream ofd输出流
     */
    public static void pptToOfd(InputStream pptInputStream, OutputStream ofdOutputStream)
            throws Exception {
        // 实例化Presentation类的对象
        Presentation ppt = new Presentation();
        ppt.loadFromStream(pptInputStream, com.spire.presentation.FileFormat.PPT);
        // 保存为OFD格式
        ppt.saveToFile(ofdOutputStream, com.spire.presentation.FileFormat.OFD);
        ppt.dispose();
    }

    /**
     * excel文件 转ofd
     *
     * @param ofdPath   ofd地址
     * @param excelPath excel地址
     */
    public static void excelToOfd(String excelPath, String ofdPath) {
        Workbook workbook = new Workbook();
        workbook.loadFromFile(excelPath);
        // 保存为OFD
        workbook.saveToFile(ofdPath, com.spire.xls.FileFormat.OFD);
        workbook.dispose();
    }

    /**
     * excel文件转ofd
     *
     * @param excelInputStream excel文件输入流
     * @param ofdPath ofd输出文件路径
     */
    public static void excelToOfd(InputStream excelInputStream, String ofdPath) {
        Workbook workbook = new Workbook();
        workbook.loadFromStream(excelInputStream);
        workbook.saveToFile(ofdPath, com.spire.xls.FileFormat.OFD);
        workbook.dispose();
    }

    /**
     * excel文件转ofd
     *
     * @param excelInputStream excel文件输入流
     * @param ofdOutputStream ofd输出流
     */
    public static void excelToOfd(InputStream excelInputStream, OutputStream ofdOutputStream) {
        Workbook workbook = new Workbook();
        workbook.loadFromStream(excelInputStream);
        // 保存为OFD
        workbook.saveToStream(ofdOutputStream, com.spire.xls.FileFormat.OFD);
        workbook.dispose();
    }

    /**
     * 图片转ofd
     *
     * @param imagePath 图片路径
     * @param ofdPath ofd输出路径
     */
    public static void toImageOfd(String imagePath, String ofdPath) {
        // 创建Pdf 文档
        PdfDocument pdf = new PdfDocument();
        // 读取图像
        PdfImage image = PdfImage.fromFile(imagePath);
        // 设置A4纸的尺寸
        PdfPageBase pdfPage = pdf.getPages().add(PdfPageSize.A4, new PdfMargins(0));
        double widthFitRate = image.getPhysicalDimension().getWidth()
                / pdfPage.getCanvas().getClientSize().getWidth();
        double heightFitRate = image.getPhysicalDimension().getHeight()
                / pdfPage.getCanvas().getClientSize().getHeight();
        double fitRate = Math.max(widthFitRate, heightFitRate);
        // 图片大小
        double fitWidth = image.getPhysicalDimension().getWidth() / fitRate;
        double fitHeight = image.getPhysicalDimension().getHeight() / fitRate;
        // 初始化一个 PdfTextLayout 实例
        PdfTextLayout layout = new PdfTextLayout();
        // 将文本布局设置为一页(如果不设置，内容将无法适应页面大小)
        layout.setLayout(PdfLayoutType.One_Page);
        // 调整图片大小并在页面中心绘制
        int x = (int) ((PdfPageSize.A4.getWidth() - fitWidth) / 2);
        int y = (int) ((PdfPageSize.A4.getHeight() - fitHeight) / 2);
        // 在页面中间角绘制图像
        pdfPage.getCanvas().drawImage(image, x, y, fitWidth, fitHeight);

        // 保存为OFD格式
        pdf.saveToFile(ofdPath, FileFormat.OFD);
        pdf.close();
    }

    /**
     * 图片转ofd
     *
     * @param imageInputStream 图片输入流
     * @param ofdPath ofd输出文件路径
     */
    public static void toImageOfd(InputStream imageInputStream, String ofdPath) {
        PdfDocument pdf = new PdfDocument();
        PdfImage image = PdfImage.fromStream(imageInputStream);
        PdfPageBase pdfPage = pdf.getPages().add(PdfPageSize.A4, new PdfMargins(0));
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
        pdf.saveToFile(ofdPath, FileFormat.OFD);
        pdf.close();
    }

    /**
     * 图片转ofd
     *
     * @param imageInputStream 图片输入流
     * @param ofdOutputStream ofd输出流
     */
    public static void toImageOfd(InputStream imageInputStream, OutputStream ofdOutputStream) {
        // 创建Pdf 文档
        PdfDocument pdf = new PdfDocument();
        // 读取图像
        PdfImage image = PdfImage.fromStream(imageInputStream);
        // 设置A4纸的尺寸
        PdfPageBase pdfPage = pdf.getPages().add(PdfPageSize.A4, new PdfMargins(0));
        double widthFitRate = image.getPhysicalDimension().getWidth()
                / pdfPage.getCanvas().getClientSize().getWidth();
        double heightFitRate = image.getPhysicalDimension().getHeight()
                / pdfPage.getCanvas().getClientSize().getHeight();
        double fitRate = Math.max(widthFitRate, heightFitRate);
        // 图片大小
        double fitWidth = image.getPhysicalDimension().getWidth() / fitRate;
        double fitHeight = image.getPhysicalDimension().getHeight() / fitRate;
        // 初始化一个 PdfTextLayout 实例
        PdfTextLayout layout = new PdfTextLayout();
        // 将文本布局设置为一页(如果不设置，内容将无法适应页面大小)
        layout.setLayout(PdfLayoutType.One_Page);
        // 调整图片大小并在页面中心绘制
        int x = (int) ((PdfPageSize.A4.getWidth() - fitWidth) / 2);
        int y = (int) ((PdfPageSize.A4.getHeight() - fitHeight) / 2);
        // 在页面中间角绘制图像
        pdfPage.getCanvas().drawImage(image, x, y, fitWidth, fitHeight);

        // 保存为OFD格式
        pdf.saveToStream(ofdOutputStream, FileFormat.OFD);
        pdf.close();
    }

    /**
     * ofd 转pdf
     *
     * @param ofdPath ofd地址
     * @param pdfPath pdf地址
     */
    public static void ofdToPdf(String pdfPath, String ofdPath) {
        // 加载OFD文档
        OfdConverter converter = new OfdConverter(ofdPath);
        // 保存为PDF格式
        converter.toPdf(pdfPath);
        converter.dispose();
    }

    /**
     * ofd转pdf
     *
     * @param ofdInputStream ofd文件输入流
     * @param pdfPath pdf输出文件路径
     */
    public static void ofdToPdf(InputStream ofdInputStream, String pdfPath) {
        OfdConverter converter = new OfdConverter(ofdInputStream);
        converter.toPdf(pdfPath);
        converter.dispose();
    }

    /**
     * ofd转pdf
     *
     * @param ofdInputStream ofd文件输入流
     * @param pdfOutputStream pdf输出流
     */
    public static void ofdToPdf(InputStream ofdInputStream, OutputStream pdfOutputStream) {
        // 加载OFD文档
        OfdConverter converter = new OfdConverter(ofdInputStream);
        // 保存为PDF格式
        converter.toPdf(pdfOutputStream);
        converter.dispose();
    }

}
