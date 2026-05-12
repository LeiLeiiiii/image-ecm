package com.sunyard.module.system.vo;

import java.io.Serializable;

import lombok.Data;

/**
 * <p>
 * 对外api接口表
 * </p>
 *
 * @author 吴丙扬
 * @since 2022-01-06
 */
@Data
public class SysApiVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    private Long id;

    /**
     * 接口编码
     */
    private String apiCode;

    /**
     * 接口名称
     */
    private String apiName;

    /**
     *接口url
     */
    private String apiUrl;

    /**
     *0开启 1关闭
     */
    private Integer status;

    /**
     *0底座 1档案 2影像
     */
    private Integer systemType;

}
