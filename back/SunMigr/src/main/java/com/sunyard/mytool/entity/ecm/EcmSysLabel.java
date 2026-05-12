package com.sunyard.mytool.entity.ecm;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * ecm_sys_label - 影像标签定义实体类
 */
@Data
@TableName("ecm_sys_label")
public class EcmSysLabel implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 标签id
     */
    @TableId(value = "label_id", type = IdType.NONE)
    private Long labelId;

    /**
     * 标签名称
     */
    @TableField(value = "label_name")
    private String labelName;

    /**
     * 创建人
     */
    @TableField(value = "create_user")
    private String createUser;

    /**
     * 创建时间
     */
    @TableField(value = "create_time")
    private Date createTime;

    /**
     * 最新修改人
     */
    @TableField(value = "update_user")
    private String updateUser;

    /**
     * 更新时间
     */
    @TableField(value = "update_time")
    private Date updateTime;

    /**
     * 上级标签id
     */
    @TableField(value = "parent_id")
    private Long parentId;

    /**
     * 是否是最后层级(默认值为0，0：是，1：否)
     */
    @TableField(value = "last_level")
    private Integer lastLevel;
}
