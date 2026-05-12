package com.sunyard.ecm.dto.ecm;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author： zyl
 * @create： 2023/5/6 17:48
 * @Description：单证属性定义DTO类
 */
@Data
public class EcmDtdDefDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主键")
    private Long dtdTypeId;

    @ApiModelProperty(value = "单证类型（ocr-type）")
    private String dtdCode;

    @ApiModelProperty(value = "单证名称")
    private String dtdName;

    @ApiModelProperty(value = "单证顺序")
    private Float dtdSort;

    @ApiModelProperty(value = "创建人")
    private String createUser;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @ApiModelProperty(value = "最新修改人")
    private String updateUser;

    @ApiModelProperty(value = "最新修改时间")
    private Date updateTime;
    /**
     * 节点值
     */
    @ApiModelProperty(value = "节点值")
    private Long id;

    /**
     * 节点名称
     */
    @ApiModelProperty(value = "节点名称")
    private String label;
}
