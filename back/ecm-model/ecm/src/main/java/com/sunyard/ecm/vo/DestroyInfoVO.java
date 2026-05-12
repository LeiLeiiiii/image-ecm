package com.sunyard.ecm.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.sunyard.framework.common.page.PageForm;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author ypy
 * @date 2025/7/2
 * @describe 销毁信息VO
 */
@Data
public class DestroyInfoVO extends PageForm implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "任务id")
    private Long taskId;

    @ApiModelProperty(value = "销毁类型(0:历史业务销毁;1:历史资料销毁;2:已删除销毁)")
    private Integer destroyType;

    @ApiModelProperty(value = "业务索引")
    private String busiNo;

    @ApiModelProperty(value = "业务类型")
    private String appCode;

    @ApiModelProperty(value = "资料类型")
    private String docCode;

    @ApiModelProperty(value = "业务类型")
    private List<String> appCodes;

    @ApiModelProperty(value = "资料类型")
    private List<String> docCodes;

    @ApiModelProperty(value = "机构号")
    private String orgCode;

    @ApiModelProperty(value = "创建日期 起")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private String createTimeStart;

    @ApiModelProperty(value = "创建日期 止")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private String createTimeEnd;

    @ApiModelProperty(value = "创建日期 起")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private String destroyTimeStart;

    @ApiModelProperty(value = "创建日期 止")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private String destroyTimeEnd;

    @ApiModelProperty(value = "业务id")
    private List<Long> busiIds;

    @ApiModelProperty(value = "审核备注")
    private String auditNote;

    @ApiModelProperty(value = "审核意见(1:同意销毁,2:拒绝销毁)")
    private Integer auditOpinion;
}
