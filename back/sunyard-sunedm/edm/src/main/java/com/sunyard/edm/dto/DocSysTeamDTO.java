package com.sunyard.edm.dto;

import com.sunyard.edm.po.DocSysTeam;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * <p>
 * 公告表
 * </p>
 *
 * @author wt
 * @date 2022-12-12
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class DocSysTeamDTO extends DocSysTeam {

    private static final long serialVersionUID = 1L;

    /**
	 * 用户ID集
	 */
    private List<Long> userIds;

    /**
     * 机构或部门 文档用
     */
    /**
	 * relId
	 */
    private Long relId;

    /**
	 * 关联的类型，0:用户、1:机构、2:部门、3:团队
	 */
    private Integer relType;
}
