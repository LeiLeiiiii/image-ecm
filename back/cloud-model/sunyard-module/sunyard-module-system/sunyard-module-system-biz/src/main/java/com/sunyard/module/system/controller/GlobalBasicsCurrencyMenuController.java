package com.sunyard.module.system.controller;

import javax.annotation.Resource;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sunyard.framework.common.result.Result;
import com.sunyard.framework.log.annotation.OperationLog;
import com.sunyard.module.system.constant.LogsPrefixConstants;
import com.sunyard.module.system.service.OrgRoleService;
import com.sunyard.module.system.service.SysMenuService;
import com.sunyard.module.system.vo.SysUserVO;

/**
 * 全局/菜单管理
 *
 * @Author jin-min 2021/7/19
 */
@RestController
@RequestMapping("global")
public class GlobalBasicsCurrencyMenuController extends BaseController {
    private static final String BASELOG = LogsPrefixConstants.GLOBAL_MENU + "->";
    @Resource
    private SysMenuService sysMenuService;
    @Resource
    private OrgRoleService orgRoleService;

    /**
     * 查询菜单
     *
     * @param type 系统类型 0基础、1会计、2企业、3影像、4文档
     * @return result
     */
    @OperationLog(BASELOG + "查询菜单")
    @PostMapping("searchRouters")
    public Result searchRouters(Integer type) {
        return Result.success(
                sysMenuService.searchRouters(getToken().getId(), getToken().getLoginType(), type));
    }

    /**
     * 查询所有菜单
     *
     * @return result
     */
    @OperationLog(BASELOG + "查询所有菜单")
    @PostMapping("searchRoutersAll")
    public Result searchRoutersAll() {
        return Result.success(sysMenuService.searchRoutersAll());
    }

    /**
     * 设置默认菜单
     *
     * @param menuId 菜单id
     * @return result
     */
    @OperationLog(BASELOG + "设置默认菜单")
    @PostMapping("addDefaultMenu")
    public Result addDefaultMenu(Long menuId) {
        sysMenuService.addDefaultMenu(menuId, getToken().getId());
        return Result.success();
    }

    /**
     * 取消默认菜单
     * @param menuId 菜单id
     * @return result
     */
    @OperationLog(BASELOG + "取消默认菜单")
    @PostMapping("cancelDefaultMenu")
    public Result cancelDefaultMenu(Long menuId) {
        sysMenuService.cancelDefaultMenu(menuId, getToken().getId());
        return Result.success();
    }

    /**
     * 修改布局
     * @param sysUserVO 用户obj
     * @return Result
     */
    @OperationLog(BASELOG + "修改布局")
    @PostMapping("updateLayout")
    public Result updateLayout(SysUserVO sysUserVO) {
        sysMenuService.updateLayout(sysUserVO, getToken().getId());
        return Result.success();
    }

    /**
     * 获取所有菜单
     * @return Result
     */
    @OperationLog(BASELOG + "获取所有菜单")
    @PostMapping("searchDir")
    public Result searchDir() {
        return Result.success(sysMenuService.searchDir());
    }

    /**
     * 查询当前用户权限菜单---档案系统有使用
     * @return
     */
    @OperationLog(BASELOG + "查询当前用户权限菜单")
    @PostMapping("selectAuthMenuButton")
    public Result selectAuthMenuButton() {
        return Result.success(orgRoleService.selectAuthMenuButton(getToken()));
    }

    /**
     * 查询当前用户权限菜单(按钮)---影像系统使用
     * @return
     */
    @OperationLog(BASELOG + "查询当前用户权限菜单按钮")
    @PostMapping("selectAuthButton")
    public Result selectAuthButton() {
        return Result.success(orgRoleService.selectAuthButton(getToken()));
    }
}
