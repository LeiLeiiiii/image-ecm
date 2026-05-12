package com.sunyard.module.storage.vo;

import com.sunyard.framework.common.page.PageForm;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

/**
 * <p>
 * 文件表
 * </p>
 *
 * @author panjiazhu
 * @since 2022-07-12
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
public class StEquipmentVO extends PageForm implements Serializable {

    private static final long serialVersionUID = 1L;

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

    /**
     * 启用状态集
     */
    private List<Integer> statusList;

}
