package com.sunyard.ecm.dto.ecm;

import com.sunyard.ecm.dto.EcmAppAttrDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @Author lyq
 * @Date 2023/7/28 13:50
 * @Description 移动端业务信息DTO类
 */
@Data
public class EcmBusiInfoPhoneDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "业务表主键")
    private Long busiId;

    @ApiModelProperty(value = "业务号")
    private String busiNo;

    @ApiModelProperty(value = "业务类型")
    private String appCode;

    @ApiModelProperty(value = "业务类型名称")
    private String appTypeName;

    @ApiModelProperty(value = "压缩比例")
    private Integer resiz;

    @ApiModelProperty(value = "压缩质量(默认0.5)")
    private Float qulity;

    @ApiModelProperty(value = "是否压缩(1-是 0-否))")
    private Integer isQulity;

    @ApiModelProperty(value = "孩子节点")
    private List<EcmBusiStructureTreeDTO> children;

    @ApiModelProperty(value = "业务属性列表")
    private List<EcmAppAttrDTO> attrList;
}
