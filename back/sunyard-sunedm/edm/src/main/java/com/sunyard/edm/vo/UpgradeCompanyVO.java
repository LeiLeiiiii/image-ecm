package com.sunyard.edm.vo;

import com.sunyard.edm.dto.DocBsDocumentUserDTO;
import lombok.Data;

import java.util.List;

/**
 * @Author PJW 2022/12/12 14:01
 * <p>
 * 上架vo
 */
@Data
public class UpgradeCompanyVO {

    /**
	 * 关联标签
	 */
    List<Long> docSysTags;
    /**
	 * 关联用户权限表
	 */
    List<DocBsDocumentUserDTO> userList;
    /**
	 * 当前文档id
	 */
    private Long docId;
    /**
	 * 所属文档库id
	 */
    private Long houseId;
    /**
	 * 文件夹id
	 */
    private Long folderId;
    /**
	 * 是否通知  0是  1否
	 */
    private Integer isMsg;
}
