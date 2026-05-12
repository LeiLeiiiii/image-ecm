package com.sunyard.edm.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sunyard.edm.dto.DocBsRecentlyDTO;
import com.sunyard.edm.po.DocBsRecentlyDocument;

/**
 * @Author PJW 2022/12/14 10:09
 */
public interface DocBsRecentlyDocumentMapper extends BaseMapper<DocBsRecentlyDocument> {

    /**
     * 查询最近打开记录
     *
     * @param userIdList
     * @param docIdList
     * @param userId
     * @return
     */
    List<DocBsRecentlyDTO> searchExtend(@Param("userIdList") List<Long> userIdList,
                                        @Param("docIdList") List<Long> docIdList,
                                        @Param("userId") Long userId);
}
