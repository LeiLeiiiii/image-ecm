package com.sunyard.edm.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

/**
 * @Author PJW 2022/12/27 14:10
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class DocBsCompanyGroundingDTO implements Serializable {

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
	 * 文档后缀名
	 */
    private String docSuffix;
    /**
	 * 文档大小
	 */
    private String docSize;
    /**
	 * 文档大小Str
	 */
    private String docSizeStr;
    /**
	 * 前端所用，是文件夹还是文档，分享默认都是文档
	 */
    private Integer type;
    /**
	 * 所有者
	 */
    private String owner;
    /**
	 * 审核状态
	 */
    private Integer approvalStatus;
    /**
	 * 上传时间
	 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date uploadTime;
    /**
	 * 下架时间
	 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date lowerTime;
    /**
	 * 是否置灰,判断是否可查看
	 */
    private Boolean isAsh;
    /**
	 * 是否置灰，判断是可进行操作
	 */
    private Boolean isSelect;
    /**
	 * 所有者
	 */
    private Long docOwner;
}
