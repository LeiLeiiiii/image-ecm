package com.sunyard.module.system.service;


import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.sunyard.framework.common.page.PageForm;
import com.sunyard.framework.common.result.Result;
import com.sunyard.framework.common.util.date.DateUtils;
import com.sunyard.framework.mybatis.util.PageCopyListUtils;
import com.sunyard.module.system.api.dto.SysLogDTO;
import com.sunyard.module.system.api.dto.SysLogLoginDTO;
import com.sunyard.module.system.dto.ApiLogDTO;
import com.sunyard.module.system.dto.SystemLogDTO;
import com.sunyard.module.system.dto.excel.LoginLogModel;
import com.sunyard.module.system.mapper.SysApiLogMapper;
import com.sunyard.module.system.mapper.SysLogLoginMapper;
import com.sunyard.module.system.mapper.SysLogMapper;
import com.sunyard.module.system.po.SysApiLog;
import com.sunyard.module.system.po.SysLog;
import com.sunyard.module.system.po.SysLogLogin;

/**
 * 系统管理-日志管理
 *
 * @author wubingyang
 * @date 2021/7/20 21:44
 */
@Service
public class SysLogService {
    @Resource
    private SysLogMapper mapper;
    @Resource
    private SysApiLogMapper sysApiLogMapper;
    @Resource
    private SysLogLoginMapper sysLogLoginMapper;
    /**
     * 查询日志
     *
     * @param page 分页参数
     * @param log 日志obj
     * @return Result
     */
    public PageInfo search(PageForm page, SysLogDTO log) {
        log.setLogsEndDate(DateUtils.getDayEndTime(log.getLogsEndDate()));
        Integer size = (page.getPageNum() - 1) * page.getPageSize();
        log.setPageSize(page.getPageSize());
        log.setSize(size);
        PageHelper.startPage(page.getPageNum(), page.getPageSize());
        List<SysLogDTO> list = mapper.search(log);
        PageInfo<SysLogDTO> sysLogDTOPageInfo = new PageInfo<>(list);
        PageInfo<SystemLogDTO> pageInfo = PageCopyListUtils.getPageInfo(sysLogDTOPageInfo,
                SystemLogDTO.class);
        pageInfo.getList().forEach(item -> {
            item.setResponseCodeStr(item.getResponseCode().equals(0) ? "成功"
                    : item.getResponseCode().equals(1) ? "失败" : "异常");
        });
        Long l = mapper.selectCounts(log);
        pageInfo.setTotal(l);
        return pageInfo;
    }

    /**
     * 查看日志详情
     *
     * @param id id
     * @return Result
     */
    public SysLog select(Long id) {
        Assert.notNull(id, "参数错误");
        SysLog po = mapper.selectById(id);
        return po;
    }

    /**
     * 查询接口日志
     * @param page 分页参数
     * @param log 日志obj
     * @return Result
     */
    public PageInfo searchApi(PageForm page, SysLogDTO log) {
        Integer size = (page.getPageNum() - 1) * page.getPageSize();
        log.setPageSize(page.getPageSize());
        log.setSize(size);
        PageHelper.startPage(page.getPageNum(), page.getPageSize());
        List<SysApiLog> sysApiLogs = sysApiLogMapper.selectList(
                new LambdaQueryWrapper<SysApiLog>()
                        .like(StringUtils.hasText(log.getRequestDesc()), SysApiLog::getRequestDesc,
                                log.getRequestDesc())
                        .eq(StringUtils.hasText(log.getLogSystem()), SysApiLog::getLogSystem,
                                log.getLogSystem())
                        .eq(null != log.getResponseCode(), SysApiLog::getResponseCode,
                                log.getResponseCode())
                        .like(StringUtils.hasText(log.getRequestIp()), SysApiLog::getRequestIp,
                                log.getRequestIp())
                        .between(!ObjectUtils.isEmpty(log.getLogsStartDate()),
                                SysApiLog::getCreateTime, log.getLogsStartDate(),
                                log.getLogsEndDate())
                        .orderByDesc(SysApiLog::getId));
        Long l = sysApiLogMapper.selectCount(new LambdaQueryWrapper<SysApiLog>()
                .like(StringUtils.hasText(log.getRequestDesc()), SysApiLog::getRequestDesc,
                        log.getRequestDesc())
                .eq(StringUtils.hasText(log.getLogSystem()), SysApiLog::getLogSystem,
                        log.getLogSystem())
                .eq(null != log.getResponseCode(), SysApiLog::getResponseCode,
                        log.getResponseCode())
                .like(StringUtils.hasText(log.getRequestIp()), SysApiLog::getRequestIp,
                        log.getRequestIp())
                .between(!ObjectUtils.isEmpty(log.getLogsStartDate()), SysApiLog::getCreateTime,
                        log.getLogsStartDate(), log.getLogsEndDate()));
        PageInfo<ApiLogDTO> pageInfo = PageCopyListUtils.getPageInfo(new PageInfo<>(sysApiLogs),
                ApiLogDTO.class);
        pageInfo.setTotal(l);
        pageInfo.getList().forEach(item -> {
            item.setResponseCodeStr(item.getResponseCode().equals(0) ? "成功"
                    : item.getResponseCode().equals(1) ? "失败" : "异常");
        });
        return pageInfo;
    }

    /**
     * 导出系统日志
     * @param ids id集
     * @return Result
     */
    public List<SystemLogDTO> exportSysLog(Long[] ids) {
        List<SysLogDTO> sysLogDTOS = mapper.searchListByIds(ids);
        List<SystemLogDTO> systemLogDTOS = PageCopyListUtils.copyListProperties(sysLogDTOS,
                SystemLogDTO.class);
        systemLogDTOS.forEach(item -> {
            item.setResponseCodeStr(item.getResponseCode().equals(0) ? "成功"
                    : item.getResponseCode().equals(1) ? "失败" : "异常");
        });
        return systemLogDTOS;
    }

    /**
     * 导出接口日志
     * @param ids id集
     * @return Result
     */
    public List<ApiLogDTO> exportApiLog(Long[] ids) {
        List<SysApiLog> sysApiLogs = sysApiLogMapper.selectList(new LambdaQueryWrapper<SysApiLog>()
                .in(null != ids && ids.length > 0, SysApiLog::getId, ids)
                .orderByDesc(SysApiLog::getCreateTime));
        List<ApiLogDTO> apiLogDTOS = PageCopyListUtils.copyListProperties(sysApiLogs,
                ApiLogDTO.class);
        apiLogDTOS.forEach(item -> {
            item.setResponseCodeStr(item.getResponseCode().equals(0) ? "成功"
                    : item.getResponseCode().equals(1) ? "失败" : "异常");
        });
        return apiLogDTOS;
    }

    /**
     * 查询登录日志
     * @param pageForm 分页参数
     * @return Result
     */
    public Result searchLogin(PageForm pageForm, SysLogLoginDTO log) {
        Integer size = (pageForm.getPageNum() - 1) * pageForm.getPageSize();
        log.setPageSize(pageForm.getPageSize());
        log.setSize(size);
        log.setLoginDateEnd(DateUtils.getDayEndTime(log.getLoginDateEnd()));
        PageHelper.startPage(pageForm.getPageNum(), pageForm.getPageSize());
        List<SysLogLogin> sysLogLogins = sysLogLoginMapper.selectList(new LambdaQueryWrapper<SysLogLogin>()
                .eq(StringUtils.hasText(log.getUserName()), SysLogLogin::getUserName,
                        log.getUserName())
                .eq(null != log.getLoginStatus(), SysLogLogin::getLoginStatus, log.getLoginStatus())
                .between(!ObjectUtils.isEmpty(log.getLoginDateStart()), SysLogLogin::getLoginTime,
                        log.getLoginDateStart(), log.getLoginDateEnd())
                .orderByDesc(SysLogLogin::getId));
        PageInfo<SysLogLoginDTO> pageInfo = PageCopyListUtils
                .getPageInfo(new PageInfo<>(sysLogLogins), SysLogLoginDTO.class);
        Long l = sysLogLoginMapper.selectCount(new LambdaQueryWrapper<SysLogLogin>()
                .eq(StringUtils.hasText(log.getUserName()), SysLogLogin::getUserName,
                        log.getUserName())
                .eq(null != log.getLoginStatus(), SysLogLogin::getLoginStatus, log.getLoginStatus())
                .between(!ObjectUtils.isEmpty(log.getLoginDateStart()), SysLogLogin::getLoginTime,
                        log.getLoginDateStart(), log.getLoginDateEnd())
                .orderByDesc(SysLogLogin::getLoginTime));
        pageInfo.setTotal(l);
        pageInfo.getList().forEach(item -> {
            item.setLoginStatusStr(item.getLoginStatus().equals(0) ? "成功" : "失败");
        });
        return Result.success(pageInfo);
    }

    /**
     * 导出登录日志
     * @param ids id集
     * @return Result
     */
    public List<LoginLogModel> exportLoginLog(Long[] ids) {
        List<SysLogLogin> sysLogLogins = sysLogLoginMapper.selectList(new LambdaQueryWrapper<SysLogLogin>()
                .in(null != ids && ids.length > 0, SysLogLogin::getId, ids)
                .orderByDesc(SysLogLogin::getCreateTime));
        List<LoginLogModel> loginLogModels = PageCopyListUtils.copyListProperties(sysLogLogins,
                LoginLogModel.class);
        loginLogModels.forEach(item -> {
            item.setLoginStatusStr(item.getLoginStatus().equals(0) ? "成功"
                    : item.getLoginStatus().equals(1) ? "失败" : "异常");
        });
        return loginLogModels;
    }
}
