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
 * 对外接口请求数据
 * </p>
 *
 * @author pjw
 * @since 2024-04-11
 */
@Getter
@Setter
@TableName("afm_api_data")
public class AfmApiData implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 队列处理状态，0未成功，1成功
     */
    private Integer status;

    /**
     * 推送参数
     */
    private String requestParams;
    /**
     * 重试次数
     */
    private Integer retryNum;

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
     * 推送参数
     */
    private String errorMsg;

    /**
     * 查查类型
     */
    private Integer type;


}
