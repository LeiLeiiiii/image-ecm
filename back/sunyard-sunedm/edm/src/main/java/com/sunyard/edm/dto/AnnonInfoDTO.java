package com.sunyard.edm.dto;

import com.github.pagehelper.PageInfo;
import com.sunyard.edm.po.DocSysAnnoun;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @Author PJW 2022/12/12 14:01
 * 公告详情扩展
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class AnnonInfoDTO {
    /**
	 * 公告信息
	 */
    private DocSysAnnoun docSysAnnoun;

    /**
	 * 关联列表
	 */
    private PageInfo<DocSysAnnounUserDTO> docSysAnnounUserExtendPageInfo;

    /**
	 * 机构数量
	 */
    private Integer instNum;

    /**
	 * 部门数量
	 */
    private Integer deptNum;

    /**
	 * 团队数量
	 */
    private Integer teamNum;

    /**
	 * 用户数量
	 */
    private Integer userNum;

}
