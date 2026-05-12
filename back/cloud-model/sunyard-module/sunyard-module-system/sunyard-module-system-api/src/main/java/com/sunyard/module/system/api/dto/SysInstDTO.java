package com.sunyard.module.system.api.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author zhouleibin
 */
@Data
public class SysInstDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    private Long id;

    /**
     * 机构id
     */
    private Long instId;

    /**
     * 机构号
     */
    private String instNo;

    /**
     * 机构id-父级
     */
    private Long parentId;

    /**
     * 机构名称
     */
    private String name;

    /**
     * 机构名称-递归显示
     */
    private String nameLevel;

    /**
     * 层级
     */
    private Integer newlevel;

    /**
     * 备注
     */
    private String remarks;

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

    /**
     * 第三方系统同步主键
     */
    private String ldapId;

    /**
     * ldap查询dn
     */
    private String ldapQueryDn;

    /**
     * ldap查询用户的条件
     */
    private String ldapQueryUserTxt;

    /**
     * ldap查询部门的条件
     */
    private String ldapQueryDptTxt;

}
