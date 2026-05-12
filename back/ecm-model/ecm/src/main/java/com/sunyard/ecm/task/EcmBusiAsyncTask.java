package com.sunyard.ecm.task;

import com.sunyard.ecm.manager.AsyncTaskService;
import com.sunyard.ecm.manager.BusiCacheService;
import com.sunyard.framework.quartz.annotation.QuartzLog;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.List;

/**
 @Author yzy
 @time 2025/03/05 16:05
 **/
@DisallowConcurrentExecution
public class EcmBusiAsyncTask extends QuartzJobBean {
    @Resource
    private AsyncTaskService asyncTaskService;
    @Resource
    private BusiCacheService busiCacheService;
    @Override
    @QuartzLog
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        List<String> busiIds= busiCacheService.getNeedPushEcmAsyncTaskList();
        if(!CollectionUtils.isEmpty(busiIds)) {
            asyncTaskService.pushAsyncTaskList(busiIds);
        }
    }

}
