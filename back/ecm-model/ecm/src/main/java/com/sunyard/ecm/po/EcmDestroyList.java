package com.sunyard.ecm.po;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;

import lombok.Data;

/**
 * <p>
 * 影像销毁清单表
 * </p>
 *
 * @author ypy
 * @since 2025-07-01
 */
@Data
@TableName("ecm_destroy_list")
public class EcmDestroyList implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 序号，自增主键，用于唯一标识每条记录
     */
    @TableId(value = "id")
    private Long id;

    /**
     * 销毁任务表id
     */
    private Long destroyTaskId;

    /**
     * 业务类型
     */
    private String appCode;

    /**
     * 资料类型
     */
    private String docCode;

    /**
     * 业务id
     */
    private Long busiId;

    /**
     * 业务编号
     */
    private String busiNo;

    /**
     * 机构号
     */
    private String orgCode;

    /**
     * 创建人
     */
    private String createUser;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    /**
     * 最近修改时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;

    /**
     * 业务对应文件数量
     */
    private Long fileCount;

    /**
     * 机构号名称
     */
    private String orgName;

    /**
     * 创建人名称
     */
    private String createUserName;
}
