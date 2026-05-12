package com.sunyard.module.system.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.sunyard.framework.redis.util.RedisUtils;
import com.sunyard.module.system.constant.CachePrefixConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.sunyard.framework.common.exception.SunyardException;
import com.sunyard.framework.common.page.PageForm;
import com.sunyard.framework.common.result.ResultCode;
import com.sunyard.module.system.api.dto.MetaDTO;
import com.sunyard.module.system.api.dto.RouterDTO;
import com.sunyard.module.system.api.dto.SysMenuDTO;
import com.sunyard.module.system.constant.MenuConstants;
import com.sunyard.module.system.constant.StateConstants;
import com.sunyard.module.system.mapper.SysMenuDefaultMapper;
import com.sunyard.module.system.mapper.SysMenuMapper;
import com.sunyard.module.system.mapper.SysUserMapper;
import com.sunyard.module.system.po.SysMenu;
import com.sunyard.module.system.po.SysMenuDefault;
import com.sunyard.module.system.po.SysUser;
import com.sunyard.module.system.vo.SysMenuVO;
import com.sunyard.module.system.vo.SysUserVO;

/**
 * 系统管理-菜单管理
 *
 * @Author PJW 2021/7/6 9:28
 */
@Service
@Slf4j
public class SysMenuService {

    @Resource
    private SysMenuMapper sysMenuMapper;
    @Resource
    private SysUserMapper sysUserMapper;
    @Resource
    private SysMenuDefaultMapper sysMenuDefaultMapper;
    @Resource
    private RedisUtils redisUtils;

    /**
     * 菜单详情
     *
     * @param menuId 菜单id
     * @return Result
     */
    public SysMenu select(String menuId) {
        Assert.notNull(menuId, "参数错误");
        SysMenu sysMenu = sysMenuMapper.selectById(menuId);
        return sysMenu;
    }

    /**
     * 获取菜单列表
     *
     * @param page 分页参数
     * @param menu 菜单obj
     * @return Result
     */
    public PageInfo search(PageForm page, SysMenuVO menu) {
        PageHelper.startPage(page.getPageNum(), page.getPageSize());
        List<SysMenu> result = sysMenuMapper.selectList(
            new LambdaQueryWrapper<SysMenu>()
                .in(!CollectionUtils.isEmpty(menu.getStatusList()), SysMenu::getStatus, menu.getStatusList())
                .eq(null != menu.getMenuId(), SysMenu::getParentId, menu.getMenuId())
                .orderByAsc(SysMenu::getOrderNum));
        return new PageInfo<SysMenu>(result);
    }

    /**
     * 获取菜单列表
     *
     * @return Result
     */
    public PageInfo searchAll() {
        PageHelper.startPage(0, 0, false);
        List<SysMenu> result = sysMenuMapper.selectList(
            new LambdaQueryWrapper<SysMenu>()
                .orderByAsc(SysMenu::getOrderNum)
                .ne(SysMenu::getMenuType, "B"));
        return new PageInfo<SysMenu>(result);
    }

    /**
     * 新增菜单
     *
     * @param menu 菜单obj
     */
    public void add(SysMenu menu) {
        if (!StringUtils.hasText(menu.getMenuName()) || !StringUtils.hasText(menu.getMenuType())
                || null == menu.getParentId()) {
            throw new SunyardException(ResultCode.PARAM_ERROR, "参数错误");
        }
        SysMenu sysMenus = sysMenuMapper.selectOne(
            new LambdaQueryWrapper<SysMenu>()
                .eq(SysMenu::getMenuName, menu.getMenuName())
                .eq(SysMenu::getParentId, menu.getParentId()));
        Assert.isNull(sysMenus, "菜单已存在");
        List<SysMenu> maxIndexMenus = sysMenuMapper.selectList(new LambdaQueryWrapper<SysMenu>()
                .select(SysMenu::getOrderNum)
                .eq(SysMenu::getParentId, menu.getParentId())
                .orderByDesc(SysMenu::getOrderNum));
        Integer maxIndex = CollectionUtils.isEmpty(maxIndexMenus) ? 1 : maxIndexMenus.get(0).getOrderNum() + 1;
        //为新目录递增menu_system,子菜单都根据目录进行设置
        if (MenuConstants.TYPE_DIR.equals(menu.getMenuType())) {
            Long count = sysMenuMapper.selectCount(new LambdaQueryWrapper<SysMenu>()
                    .eq(SysMenu::getMenuType, MenuConstants.TYPE_DIR));
            menu.setMenuSystem(count.intValue());
        }
        menu.setMenuId(null);
        menu.setOrderNum(maxIndex);
        menu.setCreateTime(new Date());
        sysMenuMapper.insert(menu);
    }

    /**
     * 编辑菜单
     *
     * @param menu 菜单obj
     */
    public void update(SysMenu menu) {
        Assert.notNull(menu, "参数错误");
        SysMenu sysMenu = sysMenuMapper.selectById(menu.getMenuId());
        Assert.notNull(sysMenu, "参数错误");
        sysMenuMapper.updateById(menu);
    }

    /**
     * 删除菜单
     *
     * @param menuId 菜单id
     */
    public void del(String menuId) {
        Assert.notNull(menuId, "参数错误");
        List<SysMenu> list = sysMenuMapper.selectList(
            new LambdaQueryWrapper<SysMenu>()
                .eq(SysMenu::getParentId, menuId));
        Assert.isTrue(list.size() < 1, "请删除子菜单后再进行删除操作");
        sysMenuMapper.deleteById(menuId);
    }

    /**
     * 修改菜单状态
     *
     * @param ids id
     * @param status 状态
     */
    @Transactional(rollbackFor = Exception.class)
    public void editStatus(Long[] ids, Integer status) {
        Assert.notEmpty(ids, "参数错误");
        Assert.notNull(status, "参数错误");
        List<SysMenu> menuTbs = sysMenuMapper.selectList(
            new LambdaQueryWrapper<SysMenu>()
                .in(SysMenu::getMenuId, ids));
        for (SysMenu menu : menuTbs) {
            menu.setStatus(status);
            sysMenuMapper.updateById(menu);
        }
        //修改redis
        updateMenuStatus(menuTbs);
    }

    /**
     * 更新菜单缓存
     * @param menuTbs
     */

    private void updateMenuStatus(List<SysMenu> menuTbs) {
        if (menuTbs == null || menuTbs.isEmpty()) {
            return;
        }
        //定义状态 0：开启， 1：关闭
        int sysMenuStatusEnabled = 0;
        int sysMenuStatusDisabled = 1;
        Map<Integer, List<SysMenu>> groupedBySystem = new HashMap<>();
        for (SysMenu menu : menuTbs) {
            if (menu.getMenuSystem() == null || menu.getPerms() == null) {
                continue;
            }
            groupedBySystem.computeIfAbsent(menu.getMenuSystem(), k -> new ArrayList<>()).add(menu);
        }
        //处理数据
        for (Map.Entry<Integer, List<SysMenu>> entry : groupedBySystem.entrySet()) {
            //系统id
            Integer menuSystem = entry.getKey();
            String menuSystemId = String.valueOf(menuSystem);
            //取数
            Object cachedValue = redisUtils.hget(CachePrefixConstants.SYS_MENU, menuSystemId);
            List<String> existing = new ArrayList<>();
            if (cachedValue instanceof String) {
                try {
                    existing = JSON.parseObject(
                            (String) cachedValue,
                            new TypeReference<List<String>>() {}
                    );
                } catch (Exception e) {
                    log.error("反序列化缓存失败，menuSystem={}", menuSystem, e);
                }
            }
            //更新数据
            List<SysMenu> sysMenus = entry.getValue();
            for (SysMenu sysMenu : sysMenus) {
                String perms = sysMenu.getPerms();
                if (perms == null) continue;
                if (sysMenu.getStatus() == sysMenuStatusEnabled) {
                    //开启状态
                    existing.add(perms);
                } else if (sysMenu.getStatus() == sysMenuStatusDisabled) {
                    //关闭状态
                    existing.remove(perms);
                }
            }
            // 更新缓存
            if (!existing.isEmpty()) {
                // 有数据：序列化后更新
                String newJson = JSON.toJSONString(new ArrayList<>(existing)); // 转回 List 保证 JSON 是数组
                redisUtils.hset(CachePrefixConstants.SYS_MENU, menuSystemId,newJson);
            }
        }
    }

    /**
     * 置顶
     *
     * @param list 菜单obj
     */
    @Transactional(rollbackFor = Exception.class)
    public void editSort(List<SysMenu> list) {
        Assert.notEmpty(list, "参数错误");
        list.forEach(i -> sysMenuMapper.updateById(i));
    }

    /**
     * 菜单权限查询 接口 查询vue前端路由菜单
     *
     * @param userId 用户id
     * @param loginType 登录类型
     * @param type 类型
     * @return Result
     */
    public List<RouterDTO> searchRouters(Long userId, Integer loginType, Integer type) {
        log.info("searchRouters userId={}, loginType={}, type={}", userId, loginType, type);
        List<SysMenu> list = new ArrayList<>();
        if (loginType != null && loginType.equals(StateConstants.LOGIN_TYPE_999)) {
            list = sysMenuMapper.selectList(new LambdaQueryWrapper<SysMenu>()
                    .select(SysMenu::getMenuId, SysMenu::getParentId, SysMenu::getMenuName, SysMenu::getPerms, SysMenu::getPath, SysMenu::getComponent, SysMenu::getMenuType, SysMenu::getIsCache, SysMenu::getIsFrame, SysMenu::getIcon, SysMenu::getStatus, SysMenu::getVisible, SysMenu::getOrderNum, SysMenu::getRemark)
                    .groupBy(SysMenu::getMenuId, SysMenu::getParentId, SysMenu::getMenuName, SysMenu::getPerms, SysMenu::getPath, SysMenu::getComponent, SysMenu::getMenuType, SysMenu::getIsCache, SysMenu::getIsFrame, SysMenu::getIcon, SysMenu::getStatus, SysMenu::getVisible, SysMenu::getOrderNum, SysMenu::getRemark)
                    .orderByAsc(SysMenu::getOrderNum)
            );
        } else {
            list = sysMenuMapper.getMenuByUserId(userId, type);
        }
        List<RouterDTO> routers = getRouters(list);
        setDefaultRouter(routers, userId);
        return routers;
    }

    /**
     * 查询vue前端路由菜单 所有
     *
     * @return Result
     */
    public List<RouterDTO> searchRoutersAll() {
        return getRouters(sysMenuMapper.selectList(new LambdaQueryWrapper<SysMenu>()
                .select(SysMenu::getMenuId, SysMenu::getParentId, SysMenu::getMenuName, SysMenu::getPerms, SysMenu::getPath, SysMenu::getComponent, SysMenu::getMenuType, SysMenu::getIsCache, SysMenu::getIsFrame, SysMenu::getIcon, SysMenu::getStatus, SysMenu::getVisible, SysMenu::getOrderNum, SysMenu::getRemark)
                .groupBy(SysMenu::getMenuId, SysMenu::getParentId, SysMenu::getMenuName, SysMenu::getPerms, SysMenu::getPath, SysMenu::getComponent, SysMenu::getMenuType, SysMenu::getIsCache, SysMenu::getIsFrame, SysMenu::getIcon, SysMenu::getStatus, SysMenu::getVisible, SysMenu::getOrderNum, SysMenu::getRemark)
                .orderByAsc(SysMenu::getOrderNum)
        ));
    }

    /**
     * 查询菜单
     *
     * @param menuIds 菜单id
     * @return Result
     */
    public List<SysMenuDTO> searchMenuList(Long[] menuIds) {
        List<SysMenu> poList = sysMenuMapper.selectBatchIds(Arrays.asList(menuIds));
        List<SysMenuDTO> dtoList = poList.stream().map(po -> {
            SysMenuDTO dto = new SysMenuDTO();
            BeanUtils.copyProperties(po, dto);
            return dto;
        }).collect(Collectors.toList());
        return dtoList;
    }

    /**
     * 设置默认菜单
     * @param menuId 菜单id
     * @param userId 用户id
     */
    @Transactional(rollbackFor = Exception.class)
    public void addDefaultMenu(Long menuId, Long userId) {
        Assert.notNull(menuId, "参数错误");
        Assert.notNull(userId, "参数错误");
        sysMenuDefaultMapper.delete(
                new LambdaUpdateWrapper<SysMenuDefault>().eq(SysMenuDefault::getUserId, userId));
        SysMenuDefault sysMenuDefault = new SysMenuDefault();
        sysMenuDefault.setMenuId(menuId);
        sysMenuDefault.setUserId(userId);
        sysMenuDefaultMapper.insert(sysMenuDefault);
    }

    /**
     * 取消默认菜单
     * @param menuId 菜单id
     * @param userId 用户id
     */
    public void cancelDefaultMenu(Long menuId, Long userId) {
        Assert.notNull(menuId, "参数错误");
        Assert.notNull(userId, "参数错误");
        sysMenuDefaultMapper.delete(
                new LambdaUpdateWrapper<SysMenuDefault>().eq(SysMenuDefault::getUserId, userId));
    }

    /**
     * 修改布局
     * @param sysUserVO 用户obj
     * @param userId 用户id
     */
    public void updateLayout(SysUserVO sysUserVO, Long userId) {
        SysUser sysUser = new SysUser();
        BeanUtils.copyProperties(sysUserVO, sysUser);
        sysUser.setUserId(userId);
        sysUserMapper.updateById(sysUser);
    }

    /**
     * 获取所有菜单
     * @return Result
     */
    public Map<Integer, String> searchDir() {
        List<SysMenu> sysMenus = sysMenuMapper.selectList(
                new LambdaQueryWrapper<SysMenu>().eq(SysMenu::getMenuType, MenuConstants.TYPE_DIR));
        Map<Integer, String> collect = sysMenus.stream()
                .collect(Collectors.toMap(SysMenu::getMenuSystem, SysMenu::getMenuName));
        collect.put(1, "存储服务");
        return collect;
    }

    /**
     * db转vue路由
     *
     * @param list 菜单list
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
                    menu.getIsCache().intValue() == 0));
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

    /**
     * 构建父子菜单树
     * @param list 菜单list
     * @return result
     */
    private List<RouterDTO> getRouters(List<SysMenu> list) {
        List<RouterDTO> dbrouters = this.dbMenusToRouters(list);
        List<RouterDTO> routers = new ArrayList<>();
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
     * 设置默认菜单
     * @param list 菜单list
     * @param userId 用户id
     */
    private void setDefaultRouter(List<RouterDTO> list, Long userId) {
        SysMenuDefault sysMenuDefault = sysMenuDefaultMapper.selectOne(
                new LambdaQueryWrapper<SysMenuDefault>().eq(SysMenuDefault::getUserId, userId));
        if (!ObjectUtils.isEmpty(sysMenuDefault)) {
            list.forEach(item -> {
                if (item.getId().equals(sysMenuDefault.getMenuId() + "")) {
                    item.setIsDefault(true);
                }
            });
        }
    }

    /**
     * 根据系统id 获取菜单配置
     * @param menuSystem 菜单系统id
     * @return
     */
    public List<String> getPermsByMenuSystem(Integer menuSystem) {
        if (menuSystem == null) return Collections.emptyList();

        String field = String.valueOf(menuSystem);
        Object value = redisUtils.hget(CachePrefixConstants.SYS_MENU, field);

        if (value == null || !(value instanceof String)) {
            return Collections.emptyList();
        }

        try {
            List<String> strings = JSON.parseObject(
                    (String) value,
                    new TypeReference<List<String>>() {
                    }
            );
            return strings;
        } catch (Exception e) {
            log.error("Fastjson 反序列化失败: menuSystem={}", menuSystem, e);
            return Collections.emptyList();
        }
    }
}
