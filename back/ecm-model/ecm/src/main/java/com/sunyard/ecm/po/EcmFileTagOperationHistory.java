package com.sunyard.ecm.po;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

import com.baomidou.mybatisplus.extension.handlers.FastjsonTypeHandler;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 文件标签操作历史记录表（支持批量新增/删除标签）
 * </p>
 *
 * @author yzy
 * @since 2025-09-18
 */
@Getter
@Setter
@TableName(value = "ecm_file_tag_operation_history",autoResultMap = true)
public class EcmFileTagOperationHistory implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
//    @TableId(value = "id", type = IdType.AUTO)
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 关联文件ID
     */
    private String fileId;

    /**
     * 操作人ID
     */
    private String operatorId;

    /**
     * 操作人姓名
     */
    private String operatorName;

    /**
     * 新增标签列表（JSON格式：[{"tag_id":"xxx","tag_name":"xxx"},...]）
     */
    @TableField(typeHandler = FastjsonTypeHandler.class)
    private List<EcmFileLabel> addTags;

    /**
     * 删除标签列表（JSON格式：[{"tag_id":"xxx","tag_name":"xxx"},...]）
     */
    @TableField(typeHandler = FastjsonTypeHandler.class)
    private List<EcmFileLabel> deleteTags;

    /**
     * 操作创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    /**
     * 备注
     */
    private String remark;
}
