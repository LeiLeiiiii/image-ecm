package com.sunyard.framework.message.enums;

import lombok.Getter;

/**
 * @author P-JWei
 * @date 2023/7/6 17:29
 * @title 消息通知-结果-内容模板枚举类
 * @description 集成通用的一些模块。可供生成msg对象使用
 */
@Getter
public enum MessageResultEnum {
    /**
     * 通过
     */
    PASS("通过", "申请通过提醒", "您的%s申请(编码: %s )审批通过"),
    /**
     * 未通过
     */
    UNPASS("未通过", "申请未通过提醒", "您的%s申请(编码: %s )审批未通过"),
    /**
     * 待审批
     */
    APPROVAL("待审批", "待审批提醒", "您有一个%s申请(编码: %s )等待审批"),
    /**
     * 催还
     */
    CUIHUAN("催还", "催还通知", "您收到一条%s消息(借阅编码: %s ),请您及时归还"),
    /**
     * 归还
     */
    GUIHUAN("归还", "待归还提醒", "您收到一条%s消息,您借阅申请(编码: %s )已过期，请您及时归还"),
    /**
     * 移交审批
     */
    TRANSFERAPPROVAL("移交审批", "移交档案提醒", "您有一份%s申请(编码: %s )待接收"),
    /**
     * 移交通过
     */
    TRANSFERPASS("移交通过", "移交完成提醒", "您发起的档案%s(编码: %s )已接收完成"),
    /**
     * 移交退回
     */
    TRANSFERUNPASS("移交退回", "移交退回提醒", "您发起的档案%s(编码: %s )被退回"),
    /**
     * 流程通过
     */
    FLOWPASS("流程通过", "审批通过提醒", "您收到一条%s通过消息(档案编号: %s)申请通过"),
    /**
     * 流程未通过
     */
    FLOWUNPASS("流程未通过", "审批未通过提醒", "您收到一条%s未通过消息(档案编号: %s)申请未通过"),
    /**
     * 流程待审批
     */
    FLOWAPPROVAL("流程待审批", "待审批提醒", "您有一个%s申请(编码: %s )等待审批");
    /**
     * 信息
     */
    private String desc;
    /**
     * 头
     */
    private String head;
    /**
     * 内容
     */
    private String content;

    MessageResultEnum(String desc, String head, String content) {
        this.desc = desc;
        this.head = head;
        this.content = content;
    }
}
