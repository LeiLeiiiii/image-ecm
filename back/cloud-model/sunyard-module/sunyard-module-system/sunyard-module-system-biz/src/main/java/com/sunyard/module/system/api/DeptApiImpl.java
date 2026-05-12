package com.sunyard.module.system.api;

import com.sunyard.framework.common.result.Result;
import com.sunyard.module.system.api.dto.SysDeptDTO;
import com.sunyard.module.system.api.dto.SysOrgDTO;
import com.sunyard.module.system.po.SysDept;
import com.sunyard.module.system.service.OrgDeptService;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * user模块对内提供的controller
 *
 * @Author PJW 2023/2/15 15:35
 */
@RestController
public class DeptApiImpl implements DeptApi {

    @Resource
    private OrgDeptService service;

    @Override
    public Result<SysDeptDTO> selectById(Long deptId) {
        SysDept po = service.selectById(deptId);
        SysDeptDTO dto = new SysDeptDTO();
        BeanUtils.copyProperties(po, dto);
        return Result.success(dto);
    }

    @Override
    public Result<List<SysDeptDTO>> selectDeptById(Long deptId,Integer sort) {
        List<SysDept> poList = service.selectDeptById(deptId, sort);
        List<SysDeptDTO> dtoList = poList.stream().map(po -> {
            SysDeptDTO dto = new SysDeptDTO();
            BeanUtils.copyProperties(po, dto);
            return dto;
        }).collect(Collectors.toList());
        return Result.success(dtoList);
    }

    @Override
    public Result<List<SysDeptDTO>> selectByIds(Long[] deptIds) {
        List<SysDept> poList = service.selectByIds(Arrays.asList(deptIds));
        List<SysDeptDTO> dtoList = poList.stream().map(po -> {
            SysDeptDTO dto = new SysDeptDTO();
            BeanUtils.copyProperties(po, dto);
            return dto;
        }).collect(Collectors.toList());
        return Result.success(dtoList);
    }

    @Override
    public Result<List<SysOrgDTO>> searchDeptTree(Long parentId) {
        return Result.success(service.searchDeptTree(parentId));
    }

    @Override
    public Result<List<SysOrgDTO>> searchUserDeptTree(Long parentId) {
        return Result.success(service.searchUserDeptTree(parentId));
    }

    @Override
    public Result<List<SysDeptDTO>> getDeptAll() {
        List<SysDept> poList = service.getDeptAll();
        List<SysDeptDTO> dtoList = poList.stream().map(po -> {
            SysDeptDTO dto = new SysDeptDTO();
            BeanUtils.copyProperties(po, dto);
            return dto;
        }).collect(Collectors.toList());
        return Result.success(dtoList);
    }

    @Override
    public Result<List<SysDeptDTO>> searchByParentId(Long parentId) {
        return Result.success(service.searchByParentId(parentId));
    }

    @Override
    public Result<List<SysDeptDTO>> getDeptByNo(String deptNo) {
        return Result.success(service.selectByNo(deptNo));
    }

    @Override
    public Result<Integer> addDept(SysDeptDTO sysDeptDTO) {
        Integer i = service.addSysDeptDTO(sysDeptDTO);
        return Result.success(i);
    }

    @Override
    public Result<Integer> delDeptByDeptId(Long[] deptIds) {
        Integer i = service.delDeptByDeptId(Arrays.asList(deptIds));
        return Result.success(i);
    }

    @Override
    public Result<Integer> updateDeptByDeptIdAndLevel(SysDeptDTO sysDeptDTO) {
        Integer i = service.updateDeptByDeptIdAndLevel(sysDeptDTO);
        return Result.success(i);
    }

    @Override
    public Result<List<SysDeptDTO>> getAllLevelDept() {
        List<SysDept> poList = service.getAllLevelDept();
        List<SysDeptDTO> dtoList = poList.stream().map(po -> {
            SysDeptDTO dto = new SysDeptDTO();
            BeanUtils.copyProperties(po, dto);
            return dto;
        }).collect(Collectors.toList());
        return Result.success(dtoList);
    }
}
