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

import lombok.Data;

import java.io.Serializable;

/**
 * @author wml
 * @Desc
 * @date 2025/11/8 14:43
 */
@Data
public class OaPost implements Serializable, Comparable<OaPost> {

    /** 岗位id */
    private String id;
    /** 相关文档id */
    private String jobdoc;
    /** 备注 */
    private String jobtitleremark;
    /** 职责 */
    private String jobresponsibility;
    /** 全称 */
    private String jobtitlename;
    /** 任职资格 */
    private String jobcompetency;
    /** 岗位编码 */
    private String jobtitlecode;
    /** 创建时间戳 */
    private String created;
    /** 修改时间戳 */
    private String modified;
    /** 简称 */
    private String jobtitlemark;
    /** 外键 （新增） */
    private String outkey;
    /** 部门id (废弃字段) */
    private String jobdepartmentid;
    /** 分页-总数 */
    private String totalSize;

    @Override
    public int compareTo(OaPost o) {
        return this.getId().compareTo(o.getId());
    }
}
/**
 * Revision history
 * -------------------------------------------------------------------------
 * Date Author Note
 * -------------------------------------------------------------------------
 * 2025/11/8 wml creat
 */
