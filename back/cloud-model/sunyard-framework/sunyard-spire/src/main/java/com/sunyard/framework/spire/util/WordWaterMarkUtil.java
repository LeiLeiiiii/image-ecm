package com.sunyard.framework.spire.util;

import java.awt.Color;
import java.awt.Font;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.util.stream.Stream;

import org.apache.poi.poifs.crypt.Decryptor;
import org.apache.poi.poifs.crypt.EncryptionInfo;
import org.apache.poi.poifs.crypt.EncryptionMode;
import org.apache.poi.poifs.crypt.Encryptor;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.wp.usermodel.HeaderFooterType;
import org.apache.poi.xwpf.model.XWPFHeaderFooterPolicy;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFHeader;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTP;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTPPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTPicture;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTR;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTRPr;

import com.microsoft.schemas.office.office.CTLock;
import com.microsoft.schemas.vml.CTGroup;
import com.microsoft.schemas.vml.CTShape;
import com.microsoft.schemas.vml.CTShapetype;
import com.microsoft.schemas.vml.CTTextPath;
import com.microsoft.schemas.vml.STExt;
import com.microsoft.schemas.vml.STTrueFalse;

import lombok.extern.slf4j.Slf4j;

/**
 * @Author lyq
 * @Description
 * @Date 2023/8/9 14:30
 */
@Slf4j
public class WordWaterMarkUtil {

    /**
     * word字体
     */
    private static final String FONT_NAME = "宋体";
    private static final int WIDTH_PER_WORD = 10;


    /**
     * doc添加水印  冰蓝无法做出要的效果，改用poi处理
     *
     * @param srcImgPath     文件输入流
     * @param path           临时存储路径
     * @param degree         水印旋转度
     * @param configFont     字体配置
     * @param configColor    字体颜色
     * @param watermarkValue 水印内容
     */
    public static void addWaterMarkByConfig(InputStream srcImgPath, String path,
                                            Integer degree, Font configFont,
                                            Color configColor, String watermarkValue,
                                            String password) {
        XWPFDocument doc = null;

        try {
            // 如果有密码，使用解密方式
            if (password != null && !password.trim().isEmpty()) {
                doc = openEncryptedDocument(srcImgPath, password);
            } else {
                // 无密码，直接打开
                doc = new XWPFDocument(srcImgPath);
            }

            String color = String.format("#%02x%02x%02x",
                    configColor.getRed(), configColor.getGreen(), configColor.getBlue());

            makeWaterMarkByWordArt(doc, watermarkValue, color,
                    String.valueOf(configFont.getSize()), String.valueOf(degree));

            if (password != null && !password.trim().isEmpty()) {
                saveEncryptedDocument(doc, path, password);
            } else {
                // 无密码直接保存
                try (OutputStream os = new FileOutputStream(path)) {
                    doc.write(os);
                }
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (doc != null) {
                try {
                    doc.close();
                } catch (IOException e) {
                    log.error("关闭流异常:",e);
                }
            }
        }
    }

    /**
     * 解密并打开带密码的Word文档
     */
    private static XWPFDocument openEncryptedDocument(InputStream encryptedStream, String password)
            throws Exception {
        POIFSFileSystem fs = new POIFSFileSystem(encryptedStream);
        EncryptionInfo info = new EncryptionInfo(fs);
        Decryptor decryptor = Decryptor.getInstance(info);

        if (!decryptor.verifyPassword(password)) {
            throw new IllegalArgumentException("文档密码错误");
        }

        try (InputStream decryptedStream = decryptor.getDataStream(fs)) {
            return new XWPFDocument(decryptedStream);
        }
    }

    /**
     * 带密码保存文档（重新加密）
     */
    private static void saveEncryptedDocument(XWPFDocument doc, String destPath, String password)
            throws IOException, GeneralSecurityException {
        // 1. 将文档写入临时字节流
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            doc.write(baos);
            baos.flush();

            // 2. 创建加密输出流
            try (POIFSFileSystem fs = new POIFSFileSystem();
                 FileOutputStream fos = new FileOutputStream(destPath)) {

                // 3. 配置加密参数
                EncryptionInfo info = new EncryptionInfo(EncryptionMode.agile);
                Encryptor encryptor = info.getEncryptor();
                encryptor.confirmPassword(password);

                // 4. 写入加密内容
                try (OutputStream encryptedOs = encryptor.getDataStream(fs)) {
                    encryptedOs.write(baos.toByteArray());
                }

                // 5. 保存加密后的文件
                fs.writeFilesystem(fos);
            }
        }
    }

    /**
     * word文字水印(调用poi封装的createWatermark方法)
     *
     * @param doc     XWPFDocument对象
     * @param markStr 水印文字
     */
    public static void setWordWaterMark(XWPFDocument doc, String markStr, String fontColor) {
        XWPFParagraph paragraph = doc.createParagraph();
        XWPFHeaderFooterPolicy headerFooterPolicy = doc.getHeaderFooterPolicy();
        if (headerFooterPolicy == null) {
            headerFooterPolicy = doc.createHeaderFooterPolicy();
        }
        // create default Watermark - fill color black and not rotated
        headerFooterPolicy.createWatermark(markStr);
        // get the default header
        // Note: createWatermark also sets FIRST and EVEN headers
        // but this code does not updating those other headers
        XWPFHeader header = headerFooterPolicy.getHeader(XWPFHeaderFooterPolicy.DEFAULT);
        paragraph = header.getParagraphArray(0);
//            // get com.microsoft.schemas.vml.CTShape where fill color and rotation is set
        paragraph.getCTP().newCursor();
        org.apache.xmlbeans.XmlObject[] xmlobjects = paragraph.getCTP().getRArray(0).getPictArray(0).selectChildren(
                new javax.xml.namespace.QName("urn:schemas-microsoft-com:vml", "shape"));
        if (xmlobjects.length > 0) {
            CTShape ctshape = (CTShape) xmlobjects[0];
            ctshape.setFillcolor(fontColor);
            ctshape.setStyle(ctshape.getStyle() + ";rotation:315");
        }
    }

    /**
     * 以艺术字方式加上水印(平铺)
     *
     * @param docx       XWPFDocument对象
     * @param customText 水印文字
     */
    public static void makeFullWaterMarkByWordArt(XWPFDocument docx, String customText, String fontColor, String fontSize, String styleRotation) {
        // 水印文字之间使用8个空格分隔
        customText = customText + repeatString(" ", 16);
        // 一行水印重复水印文字次数
        customText = repeatString(customText, 10);
        // 与顶部的间距
        String styleTop = "0pt";
        if (docx == null) {
            return;
        }
        // 遍历文档，添加水印
        for (int lineIndex = -10; lineIndex < 20; lineIndex++) {
            waterMarkDocXDocument(docx, customText, styleTop, 1, fontColor, fontSize, styleRotation);
        }
    }

    /**
     * 以艺术字方式加上水印(单个)
     *
     * @param docx       XWPFDocument对象
     * @param customText 水印文字
     */
    public static void makeWaterMarkByWordArt(XWPFDocument docx, String customText, String fontColor, String fontSize, String rotation) {
        // 与顶部的间距
        String styleTop = "0pt";
        if (docx == null) {
            return;
        }
        // 添加水印
        waterMarkDocXDocument(docx, customText, styleTop, 2, fontColor, fontSize, rotation);
    }

    /**
     * 将指定的字符串重复repeats次.
     *
     * @param pattern 字符串
     * @param repeats 重复次数
     * @return Result 生成的字符串
     */
    private static String repeatString(String pattern, int repeats) {
        StringBuilder buffer = new StringBuilder(pattern.length() * repeats);
        Stream.generate(() -> pattern).limit(repeats).forEach(buffer::append);
        return new String(buffer);
    }

    /**
     * 为文档添加水印
     * 实现参考了{@link XWPFHeaderFooterPolicy#(String, int)}
     *
     * @param doc        需要被处理的docx文档对象
     * @param customText 水印文本
     * @param type       类型：1.平铺；2.单个
     */
    private static void waterMarkDocXDocument(XWPFDocument doc, String customText, String styleTop, int type,
                                              String fontColor, String fontSize, String rotation) {
        // 如果之前已经创建过 DEFAULT 的Header，将会复用之
        XWPFHeader header = doc.createHeader(HeaderFooterType.DEFAULT);
        int size = header.getParagraphs().size();
        if (size == 0) {
            header.createParagraph();
        }
        CTP ctp = header.getParagraphArray(0).getCTP();
        byte[] rsidr = doc.getDocument().getBody().getPArray(0).getRsidR();
        byte[] rsidrdefault = doc.getDocument().getBody().getPArray(0).getRsidRDefault();
        ctp.setRsidP(rsidr);
        ctp.setRsidRDefault(rsidrdefault);
        CTPPr ppr = ctp.addNewPPr();
        ppr.addNewPStyle().setVal("Header");
        // 开始加水印
        CTR ctr = ctp.addNewR();
        CTRPr ctrpr = ctr.addNewRPr();
        ctrpr.addNewNoProof();
        CTGroup group = CTGroup.Factory.newInstance();
        CTShapetype shapetype = group.addNewShapetype();
        CTTextPath shapeTypeTextPath = shapetype.addNewTextpath();
        shapeTypeTextPath.setOn(STTrueFalse.T);
        shapeTypeTextPath.setFitshape(STTrueFalse.T);
        CTLock lock = shapetype.addNewLock();
        lock.setExt(STExt.VIEW);
        CTShape shape = group.addNewShape();
        shape.setId("PowerPlusWaterMarkObject");
        shape.setSpid("_x0000_s102");
        shape.setType("#_x0000_t136");
        if (type == 1) {
            // 设置形状样式（旋转，位置，相对路径等参数）
            shape.setStyle(getShapeStyle(customText, styleTop, rotation));
        } else {
            // 设置形状样式（旋转，位置，相对路径等参数）
            shape.setStyle(getShapeStyle(customText, rotation));
        }
        shape.setFillcolor(fontColor);
        // 字体设置为实心
        shape.setStroked(STTrueFalse.FALSE);
        // 绘制文本的路径
        CTTextPath shapeTextPath = shape.addNewTextpath();
        // 设置文本字体与大小
        shapeTextPath.setStyle("font-family:" + FONT_NAME + ";font-size:" + fontSize);
        shapeTextPath.setString(customText);
        CTPicture pict = ctr.addNewPict();
        pict.set(group);
    }

    /**
     * 加载docx格式的word文档
     *
     * @param inputStream 输入流
     * @return Result
     */
    private static XWPFDocument loadDocXDocument(InputStream inputStream) {
        XWPFDocument doc;
        try {
            doc = new XWPFDocument(inputStream);
        } catch (Exception e) {
            log.error("系统异常", e);
            throw new RuntimeException(e);
        }
        return doc;
    }

    /**
     * 构建Shape的样式参数
     *
     * @param customText 水印文本
     * @return Result
     */
    private static String getShapeStyle(String customText, String styleTop, String styleRotation) {
        StringBuilder sb = new StringBuilder();
        // 文本path绘制的定位方式
        sb.append("position: ").append("absolute");
        // 计算文本占用的长度（文本总个数*单字长度）
        sb.append(";width: ").append(customText.length() * WIDTH_PER_WORD).append("pt");
        // 字体高度
        sb.append(";height: ").append("20pt");
        sb.append(";z-index: ").append("-251654144");
        sb.append(";mso-wrap-edited: ").append("f");
        sb.append(";margin-top: ").append(styleTop);
        sb.append(";mso-position-horizontal-relative: ").append("margin");
        sb.append(";mso-position-vertical-relative: ").append("margin");
        sb.append(";mso-position-vertical: ").append("left");
        sb.append(";mso-position-horizontal: ").append("center");
        sb.append(";rotation: ").append(styleRotation);
        return sb.toString();
    }

    /**
     * 构建Shape的样式参数
     *
     * @param customText 水印文本
     * @return Result
     */
    private static String getShapeStyle(String customText, String styleRotation) {
        StringBuilder sb = new StringBuilder();
        // 文本path绘制的定位方式
        sb.append("position: ").append("absolute");
        // 计算文本占用的长度（文本总个数*单字长度）
        sb.append(";width: ").append(customText.length() * WIDTH_PER_WORD).append("pt");
        // 字体高度
        sb.append(";height: ").append("20pt");
        sb.append(";z-index: ").append("-251654144");
        sb.append(";mso-wrap-edited: ").append("f");
        sb.append(";margin-top: ").append("270pt");
        sb.append(";mso-position-horizontal-relative: ").append("margin");
        sb.append(";mso-position-vertical-relative: ").append("margin");
        sb.append(";mso-position-vertical: ").append("left");
        sb.append(";mso-position-horizontal: ").append("center");
        sb.append(";rotation: ").append(styleRotation);
        return sb.toString();
    }

    /**
     * 构建Shape的样式参数
     *
     * @return Result
     */
    private static String getShapeStyle() {
        StringBuilder sb = new StringBuilder();
        // 文本path绘制的定位方式
        sb.append("position: ").append("absolute");
        sb.append(";left: ").append("opt");
        // 计算文本占用的长度（文本总个数*单字长度）
        sb.append(";width: ").append("500pt");
        // 字体高度
        sb.append(";height: ").append("150pt");
        sb.append(";z-index: ").append("-251654144");
        sb.append(";mso-wrap-edited: ").append("f");
        sb.append(";margin-left: ").append("-50pt");
        sb.append(";margin-top: ").append("270pt");
        sb.append(";mso-position-horizontal-relative: ").append("margin");
        sb.append(";mso-position-vertical-relative: ").append("margin");
        sb.append(";mso-width-relative: ").append("page");
        sb.append(";mso-height-relative: ").append("page");
        sb.append(";rotation: ").append("-2949120f");
        return sb.toString();
    }

}
