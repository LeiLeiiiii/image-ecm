package com.sunyard.module.system.api.dto;
/*
 * Project: am
 *
 * File Created at 2021/7/12
 *
 * Copyright 2016 Corporation Limited. All rights reserved.
 *
 * This software is the confidential and proprietary information of Company. ("Confidential Information"). You shall not
 * disclose such Confidential Information and shall use it only in accordance with the terms of the license.
 */

import lombok.Data;

import java.io.Serializable;

/**
 * @author zhouleibin
 * @Type
 * @Desc
 * @date 2021/7/12 13:25
 */
@Data
public class MetaDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 设置该路由在侧边栏和面包屑中展示的名字
     */
    private String title;

    /**
     * 设置该路由的图标，对应路径src/assets/icons/svg
     */
    private String icon;

    /**
     * 设置为true，则不会被 <keep-alive>缓存
     */
    private Boolean noCache;

    public MetaDTO() {
    }

    public MetaDTO(String title, String icon) {
        this.title = title;
        this.icon = icon;
    }

    public MetaDTO(String title, String icon, Boolean noCache) {
        this.title = title;
        this.icon = icon;
        this.noCache = noCache;
    }
}
