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
import java.util.List;

/**
 * @author zhouleibin
 * @Type
 * @Desc
 * @date 2021/7/12 13:25
 */
@Data
public class RouterDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    private String id;
    private String parentId;
    /**
     * 路由名字
     */
    private String name;

    /**
     * 接口地址
     */
    private String path;

    /**
     * 是否隐藏路由，当设置 true 的时候该路由不会再侧边栏出现
     */
    private Boolean hidden;

    /**
     * 重定向地址，当设置 noRedirect 的时候该路由在面包屑导航中不可被点击
     */
    private String redirect;

    /**
     * 组件地址
     */
    private String component;

    /**
     * 当你一个路由下面的 children 声明的路由大于1个时，自动会变成嵌套的模式--如组件页面
     */
    private Boolean alwaysShow;

    /**
     * 其他元素
     */
    private MetaDTO meta;

    /**
     * 子路由
     */
    private List<RouterDTO> children;

    /**
     * 显示顺序
     */
    private Integer orderNum;

    /**
     * 是否默认
     */
    private Boolean isDefault;

}
