package com.sunyard.edm.dto;

import com.github.pagehelper.PageInfo;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * @author huronghao
 * @Type 提示返回
 * @Desc
 * @date 2022-12-20 16:05
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class UserTeamDeptInitDTO {

    /**
	 * 选中的数据列表
	 */
    private List<DocBsDocumentUserDTO> docBsDocumentUserDTOList;

    /**
	 * 选中的数据列表
	 */
    private PageInfo<DocBsDocumentUserDTO> pageInfo;

    /**
	 * 用户选中列表
	 */
    private List<Long> userList;

    /**
	 * 团队选中列表
	 */
    private List<Long> teamList;

    /**
	 * 组织机构选中列表
	 */
    private List<Long> instDeptList;
}
