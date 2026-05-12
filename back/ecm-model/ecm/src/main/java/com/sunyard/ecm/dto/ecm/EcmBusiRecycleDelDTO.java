package com.sunyard.ecm.dto.ecm;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 影像业务回收站删除DTO
 *
 * @author wzz
 * @Date: 2024/6/6
 */
@Data
public class EcmBusiRecycleDelDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "业务回收ID")
    private Long recycleId;

    @ApiModelProperty(value = "业务ID")
    private Long busiId;

}
