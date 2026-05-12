package com.sunyard.framework.onlyoffice.dto.documentfunction;

import java.io.Serializable;

import lombok.Data;

/**
 * @author PJW
 * @BelongsProject: leaf-onlyoffice
 * @BelongsPackage: com.ideayp.leaf.onlyoffice.dto.document
 */
@Data
public class FwSharingSettings implements Serializable {

    /**
     * 将用户图标更改为链接图标
     */
    private Boolean isLink;
    /**
     * 完全访问，只读或拒绝访问  Full Access, Read Only , Deny Access
     */
    private String[] permissions;
    /**
     * 共享文档的用户的名称
     */
    private String user;

}
