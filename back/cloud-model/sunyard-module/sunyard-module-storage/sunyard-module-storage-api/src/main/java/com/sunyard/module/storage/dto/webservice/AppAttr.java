package com.sunyard.module.storage.dto.webservice;

import lombok.Data;

import java.io.Serializable;
import java.util.Calendar;

/**
 * AppAttr.java
 * <p>
 * This file was auto-generated from WSDL by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 *
 * @author 刘港
 */

@Data
public class AppAttr implements Serializable {
    private String attrName;

    private Calendar createTime;

    private String createUser;

    private String defaultValue;

    private AppAttrId id;

    private int inputType;

    private int isKey;

    private int isNull;

    private int isShow;

    private String listType;

    private Calendar modifyTime;

    private String modifyUser;

    private String regex;

    private int status;

}
