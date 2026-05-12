package com.sunyard.ecm.dto.ecm;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 影像销毁任务DTO
 *
 * @author： ypy
 * @create： 2025/7/3
 */
@Data
public class EcmDestroyTaskDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "业务表主键")
    private Long id;

    @ApiModelProperty(value = "销毁类型(0:历史业务销毁;1:历史资料销毁;2:已删除销毁)")
    private Integer destroyType;

    @ApiModelProperty(value = "业务类型")
    private String appCode;

    @ApiModelProperty(value = "资料类型")
    private String docCode;

    @ApiModelProperty(value = "业务创建日期区间")
    private String busiCreateDate;

    @ApiModelProperty(value = "机构号")
    private String orgCode;

    @ApiModelProperty(value = "创建人")
    private String createUser;

    @ApiModelProperty(value = "创建时间")
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    @ApiModelProperty(value = "任务状态(0:待审核;1:待销毁;2:审核不通过;3:已销毁;)")
    private Integer status;

    @ApiModelProperty(value = "审核人")
    private String auditUser;

    @ApiModelProperty(value = "删除状态(否:0,是:1)")
    private Integer isDeleted;

    @ApiModelProperty(value = "机构号名称")
    private String orgName;

    @ApiModelProperty(value = "创建人名称")
    private String createUserName;

    @ApiModelProperty(value = "审核人名称")
    private String auditUserName;

    @ApiModelProperty(value = "业务类型名称")
    private String appTypeName;

    @ApiModelProperty(value = "资料类型名称")
    private String docTypeName;

    @ApiModelProperty(value = "审核时间")
    private Date auditTime;

    @ApiModelProperty(value = "审核备注")
    private String auditNote;

    @ApiModelProperty(value = "销毁时间")
    private Date destroyTime;

    @ApiModelProperty(value = "销毁类型名称(0:历史业务销毁;1:历史资料销毁;2:已删除销毁)")
    private String destroyTypeStr;

    @ApiModelProperty(value = "任务状态(0:待审核;1:待销毁;2:审核不通过;3:已销毁;)")
    private String  statusStr;

    @ApiModelProperty(value = "任务状态(0:待审核;1:待销毁;2:审核不通过;3:已销毁;)")
    private String  busiTypeStr;

    @ApiModelProperty(value = "业务创建人")
    private String busiCreateUserName;

    @ApiModelProperty(value = "业务创建时间")
    private String busiCreateTime;

    @ApiModelProperty(value = "业务编号")
    private String busiNo;

    @ApiModelProperty(value = "审核意见")
    private String auditOpinion;
}
