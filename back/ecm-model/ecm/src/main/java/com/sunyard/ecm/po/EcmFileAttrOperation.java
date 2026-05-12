package com.sunyard.ecm.po;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 文件属性操作记录表（change_details以attr_id为键，按file_id查询优化）
 * </p>
 *
 * @author yzy
 * @since 2025-09-19
 */
@Getter
@Setter
@TableName("ecm_file_attr_operation")
public class EcmFileAttrOperation implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 记录ID（主键）
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 关联文件ID（核心查询字段）
     */
    private String fileId;

    /**
     * 操作人ID
     */
    private String operatorId;

    /**
     * 操作人姓名（冗余，避免联表）
     */
    private String operatorName;

    /**
     * 操作时间
     */
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    /**
     * 属性变动详情（JSON格式）：{	    "1": {  // attr_id为键	      "attr_name": "文件大小",	      "old_value": {"value": "100MB"},  // 可为null（新增时）	      "new_value": {"value": "200MB"},	      "op_type": 2  // 1-新增，2-修改	    },	    "2": {  // 另一个attr_id	      "attr_name": "文件格式",	      "old_value": {"value": "DOC"},	      "new_value": {"value": "PDF"},	      "op_type": 2	    }	  }
     */
    private String changeDetails;

    /**
     * 操作备注（如“批量修改文件属性”）
     */
    private String remark;

    /**
     * 属性删除详情
     */
    private String deleteDetails;

    /**
     * 属性新增详情
     */
    private String addDetails;
}
