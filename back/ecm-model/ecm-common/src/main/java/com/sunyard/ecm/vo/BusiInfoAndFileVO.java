package com.sunyard.ecm.vo;

import com.sunyard.ecm.dto.EcmBusiStructureTreeDTO;
import com.sunyard.ecm.dto.FileInfoRedisDTO;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author lw
 * @date 2023/4/26
 * @describe 业务和文件信息VO
 */
@Data
public class BusiInfoAndFileVO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 业务结构树信息
     */
    List<EcmBusiStructureTreeDTO> ecmBusiStructureTreeDTOList;

    /**
     * 业务文件信息
     */
    List<FileInfoRedisDTO> fileList;

    /**
     * 业务信息
     */
    BusiInfoVO busiInfoVO;


}
