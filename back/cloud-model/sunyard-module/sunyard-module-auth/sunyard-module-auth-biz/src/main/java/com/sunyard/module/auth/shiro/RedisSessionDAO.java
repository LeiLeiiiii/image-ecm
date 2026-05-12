package com.sunyard.module.auth.shiro;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.shiro.session.Session;
import org.apache.shiro.session.mgt.eis.EnterpriseCacheSessionDAO;

import com.sunyard.module.auth.constant.CachePrefixConstants;

import lombok.extern.slf4j.Slf4j;

/**
 * @Type RedisSessionDAO.java
 * @Desc
 * @author zhouleibin-bwf
 * @date 2018年7月24日 上午10:04:06
 * @version
 */
@Slf4j
public class RedisSessionDAO extends EnterpriseCacheSessionDAO {

    @Override
    public Collection<Session> getActiveSessions() {
        Set<Session> sessions = new HashSet<Session>();
        String namePrefix = getActiveSessionsCacheName();
        Set<Object> keys = getCacheManager().getCache(namePrefix).keys();
        if (keys != null && !keys.isEmpty()) {
            for (Object key : keys) {
                Session s = null;
                try {
                    //ShiroCache 已经增加了前缀，这边需要过滤替换掉
                    s = (Session) getCacheManager().getCache(namePrefix)
                            .get(key.toString().replace(CachePrefixConstants.AUTH+ namePrefix + ":", ""));
                } catch (Exception e) {
                    log.error("系统异常", e);
                    continue;
                }
                sessions.add(s);
            }
        }
        return sessions;
    }
}

/**
 * Revision history -------------------------------------------------------------------------
 *
 * Date Author Note ------------------------------------------------------------------------- 2018年9月12日 zhouleibin-bwf
 * creat
 */
