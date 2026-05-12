package com.sunyard.ecm.po;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@TableName("ecm_es_async_task")
public class EcmEsAsyncTask implements Serializable {

    private static final long serialVersionUID = 1L;


    /**
     * 业务主键
     */
    @ApiModelProperty(value = "业务表主键")
    @TableId(value = "busi_id", type = IdType.ASSIGN_ID)
    private Long busiId;

    /**
     * 状态  0：未处理 1：成功 2：失败
     */
    @ApiModelProperty(value = "处理状态")
    private Integer status;

    /**
     * 重试次数
     */
    @ApiModelProperty(value = "重试次数")
    private Integer retryCount;

    /**
     * 重试记录
     */
    @ApiModelProperty(value = "重试记录")
    private String failReason;

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
     * 创建业务的tokenId
     */
    @ApiModelProperty(value = "tokenId")
    private Long tokenId;
}
