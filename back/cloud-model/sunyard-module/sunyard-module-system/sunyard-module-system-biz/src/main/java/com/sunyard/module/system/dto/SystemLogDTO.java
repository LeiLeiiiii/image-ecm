package com.sunyard.module.system.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author P-JWei
 * @date 2023/8/17 14:18:26
 * @title
 * @description
 */
@Data
public class SystemLogDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;

    /**
     * 操作描述
     */
    private String requestDesc;

    /**
     * 操作人
     */
    private String userName;

    /**
     * 操作时间
     */
    private Date createTime;

    /**
     * 请求地址
     */
    private String requestIp;

    /**
     * 日志状态
     */
    private Integer responseCode;

    /**
     * 日志状态str
     */
    private String responseCodeStr;

    /**
     * 异常信息
     */
    private String exceptionMsg;

    /**
     * 所属系统
     */
    private String logSystem;

}
