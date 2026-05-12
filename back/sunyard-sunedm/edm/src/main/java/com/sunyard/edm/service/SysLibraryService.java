package com.sunyard.edm.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
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
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.sunyard.edm.constant.DocConstants;
import com.sunyard.edm.dto.DocSysHouseDTO;
import com.sunyard.edm.dto.DocSysHouseUserDTO;
import com.sunyard.edm.dto.HouseInfoDTO;
import com.sunyard.edm.mapper.DocBsDocumentMapper;
import com.sunyard.edm.mapper.DocSysHouseMapper;
import com.sunyard.edm.mapper.DocSysHouseUserMapper;
import com.sunyard.edm.mapper.DocSysTeamMapper;
import com.sunyard.edm.po.DocBsDocument;
import com.sunyard.edm.po.DocSysHouse;
import com.sunyard.edm.po.DocSysHouseUser;
import com.sunyard.edm.po.DocSysTeam;
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
 * @author wt
 * @DESC 系统管理-文档库管理实现类
 * @date 11.16
 */
@Service
public class SysLibraryService {
    @Resource
    private SnowflakeUtils snowflakeUtil;
    @Resource
    private SqlSessionFactory sqlSessionFactory;
    @Resource
    private DocSysHouseMapper docSysHouseMapper;
    @Resource
    private DocSysHouseUserMapper docSysHouseUserMapper;
    @Resource
    private DocBsDocumentMapper docBsDocumentMapper;
    @Resource
    private DocSysTeamMapper teamMapper;
    @Resource
    private UserApi userApi;
    @Resource
    private InstApi instApi;
    @Resource
    private DeptApi deptApi;
    @Resource
    private CenterCommonService docCommonService;
    /**
     * 获取当前用户可查看的文档库列表
     */
    public PageInfo<DocSysHouseDTO> queryHouseList(DocSysHouseDTO houseVo, PageForm pageForm, AccountToken token) {
        PageHelper.startPage(pageForm.getPageNum(), pageForm.getPageSize());
        List<DocSysHouse> houseList = docSysHouseMapper.selectList(new LambdaQueryWrapper<DocSysHouse>()
                .like(!StringUtils.isEmpty(houseVo.getHouseName()), DocSysHouse::getHouseName, houseVo.getHouseName())
                .orderByAsc(DocSysHouse::getHouseSeq)
                .orderByDesc(DocSysHouse::getCreateTime)
        );
        PageInfo<DocSysHouseDTO> pageInfo =
                PageCopyListUtils.getPageInfo(new PageInfo<>(houseList), DocSysHouseDTO.class);
        List<DocSysHouseDTO> list = pageInfo.getList();
        if (CollectionUtils.isEmpty(houseList)) {
            return new PageInfo<DocSysHouseDTO>(list);
        }
        //获得列表
        List<Long> houseIdList = list.stream().map(DocSysHouseDTO::getHouseId).collect(Collectors.toList());
        List<DocSysHouseDTO> docSysHouseDTOS = docCommonService.queryHouseList(token);
        List<DocSysHouseDTO> permissMax = getPermissMax(docSysHouseDTOS);
        Map<Long, List<DocSysHouseDTO>> collect = permissMax.stream().collect(Collectors.groupingBy(DocSysHouseDTO::getHouseId));
        list.forEach(s -> {
            List<DocSysHouseDTO> docSysHouseMsg = collect.get(s.getHouseId());
            if (!CollectionUtils.isEmpty(docSysHouseMsg)) {
                s.setPermissType(docSysHouseMsg.get(0).getPermissType());
            }
        });
        pageInfo.setList(list);
        return pageInfo;
    }

    /**
     * 多权限数据过滤
     */
    public List<DocSysHouseDTO> getPermissMax(List<DocSysHouseDTO> ret) {
        //过滤
        List<DocSysHouseDTO> re = new ArrayList<>();
        if (!CollectionUtils.isEmpty(ret)) {
            //根据权限级别倒叙，并且根据houseid分组，获取分组后的第一条数据，即，权限最大的数据
            Map<Long, List<DocSysHouseDTO>> collect = ret.stream()
                    .sorted(Comparator.comparing(DocSysHouseDTO::getPermissType, Comparator.nullsLast(Integer::compareTo)).reversed())
                    .collect(Collectors.groupingBy(DocSysHouse::getHouseId));
            collect.keySet().forEach(s -> re.add(collect.get(s).get(0)));
        }
        return re;
    }

    /**
     * 新增
     */
    @Transactional(rollbackFor = Exception.class)
    @Lock4j(keys = "#houseVo.houseSeq")
    public void addHouse(DocSysHouseDTO houseVo) {
        AssertUtils.isNull(houseVo.getHouseName(), "参数错误");
        AssertUtils.isNull(houseVo.getUserTeamDeptListExtends(), "参数错误");
        DocSysHouse docSysHouse = new DocSysHouse();
        BeanUtils.copyProperties(houseVo, docSysHouse);
        //文档库名称不允许重复
        houseNameHave(houseVo);
        long l = snowflakeUtil.nextId();
        docSysHouse.setHouseId(l);
        docSysHouse.setUpdateTime(new Date());
        docSysHouseMapper.insert(docSysHouse);
        addHouseUser(houseVo, l);
    }

    /**
     * 文档库名称判断
     */
    private void houseNameHave(DocSysHouseDTO houseVo) {
        Long houseNameNum = docSysHouseMapper.selectCount(new LambdaQueryWrapper<DocSysHouse>()
                .eq(DocSysHouse::getHouseName, houseVo.getHouseName()));
        Assert.isTrue(houseNameNum.equals(DocConstants.DOC_HOUSE_EQNAME), "文档库名称不能重名");
    }

    /**
     * 新增文档库用户表信息
     *
     */
    private void addHouseUser(DocSysHouseDTO houseVo, long l) {
        if (!ObjectUtils.isEmpty(houseVo.getUserTeamDeptListExtends())) {
            //获取权限集合
            List<Integer> permissTypeCollect = houseVo.getUserTeamDeptListExtends().stream().map(DocSysHouseUserDTO::getPermissType).collect(Collectors.toList());
            AssertUtils.isTrue(!permissTypeCollect.contains(DocConstants.DOC_COMMON_PERMISSION_TYPE_MANAGE), "至少有一个管理者");
            List<DocSysHouseUser> houseUsers = new ArrayList<>();
            for (DocSysHouseUserDTO s : houseVo.getUserTeamDeptListExtends()) {
                DocSysHouseUser houseUser = new DocSysHouseUser();
                houseUser.setHouseId(l);
                houseUser.setRelId(s.getRelId());
                houseUser.setType(s.getType());
                houseUser.setPermissType(s.getPermissType());
                houseUsers.add(houseUser);
            }
            MybatisBatch<DocSysHouseUser> docBatchs = new MybatisBatch<>(sqlSessionFactory, houseUsers);
            MybatisBatch.Method<DocSysHouseUser> docMethod = new MybatisBatch.Method<>(DocSysHouseUserMapper.class);
            docBatchs.execute(docMethod.insert());
        }
    }

    /**
     * 查询详情
     */
    public HouseInfoDTO getInfo(AccountToken token, Long houseId, PageForm pageForm) {
        AssertUtils.isNull(houseId, "参数错误");
        DocSysHouse docSysHouse = docSysHouseMapper.selectById(houseId);

        PageHelper.startPage(pageForm.getPageNum(), pageForm.getPageSize());
        HouseInfoDTO houseInfoDTO = new HouseInfoDTO();
        List<DocSysHouseUser> houseUserList = docSysHouseUserMapper.selectList(
                new LambdaQueryWrapper<DocSysHouseUser>()
                        .eq(DocSysHouseUser::getHouseId, houseId)
        );
        if (!CollectionUtils.isEmpty(houseUserList)) {
            PageInfo<DocSysHouseUserDTO> pageInfo =
                    PageCopyListUtils.getPageInfo(new PageInfo<>(houseUserList), DocSysHouseUserDTO.class);

            //获得列表
            List<DocSysHouseUserDTO> list = pageInfo.getList();
            List<DocSysHouseUserDTO> docSysHouseUserDTOS = getDocSysHouseUserExtends(list);
            pageInfo.setList(docSysHouseUserDTOS);
            houseInfoDTO.setDocSysHouseUserExtendPageInfo(pageInfo);
            houseInfoDTO.setDocSysHouse(docSysHouse);
        } else {
            houseInfoDTO.setDocSysHouse(docSysHouse);
        }

        //设置权限
        List<DocSysHouseDTO> docSysHouseDTOS = docCommonService.queryHouseList(token);
        if (!CollectionUtils.isEmpty(docSysHouseDTOS)) {
            List<DocSysHouseDTO> collect = docSysHouseDTOS.stream().filter(s -> s.getHouseId().equals(houseId)).collect(Collectors.toList());
            if (!CollectionUtils.isEmpty(collect)) {
                DocSysHouseDTO docSysHouseDTO = collect.get(0);
                houseInfoDTO.setPermissType(docSysHouseDTO.getPermissType());
            }
        }
        return houseInfoDTO;
    }

    /**
     * 计算权限 机构 部门 团队 用户 的数量
     *
     */
    public HouseInfoDTO getTypeNum(Long houseId) {
        AssertUtils.isNull(houseId, "参数错误");
        List<DocSysHouseUser> houseUserList = docSysHouseUserMapper.selectList(
                new LambdaQueryWrapper<DocSysHouseUser>()
                        .eq(DocSysHouseUser::getHouseId, houseId)
        );
        HouseInfoDTO houseInfoDTO = new HouseInfoDTO();
        typeNum(houseInfoDTO, houseUserList);
        return houseInfoDTO;
    }

    /**
     * 计算权限 机构 部门 团队 用户 的数量
     */
    private void typeNum(HouseInfoDTO houseInfoDTO, List<DocSysHouseUser> list) {
        Map<Integer, Long> collect = list.stream().collect(Collectors.groupingBy(DocSysHouseUser::getType, Collectors.counting()));
        if (!CollectionUtils.isEmpty(collect)) {
            if (!ObjectUtils.isEmpty(collect.get(DocConstants.INST))) {
                houseInfoDTO.setInstNum(collect.get(DocConstants.INST).intValue());
            } else {
                houseInfoDTO.setInstNum(DocConstants.ZERO);
            }
            if (!ObjectUtils.isEmpty(collect.get(DocConstants.DEPT))) {
                houseInfoDTO.setDeptNum(collect.get(DocConstants.DEPT).intValue());
            } else {
                houseInfoDTO.setDeptNum(DocConstants.ZERO);
            }
            if (!ObjectUtils.isEmpty(collect.get(DocConstants.TEAM))) {
                houseInfoDTO.setTeamNum(collect.get(DocConstants.TEAM).intValue());
            } else {
                houseInfoDTO.setTeamNum(DocConstants.ZERO);
            }
            if (!ObjectUtils.isEmpty(collect.get(DocConstants.USER))) {
                houseInfoDTO.setUserNum(collect.get(DocConstants.USER).intValue());
            } else {
                houseInfoDTO.setUserNum(DocConstants.ZERO);
            }
        }
    }

    /**
     * 修改文档库信息
     */
    @Transactional(rollbackFor = Exception.class)
    @Lock4j(keys = "#houseVo.houseSeq")
    public void updateHouse(DocSysHouseDTO houseVo, AccountToken token) {
        AssertUtils.isNull(houseVo.getHouseId(), "参数错误");
        AssertUtils.isNull(houseVo.getHouseName(), "参数错误");
        AssertUtils.isNull(houseVo.getUserTeamDeptListExtends(), "参数错误");
        //判断是否有编辑权限
        docCommonService.isEditPermissHouse(token, houseVo.getHouseId());
        Long houseNameNum = docSysHouseMapper.selectCount(new LambdaQueryWrapper<DocSysHouse>()
                .eq(DocSysHouse::getHouseName, houseVo.getHouseName())
                .ne(!ObjectUtils.isEmpty(houseVo.getHouseId()), DocSysHouse::getHouseId, houseVo.getHouseId())
        );
        Assert.isTrue(houseNameNum.equals(DocConstants.DOC_HOUSE_EQNAME), "文档库名称不能重名");
        docSysHouseMapper.update(null,
                new LambdaUpdateWrapper<DocSysHouse>()
                        .set(DocSysHouse::getHouseName, houseVo.getHouseName())
                        .set(!ObjectUtils.isEmpty(houseVo.getHouseSeq()), DocSysHouse::getHouseSeq, houseVo.getHouseSeq())
                        .eq(DocSysHouse::getHouseId, houseVo.getHouseId())
        );
        docSysHouseUserMapper.delete(
                new LambdaQueryWrapper<DocSysHouseUser>()
                        .eq(DocSysHouseUser::getHouseId, houseVo.getHouseId())
        );
        addHouseUser(houseVo, houseVo.getHouseId());
    }

    /**
     * 删除文档库
     *
     */
    @Transactional(rollbackFor = Exception.class)
    public void delHouse(Long[] houseIds, AccountToken token) {
        AssertUtils.isNull(houseIds, "参数错误");
        //当前登录用户可查看的文档库列表
        List<DocSysHouseDTO> docSysHouseDTOS = docCommonService.queryHouseList(token);
        if (!CollectionUtils.isEmpty(docSysHouseDTOS)) {
            List<DocSysHouseDTO> permissMax = getPermissMax(docSysHouseDTOS);
            Map<Long, List<DocSysHouseDTO>> collect = permissMax.stream().collect(Collectors.groupingBy(DocSysHouseDTO::getHouseId));
            for (Long s : houseIds) {
                List<DocSysHouseDTO> docSysHouseMsg = collect.get(s);
                //不在可查看的范围内
                AssertUtils.isNull(docSysHouseMsg, "暂无权限");
                AssertUtils.isTrue(!docSysHouseMsg.get(0).getPermissType().equals(DocConstants.DOC_COMMON_PERMISSION_TYPE_MANAGE), "暂无权限删除");
            }
        }
        //查询有关联库的文档或文件夹
        List<DocBsDocument> docBsDocuments = docBsDocumentMapper.selectList(
                new LambdaQueryWrapper<DocBsDocument>()
                        .in(DocBsDocument::getHouseId, houseIds)
        );
        AssertUtils.notNull(docBsDocuments, "当前库中存在文件夹，不允许删除！");
        //删库
        docSysHouseUserMapper.delete(
                new LambdaQueryWrapper<DocSysHouseUser>()
                        .in(DocSysHouseUser::getHouseId, houseIds)
        );
        docSysHouseMapper.delete(
                new LambdaQueryWrapper<DocSysHouse>()
                        .in(DocSysHouse::getHouseId, houseIds)
        );
    }


    /**
     * 获取文件夹的顺序号
     */
    public Integer getFolderSeq() {
        List<DocSysHouse> houseList = docSysHouseMapper.selectList(new LambdaQueryWrapper<DocSysHouse>()
                .orderByDesc(DocSysHouse::getHouseSeq)
        );
        //如果为空 返回1 不为空返回最大顺序号 个数加1
        if (CollectionUtils.isEmpty(houseList)) {
            return DocConstants.ONE.intValue();
        } else {
            return houseList.size() + 1;
        }
    }

    /**
     * 展示原有文档库关联列表
     */
    public List<DocSysHouseUserDTO> selectHouseUser(Long houseId) {
        AssertUtils.isNull(houseId, "参数错误");
        //查询文档库用户关联表
        List<DocSysHouseUser> houseUserList = docSysHouseUserMapper.selectList(
                new LambdaQueryWrapper<DocSysHouseUser>()
                        .eq(DocSysHouseUser::getHouseId, houseId)
        );
        if (CollectionUtils.isEmpty(houseUserList)) {
            return null;
        }
        List<DocSysHouseUserDTO> docSysHouseUserDTOS = PageCopyListUtils.copyListProperties(houseUserList, DocSysHouseUserDTO.class);
        return getDocSysHouseUserExtends(docSysHouseUserDTOS);
    }


    /**
     * 获取权限名称
     *
     */
    private List<DocSysHouseUserDTO> getDocSysHouseUserExtends(List<DocSysHouseUserDTO> docSysHouseUserDTOS) {
        Map<Integer, List<DocSysHouseUserDTO>> typeCollect = docSysHouseUserDTOS.stream().collect(Collectors.groupingBy(DocSysHouseUserDTO::getType));
        List<DocSysHouseUserDTO> docSysHouseUserInst = typeCollect.get(DocConstants.ONE);
        List<DocSysHouseUserDTO> docSysHouseUserDept = typeCollect.get(DocConstants.TWO);
        List<DocSysHouseUserDTO> docSysHouseUserPeople = typeCollect.get(DocConstants.ZERO);
        List<DocSysHouseUserDTO> docSysHouseUserTeam = typeCollect.get(DocConstants.THREE);
        List<Long> inst = new ArrayList();
        if (!CollectionUtils.isEmpty(docSysHouseUserInst)) {
            for (DocSysHouseUserDTO s : docSysHouseUserInst) {
                inst.add(s.getRelId());
            }
        }
        List<Long> dept = new ArrayList();
        if (!CollectionUtils.isEmpty(docSysHouseUserDept)) {
            for (DocSysHouseUserDTO s : docSysHouseUserDept) {
                dept.add(s.getRelId());
            }
        }
        List<Long> user = new ArrayList();
        if (!CollectionUtils.isEmpty(docSysHouseUserPeople)) {
            for (DocSysHouseUserDTO s : docSysHouseUserPeople) {
                user.add(s.getRelId());
            }
        }
        List<Long> team = new ArrayList();
        if (!CollectionUtils.isEmpty(docSysHouseUserTeam)) {
            for (DocSysHouseUserDTO s : docSysHouseUserTeam) {
                team.add(s.getRelId());
            }
        }
        Map<Long, List<SysInstDTO>> collectInst = new HashMap<>();
        if(!CollectionUtils.isEmpty(inst)){
            Result<List<SysInstDTO>> instList = instApi.getInstsByInstIds(inst.toArray(new Long[inst.size()]));
            collectInst = instList.getData().stream().collect(Collectors.groupingBy(SysInstDTO::getInstId));
        }
        Map<Long, List<SysDeptDTO>> collectDept = new HashMap<>();
        if(!CollectionUtils.isEmpty(dept)){
            Result<List<SysDeptDTO>> deptList = deptApi.selectByIds(dept.toArray(new Long[dept.size()]));
            collectDept = deptList.getData().stream().collect(Collectors.groupingBy(SysDeptDTO::getDeptId));
        }
        Map<Long, List<SysUserDTO>> collectUser = new HashMap<>();
        if(!CollectionUtils.isEmpty(user)){
            Result<List<SysUserDTO>> userList = userApi.getUserListByUserIds(user.toArray(new Long[user.size()]));
            collectUser = userList.getData().stream().collect(Collectors.groupingBy(SysUserDTO::getUserId));
        }
        List<DocSysTeam> teamList = teamMapper.selectList(
                new LambdaQueryWrapper<DocSysTeam>()
                        .in(!CollectionUtils.isEmpty(team), DocSysTeam::getTeamId, team)
        );
        Map<Long, List<DocSysTeam>> collectTeam = teamList.stream().collect(Collectors.groupingBy(DocSysTeam::getTeamId));
        organFor(docSysHouseUserDTOS, collectInst, collectDept, collectUser, collectTeam);
        return docSysHouseUserDTOS;
    }

    private void organFor(List<DocSysHouseUserDTO> docSysHouseUserDTOS, Map<Long, List<SysInstDTO>> collectInst, Map<Long, List<SysDeptDTO>> collectDept, Map<Long, List<SysUserDTO>> collectUser, Map<Long, List<DocSysTeam>> collectTeam) {
        //循环存关联的组织名称
        for (DocSysHouseUserDTO s : docSysHouseUserDTOS) {
            if (!ObjectUtils.isEmpty(s.getType())) {
                //根据type获取关联类型，对应表根据关联id获取对应名称
                if (s.getType().equals(DocConstants.USER)) {
                    List<SysUserDTO> sysUsers = collectUser.get(s.getRelId());
                    if (!ObjectUtils.isEmpty(sysUsers) && !ObjectUtils.isEmpty(sysUsers.get(DocConstants.ZERO).getName())) {
                        s.setRelIdStr(sysUsers.get(DocConstants.ZERO).getName());
                    }
                }
                if (s.getType().equals(DocConstants.INST)) {
                    List<SysInstDTO> sysInsts = collectInst.get(s.getRelId());
                    if (!ObjectUtils.isEmpty(sysInsts) && !ObjectUtils.isEmpty(sysInsts.get(DocConstants.ZERO).getName())) {
                        s.setRelIdStr(sysInsts.get(DocConstants.ZERO).getName());
                    }
                }
                if (s.getType().equals(DocConstants.DEPT)) {
                    List<SysDeptDTO> sysDepts = collectDept.get(s.getRelId());
                    if (!ObjectUtils.isEmpty(sysDepts) && !ObjectUtils.isEmpty(sysDepts.get(DocConstants.ZERO).getName())) {
                        s.setRelIdStr(sysDepts.get(DocConstants.ZERO).getName());
                    }
                }
                if (s.getType().equals(DocConstants.TEAM)) {
                    List<DocSysTeam> docSysTeams = collectTeam.get(s.getRelId());
                    if (!ObjectUtils.isEmpty(docSysTeams) && !ObjectUtils.isEmpty(docSysTeams.get(DocConstants.ZERO).getTeamName())) {
                        s.setRelIdStr(docSysTeams.get(DocConstants.ZERO).getTeamName());
                    }
                }
            }
        }
    }
}

