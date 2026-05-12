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
 * 影像销毁任务表
 * </p>
 *
 * @author ypy
 * @since 2025-07-01
 */
@Data
@TableName("ecm_destroy_task")
public class EcmDestroyTask implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键id
     */
    @TableId(value = "id")
    private Long id;

    /**
     * 销毁类型(0:历史业务销毁;1:历史资料销毁;2:已删除销毁)
     */
    private Integer destroyType;

    /**
     * 业务类型
     */
    private String appCode;

    /**
     * 资料类型
     */
    private String docCode;

    /**
     * 业务创建日期，这里存的是区间，用字符串，若需拆分可设日期类型字段
     */
    private String busiCreateDate;

    /**
     * 机构号
     */
    private String orgCode;

    /**
     * 任务创建人
     */
    private String createUser;

    /**
     * 任务创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    /**
     * 任务状态(0:待审核;1:待销毁;2:审核不通过;3:已销毁;)
     */
    private Integer status;

    /**
     * 审核人
     */
    private String auditUser;

    /**
     * 审核时间
     */
    private Date auditTime;

    /**
     * 审核备注
     */
    private String auditNote;

    /**
     * 审核意见(1:同意销毁;2:拒绝销毁)
     */
    private Integer auditOpinion;

    /**
     * 销毁时间
     */
    private Date destroyTime;

    /**
     * 是否撤销(0;正常;1:已撤销)
     */
    private Integer isDelete;

    /**
     * 机构号名称
     */
    private String orgName;

    /**
     * 创建人名称
     */
    private String createUserName;

    /**
     * 审核人名称
     */
    private String auditUserName;
}
