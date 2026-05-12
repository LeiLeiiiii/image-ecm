package com.sunyard.ecm.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @author zyl
 * @description
 * @since 2024/7/3
 */
@Data
public class EcmStatisticsVO {
    /**
     * 机构集合
     */
    @ApiModelProperty(value = "机构集合")
    private List<String> orgCodes;
    /**
     * 业务号集合
     */
    @ApiModelProperty(value = "业务号集合")
    private List<String> appCodes;
    /**
     * 统计单位 0天 1月 2年
     */
    @ApiModelProperty(value = "统计单位 0天 1月 2年")
    private Integer unit;
    /**
     * 开始日期
     */
    @ApiModelProperty(value = "开始日期")
    private Date startDate;

    /**
     * 结束日期
     */
    @ApiModelProperty(value = "结束日期")
    private Date endDate;


    /**
     * 操作员
     */

    @ApiModelProperty(value = "操作员")
    List<String> mapList;

    /**
     * 当前页
     */
    @ApiModelProperty(value = "当前页")
    private Integer pageNum;

    /**
     * 页数
     */
    @ApiModelProperty(value = "页数")
    private Integer pageSize;

    /**
     * 排序规则
     */
    @ApiModelProperty(value = "排序规则")
    private String sortRule;

    /**
     * 要排序的列
     */
    @ApiModelProperty(value = "要排序的列")
    private String sortColumn;


}
