package com.sunyard.module.system.api.dto; /*
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

import java.util.List;

/**
 * @author zhaojianmou
 * @Desc
 * @date 2025/8/26 15:27
 */
@Data
public class SmsSendBatchSmsRequestDTO {
    /**  短信发送对接渠道模板id */
    private String templateNum;
    /**  批次号 */
    private String batchNo;
    /** 批量发送短信手机号参数 */
    private List<SmsDetailDTO> details;
}
/**
 * Revision history
 * -------------------------------------------------------------------------
 * Date Author Note
 * -------------------------------------------------------------------------
 * 2025/8/26 mm creat
 */
