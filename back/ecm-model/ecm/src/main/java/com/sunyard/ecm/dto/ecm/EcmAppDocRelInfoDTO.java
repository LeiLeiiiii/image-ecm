package com.sunyard.ecm.dto.ecm;

import com.sunyard.ecm.po.EcmAppDocRel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @author: lw
 * @Date: 2023/8/2
 * @Description: 资料业务关联参数
 */
@Data
public class EcmAppDocRelInfoDTO extends EcmAppDocRel implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "资料类型code")
    private String  docCode;

    @ApiModelProperty(value = "资料类型名称")
    private String  docName;

    @ApiModelProperty(value = "资料类型id")
    private Long  docId;




}
