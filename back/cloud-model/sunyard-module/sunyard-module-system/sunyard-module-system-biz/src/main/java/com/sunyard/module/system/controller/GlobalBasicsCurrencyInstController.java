package com.sunyard.module.system.controller;

import com.sunyard.framework.common.result.Result;
import com.sunyard.framework.log.annotation.OperationLog;
import com.sunyard.module.system.api.dto.SysOrgDTO;
import com.sunyard.module.system.constant.LogsPrefixConstants;
import com.sunyard.module.system.service.OrgDeptService;
import com.sunyard.module.system.service.OrgInstService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * 全局/组织管理
 *
 * @Author PJW 2021/7/5 16:50
 */
@RestController
@RequestMapping("global")
public class GlobalBasicsCurrencyInstController extends BaseController {
    private static final String BASELOG = LogsPrefixConstants.GLOBAL_INST + "->";
    @Value("${kkOnline.url:http://127.0.0.1:8041}")
    private String kkOnlineUrl;
    @Resource
    private OrgDeptService orgDeptService;
    @Resource
    private OrgInstService orgInstService;

    /**
     * 获得当前机构下部门信息
     *
     * @return result
     */
    @OperationLog(BASELOG + "获得当前机构下部门信息")
    @PostMapping("getDeptEnum")
    public Result getDeptEnum() {
        return Result.success(orgDeptService.getDeptEnum(getToken().getInstId()));
    }

    /**
     * 查询机构树
     *
     * @return result
     */
    @OperationLog(BASELOG + "查询机构树")
    @PostMapping("searchInstTree")
    public Result<List<SysOrgDTO>> searchInstTree() {
        return Result.success(orgInstService.searchInstTree(getToken().getInstId()));
    }

    /**
     * 查询组织树(机构+部门)
     *
     * @return result
     */
    @OperationLog(BASELOG + "查询组织树(机构+部门)")
    @PostMapping("searchOrgTree")
    public Result<List<SysOrgDTO>> searchOrgTree() {
        return Result.success(orgInstService.searchOrgTree(getToken().getInstId(), getToken().getDeptId()));
    }

    /**
     * 查询部门树
     *
     * @return result
     */
    @OperationLog(BASELOG + "查询部门树")
    @PostMapping("searchDeptTree")
    public Result<List<SysOrgDTO>> searchDeptTree() {
        return Result.success(orgDeptService.searchDeptTree(getToken().getInstId()));
    }

    /**
     * 获得登录用户所在部门及下属部门树
     *
     * @return result
     */
    @OperationLog(BASELOG + "获得登录用户部门树")
    @PostMapping("searchUserDeptTree")
    public Result<List<SysOrgDTO>> searchUserDeptTree() {
        return Result.success(orgDeptService.searchUserDeptTree(getToken().getDeptId()));
    }

    /**
     * 获取在线预览服务地址
     *
     * @return result
     */
    @OperationLog(BASELOG + "获取在线预览服务地址")
    @PostMapping("getKkOnlineUrl")
    public Result getKkOnlineUrl() {
        return Result.success(kkOnlineUrl);
    }
}
