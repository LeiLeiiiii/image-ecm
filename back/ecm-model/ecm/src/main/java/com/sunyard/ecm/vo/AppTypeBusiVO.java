package com.sunyard.ecm.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author： ty
 * @since： 2023/5/17 16:31
 * @desc: 新增业务类型VO
 */
@Data
public class AppTypeBusiVO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 业务类型id
     */
    @ApiModelProperty(value = "业务类型id")
    private String appCode;
    /**
     * 业务ids
     */
    @ApiModelProperty(value = "业务ids")
    private List<Long> busiIds;
}
