package com.sunyard.ecm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sunyard.ecm.po.EcmFileComment;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;

/**
 * @author ty
 * @since 2023-12-15 9:43
 * @desc 文件评论接口
 */
public interface EcmFileCommentMapper extends BaseMapper<EcmFileComment> {

    /**
     * 根据文件ID批量删除
     * 注意：物理删除，慎用
     *
     * @param fileIds
     * @return
     */
    int deleteBatchByFileId(@Param("coll") Collection<Long> fileIds);
}
