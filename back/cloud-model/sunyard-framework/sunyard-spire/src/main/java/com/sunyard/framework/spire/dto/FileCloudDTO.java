package com.sunyard.framework.spire.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * <p>
 *
 * </p>
 *
 * @author panjiazhu
 * @since 2022-07-12
 */
@Data
public class FileCloudDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 文件原始名称
     */
    private String originalFilename;

    /**
     * 文件扩展名
     */
    private String ext;

    /**
     * 文件访问地址(local)
     */
    private String url;

}
