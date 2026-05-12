package com.sunyard.edm.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

/**
 * @Author PJW 2022/12/12 15:45
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class DocBsHomeDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
	 * 事项id
	 */
    private Long id;

    /**
	 * 内容id
	 */
    private Long contentId;

    /**
	 * 事项内容
	 */
    private String content;

    /**
	 * 仅‘最新消息0未读 ，1已读 ’,'分享中心0有效 1失效'模块才会有值
	 */
    private Integer isRead;

    /**
	 * 是否回收
	 */
    private Boolean isRecovery;

    /**
	 * 是否下架
	 */
    private Boolean isNoSelf;

    /**
	 * 时间
	 */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date time;

    /**
     * url拼接
     */
    private String messageContent;

    /**
     * url拼接需要
     */
    private String docFolder;

}
