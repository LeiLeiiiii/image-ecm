package com.sunyard.ecm.mapper;

import com.sunyard.ecm.po.EcmFileTagOperationHistory;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 文件标签操作历史记录表（支持批量新增/删除标签） Mapper 接口
 * </p>
 *
 * @author yzy
 * @since 2025-09-18
 */
@Mapper
public interface EcmFileTagOperationHistoryMapper extends BaseMapper<EcmFileTagOperationHistory> {

}
