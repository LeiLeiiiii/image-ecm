package com.sunyard.ecm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sunyard.ecm.po.EcmFileHistory;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;

/**
 * @author ty
 * @since 2023-7-18 9:43
 * @desc 文件历史接口
 */
public interface EcmFileHistoryMapper extends BaseMapper<EcmFileHistory> {
    /**
     * 批量插入
     * @param ecmFileHistoryList
     */
    void insertBatch(List<EcmFileHistory> ecmFileHistoryList);

    /**
     * 根据文件ID批量删除
     * 注意：物理删除，慎用
     *
     * @param fileIds
     * @return
     */
    int deleteBatchByFileId(@Param("coll") Collection<Long> fileIds);
}
