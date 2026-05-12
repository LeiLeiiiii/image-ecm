package com.sunyard.framework.licence.config;

import javax.annotation.Resource;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.sunyard.framework.common.result.Result;
import com.sunyard.framework.licence.task.service.ServerAuthService;

import lombok.extern.slf4j.Slf4j;

/**
 * @title： 启动授权校验
 */
@Slf4j
@Component
public class ServerAuthVerify implements CommandLineRunner {

    @Resource
    private ServerAuthService serverAuthService;

    @Override
    public void run(String... args) {
        Result result = serverAuthService.verifyServerAuth("0","0");
        if (result.getData().equals(true)) {
            System.exit(0);
        } else {
            log.info("启动授权成功!");
        }
    }
}
