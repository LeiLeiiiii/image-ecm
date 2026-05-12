package com.sunyard.edm.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

/**
 * @Author PJW 2022/12/12 14:21
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class DocBsShapeMeToDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
	 * 分享id
	 */
    private Long shapeId;

    /**
	 * 分享类别（0内部 1外部）
	 */
    private Integer shapeType;

    /**
	 * 分享类别（0内部 1外部）
	 */
    private String shapeTypeStr;

    /**
	 * 文档id
	 */
    private Long docId;

    /**
	 * 文档名称
	 */
    private String docName;

    /**
	 * 前端所用，是文件夹还是文档，分享默认都是文档
	 */
    private Integer type;

    /**
	 * 文档类型后缀
	 */
    private String docSuffix;

    /**
	 * 文档的大小
	 */
    private String docSize;

    /**
	 * 文档的大小Str
	 */
    private String docSizeStr;

    /**
	 * 分享时间
	 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date shapeTime;

    /**
	 * 分享预览次数
	 */
    private Integer shapePreview;

    /**
	 * 分享状态
	 */
    private Integer shapeState;

    /**
	 * 分享区间（0：3天 1：7天 2：永久
	 */
    private Integer shapeSection;

    /**
	 * 到期时间
	 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date invalidTime;

    /**
	 * 是否置灰
	 */
    private Boolean isAsh;

    /**
	 * 是否回收
	 */
    private Boolean isRecovery;

    /**
	 * 是否下架
	 */
    private Boolean isNoSelf;

}
