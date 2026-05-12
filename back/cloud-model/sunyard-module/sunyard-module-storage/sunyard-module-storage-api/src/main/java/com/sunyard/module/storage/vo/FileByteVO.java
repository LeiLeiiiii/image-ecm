package com.sunyard.module.storage.vo;

import lombok.Data;

/**
 * @author zyl
 * @Description
 * @since 2023/11/24 9:58
 */
@Data
public class FileByteVO {
    /**
     * 配置编号  /银行影像  batchId (arcId)
     */
    private Long equipmentId;

    /**
     * 文件id
     */
    private Long fileId;


    /**
     * 当前会话id  /银行影像   contentId
     */
    private String sessionId;

    /**
     * 登录名称   /银行影像uploadDate
     */
    private String username;

    /**
     * 用户name
     */
    private String name;

    /**
     * 机构code
     */
    private String orgCode;

    /**
     * 机构名称
     */
    private String orgName;

    /**
     * 是否有水印 0:无水印 1:有水印  空:无水印
     */
    private Integer openFlag;
    /**
     * 1 银行影像查询
     */
    private Integer type;

    /**
     * 文件密码
     */
    private String password;
}
