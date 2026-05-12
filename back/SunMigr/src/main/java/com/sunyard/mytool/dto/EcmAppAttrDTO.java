package com.sunyard.mytool.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 *  业务属性DTO
 */
@Data
public class EcmAppAttrDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 影像业务属性定义表主键
     */
    private Long appAttrId;

    /**
     * 业务类型id
     */
    private String appCode;

    /**
     * 业务属性代码
     */
    private String attrCode;

    /**
     * 业务属性名称
     */
    private String attrName;

    /**
     * 属性顺序
     */
    private Integer attrSort;

    /**
     * 输入类型：1：输入项2：日期项3：下拉
     */
    private Integer inputType;

    /**
     * 状态(默认值为1；0：无效1：有效)
     */
    private Integer status;

    /**
     * 是否主键(默认值为0；0：不作为业务主键1：作为业务主键)
     */
    private Integer isKey;

    /**
     * 是否允许为空(默认值为0；0：不可为空；1：可为空；)
     */
    private Integer isNull;

    /**
     * 是否显示(默认值为1；0：不在批次树根节点显示；1：在批次树根节点显示；)
     */
    private Integer treeShow;

    /**
     * 列表显示
     */
    private Integer listShow;

    /**
     * 默认值
     */
    private String defaultValue;

    /**
     * 校验表达式
     */
    private String regex;

    /**
     * 下拉列表的值，json存储(LISTE:表示从扩展表查询)
     */
    private String listValue;

    /**
     * 创建人
     */
    private String createUser;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 最新修改人
     */
    private String updateUser;

    /**
     * 最新修改时间
     */
    private Date updateTime;

    /**
     * 属性值
     */
    private String appAttrValue;

    /**
     * 前端所用
     */
    private String label;

    /**
     * 创建人名称
     */
    private String createUserName;

    /**
     * 最新修改人名称
     */
    private String updateUserName;

}
