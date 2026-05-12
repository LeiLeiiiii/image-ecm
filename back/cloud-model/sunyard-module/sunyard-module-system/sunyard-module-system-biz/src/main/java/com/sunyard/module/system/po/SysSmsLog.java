package com.sunyard.module.system.po;

import java.io.Serializable;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

/**
 * <p>
 * 信息发送日志
 * </p>
 *
 * @author zjm
 * @since 2025-08-26
 */
@Data
@TableName("sys_sms_log")
public class SysSmsLog implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键id
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 短信响应内容
     */
    private String responseContent;

    /**
     * 响应状态码
     */
    private String smsCode;

    /**
     * 返回操作信息
     */
    private String smsMsg;

    /**
     * 请求内容
     */
    private String requestContent;

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
