package com.sunyard.module.system.api;

import com.sunyard.framework.common.result.Result;
import com.sunyard.module.system.api.dto.SysApiLogDTO;
import com.sunyard.module.system.api.dto.SysLogDTO;
import com.sunyard.module.system.api.dto.SysLogLoginDTO;
import com.sunyard.module.system.constant.ApiConstants;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * @description: 调用授权微服务，验证连接是否授权
 * @author: raochangmei
 * @time: 2022-10-12
 */
@FeignClient(value = ApiConstants.NAME)
public interface LogApi {

    String PREFIX = ApiConstants.PREFIX + "/log/";

    /**
     * 新增日志
     *
     * @param log 日志obj
     * @return Result
     */
    @PostMapping(PREFIX + "add")
    Result<Boolean> add(@RequestBody SysLogDTO log);

    /**
     * 新增api日志
     *
     * @param log 日志obj
     * @return Result
     */
    @PostMapping(PREFIX + "addSysApiLog")
    Result<Boolean> addSysApiLog(@RequestBody SysApiLogDTO log);

    /**
     * 查询系统日志--现在只有档案系统报表用到
     * 
     * @param log 日志obj
     * @return Result
     */
    @PostMapping(PREFIX + "selectSysApiLog")
    Result<List<SysLogDTO>> selectSysApiLog(@RequestBody SysLogDTO log);

    /**
     * 报表统计
     * @param sysLogDTO 查询条件
     * @return Result 日志对象
     */
    @PostMapping(PREFIX + "countBySysApiLog")
    Result<List<SysLogDTO>> countBySysApiLog(@RequestBody SysLogDTO sysLogDTO);

    /**
     *  添加登录日志
     * @param sysLogDTO 日志obj
     * @return Result
     */
    @PostMapping(PREFIX +"addLogin")
    Result<Boolean> addLogin(@RequestBody SysLogLoginDTO sysLogDTO);
}
