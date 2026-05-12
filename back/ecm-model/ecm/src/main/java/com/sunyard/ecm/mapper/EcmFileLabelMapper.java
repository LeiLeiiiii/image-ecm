package com.sunyard.ecm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sunyard.ecm.po.EcmFileLabel;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;

/**
 * 业务回收mapper
 *
 * @author wzz
 * @since 2024-6-6
 */
public interface EcmFileLabelMapper extends BaseMapper<EcmFileLabel> {


    /**
     * 根据文件ID批量删除
     * 注意：物理删除，慎用
     *
     * @param fileIds
     * @return
     */
    int deleteBatchByFileId(@Param("coll") Collection<Long> fileIds);
}
