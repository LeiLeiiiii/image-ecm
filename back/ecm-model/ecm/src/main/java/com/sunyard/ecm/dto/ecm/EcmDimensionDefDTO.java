package com.sunyard.ecm.dto.ecm;

import com.sunyard.ecm.po.EcmDimensionDef;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * @author ty
 * @since 2023-4-20 10:10
 * @desc 业务多纬度DTO类
 */
@Data
public class EcmDimensionDefDTO extends EcmDimensionDef {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "影像维度定义表主键")
    private Long id;

    @ApiModelProperty(value = "维度代码")
    private String dimCode;

    @ApiModelProperty(value = "维度名称")
    private String dimName;

    @ApiModelProperty(value = "维度取值范围(下拉列表的值，json存储)")
    private String dimValue;

    @ApiModelProperty(value = "创建人")
    private String createUser;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @ApiModelProperty(value = "最新修改人")
    private String updateUser;

    @ApiModelProperty(value = "最新修改时间")
    private Date updateTime;

    @ApiModelProperty(value = "创建人名称")
    private String createUserName;

    @ApiModelProperty(value = "最新修改人名称")
    private String updateUserName;

    @ApiModelProperty(value = "维度被关联状态：0未被关联，1已被关联")
    private Integer relateStatus;

    @ApiModelProperty(value = "是否必选：0否，1是")
    private Integer required;

}
