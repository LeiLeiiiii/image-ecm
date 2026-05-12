package com.sunyard.module.system.ldap;
/*
 * Project: SunAM
 *
 * Copyright 2016 Corporation Limited. All rights reserved.
 *
 * This software is the confidential and proprietary information of Company. ("Confidential Information"). You shall not
 * disclose such Confidential Information and shall use it only in accordance with the terms of the license.
 */

import javax.naming.Name;

import org.springframework.ldap.odm.annotations.Attribute;
import org.springframework.ldap.odm.annotations.Entry;
import org.springframework.ldap.odm.annotations.Id;

import lombok.Data;

/**
 * @author zhouleibin
 * @date 2022/1/7 13:50
 * @Desc
 */
@Data
@Entry(base = "DC=generali-china,DC=cn", objectClasses = "organizationalPerson")
public class LdapUser {
    /**
     *户主键用
     */
    @Id
    private Name id;
    /**
     *
     */
    @Attribute(name = "cn")
    private String cn;
    /**
     *职责
     */
    @Attribute(name = "title")
    private String role;
    /**
     *姓名
     */
    @Attribute(name = "name")
    private String name;
    /**
     *登录名
     */
    @Attribute(name = "mailNickname")
    private String loginName;
    /**
     *邮箱
     */
    @Attribute(name = "mail")
    private String mail;
    /**
     *创建时间
     */
    @Attribute(name = "whenCreated")
    private String whenCreated;
    /**
     *修改时间
     */
    @Attribute(name = "whenChanged")
    private String whenChanged;
    /**
     *机构
     */
    @Attribute(name = "company")
    private String company;
    /**
     *部门
     */
    @Attribute(name = "department")
    private String department;
    /**
     *组织结构
     */
    @Attribute(name = "distinguishedName")
    private String distinguishedName;
    /**
     *新登录名
     */
    @Attribute(name = "sAMAccountName")
    private String sAMAccountName;
}

/**
 * Revision history -------------------------------------------------------------------------
 * <p>
 * Date Author Note ------------------------------------------------------------------------- 2022/1/7 zhouleibin creat
 */
