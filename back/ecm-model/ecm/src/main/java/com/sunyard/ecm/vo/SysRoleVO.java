package com.sunyard.ecm.vo;

import com.sunyard.framework.common.page.PageForm;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author ty
 * @since 2023-4-13 9:24
 * @desc 系统角色VO
 */
@Data
public class SysRoleVO extends PageForm implements Serializable {
    private static final long serialVersionUID = 1L;
    @ApiModelProperty(value = "角色id")
    private Long roleId;
    private Long instId;
    @ApiModelProperty(value = "角色名称")
    private String name;
    private String remarks;
    private Integer status;
    private Long[] menuIds;

    @ApiModelProperty(value = "关联功能权限列表")
    private List<Long> menuList;

    //角色代码
    @ApiModelProperty(value = "角色代码")
    private String roleCode;

    //系统区分：0档案，1影像
    @ApiModelProperty(value = "系统区分：0档案，1影像")
    private Integer systemCode;

    //关联用户
    @ApiModelProperty(value = "关联用户")
    private String relateUserName;

    @ApiModelProperty(value = "关联用户列表")
    private List<Long> relateUserList;

    @ApiModelProperty(value = "创建人")
    private String createUser;

    @ApiModelProperty(value = "最近修改人")
    private String updateUser;

    @ApiModelProperty(value = "组织id")
    private List<Long> orgIds;

    @ApiModelProperty(value = "登陆用户名")
    private String userName;

    @ApiModelProperty(value = "业务类型id")
    private String appCode;

    @ApiModelProperty(value = "版本号")
    private Integer rightVer;
}

