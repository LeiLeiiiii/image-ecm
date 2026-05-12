package com.sunyard.edm.dto;

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
 * 外部分享-外联关联表
 * </p>
 *
 * @author pjw
 * @since 2022-12-12
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class DocBsShapeOutsideLinkDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
	 * 链接id
	 */
    @TableId(value = "link_id", type = IdType.ASSIGN_ID)
    private Long linkId;

    /**
	 * 分享链接访问pwd
	 */
    private String linkPwd;

    /**
	 * 分享链接url
	 */
    private String linkUrl;

    /**
	 * 分享外链类别 （0公开 1密码）
	 */
    private Integer linkType;

    /**
	 * 分享id
	 */
    private Long shareId;

    /**
	 * 分享人
	 */
    private String shareUserName;

    /**
	 * 有效期
	 */
    private Integer shareSection;

    /**
	 * 有效期
	 */
    private String shareSectionStr;

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


}
