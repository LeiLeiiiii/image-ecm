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
 * 消息通知接收范围表
 * </p>
 *
 * @author pjw
 * @since 2022-12-12
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class DocBsMessageRange implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
	 * 范围id
	 */
    @TableId(value = "range_id", type = IdType.ASSIGN_ID)
    private Long rangeId;

    /**
	 * 范围key
	 */
    private Integer rangeKey;

    /**
	 * 通知人id
	 */
    private Long userId;

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
