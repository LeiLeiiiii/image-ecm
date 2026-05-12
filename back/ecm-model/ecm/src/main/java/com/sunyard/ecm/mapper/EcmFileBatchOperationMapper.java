package com.sunyard.ecm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sunyard.ecm.po.EcmFileBatchOperation;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;

/**
 * @author ty
 * @since 2023-4-18 9:43
 * @desc 批量操作文件接口
 */
public interface EcmFileBatchOperationMapper extends BaseMapper<EcmFileBatchOperation> {

    /**
     *
     * @param list
     */
    void insertList(@Param("list") List list);

    /**
     * 根据文件ID批量删除
     * 注意：物理删除，慎用
     *
     * @param fileIds
     * @return
     */
    int deleteBatchByFileId(@Param("coll") Collection<Long> fileIds);
}
