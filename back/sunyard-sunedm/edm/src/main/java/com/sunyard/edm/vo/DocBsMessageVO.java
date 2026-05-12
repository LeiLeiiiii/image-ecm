package com.sunyard.edm.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

/**
 * @Author PJW 2022/12/19 15:43
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class DocBsMessageVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
	 * 通知人id
	 */
    private Long userId;

    /**
	 * 消息类型 0待审批提醒 1上架审批结果提醒 2申请审批结果提醒 3分享给我的提醒 4新文档上架提醒
	 */
    private Integer messageType;

    /**
     * 消息标题
     * 待审批提醒(上架待审批、申请待审批)
     * 上架审批结果提醒(上架未通过提醒、上架通过提醒)
     * 申请审批结果提醒(申请未通过提醒、申请通过提醒)
     * 分享给我的提醒(有文档分享给我)
     * 新文档上架提醒(有新的文档上架)
     *
     */
    private String messageTitle;

    /**
	 * 消息体
	 */
    private String messageContent;

    /**
	 * 文档目录（仅用于新文档上架提醒）
	 */
    private String docFolder;

    /**
	 * 文档库id（仅用于新文档上架提醒）
	 */
    private Long docHouseId;

    /**
	 * 文档父级目录id（仅用于新文档上架提醒）
	 */
    private Long docParentId;

    /**
	 * 通知时间
	 */
    private Date informTime;

}
