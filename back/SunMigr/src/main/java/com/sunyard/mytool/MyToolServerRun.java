package com.sunyard.mytool;

import org.dromara.easyes.starter.register.EsMapperScan;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@MapperScan("com.sunyard.mytool.mapper.db")
@EsMapperScan("com.sunyard.mytool.mapper.es")
@SpringBootApplication
@EnableTransactionManagement
@EnableScheduling
@EnableAsync
public class MyToolServerRun {
    public static void main(String[] args) {
        SpringApplication.run(MyToolServerRun.class, args);
    }
}
