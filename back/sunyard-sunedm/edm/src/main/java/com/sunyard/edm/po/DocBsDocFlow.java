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
 * 文档流转表
 * </p>
 *
 * @author pjw
 * @since 2022-12-14
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class DocBsDocFlow implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
	 * 主键id
	 */
    @TableId(value = "flow_id", type = IdType.ASSIGN_ID)
    private Long flowId;

    /**
	 * 关联文档id
	 */
    private Long docId;

    /**
	 * 流转名称  1:上传，2:上传审核，3:修改，4:下架，5:重新上架
	 */
    private Integer flowType;

    /**
	 * 流转描述
	 */
    private String flowDescribe;

    /**
	 * 操作人id
	 */
    private Long userId;

    /**
	 * 流转时间
	 */
    private Date flowDate;

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
