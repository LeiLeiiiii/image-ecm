package com.sunyard.edm.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * @Author PJW 2023/1/12 10:46
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class DocBsLevelFolderDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
	 * 文件夹id
	 */
    private Long docId;

    /**
	 * 文件夹层级目录名称
	 */
    private String folderName;
}
