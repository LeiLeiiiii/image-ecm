package com.sunyard.ecm.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author scm
 * @since 2023/8/10 14:05
 * @desc 文件历史DTO
 */
@Data
public class EcmFileHistoryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    private Long id;

    /**
     * 业务表主键
     */
    private Long busiId;

    /**
     * 主体id,文件id（第一份文件的文件id，即需要记录的此文件的生命周期）
     */
    private Long fileId;

    /**
     * 修改后的文件id
     */
    private Long newFileId;

    /**
     * 操作（0:旋转，1:压缩，2：重命名，3：删除）
     */
    private Integer fileOperation;

    /**
     * 创建人
     */
    private String createUser;


    private Date createTime;

}
