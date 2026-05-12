package com.sunyard.module.system.api.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @author zhouleibin
 */
@Data
public class SysRoleDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 角色id
     */
    private Long roleId;

    /**
     * 机构id
     */
    private Long instId;

    /**
     * 角色名称
     */
    private String name;

    /**
     * 描述
     */
    private String remarks;

    /**
     * 启用状态(启用:0,停用:1)
     */
    private Integer status;

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

    private Long[] menuIds;

    /**
     * 关联功能权限列表
     */
    private List<Long> menuList;

    /**
     * 角色代码
     */
    private String roleCode;

    /**
     * 系统区分：0档案，1影像
     */
    private String systemCode;

    /**
     * 关联用户
     */
    private String relateUserName;

    /**
     * 关联用户列表
     */
    private List<Long> relateUserList;

    /**
     * 创建人
     */
    private String createUser;

    /**
     * 最近修改人
     */
    private String updateUser;

    /**
     * 组织id
     */
    private List<Long> orgIds;

    /**
     * 登陆用户名
     */
    private String userName;

    /**
     * 业务类型id
     */
    private Long appTypeId;

    /**
     * 版本号
     */
    private Integer rightVer;

    /**
     * 角色关联的用户，"，"分隔
     */
    private String relateUserNames;
    /**
     * 已选用户ids
     */
    private List<Long> selectedUserIds;
    /**
     * 已选组织ids
     */
    private List<Long> selectedOrgIds;

    /**
     * 当前页
     */
    private Integer pageNum = 1;
    /**
     * 分页数量
     */
    private Integer pageSize = 20;
}
