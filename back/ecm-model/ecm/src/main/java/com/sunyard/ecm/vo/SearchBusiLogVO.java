package com.sunyard.ecm.vo;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.sunyard.ecm.po.EcmBusiLog;
import com.sunyard.framework.common.page.PageForm;
import com.sunyard.framework.common.util.date.DateUtils;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.util.ObjectUtils;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @author scm
 * @since 2023/8/2 16:28
 * @desc 业务日志VO
 */
@Data
public class SearchBusiLogVO extends PageForm implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "业务类型Id")
    private List<String> appType;

    @ApiModelProperty(value = "业务主索引")
    private String busiNo;

    @ApiModelProperty(value = "操作人")
    private String operator;

    @ApiModelProperty(value = "机构号")
    private String orgCode;

    @ApiModelProperty(value = "操作日期 起")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date createTimeStart;

    @ApiModelProperty(value = "操作日期 止")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date createTimeEnd;

    @ApiModelProperty(value = "业务类型")
    private List<String> appCode;

    @ApiModelProperty(value = "操作类型")
    private Integer operatorType;
    /**
     *
     * @param <T>
     * @return
     */
    public <T> LambdaQueryWrapper<EcmBusiLog> queryWrapper() {
        LambdaQueryWrapper<EcmBusiLog> queryWrapper = new LambdaQueryWrapper<>();
        if (!ObjectUtils.isEmpty(busiNo)) {
            queryWrapper.like(EcmBusiLog::getBusiNo, busiNo);
        }
        if (!ObjectUtils.isEmpty(operator)) {
            queryWrapper.like(EcmBusiLog::getOperator,operator);
        }
        if (!ObjectUtils.isEmpty(orgCode)) {
            queryWrapper.like(EcmBusiLog::getOrgCode,orgCode);
        }
        if (!ObjectUtils.isEmpty(createTimeStart)&&!ObjectUtils.isEmpty(createTimeEnd)){
            queryWrapper.ge(EcmBusiLog::getCreateTime,createTimeStart);
            queryWrapper.le(EcmBusiLog::getCreateTime,DateUtils.getDayEndTime(createTimeEnd));
        }
        if (!ObjectUtils.isEmpty(appCode)){
            queryWrapper.in(EcmBusiLog::getAppCode, appCode);
        }
        if (!ObjectUtils.isEmpty(operatorType)){
            queryWrapper.in(EcmBusiLog::getOperatorType, operatorType);
        }
        queryWrapper.orderByDesc(EcmBusiLog::getCreateTime);
        return queryWrapper;
    }
}
