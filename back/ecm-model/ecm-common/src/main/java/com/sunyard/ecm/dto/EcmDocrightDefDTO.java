package com.sunyard.ecm.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @author ty
 * @since 2023-4-18 17:46
 * @desc 资料权限定义DTO
 */
@Data
public class EcmDocrightDefDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 影像资料权限定义表主键
     */
    private Long docrightId;

    /**
     * 角色值或维度值串(按关联顺序组值)
     */
    private String roleDimVal;

    /**
     * 业务类型id
     */
    private String appCode;

    /**
     * 资料类型
     */
    private String docCode;

    /**
     * 版本号
     */
    private Integer rightVer;

    /**
     * 维度类型：0角色维度，1业务多维度
     */
    private Integer dimType;

    /**
     * 是否使用：0否，1是
     */
    private Integer isUse;

    /**
     * 新增权限（0：无新增权限1：有新增权限）
     */
    private String addRight;

    /**
     * 查看权限（0：无查看权限1：有查看权限）
     */
    private String readRight;

    /**
     * 修改权限（0：无修改权限1：有修改权限）
     */
    private String updateRight;

    /**
     * 删除权限（0：无删除权限1：有删除权限）
     */
    private String deleteRight;

    /**
     * 查看缩略图权限（0：无查看缩略图权限1：有查看缩略图权限）
     */
    private String thumRight;

    /**
     * 打印权限（0：无打印权限1：有打印权限）
     */
    private String printRight;

    /**
     * 下载权限（0：无下载权限1：有下载权限）
     */
    private String downloadRight;

    /**
     * 最小上传数（默认值为0；0：不限制）
     */
    private Integer minPages;

    /**
     * 最大上传数（默认值为0；0：不限制）
     */
    private Integer maxPages;

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

    /**
     * 资料类型名称
     */
    private String docName;

    /**
     * 允许上传文件类型列表
     */
    private List<EcmFileTypeDefDTO> ecmFileTypeDefList;
}
