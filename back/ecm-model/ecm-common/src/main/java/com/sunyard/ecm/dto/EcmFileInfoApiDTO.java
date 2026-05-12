package com.sunyard.ecm.dto;

import com.sunyard.ecm.dto.split.SysFileApiDTO;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author： ty
 * @create： 2023/4/25 14:41
 * @desc：文件信息DTO
 */
@Data
public class EcmFileInfoApiDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 文件信息
     */
    private EcmFileInfoDTO ecmFileInfoDTO;


    /**
     * 基本信息
     */
    private AddBusiDTO addBusiDTO;


    /**
     * 文件信息
     */
    List<SysFileApiDTO> files;
}
