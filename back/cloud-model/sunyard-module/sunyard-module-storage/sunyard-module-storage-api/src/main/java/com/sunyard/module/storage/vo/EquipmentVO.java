package com.sunyard.module.storage.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 存储设备
 * @author PJW
 */
@Data
public class EquipmentVO  implements Serializable {


    /**
     * 主键
     */
    private List<Long> ids;
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
    private Long createUser;

    /**
     * 更新人
     */
    private Long updateUser;
}
