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
 * es上传记录记录表
 * </p>
 *
 * @author pjw
 * @since 2023-02-17
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class DocUploadRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
	 * 主键
	 */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
	 * 文档id
	 */
    private Long busId;

    /**
	 * 索引
	 */
    private String indexName;

    /**
	 * 是否上传成功1成功0失败
	 */
    private Integer isSucceed;

    /**
	 * 异常信息
	 */
    private String exceptionMsg;

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
	 * 是否删除
	 */
    @TableLogic
    private Integer isDeleted;


}
