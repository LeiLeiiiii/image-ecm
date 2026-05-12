package com.sunyard.edm.mapper;

import java.util.Date;
import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sunyard.edm.dto.DocBsCollectionDTO;
import com.sunyard.edm.po.DocBsCollection;

/**
 * @Author PJW 2022/12/14 10:07
 */
public interface DocBsCollectionMapper extends BaseMapper<DocBsCollection> {

    /**
     * 查询收藏list
     *
     * @param userId
     * @param docIdList
     * @param userIdList
     * @param tagList
     * @param docName
     * @param owner
     * @param collectionTimeTo
     * @param collectionTimeDo
     * @param collectionTimeSort
     * @param updateTimeSort
     * @return
     */
    List<DocBsCollectionDTO> searchListExtend(@Param("userId") Long userId, @Param("docIdList") List<Long> docIdList, @Param("userIdList") List<Long> userIdList, @Param("tagList") List<Long> tagList,
                                              @Param("docName") String docName, @Param("owner") String owner, @Param("collectionTimeTo") Date collectionTimeTo, @Param("collectionTimeDo") Date collectionTimeDo,
                                              @Param("collectionTimeSort") String collectionTimeSort, @Param("updateTimeSort") String updateTimeSort);
}
