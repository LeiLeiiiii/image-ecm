package com.sunyard.ecm.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.sunyard.ecm.config.properties.EcmRetryTaskProperties;
import com.sunyard.ecm.constant.IcmsConstants;
import com.sunyard.ecm.dto.redis.EcmBusiInfoRedisDTO;
import com.sunyard.ecm.manager.BusiCacheService;
import com.sunyard.ecm.mapper.EcmEsAsyncTaskMapper;
import com.sunyard.ecm.po.EcmEsAsyncTask;
import com.sunyard.ecm.service.OperateFullQueryService;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;

import javax.annotation.Resource;
import java.util.List;


@Slf4j
@DisallowConcurrentExecution
public class EcmEsRetryTask  extends QuartzJobBean {

    @Resource
    private EcmRetryTaskProperties ecmRetryTaskProperties;
    @Resource
    private EcmEsAsyncTaskMapper ecmEsAsyncTaskMapper;
    @Resource
    private BusiCacheService busiCacheService;
    @Resource
    private OperateFullQueryService operateFullQueryService;
    @Resource
    private SqlSessionFactory sqlSessionFactory;


    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        log.info("####### EcmIntelligentRetryTask 业务es任务重试开始 ##########");
        List<EcmEsAsyncTask> tasks = getEcmEsAsyncTaskListInRetry(ecmRetryTaskProperties);
        log.info("####### 重试数量为：{} ##########", tasks.size());

        tasks.forEach(
                task ->
                {
                    EcmBusiInfoRedisDTO ecmBusiInfoRedisDTO = busiCacheService.getEcmBusiInfoRedisDTO(null, task.getBusiId());
                    operateFullQueryService.addEsBusiInfo(ecmBusiInfoRedisDTO, task.getTokenId());
                    task.setRetryCount(task.getRetryCount()+1);
                    log.info("{},推送业务ES重试任务结束",task.getBusiId());
                }
        );

        if (!tasks.isEmpty()) {
            batchUpdateTask(tasks);
        }
    }

    private List<EcmEsAsyncTask> getEcmEsAsyncTaskListInRetry(EcmRetryTaskProperties ecmRetryTaskProperties) {
        LambdaQueryWrapper<EcmEsAsyncTask> lambdaQueryWrapper=new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(EcmEsAsyncTask::getStatus, IcmsConstants.TWO);
        lambdaQueryWrapper.le(EcmEsAsyncTask::getRetryCount,ecmRetryTaskProperties.getMaxRetryCount());
        lambdaQueryWrapper.last("LIMIT "+ecmRetryTaskProperties.getLimitCount());
        return ecmEsAsyncTaskMapper.selectList(lambdaQueryWrapper);
    }

    public void batchUpdateTask(List<EcmEsAsyncTask> ecmAsyncTaskList) {
        if (ecmAsyncTaskList == null || ecmAsyncTaskList.isEmpty()) {
            return;
        }

        // 使用 MyBatis 的 BATCH 执行器，高效批量更新
        try (SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH)) {
            EcmEsAsyncTaskMapper mapper = sqlSession.getMapper(EcmEsAsyncTaskMapper.class);

            for (EcmEsAsyncTask task : ecmAsyncTaskList) {
                // 只更新 count 字段，WHERE id = ?
                mapper.update(null,
                        Wrappers.<EcmEsAsyncTask>lambdaUpdate()
                                .eq(EcmEsAsyncTask::getBusiId, task.getBusiId())
                                .set(EcmEsAsyncTask::getRetryCount, task.getRetryCount())
                );
            }

            sqlSession.commit(); // 提交批量操作
        }

    }


}
