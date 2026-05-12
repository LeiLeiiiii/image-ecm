package com.sunyard.module.system.api.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * <p>
 * 对外api接口表
 * </p>
 *
 * @author liugang
 * @since 2021-12-15
 */
@Data
public class SysApiDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String appId;

    private String systemName;

    private String systemCode;

    private String publicKey;

    private String ip;

    private String apiCode;

    private String apiName;

    private Integer systemType;


}
