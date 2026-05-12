package com.sunyard.ecm.task;

import com.sunyard.ecm.service.StatisticsWorkService;
import com.sunyard.framework.quartz.annotation.QuartzLog;
import lombok.extern.slf4j.Slf4j;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;

import javax.annotation.Resource;

/**
 @Author 朱山成
 @time 2024/6/12 9:14
 **/
@Slf4j
@DisallowConcurrentExecution
public class EcmStatisticBusiTask extends QuartzJobBean {
    @Resource
    private StatisticsWorkService statisticsWorkService;
    @Override
    @QuartzLog
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        log.info("####### ecmStatisticAnalysisTask 业务量统计数据开始更新 ##########");
        statisticsWorkService.ecmStatisticAnalysisTask();
        log.info("###### ecmStatisticAnalysisTask 业务量统计数据更新结束 ##########");

        log.info("####### ecmStatisticAnalysisTask 工作量统计数据开始更新 ##########");
        statisticsWorkService.ecmUserStatisticAnalysisTask();
        log.info("####### ecmStatisticAnalysisTask 工作量统计数据结束更新 ##########");
    }

}
