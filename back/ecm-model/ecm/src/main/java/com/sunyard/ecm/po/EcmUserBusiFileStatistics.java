package com.sunyard.ecm.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 @Author 朱山成
 @time 2024/6/12 9:48
 **/
@Data
@EqualsAndHashCode(callSuper = false)
@ApiModel(value = "EcmUserBusiFileStatistics对象", description = "用户工作量统计表")
public class EcmUserBusiFileStatistics {
    @ApiModelProperty(value = "主键")
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;
    @ApiModelProperty(value = "机构号")
    private String orgCode;
    @ApiModelProperty(value = "业务号")
    private String appCode;
    @ApiModelProperty(value = "当天新增文件数量")
    private Long fileNumber;
    @ApiModelProperty(value = "创建时间")
    private Date createTime;
    @ApiModelProperty(value = "上传人")
    private String createUser;
    @ApiModelProperty(value = "更新时间")
    private Date updateTime;
    @ApiModelProperty(value = "员工号")
    private String statsUser;
    @ApiModelProperty(value = "统计日期yyyymmdd")
    private Integer statsDate;
}
