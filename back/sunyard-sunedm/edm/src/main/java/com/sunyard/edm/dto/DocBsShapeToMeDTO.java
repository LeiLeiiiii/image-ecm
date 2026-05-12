package com.sunyard.edm.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 * 分享-文档关联表
 * </p>
 *
 * @author pjw
 * @since 2022-12-12
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class DocBsShapeToMeDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
	 * 分享id
	 */
    private Long shapeId;

    /**
	 * 文档id
	 */
    private Long docId;

    /**
	 * 前端所用，是文件夹还是文档，分享默认都是文档
	 */
    private Integer type;

    /**
	 * 文档类型后缀
	 */
    private String docSuffix;

    /**
	 * 文档名称
	 */
    private String docName;

    /**
	 * 文档的大小
	 */
    private String docSize;

    /**
	 * 文档的大小Str
	 */
    private String docSizeStr;

    /**
	 * 分享者
	 */
    private String shapeUserName;

    /**
	 * 分享时间
	 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date shapeTime;

    /**
	 * 分享状态 0有效 1失效
	 */
    private Integer shapeState;

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
    /**
	 * 分享者id
	 */
    private Long shapeUserId;
}
