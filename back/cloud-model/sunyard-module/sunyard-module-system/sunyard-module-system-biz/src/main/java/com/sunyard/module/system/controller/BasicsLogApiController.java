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
import com.sunyard.module.system.dto.ApiLogDTO;
import com.sunyard.module.system.dto.excel.ApiLogModel;
import com.sunyard.module.system.service.SysLogService;

import cn.hutool.core.bean.BeanUtil;

/**
 * 基础管理/日志管理/接口日志
 *
 * @author wubingyang
 * @date 2021/7/21 9:00
 */
@RestController
@RequestMapping("basics/log/api")
public class BasicsLogApiController extends BaseController {
    private static final String BASELOG = LogsPrefixConstants.MENU_LOG + "-接口日志->";
    @Resource
    private SysLogService sysLogService;

    /**
     * 查询接口日志
     *
     * @param page 分页参数
     * @param log  日志obj
     * @return result
     */
    @OperationLog(BASELOG + "查询接口日志")
    @PostMapping("searchApi")
    public Result searchApi(PageForm page, SysLogDTO log) {
        return Result.success(sysLogService.searchApi(page, log));
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
        List<ApiLogDTO> sysApiLogs = sysLogService.exportApiLog(ids);
        List<ApiLogModel> apiLogModels = BeanUtil.copyToList(sysApiLogs, ApiLogModel.class);
        ExcelUtils.writeWithSheetsWeb(response, "接口日志")
                .writeModel(ApiLogModel.class, apiLogModels, "接口日志").finish();
    }
}
