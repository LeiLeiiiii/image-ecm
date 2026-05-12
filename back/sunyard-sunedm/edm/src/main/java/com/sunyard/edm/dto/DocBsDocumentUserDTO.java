package com.sunyard.edm.dto;

import com.sunyard.edm.po.DocBsDocumentUser;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author huronghao
 * @Type
 * @Desc
 * @date 2022-12-19 16:37
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class DocBsDocumentUserDTO extends DocBsDocumentUser {

    /**
	 * 对象名称
	 */
    private String relIdStr;

    /**
	 * 用户名
	 */
    private String userIdStr;

    /**
	 * 组织名
	 */
    private String deptIdStr;

    /**
	 * 机构名
	 */
    private String instIdStr;

    /**
	 * 团队名
	 */
    private String teamIdStr;

    /**
	 * 文档库id
	 */
    private Long houseId;

    /**
	 * 前端处理需要 关联的类型，0:用户、1:机构、2:部门、3:团队
	 */
    private Integer relType;
}
