package com.sunyard.module.storage.task.job;

import javax.annotation.Resource;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.springframework.scheduling.quartz.QuartzJobBean;

import com.sunyard.framework.quartz.annotation.QuartzLog;
import com.sunyard.module.storage.task.job.service.CacheDelService;

/**
 * 删除存储服务
 *
 * @author wubingyang
 * @date 2021/12/20 9:03
 */
@DisallowConcurrentExecution
public class CacheDelJob extends QuartzJobBean {

    @Resource
    private CacheDelService cacheDelService;

    @QuartzLog
    @Override
    protected void executeInternal(JobExecutionContext context) {
        cacheDelService.delCache();
    }

}
