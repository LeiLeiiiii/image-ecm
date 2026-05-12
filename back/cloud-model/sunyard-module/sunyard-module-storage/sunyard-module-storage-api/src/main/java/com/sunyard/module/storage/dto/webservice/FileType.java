package com.sunyard.module.storage.dto.webservice;

import lombok.Data;

import java.io.Serializable;

/**
 * FileType.java
 * <p>
 * This file was auto-generated from WSDL by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 *
 * @author 刘港
 */
@Data
public class FileType implements Serializable {
    private String fileTypeName;

    private FileTypeId id;

    private int maxsize;

    private String remark;

    private int uploadSize;

}
