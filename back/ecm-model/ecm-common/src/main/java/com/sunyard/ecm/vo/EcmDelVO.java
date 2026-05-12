package com.sunyard.ecm.vo;

import com.sunyard.ecm.dto.EcmBaseInfoDTO;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author scm
 * @since 2023/8/9 15:11
 * @desc 业务类型定义VO
 */
@Data
public class EcmDelVO implements Serializable {
    private static final long serialVersionUID = 1L;
    private EcmBaseInfoDTO ecmBaseInfoDTO;
    /**
     * 业务类型code
     */
    private String appCode;

    /**
     * 业务id
     */
    private String busiNo;

    /**
     * 资料节点
     */
    private List<String> docNo;

    /**
     * 文件id列表
     */
    private List<Long> fileIdList;


}

