package com.sunyard.module.auth.shiro;
/*
 * Project: Sunyard
 *
 * File Created at 2024/7/3
 *
 * Copyright 2016 Corporation Limited. All rights reserved.
 *
 * This software is the confidential and proprietary information of Company. ("Confidential Information"). You shall not
 * disclose such Confidential Information and shall use it only in accordance with the terms of the license.
 */

import java.io.Serializable;

import javax.servlet.ServletRequest;

import org.apache.shiro.session.Session;
import org.apache.shiro.session.mgt.SessionKey;
import org.apache.shiro.web.session.mgt.DefaultWebSessionManager;
import org.apache.shiro.web.session.mgt.WebSessionKey;

/**
 * @author Leo
 * @Desc
 * @date 2024/7/3 12:33
 */
public class ShiroSessionManager extends DefaultWebSessionManager {

    public ShiroSessionManager() {
        super();
    }

    /**
     * 重写这个方法为了减少多次从redis中读取session（自定义redisSessionDao中的doReadSession方法）
     */
    @Override
    protected Session retrieveSession(SessionKey sessionKey) {
        Serializable sessionId = getSessionId(sessionKey);
        ServletRequest request = null;
        if (sessionKey instanceof WebSessionKey) {
            request = ((WebSessionKey)sessionKey).getServletRequest();
        }
        if (request != null && sessionId != null) {
            Session session = (Session)request.getAttribute(sessionId.toString());
            if (session != null) {
                return session;
            }
        }
        Session session = super.retrieveSession(sessionKey);
        if (request != null && sessionId != null) {
            request.setAttribute(sessionId.toString(), session);
        }
        return session;
    }
}
/**
 * Revision history ------------------------------------------------------------------------- Date Author Note
 * ------------------------------------------------------------------------- 2024/7/3 Leo creat
 */