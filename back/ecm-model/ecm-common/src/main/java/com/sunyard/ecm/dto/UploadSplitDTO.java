package com.sunyard.ecm.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.io.Serializable;

/**
 * @author scm
 * @since 2023/8/24 13:45
 * @desc 文件分片DTO
 */
@Data
public class UploadSplitDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 基本信息
     */
    @JacksonXmlProperty(localName = "BASE_DATA")
    @JsonProperty("ecmBaseInfoDTO")
    private EcmBaseInfoDTO ecmBaseInfoDTO;

    /**
     * 扩展信息（包含动态结构或者多维结构）
     */
    @JacksonXmlProperty(localName = "EXTEND")
    @JsonProperty("ecmBusExtendDTOS")
    private EcmBusExtendDTO ecmBusExtendDTO;

    /**
     * 上传的文件所在路径
     */
    private String path;

    /**
     * 上传文件
     */
    private MultipartFile file;
}
