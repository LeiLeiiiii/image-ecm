package com.sunyard.module.storage.dto.webservice;

import lombok.Data;

import java.io.Serializable;
import java.util.Calendar;
import java.util.List;

/**
 * DocType.java
 * <p>
 * This file was auto-generated from WSDL by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 *
 * @author 刘港
 */

@Data
public class DocType implements Serializable {
    private List<AppType> appSet;

    private String barcode;

    private int children;

    private Calendar createTime;

    private String createUser;

    private String docCode;

    private String docDesc;

    private String docName;

    private int level;

    private Calendar modifyTime;

    private String modifyUser;

    private String parent;

    private String resize;

    private int resizeType;

    private int sort;

    private int status;
}
