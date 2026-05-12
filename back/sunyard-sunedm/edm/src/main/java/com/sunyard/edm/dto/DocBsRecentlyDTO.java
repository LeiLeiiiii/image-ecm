package com.sunyard.edm.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * @Author PJW 2022/12/13 15:55
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class DocBsRecentlyDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
	 * 文档id
	 */
    private Long docId;

    /**
	 * 文档名称
	 */
    private String docName;

    /**
	 * 文档后缀
	 */
    private String docSuffix;

    /**
	 * 文档库类型
	 */
    private Integer docLibraryType;

    /**
	 * 是否回收
	 */
    private Boolean isRecovery;

    /**
	 * 是否下架
	 */
    private Boolean isNoSelf;

    /**
	 * 文档父级目录id
	 */
    private Long lastFolderId;

    /**
	 * 是否置灰
	 */
    private Boolean isAsh;

}
