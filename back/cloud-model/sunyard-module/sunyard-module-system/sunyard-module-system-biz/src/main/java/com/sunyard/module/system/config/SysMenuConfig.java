package com.sunyard.module.system.config;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sunyard.framework.redis.util.RedisUtils;
import com.sunyard.module.system.constant.CachePrefixConstants;
import com.sunyard.module.system.constant.MenuConstants;
import com.sunyard.module.system.mapper.SysMenuMapper;
import com.sunyard.module.system.po.SysMenu;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Configuration
@Slf4j
public class SysMenuConfig {
    @Resource
    private RedisUtils redisUtils;
    @Resource
    private SysMenuMapper sysMenuMapper;

    /**
     * 初始化所有系统菜单数据到redis
     */
    @PostConstruct
    public void init() {
        try {
            List<SysMenu> menus = sysMenuMapper.selectList(
                    new LambdaQueryWrapper<SysMenu>().eq(SysMenu::getStatus, MenuConstants.YES_FRAME)
            );
            Map<String, List<String>> permsMap = menus.stream()
                    .filter(menu -> menu.getMenuSystem() != null && menu.getPerms() != null)
                    .collect(Collectors.groupingBy(
                            menu -> String.valueOf(menu.getMenuSystem()), // 转成 String
                            Collectors.mapping(SysMenu::getPerms, Collectors.toList())
                    ));

            Map<String, Object> hashData = new HashMap<>();
            for (Map.Entry<String, List<String>> entry : permsMap.entrySet()) {
                String jsonValue = JSON.toJSONString(entry.getValue());
                hashData.put(entry.getKey(), jsonValue);
            }

            redisUtils.hmset(CachePrefixConstants.SYS_MENU, hashData);
        } catch (Exception e) {
            log.error("初始化菜单缓存失败", e);
        }
    }
}
