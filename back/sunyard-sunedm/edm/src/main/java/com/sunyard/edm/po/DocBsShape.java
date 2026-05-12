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
 * 分享表
 * </p>
 *
 * @author pjw
 * @since 2022-12-12
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class DocBsShape implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
	 * 分享id
	 */
    @TableId(value = "shape_id", type = IdType.ASSIGN_ID)
    private Long shapeId;

    /**
	 * 分享者id
	 */
    private Long shapeUserId;

    /**
	 * 分享类别（0内部 1外部）
	 */
    private Integer shapeType;

    /**
	 * 分享区间（0：3天 1：7天 2：永久）
	 */
    private Integer shapeSection;

    /**
	 * 分享预览次数
	 */
    private Integer shapePreview;

    /**
	 * 文档id
	 */
    private Long docId;

    /**
	 * 到期时间
	 */
    private Date invalidTime;

    /**
	 * 分享时间
	 */
    private Date shapeTime;

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
