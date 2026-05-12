package com.sunyard.ecm.mapper;

import com.sunyard.ecm.po.EcmAsyncTask;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;

/**
 * <p>
 * 异步任务处理表 Mapper 接口
 * </p>
 *
 * @author yzl
 * @since 2025-03-03
 */
@Mapper
public interface EcmAsyncTaskMapper extends BaseMapper<EcmAsyncTask> {

    /**
     * 根据文件ID批量删除
     * 注意：物理删除，慎用
     *
     * @param fileIds
     * @return
     */
    int deleteBatchByFileId(@Param("coll") Collection<Long> fileIds);
}
