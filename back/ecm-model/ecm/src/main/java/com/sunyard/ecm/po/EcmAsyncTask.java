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
 * 异步任务处理表
 * </p>
 *
 * @author yzl
 * @since 2025-03-03
 */
@Data
@TableName("ecm_async_task")
public class EcmAsyncTask implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 业务ID
     */
    private Long busiId;

    /**
     * 异步任务类型000000六个字长,1位文档识别，2位自动转正，3模糊检测，4查重检测，5拆分合并，6翻拍检测	其中每位上 0表示无该类型，1表示处理中，2失败，3成功
     */
    private String taskType;

    /**
     * 文件ID
     */
    @TableId("file_id")
    private Long fileId;

    /**
     * 是否删除: 0-未删除, 1-已删除,不加TableLogic 物理删除
     */
    private Integer isDeleted;

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
     * 最后一次补偿时间
     */
    private Date lastCompensateTime;

    /**
     * 是否需要补偿,1补偿,0不需要
     */
    private Integer isCompensate;

    /**
     *  是否失败  0:重试中  1:成功 2:失败
     */
    private Integer isFail;

    /**
     * 重试次数 失败重试次数 大于5之后不进行重试
     */
    private Integer retryCount;
}
