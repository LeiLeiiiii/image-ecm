package com.sunyard.framework.licence.task.job;

import java.util.Locale;

import javax.annotation.Resource;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.sunyard.framework.common.result.Result;
import com.sunyard.framework.common.util.AssertUtils;
import com.sunyard.framework.licence.config.ServerAuthProperties;
import com.sunyard.framework.licence.task.service.ServerAuthService;

import lombok.extern.slf4j.Slf4j;


@Slf4j
@Configuration
@EnableScheduling
@Component
public class ServerAuthJob {

    @Resource
    private ServerAuthProperties serverAuthProperties;
    @Resource
    private ServerAuthService serverAuthService;

    /**
     * 定时任务处理非system系统
     */
    @Scheduled(cron = "0 5 0 * * ?")
    public void checkServerAuth() {
        AssertUtils.isNull(serverAuthProperties.getAppName(), "服务名不能为空！");
        if(serverAuthProperties.getAppName().toUpperCase(Locale.ENGLISH).indexOf("SYSTEM") < 0){
            executeAuth();
        }
    }

    /**
     * 定时任务处理system系统
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void checkSystemServerAuth() {
        AssertUtils.isNull(serverAuthProperties.getAppName(), "服务名不能为空！");
        if(serverAuthProperties.getAppName().toUpperCase(Locale.ENGLISH).indexOf("SYSTEM") > -1) {
            executeAuth();
        }
    }

    /**
     * 授权执行
     */
    private void executeAuth(){
        Result result = serverAuthService.verifyServerAuth("0","1");
        if (result.getData().equals(true)) {
            System.exit(0);
        } else {
            log.info("定时校验授权成功!");
        }
    }
}
