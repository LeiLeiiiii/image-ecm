package com.sunyard.module.auth;
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
 * @author zhouleibin
 * @Type com.sunyard.am
 * @Desc
 * @date 2021/6/28 16:16
 */

@SpringBootApplication
public class AuthServerRun {
    public static void main(String[] args) {
        SpringApplication.run(AuthServerRun.class, args);
    }

}
