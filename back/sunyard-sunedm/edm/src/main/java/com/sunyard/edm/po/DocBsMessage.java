package com.sunyard.edm.po;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 * 消息通知记录表
 * </p>
 *
 * @author pjw
 * @since 2022-12-12
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class DocBsMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
	 * 消息id
	 */
    @TableId(value = "message_id", type = IdType.ASSIGN_ID)
    private Long messageId;

    /**
	 * 通知人id
	 */
    private Long userId;

    /**
	 * 阅读状态（0：未读    1：已读）
	 */
    private Integer messageStatus;

    /**
	 * 消息类型
	 */
    private Integer messageType;

    /**
	 * 消息标题
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
	 * 文档库（用于新文档上架提醒）
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

    /**
	 * 创建时间
	 */
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    /**
	 * 更新时间
	 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;

    /**
	 * 是否删除（0否 1是）
	 */
    @TableLogic
    private Integer isDeleted;


}
