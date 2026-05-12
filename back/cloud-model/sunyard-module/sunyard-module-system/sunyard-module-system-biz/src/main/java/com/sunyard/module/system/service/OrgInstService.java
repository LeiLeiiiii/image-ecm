package com.sunyard.module.system.service;

import java.util.ArrayList;
import java.util.Comparator;
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
import com.sunyard.framework.mybatis.util.SnowflakeUtils;
import com.sunyard.module.system.api.dto.SysInstDTO;
import com.sunyard.module.system.api.dto.SysInstExportDTO;
import com.sunyard.module.system.api.dto.SysOrgDTO;
import com.sunyard.module.system.api.dto.SysUserAdminDTO;
import com.sunyard.module.system.api.dto.SysUserDTO;
import com.sunyard.module.system.mapper.SysDeptMapper;
import com.sunyard.module.system.mapper.SysInstMapper;
import com.sunyard.module.system.mapper.SysMenuMapper;
import com.sunyard.module.system.mapper.SysRoleMapper;
import com.sunyard.module.system.mapper.SysUserAdminMapper;
import com.sunyard.module.system.mapper.SysUserMapper;
import com.sunyard.module.system.po.SysDept;
import com.sunyard.module.system.po.SysInst;
import com.sunyard.module.system.po.SysMenu;
import com.sunyard.module.system.po.SysRole;
import com.sunyard.module.system.po.SysUser;

import lombok.extern.slf4j.Slf4j;

/**
 * 组织机构-机构管理
 *
 * @Author PJW 2021/7/6 9:28
 */
@Slf4j
@Service
public class OrgInstService {

    @Resource
    private SnowflakeUtils snowflakeUtils;
    @Resource
    private SqlSessionFactory sqlSessionFactory;
    @Resource
    private SysInstMapper sysInstMapper;
    @Resource
    private SysDeptMapper sysDeptMapper;
    @Resource
    private SysUserMapper sysUserMapper;
    @Resource
    private SysRoleMapper sysRoleMapper;
    @Resource
    private SysMenuMapper sysMenuMapper;
    @Resource
    private SysUserAdminMapper sysUserAdminMapper;

    /**
     * 查询层级机构
     *
     * @param parentId 父级id
     * @return Result
     */
    public List<SysOrgDTO> searchInstTree(Long parentId) {
        List<SysOrgDTO> list = sysInstMapper.searchInstTree(parentId);
        for (SysOrgDTO inst : list) {
            inst.setId(inst.getInstId());
        }
        return list;
    }

    /**
     * 查询机构
     *
     * @param instId 机构id
     * @return Result
     */
    public List<SysInst> search(Long instId) {
        return sysInstMapper.selectList(new LambdaQueryWrapper<SysInst>()
                .eq(SysInst::getInstId, instId));
    }

    /**
     * 查看机构详情
     *
     * @param instId 机构id
     * @return Result
     */
    public SysInst select(Long instId) {
        Assert.notNull(instId, "参数错误");
        return sysInstMapper.selectOne(new LambdaQueryWrapper<SysInst>()
                .eq(SysInst::getInstId, instId)
                .eq(SysInst::getNewlevel, 0));
    }

    /**
     * 批量查询机构
     * @param instIds 机构id
     * @return Result
     */
    public List<SysInst> selectByIds(List<Long> instIds) {
        Assert.notEmpty(instIds, "参数错误");
        return sysInstMapper.selectList(new LambdaQueryWrapper<SysInst>()
                .in(SysInst::getInstId, instIds)
                .eq(SysInst::getNewlevel, 0));
    }

    /**
     * 根据机构获取本身及子机构
     *
     * @param instId 机构id
     * @return Result
     */
    public List<SysInst> getInstListByInstId(Long instId) {
        return sysInstMapper.selectList(new LambdaQueryWrapper<SysInst>()
                .eq(SysInst::getParentId, instId)
                .or()
                .eq(SysInst::getInstId, instId));
    }

    /**
     * 添加机构
     *
     * @param sysInst 机构obj
     */
    @Transactional(rollbackFor = Exception.class)
    @Lock4j(keys = "#sysInst.instNo")
    public void add(SysInst sysInst) {
        Assert.notNull(sysInst, "参数错误");
        Assert.notNull(sysInst.getName(), "机构名称不能为空");
        Assert.notNull(sysInst.getInstNo(), "机构号不能为空");
        SysInst smOrganTb = sysInstMapper.selectOne(new LambdaQueryWrapper<SysInst>()
                .eq(SysInst::getName, sysInst.getName())
                .eq(SysInst::getParentId, sysInst.getParentId()));
        Assert.isNull(smOrganTb, "机构已存在");
        SysInst sysInst1 = sysInstMapper.selectOne(new LambdaQueryWrapper<SysInst>()
                .eq(SysInst::getInstNo, sysInst.getInstNo())
                .eq(SysInst::getNewlevel, 0));
        Assert.isNull(sysInst1, "机构号已存在");
        List<SysInst> sysInsts = sysInstMapper.selectList(new LambdaQueryWrapper<SysInst>()
                .eq(SysInst::getInstId, sysInst.getParentId())
                .orderByAsc(SysInst::getNewlevel));
        List<SysInst> list = new ArrayList<>();
        Long instId = ObjectUtils.isEmpty(sysInst.getInstId())?snowflakeUtils.nextId():sysInst.getInstId();
        StringBuffer nameLevel = new StringBuffer();
        if (sysInsts.size() > 0) {
            nameLevel.append(sysInsts.get(0).getNameLevel()).append("-").append(sysInst.getName());
        } else {
            nameLevel.append(sysInst.getName());
        }
        Integer level = 0;
        for (SysInst org : sysInsts) {
            level++;
            SysInst newOrg = new SysInst();
            newOrg.setInstId(instId);
            newOrg.setInstNo(org.getInstNo());
            newOrg.setParentId(org.getParentId());
            newOrg.setName(sysInst.getName());
            newOrg.setNameLevel(nameLevel.toString());
            newOrg.setNewlevel(level);
            newOrg.setRemarks(sysInst.getRemarks());
            list.add(newOrg);
        }
        SysInst newOrg = new SysInst();
        newOrg.setInstId(instId);
        newOrg.setInstNo(sysInst.getInstNo());
        newOrg.setParentId(sysInst.getParentId());
        newOrg.setName(sysInst.getName());
        newOrg.setNameLevel(nameLevel.toString());
        newOrg.setNewlevel(0);
        newOrg.setRemarks(sysInst.getRemarks());
        list.add(newOrg);
        MybatisBatch<SysInst> mybatisBatch = new MybatisBatch<>(sqlSessionFactory, list);
        MybatisBatch.Method<SysInst> method = new MybatisBatch.Method<>(SysInstMapper.class);
        mybatisBatch.execute(method.insert());
    }

    /**
     * 修改机构
     *
     * @param sysInst 机构obj
     */
    @Transactional(rollbackFor = Exception.class)
    public void update(SysInst sysInst) {
        Assert.notNull(sysInst, "参数错误");
        Assert.notNull(sysInst.getInstId(), "参数错误");
        SysInst smOrganTb = sysInstMapper.selectOne(new LambdaQueryWrapper<SysInst>()
                .eq(SysInst::getName, sysInst.getName())
                .eq(SysInst::getParentId, sysInst.getParentId())
                .ne(SysInst::getInstId, sysInst.getInstId()));
        Assert.isNull(smOrganTb, "机构已存在");
        SysInst sysInst1 = sysInstMapper.selectOne(new LambdaQueryWrapper<SysInst>()
                .eq(SysInst::getInstNo, sysInst.getInstNo())
                .eq(SysInst::getNewlevel, 0)
                .ne(SysInst::getInstId, sysInst.getInstId()));
        Assert.isNull(sysInst1, "机构号已存在");
        SysInst oldSysInst = sysInstMapper.selectOne(
                new LambdaQueryWrapper<SysInst>().eq(SysInst::getInstId, sysInst.getInstId()).eq(SysInst::getNewlevel, 0));
        Assert.notNull(oldSysInst, "参数错误");
        sysInst.setParentId(null);
        sysInst.setInstId(null);
        sysInst.setNewlevel(null);
        sysInst.setNameLevel(
                oldSysInst.getNameLevel().replaceAll(oldSysInst.getName(), sysInst.getName()));
        sysInstMapper.update(sysInst,
                new LambdaQueryWrapper<SysInst>().eq(SysInst::getInstId, oldSysInst.getInstId()));
    }

    /**
     * 删除机构
     *
     * @param instId 机构id
     */
    public void delete(Long instId) {
        Assert.notNull(instId, "参数错误");
        SysInst sysInst = sysInstMapper
                .selectOne(new LambdaQueryWrapper<SysInst>().eq(SysInst::getInstId, instId).eq(SysInst::getNewlevel, 0));
        Assert.notNull(sysInst, "参数错误");
        Long countOrg = sysInstMapper
                .selectCount(new LambdaQueryWrapper<SysInst>().eq(SysInst::getParentId, instId));
        Assert.isTrue(countOrg < 1, "请删除子机构后再进行删除操作");
        Long countDept = sysDeptMapper
                .selectCount(new LambdaQueryWrapper<SysDept>().eq(SysDept::getParentId, instId));
        Assert.isTrue(countDept < 1, "请删除机构下部门后再进行删除操作");
        Long countUser = sysUserMapper
                .selectCount(new LambdaQueryWrapper<SysUser>().eq(SysUser::getInstId, instId));
        Assert.isTrue(countUser < 1, "请删除机构下用户后再进行删除操作");
        sysInstMapper.delete(new LambdaQueryWrapper<SysInst>().eq(SysInst::getInstId, instId));
    }

    /**
     * 机构导入
     *
     * @param maps 数据map
     */
    @Transactional(rollbackFor = Exception.class)
    public void instImport(List<Map<Integer, String>> maps) {
        for (Map<Integer, String> map : maps) {
            SysInst sysInst = new SysInst();
            sysInst.setParentId(0L);
            if (StringUtils.hasText(map.get(0))) {
                List<SysInst> sysInstList = sysInstMapper
                        .selectList(new LambdaQueryWrapper<SysInst>()
                                .eq(SysInst::getName, map.get(0)));
                if (!CollectionUtils.isEmpty(sysInstList)) {
                    sysInst.setParentId(sysInstList.get(0).getInstId());
                }
            }
            sysInst.setName(map.get(1));
            sysInst.setInstNo(map.get(2));
            sysInst.setRemarks(map.get(5));
            add(sysInst);
        }
    }

    /**
     * 组织架构导出数据
     * @return result
     */
    public List<SysInstExportDTO> exportList() {
        return sysInstMapper.exportList();
    }

    /**
     * 查询系统菜单
     *
     * @return Result
     */
    public List<SysMenu> searchShiroPaths() {
        return sysMenuMapper.selectList(new LambdaQueryWrapper<SysMenu>().orderByDesc(SysMenu::getPath));
    }

    /**
     * 查询超级管理员信息
     *
     * @param id 用户id
     * @return Result
     */
    public SysUserAdminDTO selectSuperOrg(Long id) {
        Assert.notNull(id, "参数错误");
        SysUserAdminDTO sysUserAdminDTO = sysUserAdminMapper.selectOrg(id);
        List<SysRole> sysRoles = sysRoleMapper.searchRoleNameByUserId(sysUserAdminDTO.getUserId());
        StringBuilder roleStr = new StringBuilder();
        for (SysRole r : sysRoles) {
            roleStr.append(r.getName()).append(",");
        }
        if (roleStr.length() > 0) {
            roleStr.deleteCharAt(roleStr.length() - 1);
        }
        sysUserAdminDTO.setRoleStr(roleStr.toString());
        //用户敏感信息泄露处理---微服务之间调用、反显给前端，都是走UserApiImpl，所以此处统一处理
        //安全信息
        sysUserAdminDTO.setPwd(null);
        sysUserAdminDTO.setSalt(null);
        sysUserAdminDTO.setIsDeleted(null);
        return sysUserAdminDTO;
    }

    /**
     * 查询用户个人信息
     *
     * @param userId 用户id
     * @return Result
     */
    public SysUserDTO selectOrg(Long userId) {
        Assert.notNull(userId, "参数错误");
        SysUserDTO sysUserDTO = sysUserMapper.selectOrg(userId);
        List<SysRole> sysRoles = sysRoleMapper.searchRoleNameByUserId(sysUserDTO.getUserId());
        StringBuilder roleStr = new StringBuilder();
        for (SysRole r : sysRoles) {
            roleStr.append(r.getName()).append(",");
        }
        if (roleStr.length() > 0) {
            roleStr.deleteCharAt(roleStr.length() - 1);
        }
        sysUserDTO.setRoleStr(roleStr.toString());
        //用户敏感信息泄露处理---微服务之间调用、反显给前端，都是走UserApiImpl，所以此处统一处理
        //安全信息
        sysUserDTO.setPwd(null);
        sysUserDTO.setSalt(null);
        sysUserDTO.setIsDeleted(null);
        return sysUserDTO;
    }

    /**
     * 查询组织树(机构+部门)
     *
     * @param instId 机构id
     * @param deptId 部门id
     * @return Result
     */
    public List<SysOrgDTO> searchOrgTree(Long instId, Long deptId) {
        List<SysOrgDTO> list = new ArrayList<>();
        if (null == deptId || 0L == deptId) {
            List<SysInst> insts = sysInstMapper.selectList(new LambdaQueryWrapper<SysInst>()
                    .eq(SysInst::getNewlevel, 0)
                    .eq(null != instId, SysInst::getInstId, instId));
            for (SysInst inst : insts) {
                SysOrgDTO sysOrgExtend = new SysOrgDTO();
                sysOrgExtend.setId(inst.getInstId());
                sysOrgExtend.setParentId(inst.getParentId());
                sysOrgExtend.setName(inst.getName());
                sysOrgExtend.setType(1);
                list.add(sysOrgExtend);
            }
        }
        deptId = null == deptId ? instId : 0L == deptId ? instId : deptId;
        List<SysOrgDTO> depts = sysDeptMapper.searchDeptTree(deptId);
        for (SysOrgDTO dept : depts) {
            dept.setId(dept.getDeptId());
            dept.setType(2);
            list.add(dept);
        }
        list.sort(Comparator.comparingLong(SysOrgDTO::getId));
        return list;
    }

    /**
     * 查询所有机构
     *
     * @return Result
     */
    public List<SysInst> getInstAll() {
        return sysInstMapper.selectList(new LambdaQueryWrapper<SysInst>()
                .eq(SysInst::getNewlevel, 0));
    }

    /**
     * 根据机构号查询机构
     *
     * @param instNo 机构号
     * @return Result
     */
    public List<SysInstDTO> selectByNo(String instNo) {
        List<SysInst> sysInsts = sysInstMapper.selectList(
                new LambdaQueryWrapper<SysInst>().eq(null != instNo, SysInst::getInstNo, instNo));
        List<SysInstDTO> dtoList = sysInsts.stream().map(po -> {
            SysInstDTO dto = new SysInstDTO();
            BeanUtils.copyProperties(po, dto);
            return dto;
        }).collect(Collectors.toList());
        return dtoList;
    }
}
