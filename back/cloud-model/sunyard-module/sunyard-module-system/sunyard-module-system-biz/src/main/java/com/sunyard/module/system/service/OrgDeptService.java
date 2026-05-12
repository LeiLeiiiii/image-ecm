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
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import com.baomidou.mybatisplus.core.batch.MybatisBatch;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.sunyard.framework.common.page.PageForm;
import com.sunyard.framework.common.util.AssertUtils;
import com.sunyard.framework.mybatis.util.SnowflakeUtils;
import com.sunyard.module.system.api.dto.SysDeptDTO;
import com.sunyard.module.system.api.dto.SysDeptExportDTO;
import com.sunyard.module.system.api.dto.SysOrgDTO;
import com.sunyard.module.system.constant.StateConstants;
import com.sunyard.module.system.mapper.SysDeptMapper;
import com.sunyard.module.system.mapper.SysInstMapper;
import com.sunyard.module.system.mapper.SysUserMapper;
import com.sunyard.module.system.po.SysDept;
import com.sunyard.module.system.po.SysInst;
import com.sunyard.module.system.po.SysUser;

/**
 * 组织机构-部门管理
 *
 * @Author PJW 2021/7/6 9:28
 */
@Service
public class OrgDeptService {

    @Resource
    private SnowflakeUtils snowflakeUtils;
    @Resource
    private SqlSessionFactory sqlSessionFactory;
    @Resource
    private SysDeptMapper sysDeptMapper;
    @Resource
    private SysUserMapper sysUserMapper;
    @Resource
    private SysInstMapper sysInstMapper;

    /**
     * 添加部门
     *
     * @param dept 部门obj
     * @return Result
     */
    @Transactional(rollbackFor = Exception.class)
    public String add(SysDept dept) {
        Assert.notNull(dept, "参数错误");
        SysDept sysDept = sysDeptMapper.selectOne(new LambdaQueryWrapper<SysDept>()
                .eq(SysDept::getName, dept.getName()).eq(SysDept::getParentId, dept.getParentId()));
        Assert.isNull(sysDept, "部门已存在");
        sysDept = sysDeptMapper.selectOne(new LambdaQueryWrapper<SysDept>()
                .eq(SysDept::getDeptNo, dept.getDeptNo()).eq(SysDept::getParentId, dept.getParentId()));
        Assert.isNull(sysDept, "部门号已存在");
        List<SysDept> sysDepts = sysDeptMapper.selectList(new LambdaQueryWrapper<SysDept>()
                .eq(SysDept::getDeptId, dept.getParentId()).orderByAsc(SysDept::getNewlevel));
        List<SysDept> list = new ArrayList<>();
        Long dptId = ObjectUtils.isEmpty(dept.getDeptId())?snowflakeUtils.nextId():dept.getDeptId();;
        StringBuffer nameLevel = new StringBuffer();
        if (sysDepts.size() > 0) {
            nameLevel.append(sysDepts.get(0).getNameLevel()).append("-").append(dept.getName());
        } else {
            nameLevel.append(dept.getName());
        }
        Integer level = 0;
        for (SysDept s : sysDepts) {
            level++;
            SysDept newDpet = new SysDept();
            newDpet.setDeptId(dptId);
            newDpet.setDeptNo(dept.getDeptNo());
            newDpet.setParentId(s.getParentId());
            newDpet.setName(dept.getName());
            newDpet.setNameLevel(nameLevel.toString());
            newDpet.setNewlevel(level);
            newDpet.setLdapId(dept.getLdapId());
            list.add(newDpet);
        }
        SysDept newDpet = new SysDept();
        newDpet.setDeptId(dptId);
        newDpet.setDeptNo(dept.getDeptNo());
        newDpet.setParentId(dept.getParentId());
        newDpet.setName(dept.getName());
        newDpet.setNameLevel(nameLevel.toString());
        newDpet.setLdapId(dept.getLdapId());
        newDpet.setNewlevel(0);
        list.add(newDpet);

        MybatisBatch<SysDept> mybatisBatch = new MybatisBatch<>(sqlSessionFactory, list);
        MybatisBatch.Method<SysDept> method = new MybatisBatch.Method<>(SysDeptMapper.class);
        mybatisBatch.execute(method.insert());
        return list.get(0).getDeptId().toString();
    }

    /**
     * 获得当前机构下部门信息
     *
     * @param instId 机构id
     * @return Result
     */
    public List<HashMap<String, String>> getDeptEnum(Long instId) {
        List<SysDept> sysDepts = sysDeptMapper.selectList(new LambdaQueryWrapper<SysDept>()
                .eq(null != instId, SysDept::getParentId, instId)
                .eq(SysDept::getNewlevel, 0));
        List<HashMap<String, String>> result = new ArrayList<>();
        for (SysDept sysDept : sysDepts) {
            HashMap<String, String> map = new HashMap<>(6);
            map.put("code", sysDept.getDeptId().toString());
            map.put("desc", sysDept.getName());
            result.add(map);
        }
        return result;
    }

    /**
     * 修改部门
     *
     * @param dept 部门obj
     */
    @Transactional
    public void update(SysDept dept) {
        Assert.notNull(dept, "参数错误");
        Assert.notNull(dept.getDeptId(), "参数错误");
        SysDept sysDeptName = sysDeptMapper.selectOne(new LambdaQueryWrapper<SysDept>()
                .eq(SysDept::getName, dept.getName())
                .eq(SysDept::getParentId, dept.getParentId())
                .eq(SysDept::getNewlevel, 0)
                .ne(SysDept::getDeptId, dept.getDeptId()));
        Assert.isNull(sysDeptName, "部门名重复");
        SysDept sysDeptNo = sysDeptMapper.selectOne(new LambdaQueryWrapper<SysDept>()
                .eq(SysDept::getDeptNo, dept.getDeptNo())
                .eq(SysDept::getParentId, dept.getParentId())
                .eq(SysDept::getNewlevel, 0)
                .ne(SysDept::getDeptId, dept.getDeptId()));
        Assert.isNull(sysDeptNo, "部门号重复");
        SysDept seltDept = sysDeptMapper.selectOne(new LambdaQueryWrapper<SysDept>()
                .eq(SysDept::getDeptId, dept.getDeptId())
                .eq(SysDept::getNewlevel, 0));
        Assert.notNull(seltDept, "参数错误");
        SysDept newDept = new SysDept();
        newDept.setName(dept.getName());
        newDept.setDeptNo(dept.getDeptNo());
        newDept.setNameLevel(seltDept.getNameLevel().replace(seltDept.getName(), dept.getName()));
        sysDeptMapper.update(newDept, new LambdaUpdateWrapper<SysDept>()
                .eq(SysDept::getDeptId, seltDept.getDeptId()));

        //新增逻辑：更新联级子节点name_level的前缀
        List<SysDept> childList = sysDeptMapper.selectList(new LambdaQueryWrapper<SysDept>()
                .eq(SysDept::getParentId, dept.getDeptId()));
        for (SysDept childSysDept : childList) {
            SysDept newNameLevelDept = new SysDept();
            newNameLevelDept.setNameLevel(
                    childSysDept.getNameLevel().replaceFirst(seltDept.getName(), dept.getName()));
            sysDeptMapper.update(newNameLevelDept, new LambdaUpdateWrapper<SysDept>()
                    .eq(SysDept::getDeptId, childSysDept.getDeptId()));
        }
    }

    /**
     * 删除部门
     *
     * @param deptId 部门id
     */
    public void delete(Long deptId) {
        Assert.notNull(deptId, "参数错误");
        Long count = sysDeptMapper.selectCount(new LambdaQueryWrapper<SysDept>()
                .eq(SysDept::getParentId, deptId));
        Assert.isTrue(count < 1, "存在子部门，无法删除");
        Long countUser = sysUserMapper.selectCount(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getDeptId, deptId));
        Assert.isTrue(countUser < 1, "请删除部门下用户后再进行删除操作");
        // 删除部门表数据
        sysDeptMapper.delete(new LambdaQueryWrapper<SysDept>()
                .eq(SysDept::getDeptId, deptId));
    }

    /**
     * 查询部门 分页
     *
     * @param page 分页参数
     * @param dept 部门obj
     * @return Result
     */
    public PageInfo search(PageForm page, SysDept dept) {
        PageHelper.startPage(page.getPageNum(), page.getPageSize());
        List<SysDept> result = sysDeptMapper
                .selectList(new LambdaQueryWrapper<SysDept>().eq(SysDept::getNewlevel, 0)
                        .eq(StringUtils.hasText(dept.getDeptId().toString()), SysDept::getParentId,
                                dept.getDeptId())
                        .likeRight(StringUtils.hasText(dept.getName()), SysDept::getName, dept.getName()));
        return new PageInfo<SysDept>(result);
    }

    /**
     * 查询部门树
     *
     * @param parentId 部门id-父级
     * @return Result
     */
    public List<SysOrgDTO> searchDeptTree(Long parentId) {
        List<SysOrgDTO> list = sysDeptMapper.searchDeptTree(parentId);
        for (SysOrgDTO dept : list) {
            dept.setId(dept.getDeptId());
        }
        return list;
    }

    /**
     * 根据登录用户部门id获取所属部门及子部门树
     * @param deptId 部门id
     * @return Result
     */
    public List<SysOrgDTO> searchUserDeptTree(Long deptId) {
        List<SysOrgDTO> list = sysDeptMapper.searchDeptTree(deptId);
        for (SysOrgDTO dept : list) {
            dept.setId(dept.getDeptId());
        }
        return list;
    }

    /*******************部门同步*****************************/
    /**
     * 添加部门
     *
     * @param deptDTO 部门obj
     * @return Result
     */
    public Integer addSysDeptDTO(SysDeptDTO deptDTO) {
        AssertUtils.isNull(deptDTO.getDeptNo(), "参数错误");
        AssertUtils.isNull(deptDTO.getDeptId(), "参数错误");
        AssertUtils.isNull(deptDTO.getName(), "参数错误");
        AssertUtils.isNull(deptDTO.getParentId(), "参数错误");
        AssertUtils.isNull(deptDTO.getNameLevel(), "参数错误");
        AssertUtils.isNull(deptDTO.getNewlevel(), "参数错误");
        SysDept sysDept = new SysDept();
        BeanUtils.copyProperties(deptDTO, sysDept);
        return sysDeptMapper.insert(sysDept);
    }

    /**
     * 删除部门
     * @param deptIdList
     * @return
     */
    public Integer delDeptByDeptId(List<Long> deptIdList) {
        if (CollectionUtils.isEmpty(deptIdList)) {
            return StateConstants.ZERO;
        }
        return sysDeptMapper
                .delete(new LambdaQueryWrapper<SysDept>().in(SysDept::getDeptId, deptIdList));
    }

    /**
     * 编辑部门
     *
     * @param sysDeptDTO
     * @return
     */
    public Integer updateDeptByDeptIdAndLevel(SysDeptDTO sysDeptDTO) {
        AssertUtils.isNull(sysDeptDTO.getDeptNo(), "参数错误");
        AssertUtils.isNull(sysDeptDTO.getDeptId(), "参数错误");
        AssertUtils.isNull(sysDeptDTO.getName(), "参数错误");
        AssertUtils.isNull(sysDeptDTO.getParentId(), "参数错误");
        AssertUtils.isNull(sysDeptDTO.getNameLevel(), "参数错误");
        AssertUtils.isNull(sysDeptDTO.getNewlevel(), "参数错误");
        SysDept sysDept = new SysDept();
        BeanUtils.copyProperties(sysDeptDTO, sysDept);
        return sysDeptMapper.update(sysDept, new LambdaUpdateWrapper<SysDept>()
                .eq(SysDept::getDeptId, sysDeptDTO.getDeptId()).eq(SysDept::getNewlevel, sysDeptDTO.getNewlevel()));
    }

    /**
     * 通过id获取数据
     *
     * @param deptId 部门id
     * @return Result
     */
    public SysDept selectById(Long deptId) {
        Assert.notNull(deptId, "参数错误");
        SysDept sysDept = sysDeptMapper
                .selectOne(new LambdaQueryWrapper<SysDept>().eq(SysDept::getDeptId, deptId).eq(SysDept::getNewlevel, 0));
        return sysDept;
    }

    /**
     * 批量查询不部门
     *
     * @param deptIds 部门id集
     * @return Result
     */
    public List<SysDept> selectByIds(List<Long> deptIds) {
        Assert.notEmpty(deptIds, "参数错误");
        List<SysDept> sysDepts = sysDeptMapper
                .selectList(new LambdaQueryWrapper<SysDept>().in(SysDept::getDeptId, deptIds).eq(SysDept::getNewlevel, 0));
        return sysDepts;
    }

    /**
     * 获取所有部门
     *
     * @return Result
     */
    public List<SysDept> getDeptAll() {
        List<SysDept> sysDepts = sysDeptMapper
                .selectList(new LambdaQueryWrapper<SysDept>().eq(SysDept::getNewlevel, 0));
        return sysDepts;
    }

    /**
     * 部门导入
     *
     * @param maps 数据map
     */
    @Transactional(rollbackFor = Exception.class)
    public void deptImport(List<Map<Integer, String>> maps) {
        for (Map<Integer, String> map : maps) {
            String instName = map.get(2);
            Assert.hasText(instName, map.get(0) + "所属机构为空!");
            List<SysInst> instList = sysInstMapper
                    .selectList(new LambdaQueryWrapper<SysInst>().eq(SysInst::getName, instName));
            Assert.isTrue(CollectionUtils.isNotEmpty(instList), map.get(0) + "所属机构为空!");
            List<SysDept> deptList = sysDeptMapper.selectList(new LambdaQueryWrapper<SysDept>()
                    .eq(SysDept::getParentId, instList.get(0).getInstId()).eq(SysDept::getName, map.get(3)));

            SysDept sysDept = new SysDept();
            if (CollectionUtils.isNotEmpty(deptList)) {
                sysDept.setParentId(deptList.get(0).getDeptId());
            } else {
                sysDept.setParentId(instList.get(0).getInstId());
            }
            sysDept.setName(map.get(0));
            sysDept.setDeptNo(map.get(1));
            add(sysDept);
        }
    }

    /**
     * 部门导出数据
     *
     * @return Result
     */
    public List<SysDeptExportDTO> exportList() {
        List<SysOrgDTO> instList = sysInstMapper.searchInstTree(null);
        List<SysDeptExportDTO> list = new ArrayList<>();
        for (SysOrgDTO inst : instList) {
            inst.setId(inst.getInstId());
            List<SysDeptExportDTO> deptList = sysDeptMapper.exportListByInstId(inst.getId());
            deptList.forEach(dept -> dept.setInstName(inst.getName()));
            list.addAll(deptList);
        }
        return list;
    }

    /**
     * 根据父id拿到部门lsit
     *
     * @param parentId 父级id
     * @return Result
     */
    public List<SysDeptDTO> searchByParentId(Long parentId) {
        List<SysDept> sysDepts = sysDeptMapper.selectList(new LambdaQueryWrapper<SysDept>()
                .eq(null != parentId, SysDept::getParentId, parentId).eq(SysDept::getNewlevel, 0));
        List<SysDeptDTO> dtoList = sysDepts.stream().map(po -> {
            SysDeptDTO dto = new SysDeptDTO();
            BeanUtils.copyProperties(po, dto);
            return dto;
        }).collect(Collectors.toList());
        return dtoList;
    }

    /**
     * 根据部门号拿到部门
     *
     * @param deptNo 部门号
     * @return Result
     */
    public List<SysDeptDTO> selectByNo(String deptNo) {
        List<SysDept> sysDepts = sysDeptMapper.selectList(
                new LambdaQueryWrapper<SysDept>().eq(null != deptNo, SysDept::getDeptNo, deptNo));
        List<SysDeptDTO> dtoList = sysDepts.stream().map(po -> {
            SysDeptDTO dto = new SysDeptDTO();
            BeanUtils.copyProperties(po, dto);
            return dto;
        }).collect(Collectors.toList());
        return dtoList;
    }

    /**
     * 根据部门id获取闭包表部门 根据newlevel倒序
     *
     * @param deptId 部门id
     * @param sort
     * @return
     */
    public List<SysDept> selectDeptById(Long deptId, Integer sort) {
        LambdaQueryWrapper<SysDept> wrapper = new LambdaQueryWrapper<SysDept>().eq(SysDept::getDeptId, deptId);
        //0 或者奴来了desc 1 asc
        if (StateConstants.COMMON_ONE.equals(sort)) {
            wrapper.orderByAsc(SysDept::getNewlevel);
        } else {
            wrapper.orderByDesc(SysDept::getNewlevel);
        }
        return sysDeptMapper.selectList(wrapper);
    }

    /**
     * ldap添加部门
     *
     * @param dept 部门obj
     */
    public void addLdap(SysDept dept) {
        Assert.notNull(dept, "参数错误");
        SysDept sysDept = sysDeptMapper.selectOne(new LambdaQueryWrapper<SysDept>()
                .eq(SysDept::getName, dept.getName()).eq(SysDept::getParentId, dept.getParentId()).eq(SysDept::getNewlevel, 0));
        Assert.isNull(sysDept, "部门已存在");
        List<SysDept> sysDepts = sysDeptMapper.selectList(new LambdaQueryWrapper<SysDept>()
                .eq(SysDept::getDeptId, dept.getParentId()).orderByAsc(SysDept::getNewlevel));
        List<SysDept> list = new ArrayList<>();
        Long dptId = dept.getDeptId();
        StringBuffer nameLevel = new StringBuffer();
        if (sysDepts.size() > 0) {
            nameLevel.append(sysDepts.get(0).getNameLevel()).append("-").append(dept.getName());
        } else {
            nameLevel.append(dept.getName());
        }
        Integer level = 0;
        for (SysDept s : sysDepts) {
            level++;
            SysDept newDpet = new SysDept();
            newDpet.setDeptId(dptId);
            newDpet.setParentId(s.getParentId());
            newDpet.setName(dept.getName());
            newDpet.setNameLevel(nameLevel.toString());
            newDpet.setNewlevel(level);
            newDpet.setLdapId(dept.getLdapId());
            newDpet.setDeptNo(s.getDeptNo());
            list.add(newDpet);
        }
        SysDept newDpet = new SysDept();
        newDpet.setDeptId(dptId);
        newDpet.setParentId(dept.getParentId());
        newDpet.setName(dept.getName());
        newDpet.setNameLevel(nameLevel.toString());
        newDpet.setLdapId(dept.getLdapId());
        newDpet.setDeptNo(dept.getDeptNo());
        newDpet.setNewlevel(0);
        list.add(newDpet);

        MybatisBatch<SysDept> mybatisBatch = new MybatisBatch<>(sqlSessionFactory, list);
        MybatisBatch.Method<SysDept> method = new MybatisBatch.Method<>(SysDeptMapper.class);
        mybatisBatch.execute(method.insert());
    }

    /**
     * 修改部门Ldap
     *
     * @param dept 部门obj
     */
    public void updateLdap(SysDept dept) {
        Assert.notNull(dept, "参数错误");
        Assert.notNull(dept.getDeptId(), "参数错误");
        SysDept sysDept = sysDeptMapper.selectOne(new LambdaQueryWrapper<SysDept>()
                .eq(SysDept::getName, dept.getName()).eq(SysDept::getParentId, dept.getParentId()).eq(SysDept::getNewlevel, 0)
                .ne(SysDept::getDeptId, dept.getDeptId()));
        Assert.isNull(sysDept, "部门已存在");
        // 查询部门 所有级别
        List<SysDept> oldDeptList = sysDeptMapper.selectList(new LambdaQueryWrapper<SysDept>()
                .eq(SysDept::getDeptId, dept.getDeptId()).orderByDesc(SysDept::getNewlevel));
        if (!CollectionUtils.isEmpty(oldDeptList)) {

            // 从级别0开始修改部门信息 不同级别父节点不同
            for (int i = 0; i <= oldDeptList.get(0).getNewlevel(); i++) {
                // 查询父级部门是否存在 当父节点为机构号时父级部门不存在
                List<SysDept> isDept = sysDeptMapper.selectList(new LambdaQueryWrapper<SysDept>()
                        .eq(SysDept::getDeptId, dept.getParentId()).orderByAsc(SysDept::getNewlevel));
                SysDept oldDept = sysDeptMapper.selectOne(new LambdaQueryWrapper<SysDept>()
                        .eq(SysDept::getDeptId, dept.getDeptId()).eq(SysDept::getNewlevel, i));
                Assert.notNull(oldDept, "参数错误");
                dept.setNewlevel(null);
                dept.setNameLevel(
                        oldDept.getNameLevel().replaceAll(oldDept.getName(), dept.getName()));
                sysDeptMapper.update(dept, new LambdaUpdateWrapper<SysDept>()
                        .eq(SysDept::getDeptId, dept.getDeptId()).eq(SysDept::getNewlevel, i));
                if (!CollectionUtils.isEmpty(isDept)) {
                    dept.setParentId(isDept.get(0).getParentId());
                }
            }
        }
    }

    public List<SysDept> getAllLevelDept() {
        return sysDeptMapper
                .selectList(new LambdaQueryWrapper<SysDept>());
    }
}
