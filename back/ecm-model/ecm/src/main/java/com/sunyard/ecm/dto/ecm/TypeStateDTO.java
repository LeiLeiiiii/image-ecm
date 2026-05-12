package com.sunyard.ecm.dto.ecm;

import lombok.Data;

/**
 * @author yzy
 * @desc
 * @since 2025/9/18
 */
@Data
public class TypeStateDTO {
    /**
     * 筛选类型
     */
    private Integer type;
    /**
     * 开关状态
     */
    private Integer state;
}
