package com.sunyard.ecm.dto;

import com.sunyard.ecm.dto.split.SysFileApiDTO;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author： ty
 * @create： 2023/4/25 14:41
 * @desc: 业务文件信息DTO
 */
@Data
public class EcmBusiFileInfoDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 业务关联文件信息
     */
    private EcmFileInfoDTO ecmFileInfoDTO;

    /**
     * 重复文件列表
     */
    private List<FileDTO> repeatFileMd5List;

    /**
     * 文件类型不符文件列表
     */
    private List<FileDTO> fileTypeNoRightList;

    /**
     * 文件类型符文件列表
     */
    private List<FileDTO> matchFileList;

    /**
     * 保存成功的文件
     */
    private List<SysFileApiDTO> saveFileSucc;

}
