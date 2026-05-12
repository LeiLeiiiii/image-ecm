package com.sunyard.ecm.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @author scm
 * @since 2023/8/9 15:11
 * @desc 文件信息VO
 */
@Data
public class FileInfoVO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 业务id
     */
    private Long busiId;

    /**
     * 文件id
     */
    private Long fileId;

    /**
     * 文件id列表
     */
    private List<Long> fileIdList;

    /**
     * 文件新名称
     */
    private String newName;

    /**
     * 当前用户id
     */
    private Long curentUserId;

    /**
     * 修改时间
     */
    private Date updateTime;

}

