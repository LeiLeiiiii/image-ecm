package com.sunyard.ecm.dto.ecm;

import lombok.Data;

import java.util.Date;

/**
 * @author yzy
 * @desc
 * @since 2025/3/3
 */
@Data
public class EcmAsyncTaskDTO {

    /**
     * 业务ID
     */
    private Long busiId;

    /**
     * 异步任务状态1表示处理中，2失败，3成功
     */
    private String status;

    /**
     * 文件ID
     */
    private Long fileId;

    /**
     * 存储文件ID
     */
    private Long newFileId;

    /**
     * 文件名
     */
    private String fileName;

    /**
     * 是否删除: 0-未删除, 1-已删除
     */
    private Integer isDeleted;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 修改时间
     */
    private Date updateTime;
}
