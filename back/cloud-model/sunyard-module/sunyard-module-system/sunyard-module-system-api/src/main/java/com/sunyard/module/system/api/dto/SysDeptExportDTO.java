package com.sunyard.module.system.api.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 部门导出数据
 *
 * @author wubingyang
 * @date 2022/4/2 9:39
 */
@Data
public class SysDeptExportDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 部门名称
     */
    private String name;

    /**
     * 部门号
     */
    private String deptNo;

    /**
     * 所属机构
     */
    private String instName;

    /**
     * 上级部门
     */
    private String parentName;
}
