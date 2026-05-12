package com.sunyard.edm.dto;

import com.github.pagehelper.PageInfo;
import com.sunyard.edm.po.DocSysTeam;
import com.sunyard.module.system.api.dto.SysOrgDTO;
import com.sunyard.module.system.api.dto.SysUserDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.List;

/**
 * <p>
 * 团队表
 * </p>
 *
 * @author pjw
 * @since 2022-12-12
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class UserTeamDeptDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
	 * 搜索条件
	 */
    String name;

    /**
	 * 类型，1：组织机构列表，3：团队列表，0：用户列表
	 */
    Integer type;

    /**
	 * 用户列表
	 */
    List<SysUserDTO> userList;
    /**
	 * 团队列表
	 */
    List<DocSysTeam> docSysTeams;
    /**
	 * 组织机构列表
	 */
    List<SysOrgDTO> sysOrgExtends;
    /**
	 * 用户列表分页
	 */
    private PageInfo<SysUserDTO> userPageInfo;
    /**
	 * 团队列表分页
	 */
    private PageInfo<DocSysTeamDTO> teamPageInfo;
}
