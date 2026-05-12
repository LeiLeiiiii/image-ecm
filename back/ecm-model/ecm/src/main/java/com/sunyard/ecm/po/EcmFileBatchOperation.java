package com.sunyard.ecm.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * 影像文件批量操作记录表
 * </p>
 *
 * @author zyl
 * @Description
 * @since 2023/9/19 16:43
 */
@Data
@EqualsAndHashCode(callSuper = false)
@ApiModel(value = "EcmFileBatchOperation对象", description = "影像文件批量操作记录表")
public class EcmFileBatchOperation {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主键")
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    @ApiModelProperty(value = "文件类型id")
    private Long fileId;

    @ApiModelProperty(value = "影像操作日志主键id")
    private Long ecmBusiLogId;
}
