package com.sunyard.module.system.api;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.beans.BeanUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RestController;

import com.sunyard.framework.common.result.Result;
import com.sunyard.framework.log.annotation.CloseLog;
import com.sunyard.module.system.api.dto.SysApiLogDTO;
import com.sunyard.module.system.api.dto.SysLogDTO;
import com.sunyard.module.system.api.dto.SysLogLoginDTO;
import com.sunyard.module.system.constant.StateConstants;
import com.sunyard.module.system.mapper.SysApiLogMapper;
import com.sunyard.module.system.mapper.SysLogLoginMapper;
import com.sunyard.module.system.mapper.SysLogMapper;
import com.sunyard.module.system.po.SysApiLog;
import com.sunyard.module.system.po.SysLog;
import com.sunyard.module.system.po.SysLogLogin;

/**
 * 组织管理-日志管理
 *
 * @author wubingyang
 * @date 2021/7/21 9:00
 */
@CloseLog
@RestController
public class LogApiImpl implements LogApi {
    @Resource
    private SysLogMapper mapper;
    @Resource
    private SysApiLogMapper sysApiLogMapper;
    @Resource
    private SysLogLoginMapper sysLogLoginMapper;

    @Async("LogThreadPool")
    @Override
    public Result<Boolean> add(SysLogDTO sysLog) {
        Assert.notNull(sysLog, "参数错误!");
        SysLog sysLog1 = new SysLog();
        BeanUtils.copyProperties(sysLog, sysLog1);
        mapper.insert(sysLog1);
        return Result.success(true);
    }

    @Async("LogThreadPool")
    @Override
    public Result<Boolean> addSysApiLog(SysApiLogDTO log) {
        Assert.notNull(log, "参数错误!");
        SysApiLog sysApiLog = new SysApiLog();
        BeanUtils.copyProperties(log, sysApiLog);
        sysApiLogMapper.insert(sysApiLog);
        return Result.success(true);
    }

    @Override
    public Result<List<SysLogDTO>> selectSysApiLog(SysLogDTO log) {
        List<SysLogDTO> logList =
                mapper.searchList(log);
        return Result.success(logList);
    }

    /**
     * 报表统计
     *
     * @param
     * @return Result
     */
    @Override
    public Result<List<SysLogDTO>> countBySysApiLog(SysLogDTO sysLogDTO) {
        List<SysLogDTO> sysLogDtos = new ArrayList<>();
        if (StateConstants.LOG_REQUEST_DESC.equals(sysLogDTO.getGroupStr())) {
            sysLogDtos = mapper.countByDesc(
                    sysLogDTO.getUserIds(),
                    sysLogDTO.getLogsStartDate(),
                    sysLogDTO.getLogsEndDate(),
                    sysLogDTO.getGroupStr()
            );
        } else if (StateConstants.LOG_REQUEST_PARAMS.equals(sysLogDTO.getGroupStr())) {
            sysLogDtos = mapper.searchByParams(
                    sysLogDTO.getUserIds(),
                    sysLogDTO.getLogsStartDate(),
                    sysLogDTO.getLogsEndDate(),
                    sysLogDTO.getGroupStr()
            );
        }
        return Result.success(sysLogDtos);
    }

    @Async("LogThreadPool")
    @Override
    public Result<Boolean> addLogin(SysLogLoginDTO sysLogDTO) {
        Assert.notNull(sysLogDTO, "参数错误!");
        SysLogLogin sysLog = new SysLogLogin();
        BeanUtils.copyProperties(sysLogDTO, sysLog);
        sysLogLoginMapper.insert(sysLog);
        return Result.success(true);
    }
}
