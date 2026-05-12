package com.sunyard.module.system.api.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author： ty
 * @since： 2023/6/12 17:53
 */
@Data
public class SysRoleUserDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 主键
     */
    private Long id;

    /**
     * 角色id
     */
    private Long roleId;

    /**
     * 用户id
     */
    private Long userId;

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
     * 用户名称
     */
    private String userName;

    /**
     * 机构id
     */
    private Long instId;

    /**
     * 部门id
     */
    private Long deptId;
}
