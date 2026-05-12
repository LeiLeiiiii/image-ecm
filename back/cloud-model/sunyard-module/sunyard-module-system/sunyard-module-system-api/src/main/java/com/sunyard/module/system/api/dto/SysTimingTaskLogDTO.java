package com.sunyard.module.system.api.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author P-JWei
 * @date 2023/5/31 15:01
 * @title：
 * @description:
 */
@Data
public class SysTimingTaskLogDTO implements Serializable {

    private Long id;

    private Long taskId;

    private String jobName;

    private String serviceName;

    private String classAbsolutePath;

    private String errorMsg;

    private Integer status;

    private Date runStartTime;

    private Date runOverTime;
}
