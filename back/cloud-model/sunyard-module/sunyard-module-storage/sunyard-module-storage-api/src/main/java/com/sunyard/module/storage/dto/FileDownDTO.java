package com.sunyard.module.storage.dto;

import lombok.Data;
import lombok.experimental.Accessors;


/**
 * @author zyl
 * @Description
 * @since 2024/3/20 9:25
 */
@Data
@Accessors(chain = true)
public class FileDownDTO {
    /**
     * 文件流
     */
    private Long fileId;

    /**
     * 用户名
     */
    private String userName;

    /**
     * 机构名
     */
    private String instName;

    /**
     * 用户电话
     */
    private String userPhone;

    /**
     * 机构电话
     */
    private String instPhone;

    /**
     * 文件密码
     */
    private String password;



}
