package com.sunyard.edm.dto;

import com.sunyard.edm.po.DocSysHouseUser;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * 文档库对象表
 * </p>
 *
 * @author wt
 * @since 2022-12-16
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class DocSysHouseUserDTO extends DocSysHouseUser {

    /**
	 * 关联中文
	 */
    private String relIdStr;

}
