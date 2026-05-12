package com.sunyard.ecm.dto.ecm;


import com.sunyard.ecm.po.EcmSysLabel;
import lombok.Data;


import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * <p>
 * 文件标签操作历史记录表（支持批量新增/删除标签）
 * </p>
 *
 * @author yzy
 * @since 2025-09-18
 */
@Data
public class EcmFileTagOperationHistoryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
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
    private List<EcmSysLabel> addTags;

    /**
     * 删除标签列表（JSON格式：[{"tag_id":"xxx","tag_name":"xxx"},...]）
     */
    private List<EcmSysLabel> deleteTags;

    /**
     * 操作创建时间
     */
    private Date createTime;

    /**
     * 备注
     */
    private String remark;

    /**
     * 标签ID
     */
    private List<String> labelIds;
}
