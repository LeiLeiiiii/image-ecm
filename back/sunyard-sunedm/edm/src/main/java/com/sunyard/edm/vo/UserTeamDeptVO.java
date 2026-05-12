package com.sunyard.edm.vo;

import com.sunyard.edm.po.DocSysTag;
import lombok.Data;

import java.util.List;

/**
 * @Author PJW 2022/12/12 14:01
 * 用户组织vo
 */
@Data
public class UserTeamDeptVO {


    /**
	 * 关联标签
	 */
    List<DocSysTag> docSysTags;
    /**
	 * 关联用户id
	 */
    private Long relId;
    /**
	 * 文件夹id
	 */
    private Long folderId;


}
