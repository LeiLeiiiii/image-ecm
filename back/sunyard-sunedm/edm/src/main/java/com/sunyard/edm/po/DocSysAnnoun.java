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
 * 公告表
 * </p>
 *
 * @author pjw
 * @since 2022-12-12
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class DocSysAnnoun implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
	 * 公告id
	 */
    @TableId(value = "ananoun_id", type = IdType.ASSIGN_ID)
    private Long ananounId;

    /**
	 * 公告标题
	 */
    private String ananounTitle;

    /**
	 * 公告内容
	 */
    private String ananounContent;

    /**
	 * 公开状态 公开状态 0：未公开，1:公开
	 */
    private Integer status;

    /**
	 * 发布时间
	 */
    private Date releaseTime;

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
