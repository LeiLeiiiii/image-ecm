package com.sunyard.ecm.dto.ecm.statistics;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.HashMap;

/**
 * @author zyl
 * @description
 * @since 2024/7/2
 */
@Data
public class EcmWorkStatisticsDTO {

    @ApiModelProperty(value = "业务号")
    private String appCode;

    @ApiModelProperty(value = "机构号")
    private String orgCode;

    @ApiModelProperty(value = "业务类型名称")
    private String appName;

    @ApiModelProperty(value = "上传人")
    private String createUser;

    @ApiModelProperty(value = "上传人名称")
    private String createUserName;

    @ApiModelProperty(value = "上传人时间")
    private String createDate;

    @ApiModelProperty(value = "总计")
    private Long total;

    @ApiModelProperty(value = "业务类型map")
    private HashMap<String ,Long> appMap;

    @ApiModelProperty(value = "业务类型文件数")
    private Long fileNumber;
}
