package com.sunyard.edm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sunyard.edm.dto.DocBsCompanyGroundingDTO;
import com.sunyard.edm.dto.DocBsDocumentDTO;
import com.sunyard.edm.dto.DocBsDocumentSearchDTO;
import com.sunyard.edm.dto.DocBsLevelFolderDTO;
import com.sunyard.edm.dto.ExtendPageDTO;
import com.sunyard.edm.po.DocBsDocument;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

/**
 * <p>
 * Mapper 接口
 * </p>
 *
 * @author raochangmei
 * @since 2021-12-14
 */
public interface DocBsDocumentMapper extends BaseMapper<DocBsDocument> {
    /**
     * 查询
     * @return
     */
    List<DocBsDocumentDTO> selectDocBsDocumentList(@Param("houseId") Long houseId,
                                                   @Param("relId1") Long relId1, @Param("type1") Integer type1,
                                                   @Param("relId2") Long relId2, @Param("type2") Integer type2,
                                                   @Param("relId3") Long relId3, @Param("type3") Integer type3,
                                                   @Param("teams") List<Long> teams, @Param("type4") Integer type4);

    /**
     * 查询
     *
     * @param extendPageDTO
     * @return
     */
    List<DocBsDocumentDTO> selectListExtend(@Param("extendPageDTO") ExtendPageDTO extendPageDTO);

    /**
     * 查询
     *
     * @return
     */
    List<DocBsDocumentDTO> selectDocBsDocumentListSort(@Param("houseId") Long houseId,
                                                       @Param("relId1") Long relId1, @Param("type1") Integer type1,
                                                       @Param("relId2") Long relId2, @Param("type2") Integer type2,
                                                       @Param("relId3") Long relId3, @Param("type3") Integer type3,
                                                       @Param("teams") List<Long> teams, @Param("type4") Integer type4);

    /**
     * 查询回收站数据
     *
     * @param searchDTO
     * @return
     */
    List<DocBsDocumentDTO> selectListByRecycle(@Param("searchDTO") DocBsDocumentSearchDTO searchDTO);

    /**
     * 查询回收站数据
     *
     * @param recycleIds
     * @return
     */
    List<DocBsDocumentDTO> selectListByrecycleIds(@Param("recycleIds") List<Long> recycleIds);

    /**
     * 查询回收站数据
     *
     * @param busIds
     * @return
     */
    List<DocBsDocumentDTO> selectListByBusIds(@Param("busIds") List<Long> busIds);


    /**
     * 文件夹详情
     *
     * @param BusiId
     * @return DocBsDocumentExtend
     */
    DocBsDocumentDTO selectFolderInfo(@Param("busId") Long BusiId);

    /**
     * 查询有权限的文件夹(全量)
     * @param houseId
     * @return DocBsDocumentExtend
     */
    List<DocBsDocumentDTO> selectAuthFolderAll(@Param("houseId") Long houseId);

    /**
     * 查询有权限的文件夹
     *
     * @return DocBsDocumentExtend
     */
    List<DocBsDocumentDTO> selectAuthFolderByBusIds(@Param("busId") List<Long> busId,@Param("houseId") Long houseId,
                                            @Param("relId1") Long relId1, @Param("type1") Integer type1,
                                            @Param("relId2") Long relId2, @Param("type2") Integer type2,
                                            @Param("relId3") Long relId3, @Param("type3") Integer type3,
                                            @Param("teams") List<Long> teams, @Param("type4") Integer type4);

    /**
     * 查询有权限的文件夹
     * @return DocBsDocumentExtend  recycleStatus
     */
    List<DocBsDocumentDTO> selectAuthFolder(@Param("houseId") Long houseId,@Param("recycleStatus") Integer recycleStatus,
                                            @Param("relId1") Long relId1, @Param("type1") Integer type1,
                                            @Param("relId2") Long relId2, @Param("type2") Integer type2,
                                            @Param("relId3") Long relId3, @Param("type3") Integer type3,
                                            @Param("teams") List<Long> teams, @Param("type4") Integer type4,
                                            @Param("permissType") List<Integer> permissType);


    /**
     * 分页所用的
     *
     * @param extendPageDTO
     * @return
     */
    List<DocBsDocumentDTO> selectListExtendPage(@Param("extendPageDTO") ExtendPageDTO extendPageDTO, @Param("searchType") String searchType );

    /**
     * 查询未上架列表
     *
     * @param userIdList
     * @param tagList
     * @return
     */
    List<DocBsCompanyGroundingDTO> queryGrounding(@Param("userIdList") List<Long> userIdList,
                                                  @Param("tagList") List<Long> tagList,
                                                  @Param("docStatus") Integer docStatus,
                                                  @Param("docName") String docName,
                                                  @Param("uploadTimeTo") Date uploadTimeTo,
                                                  @Param("uploadTimeDo") Date uploadTimeDo,
                                                  @Param("lowerTimeTo") Date lowerTimeTo,
                                                  @Param("lowerTimeDo") Date lowerTimeDo,
                                                  @Param("uploadTimeSort") String uploadTimeSort,
                                                  @Param("lowerTimeSort") String lowerTimeSort);

    /**
     * 查询文件夹的目录结构
     *
     * @param folderId
     * @return
     */
    DocBsLevelFolderDTO searchLevelFolder(@Param("folderId") Long folderId);
}
