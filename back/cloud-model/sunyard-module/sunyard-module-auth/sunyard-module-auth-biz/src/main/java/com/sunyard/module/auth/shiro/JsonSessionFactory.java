package com.sunyard.module.auth.shiro;
/*
 * Project: SunAM
 *
 * File Created at 2022/6/2
 *
 * Copyright 2016 Corporation Limited. All rights reserved.
 *
 * This software is the confidential and proprietary information of Company. ("Confidential Information"). You shall not
 * disclose such Confidential Information and shall use it only in accordance with the terms of the license.
 */

import org.apache.shiro.session.Session;
import org.apache.shiro.session.mgt.SessionContext;
import org.apache.shiro.session.mgt.SessionFactory;

/**
 * @author Leo
 * @Desc
 * @date 2022/6/2 7:52
 */
public class JsonSessionFactory implements SessionFactory {
    @Override
    public Session createSession(SessionContext initData) {
        if (initData != null) {
            String host = initData.getHost();
            if (host != null) {
                return new JsonSession(host);
            }
        }
        return new JsonSession();
    }
}
/**
 * Revision history -------------------------------------------------------------------------
 * <p>
 * Date Author Note ------------------------------------------------------------------------- 2022/6/2 Leo creat
 */
