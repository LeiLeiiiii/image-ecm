package com.sunyard.edm.dto;

import com.sunyard.edm.po.DocSysTag;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.List;

/**
 * @author huronghao
 * @Type
 * @Desc
 * @date 2022-12-12 15:54
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class DocSysTagDTO extends DocSysTag implements Serializable {

    /**
	 * 子类
	 */
    private List<DocSysTagDTO> children;

    /**
	 * 是否存在子类标签
	 */
    private Integer isChildren;

    /**
	 * 父级标签名
	 */
    private String parentTagName;

    /**
	 * 前端使用的名称
	 */
    private String label;

    /**
	 * 前端使用的值
	 */
    private String value;
}
