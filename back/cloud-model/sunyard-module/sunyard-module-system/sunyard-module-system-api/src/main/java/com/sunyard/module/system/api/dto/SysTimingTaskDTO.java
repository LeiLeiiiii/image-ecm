package com.sunyard.module.system.api.dto;

import java.io.Serializable;
import java.util.Date;

import lombok.Data;

/**
 * @author P-JWei
 * @date 2023/4/11 16:49 @title：
 * @description:
 */
@Data
public class SysTimingTaskDTO implements Serializable {

    private Long id;

    private String name;

    private String serviceName;

    private String classAbsolutePath;

    private Integer status;

    private Integer initType;

    private String cronExpression;

    private Date lastRunTime;
    //存活服务器ip+端口
    private String survivalServer;
    //存活时间，心跳
    private Date survivalTime;

}
