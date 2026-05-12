package com.sunyard.ecm.controller;

import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sunyard.ecm.annotation.TimeCalculateAnnotation;
import com.sunyard.ecm.constant.LogsConstants;
import com.sunyard.ecm.dto.ecm.statistics.EcmBusiStatisticsDTO;
import com.sunyard.ecm.dto.ecm.statistics.EcmStatisticsDTO;
import com.sunyard.ecm.service.ModelBusiService;
import com.sunyard.ecm.service.StatisticsBusiService;
import com.sunyard.ecm.vo.EcmAppDefAttrVO;
import com.sunyard.framework.common.result.Result;
import com.sunyard.framework.log.annotation.OperationLog;
import com.sunyard.module.system.api.InstApi;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * @Author 朱山成
 * @time 2024/6/11 16:16
 **/
@Api(tags = "统计分析-业务量统计")
@TimeCalculateAnnotation
@RestController
@RequestMapping("statistics/busi")
public class StatisticsBusiController extends BaseController {
    @Resource
    private InstApi instApi;
    @Resource
    private ModelBusiService modelBusiService;
    @Resource
    private StatisticsBusiService statisticsBusiService;

    /**
     * 业务量统计-总计
     */
    @ApiOperation("业务量统计-总计")
    @OperationLog(LogsConstants.STATISTICS + "业务量统计")
    @PostMapping("trafficTotal")
    public Result<EcmBusiStatisticsDTO> trafficTotal() {
        return statisticsBusiService.trafficTotal(true);
    }

    /**
     * 业务量统计-柱状图/折状图
     */
    @ApiOperation("业务量统计-柱状图/折状图")
    @OperationLog(LogsConstants.STATISTICS + "业务量统计-柱状图/折状图")
    @PostMapping("trafficHistogram")
    public Result<Map<String, Object>> trafficHistogram(@RequestBody EcmStatisticsDTO ecmStatisticsDTO) {
        return statisticsBusiService.trafficSearch(ecmStatisticsDTO, getToken());
    }

    /**
     * 业务量统计-表格
     */
    @ApiOperation("业务量统计-表格")
    @OperationLog(LogsConstants.STATISTICS + "业务量统计-表格")
    @PostMapping("trafficForm")
    public Result trafficForm(@RequestBody EcmStatisticsDTO ecmStatisticsDTO) {
        return statisticsBusiService.trafficForm(ecmStatisticsDTO, getToken());
    }

    /**
     * 业务量统计-表格汇总
     */
    @ApiOperation("业务量统计-表格")
    @OperationLog(LogsConstants.STATISTICS + "业务量统计-表格")
    @PostMapping("trafficFormTotal")
    public Result trafficFormTotal(@RequestBody EcmStatisticsDTO ecmStatisticsDTO) {
        return statisticsBusiService.trafficFormTotal(ecmStatisticsDTO, getToken());
    }

    /**
     * 业务量统计-导出表格
     */
    @ApiOperation("业务量统计-导出表格")
    @OperationLog(LogsConstants.STATISTICS + "业务量统计-导出表格")
    @PostMapping("trafficBusiExport")
    public void trafficBusiExport(HttpServletResponse response,
                                  @RequestBody EcmStatisticsDTO ecmStatisticsDTO) {
        statisticsBusiService.trafficBusiExport(response, ecmStatisticsDTO, getToken());
    }

    /**
     * 业务量统计-汇总数据导出表格
     */
    @ApiOperation("业务量统计-汇总导出表格")
    @OperationLog(LogsConstants.STATISTICS + "业务量统计-导出表格")
    @PostMapping("trafficBusiTotalExport")
    public void trafficBusiTotalExport(HttpServletResponse response,
                                       @RequestBody EcmStatisticsDTO ecmStatisticsDTO) {
        statisticsBusiService.trafficBusiTotalExport(response, ecmStatisticsDTO, getToken());
    }

    /**
     * 业务量统计-饼图
     */
    @ApiOperation("业务量统计-饼图")
    @OperationLog(LogsConstants.STATISTICS + "业务量统计-饼图")
    @PostMapping("trafficPie")
    public Result trafficPie(@RequestBody EcmStatisticsDTO ecmStatisticsDTO) {
        return statisticsBusiService.trafficPie(ecmStatisticsDTO, getToken());
    }

    /**
     * 查询机构树
     */
    @ApiOperation("查询机构树")
    @OperationLog(LogsConstants.STATISTICS + "查询机构树")
    @PostMapping("searchInstTree")
    public Result searchInstTree() {
        return instApi.searchInstTree(null);
    }

    /**
     * 查询业务类型树
     */
    @ApiOperation("查询业务类型树")
    @OperationLog("查询业务类型树")
    @PostMapping("searchBusiTypeTree")
    public Result<List<EcmAppDefAttrVO>> searchBusiTypeTree(String appCode) {
        return Result.success(modelBusiService.searchBusiTypeTree(appCode, getToken()));
    }

}
