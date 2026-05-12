package com.sunyard.edm.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import com.baomidou.mybatisplus.core.batch.MybatisBatch;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import com.baomidou.lock.annotation.Lock4j;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.sunyard.edm.constant.DocConstants;
import com.sunyard.edm.dto.DocSysTeamDTO;
import com.sunyard.edm.dto.TeamInfoDTO;
import com.sunyard.edm.mapper.DocBsDocumentUserMapper;
import com.sunyard.edm.mapper.DocSysHouseUserMapper;
import com.sunyard.edm.mapper.DocSysTeamMapper;
import com.sunyard.edm.mapper.DocSysTeamUserMapper;
import com.sunyard.edm.po.DocBsDocumentUser;
import com.sunyard.edm.po.DocSysHouseUser;
import com.sunyard.edm.po.DocSysTeam;
import com.sunyard.edm.po.DocSysTeamUser;
import com.sunyard.framework.common.page.PageForm;
import com.sunyard.framework.common.result.Result;
import com.sunyard.framework.common.token.AccountToken;
import com.sunyard.framework.common.util.AssertUtils;
import com.sunyard.framework.mybatis.util.PageCopyListUtils;
import com.sunyard.framework.mybatis.util.SnowflakeUtils;
import com.sunyard.module.system.api.DeptApi;
import com.sunyard.module.system.api.InstApi;
import com.sunyard.module.system.api.RoleApi;
import com.sunyard.module.system.api.UserApi;
import com.sunyard.module.system.api.dto.SysDeptDTO;
import com.sunyard.module.system.api.dto.SysInstDTO;
import com.sunyard.module.system.api.dto.SysRoleDTO;
import com.sunyard.module.system.api.dto.SysUserDTO;

/**
 * @author wt
 * @date 11.16
 * @DESC 系统管理-团队管理实现类
 */
@Service
public class SysTeamService {
    @Resource
    private SnowflakeUtils snowflakeUtil;
    @Resource
    private SqlSessionFactory sqlSessionFactory;
    @Resource
    private DocSysTeamMapper docSysTeamMapper;
    @Resource
    private DocSysTeamUserMapper docSysTeamUserMapper;
    @Resource
    private DocSysHouseUserMapper docSysHouseUserMapper;
    @Resource
    private DocBsDocumentUserMapper docBsDocumentUserMapper;
    @Resource
    private UserApi userApi;
    @Resource
    private RoleApi roleApi;
    @Resource
    private InstApi instApi;
    @Resource
    private DeptApi deptApi;

    /**
     * 查询
     */
    public PageInfo<DocSysTeam> searchTeam(DocSysTeamDTO docSysTeamVo, PageForm pageForm) {
        PageHelper.startPage(pageForm.getPageNum(), pageForm.getPageSize());
        List<DocSysTeam> teamList = docSysTeamMapper.selectList(
                new LambdaQueryWrapper<DocSysTeam>()
                        .like(!StringUtils.isEmpty(docSysTeamVo.getTeamName()), DocSysTeam::getTeamName, docSysTeamVo.getTeamName())
                        .orderByDesc(DocSysTeam::getCreateTime)
        );
        if (CollectionUtils.isEmpty(teamList)) {
            return new PageInfo<>(teamList);
        }
        return new PageInfo<>(teamList);
    }

    /**
     * 删除
     */
    @Transactional(rollbackFor = Exception.class)
    public void delTeam(Long[] teamIds) {
        AssertUtils.isNull(teamIds, "参数错误");
        //查询关联表中关联类型有关团队的可管理关联信息
        delHouseTeam(teamIds);
        delHouseDoc(teamIds);
        docSysTeamUserMapper.delete(
                new LambdaQueryWrapper<DocSysTeamUser>()
                        .in(DocSysTeamUser::getTeamId, teamIds)
        );
        docSysHouseUserMapper.delete(
                new LambdaQueryWrapper<DocSysHouseUser>()
                        .in(DocSysHouseUser::getRelId, teamIds)
        );
        docBsDocumentUserMapper.delete(
                new LambdaQueryWrapper<DocBsDocumentUser>()
                        .in(DocBsDocumentUser::getRelId, teamIds)
        );
        docSysTeamMapper.delete(
                new LambdaQueryWrapper<DocSysTeam>()
                        .in(DocSysTeam::getTeamId, teamIds)
        );
    }

    /**
     * 判断团队文档库关联关系
     */
    private void delHouseTeam(Long[] teamIds) {
        List<DocSysHouseUser> docSysHousecollect = docSysHouseUserMapper.selectList(
                new LambdaQueryWrapper<DocSysHouseUser>()
                        //仅当下团队id对应关联信息
                        .in(DocSysHouseUser::getRelId, teamIds)
                        .eq(DocSysHouseUser::getType, DocConstants.TEAM)
                        .eq(DocSysHouseUser::getPermissType, DocConstants.DOC_COMMON_PERMISSION_TYPE_MANAGE)
        );
        if (CollectionUtils.isEmpty(docSysHousecollect)) {
            return;
        }
        //过滤掉houseId为空的数据
        List<Long> houseIdcollect = docSysHousecollect.stream().filter(s -> s.getHouseId() != null).map(DocSysHouseUser::getHouseId).collect(Collectors.toList());
        //过滤掉文档库id为空的数据有可能就没数据了，还是空
        if (CollectionUtils.isEmpty(houseIdcollect)) {
            return;
        }
        //据文档库ID判断还有无其他管理者
        List<DocSysHouseUser> docSysHousePremiss = docSysHouseUserMapper.selectList(
                new LambdaQueryWrapper<DocSysHouseUser>()
                        .in(DocSysHouseUser::getHouseId, houseIdcollect)
                        .eq(DocSysHouseUser::getPermissType, DocConstants.DOC_COMMON_PERMISSION_TYPE_MANAGE)
        );
        if (CollectionUtils.isEmpty(docSysHousePremiss)) {
            return;
        }
        //根据文档库id分组
        //求出每组权限为2的数量，比较是否为1
        Map<Long, List<DocSysHouseUser>> preCollect = docSysHousePremiss.stream()
                .collect(Collectors.groupingBy(DocSysHouseUser::getHouseId));
        for (Long t : houseIdcollect) {
            AssertUtils.isTrue(preCollect.get(t).size() == DocConstants.ONE, "存在唯一管理者无法删除");
        }
    }


    /**
     * 判断团队文档文件夹关联关系
     */
    private void delHouseDoc(Long[] teamIds) {
        List<DocBsDocumentUser> docSysDocCollect = docBsDocumentUserMapper.selectList(
                new LambdaQueryWrapper<DocBsDocumentUser>()
                        .in(DocBsDocumentUser::getRelId, teamIds)
                        .eq(DocBsDocumentUser::getType, DocConstants.TEAM)
                        .eq(DocBsDocumentUser::getPermissType, DocConstants.DOC_COMMON_PERMISSION_TYPE_MANAGE)
        );
        if (CollectionUtils.isEmpty(docSysDocCollect)) {
            return;
        }
        List<Long> docIdcollect = docSysDocCollect.stream().filter(s -> s.getDocId() != null).map(DocBsDocumentUser::getDocId).collect(Collectors.toList());
        //过滤掉文档文件夹id为空的数据有可能就没数据了，还是空
        if (CollectionUtils.isEmpty(docIdcollect)) {
            return;
        }
        //据文档库ID判断还有无其他管理者
        List<DocBsDocumentUser> docSysDocPremiss = docBsDocumentUserMapper.selectList(
                new LambdaQueryWrapper<DocBsDocumentUser>()
                        .in(DocBsDocumentUser::getDocId, docIdcollect)
                        .eq(DocBsDocumentUser::getPermissType, DocConstants.DOC_COMMON_PERMISSION_TYPE_MANAGE)
        );
        if (CollectionUtils.isEmpty(docSysDocPremiss)) {
            return;
        }
        //获取文档库ID
        //根据文档库id进行分组，计算每组有管理权限的关联数，大于1可以删除，否则提示不可删除
        Map<Long, List<DocBsDocumentUser>> premissCollect = docSysDocPremiss.stream().collect(Collectors.groupingBy(DocBsDocumentUser::getDocId));
        for (Long s : docIdcollect) {
            AssertUtils.isTrue(premissCollect.get(s).size() == DocConstants.ONE, "存在唯一管理者无法删除");
        }
    }


    /**
     * 新增
     *
     */
    @Lock4j(keys = "#teamName")
    @Transactional(rollbackFor = Exception.class)
    public void addTeam(DocSysTeamDTO docSysTeamVo) {
        AssertUtils.isNull(docSysTeamVo.getTeamName(), "参数错误");
        AssertUtils.isNull(docSysTeamVo.getUserIds(), "参数错误");
        //新增团队信息
        DocSysTeam docSysTeam = new DocSysTeam();
        BeanUtils.copyProperties(docSysTeamVo, docSysTeam);
        //判断团队名称是否重名
        teamNameHave(docSysTeamVo);
        long teamId = snowflakeUtil.nextId();
        docSysTeam.setTeamId(teamId);
        docSysTeam.setUpdateTime(new Date());
        docSysTeamMapper.insert(docSysTeam);
        //新增团队用户关联关系
        addTeamUserMsg(docSysTeamVo.getUserIds(), teamId);
    }

    /**
     * 增加
     */
    private void addTeamUserMsg(List<Long> userIds, long teamId) {
        List<DocSysTeamUser> docSysTeamUsers = new ArrayList<>();
        for (Long s : userIds) {
            DocSysTeamUser docSysTeamUser = new DocSysTeamUser();
            docSysTeamUser.setTeamId(teamId);
            docSysTeamUser.setUserId(s);
            docSysTeamUsers.add(docSysTeamUser);
        }
        MybatisBatch<DocSysTeamUser> docBatchs = new MybatisBatch<>(sqlSessionFactory, docSysTeamUsers);
        MybatisBatch.Method<DocSysTeamUser> docMethod = new MybatisBatch.Method<>(DocSysTeamUserMapper.class);
        docBatchs.execute(docMethod.insert());
    }


    /**
     * 修改团队信息
     */
    @Lock4j(keys = "#teamName")
    @Transactional(rollbackFor = Exception.class)
    public void updateTeam(DocSysTeamDTO docSysTeamVo) {
        AssertUtils.isNull(docSysTeamVo.getUserIds(), "参数错误");
        AssertUtils.isNull(docSysTeamVo.getTeamId(), "参数错误");
        AssertUtils.isNull(docSysTeamVo.getTeamName(), "参数错误");
        //判断团队名称是否重名
        Long teamNameNum = docSysTeamMapper.selectCount(new LambdaQueryWrapper<DocSysTeam>()
                .eq(DocSysTeam::getTeamName, docSysTeamVo.getTeamName())
                .ne(!ObjectUtils.isEmpty(docSysTeamVo.getTeamId()), DocSysTeam::getTeamId, docSysTeamVo.getTeamId())
        );
        Assert.isTrue(teamNameNum.equals(DocConstants.DOC_TEAM_EQNAME), "团队名称不能重名");
        docSysTeamMapper.update(null,
                new LambdaUpdateWrapper<DocSysTeam>()
                        .set(DocSysTeam::getTeamName, docSysTeamVo.getTeamName())
                        .set(!StringUtils.isEmpty(docSysTeamVo.getTeamRemark()),DocSysTeam::getTeamRemark, docSysTeamVo.getTeamRemark())
                        .eq(DocSysTeam::getTeamId, docSysTeamVo.getTeamId())
        );
        docSysTeamUserMapper.delete(
                new LambdaQueryWrapper<DocSysTeamUser>()
                        .eq(DocSysTeamUser::getTeamId, docSysTeamVo.getTeamId())
        );
        addTeamUserMsg(docSysTeamVo.getUserIds(), docSysTeamVo.getTeamId());
    }

    private void teamNameHave(DocSysTeamDTO docSysTeamVo) {
        Long teamNameNum = docSysTeamMapper.selectCount(new LambdaQueryWrapper<DocSysTeam>()
                .eq(DocSysTeam::getTeamName, docSysTeamVo.getTeamName()));
        Assert.isTrue(teamNameNum.equals(DocConstants.DOC_TEAM_EQNAME), "团队名称不能重名");
    }

    /**
     * 修改-展示原有团队的成员
     */
    public List<SysUserDTO> selectTeamUser(Long teamId) {
        AssertUtils.isNull(teamId, "参数错误");
        List<DocSysTeamUser> teamUserList = docSysTeamUserMapper.selectList(
                new LambdaQueryWrapper<DocSysTeamUser>()
                        .eq(DocSysTeamUser::getTeamId, teamId)
        );
        if (CollectionUtils.isEmpty(teamUserList)) {
            return null;
        }
        //获取用户ID集合
        List<Long> userIdList = teamUserList.stream().map(DocSysTeamUser::getUserId).collect(Collectors.toList());
        //根据用户ID查询对应用户信息
        Result<List<SysUserDTO>> userListByUserIds = userApi.getUserListByUserIds(userIdList.toArray(new Long[userIdList.size()]));
        List<SysUserDTO> sysUserDTOS = PageCopyListUtils.copyListProperties(userListByUserIds.getData(), SysUserDTO.class);
        if (!CollectionUtils.isEmpty(userListByUserIds.getData())) {
            getInstDeptName(sysUserDTOS);
        }
        return sysUserDTOS;
    }

    /**
     * 计算权限 机构 部门 团队 用户 的数量
     */
    public TeamInfoDTO getTypeNum(Long teamId) {
        AssertUtils.isNull(teamId, "参数错误");
        Long teamNum = docSysTeamUserMapper.selectCount(
                new LambdaQueryWrapper<DocSysTeamUser>()
                        .eq(DocSysTeamUser::getTeamId, teamId)
        );
        TeamInfoDTO teamInfoDTO = new TeamInfoDTO();
        teamInfoDTO.setUserNum(teamNum);
        return teamInfoDTO;
    }

    /**
     * 查询详情
     */
    public TeamInfoDTO getInfo(Long teamId, PageForm pageForm) {
        AssertUtils.isNull(teamId, "参数错误");
        //根据团队ID查询唯一团队数据
        DocSysTeam docSysTeamContext = docSysTeamMapper.selectById(teamId);
        //根据团队ID查询所有团队用户关联数据
        List<DocSysTeamUser> teamUser = docSysTeamUserMapper.selectList(
                new LambdaQueryWrapper<DocSysTeamUser>()
                        .eq(DocSysTeamUser::getTeamId, teamId)
        );
        //初始化返回的扩展
        TeamInfoDTO teamInfoDTO = new TeamInfoDTO();
        if (!CollectionUtils.isEmpty(teamUser)) {
            //获取用户ID集合
            List<Long> userIdList = teamUser.stream().map(DocSysTeamUser::getUserId).collect(Collectors.toList());
            PageHelper.startPage(pageForm.getPageNum(), pageForm.getPageSize());
            //根据用户ID查询对应用户信息
            Result<List<SysUserDTO>> userListByUserIds = userApi.getUserListByUserIds(userIdList.toArray(new Long[userIdList.size()]));
            if (!CollectionUtils.isEmpty(userListByUserIds.getData())) {
                PageInfo<SysUserDTO> pageInfo =
                        PageCopyListUtils.getPageInfo(new PageInfo<>(userListByUserIds.getData()), SysUserDTO.class);
                List<SysUserDTO> list = pageInfo.getList();
                getInstDeptName(list);
                pageInfo.setList(list);
                teamInfoDTO.setDocSysTeamUserExtendPageInfo(pageInfo);
                teamInfoDTO.setDocSysTeam(docSysTeamContext);
            } else {
                teamInfoDTO.setDocSysTeam(docSysTeamContext);
            }
        } else {
            teamInfoDTO.setDocSysTeam(docSysTeamContext);
        }
        return teamInfoDTO;
    }


    /**
     * 展示用户及所属机构列表
     */
    public PageInfo<SysUserDTO> getInstTeamUser(SysUserDTO userExtend, PageForm pageForm) {
        //查询符合角色的关联信息
        Result<List<SysUserDTO>> userRole = userApi.getUserByDeptIdAndRoleId(null, userExtend.getRoleId());
        if (!ObjectUtils.isEmpty(userExtend.getRoleId()) && CollectionUtils.isEmpty(userRole.getData())) {
            return new PageInfo<>();
        }
        List<Long> userRolecollect = new ArrayList<>();
        if (!ObjectUtils.isEmpty(userExtend.getRoleId())) {
            userRolecollect = userRole.getData().stream().map(SysUserDTO::getUserId).collect(Collectors.toList());
        }
        PageHelper.startPage(pageForm.getPageNum(), pageForm.getPageSize());
        Result<List<SysUserDTO>> userDetailByName = userApi.getUserDetailByName(userExtend.getName());

        List<SysUserDTO> userData = userDetailByName.getData();
        List<Long> finalUserRolecollect = userRolecollect;
        List<SysUserDTO> filteredData = userData.stream()
                .filter(user -> {
                    boolean deptInstCondition = !ObjectUtils.isEmpty(userExtend.getDeptId()) || !ObjectUtils.isEmpty(userExtend.getInstId());
                    boolean userRoleCondition = !ObjectUtils.isEmpty(finalUserRolecollect);
                    if (deptInstCondition && userRoleCondition) {
                        return (
                                (user.getDeptId() != null && user.getDeptId().equals(userExtend.getDeptId())) ||
                                        (user.getInstId() != null && user.getInstId().equals(userExtend.getInstId()))
                        ) && finalUserRolecollect.contains(user.getUserId());
                    } else if (deptInstCondition) {
                        return (
                                (user.getDeptId() != null && user.getDeptId().equals(userExtend.getDeptId())) ||
                                        (user.getInstId() != null && user.getInstId().equals(userExtend.getInstId()))
                        );
                    } else if (userRoleCondition) {
                        return finalUserRolecollect.contains(user.getUserId());
                    } else {
                        return true;
                    }
                })
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(filteredData)) {
            return new PageInfo<>();
        }
       //手动分页
        int startIndex = (pageForm.getPageNum() - 1) * pageForm.getPageSize();
        int endIndex = Math.min(startIndex + pageForm.getPageSize(), filteredData.size());
        List<SysUserDTO> sysUserDTOS = PageCopyListUtils.copyListProperties(filteredData, SysUserDTO.class);
        sysUserDTOS = sysUserDTOS.subList(startIndex, endIndex);
        PageInfo<SysUserDTO> pageInfo = new PageInfo<>();
        pageInfo.setList(sysUserDTOS);
        pageInfo.setPageNum(pageForm.getPageNum());
        pageInfo.setPageSize(pageForm.getPageSize());
        pageInfo.setTotal(filteredData.size());
        List<SysUserDTO> list = pageInfo.getList();
        if (CollectionUtils.isEmpty(filteredData)) {
            return new PageInfo<>(list);
        }
        //查询用户角色及名称列表
        getInstDeptName(list);
        //获取角色名称
        getRoleName(list);
        pageInfo.setList(list);
        return pageInfo;
    }

    /**
     * 获取角色列表
     */
    public List<SysRoleDTO> getRole(AccountToken token) {
        if (ObjectUtils.isEmpty(token.getInstId())) {
            return null;
        }
        Result<List<SysRoleDTO>> sysRoles = roleApi.getRoleByInstId(token.getInstId());
        return sysRoles.getData();
    }


    /**
     * 获取机构部门名称
     */
    private void getInstDeptName(List<SysUserDTO> list) {
        //去除重复数据
        List<Long> collect = list.stream().map(SysUserDTO::getInstId).distinct().collect(Collectors.toList());
        //查询机构信息
        Result<List<SysInstDTO>> inst = instApi.getInstsByInstIds(collect.toArray(new Long[collect.size()]));
        if (!CollectionUtils.isEmpty(inst.getData())) {
            Map<Long, List<SysInstDTO>> collectList = inst.getData().stream().collect(Collectors.groupingBy(SysInstDTO::getInstId));
            for (SysUserDTO ent : list) {
                List<SysInstDTO> sysInsts = collectList.get(ent.getInstId());
                if (!ObjectUtils.isEmpty(sysInsts)) {
                    SysInstDTO sysInst = sysInsts.get(0);
                    if (!ObjectUtils.isEmpty(sysInst.getNameLevel())) {
                        ent.setInstName(sysInst.getNameLevel());
                    }
                }
            }
        }
        List<Long> collectDept = list.stream().map(SysUserDTO::getDeptId).distinct().collect(Collectors.toList());
        Result<List<SysDeptDTO>> dept = deptApi.selectByIds(collectDept.toArray(new Long[collectDept.size()]));
        if (!CollectionUtils.isEmpty(dept.getData())) {
            Map<Long, List<SysDeptDTO>> collectDeptId = dept.getData().stream().collect(Collectors.groupingBy(SysDeptDTO::getDeptId));
            for (SysUserDTO ent : list) {
                List<SysDeptDTO> sysDepts = collectDeptId.get(ent.getDeptId());
                if (!CollectionUtils.isEmpty(sysDepts)) {
                    SysDeptDTO sysDpet = sysDepts.get(0);
                    if (!ObjectUtils.isEmpty(sysDpet.getNameLevel())) {
                        ent.setDeptName(sysDpet.getNameLevel());
                    }
                }
            }
        }
    }

    /**
     * 获取角色名称
     */
    private void getRoleName(List<SysUserDTO> list) {
        List<Long> collect = list.stream().map(SysUserDTO::getUserId).collect(Collectors.toList());
        for (int i = 0; i < collect.size(); i ++) {
            Result<List<SysRoleDTO>> roleByUserIds = roleApi.getRoleByUserIds(collect.toArray(new Long[0]));
            Map<Long, List<SysRoleDTO>> collect2 = roleByUserIds.getData().stream().collect(Collectors.groupingBy(SysRoleDTO::getUserId));
            for (SysUserDTO s:list) {
                List<SysRoleDTO> sysRoleDTOS = collect2.get(s.getUserId());
                s.setRoleNameCollect("");
                if (sysRoleDTOS!=null&&sysRoleDTOS.size()>0) {
                    StringBuilder outPut = new StringBuilder();
                    outPut.append(sysRoleDTOS.get(0).getName());
                    for (int j = 1; j < sysRoleDTOS.size(); j++) {
                        outPut.append(",");
                        outPut.append(sysRoleDTOS.get(j).getName());
                    }
                    s.setRoleNameCollect(outPut.toString());
                }
            }
        }
    }
}
