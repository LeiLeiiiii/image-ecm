package com.sunyard.edm.vo;

import com.sunyard.edm.dto.DocBsDocumentUserDTO;
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
public class AddOrUpdateDocumentListVO {

    /**
     * 文档关联附件
     */
    List<AddOrUpdateDocumentVO> addOrUpdateDocumentVOS;
    /**
     * 标签ids
     */
    List<Long> tagIds;

    /**
     * 关联用户权限-保存
     */
    List<DocBsDocumentUserDTO> userTeamDeptListExtends;

    /**
     * 所有者
     */
    private Long docOwner;
}
