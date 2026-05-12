package com.sunyard.edm.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.List;

/**
 * @Author PJW 2022/12/12 15:13
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class DocBsShapeAddVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
	 * 文档id集合
	 */
    private List<Long> docIdList;

    /**
	 * 分享类别（0内部 1外部）
	 */
    private Integer shapeType;

    /**
	 * 是否消息通知（0通知 1不通知）
	 */
    private Integer isMsg;

    /**
	 * 分享对象 仅‘内部分享’传值
	 */
    private List<DocBsShapeAcceptVO> shapeAcceptList;

    /**
	 * 分享区间（0：3天 1：7天 2：永久）
	 */
    private Integer shapeSection;

    /**
	 * 分享形式 0公开 1密码 仅‘外部分享’传值
	 */
    private Integer shapeLinkType;
}
