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
import com.sunyard.module.system.api.dto.SysLogDTO;
import com.sunyard.module.system.constant.LogsPrefixConstants;
import com.sunyard.module.system.dto.SystemLogDTO;
import com.sunyard.module.system.dto.excel.SystemLogModel;
import com.sunyard.module.system.service.SysLogService;

import cn.hutool.core.bean.BeanUtil;

/**
 * 基础管理/日志管理/系统日志
 *
 * @author wubingyang
 * @date 2021/7/21 9:00
 */
@RestController
@RequestMapping("basics/log/sys")
public class BasicsLogSysController extends BaseController {
    private static final String BASELOG = LogsPrefixConstants.MENU_CURRENCY + "-系统日志->";
    @Resource
    private SysLogService sysLogService;

    /**
     * 查询日志
     *
     * @param page 分页参数
     * @param log  日志obj
     * @return result
     */
    @OperationLog(BASELOG + "查询日志")
    @PostMapping("search")
    public Result search(PageForm page, SysLogDTO log) {
        return Result.success(sysLogService.search(page, log));
    }

    /**
     * 查看日志详情
     *
     * @param id 日志id
     * @return Result
     */
    @OperationLog(BASELOG + "查看日志详情")
    @PostMapping("select")
    public Result select(Long id) {
        return Result.success(sysLogService.select(id));
    }

    /**
     * 日志导出
     *
     * @param ids 日志id集
     * @throws IOException 异常
     */
    @OperationLog(BASELOG + "日志导出")
    @PostMapping("export")
    public void export(Long[] ids) throws IOException {
        List<SystemLogDTO> sysLogs = sysLogService.exportSysLog(ids);
        List<SystemLogModel> systemLogModels = BeanUtil.copyToList(sysLogs, SystemLogModel.class);
        ExcelUtils.writeWithSheetsWeb(response, "系统日志")
                .writeModel(SystemLogModel.class, systemLogModels, "系统日志").finish();
    }
}
