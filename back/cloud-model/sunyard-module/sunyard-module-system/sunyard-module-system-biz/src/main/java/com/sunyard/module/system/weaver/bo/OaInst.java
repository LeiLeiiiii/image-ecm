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
public class OaInst implements Serializable, Comparable<OaInst> {

    /** 分部id */
    private String id;
    /** 上级分部id, 0 或者 空为表示没有上级分部 */
    private String supsubcomid;
    /** 分部编码 */
    private String subcompanycode;
    /** 分部简称 */
    private String subcompanyname;
    /** 分部全称 */
    private String subcompanydesc;
    /** 封存标志，1 封存，其他为未封存 */
    private String canceled;
    /** 废弃字段 */
    private String url;
    /** 自定义数据 */
    private String custom_data;
    /** 排序 */
    private String showorder;
    /** 创建时间戳 */
    private String created;
    /** 修改时间戳 */
    private String modified;

    @Override
    public int compareTo(OaInst o) {
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
