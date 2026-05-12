package com.sunyard.module.storage.vo;

import lombok.Data;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import java.util.List;
import java.util.Map;

/**
 * @Author 朱山成
 * @time 2024/4/2 15:00
 **/
@Data
@XmlAccessorType(XmlAccessType.FIELD)
public class BackEcmRequestVo {
    /**
     * 批次号  档案id
     */
    private String batchId;

    /**
     * 批次号
     */
    private String contentId;
    /**
     * 文件路径 文件路径  batchId  下路径  /batchId/filePath
     */
    private String filePath;


    /**
     * 开始日期 年月日
     */
    private String startDate;

    private List<Map<String, String>> map;

    private List<UploadListVO> files;
}
