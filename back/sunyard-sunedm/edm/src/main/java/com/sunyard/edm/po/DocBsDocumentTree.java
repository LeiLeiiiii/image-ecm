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
 * 文件夹层级的关联关系
 * </p>
 *
 * @author pjw
 * @since 2022-12-20
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class DocBsDocumentTree implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
	 * 主键
	 */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
	 * 本级文件夹
	 */
    private Long docId;

    /**
	 * 父级文件夹
	 */
    private Long fatherId;

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
