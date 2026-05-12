package com.sunyard.mytool.entity.ecm;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 * 影像单证类型定义表
 * </p>
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class EcmDtdDef implements Serializable {

    private static final long serialVersionUID = 1L;


    @TableId(value = "dtd_type_id", type = IdType.ASSIGN_ID)
    private Long dtdTypeId;


    private String dtdCode;


    private String dtdName;


    private Float dtdSort;


    private String createUser;


    @TableField(fill = FieldFill.INSERT)
    private Date createTime;


    private String updateUser;


    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;


}
