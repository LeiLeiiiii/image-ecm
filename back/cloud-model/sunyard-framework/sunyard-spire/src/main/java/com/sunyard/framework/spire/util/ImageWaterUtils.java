package com.sunyard.framework.spire.util;

import cn.hutool.core.util.ObjectUtil;
import lombok.extern.slf4j.Slf4j;
import sun.font.FontDesignMetrics;

import javax.imageio.ImageIO;
import javax.swing.JLabel;
import java.awt.Font;
import java.awt.Color;
import java.awt.Image;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.FontMetrics;
import java.awt.AlphaComposite;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/**
 * @author tqf
 * @version 创建时间：2020-4-3 上午10:49:02 类说明:图片添加水印 文字&图片水印
 */
@Slf4j
public class ImageWaterUtils {
    /**
     * 水印之间的间隔
     */
    private static final int YMOVE = 120;
    /**
     * 水印之间的间隔
     */
    private static final int XMOVE = 80;

    /**
     * 水印透明度
     */
    private static float alpha = 0.5f;
    /**
     * 水印横向位置
     */
    private static int positionWidth = 150;
    /**
     * 水印纵向位置
     */
    private static int positionHeight = 300;
    /**
     * 水印文字字体
     */
    private static Font font = new Font("宋体", Font.BOLD, 22);
    /**
     * 水印文字颜色
     */
    private static Color color = new Color(180, 180, 182);

    /**
     * @param alpha          水印透明度
     * @param positionWidth  水印横向位置
     * @param positionHeight 水印纵向位置
     * @param font           水印文字字体
     * @param color          水印文字颜色
     */
    public static void setImageMarkOptions(float alpha, int positionWidth, int positionHeight, Font font, Color color) {
        Float af = new Float(alpha);
        Float bf = new Float(0.0f);

        if (af.compareTo(bf) != 0) {
            ImageWaterUtils.alpha = alpha;
        }
        if (positionWidth != 0) {
            ImageWaterUtils.positionWidth = positionWidth;
        }
        if (positionHeight != 0) {
            ImageWaterUtils.positionHeight = positionHeight;
        }
        if (font != null) {
            ImageWaterUtils.font = font;
        }
        if (color != null) {
            ImageWaterUtils.color = color;
        }
    }

    /**
     * 给图片添加水印文字、可设置水印文字的旋转角度
     *
     * @param logoText   水印内容
     * @param srcImgPath 图片文件流
     * @param degree     偏移度
     */
    public static void markImageByText(String logoText, InputStream srcImgPath, OutputStream outputStream,
                                       Integer degree) {

        if (degree == null) {
            degree = 10;
        }
        InputStream is = null;
        try {
            // 1、源图片
            Image srcImg = ImageIO.read(srcImgPath);
            BufferedImage buffImg =
                    new BufferedImage(srcImg.getWidth(null), srcImg.getHeight(null), BufferedImage.TYPE_INT_RGB);
            // 2、得到画笔对象
            Graphics2D g = buffImg.createGraphics();
            // 3、设置对线段的锯齿状边缘处理
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.drawImage(srcImg.getScaledInstance(srcImg.getWidth(null), srcImg.getHeight(null), Image.SCALE_SMOOTH), 0,
                    0, null);
            // 4、设置水印旋转
            g.rotate(Math.toRadians(degree), (double) buffImg.getWidth() / 2, (double) buffImg.getHeight() / 2);
            // 5、设置水印文字颜色
            g.setColor(color);
            // 6、设置水印文字Font
            g.setFont(new Font("宋体", Font.BOLD, 20));
            // 7、设置水印文字透明度
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, alpha));
            // 获取图片的宽
            int srcImgWidth = srcImg.getWidth(null);
            // 获取图片的高
            int srcImgHeight = srcImg.getHeight(null);
            JLabel label = new JLabel(logoText);
            FontMetrics metrics = label.getFontMetrics(font);
            // 文字水印的宽
            int width = metrics.stringWidth(label.getText()) + XMOVE;
            // 图片的高 除以 文字水印的宽 ——> 打印的行数(以文字水印的宽为间隔)
            int rowsNumber = srcImgHeight / width;
            // 图片的宽 除以 文字水印的宽 ——> 每行打印的列数(以文字水印的宽为间隔)
            int columnsNumber = srcImgWidth / width;
            // 防止图片太小而文字水印太长，所以至少打印一次
            /*
            if(rowsNumber < 1){
                rowsNumber = 1;
            }
            if(columnsNumber < 1){
                columnsNumber = 1;
            }*/
            for (int j = 0; j < rowsNumber; j++) {
                for (int i = 0; i < columnsNumber; i++) {
                    // 画出水印,并设置水印位置
                    g.drawString(logoText, i * width + j * width, -i * width + j * width + YMOVE);
                }
            }
            // 9、释放资源
            g.dispose();
            // 10、生成图片
            ImageIO.write(buffImg, "JPG", outputStream);
        } catch (Exception e) {
            log.error("系统异常",e);
            throw new RuntimeException(e);
        } finally {
            if (null != is) {
                try {
                    is.close();
                } catch (IOException e) {
                }
            }
        }
    }


    /**
     * 添加水印
     * @param srcImgInputStream 图片输入流
     * @param tempPath 临时地址
     * @param degree 角度
     * @param configFont 字体
     * @param logoText 内容
     * @param configColor 颜色
     * @param ext 文件后缀
     */
    public static void markImgByConfig(InputStream srcImgInputStream, String tempPath,
                                       Integer degree, Font configFont,
                                       String logoText, Color configColor, String ext) {
        if (degree == null) {
            degree = 0;
        }
        InputStream is = null;
        OutputStream os = null;
        try {
            // 1、源图片
            Image srcImg = ImageIO.read(srcImgInputStream);
            BufferedImage buffImg =
                    new BufferedImage(srcImg.getWidth(null), srcImg.getHeight(null), BufferedImage.TYPE_INT_RGB);
            // 2、得到画笔对象
            Graphics2D g = buffImg.createGraphics();
            // 3、设置对线段的锯齿状边缘处理
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.drawImage(srcImg.getScaledInstance(srcImg.getWidth(null), srcImg.getHeight(null), Image.SCALE_SMOOTH), 0,
                    0, null);
            // 4、设置水印旋转
            g.rotate(Math.toRadians(degree), (double) buffImg.getWidth() / 2, (double) buffImg.getHeight() / 2);
            // 5、设置水印文字颜色
            g.setColor(configColor);
            // 6、设置水印文字Font
            g.setFont(configFont);
            // 7、设置水印文字透明度
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, alpha));
            // 获取图片的宽
            int srcImgWidth = srcImg.getWidth(null);
            // 获取图片的高
            int srcImgHeight = srcImg.getHeight(null);
            //9、设置文字位置
            FontDesignMetrics metrics = FontDesignMetrics.getMetrics(g.getFont());
            //获取文字宽度
            int strWidth = metrics.stringWidth(logoText);

            int xNum = srcImgWidth / strWidth + 1;

            int yNum = srcImgHeight / 50 + 1;

            int split = 50;

            for (int i = 1; i <= 2 * yNum; i++) {
                int y = -srcImgHeight + 50 * i + 5 * split * i;
                for (int j = 0; j < xNum; j++) {
                    int x = strWidth * j + 3 * split * j;
                    g.drawString(logoText, x, y);
                }
            }

            // 9、释放资源
            g.dispose();
            // 10、生成图片
            ImageIO.write(buffImg, ext, new File(tempPath));
        } catch (Exception e) {
            log.error("系统异常",e);
            throw new RuntimeException(e);
        } finally {
            try {
                if (null != is) {
                    is.close();
                }
                if (null != os) {
                    os.close();
                }
                if (ObjectUtil.isNotEmpty(srcImgInputStream)) {
                    srcImgInputStream.close();
                }
            } catch (Exception e) {
                log.error("系统异常",e);
                throw new RuntimeException(e);
            }
        }
    }


    /**
     * 获取字符串占用的宽度
     *
     * @param str      字符串
     * @param fontSize 文字大小
     * @return Result 字符串占用的宽度
     */
    public static int getStrWidth(String str, int fontSize) {
        char[] chars = str.toCharArray();
        int fontSize2 = fontSize / 2;
        int width = 0;
        for (char c : chars) {
            int len = String.valueOf(c).getBytes(StandardCharsets.UTF_8).length;
            // 汉字为3,其余1
            // 可能还有一些特殊字符占用2等等,统统计为汉字
            if (len != 1) {
                width += fontSize;
            } else {
                width += fontSize2;
            }
        }
        return width;
    }

}
