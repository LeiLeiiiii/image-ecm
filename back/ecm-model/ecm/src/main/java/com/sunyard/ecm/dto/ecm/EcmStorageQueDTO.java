package com.sunyard.ecm.dto.ecm;

import com.sunyard.module.storage.dto.EquipmentDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author： zyl
 * @create： 2023/4/26 14:30
 * @desc：存储队列DTO类
 */
@Data
public class EcmStorageQueDTO extends EquipmentDTO {
    private static final long serialVersionUID = 1L;
    private Boolean checked = false;


    @ApiModelProperty(value = "设备或者队列的唯一值")
    private String uniqId;

    @ApiModelProperty(value = "队列名称")
    private String mqName;

    @ApiModelProperty(value = "队列地址")
    private String mqAddress;
}
