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
 * 收藏表
 * </p>
 *
 * @author pjw
 * @since 2022-12-12
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class DocBsCollection implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
	 * 收藏id
	 */
    @TableId(value = "collection_id", type = IdType.ASSIGN_ID)
    private Long collectionId;

    /**
	 * 用户id
	 */
    private Long userId;

    /**
	 * 文档id
	 */
    private Long docId;

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
	 * 删除状态(否:0,是:1)
	 */
    @TableLogic
    private Integer isDeleted;


}
