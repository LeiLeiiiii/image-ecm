package com.sunyard.framework.spire.util;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.geom.Dimension2D;
import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;
import java.awt.geom.Rectangle2D;
import java.io.InputStream;
import java.io.OutputStream;

import com.spire.pdf.FileFormat;
import com.spire.pdf.PdfDocument;
import com.spire.pdf.PdfPageBase;
import com.spire.pdf.graphics.PdfBrush;
import com.spire.pdf.graphics.PdfBrushes;
import com.spire.pdf.graphics.PdfRGBColor;
import com.spire.pdf.graphics.PdfSolidBrush;
import com.spire.pdf.graphics.PdfStringFormat;
import com.spire.pdf.graphics.PdfTemplate;
import com.spire.pdf.graphics.PdfTextAlignment;
import com.spire.pdf.graphics.PdfTilingBrush;
import com.spire.pdf.graphics.PdfTrueTypeFont;

/**
 * @author zhouleibin 水印工具类
 */
public class WatermarkUtils {

    /**
     * 插入水印
     * @param page pdf页数
     * @param watermark 水印
     * @param vmFontSize 字体大小
     */
    private static void insertWatermark(PdfPageBase page, String watermark, int vmFontSize) {
        Dimension2D dimension2D = new Dimension();
        if (page.getCanvas().getClientSize().getWidth() > page.getCanvas().getClientSize().getHeight()) {
            dimension2D.setSize(page.getCanvas().getClientSize().getWidth() / 3,
                    page.getCanvas().getClientSize().getHeight() / 2);
        } else {
            dimension2D.setSize(page.getCanvas().getClientSize().getWidth() / 2,
                    page.getCanvas().getClientSize().getHeight() / 3);
        }
        PdfTilingBrush brush = new PdfTilingBrush(dimension2D);
        brush.getGraphics().setTransparency(0.3F);
        brush.getGraphics().save();
        brush.getGraphics().translateTransform((float) brush.getSize().getWidth() / 2,
                (float) brush.getSize().getHeight() / 2);
        brush.getGraphics().rotateTransform(-45);
        Font font = new Font("宋体", 10, vmFontSize);
        PdfTrueTypeFont pdfTrueTypeFont = new PdfTrueTypeFont(font, true);
        brush.getGraphics().drawString(watermark, pdfTrueTypeFont, PdfBrushes.getGray(), 0, 0,
                new PdfStringFormat(PdfTextAlignment.Center));
        brush.getGraphics().restore();
        brush.getGraphics().setTransparency(1);
        Rectangle2D loRect = new Rectangle2D.Float();
        loRect.setFrame(new Point2D.Float(0, 0), page.getCanvas().getClientSize());
        page.getCanvas().drawRectangle(brush, loRect);
    }

    /**
     * pdf添加水印
     *
     * @param oldPdfPath  原始pdf文件路径
     * @param tempPdfPath 生成pdf水印文件临时路径
     * @param text        水印文本
     */
    public static void pdfWaterMark(String oldPdfPath, String tempPdfPath,
                                    String text, int vmFontSize) {
        PdfDocument pdf = new PdfDocument();

        // 加载示例文档
        pdf.loadFromFile(oldPdfPath);
        // 创建新文档去绘制水印到上面
        PdfDocument templatePdf = new PdfDocument();
        PdfPageBase templatePage = templatePdf.getPages().add(pdf.getPages().get(0).getSize());
        insertWatermark(templatePage, text, vmFontSize);

        // 把绘制了水印的这一页作为模版
        PdfTemplate pdfTemplate = templatePage.createTemplate();
        // 获取第一页
        int i = 0;
        pdf.getPages().getCount();
        for (i = 0; i < pdf.getPages().getCount(); i++) {
            PdfPageBase page = pdf.getPages().get(i);
            // 把模版绘制到源文档每一页
            page.getCanvas().drawTemplate(pdfTemplate,new Double(0,0));
        }
        // 保存文档
        pdf.saveToFile(tempPdfPath, FileFormat.PDF);
        pdf.close();
    }

    /**
     * pdf添加水印
     *
     * @param inputStream 原始pdf文件流
     * @param tempPdfPath 生成pdf水印文件临时路径
     * @param text 内容
     */
    public static void pdfWaterMark(InputStream inputStream, String tempPdfPath,
                                    String text, int vmFontSize) {
        PdfDocument pdf = new PdfDocument();

        // 加载示例文档
        pdf.loadFromStream(inputStream);
        // 创建新文档去绘制水印到上面
        PdfDocument templatePdf = new PdfDocument();
        PdfPageBase templatePage = templatePdf.getPages().add(pdf.getPages().get(0).getSize());
        insertWatermark(templatePage, text, vmFontSize);

        // 把绘制了水印的这一页作为模版
        PdfTemplate pdfTemplate = templatePage.createTemplate();
        // 获取第一页
        int i = 0;
        pdf.getPages().getCount();
        for (i = 0; i < pdf.getPages().getCount(); i++) {
            PdfPageBase page = pdf.getPages().get(i);
            // 把模版绘制到源文档每一页
            page.getCanvas().drawTemplate(pdfTemplate,new Double(0,0));
        }
        // 保存文档
        pdf.saveToFile(tempPdfPath, FileFormat.PDF);
        pdf.close();
    }

    /**
     * pdf添加水印
     *
     * @param inputStream 原始pdf文件流 生成pdf水印文件临时路径
     * @param text        水印文本
     */
    public static void pdfwaterMark(InputStream inputStream, String text,
                                    int vmFontSize, OutputStream outputStream) {
        PdfDocument pdf = new PdfDocument();

        // 加载示例文档
        pdf.loadFromStream(inputStream);
        // 创建新文档去绘制水印到上面
        PdfDocument templatePdf = new PdfDocument();
        PdfPageBase templatePage = templatePdf.getPages().add(pdf.getPages().get(0).getSize());
        insertWatermark(templatePage, text, vmFontSize);

        // 把绘制了水印的这一页作为模版
        PdfTemplate pdfTemplate = templatePage.createTemplate();
        // 获取第一页
        int i = 0;
        pdf.getPages().getCount();
        for (i = 0; i < pdf.getPages().getCount(); i++) {
            PdfPageBase page = pdf.getPages().get(i);
            // 把模版绘制到源文档每一页
            page.getCanvas().drawTemplate(pdfTemplate,new Double(0,0));
        }
        // 保存文档
        pdf.saveToStream(outputStream);
        pdf.close();
    }

    /**
     * pdf添加水印
     *
     * @param inputStream    文件输入流
     * @param path           临时存储路径
     * @param degree         水印旋转度
     * @param configFont     字体配置
     * @param configColor    字体颜色
     * @param watermarkValue 水印内容
     */
    public static void addPdfWaterMarkByConfig(InputStream inputStream, String path, Integer degree, Font configFont,
                                            Color configColor, String watermarkValue,String password) {
        PdfDocument pdf = new PdfDocument();
        if (password != null && !password.trim().isEmpty()) {
            pdf.loadFromStream(inputStream,password);
//            pdf.decrypt();
        }else {
            pdf.loadFromStream(inputStream);
        }
        // 创建新文档去绘制水印到上面
        PdfDocument templatePdf = new PdfDocument();
        PdfPageBase templatePage = templatePdf.getPages().add(pdf.getPages().get(0).getSize());
        getWaterMark(degree, configFont, configColor, watermarkValue, templatePage);

        // 把绘制了水印的这一页作为模版
        PdfTemplate pdfTemplate = templatePage.createTemplate();
        // 获取第一页
        int i = 0;
        pdf.getPages().getCount();
        for (i = 0; i < pdf.getPages().getCount(); i++) {
            PdfPageBase page = pdf.getPages().get(i);
            // 把模版绘制到源文档每一页
            page.getCanvas().drawTemplate(pdfTemplate,new Double(0,0));
        }
        // 保存文档
        pdf.saveToFile(path);
        pdf.close();
    }

    /**
     * pdf添加水印
     *
     * @param inputStream    文件输入流
     * @param outputStream   文件输出流
     * @param degree         水印旋转度
     * @param configFont     字体配置
     * @param configColor    字体颜色
     * @param watermarkValue 水印内容
     */
    public static void addPdfWaterMarkByConfig(InputStream inputStream, OutputStream outputStream, Integer degree, Font configFont,
                                               Color configColor, String watermarkValue) {
        PdfDocument pdf = new PdfDocument();
        pdf.loadFromStream(inputStream);
        // 创建新文档去绘制水印到上面
        PdfDocument templatePdf = new PdfDocument();
        PdfPageBase templatePage = templatePdf.getPages().add(pdf.getPages().get(0).getSize());
        getWaterMark(degree, configFont, configColor, watermarkValue, templatePage);

        // 把绘制了水印的这一页作为模版
        PdfTemplate pdfTemplate = templatePage.createTemplate();
        // 获取第一页
        int i = 0;
        pdf.getPages().getCount();
        for (i = 0; i < pdf.getPages().getCount(); i++) {
            PdfPageBase page = pdf.getPages().get(i);
            // 把模版绘制到源文档每一页
            page.getCanvas().drawTemplate(pdfTemplate,new Double(0,0));
        }
        // 保存文档
        pdf.saveToStream(outputStream);
        pdf.close();
    }

    /**
     * 获取水印
     * @param degree 角度
     * @param configFont 字体
     * @param configColor 颜色
     * @param watermarkValue 内容
     * @param page 页码
     */
    private static void getWaterMark(Integer degree, Font configFont, Color configColor, String watermarkValue, PdfPageBase page) {
        Dimension2D dimension2D = new Dimension();
        if (page.getCanvas().getClientSize().getWidth() > page.getCanvas().getClientSize().getHeight()) {
            dimension2D.setSize(page.getCanvas().getClientSize().getWidth() / 3, page.getCanvas().getClientSize().getHeight() / 2);
        } else {
            dimension2D.setSize(page.getCanvas().getClientSize().getWidth() / 2, page.getCanvas().getClientSize().getHeight() / 3);
        }
        PdfTilingBrush brush = new PdfTilingBrush(dimension2D);
        brush.getGraphics().setTransparency(0.6F);
        brush.getGraphics().save();
        brush.getGraphics().translateTransform(
                (float) brush.getSize().getWidth() / 2,
                (float) brush.getSize().getHeight() / 2);
        brush.getGraphics().rotateTransform(degree);
        // 字体
        PdfTrueTypeFont pdfTrueTypeFont = new PdfTrueTypeFont(configFont, true);
        // 字体颜色
        PdfBrush color = new PdfSolidBrush(new PdfRGBColor(configColor));
        brush.getGraphics().drawString(watermarkValue, pdfTrueTypeFont,
                color, 0, 0,
                new PdfStringFormat(PdfTextAlignment.Center));
        brush.getGraphics().restore();
        brush.getGraphics().setTransparency(1);
        Rectangle2D loRect = new Rectangle2D.Float();
        loRect.setFrame(new Point2D.Float(0, 0), page.getCanvas().getClientSize());
        page.getCanvas().drawRectangle(brush, loRect);
    }

    /**
     * pdf添加水印
     *
     * @param inputStream    文件输入流
     * @param degree         水印旋转度
     * @param configFont     字体配置
     * @param watermarkValue 水印内容
     * @param configColor    字体颜色
     */
    public static void getPdfWaterMarkStreamByConfig(InputStream inputStream, Integer degree, Font configFont,
                                                  String watermarkValue, Color configColor, OutputStream outputStream) {
        PdfDocument pdf = new PdfDocument();
        pdf.loadFromStream(inputStream);
        // 获取第一页
        int i = 0;
        pdf.getPages().getCount();
        for (i = 0; i < pdf.getPages().getCount(); i++) {
            PdfPageBase page = pdf.getPages().get(i);
            getWaterMark(degree, configFont, configColor, watermarkValue, page);
        }
        // 保存至输出流
        pdf.saveToStream(outputStream);
        pdf.close();
    }

}
