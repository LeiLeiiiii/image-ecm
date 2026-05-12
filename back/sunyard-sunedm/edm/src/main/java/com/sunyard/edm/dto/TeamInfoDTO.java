package com.sunyard.edm.dto;

import com.github.pagehelper.PageInfo;
import com.sunyard.edm.po.DocSysTeam;
import com.sunyard.module.system.api.dto.SysUserDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 团队详情
 *
 * @author wt
 * @date 2022-12-12
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class TeamInfoDTO {

    /**
	 * 团队信息
	 */
    private DocSysTeam docSysTeam;

    /**
	 * 关联列表
	 */
    private PageInfo<SysUserDTO> docSysTeamUserExtendPageInfo;

    /**
	 * 用户数量
	 */
    private Long userNum;
}
