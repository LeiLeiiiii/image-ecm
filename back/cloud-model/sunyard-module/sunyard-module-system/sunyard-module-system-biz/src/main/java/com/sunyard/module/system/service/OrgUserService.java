package com.sunyard.module.system.service;
/*
 * Project: am
 *
 * File Created at 2021/7/15
 *
 * Copyright 2016 Corporation Limited. All rights reserved.
 *
 * This software is the confidential and proprietary information of Company. ("Confidential Information"). You shall not
 * disclose such Confidential Information and shall use it only in accordance with the terms of the license.
 */

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import com.sunyard.module.system.constant.CachePrefixConstants;
import com.sunyard.module.system.enums.table.UserStateEnum;
import org.apache.ibatis.session.SqlSessionFactory;
import org.dromara.email.api.MailClient;
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
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.sunyard.framework.common.enums.LoginTypeEnum;
import com.sunyard.framework.common.exception.SunyardException;
import com.sunyard.framework.common.page.PageForm;
import com.sunyard.framework.common.result.Result;
import com.sunyard.framework.common.result.ResultCode;
import com.sunyard.framework.common.util.PasswordUtils;
import com.sunyard.framework.common.util.RegexpUtils;
import com.sunyard.framework.common.util.UUIDUtils;
import com.sunyard.framework.message.util.EmailUtils;
import com.sunyard.framework.mybatis.util.PageCopyListUtils;
import com.sunyard.framework.redis.util.RedisUtils;
import com.sunyard.module.system.api.dto.SysMenuDTO;
import com.sunyard.module.system.api.dto.SysUserDTO;
import com.sunyard.module.system.api.dto.SysUserExportDTO;
import com.sunyard.module.system.config.properties.SystemProperties;
import com.sunyard.module.system.constant.StateConstants;
import com.sunyard.module.system.dto.SysPostUserDTO;
import com.sunyard.module.system.dto.SysPostUserListDTO;
import com.sunyard.module.system.dto.SysRoleUserListDTO;
import com.sunyard.module.system.ldap.LdapService;
import com.sunyard.module.system.mapper.SysDeptMapper;
import com.sunyard.module.system.mapper.SysInstMapper;
import com.sunyard.module.system.mapper.SysMenuMapper;
import com.sunyard.module.system.mapper.SysPostMapper;
import com.sunyard.module.system.mapper.SysPostUserMapper;
import com.sunyard.module.system.mapper.SysRoleMapper;
import com.sunyard.module.system.mapper.SysRoleUserMapper;
import com.sunyard.module.system.mapper.SysUserAdminMapper;
import com.sunyard.module.system.mapper.SysUserMapper;
import com.sunyard.module.system.po.SysDept;
import com.sunyard.module.system.po.SysInst;
import com.sunyard.module.system.po.SysMenu;
import com.sunyard.module.system.po.SysPost;
import com.sunyard.module.system.po.SysPostUser;
import com.sunyard.module.system.po.SysRole;
import com.sunyard.module.system.po.SysRoleUser;
import com.sunyard.module.system.po.SysUser;
import com.sunyard.module.system.po.SysUserAdmin;
import com.sunyard.module.system.vo.SysUserVO;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.GifCaptcha;
import lombok.extern.slf4j.Slf4j;

/**
 * @author zhouleibin
 * @Type
 * @Desc 组织机构-用户管理
 * @date 2021/7/15 14:29
 */
@Slf4j
@Service
public class OrgUserService {
    @Resource
    private SystemProperties systemProperties;
    @Resource
    private SqlSessionFactory sqlSessionFactory;
    @Resource
    private EmailUtils emailUtils;
    @Resource
    private RedisUtils redisUtils;
    @Resource
    private SysUserMapper sysUserMapper;
    @Resource
    private SysInstMapper sysInstMapper;
    @Resource
    private SysDeptMapper sysDeptMapper;
    @Resource
    private SysRoleUserMapper sysRoleUserMapper;
    @Resource
    private SysRoleMapper sysRoleMapper;
    @Resource
    private SysPostMapper sysPostMapper;
    @Resource
    private SysPostUserMapper sysPostUserMapper;
    @Resource
    private SysMenuMapper sysMenuMapper;
    @Resource
    private SysUserAdminMapper sysUserAdminMapper;
    @Resource
    private LdapService ldapService;

    /**
     * 查询用户
     *
     * @param loginName 登录名
     * @return Result
     */
    public List<SysUser> search(String loginName) {
        return sysUserMapper
                .selectList(new LambdaQueryWrapper<SysUser>().eq(SysUser::getLoginName, loginName));
    }

    /**
     * 查询用户(超级管理员)
     *
     * @param loginName 登录名
     * @return Result
     */
    public List<SysUserAdmin> adminInfoSearch(String loginName) {
        return sysUserAdminMapper.selectList(
                new LambdaQueryWrapper<SysUserAdmin>().eq(SysUserAdmin::getLoginName, loginName));
    }

    /**
     * ldap登录
     * @param loginName 登录名
     * @param pw 条件构造器
     * @return Result
     */
    public List<SysUserDTO> ldapLogin(String loginName, String pw) {
        Result result = ldapService.login(loginName, pw);
        List<SysUser> list = new ArrayList<>();
        List<SysUserDTO> listDto = new ArrayList<>();
        if (result.isSucc()) {
            list = this.search(loginName);
            if (ObjectUtils.isEmpty(list)) {
                throw new RuntimeException("用户未同步，请联系管理员处理");
            }
            listDto = list.stream().map(po -> {
                SysUserDTO dto = new SysUserDTO();
                BeanUtils.copyProperties(po, dto);
                return dto;
            }).collect(Collectors.toList());
        }
        return listDto;
    }

    /**
     * 用户详情
     *
     * @param userId 用户id
     * @return Result
     */
    public SysUserDTO select(Long userId) {
        Assert.notNull(userId, "参数错误");
        SysUser user = sysUserMapper.selectById(userId);
        SysUserDTO sysUserDTO = new SysUserDTO();
        BeanUtils.copyProperties(user, sysUserDTO);
        // 角色信息
        List<SysRole> roles = sysRoleMapper.searchLinkedRole(user.getUserId());
        List<Long> roleIdList = new ArrayList<>();
        List<String> roleNameList = new ArrayList<>();
        for (SysRole role : roles) {
            roleIdList.add(role.getRoleId());
            roleNameList.add(role.getName());
        }
        Long[] roleIds = roleIdList.toArray(new Long[roleIdList.size()]);
        String[] roleNames = roleNameList.toArray(new String[roleNameList.size()]);
        sysUserDTO.setRoleIds(roleIds);
        sysUserDTO.setRoleNames(roleNames);
        // 岗位信息
        List<Long> postIdList = new ArrayList<>();
        List<String> postNameList = new ArrayList<>();
        List<SysPost> posts = sysPostMapper.searchLinkedPost(user.getUserId());
        for (SysPost post : posts) {
            postIdList.add(post.getPostId());
            postNameList.add(post.getName());
        }
        Long[] postIds = postIdList.toArray(new Long[postIdList.size()]);
        String[] postNames = postNameList.toArray(new String[postNameList.size()]);
        sysUserDTO.setPostIds(postIds);
        sysUserDTO.setPostNames(postNames);
        return sysUserDTO;
    }

    /**
     * 获取用户列表
     *
     * @param user 用户obj
     * @param page 分页参数
     * @param userId 用户id
     * @param deptId 部门id
     * @return Result
     */
    public PageInfo search(SysUserVO user, PageForm page, Long userId, Long deptId) {
        //不传状态集，则直接返回空数据
        if (user.getStateList().isEmpty()) {
            return new PageInfo<>(new ArrayList<>());
        }
        PageHelper.startPage(page.getPageNum(), page.getPageSize());
        List<SysUserDTO> list = sysUserMapper.search(user, userId, deptId);
        if (CollectionUtils.isEmpty(list)) {
            return new PageInfo<>(new ArrayList<>());
        }
        PageInfo<SysUserDTO> pageInfo = new PageInfo<>(list);
        List<Long> userIdList = list.stream().map(SysUserDTO::getUserId).collect(Collectors.toList());
        // 根据用户id查询 角色 岗位
        List<SysRoleUserListDTO> sysRoleUserListDTOS = sysRoleUserMapper.selectListByUserIdList(userIdList);
        Map<Long, List<SysRoleUserListDTO>> roleMap = new HashMap<>();
        if (!CollectionUtils.isEmpty(sysRoleUserListDTOS)) {
            roleMap = sysRoleUserListDTOS.stream().collect(Collectors.groupingBy(SysRoleUserListDTO::getUserId));
        }
        // 岗位
        List<SysPostUserListDTO> sysPostUserListDTOS = sysPostUserMapper.selectListByUserIdList(userIdList);
        Map<Long, List<SysPostUserListDTO>> postMap = new HashMap<>();
        if (!CollectionUtils.isEmpty(sysPostUserListDTOS)) {
            postMap = sysPostUserListDTOS.stream().collect(Collectors.groupingBy(SysPostUserListDTO::getUserId));
        }
        // 匹配角色和岗位到list上
        for (SysUserDTO sysUserDTO : list) {
            // 角色
            if (!CollectionUtils.isEmpty(roleMap)) {
                List<SysRoleUserListDTO> sysRoleUserListMap = roleMap.get(sysUserDTO.getUserId());
                if (!CollectionUtils.isEmpty(sysRoleUserListMap)) {
                    // 角色id 和 角色名字
                    Long[] roleIds = sysRoleUserListMap.stream().map(SysRoleUserListDTO::getRoleId).toArray(Long[]::new);
                    String[] roleNames = sysRoleUserListMap.stream().map(SysRoleUserListDTO::getRoleName).toArray(String[]::new);
                    sysUserDTO.setRoleIds(roleIds);
                    sysUserDTO.setRoleNames(roleNames);
                }
            }
            // 岗位
            if (!CollectionUtils.isEmpty(postMap)) {
                List<SysPostUserListDTO> sysRoleUserListMap = postMap.get(sysUserDTO.getUserId());
                if (!CollectionUtils.isEmpty(sysRoleUserListMap)) {
                    // 岗位id 和 岗位名字
                    Long[] postIds = sysRoleUserListMap.stream().map(SysPostUserListDTO::getPostId).toArray(Long[]::new);
                    String[] postNames = sysRoleUserListMap.stream().map(SysPostUserListDTO::getPostName).toArray(String[]::new);
                    sysUserDTO.setPostIds(postIds);
                    sysUserDTO.setPostNames(postNames);
                }
            }
        }
        list = list.stream().collect(Collectors.collectingAndThen(Collectors.toCollection(
                () -> new TreeSet<>(Comparator.comparing(SysUserDTO::getLoginName))),
                ArrayList::new));
        pageInfo.setList(list);
        return pageInfo;
    }

    /**
     * 获取用户
     * @param userIds 用户id集
     * @return Result
     */
    public List<SysUser> getUserListByUserIds(List<Long> userIds) {
        if(ObjectUtils.isEmpty(userIds)){
            return new ArrayList<>();
        }
        return sysUserMapper
                .selectList(new LambdaQueryWrapper<SysUser>()
                        .in(SysUser::getUserId, userIds)
                        .eq(SysUser::getState, 1));
    }

    /**
     * 新增用户
     *
     * @param user 用户obj
     */
    @Transactional(rollbackFor = Exception.class)
    @Lock4j(keys = "#user.loginName")
    public void add(SysUserVO user) {
        checkUserData(user);
        List<SysUser> userlist = sysUserMapper.selectList(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getLoginName, user.getLoginName()));
        Assert.isTrue(userlist.isEmpty(), "登录名已存在");
        SysUser addUser = new SysUser();
        BeanUtils.copyProperties(user, addUser);
        addUser.setState(1);
        //随机添加32位盐
        String salt = UUIDUtils.generateUUID();
        addUser.setPwd(PasswordUtils.getEncryptionPassword(salt, systemProperties.getUserInitPassword()));
        addUser.setSalt(salt);
        addUser.setPwdUpdateTime(new Date());
        sysUserMapper.insert(addUser);
        batchInsertRoleAndPostUser(user, addUser);
    }

    /**
     * 编辑用户
     *
     * @param user 用户obj
     */
    @Transactional(rollbackFor = Exception.class)
    public void update(SysUserVO user) {
        checkUserData(user);
        SysUser updateUser = new SysUser();
        BeanUtils.copyProperties(user, updateUser);
        sysUserMapper.updateById(updateUser);
        sysRoleUserMapper.delete(new LambdaQueryWrapper<SysRoleUser>().eq(SysRoleUser::getUserId,
                updateUser.getUserId()));
        sysPostUserMapper.delete(new LambdaQueryWrapper<SysPostUser>().eq(SysPostUser::getUserId,
                updateUser.getUserId()));
        // 批量插入角色和岗位与用户关联
        batchInsertRoleAndPostUser(user, updateUser);
    }

    /**
     * 批量插入角色和岗位与用户关联
     *
     * @param user
     * @param updateUser
     */
    private void batchInsertRoleAndPostUser(SysUserVO user, SysUser updateUser) {
        if (user.getRoleIds() != null && user.getRoleIds().length != 0) {
            List<SysRoleUser> list = new ArrayList<>();
            for (Long roleId : user.getRoleIds()) {
                SysRoleUser sysRoleUser = new SysRoleUser();
                sysRoleUser.setUserId(updateUser.getUserId());
                sysRoleUser.setRoleId(roleId);
                list.add(sysRoleUser);
            }
            MybatisBatch<SysRoleUser> mybatisBatch = new MybatisBatch<>(sqlSessionFactory, list);
            MybatisBatch.Method<SysRoleUser> method = new MybatisBatch.Method<>(
                    SysRoleUserMapper.class);
            mybatisBatch.execute(method.insert());
        }
        if (user.getPostIds() != null && user.getPostIds().length != 0) {
            List<SysPostUser> list = new ArrayList<>();
            for (Long postId : user.getPostIds()) {
                SysPostUser sysPostUser = new SysPostUser();
                sysPostUser.setUserId(updateUser.getUserId());
                sysPostUser.setPostId(postId);
                list.add(sysPostUser);
            }
            MybatisBatch<SysPostUser> mybatisBatch = new MybatisBatch<>(sqlSessionFactory, list);
            MybatisBatch.Method<SysPostUser> method = new MybatisBatch.Method<>(
                    SysPostUserMapper.class);
            mybatisBatch.execute(method.insert());
        }
    }

    /**
     * 修改用户状态
     *
     * @param userId 用户id
     * @param status 状态
     */
    public void changeUserStatus(Long userId, Integer status) {
        Assert.notNull(userId, "参数错误");
        SysUser sysUser = sysUserMapper.selectById(userId);
        Assert.notNull(sysUser, "参数错误");
        sysUser.setState(status);
        sysUserMapper.updateById(sysUser);
        // 状态为1 ：启用状态，把登录错误记录清除
        if (status == 1) {
            redisUtils.del(CachePrefixConstants.LOGIN_ACCOUNT_ERROR_LIMIT + sysUser.getLoginName());
        }
    }

    /**
     * 批量处理部分 删除用户
     */
    public void del(Long[] userIds) {
        // 删除用户表数据
        sysUserMapper.delete(new LambdaQueryWrapper<SysUser>().in(SysUser::getUserId, userIds));
        // 删除用户角色关联表数据
        sysRoleUserMapper
                .delete(new LambdaQueryWrapper<SysRoleUser>().in(SysRoleUser::getUserId, userIds));
    }

    /**
     * 查询角色
     *
     * @param instId 机构id
     * @param deptId 部门id
     * @return Result
     */
    public List<SysRole> searchRole(Long instId, Long deptId) {
        if (null == deptId) {
            SysInst sysInst = sysInstMapper.selectOne(new LambdaQueryWrapper<SysInst>()
                    .eq(SysInst::getInstId, instId).eq(SysInst::getNewlevel, 0));
            Assert.notNull(sysInst, "请选择正确的机构");
        } else {
            List<SysDept> sysDept = sysDeptMapper.selectList(new LambdaQueryWrapper<SysDept>()
                    .eq(SysDept::getDeptId, deptId).orderByDesc(SysDept::getNewlevel));
            Assert.notNull(sysDept, "请选择正确的部门");
            instId = sysDept.get(0).getParentId();
        }
        List<SysRole> list = sysRoleMapper.selectList(
                new LambdaQueryWrapper<SysRole>().eq(null != instId, SysRole::getInstId, instId));
        return list;
    }

    /**
     * 编辑用户角色
     *
     * @param roleIds 角色id
     * @param userIds 用户id
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateRole(Long[] roleIds, Long[] userIds) {
        Assert.notEmpty(userIds, "参数错误");
        if (ObjectUtils.isEmpty(roleIds)) {
            for (Long userId : userIds) {
                sysRoleUserMapper.delete(
                        new LambdaQueryWrapper<SysRoleUser>().eq(SysRoleUser::getUserId, userId));
            }
        } else {
            for (Long userId : userIds) {
                // 删除该用户下所有角色
                sysRoleUserMapper.delete(
                        new LambdaQueryWrapper<SysRoleUser>().eq(SysRoleUser::getUserId, userId));
                List<SysRoleUser> list = new ArrayList<>();
                for (Long roleId : roleIds) {
                    SysRoleUser role = new SysRoleUser();
                    role.setUserId(userId);
                    role.setRoleId(roleId);
                    list.add(role);
                }
                MybatisBatch<SysRoleUser> mybatisBatch = new MybatisBatch<>(sqlSessionFactory,
                        list);
                MybatisBatch.Method<SysRoleUser> method = new MybatisBatch.Method<>(
                        SysRoleUserMapper.class);
                mybatisBatch.execute(method.insert());
            }
        }
    }

    /**
     * 根据角色 获取权限
     *
     * @param userId 用户id
     * @return Result
     */
    public List<SysMenuDTO> getRoleByUserId(Long userId) {
        List<SysMenu> listPo = sysMenuMapper.getMenuByUserId(userId, null);
        List<SysMenuDTO> listDto = new ArrayList<>();
        listDto = listPo.stream().map(po -> {
            SysMenuDTO dto = new SysMenuDTO();
            BeanUtils.copyProperties(po, dto);
            return dto;
        }).collect(Collectors.toList());

        return listDto;
    }

    /**
     * 根据部门得到用户
     *
     * @param code code
     * @param name 姓名
     * @param deptId 部门id
     * @param pageForm 分页参数
     * @return Result
     */
    public PageInfo<SysUserDTO> getUserByDeptId(String code, String name, Long deptId,
                                                PageForm pageForm) {
        Assert.notNull(deptId, "参数错误");
        PageHelper.startPage(pageForm.getPageNum(), pageForm.getPageSize());
        List<SysUserDTO> user = sysUserMapper.selectByDeptId(name, deptId, code, null);
        if (!CollectionUtils.isEmpty(user)) {
            setPostInfo(user);
        }
        return new PageInfo<SysUserDTO>(user);
    }

    /**
     * 根据部门等条件得到用户（分页）
     *
     * @param code code
     * @param name 姓名
     * @param deptId 部门id
     * @param postName 岗位名
     * @param pageForm 分页参数
     * @return Result
     */
    public PageInfo<SysUserDTO> searchUserByConditions(String code, String name, Long deptId,String postName,
                                                PageForm pageForm) {
        Assert.notNull(deptId, "参数错误");
        PageHelper.startPage(pageForm.getPageNum(), pageForm.getPageSize());
        List<SysUserDTO> user = sysUserMapper.searchUserByConditions(name, deptId, code, postName);
        setPostInfo(user);
        return new PageInfo<>(user);
    }

    /**
     * 根据部门、机构得到用户(不分页)
     *
     * @param code code
     * @param name 姓名
     * @param deptId 部门id
     * @param instId 机构id
     * @return Result
     */
    public List<SysUserDTO> getTransUserByDeptId(String code, String name, Long deptId,
                                                 Long instId) {
        List<SysUserDTO> list = sysUserMapper.selectByDeptId(name, deptId, code, instId);
        if (!CollectionUtils.isEmpty(list)) {
            setPostInfo(list);
        }
        return list;
    }

    /**
     * 更新用户扫描权限
     *
     * @param userId 用户id
     * @param isScan 是否扫描
     */
    public void updateIsScan(Long userId, Integer isScan) {
        Assert.notNull(userId, "用户ID不存在");
        sysUserMapper.update(null, new LambdaUpdateWrapper<SysUser>()
                .set(SysUser::getIsScan, isScan).eq(SysUser::getUserId, userId));
    }

    /**
     * 根据角色id获取用户
     *
     * @param roleId 角色id
     * @return Result
     */
    public List<SysUser> getUserByRoleId(Long roleId) {
        Assert.notNull(roleId, "角色ID不存在");
        List<SysRoleUser> roleUserList = sysRoleUserMapper.selectList(
                new LambdaQueryWrapper<SysRoleUser>().eq(SysRoleUser::getRoleId, roleId));
        if (CollectionUtils.isEmpty(roleUserList) && roleUserList.size() == 0) {
            return null;
        }
        List<Long> userList = roleUserList.stream().map(SysRoleUser::getUserId)
                .collect(Collectors.toList());
        List<SysUser> users = sysUserMapper
                .selectList(new LambdaQueryWrapper<SysUser>().in(SysUser::getUserId, userList));
        return users;
    }

    /**
     * 根据角色和部门获取用户
     *
     * @param roleId 角色id
     * @param deptId 部门id
     * @param instId 机构id
     * @return Result
     */
    public PageInfo getUserByRoleIdOrDeptId(Long roleId, Long deptId, PageForm page, Long instId) {
        PageHelper.startPage(page.getPageNum(), page.getPageSize());
        List<SysUserDTO> user = sysUserMapper.getUserByRoleIdorDeptId(instId, deptId, roleId, "1");
        List<Long> userids = user.stream().map(SysUserDTO::getUserId).collect(Collectors.toList());
        Set<Long> set = new HashSet<>(userids);

        if (CollectionUtils.isEmpty(set)) {
            return new PageInfo<>(user);
        }

        List<SysRoleUser> roleUserList = sysRoleUserMapper
                .selectList(new LambdaQueryWrapper<SysRoleUser>().in(SysRoleUser::getUserId, set));
        Map<Long, List<SysRoleUser>> roleUser = roleUserList.stream()
                .collect(Collectors.groupingBy(SysRoleUser::getUserId));
        for (SysUserDTO sysUserDTO : user) {
            List<SysRoleUser> roleUserList1 = roleUser.get(sysUserDTO.getUserId());
            if (!CollectionUtils.isEmpty(roleUserList1)) {
                List<Long> roles = roleUserList1.stream().map(SysRoleUser::getRoleId)
                        .collect(Collectors.toList());
                List<String> rolenames = sysRoleMapper
                        .selectList(new LambdaQueryWrapper<SysRole>().in(SysRole::getRoleId, roles))
                        .stream().map(SysRole::getName).collect(Collectors.toList());
                sysUserDTO.setRoleName(String.join(",", rolenames));
            }
        }

        return new PageInfo<SysUserDTO>(user);
    }

    /**
     * 导入用户
     * @param userMaps 用户数据map
     */
    @Transactional(rollbackFor = Exception.class)
    public void userImport(List<Map<Integer, String>> userMaps) {
        for (Map<Integer, String> map : userMaps) {
            SysUserVO user = new SysUserVO();
            user.setLoginName(map.get(0));
            user.setName(map.get(1));

            List<SysDept> deptList = sysDeptMapper
                    .selectList(new LambdaQueryWrapper<SysDept>().eq(SysDept::getName, map.get(2)));
            if (!CollectionUtils.isEmpty(deptList)) {
                user.setDeptId(deptList.get(0).getDeptId());
            }

            List<SysInst> instList = sysInstMapper
                    .selectList(new LambdaQueryWrapper<SysInst>().eq(SysInst::getName, map.get(3)));
            if (!CollectionUtils.isEmpty(instList)) {
                user.setInstId(instList.get(0).getInstId());
            }

            user.setCode(map.get(4));
            if (StringUtils.hasText(map.get(5))) {
                user.setSex("男".equals(map.get(5)) ? 1 : 0);
            }
            user.setPhone(map.get(6));
            user.setEmail(map.get(7));
            add(user);
        }
    }

    /**
     * 导出
     *
     * @return Result
     */
    public List<SysUserExportDTO> exportList() {
        return sysUserMapper.exportList();
    }

    /**
     * 修改用户状态
     * @param userName 用户姓名
     * @param status 状态
     * @param loginTypeEnum 登录类型
     */
    public void changeStatusByUserName(String userName, Integer status,
                                       LoginTypeEnum loginTypeEnum) {
        Assert.notNull(userName, "参数错误");
        if (loginTypeEnum.equals(LoginTypeEnum.SUPER)) {
            SysUserAdmin sysUser = sysUserAdminMapper
                    .selectOne(new LambdaQueryWrapper<SysUserAdmin>().eq(SysUserAdmin::getLoginName,
                            userName));
            Assert.notNull(sysUser, "参数错误");
            sysUser.setState(status);
            sysUserAdminMapper.updateById(sysUser);
        } else {
            SysUser sysUser = sysUserMapper.selectOne(
                    new LambdaQueryWrapper<SysUser>().eq(SysUser::getLoginName, userName));
            Assert.notNull(sysUser, "参数错误");
            sysUser.setState(status);
            sysUserMapper.updateById(sysUser);
        }
        // 状态为1 ：启用状态，把登录错误记录清除
        if (status == 1) {
            redisUtils.del(CachePrefixConstants.LOGIN_ACCOUNT_ERROR_LIMIT + userName);
        }
    }

    /**
     * 获取用户信息
     *
     * @param username 用户姓名
     * @return Result
     */
    public SysUserDTO getUserDetail(String username) {
        SysUser sysUser = sysUserMapper
                .selectOne(new LambdaQueryWrapper<SysUser>().eq(SysUser::getLoginName, username)
                        .eq(SysUser::getState, 1));
        if (sysUser != null) {
            SysUserDTO sysUserDTO = new SysUserDTO();
            BeanUtils.copyProperties(sysUser, sysUserDTO);
            //详情应当返回所有信息

            List<SysRoleUser> sysRoleUsers = sysRoleUserMapper
                    .selectList(new LambdaQueryWrapper<SysRoleUser>().eq(SysRoleUser::getUserId,
                            sysUser.getUserId()));
            if (!CollectionUtils.isEmpty(sysRoleUsers)) {
                List<Long> roles = sysRoleUsers.stream().map(SysRoleUser::getRoleId)
                        .collect(Collectors.toList());

                List<SysRole> sysRoles = sysRoleMapper.selectList(
                        new LambdaQueryWrapper<SysRole>().in(SysRole::getRoleId, roles));
                List<String> collect = sysRoles.stream().map(SysRole::getRoleCode)
                        .collect(Collectors.toList());
                List<Long> collect1 = sysRoles.stream().map(SysRole::getRoleId)
                        .collect(Collectors.toList());

                sysUserDTO.setRoleIdList(collect1);
                sysUserDTO.setRoleCodeList(collect);
            }

            List<SysInst> sysInsts = sysInstMapper.selectList(
                    new LambdaQueryWrapper<SysInst>().eq(SysInst::getNewlevel, StateConstants.ZERO)
                            .eq(SysInst::getInstId, sysUser.getInstId()));
            if (!CollectionUtils.isEmpty(sysInsts)) {
                SysInst sysInst = sysInsts.get(0);
                sysUserDTO.setInstNo(sysInst.getInstNo());
                sysUserDTO.setInstId(sysInst.getInstId());
            }

            List<SysDept> sysDepts = sysDeptMapper.selectList(
                    new LambdaQueryWrapper<SysDept>().eq(SysDept::getNewlevel, StateConstants.ZERO)
                            .eq(SysDept::getDeptId, sysUser.getDeptId()));
            if (!CollectionUtils.isEmpty(sysDepts)) {
                SysDept sysDept = sysDepts.get(0);
                sysUserDTO.setDeptId(sysDept.getDeptId());
                sysUserDTO.setDeptCode(sysDept.getDeptNo());
            }
            List<SysUserDTO> list = Collections.singletonList(sysUserDTO);
            if (!CollectionUtils.isEmpty(list)) {
                setPostInfo(list);
            }
            return list.get(0);
        } else {
            throw new SunyardException("请确定当前用户是否存在。");
        }
    }

    /**
     * 根据用户id获取用户信息
     *
     * @return Result
     */
    public SysUserDTO getUserByUserId(Long userId) {
        SysUser sysUser = sysUserMapper.selectById(userId);
        SysUserDTO sysUserDTO = new SysUserDTO();
        BeanUtils.copyProperties(sysUser, sysUserDTO);
        List<SysUserDTO> list = Collections.singletonList(sysUserDTO);
        if (!CollectionUtils.isEmpty(list)) {
            setPostInfo(list);
        }
        return list.get(0);
    }

    /**
     * 根据角色id、部门id获取用户
     *
     * @param roleId 角色id
     * @param deptId 部门id
     * @return Result
     */
    public List<SysUserDTO> getUserByDeptIdAndRoleId(Long deptId, Long roleId) {
        List<SysUserDTO> userByRoleIdorDeptId = sysUserMapper.getUserByRoleIdorDeptId(null, deptId, roleId, "0");
        if (!CollectionUtils.isEmpty(userByRoleIdorDeptId)) {
            setPostInfo(userByRoleIdorDeptId);
        }
        return userByRoleIdorDeptId;
    }

    /**
     * 根据instId
     *
     * @param instId 机构id
     * @return Result
     */
    public List<SysUserDTO> getUsersByInstId(Long instId) {
        List<SysUserDTO> list = sysUserMapper.selectList(
                new LambdaQueryWrapper<SysUser>().eq(instId != null, SysUser::getInstId, instId)
                        .eq(SysUser::getState, 1))
                .stream().map(user -> {
                    SysUserDTO dto = new SysUserDTO();
                    BeanUtils.copyProperties(user, dto);
                    return dto;
                }).collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(list)) {
            setPostInfo(list);
        }
        return list;
    }

    /**
     * 设置岗位信息到用户上
     *
     * @param list
     */
    public void setPostInfo(List<SysUserDTO> list) {
        List<Long> collect = list.stream().map(SysUserDTO::getUserId).collect(Collectors.toList());
        List<SysPostUserListDTO> sysPostUserListDTOS = sysPostUserMapper.selectListByUserIdList(collect);
        if (!CollectionUtils.isEmpty(sysPostUserListDTOS)) {
            Map<Long, List<SysPostUserListDTO>> postUserMap = sysPostUserListDTOS.stream().collect(Collectors.groupingBy(SysPostUserListDTO::getUserId));
            list.forEach(user -> {
                List<SysPostUserListDTO> sysPostUserList = postUserMap.get(user.getUserId());
                if (!CollectionUtils.isEmpty(sysPostUserList)) {
                    Long[] postIds = sysPostUserList.stream().map(SysPostUserListDTO::getPostId).toArray(Long[]::new);
                    String[] postNames = sysPostUserList.stream().map(SysPostUserListDTO::getPostName).toArray(String[]::new);
                    user.setPostIds(postIds);
                    user.setPostNames(postNames);
                    user.setPostId(sysPostUserList.get(0).getPostId());
                    user.setPostName(sysPostUserList.get(0).getPostName());
                    //用户敏感信息泄露处理---微服务之间调用、反显给前端，都是走UserApiImpl，所以此处统一处理
                    //安全信息
                    user.setPwd(null);
                    user.setSalt(null);
                    user.setIsDeleted(null);
                    user.setPwdUpdateTime(null);
                    user.setUpdateTime(null);
                    user.setCreateTime(null);
                    user.getLdapId();
                    //个人信息
                    user.setCode(null);
                    user.setPhone(null);
                    user.setEmail(null);
                    //配置信息
                    user.setIsScan(null);
                    user.setDefaultMenu(null);
                    user.setThemeColor(null);
                    user.setFrameLayout(null);
                    user.setIsCollapse(null);
                    user.setIsLabel(null);
                    user.setCustomConfig(null);
                }
            });
        }
    }

    /**
     * 根据instId 和name获取用户
     * @param instId 机构id
     * @param name 登录名
     * @return Result
     */
    public List<SysUserDTO> getUsersByInstIdAndName(Long instId, String name) {
        List<SysUserDTO> collect = sysUserMapper
                .selectList(new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getState, 1)
                        .eq(!ObjectUtils.isEmpty(instId), SysUser::getInstId, instId)
                        .like(StringUtils.hasText(name), SysUser::getName, name))
                .stream().map(user -> {
                    SysUserDTO dto = new SysUserDTO();
                    BeanUtils.copyProperties(user, dto);
                    return dto;
                }).collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(collect)) {
            setPostInfo(collect);
        }
        return collect;
    }

    /**
     * 根据登录名获取邮箱
     * @param username 登录名
     * @return Result 邮箱
     */
    public Result<String> getUserMailByUserName(String username) {
        Assert.notNull(username, "参数错误");
        SysUser sysUser = sysUserMapper
                .selectOne(new LambdaUpdateWrapper<SysUser>().eq(SysUser::getLoginName, username));
        if (null != sysUser) {
            return Result.success(sysUser.getEmail());
        }
        return Result.success("");
    }

    /**
     * 发送邮箱验证码
     * @param username 登录名
     * @return Result
     */
    public Result<Boolean> sendMailCode(String username) {
        Assert.notNull(username, "参数错误");
        SysUser sysUser = sysUserMapper
                .selectOne(new LambdaUpdateWrapper<SysUser>().eq(SysUser::getLoginName, username));
        Assert.notNull(sysUser, "用户信息错误");
        Assert.notNull(sysUser.getEmail(), "用户信息错误");
        GifCaptcha gifCaptcha = CaptchaUtil.createGifCaptcha(130, 48);
        String verifyCode = gifCaptcha.getCode().toUpperCase();
        String emailUtilsContent = "您正在进行密码重置操作!\n" + "您的邮箱验证码为:%s，请勿向他人透露！有效期10分钟";
        try {
            MailClient mailClient = emailUtils.getMailClient();
            mailClient.sendMail(sysUser.getEmail(), "忘记密码",
                    String.format(emailUtilsContent, verifyCode));
        } catch (Exception e) {
            log.error("系统异常", e);
            return Result.error("发送验证码失败！请联系管理员", ResultCode.PARAM_ERROR);
        }
        redisUtils.set(CachePrefixConstants.FORGOT_PASS + username, verifyCode, 600L, TimeUnit.SECONDS);
        return Result.success(true);
    }

    /**
     * 校验状态码
     * @param username 登录名
     * @param code code
     * @return Result
     */
    public Result checkMailCode(String username, String code) {
        Assert.notNull(username, "参数错误");
        Assert.notNull(code, "参数错误");
        String reCode = redisUtils.get(CachePrefixConstants.FORGOT_PASS + username);
        if (code.equals(reCode)) {
            return Result.success(true);
        }
        return Result.error("验证码校验失败", ResultCode.PARAM_ERROR);
    }


    /**
     * 重置密码
     *
     * @param userIds 用户id
     */
    public void resetPwd(Long[] userIds) {
        Assert.notEmpty(userIds, "参数错误");
        for (Long userId : userIds) {
            SysUser user = sysUserMapper.selectById(userId);
            //随机添加32位盐
            String salt = UUIDUtils.generateUUID();
            user.setPwd(PasswordUtils.getEncryptionPassword(salt, systemProperties.getUserInitPassword()));
            user.setSalt(salt);
            user.setPwdUpdateTime(new Date());
            user.setUpdateTime(new Date());
            sysUserMapper.updateById(user);
        }
    }
    /**
     * 更新密码
     * @param username 登录名
     * @param newPwd 新密码
     * @return Result
     */
    public Result<Boolean> updatePwd(String username, String newPwd , String code) {
        Assert.isTrue(PasswordUtils.passwordValidator(newPwd), "密码校验不合法,请检查并重新输入重试...");
        Assert.notNull(username, "参数错误");
        Assert.notNull(newPwd, "参数错误");
        Assert.notNull(code, "参数错误");
        String reCode = redisUtils.get(CachePrefixConstants.FORGOT_PASS + username);
        if (!code.equals(reCode)) {
            return Result.error("验证码校验失败", ResultCode.PARAM_ERROR);
        }

        //随机生成32位盐
        String salt = UUIDUtils.generateUUID();
        sysUserMapper.update(null,
                new LambdaUpdateWrapper<SysUser>()
                        .set(SysUser::getPwd, PasswordUtils.getEncryptionPassword(salt, newPwd))
                        .set(SysUser::getSalt, salt).set(SysUser::getPwdUpdateTime, new Date())
                        .eq(SysUser::getLoginName, username));
        redisUtils.del(CachePrefixConstants.FORGOT_PASS + username);
        return Result.success(true);
    }

    /**
     * 根据机构id获取用户
     */
    public List<SysUserDTO> getUsersByInstIdList(List<Long> instId) {
        List<SysUser> sysUsers = sysUserMapper.selectList(new LambdaQueryWrapper<SysUser>()
                .in(!CollectionUtils.isEmpty(instId), SysUser::getInstId, instId)
                .eq(SysUser::getState, 1));
        List<SysUserDTO> sysUserDTOS = PageCopyListUtils.copyListProperties(sysUsers,
                SysUserDTO.class);
        if (!CollectionUtils.isEmpty(sysUserDTOS)) {
            setPostInfo(sysUserDTOS);
        }
        return sysUserDTOS;

    }

    /**
     * 根据用户名获取用户id
     * @param name 姓名
     * @return Result
     */
    public List<SysUserDTO> getUserDetailByName(String name) {
        List<SysUser> poList = sysUserMapper.selectList(
                new LambdaQueryWrapper<SysUser>().like(null != name, SysUser::getName, name)
                        .eq(SysUser::getState, 1));
        List<SysUserDTO> dtoList = poList.stream().map(po -> {
            SysUserDTO dto = new SysUserDTO();
            BeanUtils.copyProperties(po, dto);
            return dto;
        }).collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(dtoList)) {
            setPostInfo(dtoList);
        }
        return dtoList;
    }

    /**
     * 根据用户名查询用户信息
     * @param asList 登录名
     * @return Result
     */
    public List<SysUser> getUserListByUsernames(List<String> asList) {
        List<SysUser> list = sysUserMapper.selectList(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getState, 1)
                .in(!CollectionUtils.isEmpty(asList), SysUser::getLoginName, asList));
        return list;
    }

    /**
     * 查询所有用户
     * @return Result
     */
    public List<SysUser> searchAll() {
        return sysUserMapper.selectList(null);
    }

    /**
     * 通过用户姓名解锁用户
     *
     * @param userName 用户姓名
     * @param loginTypeEnum 登录方式。 管理员、普通用户
     */
    public void unlockByUserName(String userName, LoginTypeEnum loginTypeEnum) {
        Assert.notNull(userName, "参数错误");
        if (loginTypeEnum.equals(LoginTypeEnum.SUPER)) {
            //只有当state为3锁定时，才进行修改
            sysUserAdminMapper.update(null,
                    new LambdaUpdateWrapper<SysUserAdmin>()
                            .set(SysUserAdmin::getState, UserStateEnum.ENABLED.getCode())
                            .eq(SysUserAdmin::getLoginName, userName)
                            .eq(SysUserAdmin::getState, UserStateEnum.LOCKED.getCode()));
        } else {
            //只有当state为3锁定时，才进行修改
            sysUserMapper.update(null,
                    new LambdaUpdateWrapper<SysUser>()
                            .set(SysUser::getState, UserStateEnum.ENABLED.getCode())
                            .eq(SysUser::getLoginName, userName)
                            .eq(SysUser::getState, UserStateEnum.LOCKED.getCode()));
        }
        // 启用后，把登录错误记录清除
        redisUtils.del(CachePrefixConstants.LOGIN_ACCOUNT_ERROR_LIMIT + userName);
    }

    /**
     * 校验数据
     *
     * @param user 用户obj
     */
    private void checkUserData(SysUserVO user) {
        Assert.notNull(user.getLoginName(), "请填写登录账号");
        Assert.notNull(user.getName(), "请填写姓名");
        if (StringUtils.hasText(user.getEmail())) {
            Assert.isTrue(RegexpUtils.isEmail(user.getEmail()), "请填写正确的邮箱");
        }
        if (null == user.getDeptId()) {
            user.setDeptId(0L);
            SysInst sysInst = sysInstMapper.selectOne(new LambdaQueryWrapper<SysInst>()
                    .eq(SysInst::getInstId, user.getInstId()).eq(SysInst::getNewlevel, 0));
            Assert.notNull(sysInst, "请选择正确的机构");
        } else {
            List<SysDept> list = sysDeptMapper.selectList(new LambdaQueryWrapper<SysDept>()
                    .eq(SysDept::getDeptId, user.getDeptId()).orderByDesc(SysDept::getNewlevel));
            Assert.noNullElements(list, "请选择正确的部门");
            SysDept sysDept = list.get(0);
            user.setInstId(sysDept.getParentId());
        }
    }

    /**
     * 编辑用户岗位
     *
     * @param sysPostUserDTO 岗位用户id集
     */
    @Transactional(rollbackFor = Exception.class)
    public void updatePost(SysPostUserDTO sysPostUserDTO) {
        List<Long> userIds = sysPostUserDTO.getUserIds();
        List<Long> postIds = sysPostUserDTO.getPostIds();
        Assert.notEmpty(userIds, "参数不能为空");
        // 删除选中用户的所有岗位
        sysPostUserMapper
                .delete(new LambdaQueryWrapper<SysPostUser>().in(SysPostUser::getUserId, userIds));
        if (!CollectionUtils.isEmpty(postIds)) {
            List<SysPostUser> sysPostUsers = new ArrayList<>();
            // 列出所有组合
            for (Long userId : userIds) {
                for (Long postId : postIds) {
                    SysPostUser sysPostUser = new SysPostUser();
                    sysPostUser.setPostId(postId);
                    sysPostUser.setUserId(userId);
                    sysPostUsers.add(sysPostUser);
                }
            }
            // 批量插入
            MybatisBatch<SysPostUser> mybatisBatch = new MybatisBatch<>(sqlSessionFactory,
                    sysPostUsers);
            MybatisBatch.Method<SysPostUser> method = new MybatisBatch.Method<>(
                    SysPostUserMapper.class);
            mybatisBatch.execute(method.insert());
        }
    }

    /**
     * 根据用户id查询用户信息
     *
     * @param userId 用户id
     * @return Result
     */
    public SysUserDTO getUserDetailById(Long userId) {
        SysUser sysUser = sysUserMapper
                .selectOne(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUserId, userId)
                        .eq(SysUser::getState, 1));
        if (sysUser != null) {
            SysUserDTO sysUserDTO = new SysUserDTO();
            BeanUtils.copyProperties(sysUser, sysUserDTO);
            //详情应当返回所有信息

            List<SysRoleUser> sysRoleUsers = sysRoleUserMapper
                    .selectList(new LambdaQueryWrapper<SysRoleUser>().eq(SysRoleUser::getUserId,
                            sysUser.getUserId()));
            if (!CollectionUtils.isEmpty(sysRoleUsers)) {
                List<Long> roles = sysRoleUsers.stream().map(SysRoleUser::getRoleId)
                        .collect(Collectors.toList());

                List<SysRole> sysRoles = sysRoleMapper.selectList(
                        new LambdaQueryWrapper<SysRole>().in(SysRole::getRoleId, roles));
                List<String> collect = sysRoles.stream().map(SysRole::getRoleCode)
                        .collect(Collectors.toList());
                List<Long> collect1 = sysRoles.stream().map(SysRole::getRoleId)
                        .collect(Collectors.toList());

                sysUserDTO.setRoleIdList(collect1);
                sysUserDTO.setRoleCodeList(collect);
            }

            List<SysInst> sysInsts = sysInstMapper.selectList(
                    new LambdaQueryWrapper<SysInst>().eq(SysInst::getNewlevel, StateConstants.ZERO)
                            .eq(SysInst::getInstId, sysUser.getInstId()));
            if (!CollectionUtils.isEmpty(sysInsts)) {
                SysInst sysInst = sysInsts.get(0);
                sysUserDTO.setInstNo(sysInst.getInstNo());
                sysUserDTO.setInstId(sysInst.getInstId());
            }

            List<SysDept> sysDepts = sysDeptMapper.selectList(
                    new LambdaQueryWrapper<SysDept>().eq(SysDept::getNewlevel, StateConstants.ZERO)
                            .eq(SysDept::getDeptId, sysUser.getDeptId()));
            if (!CollectionUtils.isEmpty(sysDepts)) {
                SysDept sysDept = sysDepts.get(0);
                sysUserDTO.setDeptId(sysDept.getDeptId());
                sysUserDTO.setDeptCode(sysDept.getDeptNo());
            }
            List<SysUserDTO> list = Collections.singletonList(sysUserDTO);
            if (!CollectionUtils.isEmpty(list)) {
                setPostInfo(list);
            }
            return list.get(0);
        } else {
            throw new SunyardException("请确定当前用户是否存在。");
        }
    }

    public Result updatePwdByUserId(String userId, String newPwd) {
        SysUser user = sysUserMapper.selectById(userId);
        String salt = UUIDUtils.generateUUID();
        newPwd = PasswordUtils.getEncryptionPassword(salt, newPwd);
        user.setPwd(newPwd);
        user.setSalt(salt);
        user.setUpdateTime(new Date());
        user.setPwdUpdateTime(new Date());
        sysUserMapper.updateById(user);
        return Result.success(true);
    }
}
