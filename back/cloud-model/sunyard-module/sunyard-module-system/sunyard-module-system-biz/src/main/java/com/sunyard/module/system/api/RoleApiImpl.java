package com.sunyard.module.system.api;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.lock.annotation.Lock4j;
import com.baomidou.mybatisplus.core.batch.MybatisBatch;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.sunyard.framework.common.result.Result;
import com.sunyard.framework.common.util.AssertUtils;
import com.sunyard.framework.mybatis.util.PageCopyListUtils;
import com.sunyard.module.system.api.dto.SysRoleDTO;
import com.sunyard.module.system.api.dto.SysRoleUserDTO;
import com.sunyard.module.system.api.dto.SysUserDTO;
import com.sunyard.module.system.constant.RoleConstants;
import com.sunyard.module.system.constant.StateConstants;
import com.sunyard.module.system.mapper.SysDeptMapper;
import com.sunyard.module.system.mapper.SysInstMapper;
import com.sunyard.module.system.mapper.SysRoleMapper;
import com.sunyard.module.system.mapper.SysRoleMenuMapper;
import com.sunyard.module.system.mapper.SysRoleUserMapper;
import com.sunyard.module.system.mapper.SysUserMapper;
import com.sunyard.module.system.po.SysDept;
import com.sunyard.module.system.po.SysInst;
import com.sunyard.module.system.po.SysRole;
import com.sunyard.module.system.po.SysRoleMenu;
import com.sunyard.module.system.po.SysRoleUser;
import com.sunyard.module.system.po.SysUser;
import com.sunyard.module.system.service.OrgRoleService;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * role模块对内提供的controller
 *
 * @author huronghao
 * @date 2023-05-18 11:03
 */
@RestController
public class RoleApiImpl implements RoleApi {

    @Resource
    private SqlSessionFactory sqlSessionFactory;
    @Resource
    private SysRoleMapper sysRoleMapper;
    @Resource
    private SysRoleUserMapper sysRoleUserMapper;
    @Resource
    private SysRoleMenuMapper sysRoleMenuMapper;
    @Resource
    private SysUserMapper sysUserMapper;
    @Resource
    private SysInstMapper sysInstMapper;
    @Resource
    private SysDeptMapper sysDeptMapper;
    @Resource
    private OrgRoleService orgRoleService;

    @Override
    public Result<List<SysRoleDTO>> getRoleByUserId(Long userId) {
        List<SysRole> poList = sysRoleMapper.searchLinkedRole(userId);
        List<SysRoleDTO> dtoList = poList.stream().map(po -> {
            SysRoleDTO dto = new SysRoleDTO();
            BeanUtils.copyProperties(po, dto);
            return dto;
        }).collect(Collectors.toList());
        return Result.success(dtoList);
    }

    @Override
    public Result<List<SysRoleDTO>> getRoleByUserIds(Long[] userIds) {
        List<SysRoleUser> poList =
                sysRoleUserMapper.selectList(new LambdaQueryWrapper<SysRoleUser>().in(SysRoleUser::getUserId, Arrays.asList(userIds)));
        if(CollectionUtils.isEmpty(poList)){
            return Result.success(new ArrayList<>());
        }
        //获取觉得名称
        Set<Long> collect = poList.stream().map(SysRoleUser::getRoleId).collect(Collectors.toSet());
        List<SysRole> sysRoles = sysRoleMapper.selectList(new LambdaQueryWrapper<SysRole>().in(SysRole::getRoleId, collect));
        Map<Long, List<SysRole>> collect1 = sysRoles.stream().collect(Collectors.groupingBy(SysRole::getRoleId));
        List<SysRoleDTO> dtoList = poList.stream().map(po -> {
            SysRoleDTO dto = new SysRoleDTO();
            BeanUtils.copyProperties(po, dto);
            List<SysRole> sysRoles1 = collect1.get(po.getRoleId());
            if(!CollectionUtils.isEmpty(sysRoles1)){
                dto.setName(sysRoles1.get(0).getName());
            }
            return dto;
        }).collect(Collectors.toList());
        return Result.success(dtoList);
    }

    @Override
    public Result<List<SysRoleDTO>> getRoleById(Long[] roleIds) {
        List<SysRole> poList =
                sysRoleMapper.selectList(new LambdaQueryWrapper<SysRole>().in(SysRole::getRoleId, Arrays.asList(roleIds)));
        List<SysRoleDTO> dtoList = poList.stream().map(po -> {
            SysRoleDTO dto = new SysRoleDTO();
            BeanUtils.copyProperties(po, dto);
            return dto;
        }).collect(Collectors.toList());
        return Result.success(dtoList);
    }

    @Override
    public Result<List<SysRoleDTO>> getRoleByInstId(Long instId) {
        List<SysRole> poList =
                sysRoleMapper.selectList(new LambdaQueryWrapper<SysRole>().eq(!ObjectUtils.isEmpty(instId), SysRole::getInstId, instId));
        List<SysRoleDTO> dtoList = poList.stream().map(po -> {
            SysRoleDTO dto = new SysRoleDTO();
            BeanUtils.copyProperties(po, dto);
            return dto;
        }).collect(Collectors.toList());
        return Result.success(dtoList);
    }


    @Override
    public Result<List<SysRoleDTO>> getRoleListByMenuId(Long menuId) {
        AssertUtils.isNull(menuId, "参数错误");
        List<SysRoleMenu> sysRoleMenuList = sysRoleMenuMapper.selectList(new LambdaQueryWrapper<SysRoleMenu>()
                .eq(SysRoleMenu::getMenuId, menuId));
        Set<Long> roleIdList = sysRoleMenuList.stream().map(SysRoleMenu::getRoleId).collect(Collectors.toSet());
        List<SysRoleDTO> dtoList = new ArrayList<>();
        if (CollectionUtils.isEmpty(roleIdList)) {
            return Result.success(dtoList);
        }
        List<SysRole> sysRole = sysRoleMapper.selectBatchIds(roleIdList);
        dtoList = sysRole.stream().map(po -> {
            SysRoleDTO dto = new SysRoleDTO();
            BeanUtils.copyProperties(po, dto);
            return dto;
        }).collect(Collectors.toList());
        return Result.success(dtoList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @Lock4j(keys = {"#sysRoleDTO.roleCode"})
    public Result<Long> add(SysRoleDTO sysRoleDTO) {
        //校验入参
        checkParam(sysRoleDTO);
        //往role表插入数据
        addRoleData(sysRoleDTO);
        //往role-menu表插入数据
        addRoleMenuData(sysRoleDTO);
        return Result.success(sysRoleDTO.getRoleId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @Lock4j(keys = {"#roleId"})
    public Result delete(Long roleId) {
        AssertUtils.isNull(roleId, "参数错误");
        //删除角色相关数据
        delRoleData(roleId);
        return Result.success(null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result lotDelete(List<Long> roleIds) {
        AssertUtils.isNull(roleIds, "参数错误");
        //批量删除角色相关数据
        lotDelRoleData(roleIds);
        return Result.success(null);
    }

    @Override
    public Result searchList(SysRoleDTO sysRoleDTO) {
        //关联用户对应的角色id
        List<Long> roleIdsByUser = new ArrayList<>();
        //获取关联用户对应的角色id
        roleIdsByUser = getRoleIdsByUserName(sysRoleDTO, roleIdsByUser);
        PageHelper.startPage(sysRoleDTO.getPageNum(), sysRoleDTO.getPageSize());
        List<SysRole> roleList = sysRoleMapper.selectList(new LambdaQueryWrapper<SysRole>()
                .select(SysRole::getRoleId, SysRole::getName, SysRole::getRoleCode)
                        .eq(sysRoleDTO.getInstId()!=null,SysRole::getInstId,sysRoleDTO.getInstId())
                .likeRight(StringUtils.hasText(sysRoleDTO.getName()), SysRole::getName, sysRoleDTO.getName())
                .likeRight(StringUtils.hasText(sysRoleDTO.getRoleCode()), SysRole::getRoleCode, sysRoleDTO.getRoleCode())
                .in(!CollectionUtils.isEmpty(roleIdsByUser), SysRole::getRoleId, roleIdsByUser)
                .orderByDesc(SysRole::getCreateTime));
        PageInfo pageInfo = new PageInfo<>(roleList);
        if (CollectionUtils.isEmpty(roleList)) {
            return Result.success(pageInfo);
        }
        List<Long> roleIds = roleList.stream().map(SysRole::getRoleId).collect(Collectors.toList());
        List<SysRoleUserDTO> roleUserExtends = sysRoleUserMapper
                .selectListAddUserName(roleIds,null);
        if (CollectionUtils.isEmpty(roleUserExtends)) {
            return Result.success(pageInfo);
        }
        Map<Long, List<SysRoleUserDTO>> collectByRoleId = roleUserExtends.stream()
                .collect(Collectors.groupingBy(SysRoleUserDTO::getRoleId));
        List<SysRoleDTO> roleExtends = PageCopyListUtils.copyListProperties(roleList, SysRoleDTO.class);
        for (SysRoleDTO roleExtend : roleExtends) {
            if (CollectionUtils.isEmpty(collectByRoleId.get(roleExtend.getRoleId()))) {
                continue;
            }
            List<SysRoleUserDTO> roleUserExtends1 = collectByRoleId.get(roleExtend.getRoleId());
            List<String> roleUserNames = new ArrayList<>();
            List<Long> selectedUserIds = new ArrayList<>();
            List<Long> selectedOrgIds = new ArrayList<>();
            roleUserExtends1.forEach(p -> {
                roleUserNames.add(p.getUserName());
                selectedUserIds.add(p.getUserId());
                selectedOrgIds.add(ObjectUtils.isEmpty(p.getDeptId()) ? p.getInstId() : p.getDeptId());
            });
            roleExtend.setRelateUserNames(String.join("；", roleUserNames));
            roleExtend.setSelectedUserIds(selectedUserIds);
            roleExtend.setSelectedOrgIds(selectedOrgIds);
        }
        pageInfo.setList(roleExtends);
        return Result.success(pageInfo);
    }

    @Override
    public Result searchListInUsePage(SysRoleDTO sysRoleDTO) {
        //关联用户对应的角色id
        List<Long> roleIdsByUser = new ArrayList<>();
        //获取关联用户对应的角色id
        roleIdsByUser = getRoleIdsByUserName(sysRoleDTO, roleIdsByUser);
        PageHelper.startPage(sysRoleDTO.getPageNum(), sysRoleDTO.getPageSize());
        List<SysRole> roleList=sysRoleMapper.searchList(sysRoleDTO,roleIdsByUser);
        PageInfo pageInfo = new PageInfo<>(roleList);
        if (CollectionUtils.isEmpty(roleList)) {
            return Result.success(pageInfo);
        }
        List<Long> roleIds = roleList.stream().map(SysRole::getRoleId).collect(Collectors.toList());
        List<SysRoleUserDTO> roleUserExtends = sysRoleUserMapper
                .selectListAddUserName(roleIds,null);
        if (CollectionUtils.isEmpty(roleUserExtends)) {
            return Result.success(pageInfo);
        }
        Map<Long, List<SysRoleUserDTO>> collectByRoleId = roleUserExtends.stream()
                .collect(Collectors.groupingBy(SysRoleUserDTO::getRoleId));
        List<SysRoleDTO> roleExtends = PageCopyListUtils.copyListProperties(roleList, SysRoleDTO.class);
        for (SysRoleDTO roleExtend : roleExtends) {
            if (CollectionUtils.isEmpty(collectByRoleId.get(roleExtend.getRoleId()))) {
                continue;
            }
            List<SysRoleUserDTO> roleUserExtends1 = collectByRoleId.get(roleExtend.getRoleId());
            List<String> roleUserNames = new ArrayList<>();
            List<Long> selectedUserIds = new ArrayList<>();
            List<Long> selectedOrgIds = new ArrayList<>();
            roleUserExtends1.forEach(p -> {
                roleUserNames.add(p.getUserName());
                selectedUserIds.add(p.getUserId());
                selectedOrgIds.add(ObjectUtils.isEmpty(p.getDeptId()) ? p.getInstId() : p.getDeptId());
            });
            roleExtend.setRelateUserNames(String.join("；", roleUserNames));
            roleExtend.setSelectedUserIds(selectedUserIds);
            roleExtend.setSelectedOrgIds(selectedOrgIds);
        }
        pageInfo.setList(roleExtends);
        return Result.success(pageInfo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @Lock4j(keys = {"#sysRoleDTO.roleId"})
    public Result relateUser(SysRoleDTO sysRoleDTO) {
        //校验参数
        checkParam2(sysRoleDTO);
        //先删后插,删除老得关联关系，添加新的关联关系
        sysRoleUserMapper
                .delete(new LambdaQueryWrapper<SysRoleUser>()
                .eq(SysRoleUser::getRoleId, sysRoleDTO.getRoleId()));
        List<Long> relateUserList = sysRoleDTO.getRelateUserList();
        List<SysRoleUser> list = new ArrayList<>();
        for (Long userId : relateUserList) {
            SysRoleUser sysRoleUser = new SysRoleUser();
            sysRoleUser.setRoleId(sysRoleDTO.getRoleId());
            sysRoleUser.setUserId(userId);
            list.add(sysRoleUser);
        }
        if(!list.isEmpty()){
            MybatisBatch<SysRoleUser> mybatisBatch = new MybatisBatch<>(sqlSessionFactory, list);
            MybatisBatch.Method<SysRoleUser> method = new MybatisBatch.Method<>(SysRoleUserMapper.class);
            mybatisBatch.execute(method.insert());
        }
        return Result.success(null);
    }

    @Override
    public Result<SysRoleDTO> roleDetails(Long roleId) {
        AssertUtils.isNull(roleId, "参数错误");
        SysRole role = sysRoleMapper.selectById(roleId);
        List<SysRoleMenu> roleMenus = sysRoleMenuMapper
                .selectList(new LambdaQueryWrapper<SysRoleMenu>()
                        .eq(SysRoleMenu::getRoleId, roleId));
        SysRoleDTO sysRoleDTO = new SysRoleDTO();
        sysRoleDTO.setRoleCode(role.getRoleCode());
        sysRoleDTO.setName(role.getName());
        sysRoleDTO.setCreateUser(role.getCreateUser());
        sysRoleDTO.setUpdateUser(role.getUpdateUser());
        sysRoleDTO.setCreateTime(role.getCreateTime());
        sysRoleDTO.setUpdateTime(role.getUpdateTime());
        if (!CollectionUtils.isEmpty(roleMenus)) {
            List<Long> menuIds = roleMenus.stream().map(SysRoleMenu::getMenuId).collect(Collectors.toList());
            sysRoleDTO.setMenuList(menuIds);
        }
        return Result.success(sysRoleDTO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @Lock4j(keys = {"#sysRoleDTO.roleId"})
    public Result roleEdit(SysRoleDTO sysRoleDTO) {
        //校验入参
        checkParam3(sysRoleDTO);
        //更新角色信息
        sysRoleMapper.update(null, new LambdaUpdateWrapper<SysRole>()
                .set(SysRole::getName, sysRoleDTO.getName())
                .set(SysRole::getUpdateUser, sysRoleDTO.getUpdateUser())
                .set(SysRole::getUpdateTime, new Date())
                .eq(SysRole::getRoleId, sysRoleDTO.getRoleId()));
        //更新角色功能权限，先删后插
        sysRoleMenuMapper.delete(new LambdaQueryWrapper<SysRoleMenu>().eq(SysRoleMenu::getRoleId, sysRoleDTO.getRoleId()));
        List<Long> menuList = sysRoleDTO.getMenuList();
        List<SysRoleMenu> list=new ArrayList<>();
        for (Long menuId : menuList) {
            SysRoleMenu sysRoleMenu = new SysRoleMenu();
            sysRoleMenu.setRoleId(sysRoleDTO.getRoleId());
            sysRoleMenu.setMenuId(menuId);
            list.add(sysRoleMenu);
        }
        if(!list.isEmpty()){
            MybatisBatch<SysRoleMenu> mybatisBatch = new MybatisBatch<>(sqlSessionFactory, list);
            MybatisBatch.Method<SysRoleMenu> method = new MybatisBatch.Method<>(SysRoleMenuMapper.class);
            mybatisBatch.execute(method.insert());
        }
        return Result.success(null);
    }

    @Override
    public Result<List<SysUserDTO>> getRelateUserList(Long roleId) {
        AssertUtils.isNull(roleId, "参数错误");
        List<SysRoleUser> roleUsers = sysRoleUserMapper
                .selectList(new LambdaQueryWrapper<SysRoleUser>()
                        .eq(SysRoleUser::getRoleId, roleId));
        if (CollectionUtils.isEmpty(roleUsers)) {
            return Result.success(Collections.emptyList());
        }
        List<Long> userIds = roleUsers.stream().map(SysRoleUser::getUserId).collect(Collectors.toList());
        List<SysUser> sysUsers = sysUserMapper.selectList(new LambdaQueryWrapper<SysUser>()
                .select(SysUser::getUserId, SysUser::getLoginName, SysUser::getInstId, SysUser::getDeptId,SysUser::getName)
                .in(SysUser::getUserId, userIds));
        List<SysUserDTO> sysUserExtends = PageCopyListUtils.copyListProperties(sysUsers, SysUserDTO.class);
        Set<Long> instIds = sysUsers.stream()
                .filter(p -> !ObjectUtils.isEmpty(p.getInstId()))
                .map(SysUser::getInstId)
                .collect(Collectors.toSet());
        Set<Long> deptIds = sysUsers.stream()
                .filter(p -> !RoleConstants.ZEROLONG.equals(p.getDeptId()))
                .map(SysUser::getDeptId)
                .collect(Collectors.toSet());
        addOrgName(instIds, deptIds, sysUserExtends);
        return Result.success(sysUserExtends);
    }

    private void addOrgName(Set<Long> instIds, Set<Long> deptIds, List<SysUserDTO> sysUserExtends) {
        if (CollectionUtils.isEmpty(sysUserExtends)) {
            return;
        }
        List<SysInst> insts = sysInstMapper.selectList(new LambdaQueryWrapper<SysInst>()
                .in(!CollectionUtils.isEmpty(instIds), SysInst::getInstId, instIds));
        List<SysDept> depts = sysDeptMapper.selectList(new LambdaQueryWrapper<SysDept>()
                .in(!CollectionUtils.isEmpty(deptIds), SysDept::getDeptId, deptIds));
        Map<Long, List<SysInst>> collectByInstId = insts.stream().collect(Collectors.groupingBy(SysInst::getInstId));
        Map<Long, List<SysDept>> collectByDeptId = depts.stream().collect(Collectors.groupingBy(SysDept::getDeptId));
        for (SysUserDTO userExtend : sysUserExtends) {
            String orgName = "";
            if (!CollectionUtils.isEmpty(collectByInstId.get(userExtend.getInstId()))) {
                orgName = collectByInstId.get(userExtend.getInstId()).get(0).getNameLevel();
            }
            if (!RoleConstants.ZEROLONG.equals(userExtend.getDeptId())) {
                if (!CollectionUtils.isEmpty(collectByDeptId.get(userExtend.getDeptId()))) {
                    orgName = orgName + "-" + collectByDeptId.get(userExtend.getDeptId()).get(0).getNameLevel();
                }
            }
            userExtend.setOrganization(orgName.replace("-", "/"));
        }
    }

    private void checkParam3(SysRoleDTO sysRoleDTO) {
        AssertUtils.isNull(sysRoleDTO.getRoleId(), "参数错误");
        AssertUtils.isNull(sysRoleDTO.getName(), "角色名称不能为空");
        AssertUtils.isNull(sysRoleDTO.getMenuList(), "功能权限不能为空");
    }

    private void checkParam2(SysRoleDTO sysRoleDTO) {
        AssertUtils.isNull(sysRoleDTO.getRoleId(), "参数错误");
        AssertUtils.isNull(sysRoleDTO.getRelateUserList(), "关联用户不能为空");
    }

    private List<Long> getRoleIdsByUserName(SysRoleDTO sysRoleDTO, List<Long> roleIdsByUser) {
        if (ObjectUtils.isEmpty(sysRoleDTO.getRelateUserName())) {
            return roleIdsByUser;
        }
        List<SysRoleUserDTO> roleUserExtends = sysRoleUserMapper.selectListAddUserName(null,sysRoleDTO.getRelateUserName());
        if (CollectionUtils.isEmpty(roleUserExtends)) {
            return roleIdsByUser;
        }
        roleIdsByUser = roleUserExtends.stream().map(SysRoleUserDTO::getRoleId).distinct().collect(Collectors.toList());
        return roleIdsByUser;
    }


    private void lotDelRoleData(List<Long> roleIds) {
        //删除role表数据
        sysRoleMapper.deleteBatchIds(roleIds);
        //删除role-menu表数据
        sysRoleMenuMapper.delete(new LambdaQueryWrapper<SysRoleMenu>().in(SysRoleMenu::getRoleId, roleIds));
        //删除role-user表数据
        sysRoleUserMapper.delete(new LambdaQueryWrapper<SysRoleUser>().in(SysRoleUser::getRoleId, roleIds));
    }

    private void delRoleData(Long roleId) {
        //删除role表数据
        sysRoleMapper.deleteById(roleId);
        //删除role-menu表数据
        sysRoleMenuMapper.delete(new LambdaQueryWrapper<SysRoleMenu>().eq(SysRoleMenu::getRoleId, roleId));
        //删除role-user表数据
        sysRoleUserMapper.delete(new LambdaQueryWrapper<SysRoleUser>().eq(SysRoleUser::getRoleId, roleId));
    }

    private void addRoleMenuData(SysRoleDTO sysRoleDTO) {
        List<Long> menuList = sysRoleDTO.getMenuList();
        List<SysRoleMenu> list=new ArrayList<>();
        for (Long menuId : menuList) {
            SysRoleMenu sysRoleMenu = new SysRoleMenu();
            sysRoleMenu.setRoleId(sysRoleDTO.getRoleId());
            sysRoleMenu.setMenuId(menuId);
            list.add(sysRoleMenu);
        }
        if(!list.isEmpty()){
            MybatisBatch<SysRoleMenu> mybatisBatch = new MybatisBatch<>(sqlSessionFactory, list);
            MybatisBatch.Method<SysRoleMenu> method = new MybatisBatch.Method<>(SysRoleMenuMapper.class);
            mybatisBatch.execute(method.insert());
        }
    }

    private void addRoleData(SysRoleDTO sysRoleDTO) {
        SysRole sysRole = new SysRole();
        sysRole.setRoleCode(sysRoleDTO.getRoleCode());
        sysRole.setName(sysRoleDTO.getName());
        sysRole.setStatus(RoleConstants.ZERO);
        sysRole.setSystemCode(sysRoleDTO.getSystemCode());
        sysRole.setCreateUser(sysRoleDTO.getCreateUser());
        sysRoleMapper.insert(sysRole);
        sysRoleDTO.setRoleId(sysRole.getRoleId());
    }

    private void checkParam(SysRoleDTO sysRoleDTO) {
        AssertUtils.isNull(sysRoleDTO.getRoleCode(), "角色代码不能为空");
        AssertUtils.isNull(sysRoleDTO.getName(), "角色名称不能为空");
        AssertUtils.isNull(sysRoleDTO.getMenuList(), "功能权限不能为空");
        Long count = sysRoleMapper.selectCount(new LambdaQueryWrapper<SysRole>().eq(SysRole::getRoleCode, sysRoleDTO.getRoleCode()));
        AssertUtils.isTrue(count.intValue() > 0, "角色代码已被使用");
    }

    @Override
    public Result getMenuByRoleId(List<Long> roleIdList) {
        return Result.success(orgRoleService.selectMenuList(roleIdList));
    }


    @Override
    public Result<SysRoleDTO> getRoleByRoleCode(String roleCode, Integer systemCode) {
        SysRoleDTO sysRoleDTO = new SysRoleDTO();
        AssertUtils.isNull(roleCode, "角色代码不能为空");
        // todo systemCode 未使用！
        AssertUtils.isNull(systemCode, "系统代码不能为空");
        // todo 由于FIND_IN_SET适配问题，先改为先查后代码筛选
        List<SysRole> sysRoles = sysRoleMapper.selectList(new LambdaQueryWrapper<SysRole>()
                .in(SysRole::getRoleCode, roleCode)
        );
        if (!CollectionUtils.isEmpty(sysRoles)){
            if (!CollectionUtils.isEmpty(sysRoles) && sysRoles.get(StateConstants.ZERO) != null) {
                sysRoleDTO = BeanUtil.copyProperties(sysRoles.get(StateConstants.ZERO), SysRoleDTO.class);
            }
        }
        return Result.success(sysRoleDTO);
    }

    @Override
    public Result<Integer> deleteRoleUserByUserIds(List<Long> userIds) {
        AssertUtils.isNull(userIds,"参数错误");
        int i = sysRoleUserMapper.delete(new LambdaQueryWrapper<SysRoleUser>().in(SysRoleUser::getUserId, userIds));
        return Result.success(i);
    }

    @Override
    public Result<Integer> relateUserRole(SysRoleUserDTO sysRoleUserDTO) {
        AssertUtils.isNull(sysRoleUserDTO.getUserId(),"参数错误");
        AssertUtils.isNull(sysRoleUserDTO.getRoleId(),"参数错误");
        SysRoleUser sysRoleUser = new SysRoleUser();
        sysRoleUser.setRoleId(sysRoleUserDTO.getRoleId());
        sysRoleUser.setUserId(sysRoleUserDTO.getUserId());
        //插入角色关联用户数据
        int i = sysRoleUserMapper.insert(sysRoleUser);
        return Result.success(i);
    }
}
