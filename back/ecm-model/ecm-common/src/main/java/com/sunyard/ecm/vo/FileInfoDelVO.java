package com.sunyard.ecm.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @author scm
 * @since 2023/9/1 11:19
 * @desc 文件删除VO
 */
@Data
public class FileInfoDelVO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 业务主索引
     */
    private String busiNo;

    /**
     * 业务代码
     */
    private String appCode;

    /**
     * 文件id列表
     */
    private List<Long> fileIdList;

    /**
     * 操作时间
     */
    private Date updateTime;
}
