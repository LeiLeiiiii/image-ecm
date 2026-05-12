package com.sunyard.ecm.dto.ecm.statistics;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 * 资料类型查重表
 * </p>
 *
 * @author ljw
 * @since 2025-02-19
 */
@Data
@EqualsAndHashCode(callSuper = false)
@ApiModel(value = "EcmDocPlagChe对象", description = "资料类型查重表")
public class EcmDocPlagCheDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "app_attr_id")
    @ApiModelProperty(value = "主键")
    private Long appAttrId;

    @ApiModelProperty(value = "业务类型code")
    private String appCode;

    @ApiModelProperty(value = "资料类型code")
    private String docCode;

    @ApiModelProperty(value = "时间范围(近N年)")
    private Integer frameYear;

    @ApiModelProperty(value = "相似度阈值")
    private Double fileSimilarity;

    @ApiModelProperty(value = "创建人")
    private String createUser;

    @ApiModelProperty(value = "创建时间")
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    @ApiModelProperty(value = "最新修改人")
    private String updateUser;

    @ApiModelProperty(value = "更新时间")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;

    @ApiModelProperty(value = "类型：0：基础类型；1：自定义类型（动态树）")
    private Integer type;
}

