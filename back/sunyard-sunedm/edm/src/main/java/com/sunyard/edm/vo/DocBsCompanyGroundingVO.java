package com.sunyard.edm.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @Author PJW 2022/12/27 14:17
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class DocBsCompanyGroundingVO implements Serializable {


    private static final long serialVersionUID = 1L;

    /**
	 * table类型 0待上架 1已下架
	 */
    private Integer type;

    /**
	 * 文档名称
	 */
    private String docName;

    /**
	 * 文档类别
	 */
    private List<Integer> docType;

    /**
	 * 审核状态
	 */
    private List<Integer> approvalStatus;

    /**
	 * 上传时间排序 与收藏时间二选一
	 */
    private String uploadTimeSort;

    /**
	 * 下架时间排序 与更新时间二选一
	 */
    private String lowerTimeSort;

    /**
	 * 所有者
	 */
    private String owner;

    /**
	 * 标签id集合
	 */
    private List<Long> tagIdList;

    /**
	 * 上传日期 左区间
	 */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date uploadTimeTo;

    /**
	 * 上传日期 右区间
	 */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date uploadTimeDo;

    /**
	 * 下架日期 左区间
	 */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date lowerTimeTo;

    /**
	 * 下架日期 右区间
	 */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date lowerTimeDo;

    private Long userId;
    private Long deptId;
    private Long instId;
}
