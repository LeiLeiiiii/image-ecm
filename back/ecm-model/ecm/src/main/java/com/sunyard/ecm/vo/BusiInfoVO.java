package com.sunyard.ecm.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.sunyard.ecm.dto.EcmAppAttrDTO;
import com.sunyard.framework.common.page.PageForm;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author XQZ
 * @date 2023/4/26
 * @describe 业务信息VO
 */
@Data
public class BusiInfoVO extends PageForm implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "业务类型")
    private String appCode;

    @ApiModelProperty(value = "业务类型")
    private List<String> appCodes;

    @ApiModelProperty(value = "机构号")
    private List<String> orgCodes;

    @ApiModelProperty(value = "业务版本号")
    private String rightVer;

    @ApiModelProperty(value = "创建人")
    private String createUser;

    @ApiModelProperty(value = "最新修改人")
    private String updateUser;

    @ApiModelProperty(value = "业务属性列表")
    private List<EcmAppAttrDTO> attrList;

    @ApiModelProperty(value = "创建日期 起")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private String createTimeStart;

    @ApiModelProperty(value = "创建日期 止")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private String createTimeEnd;

    @ApiModelProperty(value = "最近修改日期 起")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private String updateTimeStart;

    @ApiModelProperty(value = "最近修改日期 止")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private String updateTimeEnd;

    @ApiModelProperty(value = "业务编号")
    private String busiNo;
    @ApiModelProperty(value = "页面唯一标识")
    private String pageFlag;


    @ApiModelProperty(value = "业务表主键id")
    private Long busiId;

    @ApiModelProperty(value = "业务状态")
    private List<Integer> status;

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
}
