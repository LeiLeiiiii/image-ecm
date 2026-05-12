package com.sunyard.edm.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * @Author PJW 2022/12/12 14:25
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class DocBsShapeAcceptDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
	 * 对象名称
	 */
    private String acceptName;

    /**
	 * 对象Id
	 */
    private Long acceptId;

    /**
	 * 接受者类别（（0：用户，1：机构，2：部门，3：团队））
	 */
    private Integer acceptType;

}
