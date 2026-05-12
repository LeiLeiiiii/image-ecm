package com.sunyard.edm.vo;

import com.sunyard.edm.dto.DocBsDocumentUserDTO;
import com.sunyard.edm.dto.UserTeamDeptDTO;
import com.sunyard.edm.po.DocBsDocument;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * @author huronghao
 * @Type
 * @Desc
 * @date 2022-12-13 17:30
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class AddOrUpdateDocumentVO extends DocBsDocument {

    /**
     * 文档关联附件
     */
    List<DocBsDocument> attchList;
    /**
     * 文档ids
     */
    List<Long> busIds;

    /**
     * 文档关联附件
     */
    List<Long> attchIds;

    /**
     * 标签ids
     */
    List<Long> tagIds;

    /**
     * 关联用户权限表
     */
    List<UserTeamDeptDTO> userList;

    /**
     * 关联用户权限-保存
     */
    List<DocBsDocumentUserDTO> userTeamDeptListExtends;

    /**
     * 关联文档-保存
     */
    List<Long> docIds;

    /**
     * 批量新增时，重名使用
     */
    List<String> fileOldName;

    /**
     * 批量新增时，重名使用
     */
    List<String> fileNewName;

    /**
     * 关联用户权限-权限关联ids
     */
    private List<Long> relIds;

    /**
     * 关联用户权限-权限关联类型
     */
    private List<Integer> types;

    /**
     * 关联用户权限-权限管理权限ids
     */
    private List<Integer> permissTypes;

    /**
     * 是否通知  0是  1否
     */
    private Integer isMsg;

}
