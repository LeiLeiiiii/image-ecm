package com.sunyard.mytool.scheduler;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sunyard.mytool.constant.MigrateConstant;
import com.sunyard.mytool.entity.DocTemp;
import com.sunyard.mytool.entity.ecm.FileTemp;
import com.sunyard.mytool.service.edm.DocTempService;
import com.sunyard.mytool.service.edm.EDMMigrateService;
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
public class EDMMigrateScheduler {

    @Autowired
    private EDMMigrateService edmMigrateService;
    @Autowired
    private DocTempService docTempService;
    @Autowired
    private RedisUtil redisUtil;
    @Value("${isEDMMigrate:0}")
    private String isEDMMigrate; //定时任务开关

    /**
     * 文档迁移定时任务
     */
    @Scheduled(fixedDelay = 1000, initialDelay = 10000)
    public void EDMmigrate() {
        if ("0".equals(isEDMMigrate)){
            return;
        }
        log.info("EDM文档迁移定时任务开始！");
        long startTime = System.currentTimeMillis();
        String querykey = "edm_query_lock";
        boolean lock = false;
        try {
            //查询锁
            lock = redisUtil.lock(querykey);  //获取锁
            if (!lock) {
                log.info("本轮EDM迁移定时任务未获取到查询锁，跳过本轮定时任务！");
                return;
            }
            Page<DocTemp> docTempPage = docTempService.selectMigData();
            List<DocTemp> recordsList = docTempPage.getRecords();
            try {
                if (recordsList.isEmpty()) {
                    log.info("未查询到待迁移的数据");
                    return;
                }
                //修改状态为迁移中 （1：迁移中）
                docTempService.updateBatchStatus(recordsList, MigrateConstant.MIGRATE_MIGRATING);
                log.info("查询到待迁移数据:{}条, 已修改状态为迁移中", recordsList.size());
            } finally {
                //Redis解锁
                redisUtil.releaseLock(querykey);
            }

            Map<DocTemp, Future<String>> futureMap = new HashMap<>();
            for (DocTemp docTemp : recordsList) {
                try {
                    //异步处理不同根目录
                    Future<String> migrFuture = edmMigrateService.asyncMigrate(docTemp);
                    futureMap.put(docTemp, migrFuture);
                } catch (Exception e) {
                    log.error("提交异步任务发生未知异常:批次ID:{}", docTemp.getPkId(), e);
                    String failReason = e.getMessage();
                    if (StringUtils.isNotBlank(failReason) && failReason.length() > 1024) {
                        failReason = failReason.substring(0, 1024) + "(后略)";
                    }
                    docTemp.setMigTime(new Date());
                    docTemp.setMigStatus(-1);
                    docTemp.setFailReason(failReason);
                    docTempService.updateById(docTemp);
                }
            }


            for (Map.Entry<DocTemp, Future<String>> entry : futureMap.entrySet()) {
                DocTemp docTemp = entry.getKey();
                Future<String> future = entry.getValue();
                try {
                    future.get(); // 等待异步任务完成
                } catch (Exception e) {
                    log.error("异步任务发生异常:批次ID:{}", docTemp.getPkId(), e);
                    String failReason = e.getMessage();
                    if (StringUtils.isNotBlank(failReason) && failReason.length() > 1024) {
                        failReason = failReason.substring(0, 1024) + "(后略)";
                    }
                    docTemp.setMigTime(new Date());
                    docTemp.setMigStatus(-1);
                    docTemp.setFailReason(failReason);
                    docTempService.updateById(docTemp);
                }
            }
        } finally {
            log.info("*本轮文档定时迁移任务结束*耗时:{}", System.currentTimeMillis() - startTime);
        }
    }
}
