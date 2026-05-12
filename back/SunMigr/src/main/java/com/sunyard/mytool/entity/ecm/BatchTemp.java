package com.sunyard.mytool.entity.ecm;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 迁移批次中间表实体类
 */
@Data
@TableName("B_BATCH_TEMP")
public class BatchTemp {

    /**
     * 业务表主键(自增)
     */
    @TableId(value = "ID", type = IdType.AUTO)
    private Long id;

    /**
     * 业务号
     */
    @TableField(value = "BUSI_NO")
    private String busiNo;

    /**
     * 业务扩展属性
     */
    @TableField(value = "BUSI_ATTR")
    private String busiAttr;

    /**
     * 业务类型code
     */
    @TableField(value = "APP_CODE")
    private String appCode;

    /**
     * 业务类型名称
     */
    @TableField(value = "APP_NAME")
    private String appName;

    /**
     * 批次号
     */
    @TableField(value = "BATCH_ID")
    private String batchId;

    /**
     * 批次版本
     */
    @TableField(value = "BATCH_VER")
    private String batchVer;

    /**
     * 树标志(0:静态树，1:动态树)
     */
    @TableField(value = "TREE_TYPE")
    private Integer treeType;

    /**
     * 机构号
     */
    @TableField(value = "ORG_CODE")
    private String orgCode;

    /**
     * 机构名称
     */
    @TableField(value = "ORG_NAME")
    private String orgName;

    /**
     * 插入时间
     */
    @TableField(value = "INSERT_TIME")
    private Date insertTime;

    /**
     * 迁移完成时间
     */
    @TableField(value = "MIG_TIME")
    private Date migTime;

    /**
     * 创建人code
     */
    @TableField(value = "CREATE_USER")
    private String createUser;

    /**
     * 创建人姓名
     */
    @TableField(value = "CREATE_NAME")
    private String createName;

    /**
     * 创建时间
     */
    @TableField(value = "CREATE_DATE")
    private Date createDate;

    /**
     * 修改人code
     */
    @TableField(value = "MOD_USER")
    private String modUser;

    /**
     * 修改人姓名
     */
    @TableField(value = "MOD_NAME")
    private String modName;

    /**
     * 修改时间
     */
    @TableField(value = "MOD_DATE")
    private Date modDate;

    /**
     * 用于重迁是否覆盖文件 0:不覆盖, 1: 覆盖(默认不覆盖0)
     */
    @TableField(value = "COVER")
    private Integer cover;

    /**
     * 迁移状态:0,待迁移,1:迁移中,2:迁移成功,-1:不可重复失败,-2:可重复失败
     */
    @TableField(value = "MIG_STATUS")
    private Integer migStatus;

    /**
     * 迁移失败原因
     */
    @TableField(value = "FAIL_REASON")
    private String failReason;
}
