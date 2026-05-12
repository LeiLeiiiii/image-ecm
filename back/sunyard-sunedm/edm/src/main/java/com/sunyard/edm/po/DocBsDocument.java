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
 * 文件夹、文档表
 * </p>
 *
 * @author pjw
 * @since 2022-12-14
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class DocBsDocument implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
	 * 主键id
	 */
    @TableId(value = "bus_id", type = IdType.ASSIGN_ID)
    private Long busId;

    /**
	 * 所属文档库id
	 */
    private Long houseId;

    /**
	 * 顺序号
	 */
    private Long docSeq;

    /**
	 * 类型：0:文件夹；1:文档；2:附件
	 */
    private Integer type;

    /**
	 * 层级
	 */
    private Integer folderLevel;

    /**
	 * 文件夹or文档名称
	 */
    private String docName;

    /**
	 * 文件or文档描述
	 */
    private String docDescribe;

    /**
	 * 附件关联文档id
	 */
    private Long relDoc;

    /**
	 * 文件后缀，例子：.doc/.pdf
	 */
    private String docSuffix;

    /**
	 * 文件夹或者文档的大小
	 */
    private Long docSize;

    /**
	 * 主文件上传的文件id
	 */
    private Long fileId;

    /**
	 * 文档状态，0:未上架，1:已上架
	 */
    private Integer docStatus;

    /**
	 * 类型：0:企业，1:个人
	 */
    private Integer docType;

    /**
	 * 所有者
	 */
    private Long docOwner;

    /**
	 * 创建人
	 */
    private Long docCreator;

    /**
	 * 回收截止时间
	 */
    private Date recycleDate;

    /**
	 * 回收状态 0:正常，1:已回收
	 */
    private Integer recycleStatus;

    /**
	 * 文件夹id
	 */
    private Long folderId;

    /**
	 * 上传时间
	 */
    private Date uploadTime;

    /**
	 * 下架时间
	 */
    private Date lowerTime;

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
	 * 直接父级id
	 */
    private Long parentId;


}
