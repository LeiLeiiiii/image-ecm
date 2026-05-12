package com.sunyard.module.system.controller;

import javax.annotation.Resource;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sunyard.framework.common.page.PageForm;
import com.sunyard.framework.common.result.Result;
import com.sunyard.framework.log.annotation.OperationLog;
import com.sunyard.module.system.constant.LogsPrefixConstants;
import com.sunyard.module.system.service.OrgRoleService;
import com.sunyard.module.system.vo.SysRoleVO;

/**
 * 基础管理/通用管理/角色管理
 *
 * @Author zhao-yang 2021/7/6 9:02
 */
@RestController
@RequestMapping("basics/currency/role")
public class BasicsCurrencyRoleController extends BaseController {
    private static final String BASELOG = LogsPrefixConstants.MENU_CURRENCY + "-角色管理->";
    @Resource
    private OrgRoleService orgRoleService;

    /**
     * 查询角色详情信息
     *
     * @param roleId 角色id
     * @return result
     */
    @OperationLog(BASELOG + "查询角色详情信息")
    @PostMapping("select")
    public Result select(Long roleId) {
        return Result.success(orgRoleService.select(roleId, getToken().getId()));
    }

    /**
     * 查询角色（通用型）
     *
     * @param name   角色名称
     * @param instId 机构id
     * @param deptId 部门id
     * @param page   分页参数
     * @return result
     */
    @OperationLog(BASELOG + "查询角色（通用型）")
    @PostMapping("search")
    public Result search(String name, Long instId, Long deptId, PageForm page) {
        return Result.success(orgRoleService.search(name, instId, deptId, page));
    }

    /**
     * 新增角色
     *
     * @param role 角色obj
     * @return result
     */
    @OperationLog(BASELOG + "新增角色")
    @PostMapping("add")
    public Result add(SysRoleVO role) {
        orgRoleService.add(role);
        return Result.success(true);
    }

    /**
     * 查询菜单
     *
     * @return result
     */
    @OperationLog(BASELOG + "查询菜单")
    @PostMapping("searchMenus")
    public Result searchMenus() {
        return Result
                .success(orgRoleService.searchMenus(getToken().getId(), getToken().getLoginType()));
    }

    /**
     * 查询菜单
     *
     * @return result
     */
    @OperationLog(BASELOG + "查询菜单")
    @PostMapping("searchMenusAll")
    public Result searchMenusAll() {
        return Result.success(
                orgRoleService.searchMenusAll(getToken().getId(), getToken().getLoginType()));
    }

    /**
     * 修改角色信息
     *
     * @param role 角色obj
     * @return result
     */
    @OperationLog(BASELOG + "修改角色信息")
    @PostMapping("update")
    public Result update(SysRoleVO role) {
        orgRoleService.update(role);
        return Result.success(true);
    }

    /**
     * 删除角色信息
     *
     * @param roleId 角色id
     * @return result
     */
    @OperationLog(BASELOG + "删除角色信息")
    @PostMapping("delete")
    public Result delete(Long roleId) {
        orgRoleService.delete(roleId);
        return Result.success(true);
    }

    /**
     * 状态修改
     *
     * @param roleId 角色id
     * @return result
     */
    @OperationLog(BASELOG + "状态修改")
    @PostMapping("updateStatus")
    public Result updateStatus(Long roleId) {
        orgRoleService.updateStatus(roleId);
        return Result.success(true);
    }

}
