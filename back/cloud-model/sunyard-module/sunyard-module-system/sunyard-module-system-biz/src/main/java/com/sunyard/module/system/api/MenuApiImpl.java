package com.sunyard.module.system.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sunyard.framework.common.result.Result;
import com.sunyard.framework.common.util.AssertUtils;
import com.sunyard.module.system.api.dto.MetaDTO;
import com.sunyard.module.system.api.dto.RouterDTO;
import com.sunyard.module.system.api.dto.SysMenuDTO;
import com.sunyard.module.system.api.dto.SysUserDTO;
import com.sunyard.module.system.constant.MenuConstants;
import com.sunyard.module.system.constant.RoleConstants;
import com.sunyard.module.system.constant.StateConstants;
import com.sunyard.module.system.mapper.SysMenuMapper;
import com.sunyard.module.system.mapper.SysRoleMapper;
import com.sunyard.module.system.mapper.SysRoleMenuMapper;
import com.sunyard.module.system.mapper.SysRoleUserMapper;
import com.sunyard.module.system.po.SysMenu;
import com.sunyard.module.system.po.SysRole;
import com.sunyard.module.system.po.SysRoleMenu;
import com.sunyard.module.system.po.SysRoleUser;
import com.sunyard.module.system.service.SysMenuService;

import cn.hutool.core.lang.tree.Tree;
import cn.hutool.core.lang.tree.TreeNodeConfig;
import cn.hutool.core.lang.tree.TreeUtil;

/**
 * 组织管理-菜单管理
 *
 * @author huronghao
 * @date 2023-05-18 17:14
 */
@RestController
public class MenuApiImpl implements MenuApi {
    @Resource
    private SysMenuService service;
    @Resource
    private SysMenuMapper sysMenuMapper;
    @Resource
    private SysRoleUserMapper sysRoleUserMapper;
    @Resource
    private SysRoleMenuMapper sysRoleMenuMapper;
    @Resource
    private SysRoleMapper sysRoleMapper;

    @Override
    public Result<List<SysMenuDTO>> searchMenuList(Long[] menuIds) {
        return Result.success(service.searchMenuList(menuIds));
    }

    @Override
    public Result<List<RouterDTO>> searchRouters(Long userId, Integer loginType) {
        return Result.success(service.searchRouters(userId, loginType,null));
    }

    @Override
    public Result<Object> getMenuRoutersByRoot(SysUserDTO sysUserDTO) {
        Long userId = sysUserDTO.getUserId();
        Integer type = sysUserDTO.getType();
        String menuRootPerms = sysUserDTO.getMenuRootPerms();
        List<SysMenu> list = new ArrayList<>();
        //获取系统菜单根节点
        List<SysMenu> icmsRoot = sysMenuMapper.selectList(new LambdaQueryWrapper<SysMenu>()
                .eq(SysMenu::getPerms, menuRootPerms));
        list.addAll(icmsRoot);
        if (type != null && type.equals(StateConstants.LOGIN_TYPE_999)) {
            list.addAll(sysMenuMapper.selectList(new LambdaQueryWrapper<SysMenu>()
                    .select(SysMenu::getMenuId, SysMenu::getParentId, SysMenu::getMenuName, SysMenu::getPerms, SysMenu::getPath, SysMenu::getComponent, SysMenu::getMenuType, SysMenu::getIsCache, SysMenu::getIsFrame, SysMenu::getIcon, SysMenu::getStatus, SysMenu::getVisible, SysMenu::getOrderNum, SysMenu::getRemark)
                    .groupBy(SysMenu::getMenuId, SysMenu::getParentId, SysMenu::getMenuName, SysMenu::getPerms, SysMenu::getPath, SysMenu::getComponent, SysMenu::getMenuType, SysMenu::getIsCache, SysMenu::getIsFrame, SysMenu::getIcon, SysMenu::getStatus, SysMenu::getVisible, SysMenu::getOrderNum, SysMenu::getRemark)
                    .orderByAsc(SysMenu::getOrderNum)
            ));
        } else if(!CollectionUtils.isEmpty(sysUserDTO.getRoleCode())){
            //获取角色的菜单权限
            List<SysRole> sysRoles = sysRoleMapper.selectList(new LambdaQueryWrapper<SysRole>().in(SysRole::getRoleCode, sysUserDTO.getRoleCode()));
            if(!CollectionUtils.isEmpty(sysRoles)){
                List<Long> roleids = sysRoles.stream().map(SysRole::getRoleId).collect(Collectors.toList());
                List<SysRoleMenu> roleMenus = sysRoleMenuMapper.selectList(new LambdaQueryWrapper<SysRoleMenu>()
                        .in(SysRoleMenu::getRoleId,roleids));
                if (!CollectionUtils.isEmpty(roleMenus)) {
                    List<Long> menuIds = roleMenus.stream().map(SysRoleMenu::getMenuId).collect(Collectors.toList());
                    List<SysMenu> menus = sysMenuMapper.selectList(new LambdaQueryWrapper<SysMenu>()
                            .in(SysMenu::getMenuId, menuIds)
                            .eq(SysMenu::getStatus,RoleConstants.ZERO)
                            .ne(SysMenu::getMenuType, RoleConstants.BUTTON).ne(SysMenu::getParentId, RoleConstants.ZERO));
                    list.addAll(menus);
                }
            }
        }else{
            //获取用户的角色
            List<SysRoleUser> roleUsers = sysRoleUserMapper.selectList(new LambdaQueryWrapper<SysRoleUser>()
                    .eq(SysRoleUser::getUserId, userId));
            if (!CollectionUtils.isEmpty(roleUsers)) {
                List<Long> roleIds = roleUsers.stream().map(SysRoleUser::getRoleId).collect(Collectors.toList());
                //获取角色的菜单权限
                List<SysRoleMenu> roleMenus = sysRoleMenuMapper.selectList(new LambdaQueryWrapper<SysRoleMenu>()
                        .in(SysRoleMenu::getRoleId, roleIds));
                if (!CollectionUtils.isEmpty(roleMenus)) {
                    List<Long> menuIds = roleMenus.stream().map(SysRoleMenu::getMenuId).collect(Collectors.toList());
                    List<SysMenu> menus = sysMenuMapper.selectList(new LambdaQueryWrapper<SysMenu>()
                            .in(SysMenu::getMenuId, menuIds)
                            .eq(SysMenu::getStatus,RoleConstants.ZERO)
                        .ne(SysMenu::getMenuType, RoleConstants.BUTTON).ne(SysMenu::getParentId, RoleConstants.ZERO));
                    list.addAll(menus);
                }
            }
        }
        String s = JSON.toJSONString(getRouters(list));
        return Result.success(JSON.parse(s));
    }

    @Override
    public Result<List<Tree<Long>>> getMenuTree(String menuRootPerms) {
        //获取影像内容管理系统菜单根节点
        List<SysMenu> icmsRoot = sysMenuMapper.selectList(new LambdaQueryWrapper<SysMenu>().eq(SysMenu::getPerms, menuRootPerms));
        List<SysMenu> menus = sysMenuMapper.selectList(null);
        if (CollectionUtils.isEmpty(icmsRoot)) {
            return Result.success(null);
        }
        //构建树
        TreeNodeConfig treeNodeConfig = new TreeNodeConfig();
        //通过权重解决导致排序混乱问题
        treeNodeConfig.setWeightKey("order");
        List<Tree<Long>> treeList = TreeUtil.build(menus, icmsRoot.get(0).getMenuId(), treeNodeConfig, (treeNode, tree) -> {
            tree.setId(treeNode.getMenuId());
            tree.setParentId(treeNode.getParentId());
            tree.setName(treeNode.getMenuName());
            tree.setWeight(treeNode.getOrderNum());
        });
        return Result.success(treeList);
    }

    @Override
    public Result<List<HashMap<String, String>>> getRightButtonListByMenuPerms(Long userId, String menuPerms) {
        AssertUtils.isNull(userId, "参数错误");
        //根据用户id获取当前关联的角色id
        List<SysRoleUser> roleUsers = sysRoleUserMapper.selectList(new LambdaQueryWrapper<SysRoleUser>()
                .eq(SysRoleUser::getUserId, userId));
        if (CollectionUtils.isEmpty(roleUsers)) {
            return Result.success(Collections.emptyList());
        }
        List<Long> roleIds = roleUsers.stream().map(SysRoleUser::getRoleId).distinct().collect(Collectors.toList());
        //根据角色id获取关联菜单id
        return getListResult(menuPerms, roleIds);
    }

    @Override
    public Result<List<HashMap<String, String>>> getRightButtonListByRoles(List<String> roles, String menuPerms) {
        AssertUtils.isNull(roles, "参数错误");
        List<SysRole> sysRoles = sysRoleMapper.selectList(new LambdaQueryWrapper<SysRole>().in(SysRole::getRoleCode, roles));
        List<Long> roleIds = sysRoles.stream().map(SysRole::getRoleId).collect(Collectors.toList());
        return getListResult(menuPerms, roleIds);
    }

    @Override
    public Result<List<String>> getPermsByMenuSystem(Integer menuSystemId) {
        AssertUtils.isNull(menuSystemId, "参数错误");
        List<String> permsByMenuSystem = service.getPermsByMenuSystem(menuSystemId);
        return Result.success(permsByMenuSystem);
    }

    private Result<List<HashMap<String, String>>> getListResult(String menuPerms, List<Long> roleIds) {
        //根据角色id获取关联菜单id
        List<SysRoleMenu> roleMenus = sysRoleMenuMapper.selectList(new LambdaQueryWrapper<SysRoleMenu>()
                .in(SysRoleMenu::getRoleId, roleIds));
        if (CollectionUtils.isEmpty(roleMenus)) {
            return Result.success(Collections.emptyList());
        }
        List<Long> menuIds = roleMenus.stream().map(SysRoleMenu::getMenuId).distinct().collect(Collectors.toList());
        //根据菜单id获取有权限的按钮类型的菜单id
        List<SysMenu> buttonsRight = sysMenuMapper.selectList(new LambdaQueryWrapper<SysMenu>()
                .eq(SysMenu::getStatus,StateConstants.ZERO)
                .eq(SysMenu::getMenuType, RoleConstants.BUTTON)
                .in(SysMenu::getMenuId, menuIds));
        if (CollectionUtils.isEmpty(buttonsRight)) {
            return Result.success(Collections.emptyList());
        }
        List<Long> buttonIdsRight = buttonsRight.stream().map(SysMenu::getMenuId).distinct().collect(Collectors.toList());
        //获取对应菜单下所有功能按钮列表
        List<SysMenu> buttonList = getButtonsByIcmsType(menuPerms);
        //获取当前角色所对应的按钮列表
        List<HashMap<String, String>> buttonListRight = new ArrayList<>();
        for (SysMenu button : buttonList) {
            if (buttonIdsRight.contains(button.getMenuId())) {
                HashMap<String, String> buttonRight = new HashMap<>(6);
                buttonRight.put("name", button.getMenuName());
                buttonRight.put("perms", button.getPerms());
                buttonListRight.add(buttonRight);
            }
        }
        return Result.success(buttonListRight);
    }

    private List<SysMenu> getButtonsByIcmsType(String menuPerms) {
        List<SysMenu> imageCapture = sysMenuMapper.selectList(new LambdaQueryWrapper<SysMenu>().eq(SysMenu::getStatus,StateConstants.ZERO)
                .eq(SysMenu::getPerms, menuPerms));
        List<SysMenu> buttonList = sysMenuMapper.selectList(new LambdaQueryWrapper<SysMenu>()
                .eq(SysMenu::getStatus,StateConstants.ZERO)
                .eq(SysMenu::getParentId, imageCapture.get(0).getMenuId())
            .eq(SysMenu::getMenuType, RoleConstants.BUTTON)
                .orderByAsc(SysMenu::getOrderNum));
        return buttonList;
    }

    /**
     * 构建父子菜单树
     */
    private List<RouterDTO> getRouters(List<SysMenu> list) {
        List<RouterDTO> dbrouters = this.dbMenusToRouters(list);
        List<RouterDTO> routers = new ArrayList<>();
        //显示排序
        dbrouters = dbrouters.stream().sorted(Comparator.comparing(RouterDTO::getOrderNum)).collect(Collectors.toList());
        for (RouterDTO router : dbrouters) {
            if ("0".equals(router.getParentId())) {
                routers.add(router);
            }
            for (RouterDTO cRouter : dbrouters) {
                if (router.getId().equals(cRouter.getParentId())) {
                    if (ObjectUtils.isEmpty(router.getChildren())) {
                        router.setChildren(new ArrayList<>());
                    } else {
                        router.setRedirect("noRedirect");
                    }
                    router.setAlwaysShow(true);
                    router.getChildren().add(cRouter);
                }
            }
        }
        return routers;
    }

    /**
     * db转vue路由
     *
     * @param list
     * @return Result
     */
    private List<RouterDTO> dbMenusToRouters(List<SysMenu> list) {
        List<RouterDTO> routers = new ArrayList<>();
        for (SysMenu menu : list) {
            RouterDTO router = new RouterDTO();
            router.setId(String.valueOf(menu.getMenuId()));
            router.setParentId(String.valueOf(menu.getParentId()));
            router.setName(StringUtils.capitalize(menu.getPerms()));
            router.setPath(menu.getPerms());
            router.setMeta(new MetaDTO(menu.getMenuName(), menu.getIcon(),
                    menu.getIsCache() == 0));
            router.setOrderNum(menu.getOrderNum());
            if ("0".equals(router.getParentId())) {
                router.setPath("/" + menu.getPerms());
                router.setComponent(MenuConstants.LAYOUT);
            }
            if (StringUtils.hasText(menu.getComponent())) {
                router.setComponent(menu.getComponent());
            }
            routers.add(router);
        }
        return routers;
    }
}
