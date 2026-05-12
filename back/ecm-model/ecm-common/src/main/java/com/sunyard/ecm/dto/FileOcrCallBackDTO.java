package com.sunyard.ecm.dto;

import lombok.Data;

import java.util.List;

/**
 *  ocr文本识别Dto
 */
@Data
public class FileOcrCallBackDTO {

    /**
     * 基本信息：操作人员基本信息
     */
    private EcmBaseInfoDTO ecmBaseInfoDTO;

    /**
     * 业务类型代码
     */
    private String appCode;

    /**
     * 业务主索引
     */
    private String BusiNo;

    /**
     * 文件列表
     */
    private List<FileOcrDTO> fileOcrDtos;
}
