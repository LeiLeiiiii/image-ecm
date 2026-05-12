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
import com.sunyard.module.system.api.dto.SysLogLoginDTO;
import com.sunyard.module.system.constant.LogsPrefixConstants;
import com.sunyard.module.system.dto.excel.LoginLogModel;
import com.sunyard.module.system.service.SysLogService;

/**
 * 基础管理/日志管理/登录日志
 *
 * @author wubingyang
 * @date 2021/7/21 9:00
 */
@RestController
@RequestMapping("basics/log/login")
public class BasicsLogLoginController extends BaseController {
    private static final String BASELOG = LogsPrefixConstants.MENU_LOG + "-登录日志->";
    @Resource
    private SysLogService sysLogService;

    /**
     * 查询登录日志
     *
     * @param pageForm       分页
     * @param sysLogLoginDTO 日志obj
     * @return result
     */
    @OperationLog(BASELOG + "查询登录日志")
    @PostMapping("searchLogin")
    public Result searchLogin(PageForm pageForm, SysLogLoginDTO sysLogLoginDTO) {
        return sysLogService.searchLogin(pageForm, sysLogLoginDTO);
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
        List<LoginLogModel> sysLogs = sysLogService.exportLoginLog(ids);
        ExcelUtils.writeWithSheetsWeb(response, "登录日志")
                .writeModel(LoginLogModel.class, sysLogs, "登录日志").finish();
    }
}
