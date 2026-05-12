package com.sunyard.ecm.dto.ecm;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.sunyard.ecm.dto.EcmAppAttrDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author： ty
 * @create： 2023/5/8 13:52
 * @Desc: 业务信息DTO类
 */
@Data
public class EcmBusiInfoDTO implements Serializable {
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

    @ApiModelProperty(value = "业务状态0待提交 1 已提交  2已受理（处理中）3 已作废 4 处理失败 5 已完结")
    private Integer status;

    @ApiModelProperty(value = "业务状态字符")
    private String statusStr;

    @ApiModelProperty(value = "业务状态字符")
    private Map<String,String> attrMap;

    @ApiModelProperty(value = "拓展字段值")
    @ExcelIgnore
    private List<EcmAppAttrDTO> attrList;

    @ApiModelProperty(value = "错误编码(0:不展示,1:展示)")
    private Integer errNo;

    @ApiModelProperty(value = "错误信息")
    private String remark;
}
