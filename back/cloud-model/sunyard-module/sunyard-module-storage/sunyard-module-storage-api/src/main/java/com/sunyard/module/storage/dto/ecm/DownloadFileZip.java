package com.sunyard.module.storage.dto.ecm;

import lombok.Data;

import java.util.List;

/**
 * @author yzy
 * @desc
 * @since 2025/12/1
 */
@Data
public class DownloadFileZip {

    /**
     * 压缩包一级目录名
     */
    private String dirsFirst;


    /**
     * 文件列表
     */
    private List<DocFileZip> docFileList;

    /**
     * 登录名称
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
}
