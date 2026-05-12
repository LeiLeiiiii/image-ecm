package com.sunyard.ecm.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * @author ypy
 * @since 2025/9/19 17:36
 * @Description 文件信息VO
 */
@Data
public class QueryBusiFileVO implements Serializable {


    private String fileId;
    /***原文件url**/
    private String filePath;

    private Boolean isDuplicate;


}
