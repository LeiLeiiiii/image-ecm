package com.sunyard.ecm.vo;

import com.sunyard.ecm.po.EcmDtdAttr;
import com.sunyard.ecm.po.EcmDtdDef;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author： zyl
 * @create： 2023/4/20 9:19
 * @desc: 属性定义VO
 */
@Data
public class EcmDtdDefVO extends EcmDtdDef implements Serializable {
    private static final long serialVersionUID = 1L;


    @ApiModelProperty(value = "单证属性列表")
    List<EcmDtdAttr> ecmDtdAttrList;

    @ApiModelProperty(value = "创建人")
    String createUserName;

    @ApiModelProperty(value = "最近人修改人")
    String updateUserName;
}
