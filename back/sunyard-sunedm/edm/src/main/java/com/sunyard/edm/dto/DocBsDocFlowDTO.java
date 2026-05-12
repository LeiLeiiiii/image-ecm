package com.sunyard.edm.dto;

import com.sunyard.edm.po.DocBsDocFlow;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * 文档流转表
 * </p>
 *
 * @author pjw
 * @since 2022-12-12
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class DocBsDocFlowDTO extends DocBsDocFlow {


    /**
	 * 流转名称  1:上传，2:上传审核，3:修改，4:下架，5:重新上架
	 */
    private String flowTypeStr;


    /**
	 * 操作人用户名
	 */
    private String userIdStr;
}
