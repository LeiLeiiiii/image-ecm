package com.sunyard.module.system.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sunyard.framework.common.result.Result;
import com.sunyard.module.system.api.dto.SysInstDTO;
import com.sunyard.module.system.api.dto.SysOrgDTO;
import com.sunyard.module.system.constant.RoleConstants;
import com.sunyard.module.system.mapper.SysDeptMapper;
import com.sunyard.module.system.mapper.SysInstMapper;
import com.sunyard.module.system.po.SysInst;
import com.sunyard.module.system.service.OrgInstService;

import cn.hutool.core.lang.tree.Tree;
import cn.hutool.core.lang.tree.TreeNodeConfig;
import cn.hutool.core.lang.tree.TreeUtil;

/**
 * @author zhouleibin
 */
@RestController
public class InstApiImpl implements InstApi {

    @Resource
    private OrgInstService orgInstService;
    @Resource
    private SysInstMapper sysInstMapper;
    @Resource
    private SysDeptMapper sysDeptMapper;

    @Override
    public Result<SysInstDTO> getInstByInstId(Long instId) {
        SysInst po = orgInstService.select(instId);
        SysInstDTO dto = new SysInstDTO();
        BeanUtils.copyProperties(po, dto);
        return Result.success(dto);
    }

    @Override
    public Result<List<SysInstDTO>> getInstsByInstIds(Long[] instIds) {
        List<SysInst> poList = orgInstService.selectByIds(Arrays.asList(instIds));
        List<SysInstDTO> dtoList = poList.stream().map(po -> {
            SysInstDTO dto = new SysInstDTO();
            BeanUtils.copyProperties(po, dto);
            return dto;
        }).collect(Collectors.toList());
        return Result.success(dtoList);
    }

    @Override
    public Result<List<SysOrgDTO>> searchInstTree(Long parentId) {
        return Result.success(orgInstService.searchInstTree(parentId));
    }

    @Override
    public Result<List<SysInstDTO>> getInstListByInstId(Long instId) {
        List<SysInst> poList = orgInstService.getInstListByInstId(instId);
        List<SysInstDTO> dtoList = poList.stream().map(po -> {
            SysInstDTO dto = new SysInstDTO();
            BeanUtils.copyProperties(po, dto);
            return dto;
        }).collect(Collectors.toList());
        return Result.success(dtoList);
    }

    @Override
    public Result<List<SysInstDTO>> getInstByNo(String instNo) {
        return Result.success(orgInstService.selectByNo(instNo));
    }

    @Override
    public Result<List<Tree<Long>>> getOrgTree() {
        List<SysOrgDTO> list = new ArrayList<>();
        List<SysInst> insts = sysInstMapper.selectList(new LambdaQueryWrapper<SysInst>()
                .eq(SysInst::getNewlevel, 0));
        for (SysInst inst : insts) {
            SysOrgDTO sysOrgExtend = new SysOrgDTO();
            sysOrgExtend.setId(inst.getInstId());
            sysOrgExtend.setParentId(inst.getParentId());
            sysOrgExtend.setName(inst.getName());
            sysOrgExtend.setType(1);
            list.add(sysOrgExtend);
        }
        List<SysOrgDTO> depts = sysDeptMapper.searchDeptTree(RoleConstants.ZEROLONG);
        for (SysOrgDTO dept : depts) {
            dept.setId(dept.getDeptId());
            dept.setType(2);
            list.add(dept);
        }
        //构建树
        TreeNodeConfig treeNodeConfig = new TreeNodeConfig();
        List<Tree<Long>> treeList = TreeUtil.build(list, RoleConstants.ZEROLONG, treeNodeConfig, (treeNode, tree) -> {
            tree.setId(treeNode.getId());
            tree.setParentId(treeNode.getParentId());
            tree.setName(treeNode.getName());
            tree.putExtra("deptId", treeNode.getDeptId());
            tree.putExtra("type", treeNode.getType());
            tree.putExtra("disabled", treeNode.getType() == 1);
        });
        return Result.success(treeList);
    }
}
