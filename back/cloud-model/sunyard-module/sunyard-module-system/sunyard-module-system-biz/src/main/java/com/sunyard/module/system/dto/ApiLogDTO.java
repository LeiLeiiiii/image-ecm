package com.sunyard.module.system.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author P-JWei
 * @date 2023/8/21 10:02:55
 * @title
 * @description
 */
@Data
public class ApiLogDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;

    /**
     * 接口名称
     */
    private String requestDesc;

    /**
     * 请求地址
     */
    private String requestIp;

    /**
     * 请求内容
     */
    private String requestParams;

    /**
     * 操作时间
     */
    private Date createTime;

    /**
     * 日志状态
     */
    private Integer responseCode;

    /**
     * 日志状态
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
