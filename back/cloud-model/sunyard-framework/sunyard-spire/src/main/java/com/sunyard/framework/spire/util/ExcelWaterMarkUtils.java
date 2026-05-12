package com.sunyard.framework.spire.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.poi.hssf.record.crypto.Biff8EncryptionKey;
import org.apache.poi.hssf.usermodel.HSSFClientAnchor;
import org.apache.poi.hssf.usermodel.HSSFPatriarch;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ooxml.POIXMLDocumentPart;
import org.apache.poi.openxml4j.opc.PackagePartName;
import org.apache.poi.openxml4j.opc.PackageRelationship;
import org.apache.poi.openxml4j.opc.TargetMode;
import org.apache.poi.poifs.crypt.Decryptor;
import org.apache.poi.poifs.crypt.EncryptionInfo;
import org.apache.poi.poifs.crypt.EncryptionMode;
import org.apache.poi.poifs.crypt.Encryptor;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.Picture;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFRelation;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.Transparency;
import java.awt.RenderingHints;
import java.awt.BasicStroke;
import java.awt.AlphaComposite;
import java.awt.Font;
import java.awt.Color;
import java.awt.font.TextAttribute;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;

/**
 * excel水印工具类
 *
 * @author wangzezhou
 * @date 2023/08/25
 */
@Slf4j
public class ExcelWaterMarkUtils {

    /**
     * 宽度
     */
    private static final int WIDTH = 360;

    /**
     * 高度
     */
    private static final int HEIGHT = 150;

    /**
     * 水印横向位置
     */
    private static int positionWidth = 80;

    /**
     * 水印纵向位置
     */
    private static int positionHeight = 30;

    /**
     * 水印文字 字体类型、风格、大小
     * microsoft-yahei、微软雅黑、宋体
     */
    private static Font font = new Font("微软雅黑", Font.PLAIN, 20);

    /**
     * 设置水印图片路径
     */
    private static String imgPath = "E:\\水印.png";

    /**
     * 防止生产环境部署在docker或者linux服务器上，很有可能会引起中文水印乱码，成了方块的情况，这时候需要自己导入字体放到项目路径
     */
//    private static Font fontEncode = SystemLoadFont.styleFont(imgPath, Font.PLAIN, 30);

    /**
     * 水印文字颜色
     * 最后一个参数（第四个参数）为水印透明度
     */
    private static Color color = new Color(0, 0, 0, 80);

    /**
     * 根据文字生成水印图片
     *
     * @param watermarkValue 文印文字
     * @param font           字体
     * @param color          颜色
     * @param degree         角度
     * @return BufferedImage
     */
    private static BufferedImage createWaterMarkImage(String watermarkValue, Font font, Color color, Integer degree) {
        // 获取bufferedImage对象创建空白图片
        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);
        // 获取Graphics2d对象创建图片画笔
        Graphics2D g = image.createGraphics();
        // 设置背景透明度
        image = g.getDeviceConfiguration().createCompatibleImage(WIDTH, HEIGHT, Transparency.TRANSLUCENT);
        g.dispose();
        g = image.createGraphics();
        // 设置对线段的锯齿状边缘处理
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        // 文字处理
        AttributedString ats = new AttributedString(watermarkValue);
        ats.addAttribute(TextAttribute.FONT, font, 0, watermarkValue.length());
        AttributedCharacterIterator iter = ats.getIterator();
        // 水印旋转
        g.rotate(Math.toRadians(degree), (double) image.getWidth() / 2, (double) image.getHeight() / 2);
        // 设置水印文字颜色
        g.setColor(color);
        // 设置水印字体加粗
        g.setStroke(new BasicStroke(1));
        // 设置水印文字Font
        g.setFont(font);
        // 设置水印文字透明度
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));
        // 水印位置
        g.drawString(iter, positionHeight, positionWidth);
        // 释放资源
        g.dispose();
        return image;
    }

    /**
     * 为Excel打上水印工具函数 请自行确保参数值，以保证水印图片之间不会覆盖。
     *
     * @param wb              Excel Workbook
     * @param sheet           需要打水印的Excel
     * @param image           水印图片
     * @param startXCol       水印起始列
     * @param startYRow       水印起始行
     * @param betweenXCol     水印横向之间间隔多少列
     * @param betweenYRow     水印纵向之间间隔多少行
     * @param xCount          横向共有水印多少个
     * @param yCount          纵向共有水印多少个
     * @param waterMarkWidth  水印图片宽度为多少列
     * @param waterMarkHeight 水印图片高度为多少行
     * @throws IOException 异常
     */
    private static void putWaterMarkToExcel(Workbook wb, Sheet sheet, BufferedImage image, int startXCol,
                                            int startYRow, int betweenXCol, int betweenYRow, int xCount, int yCount, int waterMarkWidth,
                                            int waterMarkHeight) throws IOException {
        // 加载图片
        ByteArrayOutputStream byteArrayOut = new ByteArrayOutputStream();
        if (null == image) {
            throw new RuntimeException("向Excel上面打印水印，读取水印图片失败！");
        }
        ImageIO.write(image, "png", byteArrayOut);
        // 开始打水印
        Drawing drawing = sheet.createDrawingPatriarch();
        // 按照共需打印多少行水印进行循环
        for (int y = 0; y < yCount; y++) {
            // 按照每行需要打印多少个水印进行循环
            for (int x = 0; x < xCount; x++) {
                // 创建水印图片位置
                int xIndexInteger = startXCol + (x * waterMarkWidth) + (x * betweenXCol);
                int yIndexInteger = startYRow + (y * waterMarkHeight) + (y * betweenYRow);
                /*
                 * 参数定义： 第一个参数是（x轴的开始节点）； 第二个参数是（是y轴的开始节点）； 第三个参数是（是x轴的结束节点）；
                 * 第四个参数是（是y轴的结束节点）； 第五个参数是（是从Excel的第几列开始插入图片，从0开始计数）；
                 * 第六个参数是（是从excel的第几行开始插入图片，从0开始计数）； 第七个参数是（图片宽度，共多少列）；
                 * 第8个参数是（图片高度，共多少行）；
                 */
                ClientAnchor anchor =
                        drawing.createAnchor(0, 0, 0, 0, xIndexInteger, yIndexInteger, xIndexInteger + waterMarkWidth, yIndexInteger + waterMarkHeight);
                Picture pic = drawing.createPicture(anchor, wb.addPicture(byteArrayOut.toByteArray(), Workbook.PICTURE_TYPE_PNG));
                pic.resize();
            }
        }
    }

    /**
     * 获取水印
     *
     * @param inputStream    输入流
     * @param path           文件路径
     * @param font           字体
     * @param color          颜色
     * @param degree         角度
     * @param watermarkValue 内容
     */
    public static void getExcelWatermark(InputStream inputStream, String path, Font font, Color color, Integer degree,
                                         String watermarkValue) {
        // 写出数据输出流到页面
        try {
            XSSFWorkbook workbook = new XSSFWorkbook(inputStream);
            byte[] bytes = watermarkValue.getBytes(StandardCharsets.UTF_8);
            watermarkValue = new String(bytes, StandardCharsets.UTF_8);
            // 创建响应图片
            BufferedImage bufferedImage = createWaterMarkImage(watermarkValue, font, color, degree);
            int sheets = workbook.getNumberOfSheets();
            // 循环sheet给每个sheet添加水印
            for (int i = 0; i < sheets; i++) {
                Sheet sheet = workbook.getSheetAt(i);
                // 获取excel实际所占行
                int row = sheet.getFirstRowNum() + sheet.getLastRowNum();
                // 获取excel实际所占列
                int cell = sheet.getRow(sheet.getFirstRowNum()).getLastCellNum() + 1;
                for (int n = 0; n < cell; n++) {
                    sheet.autoSizeColumn((short) n, false);
                }
                // 根据行与列计算实际所需多少水印
                putWaterMarkToExcel(workbook, sheet, bufferedImage, 0, 0, 5, 5, cell / 5 + 1, row / 5 + 1, 0, 0);
            }
            FileOutputStream fos = new FileOutputStream(path);
            workbook.write(fos);
            workbook.close();
            fos.close();
        } catch (IOException e) {
            log.error("系统异常",e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 根据自定义属性添加水印，创建图片画笔覆盖
     *
     * @param inputStream    文件输入流
     * @param path           路径
     * @param font           字体
     * @param color          字体颜色
     * @param degree         倾斜度
     * @param watermarkValue 水印内容
     */
    public static void addExcelWaterMark(InputStream inputStream, String path, Font font, Color color, Integer degree,
                                         String watermarkValue, String password) {
        Workbook workbook = null;
        ByteArrayOutputStream os = null;
        EncryptionMode originalEncryptionMode = null; // 记录原文档加密模式

        try {
            // 读取水印文字
            String[] textArray = watermarkValue.split("\n");

            // 单元水印图片宽高
            int width = 800;
            int height = 400;

            // 生成水印图片
            BufferedImage image = createWatermarkImage(width, height, font, color, degree, textArray);

            // 将内存中的图片写入至输出流
            os = new ByteArrayOutputStream();
            ImageIO.write(image, "png", os);

            // 使用WorkbookFactory读取文档，同时记录加密模式
            if (password != null && !password.trim().isEmpty()) {
                // 对于加密文档，先获取加密模式
                POIFSFileSystem poifs = new POIFSFileSystem(inputStream);
                EncryptionInfo encryptionInfo = new EncryptionInfo(poifs);
                originalEncryptionMode = encryptionInfo.getEncryptionMode();

                // 解密
                Decryptor decryptor = Decryptor.getInstance(encryptionInfo);
                if (!decryptor.verifyPassword(password)) {
                    throw new IllegalArgumentException("Excel密码错误");
                }
                InputStream decryptedStream = decryptor.getDataStream(poifs);

                // 用解密后的流创建Workbook
                workbook = WorkbookFactory.create(decryptedStream);

            } else {
                // 非加密文档直接打开
                workbook = WorkbookFactory.create(inputStream);
            }

            // 根据Workbook类型处理水印
            if (workbook instanceof XSSFWorkbook) {
                processXlsx((XSSFWorkbook) workbook, os);
            } else if (workbook instanceof HSSFWorkbook) {
                processXls((HSSFWorkbook) workbook, os);
            } else {
                throw new IllegalArgumentException("不支持的workbook类型: " + workbook.getClass().getName());
            }

            // 保存文档（带密码加密）
            if (password != null && !password.trim().isEmpty()) {
                saveEncryptedExcel(workbook, path, password, originalEncryptionMode);
            } else {
                // 无密码直接保存
                try (FileOutputStream fileOut = new FileOutputStream(path)) {
                    workbook.write(fileOut);
                }
            }

        } catch (Exception e) {
            log.error("添加水印失败", e);
            throw new RuntimeException("为Excel文件添加水印失败", e);
        } finally {
            IOUtils.closeQuietly(os);
            IOUtils.closeQuietly(inputStream);
            IOUtils.closeQuietly(workbook);
        }
    }

    /**
     * 带密码保存Excel文档（重新加密）
     */
    private static void saveEncryptedExcel(Workbook workbook, String destPath, String password,
                                           EncryptionMode originalMode) throws IOException, GeneralSecurityException {
        // 1. 将处理后的文档写入临时字节流
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            workbook.write(baos);
            baos.flush();

            // 2. 根据文档类型选择加密方式
            if (workbook instanceof XSSFWorkbook) {
                // .xlsx 文档加密（使用POI的加密API）
                try (POIFSFileSystem fs = new POIFSFileSystem();
                     FileOutputStream fos = new FileOutputStream(destPath)) {

                    // 优先使用原加密模式，否则默认agile（适合.xlsx）
                    EncryptionMode mode = (originalMode != null) ? originalMode : EncryptionMode.agile;
                    EncryptionInfo info = new EncryptionInfo(mode);

                    Encryptor encryptor = info.getEncryptor();
                    encryptor.confirmPassword(password);

                    // 写入加密内容
                    try (OutputStream encryptedOs = encryptor.getDataStream(fs)) {
                        encryptedOs.write(baos.toByteArray());
                    }

                    // 保存加密文件
                    fs.writeFilesystem(fos);
                }
            } else if (workbook instanceof HSSFWorkbook) {
                // .xls 文档加密（使用Biff8加密）
                try (FileOutputStream fos = new FileOutputStream(destPath)) {
                    // 设置密码（.xls使用的是RC4加密）
                    Biff8EncryptionKey.setCurrentUserPassword(password);
                    workbook.write(fos);
                } finally {
                    // 清除线程级别的密码缓存
                    Biff8EncryptionKey.setCurrentUserPassword(null);
                }
            }
        }
    }

    private static BufferedImage createWatermarkImage(int width, int height, Font font, Color color, Integer degree, String[] textArray) {
        // 缓冲区图片对象
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        // 基于特定宽高，创建图形对象
        Graphics2D g = image.createGraphics();
        // 基于图形对象生成半透明图片
        image = g.getDeviceConfiguration().createCompatibleImage(width, height, Transparency.TRANSLUCENT);
        // 释放画笔
        g.dispose();

        // 基于半透明图片，创建图形对象
        g = image.createGraphics();
        // 设置图形颜色
        g.setColor(color);
        // 设置图形字体
        g.setFont(font);
        // 设置图形倾斜度
        BigDecimal bigDecimal = new BigDecimal(degree);
        g.shear(0.0, bigDecimal.divide(BigDecimal.valueOf(100), 2, BigDecimal.ROUND_HALF_UP).doubleValue());
        // 设置字体平滑
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 画出字符串
        int y = 100;
        int x = 100;
        for (int i = 0; i < textArray.length; i++) {
            g.drawString(textArray[i], x, y);
            y = y + font.getSize();
        }

        // 释放画笔
        g.dispose();
        return image;
    }

    private static void processXlsx(XSSFWorkbook workbook, ByteArrayOutputStream os) throws IOException {
        // 将图片输出流写入Excel
        int pictureIdx = workbook.addPicture(os.toByteArray(), Workbook.PICTURE_TYPE_PNG);
        // 获取工作簿中的图片对象
        POIXMLDocumentPart poixmlDocumentPart = workbook.getAllPictures().get(pictureIdx);

        for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
            // 获取每个Sheet表
            XSSFSheet sheet = workbook.getSheetAt(i);
            PackagePartName ppn = poixmlDocumentPart.getPackagePart().getPartName();
            String relType = XSSFRelation.IMAGES.getRelation();
            // 为当前Sheet添加图片关系
            PackageRelationship pr = sheet.getPackagePart().addRelationship(ppn, TargetMode.INTERNAL, relType, null);
            // 将图片关系添加到Sheet底层结构中
            sheet.getCTWorksheet().addNewPicture().setId(pr.getId());
        }
    }

    private static void processXls(HSSFWorkbook workbook, ByteArrayOutputStream os) throws IOException {
        // 添加图片
        int pictureIdx = workbook.addPicture(os.toByteArray(), Workbook.PICTURE_TYPE_PNG);

        for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
            // 获取每个Sheet表
            HSSFSheet sheet = workbook.getSheetAt(i);
            // 创建绘图对象
            HSSFPatriarch patriarch = sheet.createDrawingPatriarch();

            // 创建一个占位符
            HSSFClientAnchor anchor = new HSSFClientAnchor(0, 0, 255, 255, (short) 0, 0, (short) 10, 10);
            anchor.setAnchorType(ClientAnchor.AnchorType.MOVE_DONT_RESIZE);

            // 创建图片
            patriarch.createPicture(anchor, pictureIdx);
        }
    }

}
