package com.sunyard.module.auth.oauth.config;
/*
 * Project: Sunyard
 *
 * File Created at 2024/7/4
 *
 * Copyright 2016 Corporation Limited. All rights reserved.
 *
 * This software is the confidential and proprietary information of Company. ("Confidential Information"). You shall not
 * disclose such Confidential Information and shall use it only in accordance with the terms of the license.
 */

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.sunyard.framework.common.util.encryption.Md5Utils;

/**
 * @author Leo
 * @Desc
 * @date 2024/7/4 14:32
 */
@Configuration
public class PasswordEncoderConfig {
    /***/
    @Bean
    public PasswordEncoder oAuthPasswordEncoder() {
        return new PasswordEncoder() {
            @Override
            public String encode(CharSequence charSequence) {
                return Md5Utils.md5Hex(String.valueOf(charSequence));
            }

            @Override
            public boolean matches(CharSequence charSequence, String s) {
                return s.equals(Md5Utils.md5Hex(String.valueOf(charSequence)));
            }
        };
    }
}
/**
 * Revision history ------------------------------------------------------------------------- Date Author Note
 * ------------------------------------------------------------------------- 2024/7/4 Leo creat
 */
