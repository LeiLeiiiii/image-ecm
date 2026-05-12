package com.sunyard.module.storage.vo;

import lombok.Data;

/**
 * @author： zyl
 * @create： 2023/4/24 17:10
 */
@Data
public class FileUpdateVo {
    /**
     * 文件唯一标识(MD5)
     */
    private Long fileId;

    /**
     * 文件名称
     */
    private Integer type;

    /**
     * 文件密码
     */
    private String password;
}
