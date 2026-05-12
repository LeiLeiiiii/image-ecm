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
public class EcmDestroyListDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "业务表主键")
    private Long id;

    @ApiModelProperty(value = "任务表主键")
    private Long destroyTaskId;

    @ApiModelProperty(value = "业务表主键")
    private Long busiId;

    @ApiModelProperty(value = "业务号")
    private String busiNo;

    @ApiModelProperty(value = "业务类型")
    private String appCode;

    @ApiModelProperty(value = "资料类型")
    private String docCode;

    @ApiModelProperty(value = "机构号")
    private String orgCode;

    @ApiModelProperty(value = "创建人")
    private String createUser;

    @ApiModelProperty(value = "创建时间")
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    @ApiModelProperty(value = "最新修改人")
    private String updateUser;

    @ApiModelProperty(value = "最新修改时间")
    private Date updateTime;

    @ApiModelProperty(value = "删除状态(否:0,是:1)")
    private Integer isDeleted;

    @ApiModelProperty(value = "创建人名称")
    private String createUserName;

    @ApiModelProperty(value = "业务类型名称")
    private String appTypeName;

    @ApiModelProperty(value = "资料类型名称")
    private String docTypeName;

    @ApiModelProperty(value = "机构号名称")
    private String orgName;

    @ApiModelProperty(value = "业务文件数量")
    private Long fileCount;

    @ApiModelProperty(value = "审核人名称")
    private String auditUserName;

    @ApiModelProperty(value = "销毁时间")
    private Date destroyTime;

}
