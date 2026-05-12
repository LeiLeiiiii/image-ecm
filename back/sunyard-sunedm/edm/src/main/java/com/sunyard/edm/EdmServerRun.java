package com.sunyard.edm;
/* Project: com.sunyard.am
 *
 * File Created at 2021/6/28
 *
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license.
 */

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;


/**
 * @author zhouleibin
 * @Type com.sunyard.am
 * @Desc
 * @date 2021/6/28 15:50
 */
@EnableAsync
@EnableScheduling
@SpringBootApplication
public class EdmServerRun {

    public static void main(String[] args) {
        SpringApplication.run(EdmServerRun.class, args);
    }
}
/**
 * Revision history
 * -------------------------------------------------------------------------
 * <p>
 * Date Author Note
 * -------------------------------------------------------------------------
 * 2021/6/28 zhouleibin creat
 */
