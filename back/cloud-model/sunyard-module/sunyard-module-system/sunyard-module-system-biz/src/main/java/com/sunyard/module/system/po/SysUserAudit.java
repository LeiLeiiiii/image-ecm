package com.sunyard.module.system.po;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 * 用户审计记录表
 * </p>
 * @author PJW
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class SysUserAudit implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;


    /**
     * 用户id
     */
    private Long userId;

    /**
     * 登录成功次数
     */
    private Integer loginNum;

    /**
     * 接口次数
     */
    private Integer apiNum;

    /**
     * 访问次数最多的接口地址
     */
    private String firstApiUrl;

    /**
     * 审计范围开始时间
     */
    private Date auditStartTime;

    /**
     * 审计范围结束时间
     */
    private Date auditEndTime;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;

    /**
     * 删除状态(否:0,是:1)
     */
    @TableLogic
    private Integer isDeleted;

    /**
     * 登录失败次数
     */
    private Integer loginFalseNum;

    /**
     * 后台访问次数
     */
    private Integer sysLogNum;
}
