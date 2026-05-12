package com.sunyard.ecm.service;

import com.sunyard.framework.common.result.Result;
import com.sunyard.module.system.api.MenuApi;
import com.sunyard.module.system.api.dto.SysUserDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author： wjj
 * @create： 2025/5/15
 * @Desc: 系统模块-菜单列表实现类
 */
@Slf4j
@Service
public class SysMenuService {
    @Resource
    private MenuApi menuApi;

    /**
     * 获取菜单权限路由
     */
    public Result getMenuRoutersByRoot(SysUserDTO sysUserDTO) {
        return menuApi.getMenuRoutersByRoot(sysUserDTO);
    }
}
