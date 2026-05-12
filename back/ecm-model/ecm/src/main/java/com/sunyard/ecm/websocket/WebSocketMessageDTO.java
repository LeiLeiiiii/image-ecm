package com.sunyard.ecm.websocket;
/*
 * Project: sunicms
 *
 * File Created at 2024/6/17
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
import java.util.List;

/**
 * @author Leo
 * @Desc
 * @date 2024/6/17 14:06
 */
@Data
public class WebSocketMessageDTO implements Serializable {
    /**
     * websocket客户端id
     */
    private String clientId;
    /**
     * 业务批次
     */
    private List<String> buisIdList;
    /**
     * 消息发送类型 默认：是空字符串（对应发送到busiId），all 发送给所有客户端
     */
    private String msgType = "";

    /**
     * 内容
     */
    private String contentText;

}
/**
 * Revision history
 * -------------------------------------------------------------------------
 * Date Author Note
 * -------------------------------------------------------------------------
 * 2024/6/17 Leo creat
 */