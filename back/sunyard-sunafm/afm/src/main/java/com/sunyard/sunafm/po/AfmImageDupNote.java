package com.sunyard.sunafm.po;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 * 图像查重记录表;
 * </p>
 *
 * @author pjw
 * @since 2024-04-08
 */
@Getter
@Setter
@TableName("afm_image_dup_note")
public class AfmImageDupNote implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 来源系统
     */
    private String sourceSys;

    /**
     * 业务类型
     */
    private String businessType;

    /**
     * 业务索引（主索引）
     */
    private String businessIndex;

    /**
     * 资料类型
     */
    private String materialType;

    /**
     * 文件id
     */
    private String exifIdOrMd5;

    /**
     * 查询相似度
     */
    private Double similarity;

    /**
     * 上传人登录名
     */
    private String uploadUserCode;

    /**
     * 上传人（姓名）
     */
    private String uploadUserName;

    /**
     * 上传机构
     */
    private String uploadOrg;

    /**
     * 图像查重时间
     */
    private Date imgDupTime;

    /**
     * 图像查重结果
     */
    private Double imgDupResult;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    /**
     * 修改时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;

    /**
     * 是否删除（0否 1是）
     */
    @TableLogic
    private Integer isDeleted;

    /**
     * 上传机构中文名称
     */
    private String uploadOrgName;

    /**
     * 文件名
     */
    private String fileName;
    /**
     * 查重记录类型  0：影像查重  1：文本查重
     */
    private int dupType;
}
