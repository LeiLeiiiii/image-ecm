package com.sunyard.module.system.api.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @author P-JWei
 * @date 2023/7/3 14:45
 * @title
 * @description
 */
@Data
public class MessageDTO implements Serializable {

    /**
     * 消息来源系统标识
     */
    private String msgSystem;

    /**
     * 消息类型
     */
    private String msgType;

    /**
     * 消息头部(标题)
     */
    private String msgHead;

    /**
     * 消息体(可存储一些业务信息、json数据、业务id等)
     */
    private String msgBody;

    /**
     * 消息内容(即展示的消息内容)
     */
    private String msgContent;

    /**
     * 接受人id
     */
    private Long acceptUser;

    /**
     * 通知时间
     */
    private Date informTime;

    /**
     * 接受邮件 地址
     */
    private String mailAddress;

    /**
     * 接受邮件地址 s
     */
    private List<String> mailAddressList;
}
