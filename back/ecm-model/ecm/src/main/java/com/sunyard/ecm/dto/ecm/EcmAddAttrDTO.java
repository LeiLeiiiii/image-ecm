package com.sunyard.ecm.dto.ecm;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author： zyl
 * @create： 2023/4/26 14:30
 * @Desc: 影像添加业务属性DTO类
 */
@Data
public class EcmAddAttrDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "属性id数组")
    private List<EcmAppAttrDTO> attrIdList;
    @ApiModelProperty(value = "类型id")
    private String typeId;
}
