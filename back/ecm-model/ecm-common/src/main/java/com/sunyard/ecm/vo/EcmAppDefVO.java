package com.sunyard.ecm.vo;


import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author lw
 * @date 2023/4/26
 * @describe 影像业务类型定义表
 */
@Data
public class EcmAppDefVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 业务代码
     */
    private String appCode;

    /**
     * 业务名称
     */
    private String appName;

    /**
     * 设备id
     */
    private Long equipmentId;

    /**
     * 是否开启到达通知，0未开启，1开启
     */
    private Integer arriveInform;

    /**
     * 关联的消息队列名称
     */
    private String queueName;

    /**
     * 父业务类型id
     */
    private String parent;

    /**
     * 顺序
     */
    private Float appSort;

    /**
     * 是否压缩（0：否，1：是）
     */
    private Integer isQulity;

    /**
     * 压缩比例（ 例：800长宽都大于上述值时进行压缩）
     */
    private Integer resiz;

    /**
     * 压缩质量
     */
    private Float qulity;

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


}
