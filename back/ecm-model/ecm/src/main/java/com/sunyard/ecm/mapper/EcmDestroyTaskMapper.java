package com.sunyard.ecm.mapper;

import com.sunyard.ecm.dto.ecm.EcmDestroyTaskDTO;
import com.sunyard.ecm.po.EcmDestroyTask;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 影像销毁任务表 Mapper 接口
 * </p>
 *
 * @author ypy
 * @since 2025-07-01
 */
@Mapper
public interface EcmDestroyTaskMapper extends BaseMapper<EcmDestroyTask> {

    /**
     * 销毁清册
     * @return
     */
    List<EcmDestroyTaskDTO> selectDestroyList(@Param("destroyTimeStart") String destroyTimeStart,
                                              @Param("destroyTimeEnd") String destroyTimeEnd,
                                              @Param("appCodes") List<String> appCodes,
                                              @Param("docCodes") List<String> docCodes,
                                              @Param("destroyType") Integer destroyType,
                                              @Param("busiNo") String busiNo);
}
