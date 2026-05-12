package com.sunyard.module.system.api.dto;
/*
 * Project: SunAM
 *
 * File Created at 2025/8/15
 *
 * Copyright 2016 Corporation Limited.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Company. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license.
 */

import lombok.Data;

import java.io.Serializable;
import java.util.HashMap;

/**
 * @Desc 批量发送短信手机号参数
 * @author Leo
 * @date 2025/8/15 11:07
 * @version
 */
@Data
public class SmsDetailDTO implements Serializable {
    /** 短信发送内容*/
    private HashMap<String, String> paramMap;
    /** 手机号码*/
    private String phoneNum;
    /** id*/
    private String id;
}
/**
 * Revision history
 * -------------------------------------------------------------------------
 * Date Author Note
 * -------------------------------------------------------------------------
 * 2025/8/15 Leo creat
 */
