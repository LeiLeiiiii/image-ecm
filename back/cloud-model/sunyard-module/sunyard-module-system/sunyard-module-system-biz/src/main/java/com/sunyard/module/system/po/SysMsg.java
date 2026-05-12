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
 * 消息提示表
 * </p>
 *
 * @author pjw
 * @since 2023-05-04
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class SysMsg implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "msg_id", type = IdType.ASSIGN_ID)
    private Long msgId;

    /**
     * 消息状态（0未读 1 已读）
     */
    private Integer msgStatus;

    /**
     * 消息来源系统
     */
    private String msgSystem;

    /**
     * 消息类型（如：借阅、鉴定等）
     */
    private String msgType;

    /**
     * 消息头（即消息标题）
     */
    private String msgHead;

    /**
     * 消息体（保存一些业务信息）
     */
    private String msgBody;

    /**
     * 消息内容
     */
    private String msgContent;

    /**
     * 消息接受人id
     */
    private Long acceptUser;

    /**
     * 通知时间
     */
    private Date informTime;

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
