package com.sunyard.module.system.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import com.baomidou.lock.annotation.Lock4j;
import com.baomidou.mybatisplus.core.batch.MybatisBatch;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.sunyard.framework.common.page.PageForm;
import com.sunyard.framework.common.token.AccountToken;
import com.sunyard.module.system.api.dto.SysMenuDTO;
import com.sunyard.module.system.constant.MenuConstants;
import com.sunyard.module.system.constant.StateConstants;
import com.sunyard.module.system.mapper.SysMenuMapper;
import com.sunyard.module.system.mapper.SysRoleMapper;
import com.sunyard.module.system.mapper.SysRoleMenuMapper;
import com.sunyard.module.system.mapper.SysRoleUserMapper;
import com.sunyard.module.system.mapper.SysUserAdminMapper;
import com.sunyard.module.system.po.SysMenu;
import com.sunyard.module.system.po.SysRole;
import com.sunyard.module.system.po.SysRoleMenu;
import com.sunyard.module.system.po.SysRoleUser;
import com.sunyard.module.system.po.SysUserAdmin;
import com.sunyard.module.system.vo.SysMenuVO;
import com.sunyard.module.system.vo.SysRoleVO;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;

/**
 * 组织机构角色管理
 *
 * @Author zhaoyang 2021/7/5 17:43
 */
@Service
public class OrgRoleService {

    @Resource
    private SqlSessionFactory sqlSessionFactory;
    @Resource
    private SysRoleMapper sysRoleMapper;
    @Resource
    private SysMenuMapper sysMenuMapper;
    @Resource
    private SysRoleUserMapper sysRoleUserMapper;
    @Resource
    private SysRoleMenuMapper sysRoleMenuMapper;
    @Resource
    private SysUserAdminMapper sysUserAdminMapper;

    /**
     * 查询角色详情信息
     *
     * @param roleId 角色id
     * @param userId 用户id
     * @return Result
     */
    public Map<String, Object> select(Long roleId, Long userId) {
        Assert.notNull(roleId, "参数错误");
        SysRole sysRole = sysRoleMapper.selectById(roleId);
        Assert.notNull(sysRole, "参数错误");
        Assert.notNull(roleId, "参数错误");
        List<SysMenu> menus = new ArrayList<>();
        SysUserAdmin sysUserAdmin = sysUserAdminMapper.selectById(userId);
        if (sysUserAdmin != null) {
            // 超级管理员，查看所有的
            menus = sysMenuMapper.selectList(new LambdaQueryWrapper<SysMenu>().orderByAsc(SysMenu::getOrderNum));
            if (!CollectionUtils.isEmpty(menus)) {
                //过滤掉状态为0的，且过滤掉子集
                List<SysMenu> collect1 = menus.stream()
                        .filter(s -> !StateConstants.ZERO.equals(s.getStatus()))
                        .collect(Collectors.toList());
                filterMenu(menus, collect1);
            }
        } else {
            List<SysRoleUser> userRole = sysRoleUserMapper
                    .selectList(new LambdaQueryWrapper<SysRoleUser>().eq(SysRoleUser::getUserId, userId));
            if (!CollectionUtils.isEmpty(userRole)) {
                List<Long> roles = userRole.stream().map(SysRoleUser::getRoleId)
                        .collect(Collectors.toList());
                List<SysRoleMenu> menuss = sysRoleMenuMapper
                        .selectList(new LambdaQueryWrapper<SysRoleMenu>().in(SysRoleMenu::getRoleId, roles));
                if (!CollectionUtils.isEmpty(menuss)) {
                    List<Long> collect = menuss.stream().map(SysRoleMenu::getMenuId)
                            .collect(Collectors.toList());
                    menus = sysMenuMapper.selectList(new LambdaQueryWrapper<SysMenu>()
                            .in(SysMenu::getMenuId, collect).orderByAsc(SysMenu::getOrderNum));
                    //过滤掉状态为0的，且过滤掉子集
                    List<SysMenu> collect1 = menus.stream()
                            .filter(s -> !StateConstants.ZERO.equals(s.getStatus()))
                            .collect(Collectors.toList());
                    filterMenu(menus, collect1);
                }
            }
        }
        List<SysRoleMenu> sysRoleMenus = sysRoleMenuMapper
                .selectList(new LambdaQueryWrapper<SysRoleMenu>().eq(SysRoleMenu::getRoleId, roleId));
        List<Long> menuIds = new ArrayList<>();
        if (!CollectionUtils.isEmpty(sysRoleMenus) && !CollectionUtils.isEmpty(menus)) {
            //如果menuIds 中id存在，但menus中的muenid不存在，前端会报错
            List<Long> menuAllIds = menus.stream().map(SysMenu::getMenuId)
                    .collect(Collectors.toList());
            menuIds = sysRoleMenus.stream().map(SysRoleMenu::getMenuId)
                    .collect(Collectors.toList());
            menuIds.retainAll(menuAllIds);
        }
        Map<String, Object> map = new HashMap<>(6);
        map.put("menus", menus);
        map.put("linkedMenus", menuIds);
        map.put("role", sysRole);
        return map;
    }

    /**
     * 查询菜单
     * @param userId 用户id
     * @param loginType 登录类型
     * @return Result
     */
    public List<SysMenu> searchMenus(Long userId, Integer loginType) {
        List<SysMenu> menus = null;
        if (loginType != null && loginType.equals(StateConstants.LOGIN_TYPE_999)) {
            menus = sysMenuMapper.selectList(new LambdaQueryWrapper<SysMenu>().orderByAsc(SysMenu::getOrderNum));
        } else {
            menus = sysMenuMapper.getMenuByUserId(userId, null);
        }
        if (CollectionUtils.isEmpty(menus)) {
            return new ArrayList<>();
        }
        List<SysMenuVO> list = this.getSysMenuVO(menus);
        List<SysMenu> newList = new ArrayList<>();
        this.treeToList(list, newList);
        return newList;
    }

    /**
     * 构建父子菜单树
     */
    private void treeToList(List<SysMenuVO> sysMenuVos, List<SysMenu> menus) {
        for (SysMenuVO sysMenuVo : sysMenuVos) {
            if (!ObjectUtils.isEmpty(sysMenuVo.getChildren())) {
                this.treeToList(sysMenuVo.getChildren(), menus);
            }
            SysMenu sysMenu = new SysMenu();
            BeanUtils.copyProperties(sysMenuVo, sysMenu);
            menus.add(sysMenu);
        }
    }

    /**
     * 构建父子菜单树
     */
    private List<SysMenuVO> getSysMenuVO(List<SysMenu> menus) {
        List<SysMenuVO> sysMenuVos = this.dbMenusToVo(menus);
        List<SysMenuVO> list = new ArrayList<>();
        for (SysMenuVO menu : sysMenuVos) {
            if (0L == menu.getParentId().longValue()) {
                list.add(menu);
            }
            for (SysMenuVO cMenu : sysMenuVos) {
                if (menu.getMenuId().longValue() == cMenu.getParentId().longValue()) {
                    if (ObjectUtils.isEmpty(menu.getChildren())) {
                        menu.setChildren(new ArrayList<>());
                    }
                    menu.getChildren().add(cMenu);
                }
            }
        }
        return list;
    }

    /**
     * db转vue路由
     *
     * @param list 菜单list
     * @return Result
     */
    private List<SysMenuVO> dbMenusToVo(List<SysMenu> list) {
        List<SysMenuVO> sysMenuVos = new ArrayList<>();
        for (SysMenu menu : list) {
            SysMenuVO sysMenuVo = new SysMenuVO();
            BeanUtils.copyProperties(menu, sysMenuVo);
            sysMenuVos.add(sysMenuVo);
        }
        return sysMenuVos;
    }

    /**
     * 查询角色（通用型）
     *
     * @param name 姓名
     * @param instId 机构id
     * @param deptId 部门id
     * @param page 分页参数
     * @return Result
     */
    public PageInfo search(String name, Long instId, Long deptId, PageForm page) {
        PageHelper.startPage(page.getPageNum(), page.getPageSize());
        List<SysRole> list = sysRoleMapper.selectList(
                new LambdaQueryWrapper<SysRole>().likeRight(StringUtils.hasText(name), SysRole::getName, name)
                        .eq(StringUtils.hasText(String.valueOf(instId)), SysRole::getInstId, instId));
        return new PageInfo<SysRole>(list);
    }

    /**
     * 新增角色
     */
    @Transactional(rollbackFor = Exception.class)
    @Lock4j(keys = "#role.roleCode")
    public void add(SysRoleVO role) {
        Assert.notNull(role, "参数错误");
        Assert.notNull(role.getRoleCode(), "参数错误");
        Assert.notNull(role.getName(), "参数错误");
        Assert.notNull(role.getInstId(), "参数错误");
        //新增用户roleCode全局唯一
        List<SysRole> sysRoles = sysRoleMapper.selectList(null);

        sysRoles = sysRoles.stream().filter(s -> s.getRoleCode().equals(role.getRoleCode())
                || s.getName().equals(role.getName())).collect(Collectors.toList());
        Assert.isTrue(CollectionUtils.isEmpty(sysRoles), "角色创建失败,该角色名字已存在");

        SysRole sysRole = new SysRole();
        BeanUtils.copyProperties(role, sysRole);
        sysRole.setRoleId(null);
        sysRoleMapper.insert(sysRole);
        if (role.getMenuIds() != null && role.getMenuIds().length != 0) {
            List<SysRoleMenu> list=new ArrayList<>();
            for (Long menuId : role.getMenuIds()) {
                SysRoleMenu sysRoleMenu = new SysRoleMenu();
                sysRoleMenu.setRoleId(sysRole.getRoleId());
                sysRoleMenu.setMenuId(menuId);
                list.add(sysRoleMenu);
            }
            MybatisBatch<SysRoleMenu> mybatisBatch = new MybatisBatch<>(sqlSessionFactory, list);
            MybatisBatch.Method<SysRoleMenu> method = new MybatisBatch.Method<>(SysRoleMenuMapper.class);
            mybatisBatch.execute(method.insert());
        }
    }

    /**
     * 修改角色信息
     */
    @Transactional(rollbackFor = Exception.class)
    public void update(SysRoleVO role) {
        Assert.notNull(role, "参数错误");
        Assert.notNull(role.getRoleId(), "参数错误");
        SysRole sysRole = sysRoleMapper
                .selectOne(new LambdaQueryWrapper<SysRole>().eq(SysRole::getName, role.getName())
                        .eq(SysRole::getInstId, role.getInstId()).ne(SysRole::getRoleId, role.getRoleId()));
        Assert.isNull(sysRole, "角色修改失败,该角色名字已存在");
        sysRole = new SysRole();
        BeanUtils.copyProperties(role, sysRole);
        sysRoleMenuMapper.delete(new LambdaQueryWrapper<SysRoleMenu>().eq(SysRoleMenu::getRoleId, role.getRoleId()));
        if (role.getMenuIds() != null && role.getMenuIds().length != 0) {
            List<SysRoleMenu> list=new ArrayList<>();
            for (Long menuId : role.getMenuIds()) {
                SysRoleMenu sysRoleMenu = new SysRoleMenu();
                sysRoleMenu.setRoleId(role.getRoleId());
                sysRoleMenu.setMenuId(menuId);
                list.add(sysRoleMenu);
            }
            MybatisBatch<SysRoleMenu> mybatisBatch = new MybatisBatch<>(sqlSessionFactory, list);
            MybatisBatch.Method<SysRoleMenu> method = new MybatisBatch.Method<>(SysRoleMenuMapper.class);
            mybatisBatch.execute(method.insert());
        }
        sysRoleMapper.updateById(sysRole);
    }

    /**
     * 删除角色信息
     */
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long roleId) {
        Assert.notNull(roleId, "参数错误");
        sysRoleMapper.deleteById(roleId);
        sysRoleMenuMapper.delete(new LambdaQueryWrapper<SysRoleMenu>().eq(SysRoleMenu::getRoleId, roleId));
        sysRoleUserMapper.delete(new LambdaQueryWrapper<SysRoleUser>().eq(SysRoleUser::getRoleId, roleId));
    }

    /**
     * 修改角色信息
     */
    public void updateStatus(Long roleId) {
        Assert.notNull(roleId, "参数错误");
        SysRole sysRole = sysRoleMapper.selectById(roleId);
        Assert.notNull(sysRole, "参数错误");
        sysRole.setStatus(sysRole.getStatus() == 0 ? 1 : 0);
        sysRoleMapper.updateById(sysRole);
    }

    /**
     *  查询菜单
     * @param roleIdList 角色id集
     * @return Result
     */
    public List<SysMenuDTO> selectMenuList(List<Long> roleIdList) {
        List<SysMenuDTO> sysMenuDTOS = new ArrayList<>();
        Assert.notNull(roleIdList, "参数错误");
        LambdaQueryWrapper<SysRoleMenu> roleWrapper = new LambdaQueryWrapper<>();
        roleWrapper.in(SysRoleMenu::getRoleId, roleIdList);
        List<SysRoleMenu> sysRoleMenus = sysRoleMenuMapper.selectList(roleWrapper);
        if (CollectionUtil.isEmpty(sysRoleMenus)) {
            Assert.notNull(CollectionUtil.isEmpty(sysRoleMenus), "角色未关联菜单");
        }
        List<Long> menuList = sysRoleMenus.stream().map(SysRoleMenu::getMenuId)
                .collect(Collectors.toList());
        LambdaQueryWrapper<SysMenu> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(SysMenu::getMenuId, menuList);
        List<SysMenu> sysMenusList = sysMenuMapper.selectList(wrapper);
        if (CollectionUtil.isNotEmpty(sysMenusList)) {
            sysMenuDTOS = BeanUtil.copyToList(sysMenusList, SysMenuDTO.class);
        }
        return sysMenuDTOS;
    }

    /**
     * 过滤掉状态为0的菜单，且连同其子集一起过滤
     *
     * @param menus 菜单list
     * @param collect1 集合
     */
    private void filterMenu(List<SysMenu> menus, List<SysMenu> collect1) {
        if (!CollectionUtils.isEmpty(collect1)) {
            menus.removeAll(collect1);
            List<Long> collect = collect1.stream().map(SysMenu::getMenuId)
                    .collect(Collectors.toList());
            List<SysMenu> collect2 = menus.stream().filter(s -> collect.contains(s.getParentId()))
                    .collect(Collectors.toList());
            filterMenu(menus, collect2);
        }
    }

    /**
     * 查询当前用户权限菜单
     * @param token 登录token
     * @return Result
     */
    public Map<String, Object> selectAuthMenuButton(AccountToken token) {
        Long userId = token.getId();
        List<SysMenu> menus = new ArrayList<>();
        SysUserAdmin sysUserAdmin = sysUserAdminMapper.selectById(userId);
        if (sysUserAdmin != null) {
            // 获取所有启用的menus
            // 超级管理员默认不限角色权限获取菜单目录按钮
            menus = sysMenuMapper.selectList(new LambdaQueryWrapper<SysMenu>()
                    .orderByAsc(SysMenu::getOrderNum));
            if (!CollectionUtils.isEmpty(menus)) {
                // 过滤掉状态为0的，获取不启用的menus
                List<SysMenu> collect1 = menus.stream()
                        .filter(s -> !StateConstants.ZERO.equals(s.getStatus()))
                        .collect(Collectors.toList());
                // 将不启用的menus及其子集过滤掉
                filterMenu(menus, collect1);
            }
        } else {
            List<SysRoleUser> userRole = sysRoleUserMapper
                    .selectList(new LambdaQueryWrapper<SysRoleUser>().eq(SysRoleUser::getUserId, userId));
            if (!CollectionUtils.isEmpty(userRole)) {
                List<Long> roles = userRole.stream().map(SysRoleUser::getRoleId)
                        .collect(Collectors.toList());
                List<SysRoleMenu> menuss = sysRoleMenuMapper
                        .selectList(new LambdaQueryWrapper<SysRoleMenu>().in(SysRoleMenu::getRoleId, roles));
                if (!CollectionUtils.isEmpty(menuss)) {
                    // 获取角色下所有去重的菜单id
                    List<Long> collect = menuss.stream().map(SysRoleMenu::getMenuId).distinct()
                            .collect(Collectors.toList());

                    // 获取菜单信息
                    menus = sysMenuMapper.selectList(
                            new LambdaQueryWrapper<SysMenu>()
                                    .in(SysMenu::getMenuId, collect).orderByAsc(SysMenu::getOrderNum));
                    // 过滤掉状态为0的，获取不启用的menus
                    List<SysMenu> collect1 = menus.stream()
                            .filter(s -> !StateConstants.ZERO.equals(s.getStatus()))
                            .collect(Collectors.toList());
                    // 将不启用的menus及其子集过滤掉
                    filterMenu(menus, collect1);
                }
            }
        }

        List<Long> buttonMenusIds = new ArrayList<>();
        // 获取menus中按钮的menu id集
        buttonMenusIds = menus.stream()
                .filter(s -> s.getMenuType().equals(MenuConstants.TYPE_BUTTON))
                .map(SysMenu::getMenuId).collect(Collectors.toList());

        Map<String, Object> map = new HashMap<>(2);
        map.put("menus", menus);
        map.put("buttonMenusIds", buttonMenusIds);
        return map;
    }

    /**
     * 查询当前用户权限菜单(按钮)
     * @param token 登录token
     * @return Result
     */
    public Map<String, Object> selectAuthButton(AccountToken token) {
        Long userId = token.getId();
        List<SysMenu> menus = new ArrayList<>();
        SysUserAdmin sysUserAdmin = sysUserAdminMapper.selectById(userId);
        if (sysUserAdmin != null) {
            // 获取所有启用的menus
            // 超级管理员默认不限角色权限获取菜单目录按钮
            menus = sysMenuMapper.selectList(new LambdaQueryWrapper<SysMenu>()
                    .eq(SysMenu::getMenuType,MenuConstants.TYPE_BUTTON)
                    .eq(SysMenu::getMenuSystem,StateConstants.ECM)//3代表影像系统
                    .orderByAsc(SysMenu::getOrderNum));
            if (!CollectionUtils.isEmpty(menus)) {
                // 过滤掉状态为0的，获取不启用的menus
                List<SysMenu> collect1 = menus.stream()
                        .filter(s -> !StateConstants.ZERO.equals(s.getStatus()))
                        .collect(Collectors.toList());
                // 将不启用的menus及其子集过滤掉
                filterMenu(menus, collect1);
            }
        } else {
            List<SysRoleUser> userRole = sysRoleUserMapper
                    .selectList(new LambdaQueryWrapper<SysRoleUser>().eq(SysRoleUser::getUserId, userId));
            if (!CollectionUtils.isEmpty(userRole)) {
                List<Long> roles = userRole.stream().map(SysRoleUser::getRoleId)
                        .collect(Collectors.toList());
                List<SysRoleMenu> menuss = sysRoleMenuMapper
                        .selectList(new LambdaQueryWrapper<SysRoleMenu>().in(SysRoleMenu::getRoleId, roles));
                if (!CollectionUtils.isEmpty(menuss)) {
                    // 获取角色下所有去重的菜单id
                    List<Long> collect = menuss.stream().map(SysRoleMenu::getMenuId).distinct()
                            .collect(Collectors.toList());
                    // 获取菜单信息
                    menus = sysMenuMapper.selectList(
                            new LambdaQueryWrapper<SysMenu>()
                                    .in(SysMenu::getMenuId, collect)
                                    .eq(SysMenu::getMenuType,MenuConstants.TYPE_BUTTON)
                                    .eq(SysMenu::getMenuSystem,StateConstants.ECM)//3代表影像系统
                                    .orderByAsc(SysMenu::getOrderNum));
                    // 过滤掉状态为0的，获取不启用的menus
                    List<SysMenu> collect1 = menus.stream()
                            .filter(s -> !StateConstants.ZERO.equals(s.getStatus()))
                            .collect(Collectors.toList());
                    // 将不启用的menus及其子集过滤掉
                    filterMenu(menus, collect1);
                }
            }
        }
        Map<String, Object> map = new HashMap<>(1);
        map.put("menus", menus);
        return map;
    }

    /**
     * 查询当前用户权限菜单----影像系统会用，不区分目录、菜单、按钮
     * @return Result
     */
    public List<SysMenu> searchMenusAll(Long userId, Integer loginType) {
        List<SysMenu> menus = null;
        if (loginType != null && loginType.equals(StateConstants.LOGIN_TYPE_999)) {
            menus = sysMenuMapper.selectList(new LambdaQueryWrapper<SysMenu>().orderByAsc(SysMenu::getOrderNum));
        } else {
            menus = sysMenuMapper.getMenuByUserIdAll(userId, null);
        }
        if (CollectionUtils.isEmpty(menus)) {
            return new ArrayList<>();
        }
        List<SysMenuVO> list = this.getSysMenuVO(menus);
        List<SysMenu> newList = new ArrayList<>();
        this.treeToList(list, newList);
        return newList;
    }

}
