package com.sunyard.framework.elasticsearch.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.easyes.annotation.HighLight;

import java.io.Serializable;

/**
 * @author P-JWei
 * @date 2023/10/8 15:40:56
 * @title 基础文件数据obj
 * @description
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class BaseFileObjEs implements Serializable {

    /**
     * 业务来源（内部服务模块系统号：acc\ent\ecm...）
     */
    private String baseBizSource;

    /**
     * 业务流水号（由业务系统生成）
     */
    private Long baseBizSourceId;

    /**
     * 文件id
     */
    private String fileId;

    /**
     * 文件名
     */
    private String fileName;

    /**
     * 文件后缀
     */
    private String fileSuffix;

    /**
     * 标题（文本类文件可提取赋值）
     */
    @HighLight(preTag = "<span style=\'color:red\'>", postTag = "</span>",fragmentSize = 100,numberOfFragments = 5)
    private String title;

    /**
     * 摘要（文本类文件可提取赋值）
     */
    private String abstracts;

    /**
     * 可交换图像文件格式（图片视频类可提取赋值）
     */
    private String exif;

    /**
     * ocr识别信息（图片视频类可提取赋值）
     */
    private String ocrInfo;

    /**
     * 文件转化成base64编码后所有的内容。
     * (
     * 文本文件在走ES，会通过attachment管道
     * 预处理成指定的信息。
     * )
     */
    @HighLight(preTag = "<span style='color:red'>", postTag = "</span>",fragmentSize = 100,numberOfFragments = 5)
    private String content;

    /**
     * 文件具体信息
     */
    private BaseAttachment attachment;

}
