package com.sunyard.module.storage.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sunyard.module.storage.dto.StFileDTO;
import com.sunyard.module.storage.po.StFile;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * Mapper 接口
 * </p>
 *
 * @author panjiazhu
 * @since 2022-07-11
 */
public interface StFileMapper extends BaseMapper<StFile> {


    /**
     * 获取文件表信息集合
     *
     * @param fileIdList 文件id
     * @return Result
     */
//    List<StFileDTO> selectListByIdSourt(@Param("fileIdList") List<Long> fileIdList);
    List<StFileDTO> selectListByIdSourt(
            @Param("fileIdList") List<Long> fileIdList,
            @Param("orderStr") String orderStr
    );

    /**
     * 批量插入
     *
     * @param list
     * @return Result
     */
    Integer insertBatch(@Param("list") List<StFileDTO> list);

    /**
     * 查询文件
     * @param id 资料Po
     * @return Result
     */
    StFileDTO selectFileDTO(Long id);

    /**
     * 查询文件
     * @param  stFile
     * @return Result
     */
    List<StFileDTO> selectFileDTOByPO(StFile stFile);

    /**
     * 批量查询文件
     * @param ids 资料Po
     * @return Result
     */
    List<StFileDTO> selectFileDTOByIds(@Param("ids") List<Long> ids);

    List<StFileDTO> selectFileDTOByIds1(@Param("ids") List<Long> ids);

    /**
     * 物理删除
     */
    void physicalDeleteById(@Param("id") Long id);

}
