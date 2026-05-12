package com.sunyard.ecm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sunyard.ecm.dto.ecm.EcmFileInfoDTO;
import com.sunyard.ecm.po.EcmFileInfo;
import com.sunyard.ecm.vo.DocFileNumVO;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * @author ty
 * @since 2023-4-18 9:43
 * @desc 文件信息接口
 */
public interface EcmFileInfoMapper extends BaseMapper<EcmFileInfo> {
    /**
     *
     * @param list
     * @return
     */
//    int updateList(@Param("list") List list);

    /**
     * 批量插入
     * @param listEcmFile
     */
    void insertEcm(@Param("list") List<EcmFileInfo> listEcmFile);

    /**
     * 批量插入
     * @param listEcmFile
     */
    void updateBatch(@Param("list") List<EcmFileInfoDTO> listEcmFile);

    /**
     *
     */
    List<EcmFileInfoDTO> selectFileAndAppcode(@Param("createTimeStart") String createTimeStart,@Param("createTimeEnd") String createTimeEnd);

    /**
     * 根据文件ID批量删除
     * 注意：物理删除，慎用
     *
     * @param fileIds
     * @return
     */
    int deleteBatchByFileId(@Param("coll") Collection<Long> fileIds);


    List<EcmFileInfo> selectWithDeleteByBusiId(@Param("busiId") Set<Long> busiId);

    List<String> searchDupMd5List(@Param("pdfExt") String pdfExt,@Param("busiIds") List<Long> busiIds);

    List<DocFileNumVO> selectDocFileNums(@Param("busiId") Long busiId);
}
