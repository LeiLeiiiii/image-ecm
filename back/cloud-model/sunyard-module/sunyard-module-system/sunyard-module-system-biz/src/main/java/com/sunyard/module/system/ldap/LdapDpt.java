package com.sunyard.module.system.ldap;

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
public class LdapDpt {

    @Id
    private Name id;
    /**
     * 部门主键
     */
    @Attribute(name = "ou")
    private String ou;
    /**
     *部门名
     */
    @Attribute(name = "name")
    private String name;
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
     *组织结构
     */
    @Attribute(name = "distinguishedName")
    private String distinguishedName;

}
