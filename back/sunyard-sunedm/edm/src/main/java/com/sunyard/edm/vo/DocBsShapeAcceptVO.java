package com.sunyard.edm.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * @Author PJW 2022/12/12 15:16
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class DocBsShapeAcceptVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
	 * 接受者类别（（0：用户，1：团队，2：部门，3：机构））
	 */
    private Integer acceptType;

    /**
	 * 接收者id
	 */
    private Long acceptId;
}
