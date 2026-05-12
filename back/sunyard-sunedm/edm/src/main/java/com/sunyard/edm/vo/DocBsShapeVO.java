package com.sunyard.edm.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @Author PJW 2022/12/12 15:04
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class DocBsShapeVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
	 * 分享类别 0内部 1外部
	 */
    private Integer shapeType;

    /**
	 * 文档名称
	 */
    private String docName;

    /**
	 * 文档类别
	 */
    private List<Integer> docType;

    /**
	 * 分享状态 0有效 1失效
	 */
    private List<Integer> shapeState;

    /**
	 * 分享时间排序 与到期时间二选一
	 */
    private String shapeTimeSort;

    /**
	 * 到期时间排序 与分享时间二选一
	 */
    private String invalidTimeSort;

    /**
	 * 分享者
	 */
    private String sharer;

    /**
	 * 标签id集合
	 */
    private List<Long> tagIdList;

    /**
	 * 分享日期 左区间
	 */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date shapeTimeTo;

    /**
	 * 分享日期 右区间
	 */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date shapeTimeDo;

    /**
	 * 用户id 后端自动获取
	 */
    private Long userId;

    /**
	 * 部门id 后端自动获取
	 */
    private Long deptId;

    /**
	 * 机构id 后端自动获取
	 */
    private Long instId;

    private List<Long> userIds;

}
