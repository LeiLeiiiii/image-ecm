package com.sunyard.module.system.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import com.sunyard.framework.common.util.AssertUtils;
import com.sunyard.module.system.api.dto.SysLogDTO;
import com.sunyard.module.system.api.dto.SysLogLoginDTO;
import com.sunyard.module.system.constant.StateConstants;
import com.sunyard.module.system.dto.ApiLogDTO;
import com.sunyard.module.system.dto.SystemLogDTO;
import com.sunyard.module.system.mapper.SysApiLogMapper;
import com.sunyard.module.system.po.SysApiLog;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.sunyard.framework.common.page.PageForm;
import com.sunyard.framework.common.result.Result;
import com.sunyard.framework.common.util.date.DateUtils;
import com.sunyard.framework.mybatis.util.PageCopyListUtils;
import com.sunyard.module.system.dto.SysUserAuditDTO;
import com.sunyard.module.system.mapper.SysLogLoginMapper;
import com.sunyard.module.system.mapper.SysLogMapper;
import com.sunyard.module.system.mapper.SysUserAuditMapper;
import com.sunyard.module.system.mapper.SysUserMapper;
import com.sunyard.module.system.po.SysLog;
import com.sunyard.module.system.po.SysLogLogin;
import com.sunyard.module.system.po.SysUser;
import com.sunyard.module.system.po.SysUserAudit;
import com.sunyard.module.system.vo.SysUserAuditVO;

/**
 * @author P-JWei
 * @date 2023/9/25 9:31:52
 * @title
 * @description
 */
@Service
public class SysUserAuditService {

    @Resource
    private SysApiLogMapper sysApiLogMapper;
    @Resource
    private SysLogMapper sysLogMapper;
    @Resource
    private SysUserMapper sysUserMapper;
    @Resource
    private SysLogLoginMapper sysLogLoginMapper;
    @Resource
    private SysUserAuditMapper sysUserAuditMapper;

    /**
     * 查询审计记录
     * @param pageForm 分页参数
     * @param vo 审计obj
     * @return Result
     */
    public Result search(PageForm pageForm, SysUserAuditVO vo) {
        LambdaQueryWrapper<SysUserAudit> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(vo.getUserName())) {
            List<SysUser> sysUsers = sysUserMapper.selectList(
                    new LambdaQueryWrapper<SysUser>().like(SysUser::getName, vo.getUserName()));
            if (CollectionUtils.isEmpty(sysUsers)) {
                return Result.success(sysUsers);
            }
            List<Long> userIds = sysUsers.stream().map(SysUser::getUserId)
                    .collect(Collectors.toList());
            wrapper.in(SysUserAudit::getUserId, userIds);
        }
        PageHelper.startPage(pageForm.getPageNum(), pageForm.getPageSize());
        vo.setAuditEndTime(DateUtils.getDayEndTime(vo.getAuditEndTime()));
        List<SysUserAudit> sysUserAudits = sysUserAuditMapper.selectList(wrapper
                .between(!ObjectUtils.isEmpty(vo.getAuditStartTime()), SysUserAudit::getCreateTime,
                        vo.getAuditStartTime(), vo.getAuditEndTime())
                .orderByDesc(SysUserAudit::getCreateTime));

        return Result.success(getExtendPageInfo(sysUserAudits));
    }

    /**
     * 新增审计记录
     * @param vo 审计obj
     * @return Result
     */
    public Result addAudit(SysUserAuditVO vo) {
        Assert.notNull(vo.getUserName(), "参数错误");
        Assert.notNull(vo.getAuditStartTime(), "参数错误");
        Assert.notNull(vo.getAuditEndTime(), "参数错误");
        vo.setAuditEndTime(DateUtils.getDayEndTime(vo.getAuditEndTime()));
        List<SysUser> sysUsers = sysUserMapper.selectList(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getName, vo.getUserName()));
        Assert.isTrue(sysUsers.size() > 0, "参数错误，无指定用户信息");
        //统计时间区间内，此用户的后台访问情况
        List<SysLog> sysLogs = sysLogMapper.selectList(new LambdaQueryWrapper<SysLog>()
                .eq(SysLog::getUserId, sysUsers.get(0).getUserId())
                .between(!ObjectUtils.isEmpty(vo.getAuditStartTime()), SysLog::getCreateTime,
                        vo.getAuditStartTime(), vo.getAuditEndTime()));
        //统计时间区间内，此用户的接口访问情况
        List<SysApiLog> sysApiLogs = sysApiLogMapper.selectList(new LambdaQueryWrapper<SysApiLog>()
                .eq(SysApiLog::getUserId, sysUsers.get(0).getUserId())
                .between(!ObjectUtils.isEmpty(vo.getAuditStartTime()), SysApiLog::getCreateTime,
                        vo.getAuditStartTime(), vo.getAuditEndTime()));
        //统计时间区间内，此用户的登录成功、出错的情况
        List<SysLogLogin> sysLogLogins = sysLogLoginMapper
                .selectList(new LambdaQueryWrapper<SysLogLogin>()
                        .eq(SysLogLogin::getUserName, sysUsers.get(0).getLoginName())
                        .between(!ObjectUtils.isEmpty(vo.getAuditStartTime()),
                                SysLogLogin::getCreateTime, vo.getAuditStartTime(),
                                vo.getAuditEndTime()));
        //统计登录成功次数和失败次数
        List<SysLogLogin> loginNum = sysLogLogins.stream()
                .filter(s -> StateConstants.ZERO.equals(s.getLoginStatus())).collect(Collectors.toList());
        Integer loginFalseNum;
        if(CollectionUtils.isEmpty(loginNum)){
            loginFalseNum=0;
        }else {
            loginFalseNum = sysLogLogins.size() - loginNum.size();
        }
        /*//找到请求对多的url地址
        String firstApiUrl = sysLogs.stream().map(SysLog::getRequestUrl)
                .collect(Collectors.toList()).stream()
                .collect(Collectors.groupingBy(e -> e, Collectors.counting())).entrySet().stream()
                .max(Map.Entry.comparingByValue()).map(Map.Entry::getKey).orElse(null);*/
        SysUserAudit sysUserAudit = new SysUserAudit();
        sysUserAudit.setUserId(sysUsers.get(0).getUserId());
        sysUserAudit.setAuditStartTime(vo.getAuditStartTime());
        sysUserAudit.setAuditEndTime(vo.getAuditEndTime());
        sysUserAudit.setApiNum(sysApiLogs.size());
        sysUserAudit.setSysLogNum(sysLogs.size());
        sysUserAudit.setLoginNum(loginNum.size());
        sysUserAudit.setLoginFalseNum(loginFalseNum);
//        sysUserAudit.setFirstApiUrl(firstApiUrl);
        sysUserAuditMapper.insert(sysUserAudit);
        //如果连续输登录失败视为敏感行为
        //如果连续访问相同的接口次数过多视为敏感行为
        return Result.success(sysUserAudit);
    }

    /**
     * 转换pageInfo
     * @param sysUserAudits 对象
     * @return Result
     */
    private PageInfo getExtendPageInfo(List<SysUserAudit> sysUserAudits) {
        PageInfo<SysUserAudit> sysUserAuditPageInfo = new PageInfo<>(sysUserAudits);
        List<SysUser> sysUsers = sysUserMapper.selectList(null);
        Map<Long, String> sysUserMap = sysUsers.stream()
                .collect(Collectors.toMap(SysUser::getUserId, SysUser::getName));
        PageInfo<SysUserAuditDTO> resultPage = new PageInfo<>();
        List<SysUserAuditDTO> sysApiAuthDTOList = PageCopyListUtils
                .copyListProperties(sysUserAuditPageInfo.getList(), SysUserAuditDTO.class);
        sysApiAuthDTOList.forEach(i -> i.setUserName(sysUserMap.get(i.getUserId())));
        BeanUtils.copyProperties(sysUserAuditPageInfo, resultPage);
        resultPage.setList(sysApiAuthDTOList);
        return resultPage;
    }

    /**
     * 查询审计记录
     * @return Result
     */
    public Result getInfo(Long id) {
        Assert.notNull(id, "参数错误");
        SysUserAudit sysUserAudit = sysUserAuditMapper.selectById(id);
        SysUserAuditDTO sysUserAuditDTO = new SysUserAuditDTO();
        BeanUtils.copyProperties(sysUserAudit,sysUserAuditDTO);
        List<SysUser> sysUsers = sysUserMapper.selectList(null);
        Map<Long, String> sysUserMap = sysUsers.stream()
                .collect(Collectors.toMap(SysUser::getUserId, SysUser::getName));
        sysUserAuditDTO.setUserName(sysUserMap.get(sysUserAuditDTO.getUserId()));
        return Result.success(sysUserAuditDTO);
    }

    public Result getLoginInfo(Long id,PageForm pageForm) {
        Assert.notNull(id, "参数错误");
        SysUserAudit sysUserAudit = sysUserAuditMapper.selectById(id);
        AssertUtils.isNull(sysUserAudit,"参数错误");
        SysUser sysUser = sysUserMapper.selectById(sysUserAudit.getUserId());
        AssertUtils.isNull(sysUser,"未找到用户信息");
        PageHelper.startPage(pageForm.getPageNum(), pageForm.getPageSize());
        List<SysLogLogin> sysLogLogins = sysLogLoginMapper.selectList(new LambdaQueryWrapper<SysLogLogin>()
                .eq( SysLogLogin::getUserName,sysUser.getLoginName())
                .between(SysLogLogin::getLoginTime,sysUserAudit.getAuditStartTime(), sysUserAudit.getAuditEndTime())
                .orderByDesc(SysLogLogin::getLoginTime));
        PageInfo<SysLogLoginDTO> pageInfo = PageCopyListUtils
                .getPageInfo(new PageInfo<>(sysLogLogins), SysLogLoginDTO.class);
        Long l = sysLogLoginMapper.selectCount(new LambdaQueryWrapper<SysLogLogin>()
                .eq( SysLogLogin::getUserName,sysUser.getLoginName())
                .between(SysLogLogin::getLoginTime,sysUserAudit.getAuditStartTime(), sysUserAudit.getAuditEndTime())
                .orderByDesc(SysLogLogin::getLoginTime));
        pageInfo.setTotal(l);
        pageInfo.getList().forEach(item -> {
            item.setLoginStatusStr(item.getLoginStatus().equals(0) ? "成功" : "失败");
        });
        return Result.success(pageInfo);
    }

    public Result getApiInfo(Long id,PageForm pageForm) {
        Assert.notNull(id, "参数错误");
        SysUserAudit sysUserAudit = sysUserAuditMapper.selectById(id);

        PageHelper.startPage(pageForm.getPageNum(), pageForm.getPageSize());
        List<SysApiLog> sysApiLogs = sysApiLogMapper.selectList(
                new LambdaQueryWrapper<SysApiLog>()
                        .eq(SysApiLog::getUserId,sysUserAudit.getUserId())
                        .between(SysApiLog::getCreateTime, sysUserAudit.getAuditStartTime(),
                                sysUserAudit.getAuditEndTime())
                        .orderByDesc(SysApiLog::getId));
        Long l = sysApiLogMapper.selectCount(new LambdaQueryWrapper<SysApiLog>()
                .eq(SysApiLog::getUserId,sysUserAudit.getUserId())
                .between(SysApiLog::getCreateTime, sysUserAudit.getAuditStartTime(),
                        sysUserAudit.getAuditEndTime())
                .orderByDesc(SysApiLog::getId));
        PageInfo<ApiLogDTO> pageInfo = PageCopyListUtils.getPageInfo(new PageInfo<>(sysApiLogs),
                ApiLogDTO.class);
        pageInfo.setTotal(l);
        pageInfo.getList().forEach(item -> {
            item.setResponseCodeStr(item.getResponseCode().equals(0) ? "成功"
                    : item.getResponseCode().equals(1) ? "失败" : "异常");
        });
        return Result.success(pageInfo);
    }

    public Result getSysLogInfo(Long id,PageForm pageForm) {
        Assert.notNull(id, "参数错误");
        SysUserAudit sysUserAudit = sysUserAuditMapper.selectById(id);
        AssertUtils.isNull(sysUserAudit,"参数错误");
        SysUser sysUser = sysUserMapper.selectById(sysUserAudit.getUserId());
        AssertUtils.isNull(sysUser,"未找到用户信息");
        SysLogDTO log = new SysLogDTO();
        log.setUserName(sysUser.getName());
        log.setLogsStartDate(sysUserAudit.getAuditStartTime());
        log.setLogsEndDate(DateUtils.getDayEndTime(sysUserAudit.getAuditEndTime()));
        Integer size = (pageForm.getPageNum() - 1) * pageForm.getPageSize();
        log.setPageSize(pageForm.getPageSize());
        log.setSize(size);
        List<SysLogDTO> list = sysLogMapper.search(log);
        PageInfo<SysLogDTO> sysLogDTOPageInfo = new PageInfo<>(list);
        PageInfo<SystemLogDTO> pageInfo = PageCopyListUtils.getPageInfo(sysLogDTOPageInfo,
                SystemLogDTO.class);
        pageInfo.getList().forEach(item -> {
            item.setResponseCodeStr(item.getResponseCode().equals(0) ? "成功"
                    : item.getResponseCode().equals(1) ? "失败" : "异常");
        });
        Long l = sysLogMapper.selectCounts(log);
        pageInfo.setTotal(l);
        return Result.success(pageInfo);
    }

    public List<SysUserAuditDTO> searchForExport(Long[] ids){
        List<SysUserAudit> sysUserAudits = sysUserAuditMapper.selectList(new LambdaQueryWrapper<SysUserAudit>()
                .in(null != ids && ids.length > 0, SysUserAudit::getId, ids)
                .orderByDesc(SysUserAudit::getCreateTime));
        List<SysUserAuditDTO> sysUserAuditDTOS = PageCopyListUtils.copyListProperties(sysUserAudits,
                SysUserAuditDTO.class);
        List<SysUser> sysUsers = sysUserMapper.selectList(null);
        Map<Long, String> sysUserMap = sysUsers.stream()
                .collect(Collectors.toMap(SysUser::getUserId, SysUser::getName));
        sysUserAuditDTOS.forEach(i -> i.setUserName(sysUserMap.get(i.getUserId())));
        return sysUserAuditDTOS;
    }
}
