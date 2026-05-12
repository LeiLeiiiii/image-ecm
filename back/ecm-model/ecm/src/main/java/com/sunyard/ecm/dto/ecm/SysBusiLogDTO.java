package com.sunyard.ecm.dto.ecm;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * @author scm
 * @since 2023/8/2 17:04
 * @desc 业务日志DTO类
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SysBusiLogDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主键")
    private Long id;

    @ApiModelProperty(value = "业务类型名称")
    private String appName;

    @ApiModelProperty(value = "业务类型代码")
    private String appCode;

    @ApiModelProperty(value = "业务主索引")
    private String busiNo;

    @ApiModelProperty(value = "操作内容")
    private String operateContent;

    @ApiModelProperty(value = "操作人")
    private String operator;

    @ApiModelProperty(value = "操作人ID")
    private Long operatorId;

    @ApiModelProperty(value = "操作时间")
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    @ApiModelProperty(value = "机构号")
    private String orgCode;

    @ApiModelProperty(value = "异常信息")
    private String errorInfo;

    @ApiModelProperty(value = "操作类型")
    private Integer operatorType;

    @ApiModelProperty(value = "操作类型名称")
    private String operatorTypeStr;
}
