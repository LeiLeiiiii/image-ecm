package com.sunyard.edm.dto;

import com.sunyard.edm.po.DocSysHouse;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * <p>
 * 文档库表
 * </p>
 *
 * @author wt
 * @since 2022-12-16
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class DocSysHouseDTO extends DocSysHouse {

    /**
	 * 关联用户权限-保存
	 */
    List<DocSysHouseUserDTO> userTeamDeptListExtends;

    /**
	 * 权限级别 权限，0:可查看，1:可编辑，2：可管理
	 */
    private Integer permissType;
}
