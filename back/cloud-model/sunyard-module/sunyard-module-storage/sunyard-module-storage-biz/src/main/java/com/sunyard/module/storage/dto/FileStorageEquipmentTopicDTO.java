package com.sunyard.module.storage.dto;

import com.sunyard.module.storage.po.StEquipment;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author yzy
 * @desc
 * @since 2025/12/18
 */
@Data
public class FileStorageEquipmentTopicDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 变更的设备列表
     */
    private List<StEquipment> stEquipmentList;
    /**
     * 存储设备类型
     */
    private Integer storageType;
}
