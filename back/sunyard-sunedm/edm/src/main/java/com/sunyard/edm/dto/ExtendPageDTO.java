package com.sunyard.edm.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
public class ExtendPageDTO implements Cloneable {

    /**
     * 附件名称
     */
    private String attchName;
    private Integer file;
    private Integer document;

    /**
     * 更新开始时间
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date updateStartDate;

    /**
     * 更新结束时间
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date updateEndDate;

    /**
     * 开始时间
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date createStartDate;

    /**
     * 结束时间
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date createEndDate;
    /**
     * 文档标题
     */
    private String docName;

    /**
     * 文件夹id
     */
    private Long folderId;

    /**
     * 文件夹ids
     */
    private List<Long> folderIds;

    /**
     * 回收状态 0:正常，1:已回收
     */
    private Integer recycleStatus;


    private List<Integer> types;
    /**
     * 0 否  1是
     */
    private Integer isDeleted;

    /**
     * 类型：0:企业，1:个人
     */
    private Integer docType;

    /**
     * 所有者中文名称
     */
    private Long docOwner;
    /**
     * 后端用-仅展示有管理权限和编辑权限的文档
     */
    private Boolean showFlag;

    /**
     * 排除当前文档所用的字段
     */
    private List<Long> relBusId;

    private Long instId;

    private Long deptId;

    private Long tokenId;

    private List<Long> relIds;


    private Integer user;
    private Integer inst;
    private Integer dept;
    private Integer team;
    ArrayList<Integer> permissType;
    

    /**
     * 所属文档库id
     */
    private Long houseId;


    /**
     * 搜索所用的标签id
     */
    private Long tagId;
    private String docOwnerStr;

    private String createTimeSort;
    private String updateTimeSort;
    public ExtendPageDTO clone(ExtendPageDTO extendPageDTO) throws CloneNotSupportedException {
        return (ExtendPageDTO) super.clone();
    }

    private Boolean contains = false;

    private int suffixSize;

    private List<String> suffixAllList;

    private List<String> dicExtraList;

    private List<String> suffixList;

    private Integer type;

    private List<Long> userIds;
}
