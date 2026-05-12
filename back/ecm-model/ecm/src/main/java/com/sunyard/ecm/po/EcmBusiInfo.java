package com.sunyard.ecm.po;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 * 影像业务信息表
 * </p>
 *
 * @author zyl
 * @since 2023-04-17
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@ApiModel(value = "EcmBusiInfo对象", description = "影像业务信息表")
public class EcmBusiInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "业务表主键")
    @TableId(value = "busi_id", type = IdType.ASSIGN_ID)
    private Long busiId;

    @ApiModelProperty(value = "业务号")
    private String busiNo;

    @ApiModelProperty(value = "业务类型")
    private String appCode;

    @ApiModelProperty(value = "资料权限版本")
    private Integer rightVer;

    @ApiModelProperty(value = "树标志(0静态树，1动态树)")
    private Integer treeType;

    @ApiModelProperty(value = "机构号")
    private String orgCode;

    @ApiModelProperty(value = "创建人")
    private String createUser;

    @ApiModelProperty(value = "创建时间")
    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date createTime;

    @ApiModelProperty(value = "最新修改人")
    private String updateUser;

    @ApiModelProperty(value = "最新修改时间")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;

    @ApiModelProperty(value = "删除状态(否:0,是:1)")
    @TableLogic
    private Integer isDeleted;

    @ApiModelProperty(value = "创建者名称")
    private String createUserName;

    @ApiModelProperty(value = "更新者名称")
    private String updateUserName;

    @ApiModelProperty(value = "机构名称")
    private String orgName;

    @ApiModelProperty(value = "业务状态0待提交 1 已提交  2已受理（处理中）3 已作废 4 处理失败 5 已完结")
    private Integer status;

    @ApiModelProperty(value = "流程类型ID")
    private String delegateType;

    @ApiModelProperty(value = "流程类型NAME")
    private String delegateTypeName;

    @ApiModelProperty(value = "业务类型ID")
    private String typeBig;

    @ApiModelProperty(value = "业务类型NAME")
    private String typeBigName;

    @ApiModelProperty(value = "业务系统来源")
    private String sourceSystem;

    @ApiModelProperty(value = "错误编码(0:不展示,1:展示)")
    private Integer errNo;

    @ApiModelProperty(value = "错误信息")
    private String remark;
}
