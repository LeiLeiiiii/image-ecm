package com.sunyard.ecm.dto.ecm;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @author： zyl
 * @create： 2023/5/29 16:24
 * @Description：单证属性DTO类
 */
@Data
public class EcmDtdAttrDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "单证属性代码（ocr-key）")
    private String attrCode;
    @ApiModelProperty(value = "单证属性名称")
    private String attrName;
}
