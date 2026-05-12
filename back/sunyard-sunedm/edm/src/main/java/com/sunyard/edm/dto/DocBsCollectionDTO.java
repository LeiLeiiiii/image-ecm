package com.sunyard.edm.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

/**
 * @Author PJW 2022/12/12 15:32
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class DocBsCollectionDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
	 * 收藏id
	 */
    private Long collectionId;

    /**
	 * 文件类型，文件夹还是文档
	 */
    private Integer type;

    /**
	 * 已收藏标记
	 */
    private Integer collectioned = 1;

    /**
	 * 文档id
	 */
    private Long docId;

    /**
	 * 文档名称
	 */
    private String docName;

    /**
	 * 文档类型后缀
	 */
    private String docSuffix;

    /**
	 * 文档的大小
	 */
    private String docSize;

    /**
	 * 文档的大小Str
	 */
    private String docSizeStr;

    /**
	 * 文件夹目录
	 */
    private String folder;

    /**
	 * 所有者
	 */
    private String docOwnerName;

    /**
	 * 更新时间
	 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;

    /**
	 * 收藏时间
	 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date collectionTime;

    /**
	 * 文档库类别 0企业 1个人 仅用于‘文件夹目录’跳转
	 */
    private Integer docLibraryType;

    /**
	 * 最子级id 仅用于‘文件夹目录’跳转
	 */
    private Long lastFolderId;

    /**
	 * 文档库id
	 */
    private Long documentHouseId;

    /**
	 * 是否置灰
	 */
    private Boolean isAsh;

    /**
	 * 是否回收
	 */
    private Boolean isRecovery;

    /**
	 * 是否下架
	 */
    private Boolean isNoSelf;

    /**
     * 是否删除
     */
    private Integer isDeleted;

}
