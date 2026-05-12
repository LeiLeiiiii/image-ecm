package com.sunyard.module.storage.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * @author： zyl
 * @Description：
 * @create： 2023/5/16 17:06
 */
@Data
public class FileSplitPdfVO implements Serializable {

    /**
     * newFileId
     */
    private String newFileId;

    /**
     * 文件名称
     */
    private String filename;

    /**
     * 业务批次号
     */
    private String busiBatchNo;

    /**
     * 设备id
     */
    private Long stEquipmentId;

    /**
     * 是否加密0否 1是
     */
    private Integer isEncrypt;

    private String token;

    /**
     * 拆分的图像数量
     */
    private Integer splitPageSize;

    /**
     * 拆分的页数
     */
    private Integer splitPageNum;

    /**
     * 文件密码
     */
    private String password;

    /**
     * 文件大小是否超阈值
     */
    private boolean isFileTooLarge;
    /**
     * 请求的图像页数
     */
    private String requestPage;
    /**
     * 文件md5
     */
    private String fileMd5;
}
