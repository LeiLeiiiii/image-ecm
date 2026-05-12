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
 * 字典表
 * </p>
 *
 * @author raochangmei
 * @since 2022-07-07
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class SysDictionary implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    private String dicKey;

    private String dicVal;

    /**
     * 字典类型，0：目录  1：字典  2：字典数据
     * 只有新创建的字典才使用
     */
    private Integer dicType;

    private String remark;

    private Integer dicSequen;

    private Integer dicLevel;

    private Long parentId;

    private Integer systemCode;

    private String dicExtra;

    @TableLogic
    private Integer isDeleted;

    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;

}
