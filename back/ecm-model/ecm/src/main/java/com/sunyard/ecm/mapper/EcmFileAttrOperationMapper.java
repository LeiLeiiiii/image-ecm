package com.sunyard.ecm.mapper;

import com.sunyard.ecm.po.EcmFileAttrOperation;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 文件属性操作记录表（change_details以attr_id为键，按file_id查询优化） Mapper 接口
 * </p>
 *
 * @author yzy
 * @since 2025-09-19
 */
@Mapper
public interface EcmFileAttrOperationMapper extends BaseMapper<EcmFileAttrOperation> {

}
