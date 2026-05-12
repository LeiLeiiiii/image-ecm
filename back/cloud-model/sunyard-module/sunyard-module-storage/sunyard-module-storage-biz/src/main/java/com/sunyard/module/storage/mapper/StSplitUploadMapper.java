package com.sunyard.module.storage.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sunyard.module.storage.po.StSplitUpload;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 * Mapper 接口
 * </p>
 *
 * @author yzy
 * @since 2024-11-04
 */
public interface StSplitUploadMapper extends BaseMapper<StSplitUpload> {

    /**
     * 物理删除
     */
    void physicalDeleteByFileId(@Param("fileId") Long fileId);
}
