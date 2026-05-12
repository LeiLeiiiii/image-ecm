package com.sunyard.edm.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

/**
 * @Author PJW 2022/12/13 9:08
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class DocBsMessageDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
	 * 消息通知type
	 */
    private String type;

    /**
	 * 0未读 ，1已读
	 */
    private Integer isRead;

    /**
	 * 消息通知标题
	 */
    private String title;

    /**
	 * 消息通知内容；显示时拼接上url参数值
	 */
    private String content;

    /**
	 * 消息通知内容中的文档目录，‘新文档上架提醒’才会有值
	 */
    private String url;

    /**
	 * 文档库id
	 */
    private Long documentHouseId;

    /**
	 * 最子级id 仅用于跳转
	 */
    private Long lastFolderId;

    /**
	 * 时间
	 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date time;

    /**
	 * 消息id
	 */
    private Long id;

    /**
	 * 是否置灰,判断是否可查看
	 */
    private Boolean isAsh;
    /**
     * 所属文档库id
     */
    private Long houseId;
    /**
     *文件夹id
     */
    private Long docId;
}
