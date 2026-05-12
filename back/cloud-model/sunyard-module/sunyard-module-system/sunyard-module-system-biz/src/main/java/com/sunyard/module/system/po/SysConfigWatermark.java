package com.sunyard.module.system.po;

import java.io.Serializable;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 *
 * </p>
 *
 * @author liugang
 * @since 2022-02-15
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class SysConfigWatermark implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 水印内容
     */
    private String content;

    /**
     * 透明度
     */
    private Integer transparency;

    /**
     * 字体
     */
    private String font;

    /**
     * 字体大小
     */
    private Integer fontSize;

    /**
     * 颜色
     */
    private String color;

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
    /**
     * 是否启用(否:0,是:1)
     */
    private Integer isActivate;
}
