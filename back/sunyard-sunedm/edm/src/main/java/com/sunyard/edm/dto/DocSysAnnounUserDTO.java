package com.sunyard.edm.dto;

import com.sunyard.edm.po.DocSysAnnounUser;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * 公告发布对象表
 * </p>
 *
 * @author wt
 * @since 2022-12-16
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class DocSysAnnounUserDTO extends DocSysAnnounUser {

    /**
	 * 关联id中文
	 */
    private String relIdStr;

}
