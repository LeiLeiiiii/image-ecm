package com.sunyard.module.system.api;

import cn.hutool.core.lang.tree.Tree;
import com.sunyard.framework.common.result.Result;
import com.sunyard.module.system.api.dto.RouterDTO;
import com.sunyard.module.system.api.dto.SysMenuDTO;
import com.sunyard.module.system.api.dto.SysUserDTO;
import com.sunyard.module.system.constant.ApiConstants;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;
import java.util.List;

/**
 * @author huronghao
 * @Type
 * @Desc
 * @date 2023-05-18 17:12
 */
@FeignClient(value = ApiConstants.NAME)
public interface MenuApi {

    String PREFIX = ApiConstants.PREFIX + "/menu/";

    /**
     * 查询菜单
     *
     * @param menuIds 菜单id
     * @return Result
     */
    @PostMapping(PREFIX + "searchMenuList")
    Result<List<SysMenuDTO>> searchMenuList(@RequestParam("menuIds") Long[] menuIds);

    /**
     * 菜单权限查询 接口 查询vue前端路由菜单
     *
     * @param userId 用户id
     * @param loginType 登录类别
     * @return Result
     */
    @PostMapping(PREFIX + "searchRouters")
    Result<List<RouterDTO>> searchRouters(@RequestParam("userId") Long userId,
        @RequestParam("loginType") Integer loginType);

    /**
     * 根据用户、菜单根节点，获取对应的菜单权限路由
     * @param sysUserDTO 用户obj
     * @return Result
     */
    @PostMapping(PREFIX + "getMenuRoutersByRoot")
    Result<Object> getMenuRoutersByRoot(@RequestBody SysUserDTO sysUserDTO);

    /**
     * 根据菜单根节点，获取功能权限菜单树
     * @param menuRootPerms 菜单参数
     * @return Result 树形id
     */
    @PostMapping(PREFIX + "getMenuTree")
    Result<List<Tree<Long>>> getMenuTree(@RequestParam("menuRootPerms") String menuRootPerms);

    /**
     * 根据菜单权限标识，获取有权限按钮列表
     * @param userId 用户id
     * @param menuPerms 菜单参数
     * @return Result 按钮map
     */
    @PostMapping(PREFIX + "getRightButtonListByMenuPerms")
    Result<List<HashMap<String, String>>> getRightButtonListByMenuPerms(@RequestParam("userId") Long userId,
                                                                    @RequestParam("menuPerms") String menuPerms);



    /**
     * 根据菜单权限标识，获取有权限按钮列表
     * @param roles 角色obj
     * @param menuPerms 菜单obj
     * @return Result
     */
    @PostMapping(PREFIX + "getRightButtonListByRoles")
    Result<List<HashMap<String, String>>> getRightButtonListByRoles(@RequestParam("roles") List<String> roles,
                                                                        @RequestParam("menuPerms") String menuPerms);

    /**
     * 根据菜单系统id获取菜单
     */
    @PostMapping(PREFIX + "getPermsByMenuSystem")
    Result<List<String>> getPermsByMenuSystem(@RequestParam("menuSystemId") Integer menuSystemId);
}
