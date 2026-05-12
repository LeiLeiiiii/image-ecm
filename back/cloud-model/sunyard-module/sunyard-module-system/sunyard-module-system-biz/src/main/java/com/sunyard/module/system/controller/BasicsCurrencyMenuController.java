package com.sunyard.module.system.controller;

import com.sunyard.framework.common.page.PageForm;
import com.sunyard.framework.common.result.Result;
import com.sunyard.framework.log.annotation.OperationLog;
import com.sunyard.module.system.constant.LogsPrefixConstants;
import com.sunyard.module.system.po.SysMenu;
import com.sunyard.module.system.service.SysMenuService;
import com.sunyard.module.system.vo.SysMenuVO;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * 基础管理/通用管理/菜单管理
 *
 * @Author jin-min 2021/7/19
 */
@RestController
@RequestMapping("basics/currency/menu")
public class BasicsCurrencyMenuController extends BaseController {
    private static final String BASELOG = LogsPrefixConstants.MENU_CURRENCY + "-菜单管理->";
    @Resource
    private SysMenuService sysMenuService;

    /**
     * 获取菜单列表
     *
     * @param page 分页
     * @param menu 菜单obj
     * @return result
     */
    @OperationLog(BASELOG + "获取菜单列表")
    @PostMapping("search")
    public Result search(PageForm page, SysMenuVO menu) {
        if (null == menu.getMenuId()) {
            menu.setMenuId(0L);
        }
        return Result.success(sysMenuService.search(page, menu));
    }

    /**
     * 获取所有菜单列表
     *
     * @return result
     */
    @OperationLog(BASELOG + "获取所有菜单列表")
    @PostMapping("searchAll")
    public Result searchAll() {
        return Result.success(sysMenuService.searchAll());
    }

    /**
     * 新增菜单
     *
     * @param menu 菜单obj
     * @return result
     */
    @OperationLog(BASELOG + "新增菜单")
    @PostMapping("add")
    public Result add(@RequestBody SysMenu menu) {
        sysMenuService.add(menu);
        return Result.success(true);
    }

    /**
     * 编辑菜单
     *
     * @param menu 菜单obj
     * @return result
     */
    @OperationLog(BASELOG + "编辑菜单")
    @PostMapping("update")
    public Result update(@RequestBody SysMenu menu) {
        sysMenuService.update(menu);
        return Result.success(true);
    }

    /**
     * 菜单详情
     *
     * @param menuId 菜单id
     * @return result
     */
    @OperationLog(BASELOG + "菜单详情")
    @PostMapping("select")
    public Result select(String menuId) {
        return Result.success(sysMenuService.select(menuId));
    }

    /**
     * 删除菜单
     *
     * @param menuId 菜单id
     * @return result
     */
    @OperationLog(BASELOG + "删除菜单")
    @PostMapping("del")
    public Result del(String menuId) {
        sysMenuService.del(menuId);
        return Result.success(true);
    }

    /**
     * 置顶
     *
     * @param list 菜单list
     * @return result
     */
    @OperationLog(BASELOG + "置顶")
    @PostMapping("editSort")
    public Result editSort(@RequestBody List<SysMenu> list) {
        sysMenuService.editSort(list);
        return Result.success(true);
    }

    /**
     * 修改菜单状态
     *
     * @param menuId 菜单id
     * @param status 状态 0显示 1停用
     * @return result
     */
    @OperationLog(BASELOG + "修改菜单状态")
    @PostMapping("changeMenuStatus")
    public Result changeMenuStatus(Long[] menuId, Integer status) {
        sysMenuService.editStatus(menuId, status);
        return Result.success(true);
    }

}
