package com.sunyard.ecm.dto.ecm;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * @author lw
 * @since 2023/8/17 14:21
 * @Desc 设备参数DTO类
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class EcmAddAppParamsDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "设备参数")
    List<EcmStorageQueDTO> equipmentList;

    @ApiModelProperty(value = "队列参数")
    List<EcmStorageQueDTO> queueList;


}
