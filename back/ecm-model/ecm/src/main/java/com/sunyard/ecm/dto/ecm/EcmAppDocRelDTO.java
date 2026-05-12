package com.sunyard.ecm.dto.ecm;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author： zyl
 * @create： 2023/5/8 16:16
 * @Desc：业务资料关联DTO类
 */
@Data
public class EcmAppDocRelDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "孩子")
    private List<EcmAppDocRelDTO> children;
    @ApiModelProperty(value = "主键")
    private String id;
    @ApiModelProperty(value = "资料类型id")
    private String docCode;
    @ApiModelProperty(value = "资料顺序")
    private Float docSort;
    @ApiModelProperty(value = "父节点id")
    private String parent;
}
