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
 * 标签表
 * </p>
 *
 * @author pjw
 * @since 2022-12-14
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class DocSysTag implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
	 * 主键id
	 */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
	 * 标签id 本级
	 */
    private Long tagId;

    /**
	 * 标签code
	 */
    private String tagCode;

    /**
	 * 标签name
	 */
    private String tagName;

    /**
	 * 标签的层级
	 */
    private Integer tagLevel;

    /**
	 * 同一层级排序
	 */
    private Long tagSequen;

    /**
	 * 备注
	 */
    private String remark;

    /**
	 * 父级id
	 */
    private Long parentId;

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
