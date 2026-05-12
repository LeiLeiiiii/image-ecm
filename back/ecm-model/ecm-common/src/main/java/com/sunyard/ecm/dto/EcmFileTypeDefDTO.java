package com.sunyard.ecm.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author scm
 * @since 2023/8/10 14:29
 * @desc 文件类型定义DTO
 */
@Data
public class EcmFileTypeDefDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 文件类型id
     */
    private Long fileTypeId;

    /**
     * 文件类型
     */
    private String fileTypeCode;

    /**
     * 文件类型名称
     */
    private String fileTypeName;

    /**
     * 最大上传文件(0：不限制（单位MB）)
     */
    private Long uploadSize;

    /**
     * 创建人
     */
    private String createUser;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 最新修改人
     */
    private String updateUser;

    /**
     * 最新修改时间
     */
    private Date updateTime;


}
