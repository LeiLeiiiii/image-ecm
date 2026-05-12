package com.sunyard.edm.dto;

import com.github.pagehelper.PageInfo;
import com.sunyard.edm.po.DocSysHouse;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 文档库vo
 *
 * @author wt
 * @date 2022-12-12
 */

@Data
@EqualsAndHashCode(callSuper = false)
public class HouseInfoDTO {
    /**
	 * 文档库信息
	 */
    private DocSysHouse docSysHouse;

    /**
	 * 权限级别 权限，0:可查看，1:可编辑，2：可管理
	 */
    private Integer permissType;

    /**
	 * 关联列表
	 */
    private PageInfo<DocSysHouseUserDTO> docSysHouseUserExtendPageInfo;

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
