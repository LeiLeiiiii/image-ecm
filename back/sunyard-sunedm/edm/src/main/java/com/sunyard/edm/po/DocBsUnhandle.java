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
 * 待办事项表
 * </p>
 *
 * @author pjw
 * @since 2022-12-12
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class DocBsUnhandle implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
	 * 待办id
	 */
    @TableId(value = "unhandle_id", type = IdType.ASSIGN_ID)
    private Long unhandleId;

    /**
	 * 发起时间
	 */
    private Date initiateTime;

    /**
	 * 发起人id
	 */
    private Long initiateUserId;

    /**
	 * 待办类型
	 */
    private Integer unhandleType;

    /**
	 * 处理人id
	 */
    private Long handleUserId;

    /**
	 * 申请记录id
	 */
    private Long applyId;

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
