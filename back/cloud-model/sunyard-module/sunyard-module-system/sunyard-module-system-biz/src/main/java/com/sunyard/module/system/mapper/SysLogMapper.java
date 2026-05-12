package com.sunyard.module.system.mapper;

import java.util.Date;
import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sunyard.module.system.api.dto.SysLogDTO;
import com.sunyard.module.system.api.dto.SysLogLoginDTO;
import com.sunyard.module.system.po.SysApiLog;
import com.sunyard.module.system.po.SysLog;
import com.sunyard.module.system.po.SysLogLogin;

/**
 * <p>
 * Mapper 接口
 * </p>
 *
 * @author zhouleibin
 * @since 2021-08-16
 */
public interface SysLogMapper extends BaseMapper<SysLog> {
    /**
     * 查询日志
     * 
     * @param log 日志obj
     * @return Result
     */
    List<SysLogDTO> search(@Param("log") SysLogDTO log);


    /**
     * 查询日志API
     *
     * @param log 日志obj
     * @return Result
     */
    List<SysLogDTO> searchList(@Param("log") SysLogDTO log);

    /**
     * 查询日志API
     *
     * @param ids 日志id
     * @return Result
     */
    List<SysLogDTO> searchListByIds(@Param("ids") Long[] ids);
    /**
     * 查询配置
     *
     * @param userIds 用户id集合
     * @param logsStartDate 开始时间
     * @param logsEndDate 结束时间
     * @param groupStr 报表统计
     * @return Result
     */
    List<SysLogDTO> searchByParams(
            @Param("userIds") List<Long> userIds,
            @Param("logsStartDate") Date logsStartDate,
            @Param("logsEndDate") Date logsEndDate,
            @Param("groupStr") String groupStr
    );

    /**
     * 保管统计
     * 
     * @param userIds 用户id集合
     * @param logsStartDate 开始时间
     * @param logsEndDate 结束时间
     * @param groupStr 报表统计
     * @return Result
     */
    List<SysLogDTO> countByDesc(
            @Param("userIds") List<Long> userIds,
            @Param("logsStartDate") Date logsStartDate,
            @Param("logsEndDate") Date logsEndDate,
            @Param("groupStr") String groupStr
    );

    /**
     * 查询该条件下 查询到的日志数量
     * @param log 日志obj
     * @return Result
     */
    Long selectCounts(@Param("log")SysLogDTO log);

    /**
     * 查询接口日志
     * @param ew 条件构造器
     * @param log 日志obj
     * @return Result
     */
    List<SysApiLog> selectSysApiLog(@Param("ew")Wrapper ew,@Param("log") SysLogDTO log);

    /**
     * 查询登录日志
     * @param ew 条件构造器
     * @param log 日志obj
     * @return Result
     */
    List<SysLogLogin> selectLogLogin(@Param("ew")Wrapper ew,@Param("log") SysLogLoginDTO log);
}
