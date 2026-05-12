package com.sunyard.ecm.dto.ecm;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 业务回收信息
 *
 * @author： wzz
 * @create： 2024/6/6
 */
@Data
public class EcmBusiRecycleInfoDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "业务表主键")
    private Long busiId;

    @ApiModelProperty(value = "业务号")
    private String busiNo;

    @ApiModelProperty(value = "业务类型")
    private String appCode;

    @ApiModelProperty(value = "资料权限版本")
    private Integer rightVer;

    @ApiModelProperty(value = "树标志(0静态树，1动态树，3静态有标记)")
    private Integer treeType;

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

    @ApiModelProperty(value = "最新修改人名称")
    private String updateUserName;

    @ApiModelProperty(value = "业务类型名称")
    private String appTypeName;

    @ApiModelProperty(value = "存储设备id")
    private Long equipmentId;

    @ApiModelProperty(value = "业务批次号")
    private String busiBatchNo;

    @ApiModelProperty(value = "机构号名称")
    private String orgName;

    @ApiModelProperty(value = "业务回收ID")
    private Long recycleId;

    @ApiModelProperty(value = "业务删除用户")
    private String recycleUser;

    @ApiModelProperty(value = "业务删除时间")
    private Date recycleTime;

}
