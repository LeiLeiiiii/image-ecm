package com.sunyard.edm.dto;

import com.sunyard.edm.po.DocSysAnnoun;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * <p>
 * 公告表
 * </p>
 *
 * @author wt
 * @since 2022-12-16
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class DocSysAnnounDTO extends DocSysAnnoun {

    private static final long serialVersionUID = 1L;

    /**
	 * 关联用户权限-保存
	 */
    List<DocSysAnnounUserDTO> userTeamDeptListExtends;


}
