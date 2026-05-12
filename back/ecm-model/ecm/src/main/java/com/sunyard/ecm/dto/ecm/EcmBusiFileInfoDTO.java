package com.sunyard.ecm.dto.ecm;

import com.sunyard.ecm.dto.FileDTO;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author： ty
 * @create： 2023/4/25 14:41
 * @Desc: 影像文件信息DTO类
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


}
