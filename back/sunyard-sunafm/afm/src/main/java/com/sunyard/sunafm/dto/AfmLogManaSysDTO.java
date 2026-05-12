package com.sunyard.sunafm.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 日志管理-系统日志列表返参
 * @author P-JWei
 * @date 2024/3/11 14:43:03
 * @title
 * @description
 */
@Data
public class AfmLogManaSysDTO implements Serializable {

    /**
     * 操作内容
     */
    private String opContent;

    /**
     * 操作人
     */
    private String opUserName;

    /**
     * 操作时间
     */
    private Date opTime;
}
