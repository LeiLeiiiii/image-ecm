package com.sunyard.module.storage;
/*
 * Project: com.sunyard.am
 *
 * File Created at 2021/6/28
 *
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of ("Confidential Information"). You shall not disclose
 * such Confidential Information and shall use it only in accordance with the terms of the license.
 */

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 存储服务主程序启动类
 *
 * @author zhouleibin
 * @Type com.sunyard.am
 * @date 2021/6/28 15:50
 */
@SpringBootApplication
public class StorageServerRun {

    public static void main(String[] args) {
        SpringApplication.run(StorageServerRun.class, args);
    }

}