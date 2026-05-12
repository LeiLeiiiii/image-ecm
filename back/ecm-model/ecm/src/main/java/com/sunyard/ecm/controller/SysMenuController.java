package com.sunyard.ecm.controller;

import javax.annotation.Resource;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sunyard.ecm.constant.LogsConstants;
import com.sunyard.ecm.constant.RoleConstants;
import com.sunyard.ecm.service.SysMenuService;
import com.sunyard.framework.common.result.Result;
import com.sunyard.framework.log.annotation.OperationLog;
import com.sunyard.module.system.api.dto.SysUserDTO;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * @author： ty
 * @create： 2023/5/5 10:23
 * @Desc: 系统模块-菜单列表
 */
@Api(tags = "系统模块-菜单列表")
@RestController
@RequestMapping("sys/menu")
public class SysMenuController extends BaseController {
    @Resource
    private SysMenuService sysMenuService;

    @ApiOperation("获取菜单权限路由")
    @OperationLog(LogsConstants.ICMS + "获取菜单权限路由")
    @PostMapping("getMenuRouters")
    public Result getMenuRouters() {
        SysUserDTO sysUserDTO = new SysUserDTO();
        sysUserDTO.setUserId(getToken().getId());
        sysUserDTO.setType(getToken().getLoginType());
        sysUserDTO.setMenuRootPerms(RoleConstants.SUNICMS);
        return sysMenuService.getMenuRoutersByRoot(sysUserDTO);
    }

}
