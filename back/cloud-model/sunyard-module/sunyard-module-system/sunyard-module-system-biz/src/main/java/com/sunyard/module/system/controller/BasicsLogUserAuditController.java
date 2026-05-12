package com.sunyard.module.system.controller;

import java.io.IOException;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sunyard.framework.common.excel.util.ExcelUtils;
import com.sunyard.framework.common.page.PageForm;
import com.sunyard.framework.common.result.Result;
import com.sunyard.framework.log.annotation.OperationLog;
import com.sunyard.module.system.constant.LogsPrefixConstants;
import com.sunyard.module.system.dto.SysUserAuditDTO;
import com.sunyard.module.system.dto.excel.SysUserAuditModel;
import com.sunyard.module.system.service.SysUserAuditService;
import com.sunyard.module.system.vo.SysUserAuditVO;

import cn.hutool.core.bean.BeanUtil;

/**
 * @author P-JWei
 * @date 2023/9/22 17:37:16
 * @title 用户审计controller
 * @description
 */
@RestController
@RequestMapping("basics/log/userAudit")
public class BasicsLogUserAuditController extends BaseController {
    private static final String BASELOG = LogsPrefixConstants.MENU_CURRENCY + "-用户审计->";

    @Resource
    private SysUserAuditService sysUserAuditService;

    /**
     * 查询审计记录
     *
     * @param pageForm 分页参数
     * @param vo       查询obj
     * @return result
     */
    @OperationLog(BASELOG + "查询审计记录")
    @PostMapping("search")
    public Result search(PageForm pageForm, SysUserAuditVO vo) {
        return sysUserAuditService.search(pageForm, vo);
    }

    /**
     * 新增审计记录
     *
     * @param vo 插入obj
     * @return result
     */
    @OperationLog(BASELOG + "新增审计记录")
    @PostMapping("addAudit")
    public Result addAudit(SysUserAuditVO vo) {
        return sysUserAuditService.addAudit(vo);
    }

    /**
     * 查询审计记录
     *
     * @return result
     */
    @OperationLog(BASELOG + "查看审计详情")
    @PostMapping("getInfo")
    public Result getInfo(Long id) {
        return sysUserAuditService.getInfo(id);
    }

    /**
     * 审计用户登录
     *
     * @return result
     */
    @OperationLog(BASELOG + "审计用户登录")
    @PostMapping("getLoginInfo")
    public Result getLoginInfo(PageForm pageForm, Long id) {
        return sysUserAuditService.getLoginInfo(id, pageForm);
    }

    /**
     * 查询审计记录
     *
     * @return result
     */
    @OperationLog(BASELOG + "查询审计记录")
    @PostMapping("getApiInfo")
    public Result getApiInfo(PageForm pageForm, Long id) {
        return sysUserAuditService.getApiInfo(id, pageForm);
    }

    /**
     * 查询审计记录
     *
     * @return result
     */
    @OperationLog(BASELOG + "查询审计记录")
    @PostMapping("getSysLogInfo")
    public Result getSysLogInfo(PageForm pageForm, Long id) {
        return sysUserAuditService.getSysLogInfo(id, pageForm);
    }

    /**
     * 导出
     *
     * @param ids 日志id集
     * @throws IOException 异常
     */
    @OperationLog(BASELOG + "日志导出")
    @PostMapping("export")
    public void export(Long[] ids) throws IOException {
        List<SysUserAuditDTO> sysUserAuditDTOS = sysUserAuditService.searchForExport(ids);
        List<SysUserAuditModel> sysUserAuditModels = BeanUtil.copyToList(sysUserAuditDTOS,
                SysUserAuditModel.class);
        ExcelUtils.writeWithSheetsWeb(response, "用户审计")
                .writeModel(SysUserAuditModel.class, sysUserAuditModels, "用户审计").finish();
    }
}
