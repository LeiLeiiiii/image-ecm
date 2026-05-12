package com.sunyard.module.storage.task.job;

import javax.annotation.Resource;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.springframework.scheduling.quartz.QuartzJobBean;

import com.sunyard.framework.quartz.annotation.QuartzLog;
import com.sunyard.module.storage.task.job.service.CacheDelService;

/**
 * @author zyl
 * @Description
 * @since 2024/3/4 9:57
 */
@DisallowConcurrentExecution
public class CacheLocalDelJob extends QuartzJobBean {

    @Resource
    private CacheDelService cacheDelService;

    @QuartzLog
    @Override
    protected void executeInternal(JobExecutionContext context) {
        cacheDelService.delLocalCache();
    }

}
