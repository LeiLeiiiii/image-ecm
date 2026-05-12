package com.sunyard.ecm.po;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 @Author 朱山成
 @time 2024/6/12 9:39
 **/
@Data
@EqualsAndHashCode(callSuper = false)
@ApiModel(value = "EcmBusiStatistics对象", description = "业务量统计表")
public class EcmBusiStatistics {

    @ApiModelProperty(value = "主键")
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;
    @ApiModelProperty(value = "机构号")
    private String orgCode;
    @ApiModelProperty(value = "业务号")
    private String appCode;
    @ApiModelProperty(value = "当天新增业务数量")
    private Long busiNumber;
    @ApiModelProperty(value = "当天新增文件数量")
    private Long fileNumber;
    @ApiModelProperty(value = "文件总大小(KB)")
    private Long fileSize;
    @ApiModelProperty(value = "创建时间")
    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date createTime;

    @ApiModelProperty(value = "时间字符串形式(yyyyMMdd)")
    private String statsDate;

    @ApiModelProperty(value = "更新时间")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;

//    @ApiModelProperty(value = "业务号")
//    private String appName;

//    @ApiModelProperty(value = "业务类型map")
//    private HashMap<String ,String> appMap;


}
