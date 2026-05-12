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
 * 任务表
 * </p>
 *
 * @author pjw
 * @since 2022-12-15
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class DocBsTask implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
	 * 主键
	 */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
	 * 类型0:删除存储任务
	 */
    private Integer taskType;

    /**
	 * 关联id
	 */
    private Long relId;

    /**
	 * 执行状态0:未完成,1:完成，2:异常
	 */
    private Integer taskStatus;

    /**
	 * 处理异常日志
	 */
    private String errorMsg;

    /**
	 * 执行完成时间
	 */
    private Date taskTime;

    /**
	 * 创建时间
	 */
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    /**
	 * 是否删除（0否 1是）
	 */
    @TableLogic
    private Integer isDeleted;

    /**
	 * 更新时间
	 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;


}
