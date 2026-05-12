package com.sunyard.module.system.api.dto;
/*
 * Project: sunyard-cloud
 *
 * File Created at 2025/8/26
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
import java.util.HashMap;

/**
 * @author zjm
 * @Desc
 * @date 2025/8/26 9:20
 */
@Data
public class SmsDTO {
    /** 短信模版编号 */
    private String templateNum;
    /** 手机号 */
    private String telePhone;
    /** 参数map */
    private HashMap paramMap;


}
/**
 * Revision history
 * -------------------------------------------------------------------------
 * Date Author Note
 * -------------------------------------------------------------------------
 * 2025/8/26 mm creat
 */
