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
 * 公告发布对象表
 * </p>
 *
 * @author pjw
 * @since 2022-12-12
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class DocSysAnnounUser implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
	 * 主键id
	 */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
	 * 公告id
	 */
    private Long ananounId;

    /**
	 * 关联id
	 */
    private Long relId;

    /**
	 * 关联的类型，0:用户、1:机构、2:部门、3:团队
	 */
    private Integer type;

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
