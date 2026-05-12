package com.sunyard.edm.vo;

import com.sunyard.edm.dto.UserTeamDeptDTO;
import com.sunyard.edm.po.DocBsDocumentUser;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.List;

/**
 * @author huronghao
 * @Type
 * @Desc
 * @date 2022-12-13 17:30
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class DocBsDocumentVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
	 * 主键id
	 */
    private Long busId;

    /**
	 * 所属文档库id
	 */
    private Long houseId;

    /**
	 * 文件夹名
	 */
    private String docName;

    /**
	 * 上级文件夹id
	 */
    private Long parentId;

    /**
	 * 顺序号
	 */
    private Long docSeq;

    /**
	 * 传入关联用户权限表
	 */
    private List<DocBsDocumentUser> docBsDocumentUsers;

    /**
	 * 关联用户权限表
	 */
    private List<UserTeamDeptDTO> userList;
}
