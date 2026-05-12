package com.sunyard.edm.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author huronghao
 * @Type 提示返回
 * @Desc
 * @date 2022-12-20 16:05
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class PromptDTO {

    /**
	 * 标签数量
	 */
    private Integer tagNum;

    /**
	 * 文件夹数量
	 */
    private Integer folderNum;

    /**
	 * 是否关联文档 0没有 1有
	 */
    private Integer isRelDoc;

    /**
	 * 是否有文档 0没有 1有
	 */
    private Integer isHaveDoc;
}
