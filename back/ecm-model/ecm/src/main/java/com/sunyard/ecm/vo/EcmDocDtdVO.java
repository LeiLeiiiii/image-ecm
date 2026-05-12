package com.sunyard.ecm.vo;

import com.sunyard.ecm.po.EcmDocDef;
import com.sunyard.ecm.po.EcmDtdDef;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author： zyl
 * @create： 2023/4/13 16:05
 * @desc: 资料属性VO
 */
@Data
public class EcmDocDtdVO extends EcmDocDef implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "业务类型列表")
    List<EcmDtdDef> ecmDtdDefs;
}
