package com.sunyard.module.system.api.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 * 系统参数表
 * </p>
 *
 * @author raochangmei
 * @since 2022-06-01
 */
@Data
public class SysParamDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String name;

    private String value;

    private Integer type;

    private String remark;

    private Integer systemCode;

    private Integer status;

    private Integer isDeleted;

    private Date createTime;

    private Date updateTime;

}
