package com.sunyard.module.system.api.dto;

import lombok.Data;

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
public class SysMessageDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    private Long messageId;

    /**
     * 提醒用户id
     */
    private Long userId;

    /**
     * 消息标题
     */
    private String messageTitle;

    /**
     * 消息内容
     */
    private String messageContent;

    /**
     * 消息类型
     */
    private String messageType;

    /**
     * 阅读状态（0：未读    1：已读）
     */
    private Integer messageStatus;

    /**
     * 通知时间
     */
    private Date informTime;

    /**
     * 档案系统分类:0办公，1财务，2业务，3人事
     */
    private Integer systemCode;

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
