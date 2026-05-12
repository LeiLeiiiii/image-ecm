package com.sunyard.edm.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import com.baomidou.mybatisplus.core.batch.MybatisBatch;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import com.baomidou.lock.annotation.Lock4j;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.sunyard.edm.constant.DocConstants;
import com.sunyard.edm.dto.AnnonInfoDTO;
import com.sunyard.edm.dto.DocSysAnnounDTO;
import com.sunyard.edm.dto.DocSysAnnounUserDTO;
import com.sunyard.edm.mapper.DocSysAnnounMapper;
import com.sunyard.edm.mapper.DocSysAnnounUserMapper;
import com.sunyard.edm.mapper.DocSysTeamMapper;
import com.sunyard.edm.mapper.DocSysTeamUserMapper;
import com.sunyard.edm.po.DocSysAnnoun;
import com.sunyard.edm.po.DocSysAnnounUser;
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
import com.sunyard.module.system.api.UserApi;
import com.sunyard.module.system.api.dto.SysDeptDTO;
import com.sunyard.module.system.api.dto.SysInstDTO;
import com.sunyard.module.system.api.dto.SysUserDTO;

/**
 * @author raochangmei
 * @date 11.15
 * @Desc 系统管理-公告管理实现类
 */
@Service
public class SysNoticeService  {
    @Resource
    private SnowflakeUtils snowflakeUtil;
    @Resource
    private SqlSessionFactory sqlSessionFactory;
    @Resource
    private DocSysTeamMapper teamMapper;
    @Resource
    private DocSysTeamUserMapper docSysTeamUserMapper;
    @Resource
    private DocSysAnnounMapper announMapper;
    @Resource
    private DocSysAnnounUserMapper announUserMapper;
    @Resource
    private UserApi userApi;
    @Resource
    private InstApi instApi;
    @Resource
    private DeptApi deptApi;


    /**
     * 查询公告列表
     */
    public PageInfo<DocSysAnnoun> searchAnnoun(DocSysAnnounDTO docSysAnnounVo, PageForm pageForm, AccountToken token) {

        //查询列表
        List<Long> teams = getTeamListByUser(token);
        //查询当前登录用户有权限看到的公告列表
        List<DocSysAnnounDTO> docSysAnnounDTOS = announMapper.selectListAnnounExtend(token.getInstId(),DocConstants.INST,
                token.getDeptId(),DocConstants.DEPT,token.getId(),DocConstants.USER,teams,DocConstants.TEAM);
        //此人没权限 什么都看不到
        if (CollectionUtils.isEmpty(docSysAnnounDTOS)) {
            return new PageInfo<>();
        }
        List<Long> b = new ArrayList<>();
        for (DocSysAnnounDTO s : docSysAnnounDTOS) {
            if (!b.contains(s.getAnanounId())) {
                b.add(s.getAnanounId());
            }
        }
        PageHelper.startPage(pageForm.getPageNum(), pageForm.getPageSize());
        List<DocSysAnnoun> ananounList = announMapper.selectList(
                new LambdaQueryWrapper<DocSysAnnoun>()
                        .in(DocSysAnnoun::getAnanounId, b)
                        .like(!ObjectUtils.isEmpty(docSysAnnounVo.getAnanounTitle()), DocSysAnnoun::getAnanounTitle, docSysAnnounVo.getAnanounTitle())
                        .orderByDesc(DocSysAnnoun::getCreateTime)
        );
        //主键不用再判空
        return new PageInfo<>(ananounList);
    }


    private List<Long> getTeamListByUser(AccountToken token) {
        List<DocSysTeamUser> users = docSysTeamUserMapper.selectList(new LambdaQueryWrapper<DocSysTeamUser>().eq(DocSysTeamUser::getUserId, token.getId()));
        return users.stream().map(DocSysTeamUser::getTeamId).collect(Collectors.toList());
    }

    /**
     * 新增
     */
    @Transactional(rollbackFor = Exception.class)
    @Lock4j(keys = "#status")
    public void addAnnoun(DocSysAnnounDTO docSysAnnounVo) {
        AssertUtils.isNull(docSysAnnounVo.getAnanounTitle(), "参数错误");
        AssertUtils.isNull(docSysAnnounVo.getStatus(), "参数错误");
        AssertUtils.isNull(docSysAnnounVo.getUserTeamDeptListExtends(), "参数错误");
        DocSysAnnoun docSysAnnoun = new DocSysAnnoun();
        BeanUtils.copyProperties(docSysAnnounVo, docSysAnnoun);
        long l = snowflakeUtil.nextId();
        docSysAnnoun.setAnanounId(l);
        docSysAnnoun.setUpdateTime(new Date());
        if (docSysAnnounVo.getStatus().equals(DocConstants.DOC_ANNOUN_OPEN)) {
            docSysAnnoun.setReleaseTime(new Date());
        }
        announMapper.insert(docSysAnnoun);
        addAnnounUser(docSysAnnounVo, l);
    }

    /**
     * 新增关联关系
     */
    private void addAnnounUser(DocSysAnnounDTO docSysAnnounVo, long l) {
        if (!ObjectUtils.isEmpty(docSysAnnounVo.getUserTeamDeptListExtends())) {
            List<DocSysAnnounUser> announUsers = new ArrayList<>();
            for (DocSysAnnounUserDTO s : docSysAnnounVo.getUserTeamDeptListExtends()) {
                DocSysAnnounUser announUser = new DocSysAnnounUser();
                announUser.setAnanounId(l);
                announUser.setRelId(s.getRelId());
                announUser.setType(s.getType());
                announUsers.add(announUser);
            }
            MybatisBatch<DocSysAnnounUser> docBatchs = new MybatisBatch<>(sqlSessionFactory, announUsers);
            MybatisBatch.Method<DocSysAnnounUser> docMethod = new MybatisBatch.Method<>(DocSysAnnounUserMapper.class);
            docBatchs.execute(docMethod.insert());
        }
    }

    /**
     * 修改
     */
    @Lock4j(keys = "#status")
    @Transactional(rollbackFor = Exception.class)
    public void updateAnnoun(DocSysAnnounDTO docSysAnnounVo) {
        AssertUtils.isNull(docSysAnnounVo.getAnanounId(), "参数错误");
        AssertUtils.isNull(docSysAnnounVo.getAnanounTitle(), "参数错误");
        AssertUtils.isNull(docSysAnnounVo.getStatus(), "参数错误");
        AssertUtils.isNull(docSysAnnounVo.getUserTeamDeptListExtends(), "参数错误");
        DocSysAnnoun docSysAnnoun = announMapper.selectById(docSysAnnounVo.getAnanounId());
        announMapper.update(null,
                new LambdaUpdateWrapper<DocSysAnnoun>()
                        .set(DocSysAnnoun::getAnanounTitle, docSysAnnounVo.getAnanounTitle())
                        .set(!ObjectUtils.isEmpty(docSysAnnounVo.getAnanounContent()),DocSysAnnoun::getAnanounContent, docSysAnnounVo.getAnanounContent())
                        .set(DocSysAnnoun::getStatus, docSysAnnounVo.getStatus())
                        .set(docSysAnnoun.getStatus().equals(DocConstants.DOC_ANNOUN_CLOSE) && docSysAnnounVo.getStatus().equals(DocConstants.DOC_ANNOUN_OPEN),DocSysAnnoun::getReleaseTime, new Date())
                        .eq(DocSysAnnoun::getAnanounId, docSysAnnounVo.getAnanounId())
        );
        announUserMapper.delete(
                new LambdaQueryWrapper<DocSysAnnounUser>()
                        .eq(DocSysAnnounUser::getAnanounId, docSysAnnounVo.getAnanounId())
        );
        addAnnounUser(docSysAnnounVo, docSysAnnounVo.getAnanounId());
    }

    /**
     * 删除
     */
    @Transactional(rollbackFor = Exception.class)
    public void delAnnoun(Long ananounId) {
        AssertUtils.isNull(ananounId, "参数错误");
        //删除公告用户关联信息
        announUserMapper.delete(
                new LambdaQueryWrapper<DocSysAnnounUser>()
                        .eq(DocSysAnnounUser::getAnanounId, ananounId)
        );
        //删除公告
        announMapper.delete(
                new LambdaQueryWrapper<DocSysAnnoun>()
                        .eq(DocSysAnnoun::getAnanounId, ananounId)
        );
    }

    /**
     * 查询详情
     */
    public AnnonInfoDTO getInfo(Long ananounId, PageForm pageForm) {
        AssertUtils.isNull(ananounId, "参数错误");
        DocSysAnnoun docSysAnnoun = announMapper.selectById(ananounId);
        PageHelper.startPage(pageForm.getPageNum(), pageForm.getPageSize());
        AnnonInfoDTO annonInfoDTO = new AnnonInfoDTO();
        List<DocSysAnnounUser> ananounUserList = announUserMapper.selectList(
                new LambdaQueryWrapper<DocSysAnnounUser>()
                        .eq(DocSysAnnounUser::getAnanounId, ananounId)
        );
        if (!CollectionUtils.isEmpty(ananounUserList)) {
            PageInfo<DocSysAnnounUserDTO> pageInfo =
                    PageCopyListUtils.getPageInfo(new PageInfo<>(ananounUserList), DocSysAnnounUserDTO.class);
            //获得列表
            List<DocSysAnnounUserDTO> list = pageInfo.getList();
            List<DocSysAnnounUserDTO> docSysAnnounUserDTOS = getDocSysAnnounUserExtends(list);
            pageInfo.setList(docSysAnnounUserDTOS);
            annonInfoDTO.setDocSysAnnounUserExtendPageInfo(pageInfo);
            annonInfoDTO.setDocSysAnnoun(docSysAnnoun);
        } else {
            annonInfoDTO.setDocSysAnnoun(docSysAnnoun);
        }
        return annonInfoDTO;

    }


    /**
     * 修改-展示原有公告关联的成员
     */

    public List<DocSysAnnounUserDTO> selectAnnounUser(Long announId) {
        AssertUtils.isNull(announId, "参数错误");
        List<DocSysAnnounUser> ananounUserList = announUserMapper.selectList(
                new LambdaQueryWrapper<DocSysAnnounUser>()
                        .eq(DocSysAnnounUser::getAnanounId, announId)
        );
        if (CollectionUtils.isEmpty(ananounUserList)) {
            return null;
        }
        List<DocSysAnnounUserDTO> docSysAnnounUserDTOS = PageCopyListUtils.copyListProperties(ananounUserList, DocSysAnnounUserDTO.class);
        return getDocSysAnnounUserExtends(docSysAnnounUserDTOS);
    }

    /**
     * 获取公告详情
     */
    public AnnonInfoDTO getTypeNum(Long announId) {
        AssertUtils.isNull(announId, "参数错误");
        List<DocSysAnnounUser> ananounUserList = announUserMapper.selectList(
                new LambdaQueryWrapper<DocSysAnnounUser>()
                        .eq(DocSysAnnounUser::getAnanounId, announId)
        );
        AnnonInfoDTO annonInfoDTO = new AnnonInfoDTO();
        typeNum(annonInfoDTO, ananounUserList);
        return annonInfoDTO;
    }

    /**
     * 计算权限 机构 部门 团队 用户 的数量
     */
    private void typeNum(AnnonInfoDTO annonInfoDTO, List<DocSysAnnounUser> list) {
        Map<Integer, Long> collect = list.stream().collect(Collectors.groupingBy(DocSysAnnounUser::getType, Collectors.counting()));
        if (!ObjectUtils.isEmpty(collect.get(DocConstants.INST))) {
            annonInfoDTO.setInstNum(collect.get(DocConstants.INST).intValue());
        } else {
            annonInfoDTO.setInstNum(DocConstants.ZERO);
        }
        if (!ObjectUtils.isEmpty(collect.get(DocConstants.DEPT))) {
            annonInfoDTO.setDeptNum(collect.get(DocConstants.DEPT).intValue());
        } else {
            annonInfoDTO.setDeptNum(DocConstants.ZERO);
        }
        if (!ObjectUtils.isEmpty(collect.get(DocConstants.TEAM))) {
            annonInfoDTO.setTeamNum(collect.get(DocConstants.TEAM).intValue());
        } else {
            annonInfoDTO.setTeamNum(DocConstants.ZERO);
        }
        if (!ObjectUtils.isEmpty(collect.get(DocConstants.USER))) {
            annonInfoDTO.setUserNum(collect.get(DocConstants.USER).intValue());
        } else {
            annonInfoDTO.setUserNum(DocConstants.ZERO);
        }
    }

    /**
     * 获取权限名称
     */
    private List<DocSysAnnounUserDTO> getDocSysAnnounUserExtends(List<DocSysAnnounUserDTO> docSysAnnounUserDTOS) {
        Map<Integer, List<DocSysAnnounUserDTO>> typeCollect = docSysAnnounUserDTOS.stream().collect(Collectors.groupingBy(DocSysAnnounUserDTO::getType));
        List<DocSysAnnounUserDTO> docSysHouseUserInst = typeCollect.get(DocConstants.ONE);
        List<DocSysAnnounUserDTO> docSysHouseUserDept = typeCollect.get(DocConstants.TWO);
        List<DocSysAnnounUserDTO> docSysHouseUserPeople = typeCollect.get(DocConstants.ZERO);
        List<DocSysAnnounUserDTO> docSysHouseUserTeam = typeCollect.get(DocConstants.THREE);
        List<Long> inst = new ArrayList();
        if (!CollectionUtils.isEmpty(docSysHouseUserInst)) {
            for (DocSysAnnounUserDTO s : docSysHouseUserInst) {
                inst.add(s.getRelId());
            }
        }
        List<Long> dept = new ArrayList();
        if (!CollectionUtils.isEmpty(docSysHouseUserDept)) {
            for (DocSysAnnounUserDTO s : docSysHouseUserDept) {
                dept.add(s.getRelId());
            }
        }
        List<Long> user = new ArrayList();
        if (!CollectionUtils.isEmpty(docSysHouseUserPeople)) {
            for (DocSysAnnounUserDTO s : docSysHouseUserPeople) {
                user.add(s.getRelId());
            }
        }
        List<Long> team = new ArrayList();
        if (!CollectionUtils.isEmpty(docSysHouseUserTeam)) {
            for (DocSysAnnounUserDTO s : docSysHouseUserTeam) {
                team.add(s.getRelId());
            }
        }
        Map<Long, List<SysInstDTO>> collectInst = new HashMap<>();
        if (!CollectionUtils.isEmpty(inst)) {
            Result<List<SysInstDTO>> instList = instApi.getInstsByInstIds(inst.toArray(new Long[inst.size()]));
            collectInst = instList.getData().stream().collect(Collectors.groupingBy(SysInstDTO::getInstId));
        }
        Map<Long, List<SysDeptDTO>> collectDept = new HashMap<>();
        if (!CollectionUtils.isEmpty(dept)) {
            Result<List<SysDeptDTO>> deptList = deptApi.selectByIds(dept.toArray(new Long[dept.size()]));
            collectDept = deptList.getData().stream().collect(Collectors.groupingBy(SysDeptDTO::getDeptId));
        }
        Map<Long, List<SysUserDTO>> collectUser = new HashMap<>();
        if (!CollectionUtils.isEmpty(user)) {
            Result<List<SysUserDTO>> userList = userApi.getUserListByUserIds(user.toArray(new Long[user.size()]));
            collectUser = userList.getData().stream().collect(Collectors.groupingBy(SysUserDTO::getUserId));
        }
        List<DocSysTeam> teamList = teamMapper.selectList(new LambdaQueryWrapper<DocSysTeam>().in(!CollectionUtils.isEmpty(team),DocSysTeam::getTeamId, team));
        Map<Long, List<DocSysTeam>> collectTeam = teamList.stream().collect(Collectors.groupingBy(DocSysTeam::getTeamId));
        organFor(docSysAnnounUserDTOS, collectInst, collectDept, collectUser, collectTeam);
        return docSysAnnounUserDTOS;
    }

    /**
     * 将获得的对应组织名称放入扩展
     */
    private void organFor(List<DocSysAnnounUserDTO> docSysAnnounUserDTOS, Map<Long, List<SysInstDTO>> collectInst, Map<Long, List<SysDeptDTO>> collectDept, Map<Long, List<SysUserDTO>> collectUser, Map<Long, List<DocSysTeam>> collectTeam) {
        //循环存关联的组织名称
        for (DocSysAnnounUserDTO s : docSysAnnounUserDTOS) {
            if (!ObjectUtils.isEmpty(s.getType())) {
                //根据type获取关联类型，对应表根据关联id获取对应名称
                if (s.getType().equals(DocConstants.USER) && !CollectionUtils.isEmpty(collectUser)) {
                    List<SysUserDTO> sysUsers = collectUser.get(s.getRelId());
                    if (!ObjectUtils.isEmpty(sysUsers) && !ObjectUtils.isEmpty(sysUsers.get(DocConstants.ZERO).getName())) {
                        s.setRelIdStr(sysUsers.get(DocConstants.ZERO).getName());
                    }
                }
                if (s.getType().equals(DocConstants.INST) && !CollectionUtils.isEmpty(collectInst)) {
                    List<SysInstDTO> sysInsts = collectInst.get(s.getRelId());
                    if (!ObjectUtils.isEmpty(sysInsts) && !ObjectUtils.isEmpty(sysInsts.get(DocConstants.ZERO).getName())) {
                        s.setRelIdStr(sysInsts.get(DocConstants.ZERO).getName());
                    }
                }
                if (s.getType().equals(DocConstants.DEPT) && !CollectionUtils.isEmpty(collectDept)) {
                    List<SysDeptDTO> sysDepts = collectDept.get(s.getRelId());
                    if (!ObjectUtils.isEmpty(sysDepts) && !ObjectUtils.isEmpty(sysDepts.get(DocConstants.ZERO).getName())) {
                        s.setRelIdStr(sysDepts.get(DocConstants.ZERO).getName());
                    }
                }
                if (s.getType().equals(DocConstants.TEAM) && !CollectionUtils.isEmpty(collectTeam)) {
                    List<DocSysTeam> docSysTeams = collectTeam.get(s.getRelId());
                    if (!ObjectUtils.isEmpty(docSysTeams) && !ObjectUtils.isEmpty(docSysTeams.get(DocConstants.ZERO).getTeamName())) {
                        s.setRelIdStr(docSysTeams.get(DocConstants.ZERO).getTeamName());
                    }
                }
            }
        }
    }
}

