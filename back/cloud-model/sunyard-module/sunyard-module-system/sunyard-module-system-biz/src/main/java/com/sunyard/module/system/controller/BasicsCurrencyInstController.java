package com.sunyard.module.system.controller;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.sunyard.framework.common.excel.util.ExcelUtils;
import com.sunyard.framework.common.exception.SunyardException;
import com.sunyard.framework.common.result.Result;
import com.sunyard.framework.common.result.ResultCode;
import com.sunyard.framework.log.annotation.OperationLog;
import com.sunyard.module.system.api.dto.SysDeptExportDTO;
import com.sunyard.module.system.api.dto.SysInstExportDTO;
import com.sunyard.module.system.api.dto.SysUserExportDTO;
import com.sunyard.module.system.constant.LogsPrefixConstants;
import com.sunyard.module.system.dto.excel.SysDeptModel;
import com.sunyard.module.system.dto.excel.SysInstModel;
import com.sunyard.module.system.dto.excel.SysUserModel;
import com.sunyard.module.system.po.SysDept;
import com.sunyard.module.system.po.SysInst;
import com.sunyard.module.system.service.OrgDeptService;
import com.sunyard.module.system.service.OrgInstService;
import com.sunyard.module.system.service.OrgUserService;

import lombok.extern.slf4j.Slf4j;

/**
 * 基础管理/通用管理/组织管理
 *
 * @Author PJW 2021/7/5 16:50
 */
@Slf4j
@RestController
@RequestMapping("basics/currency/org")
public class BasicsCurrencyInstController extends BaseController {
    private static final String BASELOG = LogsPrefixConstants.MENU_CURRENCY + "-组织管理->";
    @Resource
    private OrgInstService service;
    @Resource
    private OrgDeptService orgDeptService;
    @Resource
    private OrgUserService orgUserService;

    /**
     * 添加机构
     *
     * @param sysInst 机构对象
     * @return result
     */
    @OperationLog(BASELOG + "添加机构")
    @PostMapping("inst/add")
    public Result addInst(@RequestBody SysInst sysInst) {
        service.add(sysInst);
        return Result.success(true);
    }

    /**
     * 更新机构
     *
     * @param sysInst 更新机构对象
     * @return result
     */
    @OperationLog(BASELOG + "更新机构")
    @PostMapping("inst/update")
    public Result updateInst(@RequestBody SysInst sysInst) {
        service.update(sysInst);
        return Result.success(true);
    }

    /**
     * 删除机构
     *
     * @param instId 机构instId
     * @return result
     */
    @OperationLog(BASELOG + "删除机构")
    @PostMapping("inst/delete")
    public Result deleteInst(Long instId) {
        service.delete(instId);
        return Result.success(true);
    }

    /**
     * 查询机构详情
     *
     * @param instId 机构instId
     * @return result
     */
    @OperationLog(BASELOG + "查询机构详情")
    @PostMapping("inst/select")
    public Result selectInst(Long instId) {
        return Result.success(service.select(instId));
    }

    /**
     * 添加部门
     *
     * @param dept 部门对象
     * @return result
     */
    @OperationLog(BASELOG + "添加部门")
    @PostMapping("dept/add")
    public Result addDept(@RequestBody SysDept dept) {
        return Result.success(orgDeptService.add(dept));
    }

    /**
     * 更新部门
     *
     * @param dept 更新部门对象
     * @return result
     */
    @OperationLog(BASELOG + "更新部门")
    @PostMapping("dept/update")
    public Result updateDept(@RequestBody SysDept dept) {
        orgDeptService.update(dept);
        return Result.success(true);
    }

    /**
     * 删除部门
     *
     * @param deptId 部门id
     * @return result
     */
    @OperationLog(BASELOG + "删除部门")
    @PostMapping("dept/delete")
    public Result deleteDept(Long deptId) {
        orgDeptService.delete(deptId);
        return Result.success(true);
    }

    /**
     * 查询部门详情
     *
     * @param deptId 部门id
     * @return result
     */
    @OperationLog(BASELOG + "查询部门详情")
    @PostMapping("dept/selectDept")
    public Result selectDept(Long deptId) {
        return Result.success(orgDeptService.selectById(deptId));
    }

    /**
     * 组织架构导入
     *
     * @param file 文件
     * @return result
     */
    @OperationLog(BASELOG + "组织架构导入")
    @PostMapping("import")
    public Result orgImport(@RequestParam MultipartFile file) {
        InputStream inputStream = null;
        try {
            inputStream = file.getInputStream();
            List<Map<Integer, String>> instMaps = ExcelUtils.syncRead(inputStream, 0, 1);

            // 重新获取输入流
            inputStream = file.getInputStream();
            List<Map<Integer, String>> deptMaps = ExcelUtils.syncRead(inputStream, 1, 1);

            inputStream = file.getInputStream();
            List<Map<Integer, String>> userMaps = ExcelUtils.syncRead(inputStream, 2, 1);

            service.instImport(instMaps);
            orgDeptService.deptImport(deptMaps);
            orgUserService.userImport(userMaps);
            return Result.success(true);
        } catch (IOException e) {
            log.error("系统异常", e);
            throw new SunyardException(ResultCode.PARAM_ERROR, "组织架构导入异常");
        } finally {
            // 关闭流
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    log.error("关闭输入流失败", e);
                }
            }
        }
    }

    /**
     * 模板下载
     *
     * @throws IOException IO异常
     */
    @OperationLog(BASELOG + "模板下载")
    @GetMapping("template/download")
    public void downloadTemplate() throws IOException {
        ExcelUtils.writeWithSheetsWeb(response, "组织架构模板").writeModel(SysInstModel.class, null, "机构")
                .writeModel(SysDeptModel.class, null, "部门")
                .writeModel(SysUserModel.class, null, "用户").finish();
    }

    /**
     * 组织架构导出
     *
     * @throws IOException IO异常
     */
    @OperationLog(BASELOG + "组织架构导出")
    @GetMapping("export")
    public void export() throws IOException {
        List<SysInstExportDTO> instList = service.exportList();
        List<SysDeptExportDTO> deptList = orgDeptService.exportList();
        List<SysUserExportDTO> userList = orgUserService.exportList();

        ExcelUtils.writeWithSheetsWeb(response, "组织架构")
                .writeModel(SysInstModel.class, instList, "机构")
                .writeModel(SysDeptModel.class, deptList, "部门")
                .writeModel(SysUserModel.class, userList, "用户").finish();
    }

}
