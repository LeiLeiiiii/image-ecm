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
 * 文档关联的文档表
 * </p>
 *
 * @author pjw
 * @since 2022-12-20
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class DocBsDocRel implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
	 * 主键id
	 */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
	 * 关联文档id
	 */
    private Long docId;

    /**
	 * 关联id（文档id）
	 */
    private Long relId;

    /**
	 * 创建时间
	 */
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    /**
	 * 删除状态(否:0,是:1)
	 */
    @TableLogic
    private Integer isDeleted;


}
