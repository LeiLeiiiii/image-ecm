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
 * 篡改检测记录表;
 * </p>
 *
 * @author pjw
 * @since 2024-04-07
 */
@Getter
@Setter
@TableName("afm_image_ps_note")
public class AfmImagePsNote implements Serializable {

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
    private Long exifId;

    /**
     * 文件名称
     */
    private String fileName;

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
     * 篡改检测时间
     */
    private Date psDetTime;

    /**
     * 篡改检测结果（0无篡改 1有篡改）
     */
    private Integer psDetResult;

    /**
     * 篡改处
     */
    private Integer psCount;

    /**
     * 含篡改区域文件id，无篡改则为空
     */
    private Long psDetFileId;

    /**
     * 更新时间
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
}
