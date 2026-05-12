package com.sunyard.sunafm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @author P-JWei
 * @date 2024/3/7 11:17:18
 * @title
 * @description
 */
@EnableScheduling
@SpringBootApplication
public class AfmServerRun {

    public static void main(String[] args) {
        SpringApplication.run(AfmServerRun.class, args);
    }
}
