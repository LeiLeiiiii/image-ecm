package com.sunyard.edm.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @Author PJW 2022/12/12 15:27
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class DocBsCollectionVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 文档名称
     */
    private String docName;

    /**
     * 文档类别
     */
    private List<Integer> docType;

    /**
     * 更新时间排序 与收藏时间二选一
     */
    private String updateTimeSort;

    /**
     * 收藏时间排序 与更新时间二选一
     */
    private String collectionTimeSort;

    /**
     * 所有者
     */
    private String owner;

    /**
     * 标签id集合
     */
    private List<Long> tagIdList;

    /**
     * 收藏日期 左区间
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date collectionTimeTo;

    /**
     * 收藏日期 右区间
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date collectionTimeDo;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 用户部门
     */
    private Long deptId;

    /**
     * 用户机构
     */
    private Long instId;
}
