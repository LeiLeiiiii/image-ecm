package com.sunyard.edm.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
public class DocBsDocumentSearchDTO {

    private Integer isDeleted;

    /**
     * 所有者
     */
    private Long docOwner;
    /**
     * 文档名称
     */
    private String docName;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date delStartDate;
    /**
     * 结束时间
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date delEndDate;

    /**
     * 前端筛选
     */
    List<Long> tagId;

    /**
     * 上传时间排序 0升序 1降序
     */
    private String delTimeSort;

    /**
     * 剩余保留时间排序 0升序 1降序
     */
    private String recycleDateSort;

    /**
     * 类型：0:企业，1:个人
     */
    private Integer docType;

    /**
     * 文档库id
     */
    private Long houseId;

    private Integer type;

    private boolean contains = false;

    private int suffixSize;

    private List<String> suffixAllList;

    private List<String> dicExtraList;

    private List<String> suffixList;

    private List<Long> userIds;
}
