package com.sunyard.module.system.api.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 机构导出数据
 *
 * @author wubingyang
 * @date 2022/4/2 9:39
 */
@Data
public class SysInstExportDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 上级机构
     */
    private String parentName;

    /**
     * 机构名称
     */
    private String name;

    /**
     * 机构号
     */
    private String instNo;

    /**
     * 全宗号
     */
    private String identifier;

    /**
     * 全宗名称
     */
    private String identifierName;

    /**
     * 备注
     */
    private String remarks;

}
