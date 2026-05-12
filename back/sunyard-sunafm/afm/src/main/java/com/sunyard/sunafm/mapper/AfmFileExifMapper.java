package com.sunyard.sunafm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sunyard.sunafm.dto.AfmDetOnlineImgDetDTO;
import com.sunyard.sunafm.po.AfmFileExif;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * Mapper 接口
 * </p>
 *
 * @author pjw
 * @since 2024-04-07
 */
public interface AfmFileExifMapper extends BaseMapper<AfmFileExif> {


    /**
     * 关联表查文件相似度
     *
     * @param exifId
     * @param fileSimilarity
     */
    List<AfmDetOnlineImgDetDTO> queryFileByNoteId(@Param("noteId") Long noteId,
                                                  @Param("fileSimilarity") Double fileSimilarity,
                                                  @Param("fileIndex") String fileIndex,
                                                  @Param("exifId") Long exifId,
                                                  @Param("type")Integer type);

}
