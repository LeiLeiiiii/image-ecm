package com.sunyard.mytool.scheduler;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sunyard.mytool.constant.MigrateConstant;
import com.sunyard.mytool.entity.ecm.BatchTemp;
import com.sunyard.mytool.service.ecm.BatchTempService;
import com.sunyard.mytool.service.ecm.ECMMigrateService;
import com.sunyard.mytool.until.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.Future;


@Slf4j
@Component
public class ICMSMigrateScheduler {

    @Autowired
    private ECMMigrateService ecmMigrateService;
    @Autowired
    private BatchTempService batchTempService;
    @Autowired
    private RedisUtil redisUtil;
    @Value("${isECMMigrate:0}")
    private String isECMMigrate; //定时任务开关
    /**
     * 文档迁移定时任务
     */
    @Scheduled(fixedDelay = 1000, initialDelay = 10000)
    public void ICMSmigrate() {
        if ("0".equals(isECMMigrate)){
            return;
        }
        log.info("(ECM)影像迁移定时任务开始！");
        long startTime = System.currentTimeMillis();
        String querykey = "icms_query_lock";
        boolean lock = false;
        try {
            //查询锁
            lock = redisUtil.lock(querykey);  //获取锁
            if (!lock) {
                log.info("本轮ECM迁移定时任务未获取到查询锁，跳过本轮定时任务！");
                return;
            }
            Page<BatchTemp> batchTempPage = batchTempService.selectMigData();
            List<BatchTemp> recordsList = batchTempPage.getRecords();
            try {
                if (recordsList.isEmpty()) {
                    log.info("本轮(ECM)影像迁移任务未查询到待迁移的数据");
                    return;
                }
                //修改状态为迁移中 （1：迁移中）
                batchTempService.updateBatchStatus(recordsList, MigrateConstant.MIGRATE_MIGRATING);
                log.info("查询到待迁移数据:{}条, 已修改状态为迁移中", recordsList.size());
            } finally {
                //Redis解锁
                redisUtil.releaseLock(querykey);
            }

            //多线程交给迁移任务处理
            List<Future<String>> ecmMigFutureList = new ArrayList<>();
            for (BatchTemp batchTemp : recordsList) {
                try {
                    //异步处理
                    Future<String> future = ecmMigrateService.asyncMigrate(batchTemp);
                    ecmMigFutureList.add(future);
                } catch (Exception e) {
                    log.error("提交异步任务发生未知异常:批次ID:{}", batchTemp.getId(), e);
                }
            }

            for (Future<String> future : ecmMigFutureList) {
                try {
                    future.get();
                } catch (Exception e) {
                    //log.error("获取异步任务结果发生未知异常", e);
                }
            }
        } finally {
            log.info("*本轮文档定时迁移任务结束*耗时:{}", System.currentTimeMillis() - startTime);
        }
    }

}
