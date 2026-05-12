package com.sunyard.module.system.api.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author zhouleibin
 */
@Data
public class SysDeptDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    private Long id;

    /**
     * 部门id
     */
    private Long deptId;

    /**
     * 部门号
     */
    private String deptNo;

    /**
     * 部门id-父级
     */
    private Long parentId;

    /**
     * 部门名称
     */
    private String name;

    /**
     * 部门名称-递归显示
     */
    private String nameLevel;

    /**
     * 层级
     */
    private Integer newlevel;

    /**
     * 第三方系统同步主键
     */
    private String ldapId;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 删除状态(否:0,是:1)
     */
    private Integer isDeleted;

}
