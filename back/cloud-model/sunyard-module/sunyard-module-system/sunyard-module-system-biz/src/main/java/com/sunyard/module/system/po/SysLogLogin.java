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
 * 登录日志表
 * </p>
 *
 * @author liugang
 * @since 2021-12-02
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class SysLogLogin implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 用户名称
     */
    private String userName;

    /**
     * 登录ip
     */
    private String loginIp;

    /**
     * 登录浏览器
     */
    private String loginBrowser;

    /**
     * 登录系统
     */
    private String loginSystem;

    /**
     * 登录状态（0成功、1失败）
     */
    private Integer loginStatus;

    /**
     * 登录说明信息
     */
    private String loginMsg;

    /**
     * 登录日期
     */
    private Date loginTime;

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

}
