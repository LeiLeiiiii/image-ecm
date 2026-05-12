package com.sunyard.module.system.task.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import com.sunyard.module.system.mapper.SysPostMapper;
import com.sunyard.module.system.mapper.SysPostUserMapper;
import com.sunyard.module.system.po.SysPost;
import com.sunyard.module.system.po.SysPostUser;
import com.sunyard.module.system.weaver.bo.OaPost;
import com.sunyard.module.system.weaver.bo.QueryPostRequest;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.batch.MybatisBatch;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sunyard.framework.common.util.conversion.JsonUtils;
import com.sunyard.module.system.mapper.SysDeptMapper;
import com.sunyard.module.system.mapper.SysInstMapper;
import com.sunyard.module.system.mapper.SysSynOrgMapper;
import com.sunyard.module.system.mapper.SysUserMapper;
import com.sunyard.module.system.po.SysDept;
import com.sunyard.module.system.po.SysInst;
import com.sunyard.module.system.po.SysSynOrg;
import com.sunyard.module.system.po.SysUser;
import com.sunyard.module.system.service.OrgDeptService;
import com.sunyard.module.system.service.OrgInstService;
import com.sunyard.module.system.weaver.bo.OaDept;
import com.sunyard.module.system.weaver.bo.OaInst;
import com.sunyard.module.system.weaver.bo.OaUser;
import com.sunyard.module.system.weaver.bo.QueryDeptRequest;
import com.sunyard.module.system.weaver.bo.QueryInstRequest;
import com.sunyard.module.system.weaver.bo.QueryUserRequest;
import com.sunyard.module.system.weaver.service.WeaverOaService;

import cn.hutool.core.date.LocalDateTimeUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

/**
 * @author LQ
 * @date 2025/7/8
 * @describe 机构部门用户
 */
@Slf4j
@Service
public class SyncInstDeptUserService {
    @Resource
    private SqlSessionFactory sqlSessionFactory;
    @Resource
    private SysSynOrgMapper sysSynOrgMapper;
    @Resource
    private SysInstMapper sysInstMapper;
    @Resource
    private SysDeptMapper sysDeptMapper;
    @Resource
    private SysUserMapper sysUserMapper;
    @Resource
    private SysPostMapper sysPostMapper;
    @Resource
    private SysPostUserMapper sysPostUserMapper;
    @Resource
    private WeaverOaService weaverOaService;
    @Resource
    private OrgInstService orgInstService;
    @Resource
    private OrgDeptService orgDeptService;

    /**
     * 添加中间表数据
     */
    public void synchronizationInstDeptUser() {
        //分部同步，也就是机构
        QueryInstRequest instRequest = new QueryInstRequest();
        instRequest.setCurpage(1);
        instRequest.setPagesize(99999);
        List<OaInst> instResults = weaverOaService.queryInstData(instRequest);
        if (!ObjectUtils.isEmpty(instResults)) {
            this.addSysLdapMiddle(instResults, 0);
            this.upInstData();
        }
        //部门同步
        QueryDeptRequest deptRequest = new QueryDeptRequest();
        deptRequest.setCurpage(1);
        deptRequest.setPagesize(99999);
        List<OaDept> deptResults = weaverOaService.queryDeptData(deptRequest);
        if (!ObjectUtils.isEmpty(deptResults)) {
            this.addSysLdapMiddle(deptResults, 1);
            this.upDeptData();
        }
        //用户同步
        QueryUserRequest userRequest = new QueryUserRequest();
        userRequest.setCurpage(1);
        userRequest.setPagesize(99999);
        List<OaUser> userResults = weaverOaService.queryUserData(userRequest);
        if (!ObjectUtils.isEmpty(userResults)) {
            this.addSysLdapMiddle(userResults, 2);
            this.upUserData();
        }
        // 岗位同步
        QueryPostRequest postRequest = new QueryPostRequest();
        postRequest.setCurpage(1);
        postRequest.setPagesize(99999);
        List<OaPost> postResults = weaverOaService.queryPostData(postRequest);
        if (!ObjectUtils.isEmpty(postResults)) {
            this.addSysLdapMiddle(postResults, 3);
            this.upPostData();
        }

    }

    /**
     * 添加中间表数据
     */
    public void addSysLdapMiddle(List list, Integer dataType) {
        if (list != null && !list.isEmpty()) {
            SysSynOrg sysSynOrg = new SysSynOrg();
            sysSynOrg.setBody(JsonUtils.toJSONString(list));
            sysSynOrg.setType(dataType);
            sysSynOrgMapper.insert(sysSynOrg);
        }
    }

    /**
     * 岗位同步
     */
    public void upPostData() {
        LocalDateTime startOfDay = LocalDateTimeUtil.beginOfDay(LocalDate.now());
        LocalDateTime endOfDay = LocalDateTimeUtil.endOfDay(LocalDate.now());

        // 查询中间表获取当日更新的岗位数据
        SysSynOrg sysSynOrg = sysSynOrgMapper.selectList(new LambdaQueryWrapper<SysSynOrg>()
                .eq(SysSynOrg::getType, 3).between(SysSynOrg::getCreateTime, startOfDay, endOfDay)
                .orderByDesc(SysSynOrg::getCreateTime)).get(0);

        ArrayList<OaPost> oaPosts = new ArrayList<>();
        oaPosts.addAll(JSONObject.parseArray(sysSynOrg.getBody(), OaPost.class));
        // 查询岗位表，拿出目前已有岗位
        List<SysPost> sysPostList = sysPostMapper.selectList(new LambdaQueryWrapper<SysPost>()
                .and(w -> w.eq(SysPost::getIsDeleted, 0).or().eq(SysPost::getIsDeleted, 1)));
        Map<Long, SysPost> postMap = new HashMap<>();
        for (SysPost post : sysPostList) {
            postMap.put(post.getPostId(), post);
        }
        // 存储需要新增和修改的岗位
        List<SysPost> addPostList = new ArrayList<>();
        List<SysPost> updatePostList = new ArrayList<>();
        // 遍历OA岗位数据
        for (OaPost oaPost : oaPosts) {
            // 判断id是否有值
            if (ObjectUtils.isEmpty(oaPost.getId())) {
                continue;
            }
            // 基本属性
            Long postId = Long.parseLong(oaPost.getId());
            String name = oaPost.getJobtitlename();
            String postCode = oaPost.getJobtitlecode();
            String remarks = oaPost.getJobtitleremark();
            // 检查岗位是否存在
            if (postMap.containsKey(postId)) {
                // 修改
                SysPost sysPost = postMap.get(postId);
                sysPost.setName(name);
                sysPost.setInstId(521L);
                sysPost.setPostCode(postCode);
                sysPost.setRemarks(remarks);
                sysPost.setIsDeleted(0);
                updatePostList.add(sysPost);
            }else {
                // 增加
                SysPost sysPost = new SysPost();
                sysPost.setPostId(postId);
                sysPost.setInstId(521L);
                sysPost.setName(name);
                sysPost.setPostCode(postCode);
                sysPost.setRemarks(remarks);
                addPostList.add(sysPost);
            }
        }
        // 批量插入和修改
        if (CollectionUtils.isNotEmpty(addPostList)) {
            MybatisBatch<SysPost> mybatisBatch = new MybatisBatch<>(sqlSessionFactory, addPostList);
            MybatisBatch.Method<SysPost> method = new MybatisBatch.Method<>(SysPostMapper.class);
            mybatisBatch.execute(method.insert());
        }
        if (CollectionUtils.isNotEmpty(updatePostList)) {
            MybatisBatch<SysPost> mybatisBatch = new MybatisBatch<>(sqlSessionFactory, updatePostList);
            MybatisBatch.Method<SysPost> method = new MybatisBatch.Method<>(SysPostMapper.class);
            mybatisBatch.execute(method.updateById());
        }
    }

    /** 机构同步 */
    private void upInstData() {
        LocalDateTime startOfDay = LocalDateTimeUtil.beginOfDay(LocalDate.now());
        LocalDateTime endOfDay = LocalDateTimeUtil.endOfDay(LocalDate.now());

        //查询中间表，获取当日更新的机构数据
        SysSynOrg sysSynOrg = sysSynOrgMapper.selectList(new LambdaQueryWrapper<SysSynOrg>()
                .eq(SysSynOrg::getType, 0).between(SysSynOrg::getCreateTime, startOfDay, endOfDay)
                .orderByDesc(SysSynOrg::getCreateTime)).get(0);
        ArrayList<OaInst> onInstList = new ArrayList<>();
        onInstList.addAll(JSONObject.parseArray(sysSynOrg.getBody(), OaInst.class));
        //查询部门表，拿出目前已有部门
        List<SysInst> sysInstList = sysInstMapper.selectList(new LambdaQueryWrapper<>());
        // 2. 创建现有机构ID的映射表
        Map<Long, SysInst> instMap = new HashMap<>();
        for (SysInst inst : sysInstList) {
            instMap.put(inst.getInstId(), inst);
        }

        // 3. 存储需要新增和修改的机构
        List<SysInst> addList = new ArrayList<>();
        List<SysInst> editList = new ArrayList<>();

        // 4. 遍历JSON数组
        for (OaInst oaInst : onInstList) {
            // 获取ID并转换类型
            if (ObjectUtils.isEmpty(oaInst.getId())) {
                oaInst.setId("0");
            }
            Long id = Long.parseLong(oaInst.getId());
            String instNo = ObjectUtils.isEmpty(oaInst.getSubcompanycode()) ? ""
                    : oaInst.getSubcompanycode();
            if (ObjectUtils.isEmpty(oaInst.getSupsubcomid())) {
                continue;
            }
            Long parentId = Long.parseLong(oaInst.getSupsubcomid());
            String name = ObjectUtils.isEmpty(oaInst.getSubcompanydesc()) ? ""
                    : oaInst.getSubcompanydesc();

            // 检查机构是否存在
            if (instMap.containsKey(id)) {
                SysInst existingInst = instMap.get(id);
                // 检查字段是否需要更新
                if (!Objects.equals(existingInst.getInstNo(), instNo)
                        || !Objects.equals(existingInst.getParentId(), (parentId))
                        || !Objects.equals(existingInst.getName(), name)) {

                    // 更新字段
                    existingInst.setId(id);
                    existingInst.setInstNo(instNo);
                    existingInst.setParentId(parentId);
                    existingInst.setName(name);
                    editList.add(existingInst);
                }
            } else {
                // 创建新机构
                SysInst newInst = new SysInst();
                newInst.setInstId(id);
                newInst.setInstNo(instNo);
                newInst.setParentId(parentId);
                newInst.setName(name);
                addList.add(newInst);
            }
        }
        try {
            for (SysInst inst : addList) {
                orgInstService.add(inst);
            }
            //修改
            for (SysInst inst : editList) {
                orgInstService.update(inst);
            }
        } catch (Exception e) {
            log.error("同步机构数据异常", e);
        }
    }

    /** 部门同步 */
    private void upDeptData() {
        LocalDateTime startOfDay = LocalDateTimeUtil.beginOfDay(LocalDate.now());
        LocalDateTime endOfDay = LocalDateTimeUtil.endOfDay(LocalDate.now());

        SysSynOrg sysSynOrg = sysSynOrgMapper.selectList(new LambdaQueryWrapper<SysSynOrg>()
                .eq(SysSynOrg::getType, 1).between(SysSynOrg::getCreateTime, startOfDay, endOfDay)
                .orderByDesc(SysSynOrg::getCreateTime)).get(0);
        ArrayList<OaDept> oaInstList = new ArrayList<>();
        oaInstList.addAll(JSONObject.parseArray(sysSynOrg.getBody(), OaDept.class));
        ArrayList<OaDept> newOaDateList = oaInstList.stream()
                .sorted(Comparator.comparing(OaDept::getSupdepid)
                        .thenComparing(OaDept::getSubcompanyid1).thenComparing(OaDept::getSupdepid))
                .collect(Collectors.toCollection(ArrayList::new));

        // 查询数据库现有数据（条件不变）
        List<SysDept> sysDeptList = sysDeptMapper
                .selectList(new LambdaQueryWrapper<SysDept>().eq(SysDept::getNewlevel, 0));

        //查询部门表，拿出目前已有部门
        List<SysInst> sysInstList = sysInstMapper.selectList(new LambdaQueryWrapper<>());
        // 2. 创建现有机构ID的映射表
        Map<Long, SysInst> instMap = new HashMap<>();
        for (SysInst inst : sysInstList) {
            instMap.put(inst.getInstId(), inst);
        }

        // 构建现有部门ID映射（若sysDeptList为空则创建空Map）
        Map<Long, SysDept> deptMap = ObjectUtils.isEmpty(sysDeptList) ? new HashMap<>()
                : sysDeptList.stream().collect(Collectors.toMap(SysDept::getDeptId, dept -> dept));

        List<SysDept> addList = new ArrayList<>();
        List<SysDept> editList = new ArrayList<>();
        try {
            for (OaDept oaDept : newOaDateList) {
                log.debug("部门同步:{}", JsonUtils.toJSONString(oaDept));
                //判断部门的机构id是否存在
                if (ObjectUtils.isEmpty(oaDept.getSubcompanyid1())) {
                    continue;
                }
                if (instMap.get(Long.parseLong(oaDept.getSubcompanyid1())) == null) {
                    continue;
                }
                Long id = Long.parseLong(oaDept.getId());
                String deptNo = oaDept.getDepartmentcode();
                if (ObjectUtils.isEmpty(deptNo)) {
                    deptNo = id.toString();
                }
                String supdepid = oaDept.getSupdepid();
                Long parentId = "0".equals(supdepid) || ObjectUtils.isEmpty(supdepid)
                        ? Long.parseLong(oaDept.getSubcompanyid1())
                        : Long.parseLong(supdepid);
                String name = oaDept.getDepartmentname();
                if (deptMap.containsKey(id)) {
                    SysDept existingDept = deptMap.get(id);
                    // 检查字段变化
                    if (!Objects.equals(existingDept.getDeptNo(), deptNo)
                            || !Objects.equals(existingDept.getParentId(), parentId)
                            || !Objects.equals(existingDept.getName(), name)) {
                        existingDept.setDeptNo(deptNo);
                        existingDept.setParentId(parentId);
                        existingDept.setName(name);
                        editList.add(existingDept);
                    }
                } else {
                    // 新增部门
                    SysDept newDept = new SysDept();
                    newDept.setDeptId(id);
                    newDept.setDeptNo(deptNo);
                    newDept.setParentId(parentId);
                    newDept.setName(name);
                    addList.add(newDept);
                }
            }
            //必须排序，因为是闭包表的数据结构，并且直接调用的页面新增接口，所以需要从父级一级一级插入，数据排序也根据父级id从小到大
            List<SysDept> sortedAddList = addList.stream()
                    .sorted(Comparator.comparingLong(SysDept::getParentId)
                            .thenComparingLong(SysDept::getDeptId))
                    .collect(Collectors.toCollection(ArrayList::new));
            for (SysDept dept : sortedAddList) {
                orgDeptService.add(dept);
            }
            //修改
            for (SysDept dept : editList) {
                orgDeptService.update(dept);
            }
        } catch (Exception e) {
            log.error("同步机构数据异常", e);
        }
    }

    /** 用户同步 */
    private void upUserData() {
        LocalDateTime startOfDay = LocalDateTimeUtil.beginOfDay(LocalDate.now());
        LocalDateTime endOfDay = LocalDateTimeUtil.endOfDay(LocalDate.now());

        //查询用户表，拿出目前已有用户
        List<SysUser> sysUserList = sysUserMapper.selectList(new LambdaQueryWrapper<SysUser>()
                .and(w -> w.eq(SysUser::getIsDeleted, 0).or().eq(SysUser::getIsDeleted, 1)));
        //查询用户岗位关联表
        List<SysPostUser> sysUserPostList = sysPostUserMapper.selectList(new LambdaQueryWrapper<>());
        SysSynOrg sysSynOrg = sysSynOrgMapper.selectList(new LambdaQueryWrapper<SysSynOrg>()
                .eq(SysSynOrg::getType, 2).between(SysSynOrg::getCreateTime, startOfDay, endOfDay)
                .orderByDesc(SysSynOrg::getCreateTime)).get(0);

        ArrayList<OaUser> oaUserList = new ArrayList<>();
        oaUserList.addAll(JSONObject.parseArray(sysSynOrg.getBody(), OaUser.class));

        // 2. 创建现有用户ID的映射表 岗位人员映射表
        Map<Long, SysUser> UserMap = new HashMap<>();
        for (SysUser User : sysUserList) {
            UserMap.put(User.getUserId(), User);
        }
        Map<String, SysPostUser> postUserMap = new HashMap<>();
        for (SysPostUser postUser : sysUserPostList) {
            postUserMap.put(postUser.getUserId() + "_" + postUser.getPostId(), postUser);
        }

        // 3. 存储需要新增和修改的用户 以及岗位id关联表
        List<SysUser> addList = new ArrayList<>();
        List<SysUser> editList = new ArrayList<>();
        List<SysPostUser> addPostUserList = new ArrayList<>();

        // 4. 遍历JSON数组
        for (OaUser oaUser : oaUserList) {
            // 获取人员ID并转换类型
            if (ObjectUtils.isEmpty(oaUser.getId())) {
                continue;
            }
            Long userId = Long.parseLong(oaUser.getId());
            Long postId = null;
            if (StringUtils.hasText(oaUser.getJobtitle())) {
                postId = Long.parseLong(oaUser.getJobtitle());
            }
            // 字段映射 - 按新规则转换
            if (ObjectUtils.isEmpty(oaUser.getDepartmentid())) {
                continue;
            }
            if (ObjectUtils.isEmpty(oaUser.getSubcompanyid1())) {
                continue;
            }
            String name = oaUser.getLastname(); // 人员名称
            Long deptId = Long.parseLong(oaUser.getDepartmentid()); // 部门ID
            Long instId = Long.parseLong(oaUser.getSubcompanyid1()); // 分部ID
            //国银金租信息科技归集资料,部门用户数据迁移
            if (deptId.equals(10503l)) {
                deptId = 271l;
                instId = 21l;
            }
            String loginName = oaUser.getLoginid(); // 登录名
            String code = oaUser.getWorkcode(); // 人员编号

            String phone = oaUser.getMobile(); // 移动电话
            String sex = oaUser.getSex(); // 性别
            String email = oaUser.getEmail(); // 邮箱
            String status = oaUser.getStatus(); // 用户状态

            Integer state = this.convertBStatusToAStatus(status);
            // 检查人员是否存在
            if (UserMap.containsKey(userId)) {
                SysUser existingUser = UserMap.get(userId);
                // 检查所有字段是否需要更新
                boolean needUpdate = !Objects.equals(existingUser.getLoginName(), loginName)
                        || !Objects.equals(existingUser.getName(), name)
                        || !Objects.equals(existingUser.getCode(), code)
                        || !Objects.equals(existingUser.getDeptId(), deptId)
                        || !Objects.equals(existingUser.getPhone(), phone)
                        || !Objects.equals(existingUser.getEmail(), email)
                        || !Objects.equals(existingUser.getInstId(), instId)
                        || !Objects.equals(existingUser.getState(), state);

                if (needUpdate) {
                    // 更新所有字段
                    existingUser.setLoginName(loginName);
                    existingUser.setName(name);
                    existingUser.setCode(code);
                    existingUser.setDeptId(deptId);
                    existingUser.setPhone(phone);
                    existingUser.setSex("男".equals(sex) ? 1 : 0);
                    existingUser.setEmail(email);
                    existingUser.setInstId(instId);
                    existingUser.setType(0);
                    existingUser.setPwd("1");
                    existingUser.setState(state);
                    existingUser.setSalt(loginName);
                    existingUser.setIsDeleted(0);
                    editList.add(existingUser);
                }
            } else {
                // 创建新人员对象
                SysUser newUser = new SysUser();
                newUser.setUserId(userId); // 人员ID
                newUser.setLoginName(loginName); // 登录名
                newUser.setName(name); // 姓名
                newUser.setCode(code); // 编号
                newUser.setDeptId(deptId); // 部门ID
                newUser.setPhone(phone); // 手机号
                newUser.setSex("男".equals(sex) ? 1 : 0);
                newUser.setEmail(email); // 邮箱
                newUser.setInstId(instId); // 所属分部ID
                newUser.setType(0);
                newUser.setPwd("1");
                newUser.setState(state);
                newUser.setSalt(loginName);
                addList.add(newUser);
            }
            // 判断岗位人员关联
            if (postId != null && !postUserMap.containsKey(userId + "_" + postId)) {
                // 新增
                SysPostUser sysPostUser = new SysPostUser();
                sysPostUser.setPostId(postId);
                sysPostUser.setUserId(userId);
                addPostUserList.add(sysPostUser);
            }
        }

        //批量新增
        MybatisBatch<SysUser> mybatisBatch = new MybatisBatch<>(sqlSessionFactory, addList);
        MybatisBatch.Method<SysUser> method = new MybatisBatch.Method<>(SysUserMapper.class);
        mybatisBatch.execute(method.insert());

        //修改
        for (SysUser User : editList) {
            LambdaQueryWrapper<SysUser> userQueryWrapper = new LambdaQueryWrapper<>();
            userQueryWrapper.eq(SysUser::getUserId, User.getUserId());
            sysUserMapper.update(User, userQueryWrapper);
        }
        // 人员岗位关联
        if (CollectionUtils.isNotEmpty(addPostUserList)) {
            List<Long> userIds = addPostUserList.stream().map(SysPostUser::getUserId).collect(Collectors.toList());
            // 删除之前的
            sysPostUserMapper
                    .delete(new LambdaQueryWrapper<SysPostUser>().in(SysPostUser::getUserId, userIds));
            // 批量插入
            MybatisBatch<SysPostUser> mybatisBatchPostUser = new MybatisBatch<>(sqlSessionFactory,
                    addPostUserList);
            MybatisBatch.Method<SysPostUser> methodPostUser = new MybatisBatch.Method<>(
                    SysPostUserMapper.class);
            mybatisBatchPostUser.execute(methodPostUser.insert());
        }

    }

    /**
     * 将 B 系统用户状态转换为 A 系统用户状态
     *
     * @return 对应的 A 系统状态：0=未启用, 1=启用, 2=注销
     */
    private int convertBStatusToAStatus(String statusStr) {
        if (ObjectUtils.isEmpty(statusStr)) {
            return 0; // 未启用
        }
        int bStatus = Integer.parseInt(statusStr);
        switch (bStatus) {
            case 1: // 正式
            case 2: // 临时
            case 3: // 试用延期
                return 1; // 启用
            case 4: // 解聘
            case 5: // 离职
            case 6: // 退休
            case 7: // 无效
                return 2; // 注销
            case 0: // 试用（未被包含在启用或注销中）
            default:
                return 0; // 未启用
        }
    }

}
