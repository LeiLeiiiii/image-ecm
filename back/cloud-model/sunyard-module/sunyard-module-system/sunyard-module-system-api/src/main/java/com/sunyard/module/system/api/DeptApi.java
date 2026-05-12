package com.sunyard.module.system.api;

import com.sunyard.framework.common.result.Result;
import com.sunyard.module.system.api.dto.SysDeptDTO;
import com.sunyard.module.system.api.dto.SysOrgDTO;
import com.sunyard.module.system.constant.ApiConstants;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * @description: 调用授权微服务，验证连接是否授权
 * @author: raochangmei
 * @time: 2022-10-12
 */
@FeignClient(value = ApiConstants.NAME)
public interface DeptApi {

    String PREFIX = ApiConstants.PREFIX + "/dept/";

    /**
     * 查询部门
     *
     * @param deptId 部门id
     * @return Result 部门
     */
    @PostMapping(PREFIX + "selectById")
    Result<SysDeptDTO> selectById(@RequestParam("deptId") Long deptId);

    /**
     * 根据部门id获取闭包表部门 根据newlevel倒序
     *
     * @param deptId 部门id
     * @param sort   0或者null desc 1 asc
     * @return Result 部门集
     */
    @PostMapping(PREFIX + "selectDeptsById")
    Result<List<SysDeptDTO>> selectDeptById(@RequestParam("deptId") Long deptId, @RequestParam(value = "sort", required = false) Integer sort);

    /**
     * 查询部门集
     *
     * @param deptIds id集
     * @return Result 部门集
     */
    @PostMapping(PREFIX + "selectByIds")
    Result<List<SysDeptDTO>> selectByIds(@RequestParam("deptIds") Long[] deptIds);

    /**
     * 查询用户所属机构下的所有部门树
     *
     * @param parentId 父级id（机构id）
     * @return Result 机构集
     */
    @PostMapping(PREFIX + "searchDeptTree")
    Result<List<SysOrgDTO>> searchDeptTree(@RequestParam(value = "parentId", required = false) Long parentId);

    /**
     * 查询用户所属部门及下属所有子部门的部门树
     *
     * @param parentId 父级id(部门id)
     * @return Result 机构集
     */
    @PostMapping(PREFIX + "searchUserDeptTree")
    Result<List<SysOrgDTO>> searchUserDeptTree(@RequestParam(value = "parentId", required = false) Long parentId);

    /**
     * 获取所有部门
     *
     * @return Result 部门集
     */
    @PostMapping(PREFIX + "getDeptAll")
    Result<List<SysDeptDTO>> getDeptAll();

    /**
     * 获取部门集
     *
     * @param parentId 父级id
     * @return Result 部门集
     */
    @PostMapping(PREFIX + "searchByParentId")
    Result<List<SysDeptDTO>> searchByParentId(@RequestParam(value = "parentId", required = false) Long parentId);

    /**
     * 根据部门号查询部门
     *
     * @param deptNo 部门号
     * @return Result 部门集
     */
    @PostMapping(PREFIX + "selectByNo")
    Result<List<SysDeptDTO>> getDeptByNo(@RequestParam("deptNo") String deptNo);

    /*********************增删改  同步****************************/
    /**
     * 单个新增部门
     *
     * @param sysDeptDTO
     * @return
     */
    @PostMapping(PREFIX + "addDept")
    Result<Integer> addDept(@RequestBody SysDeptDTO sysDeptDTO);

    /**
     * 删除部门根据部门id
     *
     * @param deptIds
     * @return
     */
    @PostMapping(PREFIX + "delDeptByDeptId")
    Result<Integer> delDeptByDeptId(@RequestParam("deptIds") Long[] deptIds);

    /**
     * 编辑部门
     *
     * @param sysDeptDTO
     * @return
     */
    @PostMapping(PREFIX + "updateDeptByDeptIdAndLevel")
    Result<Integer> updateDeptByDeptIdAndLevel(@RequestBody SysDeptDTO sysDeptDTO);

    /**
     * 获取所有层级部门
     *
     * @return Result 部门集
     */
    @PostMapping(PREFIX + "getAllLevelDept")
    Result<List<SysDeptDTO>> getAllLevelDept();
}
