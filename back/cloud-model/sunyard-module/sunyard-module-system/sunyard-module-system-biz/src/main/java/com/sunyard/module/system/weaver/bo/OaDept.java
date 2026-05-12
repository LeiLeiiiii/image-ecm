package com.sunyard.module.system.weaver.bo;
/*
 * Project: Sunyard
 *
 * File Created at 2025/7/19
 *
 * Copyright 2016 Corporation Limited.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Company. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license.
 */

import java.io.Serializable;

import lombok.Data;

/**
 * @author Leo
 * @Desc
 * @date 2025/7/19 14:43
 */
@Data
public class OaDept implements Serializable, Comparable<OaDept> {

    /** 部门id */
    private String id;
    /** 分部id */
    private String subcompanyid1;
    /** 上级部门id */
    private String supdepid;
    /** 部门编码 */
    private String departmentcode;
    /** 部门简称 */
    private String departmentmark;
    /** 部门全称 */
    private String departmentname;
    /** 部门负责人 （新增） */
    private String departmenthead;
    /** 部门分管领导（新增） */
    private String departmentleader;
    /** 封存标志，1 封存，其他为未封存 */
    private String canceled;
    /** 外键 */
    private String outkey;
    /** 排序 */
    private String showorder;
    /** 创建时间戳 */
    private String created;
    /** 修改时间戳 */
    private String modified;

    @Override
    public int compareTo(OaDept o) {
        return this.getId().compareTo(o.getId());
    }
}
/**
 * Revision history
 * -------------------------------------------------------------------------
 * Date Author Note
 * -------------------------------------------------------------------------
 * 2025/7/19 Leo creat
 */
