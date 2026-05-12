package com.sunyard.ecm.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @author： ty
 * @create： 2023/5/11 14:03
 * @desc:资料标记VO
 */
@Data
public class EcmDocMarkVO implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "资料id")
    private String docId;

    @ApiModelProperty(value = "业务表主键")
    private Long busiId;

    @ApiModelProperty(value = "资料标记名称")
    private String docName;

    private String currentUserId;
}
