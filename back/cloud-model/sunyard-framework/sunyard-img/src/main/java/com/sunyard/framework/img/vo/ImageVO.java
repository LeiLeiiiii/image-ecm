package com.sunyard.framework.img.vo;

import lombok.Data;

import java.io.Serializable;

 /**
 * @author zhouleibin
 * 图像信息
 */
@Data
public class ImageVO implements Serializable {
    /**
     * 写入图片路径
     */
    private String imgUrlInput;
    /**
     * 输出图片路径（如果为空，将覆盖写入图片路径）
     */
    private String imgUrlOut;
    /**
     * 左上x
     */
    private float ltX;
    /**
     * 左上y
     */
    private float ltY;
    /**
     * 右上x
     */
    private float rtX;
    /**
     * 右上y
     */
    private float rtY;
    /**
     * 左下x
     */
    private float lbX;
    /**
     * 左下y
     */
    private float lbY;
    /**
     * 右下x
     */
    private float rbX;
    /**
     * 右下y
     */
    private float rbY;
    /**
     * 图像宽
     */
    private float imgWidth;
    /**
     * 图像高
     */
    private float imgHeight;
}
