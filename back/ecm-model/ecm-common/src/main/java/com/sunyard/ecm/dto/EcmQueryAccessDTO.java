package com.sunyard.ecm.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 查询调阅返回参数
 *
 * @author scm
 * @since 2023/8/10 10:19
 */
@Data
public class EcmQueryAccessDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    //业务信息（业务资料树+业务关联文件）

    //采集页面地址
    private String url;
}
