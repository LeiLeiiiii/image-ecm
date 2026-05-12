package com.sunyard.ecm.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.sunyard.ecm.dto.EcmAppAttrDTO;
import com.sunyard.framework.common.page.PageForm;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 回收站回收业务列表DTO
 *
 * @author wzz
 * @date 2024/6/6
 */
@Data
public class EcmBusiRecycleSearchVO extends PageForm implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "业务类型")
    private String appCode;

    @ApiModelProperty(value = "业务类型")
    private List<String> appCodes;

    @ApiModelProperty(value = "业务主索引")
    private String busiNo;

    @ApiModelProperty(value = "机构号")
    private String orgCode;

    @ApiModelProperty(value = "业务版本号")
    private String rightVer;

    @ApiModelProperty(value = "创建人")
    private String createUser;

    @ApiModelProperty(value = "业务属性列表")
    private List<EcmAppAttrDTO> attrList;

    @ApiModelProperty(value = "创建日期 起")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private String createTimeStart;

    @ApiModelProperty(value = "创建日期 止")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private String createTimeEnd;

    @ApiModelProperty(value = "删除日期 起")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private String deleteTimeStart;

    @ApiModelProperty(value = "删除日期 止")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private String deleteTimeEnd;

    @ApiModelProperty(value = "页面唯一标识")
    private String pageFlag;

}
