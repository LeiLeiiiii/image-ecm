package com.sunyard.module.storage.dto.webservice;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @author liugang
 * @Type com.sunyard.suneam.service.impl.sunecm.webservice
 * @Desc
 * @date 14:09 2021/10/26
 */
@Data
public class AppType implements Serializable {
    private String appCode;

    private String appDesc;

    private String appName;

    private int children;

    private Date createTime;

    private String createUser;

    private List<DocType> docSet;

    private int level;

    private Date modifyTime;

    private String modifyUser;

    private String parent;

    private int status;
}
