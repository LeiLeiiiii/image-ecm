package com.sunyard.edm.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @Author PJW 2022/12/12 15:45
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class DocCapacityDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
	 * 已使用容量+回收站容量
	 */
    private BigDecimal allUsedCapacity;

    /**
	 * 已使用容量
	 */
    private BigDecimal usedCapacity;

    /**
	 * 总容量
	 */
    private BigDecimal allCapacity;

    /**
	 * 回收站容量
	 */
    private BigDecimal recentlyCapacity;


    /**
	 * 回收站容量描述
	 */
    private String remark;
}
