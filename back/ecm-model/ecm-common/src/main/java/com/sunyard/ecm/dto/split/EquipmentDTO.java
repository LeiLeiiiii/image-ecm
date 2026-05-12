package com.sunyard.ecm.dto.split;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;


/**
 * @author: lw
 * @Date: 2024/1/28
 * @Description: 存储设备DTO
 */
@Data
public class EquipmentDTO implements Serializable {


    /**
     * 主键
     */
    private Long id;


    /**
     * 设备编码
     *
     */
    private String equipmentCode;

    /**
     * 设备名
     *
     */
    private String equipmentName;

    /**
     * 存储方式
     */
    private Integer storageType;

    /**
     * 基础路径
     */
    private String basePath;

    /**
     * 自定义域名
     */
    private String domainName;

    /**
     * 节点地址
     */
    private String storageAddress;

    /**
     * 存储bucket
     */
    private String bucket;

    /**
     * 存储连接key
     */
    private String accessKey;

    /**
     * 存储连接密钥
     */
    private String accessSecret;

    /**
     * 启用状态
     */
    private Integer status;

    /**
     * 上传人
     */
    private String createUser;

    /**
     * 更新人
     */
    private String updateUser;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 删除状态(否:0,是:1
     */
    private Integer isDeleted;



    /**
     * 存储方式
     */
    private String storageTypeStr;


}
