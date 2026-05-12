package com.sunyard.ecm.controller;

import java.util.ArrayList;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.github.pagehelper.PageInfo;
import com.sunyard.ecm.annotation.TimeCalculateAnnotation;
import com.sunyard.ecm.constant.LogsConstants;
import com.sunyard.ecm.dto.ecm.statistics.EcmWorkStatisticsDTO;
import com.sunyard.ecm.service.StatisticsWorkService;
import com.sunyard.ecm.vo.EcmStatisticsVO;
import com.sunyard.framework.common.result.Result;
import com.sunyard.framework.log.annotation.OperationLog;
import com.sunyard.module.system.api.InstApi;
import com.sunyard.module.system.api.UserApi;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * @Author 朱山成
 * @time 2024/6/11 16:16
 **/
@Api(tags = "统计分析-工作量统计")
@TimeCalculateAnnotation
@RestController
@RequestMapping("statistics/work")
public class StatisticsWorkController extends BaseController {
    @Resource
    private InstApi instApi;
    @Resource
    private UserApi userApi;
    @Resource
    private StatisticsWorkService statisticsWorkService;

    /**
     * 查询所有业务类型
     */
    @ApiOperation("查询所有业务类型")
    @OperationLog(LogsConstants.STATISTICS + "查询所有业务类型")
    @PostMapping("searchAllBusi")
    public Result searchAllBusi() {
        return Result.success(statisticsWorkService.searchAllBusi());
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
     * 查询所有用户
     */
    @ApiOperation("查询所有用户")
    @OperationLog(LogsConstants.STATISTICS + "查询所有用户")
    @PostMapping("searchAllUser")
    public Result searchAllUser(EcmStatisticsVO vo) {
        if (!CollectionUtils.isEmpty(vo.getOrgCodes())) {
            ArrayList<Long> longs = new ArrayList<>();
            vo.getOrgCodes().forEach(s -> longs.add(Long.parseLong(s)));
            return userApi.getUsersByInstIdList(longs);
        }
        return userApi.getUsersByInstId(null);
    }

    /**
     * 工作量统计
     */
    @ApiOperation("工作量统计")
    @OperationLog(LogsConstants.STATISTICS + "工作量统计")
    @PostMapping("workloadSearch")
    public Result<PageInfo<EcmWorkStatisticsDTO>> workloadSearch(@RequestBody EcmStatisticsVO vo) {
        return Result.success(statisticsWorkService.workloadSearch(vo, getToken()));
    }

    /**
     * 导出工作量统计表格
     */
    @ApiOperation("导出工作量统计表格")
    @OperationLog(LogsConstants.STATISTICS + "导出工作量统计表格")
    @PostMapping("workloadSearchExport")
    public void workloadSearchExport(HttpServletResponse response,
                                     @RequestBody EcmStatisticsVO vo) {
        statisticsWorkService.workloadSearchExport(response, vo, getToken());
    }

}
