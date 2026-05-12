package com.sunyard.framework.onlyoffice.dto.edit;

import java.io.Serializable;

import lombok.Data;

/**
 * 最近打开
 * @author PJW
 */
@Data
public class FwFileRecent implements Serializable {

    /**
     * 文件夹
     */
    private String folder;

    /**
     * 名称
     */
    private String title;

    /**
     * url 绝对路径
     */
    private String url;

}
